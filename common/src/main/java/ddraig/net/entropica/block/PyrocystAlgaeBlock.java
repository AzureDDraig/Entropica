package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PyrocystAlgaeBlock extends CarpetBlock {
    public static final BooleanProperty HAS_VESICLES = BooleanProperty.create("has_vesicles");

    public PyrocystAlgaeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_VESICLES, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_VESICLES);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        entity.clearFire();
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, true, true));
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(HAS_VESICLES)) {
            if (!level.isClientSide()) {
                Block.popResource(level, pos, new ItemStack(ModItems.PYROCYST_VESICLE.get(), 1 + level.random.nextInt(2)));
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.setBlock(pos, state.setValue(HAS_VESICLES, false), 3);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (!state.getValue(HAS_VESICLES) && random.nextInt(5) == 0) {
            level.setBlock(pos, state.setValue(HAS_VESICLES, true), 3);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.1 + random.nextDouble() * 0.2;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 0.93f, 0.26f, 0.26f);
        }
    }
}
