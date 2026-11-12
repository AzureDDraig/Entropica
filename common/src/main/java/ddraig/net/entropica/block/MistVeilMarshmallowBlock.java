package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MistVeilMarshmallowBlock extends EntropicaFlowerBlock {
    protected static final VoxelShape SHAPE = box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0);

    public MistVeilMarshmallowBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || 
               state.is(Blocks.WATER) || 
               state.is(Blocks.CLAY) || 
               state.is(Blocks.MUD);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        
        // Soft Bouncy Pad Mechanics
        entity.resetFallDistance();
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y < 0) {
            entity.setDeltaMovement(motion.x, -motion.y * 0.4, motion.z);
        }
    }

    private static final java.util.Map<BlockPos, Long> HARVEST_COOLDOWNS = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    protected net.minecraft.world.InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (ddraig.net.entropica.util.FloraHarvestHelper.tryShearHarvest(level, pos, state, player, hand)) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        long now = level.getGameTime();
        Long lastHarvest = HARVEST_COOLDOWNS.get(pos);
        if (lastHarvest != null && (now - lastHarvest) < 6000L) {
            return InteractionResult.PASS;
        }
        HARVEST_COOLDOWNS.put(pos, now);

        if (!level.isClientSide()) {
            popResource(level, pos, new ItemStack(ModItems.MIST_VEIL_MARSHMALLOW_POD.get()));
            level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.5;
            double z = pos.getZ() + random.nextDouble();

            // Soft Teal & Pink Sweet Sap Sparkles (#14B8A6 / #F472B6)
            float r = 0.95f;
            float g = 0.44f;
            float b = 0.71f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
