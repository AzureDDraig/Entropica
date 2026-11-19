package ddraig.net.entropica.worldgen.structure;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.biome1.GlacialLifeForceAnchorBlock;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Structure 1: Frost-Veiled Glacial Runic Spire (18 x 18 x 45 hexagonal runic blue-ice tower
 * with 3 packed-ice flying buttresses, Y+20 Frost Scriptorium, waterfall-concealed Runic Vault
 * trap basement, Y+42 Glacial Life-Force Anchor, and interior freezing/hypothermia hazard).
 */
public class FrostVeiledRunicSpireStructure extends Structure {

    public static final MapCodec<FrostVeiledRunicSpireStructure> CODEC =
            simpleCodec(FrostVeiledRunicSpireStructure::new);

    public static final int ANCHOR_WARD_RADIUS = 24;
    private static final double ANCHOR_WARD_RADIUS_SQR = ANCHOR_WARD_RADIUS * ANCHOR_WARD_RADIUS;

    public FrostVeiledRunicSpireStructure(StructureSettings settings) {
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

        // Reject deep open water columns; require a solid glacier shelf or cliff terrace above sea level
        if (oceanFloorY < 62 || surfaceY - oceanFloorY > 3) {
            return Optional.empty();
        }

        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 6, centerZ - 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 6, centerZ - 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 6, centerZ + 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 6, centerZ + 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int minCornerY = Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE));
        int maxCornerY = Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE));
        if (minCornerY < 58 || maxCornerY - minCornerY > 20) {
            return Optional.empty();
        }

        int baseY = Mth.clamp(Math.min(surfaceY, (minCornerY + maxCornerY) / 2), 63, 120);
        if (baseY + 48 >= context.heightAccessor().getMaxY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new FrostVeiledRunicSpirePiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.FROST_VEILED_GLACIAL_RUNIC_SPIRE.get();
    }

    /**
     * Evaluates the Spire's interior freezing/hypothermia hazard every 100 ticks (5 seconds)
     * for survival players standing inside a generated {@link FrostVeiledRunicSpireStructure}.
     */
    public static void tickSpireHypothermiaHazard(ServerPlayer player) {
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.tickCount <= 0 || player.tickCount % 100 != 0) {
            return;
        }

        ServerLevel serverLevel = player.level();
        var structureRegistryOpt = serverLevel.registryAccess().lookup(Registries.STRUCTURE);
        if (structureRegistryOpt.isEmpty()) {
            return;
        }
        var spireHolderOpt = structureRegistryOpt.get().get(ModStructures.FROST_VEILED_GLACIAL_RUNIC_SPIRE_KEY);
        if (spireHolderOpt.isEmpty()) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        StructureStart start = serverLevel.structureManager().getStructureWithPieceAt(playerPos, spireHolderOpt.get().value());
        if (!start.isValid()) {
            return;
        }

        if (GlacialLifeForceAnchorBlock.hasAegisFrostProtection(player)) {
            return;
        }

        if (isProtectedByActiveAnchor(serverLevel, playerPos, start)) {
            return;
        }

        player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze() + 40, player.getTicksFrozen() + 90));
        player.hurtServer(serverLevel, player.damageSources().freeze(), 2.0F);
    }

    private static boolean isProtectedByActiveAnchor(ServerLevel level, BlockPos center, StructureStart start) {
        Block anchorBlock = ModBlocks.GLACIAL_LIFE_FORCE_ANCHOR.get();

        // Fast O(1) check on the Spire's summit anchor position first
        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof FrostVeiledRunicSpirePiece spirePiece) {
                BlockPos summitAnchor = spirePiece.getSummitAnchorPos();
                if (summitAnchor.distSqr(center) <= ANCHOR_WARD_RADIUS_SQR && level.isLoaded(summitAnchor)) {
                    BlockState summitState = level.getBlockState(summitAnchor);
                    if (summitState.is(anchorBlock) && summitState.getValue(GlacialLifeForceAnchorBlock.ACTIVE)) {
                        return true;
                    }
                }
            }
        }

        // Section-palette filtered scan for any active Glacial Life-Force Anchor within 24 blocks
        int minX = center.getX() - ANCHOR_WARD_RADIUS;
        int maxX = center.getX() + ANCHOR_WARD_RADIUS;
        int minY = Math.max(level.getMinY(), center.getY() - ANCHOR_WARD_RADIUS);
        int maxY = Math.min(level.getMaxY() - 1, center.getY() + ANCHOR_WARD_RADIUS);
        int minZ = center.getZ() - ANCHOR_WARD_RADIUS;
        int maxZ = center.getZ() + ANCHOR_WARD_RADIUS;

        int minChunkX = SectionPos.blockToSectionCoord(minX);
        int maxChunkX = SectionPos.blockToSectionCoord(maxX);
        int minChunkZ = SectionPos.blockToSectionCoord(minZ);
        int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);
        int minSecY = SectionPos.blockToSectionCoord(minY);
        int maxSecY = SectionPos.blockToSectionCoord(maxY);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(cx, cz);
                for (int sy = minSecY; sy <= maxSecY; sy++) {
                    int sectionIndex = chunk.getSectionIndexFromSectionY(sy);
                    if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
                        continue;
                    }
                    LevelChunkSection section = chunk.getSection(sectionIndex);
                    if (section.hasOnlyAir() || !section.maybeHas(s -> s.is(anchorBlock) && s.getValue(GlacialLifeForceAnchorBlock.ACTIVE))) {
                        continue;
                    }

                    int x0 = Math.max(minX, cx << 4);
                    int x1 = Math.min(maxX, (cx << 4) + 15);
                    int y0 = Math.max(minY, sy << 4);
                    int y1 = Math.min(maxY, (sy << 4) + 15);
                    int z0 = Math.max(minZ, cz << 4);
                    int z1 = Math.min(maxZ, (cz << 4) + 15);

                    for (int bx = x0; bx <= x1; bx++) {
                        for (int by = y0; by <= y1; by++) {
                            for (int bz = z0; bz <= z1; bz++) {
                                cursor.set(bx, by, bz);
                                if (cursor.distSqr(center) <= ANCHOR_WARD_RADIUS_SQR) {
                                    BlockState state = section.getBlockState(bx & 15, by & 15, bz & 15);
                                    if (state.is(anchorBlock) && state.getValue(GlacialLifeForceAnchorBlock.ACTIVE)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
