package ddraig.net.entropica.gravity;

import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GravityApi {

    public static final ResourceLocation GRAVITY_MOD_ID = ResourceLocation.fromNamespaceAndPath("entropica", "gravity_override");
    public static final ResourceLocation SAFE_FALL_MOD_ID = ResourceLocation.fromNamespaceAndPath("entropica", "gravity_safe_fall");
    public static final ResourceLocation FALL_DMG_MOD_ID = ResourceLocation.fromNamespaceAndPath("entropica", "gravity_fall_damage");
    public static final ResourceLocation SOLES_MOD_ID = ResourceLocation.fromNamespaceAndPath("entropica", "graviton_soles_protection");

    public static final double VANILLA_BASE_GRAVITY = 0.08;
    public static final double MOON_GRAVITY = 0.02;
    public static final double ZERO_GRAVITY = 0.0;
    public static final double INVERTED_GRAVITY = -0.08;
    public static final double CRUSH_GRAVITY = 0.24;

    public static final Set<UUID> SOLES_INVERTED_ENTITIES = ConcurrentHashMap.newKeySet();
    public static final Map<UUID, Long> SOLES_FLIP_COOLDOWN = new ConcurrentHashMap<>();

    /**
     * Checks if the entity has a specific item equipped in Curios, Accessories, or Trinkets slots.
     */
    public static boolean hasCurioEquipped(LivingEntity entity, Item targetItem) {
        if (entity == null || targetItem == null) return false;

        // 1. Curios API (NeoForge / Forge: top.theillusivec4.curios.api.CuriosApi)
        try {
            Class<?> curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getCuriosInventoryMethod = curiosApiClass.getMethod("getCuriosInventory", LivingEntity.class);
            Object optionalInv = getCuriosInventoryMethod.invoke(null, entity);
            if (optionalInv instanceof Optional<?> opt && opt.isPresent()) {
                Object curiosItemHandler = opt.get();
                Method findFirstCurioMethod = curiosItemHandler.getClass().getMethod("findFirstCurio", Item.class);
                Object resultOpt = findFirstCurioMethod.invoke(curiosItemHandler, targetItem);
                if (resultOpt instanceof Optional<?> res && res.isPresent()) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
            // Curios not present or different API version
        }

        // 2. Accessories API (Fabric: io.wispforest.accessories.api.AccessoriesCapability)
        try {
            Class<?> accessoriesCapClass = Class.forName("io.wispforest.accessories.api.AccessoriesCapability");
            Method getMethod = accessoriesCapClass.getMethod("get", LivingEntity.class);
            Object cap = getMethod.invoke(null, entity);
            if (cap != null) {
                Method isEquippedMethod = cap.getClass().getMethod("isEquipped", Item.class);
                Object isEq = isEquippedMethod.invoke(cap, targetItem);
                if (Boolean.TRUE.equals(isEq)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
            // Accessories not present
        }

        // 3. Trinkets API (Fabric fallback: dev.emi.trinkets.api.TrinketsApi)
        try {
            Class<?> trinketsApiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi");
            Method getTrinketCompMethod = trinketsApiClass.getMethod("getTrinketComponent", LivingEntity.class);
            Object optionalComp = getTrinketCompMethod.invoke(null, entity);
            if (optionalComp instanceof Optional<?> opt && opt.isPresent()) {
                Object comp = opt.get();
                Method isEquippedMethod = comp.getClass().getMethod("isEquipped", Item.class);
                Object isEq = isEquippedMethod.invoke(comp, targetItem);
                if (Boolean.TRUE.equals(isEq)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
            // Trinkets not present
        }

        return false;
    }

    /**
     * Checks if the entity is immune to external gravity manipulation (Inertial Anchor).
     */
    public static boolean isAnchored(LivingEntity entity) {
        if (entity == null) return false;
        if (entity.hasEffect(ModEffects.INERTIAL_ANCHOR)) return true;

        // Check held items and equipped curios/armor for Inertial Anchor
        if (entity.getMainHandItem().is(ModItems.INERTIAL_ANCHOR_AMULET.get()) ||
            entity.getOffhandItem().is(ModItems.INERTIAL_ANCHOR_AMULET.get())) {
            return true;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack armor = entity.getItemBySlot(slot);
            if (!armor.isEmpty() && armor.is(ModItems.INERTIAL_ANCHOR_AMULET.get())) return true;
        }

        // Check Curios / Trinkets / Accessories slots
        if (hasCurioEquipped(entity, ModItems.INERTIAL_ANCHOR_AMULET.get())) {
            return true;
        }

        return false;
    }

    /**
     * Retrieves the current effective gravity value of the entity.
     */
    public static double getEffectiveGravity(LivingEntity entity) {
        if (entity == null) return VANILLA_BASE_GRAVITY;
        AttributeInstance inst = entity.getAttribute(Attributes.GRAVITY);
        return inst != null ? inst.getValue() : VANILLA_BASE_GRAVITY;
    }

    /**
     * Returns true if the entity's gravity is inverted (falling upward).
     */
    public static boolean isInverted(LivingEntity entity) {
        if (entity == null) return false;
        if (SOLES_INVERTED_ENTITIES.contains(entity.getUUID())) return true;
        return getEffectiveGravity(entity) < -0.005;
    }

    /**
     * Returns true if the entity is under near-zero gravity.
     */
    public static boolean isZeroG(LivingEntity entity) {
        double g = getEffectiveGravity(entity);
        return Math.abs(g) <= 0.01;
    }

    /**
     * Returns true if the entity is under moon gravity.
     */
    public static boolean isMoonGravity(LivingEntity entity) {
        double g = getEffectiveGravity(entity);
        return g > 0.01 && g <= 0.04;
    }

    /**
     * Checks if the player is wearing Graviton Soles or has them equipped in Curios.
     */
    public static boolean hasGravitonSoles(LivingEntity entity) {
        if (entity == null) return false;
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.isEmpty() && boots.is(ModItems.GRAVITON_SOLES.get())) return true;

        if (entity.getMainHandItem().is(ModItems.GRAVITON_SOLES.get()) ||
            entity.getOffhandItem().is(ModItems.GRAVITON_SOLES.get())) {
            return true;
        }

        // Check Curios / Trinkets / Accessories slots
        if (hasCurioEquipped(entity, ModItems.GRAVITON_SOLES.get())) {
            return true;
        }

        return false;
    }

    /**
     * Ticks Graviton Soles mechanics: wall sticking, climbing, ceiling traction, and sneak-ledge gravity flipping.
     */
    public static void tickGravitonSoles(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) return;

        if (!hasGravitonSoles(entity)) {
            // Clean up attributes if soles are unequipped
            AttributeInstance fallDmg = entity.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
            if (fallDmg != null && fallDmg.getModifier(SOLES_MOD_ID) != null) {
                fallDmg.removeModifier(SOLES_MOD_ID);
            }
            AttributeInstance safeFall = entity.getAttribute(Attributes.SAFE_FALL_DISTANCE);
            if (safeFall != null && safeFall.getModifier(SOLES_MOD_ID) != null) {
                safeFall.removeModifier(SOLES_MOD_ID);
            }
            if (SOLES_INVERTED_ENTITIES.remove(entity.getUUID())) {
                resetGravity(entity);
            }
            SOLES_FLIP_COOLDOWN.remove(entity.getUUID());
            if (SOLES_FLIP_COOLDOWN.size() > 50) {
                long gameTime = entity.level().getGameTime();
                SOLES_FLIP_COOLDOWN.entrySet().removeIf(entry -> gameTime >= entry.getValue());
            }
            return;
        }

        // 1. Fall damage elimination via attribute dampening
        AttributeInstance fallDmg = entity.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
        if (fallDmg != null && fallDmg.getModifier(SOLES_MOD_ID) == null) {
            fallDmg.addTransientModifier(new AttributeModifier(SOLES_MOD_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        AttributeInstance safeFall = entity.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (safeFall != null && safeFall.getModifier(SOLES_MOD_ID) == null) {
            safeFall.addTransientModifier(new AttributeModifier(SOLES_MOD_ID, 1000.0, AttributeModifier.Operation.ADD_VALUE));
        }

        // 2. Wall Contact, Sticking & Climbing
        AABB box = entity.getBoundingBox();
        AABB wallBox = new AABB(box.minX - 0.08, box.minY + 0.1, box.minZ - 0.08, box.maxX + 0.08, box.maxY - 0.1, box.maxZ + 0.08);
        boolean touchingWall = entity.horizontalCollision || !entity.level().noCollision(entity, wallBox);
        Vec3 motion = entity.getDeltaMovement();

        if (touchingWall) {
            if (entity.isCrouching()) {
                // Sticking: zero vertical movement, zero slide, reset fall distance
                entity.setDeltaMovement(motion.x * 0.5, 0.0, motion.z * 0.5);
                entity.resetFallDistance();
                entity.hasImpulse = true;
            } else {
                // Climbing up: moving forward or jumping
                if (entity.zza > 0.0F || entity.isJumping()) {
                    entity.setDeltaMovement(motion.x, 0.22, motion.z);
                    entity.resetFallDistance();
                    entity.hasImpulse = true;
                } else if (entity.zza < 0.0F) {
                    // Climbing down: moving backward
                    entity.setDeltaMovement(motion.x, -0.22, motion.z);
                    entity.resetFallDistance();
                    entity.hasImpulse = true;
                }
            }
        }

        // 3. Ceiling Adhesion
        if (!isInverted(entity)) {
            BlockPos headPos = BlockPos.containing(entity.getX(), box.maxY + 0.15, entity.getZ());
            boolean ceilingAbove = entity.level().getBlockState(headPos).isSolid();
            boolean upwardCollision = (entity.verticalCollision && motion.y >= -0.05) || (ceilingAbove && motion.y >= 0.0);
            if (!entity.onGround() && upwardCollision) {
                setGravity(entity, INVERTED_GRAVITY);
                SOLES_INVERTED_ENTITIES.add(entity.getUUID());
            }
        }

        // 4. Ceiling traction & surface locking
        applySurfaceLocking(entity);

        // 5. 360° Edge-Wrapping around block faces (Walking & Sneaking)
        handleEdgeWrap(entity);
    }

    /**
     * Handles 360° edge-wrapping around block faces and platforms (walking & sneaking).
     */
    public static void handleEdgeWrap(LivingEntity entity) {
        boolean isMoving = entity.zza != 0.0F || entity.xxa != 0.0F;
        if (!isMoving && !entity.isCrouching()) return;
        if (entity.isJumping()) return;

        long gameTime = entity.level().getGameTime();
        Long cd = SOLES_FLIP_COOLDOWN.get(entity.getUUID());
        if (cd != null) {
            if (gameTime < cd) {
                return;
            }
            SOLES_FLIP_COOLDOWN.remove(entity.getUUID());
        }
        if (SOLES_FLIP_COOLDOWN.size() > 50) {
            SOLES_FLIP_COOLDOWN.entrySet().removeIf(entry -> gameTime >= entry.getValue());
        }

        Vec3 look = entity.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0, look.z);
        if (forward.lengthSqr() > 1e-4) {
            forward = forward.normalize();
        } else {
            forward = new Vec3(0.0, 0.0, 1.0);
        }
        Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
        Vec3 moveDir = forward.scale(entity.zza).add(right.scale(entity.xxa));
        if (moveDir.lengthSqr() > 1e-4) {
            moveDir = moveDir.normalize();
        } else {
            if (entity.zza == 0.0F && entity.xxa == 0.0F) {
                return;
            }
            moveDir = forward;
        }

        Vec3 pos = entity.position();

        if (!isInverted(entity)) {
            // Normal (floor) -> check for ledge and floating platform underside
            BlockPos standingOn = BlockPos.containing(pos.x, pos.y - 0.2, pos.z);
            BlockState standingState = entity.level().getBlockState(standingOn);
            if (!standingState.isSolid()) {
                standingOn = BlockPos.containing(pos.x, pos.y - 0.05, pos.z);
                standingState = entity.level().getBlockState(standingOn);
            }

            if (standingState.isSolid()) {
                Vec3 stepCheck = pos.add(moveDir.scale(0.35));
                BlockPos aheadBelow = BlockPos.containing(stepCheck.x, pos.y - 0.5, stepCheck.z);
                BlockState aheadState = entity.level().getBlockState(aheadBelow);

                if (!aheadState.isSolid()) {
                    BlockPos undersidePos = standingOn.below();
                    BlockState underState = entity.level().getBlockState(undersidePos);
                    if (!underState.isSolid()) {
                        // Floating platform underside exists: flip gravity to Inverted (-0.08)
                        SOLES_FLIP_COOLDOWN.put(entity.getUUID(), gameTime + 10L);
                        setGravity(entity, INVERTED_GRAVITY);
                        SOLES_INVERTED_ENTITIES.add(entity.getUUID());

                        double destX = pos.x;
                        double destZ = pos.z;
                        double testX = pos.x + moveDir.x * 0.2;
                        double testZ = pos.z + moveDir.z * 0.2;
                        BlockPos testCeil = BlockPos.containing(testX, standingOn.getY() + 0.5, testZ);
                        if (testCeil.equals(standingOn) || entity.level().getBlockState(testCeil).isSolid()) {
                            destX = testX;
                            destZ = testZ;
                        }

                        double destY = standingOn.getY() - entity.getBbHeight() - 0.05;
                        entity.teleportTo(destX, destY, destZ);
                        entity.setDeltaMovement(0.0, 0.05, 0.0);

                        entity.level().playSound(null, destX, destY, destZ, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.3F);
                        if (entity.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ModParticles.SPECTRUM_SPARKLE.get(), destX, destY + entity.getBbHeight() * 0.5, destZ, 16, 0.25, 0.25, 0.25, 0.05);
                        }
                    } else {
                        // Solid wall below: lock onto wall to prevent plunging into void
                        Vec3 motion = entity.getDeltaMovement();
                        entity.setDeltaMovement(motion.x * 0.2, 0.0, motion.z * 0.2);
                        entity.resetFallDistance();
                        entity.hasImpulse = true;
                    }
                }
            }
        } else {
            // Inverted (ceiling) -> check for platform edge to flip back to Normal
            AABB box = entity.getBoundingBox();
            BlockPos ceilingOn = BlockPos.containing(pos.x, box.maxY + 0.2, pos.z);
            BlockState ceilingState = entity.level().getBlockState(ceilingOn);
            if (!ceilingState.isSolid()) {
                ceilingOn = BlockPos.containing(pos.x, box.maxY + 0.05, pos.z);
                ceilingState = entity.level().getBlockState(ceilingOn);
            }

            if (ceilingState.isSolid()) {
                Vec3 stepCheck = pos.add(moveDir.scale(0.35));
                BlockPos aheadCeiling = BlockPos.containing(stepCheck.x, box.maxY + 0.2, stepCheck.z);
                BlockState aheadCeilState = entity.level().getBlockState(aheadCeiling);

                if (!aheadCeilState.isSolid()) {
                    BlockPos topFloorPos = ceilingOn.above();
                    BlockState topFloorState = entity.level().getBlockState(topFloorPos);
                    if (!topFloorState.isSolid()) {
                        // Platform top surface exists: flip gravity back to Normal
                        SOLES_FLIP_COOLDOWN.put(entity.getUUID(), gameTime + 10L);
                        resetGravity(entity);
                        SOLES_INVERTED_ENTITIES.remove(entity.getUUID());

                        double destX = pos.x;
                        double destZ = pos.z;
                        double testX = pos.x + moveDir.x * 0.2;
                        double testZ = pos.z + moveDir.z * 0.2;
                        BlockPos testFloor = BlockPos.containing(testX, ceilingOn.getY() + 0.5, testZ);
                        if (testFloor.equals(ceilingOn) || entity.level().getBlockState(testFloor).isSolid()) {
                            destX = testX;
                            destZ = testZ;
                        }

                        double destY = ceilingOn.getY() + 1.05;
                        entity.teleportTo(destX, destY, destZ);
                        entity.setDeltaMovement(0.0, -0.05, 0.0);

                        entity.level().playSound(null, destX, destY, destZ, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.3F);
                        if (entity.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ModParticles.SPECTRUM_SPARKLE.get(), destX, destY + entity.getBbHeight() * 0.5, destZ, 16, 0.25, 0.25, 0.25, 0.05);
                        }
                    } else {
                        // Solid wall above: lock onto wall
                        Vec3 motion = entity.getDeltaMovement();
                        entity.setDeltaMovement(motion.x * 0.2, 0.0, motion.z * 0.2);
                        entity.resetFallDistance();
                        entity.hasImpulse = true;
                    }
                }
            }
        }
    }

    /**
     * Applies surface locking and ceiling traction when inverted or near ceilings to prevent slipping.
     */
    public static void applySurfaceLocking(LivingEntity entity) {
        if (entity == null) return;

        boolean inverted = isInverted(entity);
        Vec3 pos = entity.position();
        AABB box = entity.getBoundingBox();
        BlockPos ceilingPos = BlockPos.containing(pos.x, box.maxY + 0.15, pos.z);
        boolean touchingCeiling = entity.verticalCollision && entity.getDeltaMovement().y >= 0.0;
        boolean hasCeilingAbove = entity.level().getBlockState(ceilingPos).isSolid();

        if (inverted && (touchingCeiling || hasCeilingAbove)) {
            Vec3 motion = entity.getDeltaMovement();
            // Surface lock against ceiling: cancel upward drift to avoid jittering
            if (motion.y > 0.0) {
                entity.setDeltaMovement(motion.x, 0.0, motion.z);
            }
            entity.resetFallDistance();

            // Ceiling traction: counteract horizontal slipping when stationary
            if (entity.xxa == 0.0F && entity.zza == 0.0F) {
                entity.setDeltaMovement(motion.x * 0.5, entity.getDeltaMovement().y, motion.z * 0.5);
            }
            entity.hasImpulse = true;
        } else if (entity.onGround()) {
            entity.resetFallDistance();
        }
    }

    /**
     * Finds the nearest active singularity field position to a given point in the level.
     */
    public static Vec3 getNearestSingularityPos(Level level, Vec3 pos, double maxRadius) {
        if (pos == null) return null;
        GravityField field = GravityFieldManager.getNearestSingularity(level, pos, maxRadius);
        return field != null ? field.getCenterVec() : null;
    }

    public static void setGravity(LivingEntity entity, double targetGravity) {
        setGravity(entity, GRAVITY_MOD_ID, targetGravity);
    }

    public static void setGravity(LivingEntity entity, double targetGravity, double safeFallExtra) {
        setGravity(entity, GRAVITY_MOD_ID, targetGravity);
    }

    /**
     * Sets target gravity for an entity by applying custom attribute modifiers with fall damage compensation.
     */
    public static void setGravity(LivingEntity entity, ResourceLocation sourceId, double targetGravity) {
        if (entity == null) return;
        if (isAnchored(entity) && targetGravity != VANILLA_BASE_GRAVITY) return;

        AttributeInstance gravInst = entity.getAttribute(Attributes.GRAVITY);
        if (gravInst == null) return;

        // Remove existing modifier with this id
        gravInst.removeModifier(sourceId);

        // Base gravity is typically 0.08. We add the difference (target - base)
        double baseVal = gravInst.getBaseValue();
        double offset = targetGravity - baseVal;

        gravInst.addTransientModifier(new AttributeModifier(
                sourceId,
                offset,
                AttributeModifier.Operation.ADD_VALUE
        ));

        // Adjust fall damage & safe fall distance so players do not die from low/inverted gravity leaps
        AttributeInstance safeFall = entity.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        AttributeInstance fallDmg = entity.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);

        if (hasGravitonSoles(entity)) {
            // Graviton Soles completely eliminate fall damage
            if (safeFall != null) {
                safeFall.removeModifier(sourceId);
                safeFall.addTransientModifier(new AttributeModifier(sourceId, 1000.0, AttributeModifier.Operation.ADD_VALUE));
            }
            if (fallDmg != null) {
                fallDmg.removeModifier(sourceId);
                fallDmg.addTransientModifier(new AttributeModifier(sourceId, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else {
            if (safeFall != null) {
                safeFall.removeModifier(sourceId);
                if (targetGravity <= 0.0) {
                    safeFall.addTransientModifier(new AttributeModifier(sourceId, 40.0, AttributeModifier.Operation.ADD_VALUE));
                } else if (targetGravity < baseVal) {
                    safeFall.addTransientModifier(new AttributeModifier(sourceId, 16.0, AttributeModifier.Operation.ADD_VALUE));
                }
            }

            if (fallDmg != null) {
                fallDmg.removeModifier(sourceId);
                if (targetGravity <= 0.0) {
                    fallDmg.addTransientModifier(new AttributeModifier(sourceId, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                } else if (targetGravity < baseVal) {
                    fallDmg.addTransientModifier(new AttributeModifier(sourceId, -0.75, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            }
        }
    }

    public static void resetGravity(LivingEntity entity) {
        resetGravity(entity, GRAVITY_MOD_ID);
    }

    /**
     * Clears an active gravity override modifier from an entity.
     */
    public static void resetGravity(LivingEntity entity, ResourceLocation sourceId) {
        if (entity == null) return;

        AttributeInstance gravInst = entity.getAttribute(Attributes.GRAVITY);
        if (gravInst != null) gravInst.removeModifier(sourceId);

        AttributeInstance safeFall = entity.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (safeFall != null) safeFall.removeModifier(sourceId);

        AttributeInstance fallDmg = entity.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
        if (fallDmg != null) fallDmg.removeModifier(sourceId);
    }
}