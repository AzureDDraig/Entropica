package ddraig.net.entropica.block.biome3;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.item.biome3.CryoInsulatedArmorItem;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rotten Ice — decaying Blight-infused pack ice of the Frost-Bite Floes.
 * Poisons uninsulated travelers and shatters beneath non-sneaking entities unless wearing Cryo-Insulated Boots.
 */
public class RottenIceBlock extends HalfTransparentBlock {
    public static final MapCodec<RottenIceBlock> CODEC = simpleCodec(RottenIceBlock::new);

    private static final Direction[] WATER_CHECK_DIRECTIONS = new Direction[]{
            Direction.DOWN,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST
    };

    public RottenIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            if (!CryoInsulatedArmorItem.hasCryoBoots(living)) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
                if (!entity.isSteppingCarefully()) {
                    level.scheduleTick(pos, this, 2);
                }
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.9F, 0.85F + random.nextFloat() * 0.2F);
        level.sendParticles(ParticleTypes.ITEM_SLIME,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                10, 0.3D, 0.25D, 0.3D, 0.04D);
        level.sendParticles(ParticleTypes.ASH,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                8, 0.3D, 0.25D, 0.3D, 0.02D);

        if (ModItems.ROT_POWDER.isBound()) {
            int dropCount = 1 + random.nextInt(2);
            Block.popResource(level, pos, new ItemStack(ModItems.ROT_POWDER.get(), dropCount));
        }

        boolean replaceWithWater = pos.getY() <= 63 || hasAdjacentOrBelowWater(level, pos);
        level.setBlock(pos, replaceWithWater ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
    }

    private static boolean hasAdjacentOrBelowWater(ServerLevel level, BlockPos pos) {
        for (Direction dir : WATER_CHECK_DIRECTIONS) {
            if (level.getFluidState(pos.relative(dir)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }
}
