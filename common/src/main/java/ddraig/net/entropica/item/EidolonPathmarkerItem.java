package ddraig.net.entropica.item;

import ddraig.net.entropica.entity.EidolicShadowEntity;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class EidolonPathmarkerItem extends EntropicaComponentItem {

    public EidolonPathmarkerItem(Properties properties) {
        super(properties, "tooltip.entropica.eidolon_pathmarker");
    }

    // ==========================================
    // CALLED WHEN CLICKING A BLOCK
    // ==========================================
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();

        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        int size = tag.getInt("PathSize").orElse(0);

        if (size >= 64) {
            if (level.isClientSide()) {
                player.displayClientMessage(Component.translatable("msg.entropica.max_path_size_reached_64_nodes").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        tag.putLong("Path_" + size, pos.asLong());
        tag.putInt("PathSize", size + 1);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (level.isClientSide()) {
            player.displayClientMessage(Component.literal("Path Node " + (size + 1) + " marked.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        } else {
            // FIXED: Added .value() back to unwrap the Holder.Reference
            level.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 0.5f, 2.0f);
        }

        // Returning SUCCESS prevents the chest from opening!
        return InteractionResult.SUCCESS;
    }

    // ==========================================
    // CALLED WHEN CLICKING THE AIR
    // ==========================================
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        int size = tag.getInt("PathSize").orElse(0);

        if (player.isShiftKeyDown()) {
            // SNEAK + CLICK AIR: Safely clear the path without summoning
            if (size > 0) {
                tag.remove("PathSize");
                for (int i = 0; i < size; i++) {
                    tag.remove("Path_" + i);
                }
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                if (level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("msg.entropica.path_cleared").withStyle(ChatFormatting.YELLOW), true);
                } else {
                    // FIXED: Added .value() back to unwrap the Holder.Reference
                    level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return InteractionResult.SUCCESS;
            }
        } else {
            // NORMAL CLICK AIR: Summon the shadow!
            if (size >= 2) {
                if (level instanceof ServerLevel serverLevel) {
                    List<BlockPos> path = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        path.add(BlockPos.of(tag.getLong("Path_" + i).orElse(0L)));
                    }

                    EidolicShadowEntity shadow = ModEntityTypes.EIDOLIC_SHADOW.get().create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
                    if (shadow != null) {
                        BlockPos spawnPos = path.get(0);
                        // Spawn 1 block above the chest so it gracefully hovers
                        shadow.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
                        shadow.setWaypoints(path);
                        serverLevel.addFreshEntity(shadow);
                        serverLevel.playSound(null, spawnPos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0f, 0.5f);
                    }
                } else {
                    player.displayClientMessage(Component.translatable("msg.entropica.eidolic_shadow_summoned").withStyle(ChatFormatting.DARK_PURPLE), true);
                }

                // Clear the path data after successfully summoning
                tag.remove("PathSize");
                for (int i = 0; i < size; i++) {
                    tag.remove("Path_" + i);
                }
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                return InteractionResult.SUCCESS;
            } else if (size > 0) {
                if (level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("msg.entropica.path_too_short_need_at_least").withStyle(ChatFormatting.RED), true);
                }
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }
}