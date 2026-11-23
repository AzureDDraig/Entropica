package ddraig.net.entropica.worldgen.structure;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModStructures;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * Structure 3: Auroral Wellspring (24 x 24 x 12 circular geothermal sanctuary anchored at sea level
 * Y=63 in {@code 2.2 Auroral Skerries}, featuring three concentric Smooth Quartz & Quartz Bricks
 * ring basins, central Auroral Lens & Vis Vitae Anchor, four copper-domed quartz arches, hidden
 * vapor-buff dispensers, inner thermal pool cleansing + Speed II / Jump Boost II exit blessing,
 * and 20-second rising aurora vapor clouds).
 */
public class AuroralWellspringStructure extends Structure {

    public static final MapCodec<AuroralWellspringStructure> CODEC =
            simpleCodec(AuroralWellspringStructure::new);

    private static final int SEA_LEVEL_ANCHOR_Y = 63;
    private static final double INNER_THERMAL_POOL_RADIUS_SQR = 5.0D * 5.0D;
    private static final double VAPOR_PULSE_RADIUS_SQR = 10.0D * 10.0D;
    private static final String TAG_WELLSPRING_BATHED = "EntropicaWellspringBathed";

    public AuroralWellspringStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int oceanFloorCenter = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ - 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ - 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ + 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ + 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int minFloor = Math.min(oceanFloorCenter, Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE)));
        int maxFloor = Math.max(oceanFloorCenter, Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE)));

        // Shallow archipelago seabed/islets (Y=48..69 -> sea-level Y=63) OR flat inland plains (e.g. 2.10 Radiant Glaze-Plains)
        int anchorY;
        if (minFloor >= 48 && maxFloor <= 69) {
            if (SEA_LEVEL_ANCHOR_Y + 14 >= context.heightAccessor().getMaxY()) {
                return Optional.empty();
            }
            anchorY = SEA_LEVEL_ANCHOR_Y;
        } else if (minFloor > 69 && maxFloor - minFloor <= 5 && maxFloor + 14 < context.heightAccessor().getMaxY()) {
            anchorY = oceanFloorCenter;
        } else {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, anchorY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new AuroralWellspringPiece(centerX, anchorY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.AURORAL_WELLSPRING.get();
    }

    /**
     * Evaluates the Auroral Wellspring's inner thermal spring cleansing, exit agility blessing
     * (Speed II + Jump Boost II for 30s), freeze prevention, and 20-second (400-tick) rising
     * aurora-vapor pulse (Regeneration I + Invisibility I for 10s).
     */
    public static void tickWellspringThermalEffects(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }

        ServerLevel serverLevel = player.level();
        long gameTime = serverLevel.getGameTime();
        boolean isPeriodicCheck = (player.tickCount > 0 && player.tickCount % 5 == 0);
        boolean isVaporPulseTick = (gameTime > 0L && gameTime % 400L == 0L);

        CompoundTag pData = EntityHelper.getPersistentData(player);
        boolean wasBathing = pData.getBooleanOr(TAG_WELLSPRING_BATHED, false);

        // Immediate exit check if player already bathed and stepped/leaped out of water
        if (wasBathing && !player.isInWater()) {
            pData.putBoolean(TAG_WELLSPRING_BATHED, false);
            applyThermalExitBlessing(serverLevel, player);
            wasBathing = false;
        }

        if (!isPeriodicCheck && !isVaporPulseTick && !wasBathing) {
            return;
        }

        var structureRegistryOpt = serverLevel.registryAccess().lookup(Registries.STRUCTURE);
        if (structureRegistryOpt.isEmpty()) {
            return;
        }
        var wellspringHolderOpt = structureRegistryOpt.get().get(ModStructures.AURORAL_WELLSPRING_KEY);
        if (wellspringHolderOpt.isEmpty()) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        StructureStart start = serverLevel.structureManager().getStructureWithPieceAt(playerPos, wellspringHolderOpt.get().value());
        if (!start.isValid()) {
            if (wasBathing) {
                pData.putBoolean(TAG_WELLSPRING_BATHED, false);
                applyThermalExitBlessing(serverLevel, player);
            }
            return;
        }

        AuroralWellspringPiece activePiece = null;
        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof AuroralWellspringPiece wp) {
                activePiece = wp;
                break;
            }
        }
        if (activePiece == null) {
            return;
        }

        double centerX = activePiece.getOriginX() + 0.5D;
        double centerY = activePiece.getBaseY() + 0.5D;
        double centerZ = activePiece.getOriginZ() + 0.5D;
        double dx = player.getX() - centerX;
        double dz = player.getZ() - centerZ;
        double horizDistSqr = dx * dx + dz * dz;

        // 1. Inner Thermal Pool Bathing & Exit Blessing
        if (player.isInWater() && horizDistSqr <= INNER_THERMAL_POOL_RADIUS_SQR) {
            player.removeEffect(MobEffects.SLOWNESS);
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.MINING_FATIGUE);
            if (player.getTicksFrozen() > 0) {
                player.setTicksFrozen(0);
            }
            pData.putBoolean(TAG_WELLSPRING_BATHED, true);
        } else if (wasBathing) {
            pData.putBoolean(TAG_WELLSPRING_BATHED, false);
            applyThermalExitBlessing(serverLevel, player);
        }

        // Keep inner thermal pool surface completely ice-free
        if (player.tickCount % 100 == 0) {
            keepInnerPoolUnfrozen(serverLevel, activePiece);
        }

        // 2. Every 400 ticks (20 seconds): rising aurora/steam vapor clouds & Regeneration I + Invisibility I within 10 blocks
        if (isVaporPulseTick) {
            double dy = player.getY() - centerY;
            double dist3dSqr = horizDistSqr + dy * dy;
            if (dist3dSqr <= VAPOR_PULSE_RADIUS_SQR) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, true, true, true));
            }

            if (activePiece.tryClaimVaporPulseTick(gameTime)) {
                emitGazeboVaporCloudPulse(serverLevel, activePiece);
            }
        }
    }

    private static void applyThermalExitBlessing(ServerLevel level, ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 600, 1, false, true, true));
        level.sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 0.9D, player.getZ(),
                12, 0.35D, 0.45D, 0.35D, 0.04D);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.7F, 1.25F);
    }

    private static void emitGazeboVaporCloudPulse(ServerLevel level, AuroralWellspringPiece piece) {
        double cx = piece.getOriginX() + 0.5D;
        double cy = piece.getBaseY() + 1.5D;
        double cz = piece.getOriginZ() + 0.5D;

        level.sendParticles(ParticleTypes.CLOUD, cx, cy, cz, 28, 3.2D, 2.0D, 3.2D, 0.03D);
        level.sendParticles(ParticleTypes.END_ROD, cx, cy + 1.5D, cz, 18, 2.8D, 2.2D, 2.8D, 0.025D);
        level.playSound(null, piece.getCoreLensPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.85F, 1.15F);
    }

    private static void keepInnerPoolUnfrozen(ServerLevel level, AuroralWellspringPiece piece) {
        int ox = piece.getOriginX();
        int waterY = piece.getBaseY() - 1;
        int oz = piece.getOriginZ();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (dx * dx + dz * dz > 18) {
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
