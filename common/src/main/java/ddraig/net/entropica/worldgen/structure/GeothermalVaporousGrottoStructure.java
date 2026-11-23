package ddraig.net.entropica.worldgen.structure;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Structure 4: Geothermal Vaporous Grotto ({@code 20 x 20 x 18} pressurized domed geothermal cavern
 * in {@code 2.6 Steaming Skerries}, {@code 3.22 Vaporous Alpine}, and {@code 3.31 Amber Pine-Vale},
 * featuring Deepslate Brick & Steaming Spring Stone walls under a Blue-Ice greenhouse skylight dome,
 * parallel steaming bath pools divided by an Amber-Wood bridge, the 3-Copper-Plate Steam Puzzle,
 * and a rear {@code steam_vent} beneath a 12-block chimney shaft leading up to an
 * upper Amber-Wood observation balcony).
 */
public class GeothermalVaporousGrottoStructure extends Structure {

    public static final MapCodec<GeothermalVaporousGrottoStructure> CODEC =
            simpleCodec(GeothermalVaporousGrottoStructure::new);

    public GeothermalVaporousGrottoStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int centerFloor = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int centerSurface = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ - 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ - 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ + 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ + 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int minFloor = Math.min(centerFloor, Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE)));
        int maxFloor = Math.max(centerFloor, Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE)));

        // Reject deep ocean floors (< 53) or extreme vertical cliff faces (> 18 block drop across 16x16)
        if (minFloor < 53 || maxFloor < 60 || maxFloor - minFloor > 18) {
            return Optional.empty();
        }

        int baseY = Mth.clamp(Math.max(63, Math.min(centerSurface, (minFloor + maxFloor) / 2)), 63, 160);
        if (baseY + 16 >= context.heightAccessor().getMaxY() || baseY - 5 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new GeothermalVaporousGrottoPiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.GEOTHERMAL_VAPOROUS_GROTTO.get();
    }

    /**
     * Keeps the grotto's twin thermal bath pools ice-free, thaws freezing players bathing in them,
     * and supports the rear geyser chimney's rising thermal updraft & Slow Falling drift.
     */
    public static void tickGrottoThermalEffects(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }

        ServerLevel serverLevel = player.level();
        if (player.tickCount % 5 != 0) {
            return;
        }

        var structureRegistryOpt = serverLevel.registryAccess().lookup(Registries.STRUCTURE);
        if (structureRegistryOpt.isEmpty()) {
            return;
        }
        var grottoHolderOpt = structureRegistryOpt.get().get(ModStructures.GEOTHERMAL_VAPOROUS_GROTTO_KEY);
        if (grottoHolderOpt.isEmpty()) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        StructureStart start = serverLevel.structureManager().getStructureWithPieceAt(playerPos, grottoHolderOpt.get().value());
        if (!start.isValid()) {
            return;
        }

        GeothermalVaporousGrottoPiece activePiece = null;
        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof GeothermalVaporousGrottoPiece gp) {
                activePiece = gp;
                break;
            }
        }
        if (activePiece == null) {
            return;
        }

        if (player.isInWater() && player.getTicksFrozen() > 0) {
            player.setTicksFrozen(0);
        }

        BlockPos geyserPos = activePiece.getGeyserAnchorPos();
        double dx = player.getX() - (geyserPos.getX() + 0.5D);
        double dz = player.getZ() - (geyserPos.getZ() + 0.5D);
        double dy = player.getY() - (geyserPos.getY() + 1.0D);
        if (dx * dx + dz * dz <= 0.85D && dy >= -0.5D && dy <= 11.5D) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, true, true, true));
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    geyserPos.getX() + 0.5D, player.getY() + 0.2D, geyserPos.getZ() + 0.5D,
                    4, 0.2D, 0.3D, 0.2D, 0.04D);
        }

        if (player.tickCount % 100 == 0) {
            keepBathsUnfrozen(serverLevel, activePiece);
        }
    }

    private static void keepBathsUnfrozen(ServerLevel level, GeothermalVaporousGrottoPiece piece) {
        int ox = piece.getOriginX();
        int waterY = piece.getBaseY() - 1;
        int oz = piece.getOriginZ();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -6; dx <= 6; dx++) {
            if (Math.abs(dx) <= 1) {
                continue;
            }
            for (int dz = -1; dz <= 5; dz++) {
                cursor.set(ox + dx, waterY, oz + dz);
                if (!level.isLoaded(cursor)) {
                    continue;
                }
                BlockState state = level.getBlockState(cursor);
                if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
                    level.setBlock(cursor, Blocks.WATER.defaultBlockState(), 3);
                }
            }
        }
    }
}
