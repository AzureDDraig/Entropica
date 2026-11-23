package ddraig.net.entropica.worldgen.structure;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Biome #14 ({@code 2.14 Oasis Floes}) Core-Free Sanctuary:
 * {@code Amber-Willow Thermal Bathhouse} ({@code 22 x 22 x 18} open-air thermal bathhouse).
 *
 * <p>Contains strictly ZERO Shrine Cores or Anchors. Anchors cleanly onto sheltered inland
 * basin valleys and carved spring valleys ({@code groundY >= 62}).</p>
 */
public class AmberWillowBathhouseStructure extends Structure {

    public static final MapCodec<AmberWillowBathhouseStructure> CODEC =
            simpleCodec(AmberWillowBathhouseStructure::new);

    private static final ResourceLocation OASIS_RENEWAL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "oasis_renewal");

    private static final double POOL_HORIZ_RADIUS_SQR = 6.0D * 6.0D;
    private static final double POOL_VERT_RANGE = 3.5D;
    private static final Map<UUID, Long> LAST_PROCESSED_GAME_TIME = new ConcurrentHashMap<>();

    public AmberWillowBathhouseStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int centerSurfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int centerFloorY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        if (centerSurfaceY < 62 || centerFloorY < 60) {
            return Optional.empty();
        }

        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 9, centerZ - 9, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 9, centerZ - 9, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 9, centerZ + 9, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 9, centerZ + 9, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int fNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 9, centerZ - 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int fNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 9, centerZ - 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int fSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 9, centerZ + 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int fSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 9, centerZ + 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int minOceanFloor = Math.min(centerFloorY, Math.min(Math.min(fNW, fNE), Math.min(fSW, fSE)));
        if (minOceanFloor < 59) {
            return Optional.empty();
        }

        int minSurface = Math.min(centerSurfaceY, Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE)));
        int maxSurface = Math.max(centerSurfaceY, Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE)));
        if (minSurface < 60 || maxSurface - minSurface > 9) {
            return Optional.empty();
        }

        int groundY = Math.max(62, (minSurface + maxSurface) / 2);
        // dy = 2 inside the 18-block-tall piece sits flush at groundY; dy = 0..1 houses the warm pool bed & foundation
        int baseY = groundY - 2;

        if (baseY + 18 >= context.heightAccessor().getMaxY()
                || baseY - 8 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, groundY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new AmberWillowBathhousePiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.AMBER_WILLOW_BATHHOUSE.isPresent()) {
            return ModStructures.AMBER_WILLOW_BATHHOUSE.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_willow_bathhouse"));
    }

    /**
     * Soothing thermal bathhouse tick when a player wades in the central warm pool of an
     * {@code Amber-Willow Thermal Bathhouse}: clears freeze buildup and {@code SLOWNESS}, keeps
     * the bathhouse pool thawed, and grants gentle {@code Regeneration I} (plus
     * {@code entropica:oasis_renewal} if registered).
     */
    public static void tickBathhouseThermalEffects(ServerPlayer player) {
        if (player == null || !player.isAlive() || player.isSpectator() || !player.isInWater()) {
            return;
        }

        ServerLevel serverLevel = player.level();
        long gameTime = serverLevel.getGameTime();
        Long lastTick = LAST_PROCESSED_GAME_TIME.get(player.getUUID());
        if (lastTick != null && lastTick == gameTime) {
            return;
        }
        LAST_PROCESSED_GAME_TIME.put(player.getUUID(), gameTime);

        if ((gameTime + player.getId()) % 20L != 0L) {
            return;
        }

        var structureRegistryOpt = serverLevel.registryAccess().lookup(Registries.STRUCTURE);
        if (structureRegistryOpt.isEmpty()) {
            return;
        }
        var bathhouseHolderOpt = structureRegistryOpt.get().get(ModStructures.AMBER_WILLOW_BATHHOUSE_KEY);
        if (bathhouseHolderOpt.isEmpty()) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        StructureStart start = serverLevel.structureManager()
                .getStructureWithPieceAt(playerPos, bathhouseHolderOpt.get().value());
        if (!start.isValid()) {
            return;
        }

        AmberWillowBathhousePiece activePiece = null;
        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof AmberWillowBathhousePiece awbp) {
                activePiece = awbp;
                break;
            }
        }
        if (activePiece == null) {
            return;
        }

        double centerX = activePiece.getOriginX() + 0.5D;
        double centerY = activePiece.getBaseY() + 2.0D;
        double centerZ = activePiece.getOriginZ() + 0.5D;
        double dx = player.getX() - centerX;
        double dz = player.getZ() - centerZ;
        double dy = Math.abs(player.getY() - centerY);

        if (dx * dx + dz * dz > POOL_HORIZ_RADIUS_SQR || dy > POOL_VERT_RANGE) {
            return;
        }

        if (player.getTicksFrozen() > 0) {
            player.setTicksFrozen(0);
        }
        player.removeEffect(MobEffects.SLOWNESS);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, true, false, true));

        Optional<MobEffect> oasisRenewalOpt = BuiltInRegistries.MOB_EFFECT.getOptional(OASIS_RENEWAL_ID);
        if (oasisRenewalOpt.isPresent()) {
            player.addEffect(new MobEffectInstance(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(oasisRenewalOpt.get()),
                    200,
                    0,
                    true,
                    true,
                    true
            ));
        }

        if ((gameTime + player.getId()) % 60L == 0L) {
            keepBathhousePoolUnfrozen(serverLevel, activePiece);
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    player.getX(),
                    player.getY() + 0.55D,
                    player.getZ(),
                    3,
                    0.30D,
                    0.15D,
                    0.30D,
                    0.01D
            );
        }
    }

    private static void keepBathhousePoolUnfrozen(ServerLevel level, AmberWillowBathhousePiece piece) {
        int ox = piece.getOriginX();
        int waterY = piece.getBaseY() + 2;
        int oz = piece.getOriginZ();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
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
