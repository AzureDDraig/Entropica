package ddraig.net.entropica.block.biome1;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Blast-resistant geometric silver bramble infused with Aegis Materia.
 * Slows non-sneaking living entities inside and deals prick damage to hostile monsters or unarmored entities.
 */
public class ShieldingBrambleBlock extends Block {
    public static final MapCodec<ShieldingBrambleBlock> CODEC = simpleCodec(ShieldingBrambleBlock::new);
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public ShieldingBrambleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.isFaceSturdy(level, belowPos, Direction.UP)
                || belowState.is(BlockTags.ICE)
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(Blocks.POWDER_SNOW)
                || belowState.is(this)
                || (ModBlocks.RUNIC_BLUE_ICE.isBound() && belowState.is(ModBlocks.RUNIC_BLUE_ICE.get()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!(entity instanceof LivingEntity living) || living.isSteppingCarefully()) {
            return;
        }

        entity.makeStuckInBlock(state, new Vec3(0.70D, 0.65D, 0.70D));

        if (level instanceof ServerLevel serverLevel) {
            if (living instanceof Enemy || living.getArmorValue() <= 0) {
                Vec3 movement = entity.isClientAuthoritative()
                        ? entity.getKnownMovement()
                        : entity.oldPosition().subtract(entity.position());
                if (Math.abs(movement.x()) >= 0.003D || Math.abs(movement.y()) >= 0.003D || Math.abs(movement.z()) >= 0.003D) {
                    living.hurtServer(serverLevel, serverLevel.damageSources().sweetBerryBush(), 1.0F);
                }
            }
        }
    }
}
