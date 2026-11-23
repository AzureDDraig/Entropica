package ddraig.net.entropica.block.biome7;

import ddraig.net.entropica.item.GroundingCoilItem;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared thread-safe static charge, grounding, and shrub-charge
 * tracking utilities for Biome #7 (2.7 Static Ice-Plains).
 */
public final class StaticChargeHelper {

    public static final String TAG_STATIC_CHARGE_TICKS = "EntropicaStaticChargeTicks";
    public static final String TAG_SHRUB_MELEE_CHARGE = "EntropicaShrubMeleeCharge";
    public static final String TAG_STORED_STATIC = "StoredStatic";
    public static final String TAG_GALVANIC_STRIKES = "GalvanicStrikes";

    private static final Map<UUID, Integer> STATIC_CHARGE_TICKS = new ConcurrentHashMap<>();
    private static final Set<UUID> SHRUB_MELEE_CHARGED = ConcurrentHashMap.newKeySet();

    private StaticChargeHelper() {}

    public static int getStaticChargeTicks(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        Integer mem = STATIC_CHARGE_TICKS.get(entity.getUUID());
        if (mem != null && mem > 0) {
            return mem;
        }
        CompoundTag data = EntityHelper.getPersistentData(entity);
        return Math.max(0, data.getInt(TAG_STATIC_CHARGE_TICKS).orElse(0));
    }

    public static void setStaticChargeTicks(LivingEntity entity, int ticks) {
        if (entity == null) {
            return;
        }
        int clamped = Math.max(0, ticks);
        if (clamped <= 0) {
            clearStaticCharge(entity);
            return;
        }
        if (STATIC_CHARGE_TICKS.size() > 2048) {
            STATIC_CHARGE_TICKS.clear();
        }
        STATIC_CHARGE_TICKS.put(entity.getUUID(), clamped);
        EntityHelper.getPersistentData(entity).putInt(TAG_STATIC_CHARGE_TICKS, clamped);
    }

    public static int addStaticChargeTicks(LivingEntity entity, int delta) {
        if (entity == null) {
            return 0;
        }
        int next = Math.max(0, getStaticChargeTicks(entity) + delta);
        setStaticChargeTicks(entity, next);
        return next;
    }

    public static void clearStaticCharge(Entity entity) {
        if (entity == null) {
            return;
        }
        STATIC_CHARGE_TICKS.remove(entity.getUUID());
        entity.removeTag(TAG_STATIC_CHARGE_TICKS);
        if (entity instanceof LivingEntity living) {
            EntityHelper.getPersistentData(living).remove(TAG_STATIC_CHARGE_TICKS);
        }
    }

    /**
     * Returns true if {@code entity} is grounded against Static Charge shocks — by standing on
     * a Static Conductive Mat or carrying a Grounding Coil.
     */
    public static boolean isEntityGrounded(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        Level level = entity.level();
        if (level != null) {
            BlockPos pos = entity.blockPosition();
            if (ModBlocks.STATIC_CONDUCTIVE_MAT.isBound()) {
                BlockState at = level.getBlockState(pos);
                BlockState below = level.getBlockState(pos.below());
                if (at.is(ModBlocks.STATIC_CONDUCTIVE_MAT.get())
                        || below.is(ModBlocks.STATIC_CONDUCTIVE_MAT.get())) {
                    return true;
                }
            }
        }
        if (entity instanceof Player player && GroundingCoilItem.hasGroundingCoil(player)) {
            return true;
        }
        return false;
    }

    public static void grantShrubMeleeCharge(Player player) {
        if (player == null) {
            return;
        }
        if (SHRUB_MELEE_CHARGED.size() > 2048) {
            SHRUB_MELEE_CHARGED.clear();
        }
        SHRUB_MELEE_CHARGED.add(player.getUUID());
        EntityHelper.getPersistentData(player).putBoolean(TAG_SHRUB_MELEE_CHARGE, true);
    }

    public static boolean hasShrubMeleeCharge(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        return SHRUB_MELEE_CHARGED.contains(entity.getUUID())
                || EntityHelper.getPersistentData(entity).getBoolean(TAG_SHRUB_MELEE_CHARGE).orElse(false);
    }

    public static boolean consumeShrubMeleeCharge(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        boolean inSet = SHRUB_MELEE_CHARGED.remove(entity.getUUID());
        CompoundTag data = EntityHelper.getPersistentData(entity);
        boolean inTag = data.getBoolean(TAG_SHRUB_MELEE_CHARGE).orElse(false);
        if (inTag) {
            data.remove(TAG_SHRUB_MELEE_CHARGE);
        }
        return inSet || inTag;
    }
}
