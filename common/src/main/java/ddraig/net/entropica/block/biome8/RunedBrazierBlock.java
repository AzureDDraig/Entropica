package ddraig.net.entropica.block.biome8;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Runed Brazier — carved barrow-stone fire basin etched with warding runes.
 * While lit, warms players within 8 blocks and weakens nearby Undead.
 */
public class RunedBrazierBlock extends Block {
    public static final MapCodec<RunedBrazierBlock> CODEC = simpleCodec(RunedBrazierBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 3.0D, 13.0D),
            Block.box(5.0D, 3.0D, 5.0D, 11.0D, 8.0D, 11.0D),
            Block.box(2.0D, 8.0D, 2.0D, 14.0D, 14.0D, 14.0D)
    );

    public RunedBrazierBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && state.getValue(LIT)) {
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.scheduleTick(pos, this, 20);
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean lit = state.getValue(LIT);
        if (!lit && (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE))) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                BlockState litState = state.setValue(LIT, true);
                serverLevel.setBlock(pos, litState, 3);
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    serverLevel.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F,
                            serverLevel.random.nextFloat() * 0.4F + 0.8F);
                    EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                } else {
                    serverLevel.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F,
                            (serverLevel.random.nextFloat() - serverLevel.random.nextFloat()) * 0.2F + 1.0F);
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                }
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        pos.getX() + 0.5D, pos.getY() + 0.92D, pos.getZ() + 0.5D,
                        12, 0.2D, 0.15D, 0.2D, 0.03D);
                serverLevel.scheduleTick(pos, this, 20);
            }
            return InteractionResult.SUCCESS;
        }

        if (lit && (stack.is(ItemTags.SHOVELS) || stack.is(Items.WATER_BUCKET))) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                serverLevel.setBlock(pos, state.setValue(LIT, false), 3);
                serverLevel.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.85F, 1.0F);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.getX() + 0.5D, pos.getY() + 0.92D, pos.getZ() + 0.5D,
                        8, 0.2D, 0.15D, 0.2D, 0.02D);
                if (stack.is(ItemTags.SHOVELS)) {
                    EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        super.onProjectileHit(level, state, hit, projectile);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel
                && !state.getValue(LIT) && projectile.isOnFire()) {
            BlockPos pos = hit.getBlockPos();
            serverLevel.setBlock(pos, state.setValue(LIT, true), 3);
            serverLevel.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.9F, 1.0F);
            serverLevel.scheduleTick(pos, this, 20);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            pulseBrazierAura(level, pos);
            if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.scheduleTick(pos, this, 20);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        pulseBrazierAura(level, pos);
        level.scheduleTick(pos, this, 20);
    }

    private static void pulseBrazierAura(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        double maxDistSq = 8.0D * 8.0D;
        AABB box = new AABB(pos).inflate(8.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && !e.isSpectator() && e.position().distanceToSqr(center) <= maxDistSq);
        for (LivingEntity entity : entities) {
            if (entity instanceof Player player) {
                if (player.getTicksFrozen() > 0) {
                    player.setTicksFrozen(0);
                }
            } else if (entity.getType().is(EntityTypeTags.UNDEAD)) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (!state.getValue(LIT)) {
            return;
        }
        if (random.nextInt(10) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D,
                    SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    0.45F + random.nextFloat() * 0.25F, random.nextFloat() * 0.4F + 0.8F, false);
        }
        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
        double y = pos.getY() + 0.92D + random.nextDouble() * 0.08D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0D, 0.025D, 0.0D);
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.SMOKE, x, y + 0.05D, z, 0.0D, 0.035D, 0.0D);
        }
    }
}
