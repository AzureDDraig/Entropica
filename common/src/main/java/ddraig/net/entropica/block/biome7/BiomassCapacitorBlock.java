package ddraig.net.entropica.block.biome7;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Biomass Capacitor — stepped 3D bio-galvanic redstone delay bank.
 * Stores incoming Redstone or lightning pulses for a selectable delay
 * (0.5s, 1.0s, 2.0s, or 4.0s) before discharging a 10-tick Level 15 pulse.
 */
public class BiomassCapacitorBlock extends Block {
    public static final MapCodec<BiomassCapacitorBlock> CODEC = simpleCodec(BiomassCapacitorBlock::new);

    public static final BooleanProperty CHARGED = BooleanProperty.create("charged");
    public static final BooleanProperty DISCHARGING = BooleanProperty.create("discharging");
    public static final IntegerProperty DELAY_STAGE = IntegerProperty.create("delay_stage", 0, 3);

    private static final int DISCHARGE_PULSE_TICKS = 10;
    private static final Map<GlobalPos, Long> LAST_DISCHARGE_END_TICK = new ConcurrentHashMap<>();

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 3.0D, 15.0D),
            Block.box(3.0D, 3.0D, 3.0D, 13.0D, 12.0D, 13.0D),
            Block.box(2.0D, 12.0D, 2.0D, 14.0D, 15.0D, 14.0D),
            Block.box(5.0D, 15.0D, 5.0D, 11.0D, 16.0D, 11.0D)
    );

    public BiomassCapacitorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(CHARGED, false)
                .setValue(DISCHARGING, false)
                .setValue(DELAY_STAGE, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGED, DISCHARGING, DELAY_STAGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static int delayTicks(int stage) {
        return switch (stage) {
            case 1 -> 20;
            case 2 -> 40;
            case 3 -> 80;
            default -> 10;
        };
    }

    public static String delayLabel(int stage) {
        return switch (stage) {
            case 1 -> "1.0s";
            case 2 -> "2.0s";
            case 3 -> "4.0s";
            default -> "0.5s";
        };
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hitResult) {
        int nextStage = (state.getValue(DELAY_STAGE) + 1) & 3;
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(DELAY_STAGE, nextStage), 3);
            level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS,
                    0.7F, 0.55F + nextStage * 0.15F);
            player.displayClientMessage(
                    Component.literal("Biomass Capacitor Delay: " + delayLabel(nextStage))
                            .withStyle(ChatFormatting.AQUA),
                    true
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && !oldState.is(this)) {
            tryBeginChargingFromSignal(serverLevel, pos, state);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Long endedAt = LAST_DISCHARGE_END_TICK.get(GlobalPos.of(serverLevel.dimension(), pos));
            if (endedAt != null && endedAt == serverLevel.getGameTime()) {
                return;
            }
            tryBeginChargingFromSignal(serverLevel, pos, state);
        }
    }

    private void tryBeginChargingFromSignal(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.getValue(CHARGED) && !state.getValue(DISCHARGING) && level.hasNeighborSignal(pos)) {
            int delay = delayTicks(state.getValue(DELAY_STAGE));
            level.setBlock(pos, state.setValue(CHARGED, true), 3);
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.5F, 1.45F);
            level.scheduleTick(pos, this, delay);
        }
    }

    public static void chargeFromLightning(ServerLevel level, BlockPos pos, BlockState state) {
        if (level == null || pos == null || !(state.getBlock() instanceof BiomassCapacitorBlock capacitor)) {
            return;
        }
        if (!state.getValue(CHARGED) && !state.getValue(DISCHARGING)) {
            int delay = delayTicks(state.getValue(DELAY_STAGE));
            level.setBlock(pos, state.setValue(CHARGED, true), 3);
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.9F, 1.3F);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                    16, 0.3D, 0.3D, 0.3D, 0.12D);
            level.scheduleTick(pos, capacitor, delay);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(CHARGED) && !state.getValue(DISCHARGING)) {
            BlockState dischargingState = state.setValue(CHARGED, false).setValue(DISCHARGING, true);
            level.setBlock(pos, dischargingState, 3);
            notifyAllSides(level, pos);

            level.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.BLOCKS, 0.35F, 1.8F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.85F, 1.55F);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5D, pos.getY() + 0.95D, pos.getZ() + 0.5D,
                    14, 0.28D, 0.25D, 0.28D, 0.1D);

            level.scheduleTick(pos, this, DISCHARGE_PULSE_TICKS);
        } else if (state.getValue(DISCHARGING)) {
            if (LAST_DISCHARGE_END_TICK.size() > 1024) {
                LAST_DISCHARGE_END_TICK.clear();
            }
            LAST_DISCHARGE_END_TICK.put(GlobalPos.of(level.dimension(), pos.immutable()), level.getGameTime());

            BlockState idleState = state.setValue(DISCHARGING, false);
            level.setBlock(pos, idleState, 3);
            notifyAllSides(level, pos);

            // Re-sample external input 2 ticks later so sustained power cycles cleanly without self-latching
            level.scheduleTick(pos, this, 2);
        } else {
            tryBeginChargingFromSignal(level, pos, state);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        LAST_DISCHARGE_END_TICK.remove(GlobalPos.of(level.dimension(), pos));
        if (!isMoving && state.getValue(DISCHARGING)) {
            notifyAllSides(level, pos);
        }
    }

    private void notifyAllSides(Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, this);
        for (Direction dir : Direction.values()) {
            level.updateNeighborsAt(pos.relative(dir), this);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(DISCHARGING) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(DISCHARGING) ? 15 : 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if ((state.getValue(CHARGED) || state.getValue(DISCHARGING)) && random.nextInt(3) == 0) {
            double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
            double y = pos.getY() + 0.95D;
            double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0D, 0.03D, 0.0D);
        }
    }
}
