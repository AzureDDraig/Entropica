package ddraig.net.entropica.block.biome8;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Rime-Cap Mushroom — frost-glowing crypt fungus native to 2.8 Barrow-Mound Tundra.
 * Bursts in a chilling spore cloud when trampled or carelessly broken without Shears/Silk Touch.
 */
public class RimeCapMushroomBlock extends Block {
    public static final MapCodec<RimeCapMushroomBlock> CODEC = simpleCodec(RimeCapMushroomBlock::new);
    protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 11.0D, 13.0D);

    public RimeCapMushroomBlock(Properties properties) {
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
        BlockState below = level.getBlockState(belowPos);
        return below.is(Blocks.SNOW_BLOCK)
                || below.is(Blocks.DIRT)
                || below.is(Blocks.COARSE_DIRT)
                || below.is(Blocks.PODZOL)
                || below.is(Blocks.MYCELIUM)
                || below.is(Blocks.STONE)
                || below.is(Blocks.DEEPSLATE)
                || below.is(BlockTags.DIRT)
                || (ModBlocks.NECROTIC_SOIL.isBound() && below.is(ModBlocks.NECROTIC_SOIL.get()))
                || (ModBlocks.BARROW_STONE_BRICKS.isBound() && below.is(ModBlocks.BARROW_STONE_BRICKS.get()))
                || (ModBlocks.CRACKED_BARROW_BRICKS.isBound() && below.is(ModBlocks.CRACKED_BARROW_BRICKS.get()))
                || (ModBlocks.MOSSY_BARROW_BRICKS.isBound() && below.is(ModBlocks.MOSSY_BARROW_BRICKS.get()))
                || (ModBlocks.BARROW_MOSS.isBound() && below.is(ModBlocks.BARROW_MOSS.get()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (!living.isAlive() || living.isSpectator() || living.isSteppingCarefully()) {
            return;
        }
        if (living.getType().is(EntityTypeTags.UNDEAD)) {
            return;
        }
        releaseSporeBurst(serverLevel, pos);
        serverLevel.destroyBlock(pos, false);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (level instanceof ServerLevel serverLevel) {
            boolean gentleHarvest = tool.is(Items.SHEARS) || hasSilkTouch(serverLevel, tool);
            if (!gentleHarvest && serverLevel.random.nextBoolean()) {
                releaseSporeBurst(serverLevel, pos);
            }
        }
    }

    public static void releaseSporeBurst(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(pos);
        level.playSound(null, pos, SoundEvents.PUFFER_FISH_BLOW_OUT, SoundSource.BLOCKS, 0.85F, 1.15F);
        level.playSound(null, pos, SoundEvents.SNOW_BREAK, SoundSource.BLOCKS, 0.7F, 1.25F);
        level.sendParticles(ParticleTypes.SNOWFLAKE,
                center.x, pos.getY() + 0.45D, center.z,
                22, 0.55D, 0.35D, 0.55D, 0.05D);
        level.sendParticles(new DustParticleOptions(0x38BDF8, 0.95F),
                center.x, pos.getY() + 0.45D, center.z,
                18, 0.55D, 0.35D, 0.55D, 0.04D);

        AABB area = new AABB(pos).inflate(3.0D);
        double maxDistSq = 3.0D * 3.0D + 1.0D;
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive()
                        && !e.isSpectator()
                        && !e.getType().is(EntityTypeTags.UNDEAD)
                        && !(e instanceof Player p && p.isCreative())
                        && e.position().distanceToSqr(center) <= maxDistSq);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
        }
    }

    private static boolean hasSilkTouch(ServerLevel level, ItemStack tool) {
        if (tool == null || tool.isEmpty()) {
            return false;
        }
        if (EnchantmentHelper.hasTag(tool, EnchantmentTags.PREVENTS_ICE_MELTING)) {
            return true;
        }
        return level.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(reg -> reg.get(Enchantments.SILK_TOUCH))
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, tool) > 0)
                .orElse(false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.35D + random.nextDouble() * 0.35D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            level.addParticle(new DustParticleOptions(0x67E8F9, 0.6F), x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
