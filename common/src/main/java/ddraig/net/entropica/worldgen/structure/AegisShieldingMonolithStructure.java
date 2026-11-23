package ddraig.net.entropica.worldgen.structure;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Structure 2: Aegis Shielding Monolith (15 x 15 x 22 triangular obsidian-and-copper defensive
 * sanctuary in {@code 2.4 Crystalline Pack-Ice}, featuring a stepped Obsidian / Crying Obsidian /
 * Aegis-Plated Stone dais, three 12-block outer Bismuth & Obsidian Pylon Towers, three magnetic
 * Copper arches cradling a floating Aegis Crystal octahedron, and a rear-stair-accessible
 * Under-Dais Maintenance Room & Bismuth-locked Inner Vault).
 */
public class AegisShieldingMonolithStructure extends Structure {

    public static final MapCodec<AegisShieldingMonolithStructure> CODEC =
            simpleCodec(AegisShieldingMonolithStructure::new);

    private static final double BEAM_EFFECT_RANGE_SQR = 38.0D * 38.0D;

    public AegisShieldingMonolithStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int oceanFloorY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        // Reject water/ocean columns; require solid inland snow/ice valley ground above sea level
        if (oceanFloorY < 62 || surfaceY - oceanFloorY > 2) {
            return Optional.empty();
        }

        int[][] sampleCorners = {{-6, -6}, {6, -6}, {-6, 6}, {6, 6}};
        int minCornerY = oceanFloorY;
        int maxCornerY = oceanFloorY;
        for (int[] corner : sampleCorners) {
            int cx = centerX + corner[0];
            int cz = centerZ + corner[1];
            int cornerSurface = context.chunkGenerator().getFirstOccupiedHeight(
                    cx, cz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            int cornerFloor = context.chunkGenerator().getFirstOccupiedHeight(
                    cx, cz, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
            if (cornerFloor < 62 || cornerSurface - cornerFloor > 2) {
                return Optional.empty();
            }
            minCornerY = Math.min(minCornerY, cornerFloor);
            maxCornerY = Math.max(maxCornerY, cornerFloor);
        }

        // Reject overly jagged cliff drops so the 15x15 dais seats cleanly into rolling snowfields
        if (maxCornerY - minCornerY > 14) {
            return Optional.empty();
        }

        int baseY = Mth.clamp(Math.min(surfaceY, (minCornerY + maxCornerY) / 2), 63, 180);
        if (baseY + 18 >= context.heightAccessor().getMaxY() || baseY - 8 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new AegisShieldingMonolithPiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.AEGIS_SHIELDING_MONOLITH.get();
    }

    /**
     * Emits subtle aurora-cyan / violet energy beams every 10 ticks from the 3 outer
     * Bismuth & Obsidian pylons toward the central floating Aegis Crystal octahedron
     * when a player is near a Monolith start.
     */
    public static void tickMonolithFieldEffects(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }

        ServerLevel serverLevel = player.level();
        long gameTime = serverLevel.getGameTime();
        if (gameTime <= 0L || gameTime % 10L != 0L) {
            return;
        }

        var structureRegistryOpt = serverLevel.registryAccess().lookup(Registries.STRUCTURE);
        if (structureRegistryOpt.isEmpty()) {
            return;
        }
        var monolithHolderOpt = structureRegistryOpt.get().get(ModStructures.AEGIS_SHIELDING_MONOLITH_KEY);
        if (monolithHolderOpt.isEmpty()) {
            return;
        }
        Structure monolithStructure = monolithHolderOpt.get().value();

        BlockPos playerPos = player.blockPosition();
        ChunkPos playerChunk = player.chunkPosition();
        Set<AegisShieldingMonolithPiece> nearbyPieces = new HashSet<>(2);

        // Fast direct check if player is inside the piece bounding box
        StructureStart directStart = serverLevel.structureManager().getStructureWithPieceAt(playerPos, monolithStructure);
        if (directStart.isValid()) {
            collectMonolithPieces(directStart, nearbyPieces);
        }

        // Also scan loaded 3x3 chunk neighborhood so beams render cleanly while approaching the Monolith
        for (int dcx = -1; dcx <= 1; dcx++) {
            for (int dcz = -1; dcz <= 1; dcz++) {
                int cx = playerChunk.x + dcx;
                int cz = playerChunk.z + dcz;
                if (!serverLevel.hasChunk(cx, cz)) {
                    continue;
                }
                List<StructureStart> starts = serverLevel.structureManager()
                        .startsForStructure(SectionPos.of(new ChunkPos(cx, cz), 0), monolithStructure);
                for (StructureStart st : starts) {
                    if (st != null && st.isValid()) {
                        collectMonolithPieces(st, nearbyPieces);
                    }
                }
            }
        }

        if (nearbyPieces.isEmpty()) {
            return;
        }

        for (AegisShieldingMonolithPiece piece : nearbyPieces) {
            BlockPos corePos = piece.getOctahedronCorePos();
            if (corePos.distToCenterSqr(player.position()) > BEAM_EFFECT_RANGE_SQR) {
                continue;
            }
            if (!piece.tryClaimBeamTick(gameTime)) {
                continue;
            }
            if (!serverLevel.isLoaded(corePos)) {
                continue;
            }

            BlockState coreState = serverLevel.getBlockState(corePos);
            if (coreState.isAir()) {
                continue;
            }

            double targetX = corePos.getX() + 0.5D;
            double targetY = corePos.getY() + 0.5D;
            double targetZ = corePos.getZ() + 0.5D;

            for (BlockPos pylonPos : piece.getOuterPylonCorePositions()) {
                if (!serverLevel.isLoaded(pylonPos)) {
                    continue;
                }
                BlockState pylonState = serverLevel.getBlockState(pylonPos);
                if (pylonState.isAir()) {
                    continue;
                }

                emitPylonToOctahedronBeam(
                        serverLevel,
                        pylonPos.getX() + 0.5D,
                        pylonPos.getY() + 0.5D,
                        pylonPos.getZ() + 0.5D,
                        targetX,
                        targetY,
                        targetZ
                );
            }
        }
    }

    private static void collectMonolithPieces(StructureStart start, Set<AegisShieldingMonolithPiece> out) {
        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof AegisShieldingMonolithPiece mp) {
                out.add(mp);
            }
        }
    }

    private static void emitPylonToOctahedronBeam(
            ServerLevel level,
            double x0, double y0, double z0,
            double x1, double y1, double z1
    ) {
        int segments = 8;
        for (int i = 1; i <= segments; i++) {
            double t = i / (double) (segments + 1);
            double px = Mth.lerp(t, x0, x1);
            double py = Mth.lerp(t, y0, y1);
            double pz = Mth.lerp(t, z0, z1);

            if ((i & 1) == 0) {
                level.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0.03D, 0.03D, 0.03D, 0.0D);
            } else {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 1, 0.04D, 0.04D, 0.04D, 0.01D);
            }
        }
    }
}
