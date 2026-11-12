package ddraig.net.entropica.item;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * Spellgem Seed (Spellcrafting Guide Section 8.1 Pathway B — Crystalline Seed Growth).
 *
 * <p>Crafted from 1x Amethyst Shard + 4x Redstone Dust + 4x Nether Quartz.
 * Dropping a Spellgem Seed into Water prevents despawning and matures it over
 * 10 minutes ({@code 12,000} ticks) into a pure {@link ModItems#BLANK_SPELLGEM}.</p>
 */
public class SpellgemSeedItem extends Item {

    public static final int MATURATION_TICKS = 12_000; // 10 minutes * 60s * 20t
    public static final String TAG_GROWTH_TICKS = "SubmergedGrowthTicks";

    private static final Set<ItemEntity> TRACKED_SEED_ENTITIES =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public SpellgemSeedItem(Properties properties) {
        super(properties);
    }

    public static void trackDroppedEntity(ItemEntity itemEntity) {
        if (itemEntity != null && itemEntity.getItem().getItem() instanceof SpellgemSeedItem) {
            TRACKED_SEED_ENTITIES.add(itemEntity);
        }
    }

    public static int getGrowthTicks(ItemStack stack) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return cd.copyTag().getInt(TAG_GROWTH_TICKS).orElse(0);
    }

    public static void setGrowthTicks(ItemStack stack, int ticks) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (ticks <= 0) {
            tag.remove(TAG_GROWTH_TICKS);
        } else {
            tag.putInt(TAG_GROWTH_TICKS, ticks);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Advances submerged water growth on {@code stack} by {@code deltaTicks}, returning
     * either the updated seed stack or a newly matured {@link ModItems#BLANK_SPELLGEM} stack.
     */
    public static ItemStack tickSubmergedSeedStack(ItemStack stack, int deltaTicks) {
        if (stack.isEmpty() || !(stack.getItem() instanceof SpellgemSeedItem) || deltaTicks <= 0) {
            return stack;
        }
        int nextTicks = getGrowthTicks(stack) + deltaTicks;
        if (nextTicks >= MATURATION_TICKS) {
            return new ItemStack(ModItems.BLANK_SPELLGEM.get(), stack.getCount());
        }
        ItemStack copy = stack.copy();
        setGrowthTicks(copy, nextTicks);
        return copy;
    }

    /**
     * Server-level tick handler invoked from {@code Entropica.init()} to mature submerged Spellgem Seeds.
     */
    public static void tickServerLevel(ServerLevel level) {
        synchronized (TRACKED_SEED_ENTITIES) {
            Iterator<ItemEntity> it = TRACKED_SEED_ENTITIES.iterator();
            while (it.hasNext()) {
                ItemEntity entity = it.next();
                if (entity == null || !entity.isAlive() || entity.isRemoved()) {
                    it.remove();
                    continue;
                }
                if (entity.level() != level) {
                    continue;
                }
                ItemStack stack = entity.getItem();
                if (stack.isEmpty() || !(stack.getItem() instanceof SpellgemSeedItem)) {
                    it.remove();
                    continue;
                }

                boolean inWater = entity.isInWater() || level.getFluidState(entity.blockPosition()).is(FluidTags.WATER);
                if (!inWater) {
                    continue;
                }

                // Prevent vanilla 5-minute item despawn while maturing for 10 minutes in water
                entity.setExtendedLifetime();

                int next = getGrowthTicks(stack) + 1;
                if (next >= MATURATION_TICKS) {
                    ItemStack matured = new ItemStack(ModItems.BLANK_SPELLGEM.get(), stack.getCount());
                    entity.setItem(matured);
                    level.sendParticles(ParticleTypes.END_ROD,
                            entity.getX(), entity.getY() + 0.25D, entity.getZ(),
                            20, 0.25D, 0.25D, 0.25D, 0.05D);
                    level.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                            SoundSource.BLOCKS, 1.0F, 1.35F);
                    it.remove();
                } else {
                    setGrowthTicks(stack, next);
                    entity.setItem(stack);
                    if (next % 40 == 0) {
                        level.sendParticles(ParticleTypes.END_ROD,
                                entity.getX(), entity.getY() + 0.2D, entity.getZ(),
                                2, 0.15D, 0.15D, 0.15D, 0.01D);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Submerge in water for 10 minutes to crystallize into a Blank Spellgem.")
                .withStyle(ChatFormatting.GRAY));
        int ticks = getGrowthTicks(stack);
        if (ticks > 0) {
            int curSec = ticks / 20;
            int maxSec = MATURATION_TICKS / 20;
            int pct = Math.min(100, (ticks * 100) / MATURATION_TICKS);
            tooltipComponents.accept(Component.literal(String.format(
                    "Crystallization: %d%% (%dm %02ds / %dm %02ds)",
                    pct, curSec / 60, curSec % 60, maxSec / 60, maxSec % 60
            )).withStyle(ChatFormatting.AQUA));
        }
    }
}
