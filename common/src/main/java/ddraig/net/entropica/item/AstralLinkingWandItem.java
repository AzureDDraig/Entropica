package ddraig.net.entropica.item;

import ddraig.net.entropica.block.entity.AstralCollectorBlockEntity;
import ddraig.net.entropica.block.entity.BeamSplitterPrismBlockEntity;
import ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AstralLinkingWandItem extends Item {

    public static final String TAG_SOURCE_X = "SourceX";
    public static final String TAG_SOURCE_Y = "SourceY";
    public static final String TAG_SOURCE_Z = "SourceZ";
    public static final String TAG_HAS_SOURCE = "HasSource";

    public AstralLinkingWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (tag.getBoolean(TAG_HAS_SOURCE).orElse(false)) {
            tag.remove(TAG_HAS_SOURCE);
            tag.remove(TAG_SOURCE_X);
            tag.remove(TAG_SOURCE_Y);
            tag.remove(TAG_SOURCE_Z);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("§e[Astral Linking Wand] §7Cleared source node selection."), true);
            }
            return InteractionResult.SUCCESS;
        }

        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        BlockEntity clickedBE = level.getBlockEntity(clickedPos);

        // 1. Shift + Right-Click on Secondary Lens or Prism: 45° Snap Ratchet Mode
        if (player.isShiftKeyDown()) {
            if (clickedBE instanceof ddraig.net.entropica.block.entity.SecondaryAstralLensBlockEntity secondaryLensBE) {
                float currentYaw = secondaryLensBE.getYaw();
                float newYaw = (float) (Math.round((currentYaw + 45.0f) / 45.0f) * 45) % 360.0f;
                if (newYaw < 0) newYaw += 360.0f;

                secondaryLensBE.setFocus(newYaw, 0.0f, "Snapped (" + (int) newYaw + "°)", false);
                secondaryLensBE.setChanged();

                if (!level.isClientSide()) {
                    level.playSound(null, clickedPos, SoundEvents.SPYGLASS_USE, SoundSource.BLOCKS, 0.8f, 1.4f);
                    player.displayClientMessage(Component.literal("§b[Secondary Lens] §7Azimuth snapped to §f" + (int) newYaw + "° §8(" + getCardinalHeading(newYaw) + ")"), true);
                }
                return InteractionResult.SUCCESS;
            } else if (clickedBE instanceof BeamSplitterPrismBlockEntity prismBE) {
                // If prism, clear targets or rotate
                if (!level.isClientSide()) {
                    level.playSound(null, clickedPos, SoundEvents.SPYGLASS_USE, SoundSource.BLOCKS, 0.8f, 1.4f);
                    player.displayClientMessage(Component.literal("§b[Beam Splitter Prism] §7Ratchet adjusted."), true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 2. If already has a selected source node -> Link to destination!
        if (tag.getBoolean(TAG_HAS_SOURCE).orElse(false)) {
            BlockPos sourcePos = new BlockPos(
                    tag.getInt(TAG_SOURCE_X).orElse(0),
                    tag.getInt(TAG_SOURCE_Y).orElse(0),
                    tag.getInt(TAG_SOURCE_Z).orElse(0)
            );

            if (sourcePos.equals(clickedPos)) {
                // Clicked same block -> clear selection
                tag.remove(TAG_HAS_SOURCE);
                tag.remove(TAG_SOURCE_X);
                tag.remove(TAG_SOURCE_Y);
                tag.remove(TAG_SOURCE_Z);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§e[Astral Linking Wand] §7Cleared source node selection."), true);
                }
                return InteractionResult.SUCCESS;
            }

            double distSq = sourcePos.distSqr(clickedPos);
            if (distSq > 32 * 32) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§c[Astral Linking Wand] §7Target is out of range (Max: 32 blocks, Distance: " + (int) Math.sqrt(distSq) + " blocks)."), true);
                }
                return InteractionResult.FAIL;
            }

            // Perform linking logic based on source block entity
            BlockEntity sourceBE = level.getBlockEntity(sourcePos);
            boolean linked = false;

            if (sourceBE instanceof RefractiveAstralLensBlockEntity lensBE) {
                double dx = (clickedPos.getX() + 0.5) - (sourcePos.getX() + 0.5);
                double dy = (clickedPos.getY() + 0.5) - (sourcePos.getY() + 0.5625);
                double dz = (clickedPos.getZ() + 0.5) - (sourcePos.getZ() + 0.5);
                double distXZ = Math.sqrt(dx * dx + dz * dz);

                float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                if (targetYaw < 0) targetYaw += 360.0f;
                float targetPitch = (float) Math.toDegrees(Math.atan2(dy, distXZ));

                lensBE.setFocus(targetYaw, targetPitch, "Target (" + clickedPos.toShortString() + ")", true);
                lensBE.setChanged();
                linked = true;
            } else if (sourceBE instanceof ddraig.net.entropica.block.entity.SecondaryAstralLensBlockEntity secondaryLensBE) {
                double dx = (clickedPos.getX() + 0.5) - (sourcePos.getX() + 0.5);
                double dy = (clickedPos.getY() + 0.5) - (sourcePos.getY() + 0.5625);
                double dz = (clickedPos.getZ() + 0.5) - (sourcePos.getZ() + 0.5);
                double distXZ = Math.sqrt(dx * dx + dz * dz);

                float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                if (targetYaw < 0) targetYaw += 360.0f;
                float targetPitch = (float) Math.toDegrees(Math.atan2(dy, distXZ));

                secondaryLensBE.setFocus(targetYaw, targetPitch, "Target: " + clickedPos.toShortString(), true);
                secondaryLensBE.setChanged();
                linked = true;
            } else if (sourceBE instanceof BeamSplitterPrismBlockEntity prismBE) {
                prismBE.addLinkedTarget(clickedPos);
                prismBE.setChanged();
                linked = true;
            }

            if (linked) {
                tag.remove(TAG_HAS_SOURCE);
                tag.remove(TAG_SOURCE_X);
                tag.remove(TAG_SOURCE_Y);
                tag.remove(TAG_SOURCE_Z);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                if (!level.isClientSide()) {
                    level.playSound(null, clickedPos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 1.4f);
                    player.displayClientMessage(Component.literal("§a[Astral Linking Wand] §7Successfully linked §b(" + sourcePos.toShortString() + ") §7-> §b(" + clickedPos.toShortString() + ")§7!"), true);
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§c[Astral Linking Wand] §7Source at §f(" + sourcePos.toShortString() + ") §7is not a valid starlight emitter!"), true);
                }
                return InteractionResult.FAIL;
            }
        }

        // 3. Normal Right-Click on an Optical Component -> Select as Source
        if (clickedBE instanceof RefractiveAstralLensBlockEntity ||
            clickedBE instanceof ddraig.net.entropica.block.entity.SecondaryAstralLensBlockEntity ||
            clickedBE instanceof BeamSplitterPrismBlockEntity) {
            tag.putInt(TAG_SOURCE_X, clickedPos.getX());
            tag.putInt(TAG_SOURCE_Y, clickedPos.getY());
            tag.putInt(TAG_SOURCE_Z, clickedPos.getZ());
            tag.putBoolean(TAG_HAS_SOURCE, true);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            if (!level.isClientSide()) {
                level.playSound(null, clickedPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.2f);
                player.displayClientMessage(Component.literal("§b[Astral Linking Wand] §7Source node selected at §f(" + clickedPos.toShortString() + ")§7. Now right-click destination block."), true);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static String getCardinalHeading(float yaw) {
        yaw = (yaw % 360.0f + 360.0f) % 360.0f;
        if (yaw >= 337.5f || yaw < 22.5f) return "North";
        else if (yaw >= 22.5f && yaw < 67.5f) return "Northeast";
        else if (yaw >= 67.5f && yaw < 112.5f) return "East";
        else if (yaw >= 112.5f && yaw < 157.5f) return "Southeast";
        else if (yaw >= 157.5f && yaw < 202.5f) return "South";
        else if (yaw >= 202.5f && yaw < 247.5f) return "Southwest";
        else if (yaw >= 247.5f && yaw < 292.5f) return "West";
        else return "Northwest";
    }
}
