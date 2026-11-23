package ddraig.net.entropica.block;

import ddraig.net.entropica.block.biome7.StaticChargeHelper;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CryoStaticShrubBlock extends EntropicaFlowerBlock {
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    private static final long SHEAR_NEEDLE_COOLDOWN_TICKS = 1200L;
    private static final Map<GlobalPos, Long> SHEAR_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Long> HARVEST_COOLDOWNS = new ConcurrentHashMap<>();

    public CryoStaticShrubBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        if (EntropicaFloraGroundHelper.isIceOrSnowLayer(state)) {
            return false;
        }
        if (super.mayPlaceOn(state, level, pos)
                || EntropicaFloraGroundHelper.isSolidSnowBlock(state)
                || state.is(net.minecraft.tags.BlockTags.DIRT)
                || (ModBlocks.BLUE_FROZEN_MOSS.isBound() && state.is(ModBlocks.BLUE_FROZEN_MOSS.get()))
                || (ModBlocks.NECROTIC_SOIL.isBound() && state.is(ModBlocks.NECROTIC_SOIL.get()))
                || (ModBlocks.VOID_SAND_TURF.isBound() && state.is(ModBlocks.VOID_SAND_TURF.get()))) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("copper");
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);

        // 1. Powder Snow Freezing Mechanics
        entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze(), entity.getTicksFrozen() + 5));

        // 2. Static Slowness I Stun + Periodic Static Discharge & Player Melee Charge
        if (entity instanceof LivingEntity living && !level.isClientSide() && level instanceof ServerLevel serverLevel) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, false, true));

            if (entity.tickCount % 20 == 0) {
                if (!StaticChargeHelper.isEntityGrounded(living)) {
                    living.hurtServer(serverLevel, serverLevel.damageSources().lightningBolt(), 1.0F);
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            living.getX(), living.getY() + 0.5D, living.getZ(),
                            5, 0.25D, 0.25D, 0.25D, 0.06D);
                }
                if (living instanceof Player player) {
                    StaticChargeHelper.grantShrubMeleeCharge(player);
                }
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.SHEARS) && ModItems.CHARGED_STATIC_NEEDLE.isBound()) {
            GlobalPos key = GlobalPos.of(level.dimension(), pos.immutable());
            long now = level.getGameTime();
            Long lastShear = SHEAR_COOLDOWNS.get(key);
            if (lastShear != null && now >= lastShear && (now - lastShear) < SHEAR_NEEDLE_COOLDOWN_TICKS) {
                return InteractionResult.PASS;
            }
            if (SHEAR_COOLDOWNS.size() > 2048) {
                SHEAR_COOLDOWNS.clear();
            }
            SHEAR_COOLDOWNS.put(key, now);

            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                int count = 1 + level.random.nextInt(2);
                popResource(level, pos, new ItemStack(ModItems.CHARGED_STATIC_NEEDLE.get(), count));
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, slot);
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.15F);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D,
                        8, 0.25D, 0.25D, 0.25D, 0.08D);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        long now = level.getGameTime();
        BlockPos immutablePos = pos.immutable();
        Long lastHarvest = HARVEST_COOLDOWNS.get(immutablePos);
        if (lastHarvest != null && now >= lastHarvest && (now - lastHarvest) < 6000L) {
            return InteractionResult.PASS;
        }
        if (HARVEST_COOLDOWNS.size() > 2048) {
            HARVEST_COOLDOWNS.clear();
        }
        HARVEST_COOLDOWNS.put(immutablePos, now);

        if (!level.isClientSide()) {
            popResource(level, pos, new ItemStack(ModItems.CRYO_STATIC_SHRUB_ITEM.get()));
            level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.8f, 1.2f);
        } else {
            RandomSource random = level.getRandom();
            for (int i = 0; i < 10; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
                double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;

                boolean isStatic = random.nextBoolean();
                float r = isStatic ? 0.66f : 0.01f;
                float g = isStatic ? 0.33f : 0.52f;
                float b = isStatic ? 0.97f : 0.78f;

                level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);

        if (level instanceof ServerLevel serverLevel) {
            boolean gentleHarvest = tool.is(Items.SHEARS) || hasSilkTouch(serverLevel, tool);
            if (gentleHarvest) {
                if (ModItems.CRYO_STATIC_SHRUB_ITEM.isBound()) {
                    popResource(serverLevel, pos, new ItemStack(ModItems.CRYO_STATIC_SHRUB_ITEM.get()));
                }
            } else {
                if (!player.isCreative()) {
                    player.hurtServer(serverLevel, serverLevel.damageSources().lightningBolt(), 4.0F);
                    serverLevel.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.BLOCKS, 0.45F, 1.7F);
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                            14, 0.3D, 0.3D, 0.3D, 0.12D);
                }
                if (ModItems.CRYO_STATIC_TWIG.isBound()) {
                    int twigCount = 1 + serverLevel.random.nextInt(2);
                    popResource(serverLevel, pos, new ItemStack(ModItems.CRYO_STATIC_TWIG.get(), twigCount));
                }
            }
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

    /**
     * When lightning strikes within 6 blocks of existing Cryo-Static Shrubs, sprouts 1–3 new
     * Cryo-Static Shrubs on valid adjacent snow or ice blocks.
     */
    public static void spreadOnLightning(ServerLevel level, BlockPos lightningPos) {
        if (level == null || lightningPos == null || !ModBlocks.CRYO_STATIC_SHRUB.isBound()) {
            return;
        }
        Block shrubBlock = ModBlocks.CRYO_STATIC_SHRUB.get();
        List<BlockPos> nearbyShrubs = new ArrayList<>();
        for (BlockPos check : BlockPos.betweenClosed(
                lightningPos.offset(-6, -3, -6),
                lightningPos.offset(6, 3, 6))) {
            if (check.distSqr(lightningPos) <= 36.0D && level.getBlockState(check).is(shrubBlock)) {
                nearbyShrubs.add(check.immutable());
            }
        }
        if (nearbyShrubs.isEmpty()) {
            return;
        }

        BlockState shrubState = shrubBlock.defaultBlockState();
        RandomSource random = level.random;
        int targetCount = 1 + random.nextInt(3);
        int placed = 0;

        for (int attempt = 0; attempt < 24 && placed < targetCount; attempt++) {
            BlockPos origin = nearbyShrubs.get(random.nextInt(nearbyShrubs.size()));
            int dx = random.nextInt(5) - 2;
            int dy = random.nextInt(3) - 1;
            int dz = random.nextInt(5) - 2;
            if (dx == 0 && dy == 0 && dz == 0) {
                continue;
            }
            BlockPos candidate = origin.offset(dx, dy, dz);
            BlockState existing = level.getBlockState(candidate);
            boolean replaceable = existing.isAir()
                    || (existing.is(Blocks.SNOW) && existing.getValue(SnowLayerBlock.LAYERS) <= 2);
            if (replaceable && shrubState.canSurvive(level, candidate)) {
                level.setBlock(candidate, shrubState, 3);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        candidate.getX() + 0.5D, candidate.getY() + 0.5D, candidate.getZ() + 0.5D,
                        10, 0.25D, 0.3D, 0.25D, 0.08D);
                placed++;
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;

            boolean isStatic = random.nextBoolean();
            float r = isStatic ? 0.66f : 0.01f;
            float g = isStatic ? 0.33f : 0.52f;
            float b = isStatic ? 0.97f : 0.78f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
