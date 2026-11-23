package ddraig.net.entropica.item.biome9;

import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Phase-Recall Stone — Void Materia waystone that attunes to a Spatial Phase Anchor
 * and recalls the holder back within 128 blocks after a short channel.
 */
public class PhaseRecallStoneItem extends Item {
    public static final String TAG_ANCHOR_DIM = "AnchorDim";
    public static final String TAG_ANCHOR_X = "AnchorX";
    public static final String TAG_ANCHOR_Y = "AnchorY";
    public static final String TAG_ANCHOR_Z = "AnchorZ";

    public static final int USE_DURATION_TICKS = 24;
    public static final int RECALL_COOLDOWN_TICKS = 600;
    public static final double MAX_RECALL_DISTANCE = 128.0D;

    public PhaseRecallStoneItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (ModBlocks.SPATIAL_PHASE_ANCHOR.isBound() && level.getBlockState(pos).is(ModBlocks.SPATIAL_PHASE_ANCHOR.get())) {
            ItemStack stack = context.getItemInHand();
            if (!level.isClientSide()) {
                CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                tag.putString(TAG_ANCHOR_DIM, level.dimension().location().toString());
                tag.putInt(TAG_ANCHOR_X, pos.getX());
                tag.putInt(TAG_ANCHOR_Y, pos.getY());
                tag.putInt(TAG_ANCHOR_Z, pos.getZ());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 1.0F, 1.1F);
                player.displayClientMessage(Component.literal(
                        "Attuned to Spatial Phase Anchor [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"
                ).withStyle(ChatFormatting.AQUA), true);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.getString(TAG_ANCHOR_DIM).orElse("").isEmpty()) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Sneak + Right-Click a Spatial Phase Anchor to attune.")
                        .withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResult.FAIL;
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level instanceof ServerLevel serverLevel && remainingUseDuration % 6 == 0) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    livingEntity.getX(), livingEntity.getY() + 1.0D, livingEntity.getZ(),
                    5, 0.3D, 0.4D, 0.3D, 0.03D);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            String dimStr = tag.getString(TAG_ANCHOR_DIM).orElse("");
            if (dimStr.isEmpty() || !serverLevel.dimension().location().toString().equals(dimStr)) {
                player.displayClientMessage(Component.literal("Bound Spatial Phase Anchor is in another dimension.")
                        .withStyle(ChatFormatting.RED), true);
                return stack;
            }

            BlockPos anchorPos = new BlockPos(
                    tag.getInt(TAG_ANCHOR_X).orElse(0),
                    tag.getInt(TAG_ANCHOR_Y).orElse(0),
                    tag.getInt(TAG_ANCHOR_Z).orElse(0)
            );
            Vec3 anchorCenter = Vec3.atCenterOf(anchorPos);
            if (player.position().distanceToSqr(anchorCenter) > MAX_RECALL_DISTANCE * MAX_RECALL_DISTANCE) {
                player.displayClientMessage(Component.literal("Spatial Phase Anchor is out of range (max 128 blocks).")
                        .withStyle(ChatFormatting.RED), true);
                return stack;
            }

            if (!serverLevel.isLoaded(anchorPos)
                    || !ModBlocks.SPATIAL_PHASE_ANCHOR.isBound()
                    || !serverLevel.getBlockState(anchorPos).is(ModBlocks.SPATIAL_PHASE_ANCHOR.get())) {
                player.displayClientMessage(Component.literal("Bound Spatial Phase Anchor is missing or obstructed.")
                        .withStyle(ChatFormatting.RED), true);
                return stack;
            }

            BlockPos safeSpot = findSafeSpotNearAnchor(serverLevel, anchorPos);
            if (safeSpot == null) {
                player.displayClientMessage(Component.literal("No clear landing space around the Spatial Phase Anchor.")
                        .withStyle(ChatFormatting.RED), true);
                return stack;
            }

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    32, 0.35D, 0.55D, 0.35D, 0.2D);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            player.teleportTo(safeSpot.getX() + 0.5D, safeSpot.getY(), safeSpot.getZ() + 0.5D);
            player.resetFallDistance();
            player.getCooldowns().addCooldown(stack, RECALL_COOLDOWN_TICKS);

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    36, 0.35D, 0.55D, 0.35D, 0.25D);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    18, 0.3D, 0.45D, 0.3D, 0.06D);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.08F);
        }
        return stack;
    }

    private static BlockPos findSafeSpotNearAnchor(ServerLevel level, BlockPos anchorPos) {
        BlockPos above = anchorPos.above();
        if (isClearTwoTall(level, above)) {
            return above;
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos side = anchorPos.relative(dir);
            if (isClearTwoTall(level, side)) {
                return side;
            }
            BlockPos sideUp = side.above();
            if (isClearTwoTall(level, sideUp)) {
                return sideUp;
            }
        }
        return null;
    }

    private static boolean isClearTwoTall(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String dimStr = tag.getString(TAG_ANCHOR_DIM).orElse("");
        if (!dimStr.isEmpty()) {
            int x = tag.getInt(TAG_ANCHOR_X).orElse(0);
            int y = tag.getInt(TAG_ANCHOR_Y).orElse(0);
            int z = tag.getInt(TAG_ANCHOR_Z).orElse(0);
            tooltipComponents.accept(Component.literal("Bound Anchor: [" + x + ", " + y + ", " + z + "]")
                    .withStyle(ChatFormatting.AQUA));
            tooltipComponents.accept(Component.literal("Hold Right-Click: Recall within 128 blocks (30s cooldown).")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.accept(Component.literal("Sneak + Right-Click a Spatial Phase Anchor to attune.")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
