package ddraig.net.entropica.item;

import ddraig.net.entropica.entity.EidolicShadowEntity;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide() && tag.contains("PathSize")) {
                int size = tag.getInt("PathSize").orElse(0); // 1.21.10 Optional unwrap
                if (size >= 2) {
                    List<BlockPos> path = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        path.add(BlockPos.of(tag.getLong("Path_" + i).orElse(0L))); // 1.21.10 Optional unwrap
                    }

                    // 1.21.10 requires EntitySpawnReason
                    EidolicShadowEntity shadow = ModEntityTypes.EIDOLIC_SHADOW.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
                    if (shadow != null) {
                        BlockPos spawnPos = path.get(0);
                        shadow.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                        shadow.setWaypoints(path);
                        level.addFreshEntity(shadow);

                        // 1.21.10 SoundEvent unwrapping
                        level.playSound(null, spawnPos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0f, 0.5f);
                    }

                    tag.remove("PathSize");
                    CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
                } else {
                    player.displayClientMessage(Component.literal("Path too short! Need at least 2 points.").withStyle(ChatFormatting.RED), true);
                }
            } else if (level.isClientSide()) {
                player.displayClientMessage(Component.literal("Eidolic Shadow Summoned!").withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            return InteractionResult.SUCCESS;
        } else {
            if (!level.isClientSide()) {
                int size = tag.getInt("PathSize").orElse(0);

                // Add your 64 block hard limit
                if (size >= 64) {
                    player.displayClientMessage(Component.literal("Max path size reached (64 nodes). Shift-click to summon!").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }

                tag.putLong("Path_" + size, pos.asLong());
                tag.putInt("PathSize", size + 1);
                CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);

                // 1.21.10 SoundEvent unwrapping
                level.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 0.5f, 2.0f);
            } else {
                int size = tag.getInt("PathSize").orElse(0);
                player.displayClientMessage(Component.literal("Path Node " + (size + 1) + " marked.").withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
            return InteractionResult.SUCCESS;
        }
    }
}