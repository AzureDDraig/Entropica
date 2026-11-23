package ddraig.net.entropica.item.biome17;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.block.BuddingCrystalBlock;
import ddraig.net.entropica.block.biome17.StarlightIceBlock;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Starlight Tuning Chisel — Resonite-collared Stardust harmonic chisel (256 durability).
 * Passively mends under open night skies, carves Starlight Ice cleanly, tunes crystal clusters
 * (sneaking pops intact cluster; standing yields +1 bonus shard), and right-clicks to emit a
 * resonant ping that outlines nearby creatures/invisible entities and locates crystal clusters & geodes.
 */
public class StarlightTuningChiselItem extends Item {

    public static final int PING_COOLDOWN_TICKS = 160;
    public static final double ENTITY_SCAN_RADIUS = 20.0D;
    public static final int CRYSTAL_SCAN_RADIUS = 16;

    public StarlightTuningChiselItem(Properties properties) {
        super(properties.stacksTo(1).durability(256).enchantable(15));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (isIceOrCrystalTarget(state)) {
            return 9.0F;
        }
        return 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return isIceOrCrystalTarget(state);
    }

    public static boolean isIceOrCrystalTarget(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (state.getBlock() instanceof StarlightIceBlock
                || state.getBlock() instanceof AmethystClusterBlock
                || state.getBlock() instanceof AmethystBlock) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (key != null && (key.getPath().contains("crystal") || key.getPath().contains("ice"))) {
            return true;
        }
        try {
            return state.is(BlockTags.ICE) || state.is(BlockTags.CRYSTAL_SOUND_BLOCKS);
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!stack.isDamaged() || level.getGameTime() % 100L != 0L) {
            return;
        }
        BlockPos checkPos = entity.blockPosition().above();
        if (StarlightIceBlock.isNightSky(level) && level.canSeeSky(checkPos)) {
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
        }
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (state.getDestroySpeed(level, pos) != 0.0F) {
                stack.hurtAndBreak(1, miningEntity, EquipmentSlot.MAINHAND);
            }

            Item bonusShard = resolveClusterBonusShard(state);
            if (bonusShard != null) {
                if (miningEntity.isShiftKeyDown()) {
                    if (!StarlightIceBlock.hasSilkTouch(level, stack)) {
                        Item intactCluster = state.getBlock().asItem();
                        if (intactCluster != Items.AIR) {
                            Block.popResource(serverLevel, pos, new ItemStack(intactCluster));
                        }
                    }
                } else {
                    Block.popResource(serverLevel, pos, new ItemStack(bonusShard));
                }
                serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.95F, 1.3F);
            }
        }
        return true;
    }

    @Nullable
    public static Item resolveClusterBonusShard(BlockState state) {
        if (state == null || state.isAir()) {
            return null;
        }
        if (ModBlocks.STARDUST_CRYSTAL_CLUSTER.isBound() && state.is(ModBlocks.STARDUST_CRYSTAL_CLUSTER.get())) {
            return ModItems.STARDUST_FRAGMENT.isBound() ? ModItems.STARDUST_FRAGMENT.get() : Items.AMETHYST_SHARD;
        }
        if (ModBlocks.ASTRAL_CRYSTAL_CLUSTER.isBound() && state.is(ModBlocks.ASTRAL_CRYSTAL_CLUSTER.get())) {
            return ModItems.ASTRAL_CRYSTAL.isBound() ? ModItems.ASTRAL_CRYSTAL.get() : Items.AMETHYST_SHARD;
        }
        if (state.is(Blocks.AMETHYST_CLUSTER)) {
            return Items.AMETHYST_SHARD;
        }
        if (ModBlocks.AEGIS_CRYSTAL_CLUSTER.isBound() && state.is(ModBlocks.AEGIS_CRYSTAL_CLUSTER.get())) {
            return ModItems.AEGIS_CRYSTAL_SHARD.isBound() ? ModItems.AEGIS_CRYSTAL_SHARD.get() : Items.AMETHYST_SHARD;
        }
        if (ModBlocks.VOID_CRYSTAL_CLUSTER.isBound() && state.is(ModBlocks.VOID_CRYSTAL_CLUSTER.get())) {
            return ModItems.VOID_CRYSTAL_SHARD.isBound() ? ModItems.VOID_CRYSTAL_SHARD.get() : Items.AMETHYST_SHARD;
        }

        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (key == null) {
            return null;
        }
        return switch (key.getPath()) {
            case "stardust_crystal_cluster" -> resolveItem("stardust_fragment", Items.AMETHYST_SHARD);
            case "astral_crystal_cluster" -> resolveItem("astral_crystal", Items.AMETHYST_SHARD);
            case "amethyst_cluster" -> Items.AMETHYST_SHARD;
            case "aegis_crystal_cluster" -> resolveItem("aegis_crystal_shard", Items.AMETHYST_SHARD);
            case "void_crystal_cluster" -> resolveItem("void_crystal_shard", Items.AMETHYST_SHARD);
            default -> null;
        };
    }

    private static Item resolveItem(String path, Item fallback) {
        return BuiltInRegistries.ITEM
                .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path))
                .orElse(fallback);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        player.getCooldowns().addCooldown(stack, PING_COOLDOWN_TICKS);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            stack.hurtAndBreak(1, player, hand);

            double maxEntityDistSq = ENTITY_SCAN_RADIUS * ENTITY_SCAN_RADIUS;
            List<LivingEntity> entities = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(ENTITY_SCAN_RADIUS),
                    other -> other != player
                            && other.isAlive()
                            && !other.isSpectator()
                            && other.distanceToSqr(player) <= maxEntityDistSq
                            && (other.isInvisible() || other instanceof Mob || isStarlightNautilus(other))
            );

            for (LivingEntity target : entities) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
            }

            BlockPos origin = player.blockPosition();
            int maxBlockDistSq = CRYSTAL_SCAN_RADIUS * CRYSTAL_SCAN_RADIUS;
            int clusterCount = 0;
            int geodeCount = 0;
            int particlePings = 0;

            for (int dx = -CRYSTAL_SCAN_RADIUS; dx <= CRYSTAL_SCAN_RADIUS; dx++) {
                for (int dy = -CRYSTAL_SCAN_RADIUS; dy <= CRYSTAL_SCAN_RADIUS; dy++) {
                    for (int dz = -CRYSTAL_SCAN_RADIUS; dz <= CRYSTAL_SCAN_RADIUS; dz++) {
                        if (dx * dx + dy * dy + dz * dz > maxBlockDistSq) {
                            continue;
                        }
                        BlockPos checkPos = origin.offset(dx, dy, dz);
                        if (!serverLevel.hasChunkAt(checkPos)) {
                            continue;
                        }
                        BlockState checkState = serverLevel.getBlockState(checkPos);
                        if (checkState.isAir()) {
                            continue;
                        }
                        boolean isCluster = isCrystalClusterBlock(checkState);
                        boolean isGeode = !isCluster && isBuddingGeodeBlock(checkState);
                        if (isCluster || isGeode) {
                            if (isCluster) {
                                clusterCount++;
                            } else {
                                geodeCount++;
                            }
                            if (particlePings < 64) {
                                serverLevel.sendParticles(ParticleTypes.END_ROD,
                                        checkPos.getX() + 0.5D, checkPos.getY() + 0.55D, checkPos.getZ() + 0.5D,
                                        4, 0.22D, 0.22D, 0.22D, 0.025D);
                                particlePings++;
                            }
                        }
                    }
                }
            }

            serverLevel.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.3F);
            serverLevel.playSound(null, origin, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.75F, 1.15F);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    14, 0.5D, 0.4D, 0.5D, 0.04D);

            int totalResonated = clusterCount + geodeCount;
            Component readout = totalResonated > 0
                    ? Component.literal("✦ Harmonic Ping: " + clusterCount + " crystal clusters & " + geodeCount + " budding geode blocks resonated!")
                            .withStyle(ChatFormatting.AQUA)
                    : Component.literal("✦ Harmonic Ping: No crystal clusters or budding geodes resonated within 16m.")
                            .withStyle(ChatFormatting.GRAY);
            player.displayClientMessage(readout, true);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    private static boolean isStarlightNautilus(LivingEntity entity) {
        if (ModEntityTypes.STARLIGHT_NAUTILUS.isBound() && entity.getType() == ModEntityTypes.STARLIGHT_NAUTILUS.get()) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && "starlight_nautilus".equals(key.getPath());
    }

    private static boolean isCrystalClusterBlock(BlockState state) {
        if (state.getBlock() instanceof AmethystClusterBlock) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (key == null) {
            return false;
        }
        String path = key.getPath();
        return path.endsWith("_cluster") || path.endsWith("_crystal_bud") || path.endsWith("_amethyst_bud");
    }

    private static boolean isBuddingGeodeBlock(BlockState state) {
        if (state.getBlock() instanceof BuddingAmethystBlock || state.getBlock() instanceof BuddingCrystalBlock) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (key == null) {
            return false;
        }
        String path = key.getPath();
        return path.startsWith("budding_")
                || "aegis_crystal_block".equals(path)
                || "void_crystal_block".equals(path);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Mine Crystal Clusters: Stand for +1 bonus shard, or Sneak to harvest cluster intact.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Right-Click: Harmonic ping outlines mobs (20m) & locates crystal clusters/geodes (16m); self-repairs under night sky.")
                .withStyle(ChatFormatting.AQUA));
    }
}
