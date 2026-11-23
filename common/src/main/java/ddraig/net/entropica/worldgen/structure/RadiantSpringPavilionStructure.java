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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
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
 * Biome #10 ({@code 2.10 Radiant Glaze-Plains}) Core-Free Sanctuary:
 * {@code Radiant Spring Pavilion}, anchored on flat-ish inland terrain ({@code maxY - minY <= 6},
 * {@code anchorY >= 62}). Wading in its warm Vitae Spring fountain basin cleanses {@code POISON},
 * {@code WITHER}, {@code SLOWNESS}, {@code WEAKNESS}, and frost, grants {@code Regeneration I},
 * and gently mends {@code 1} durability on damaged worn armor or held items every {@code 40} ticks.
 */
public class RadiantSpringPavilionStructure extends Structure {

    public static final MapCodec<RadiantSpringPavilionStructure> CODEC =
            simpleCodec(RadiantSpringPavilionStructure::new);

    private static final double SPRING_BASIN_HORIZ_RADIUS_SQR = 7.5D * 7.5D;
    private static final double SPRING_BASIN_VERT_RANGE = 4.0D;

    private static final EquipmentSlot[] MENDABLE_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND
    };

    private static final Map<UUID, Long> LAST_PROCESSED_GAME_TIME = new ConcurrentHashMap<>();

    public RadiantSpringPavilionStructure(StructureSettings settings) {
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

        if (centerSurfaceY < 62 || centerFloorY < 61) {
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
        if (minOceanFloor < 60) {
            return Optional.empty();
        }

        int minY = Math.min(centerSurfaceY, Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE)));
        int maxY = Math.max(centerSurfaceY, Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE)));

        // Require flat-ish inland terrain (maxY - minY <= 6, anchorY >= 62)
        int anchorY = Math.max(62, (minY + maxY) / 2);
        if (minY < 61 || maxY - minY > 6) {
            return Optional.empty();
        }
        if (anchorY + 15 >= context.heightAccessor().getMaxY()
                || anchorY - 6 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, anchorY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new RadiantSpringPavilionPiece(centerX, anchorY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.RADIANT_SPRING_PAVILION.isPresent()) {
            return ModStructures.RADIANT_SPRING_PAVILION.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_spring_pavilion"));
    }

    /**
     * When a player wades in water within {@code 7.5} blocks horizontally and {@code 4} blocks
     * vertically of an active {@code RADIANT_SPRING_PAVILION} start's center, cleanses
     * {@code POISON}, {@code WITHER}, {@code SLOWNESS}, {@code WEAKNESS}, and {@code TicksFrozen},
     * grants {@code Regeneration I} ({@code 100} ticks), and every {@code 40} ticks mends {@code 1}
     * durability on damaged worn armor or held items with soft golden {@code END_ROD} sparkles.
     */
    public static void tickSpringPavilionEffects(ServerPlayer player) {
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

        boolean isMendPulseTick = ((gameTime + player.getId()) % 40L == 0L);
        boolean isPeriodicCleanseTick = (player.tickCount % 5 == 0);
        if (!isMendPulseTick && !isPeriodicCleanseTick) {
            return;
        }

        var structureRegistryOpt = serverLevel.registryAccess().lookup(Registries.STRUCTURE);
        if (structureRegistryOpt.isEmpty()) {
            return;
        }
        var pavilionHolderOpt = structureRegistryOpt.get().get(ModStructures.RADIANT_SPRING_PAVILION_KEY);
        if (pavilionHolderOpt.isEmpty()) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        StructureStart start = serverLevel.structureManager()
                .getStructureWithPieceAt(playerPos, pavilionHolderOpt.get().value());
        if (!start.isValid()) {
            return;
        }

        RadiantSpringPavilionPiece activePiece = null;
        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof RadiantSpringPavilionPiece rspp) {
                activePiece = rspp;
                break;
            }
        }
        if (activePiece == null) {
            return;
        }

        double centerX = activePiece.getOriginX() + 0.5D;
        double centerY = activePiece.getAnchorY() + 1.0D;
        double centerZ = activePiece.getOriginZ() + 0.5D;
        double dx = player.getX() - centerX;
        double dz = player.getZ() - centerZ;
        double dy = Math.abs(player.getY() - centerY);

        if (dx * dx + dz * dz > SPRING_BASIN_HORIZ_RADIUS_SQR || dy > SPRING_BASIN_VERT_RANGE) {
            return;
        }

        // 1. Cleanse poisons, debuffs, and frost + refresh soothing Regeneration I
        player.removeEffect(MobEffects.POISON);
        player.removeEffect(MobEffects.WITHER);
        player.removeEffect(MobEffects.SLOWNESS);
        player.removeEffect(MobEffects.WEAKNESS);
        if (player.getTicksFrozen() > 0) {
            player.setTicksFrozen(0);
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, true, true));

        // Keep basin water thawed if ever exposed to external frost
        if (player.tickCount % 100 == 0) {
            keepFountainBasinUnfrozen(serverLevel, activePiece);
        }

        // 2. Every 40 ticks: mend 1 durability on damaged worn armor or held items with golden END_ROD sparkles
        if (isMendPulseTick) {
            boolean mendedAny = false;
            for (EquipmentSlot slot : MENDABLE_SLOTS) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty() && stack.isDamageableItem() && stack.isDamaged()) {
                    stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
                    mendedAny = true;
                }
            }
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    player.getX(),
                    player.getY() + 0.75D,
                    player.getZ(),
                    mendedAny ? 8 : 4,
                    0.35D,
                    0.30D,
                    0.35D,
                    0.02D
            );
        }
    }

    private static void keepFountainBasinUnfrozen(ServerLevel level, RadiantSpringPavilionPiece piece) {
        int ox = piece.getOriginX();
        int waterY = piece.getAnchorY() + 1;
        int oz = piece.getOriginZ();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                if (dx * dx + dz * dz > 44) {
                    continue;
                }
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
