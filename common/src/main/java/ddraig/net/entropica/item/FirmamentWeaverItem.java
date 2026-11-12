package ddraig.net.entropica.item;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.BarrierFieldManager;
import ddraig.net.entropica.forcefield.BarrierFilterMode;
import ddraig.net.entropica.forcefield.BarrierShape;
import ddraig.net.entropica.forcefield.SnapResult;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Handheld celestial instrument to weave, shape, and dispel forcefield barriers in mid-air.
 * Supports Two-Point Drag & Snap ("Anchor & Stretch"), Edge-Fusing seamless snapping,
 * shape cycling, and creator permissions.
 */
public class FirmamentWeaverItem extends Item {

    public static final String TAG_HAS_ANCHOR = "HasAnchor";
    public static final String TAG_ANCHOR_X = "AnchorX";
    public static final String TAG_ANCHOR_Y = "AnchorY";
    public static final String TAG_ANCHOR_Z = "AnchorZ";
    public static final String TAG_ANCHOR_DIM = "AnchorDim";
    public static final String TAG_ANCHOR_TIME = "AnchorTime";
    public static final String TAG_ACTIVE_SHAPE = "ActiveShape";
    public static final double SNAP_TOLERANCE = 0.50;
    public static final double MAX_SPAN_DISTANCE = 32.0;

    public FirmamentWeaverItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean hasAnchor(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getBoolean(TAG_HAS_ANCHOR).orElse(false);
    }

    public static Optional<Vec3> getAnchor(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.getBoolean(TAG_HAS_ANCHOR).orElse(false)) {
            double x = tag.getDouble(TAG_ANCHOR_X).orElse(0.0);
            double y = tag.getDouble(TAG_ANCHOR_Y).orElse(0.0);
            double z = tag.getDouble(TAG_ANCHOR_Z).orElse(0.0);
            return Optional.of(new Vec3(x, y, z));
        }
        return Optional.empty();
    }

    public static void setAnchor(ItemStack stack, Vec3 pos) {
        setAnchor(stack, pos, null);
    }

    public static void setAnchor(ItemStack stack, Vec3 pos, @Nullable Level level) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putBoolean(TAG_HAS_ANCHOR, true);
        tag.putDouble(TAG_ANCHOR_X, pos.x);
        tag.putDouble(TAG_ANCHOR_Y, pos.y);
        tag.putDouble(TAG_ANCHOR_Z, pos.z);
        if (level != null) {
            tag.putString(TAG_ANCHOR_DIM, level.dimension().location().toString());
            tag.putLong(TAG_ANCHOR_TIME, level.getGameTime());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void clearAnchor(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.remove(TAG_HAS_ANCHOR);
        tag.remove(TAG_ANCHOR_X);
        tag.remove(TAG_ANCHOR_Y);
        tag.remove(TAG_ANCHOR_Z);
        tag.remove(TAG_ANCHOR_DIM);
        tag.remove(TAG_ANCHOR_TIME);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, net.minecraft.world.entity.Entity entity, @Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        tickAnchor(stack, level, entity);
    }

    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        tickAnchor(stack, level, entity);
    }

    private static void tickAnchor(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity) {
        if (!hasAnchor(stack)) return;

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        // Check dimension change
        String anchorDim = tag.getString(TAG_ANCHOR_DIM).orElse("");
        String currentDim = level.dimension().location().toString();
        if (!anchorDim.isEmpty() && !anchorDim.equals(currentDim)) {
            clearAnchor(stack);
            if (entity instanceof Player player && !level.isClientSide()) {
                player.displayClientMessage(Component.literal("§e[Firmament Weaver] §7Anchor Point A expired (dimension changed)."), true);
            }
            return;
        }

        // Check distance limit (>32m)
        Optional<Vec3> optA = getAnchor(stack);
        if (optA.isPresent()) {
            Vec3 anchor = optA.get();
            if (entity.position().distanceTo(anchor) > MAX_SPAN_DISTANCE) {
                clearAnchor(stack);
                if (entity instanceof Player player && !level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§e[Firmament Weaver] §7Anchor Point A expired (exceeded 32m maximum span)."), true);
                }
                return;
            }
        }

        // Check 60s timeout (1200 ticks)
        long anchorTime = tag.getLong(TAG_ANCHOR_TIME).orElse(0L);
        if (anchorTime > 0L && (level.getGameTime() - anchorTime) > 1200L) {
            clearAnchor(stack);
            if (entity instanceof Player player && !level.isClientSide()) {
                player.displayClientMessage(Component.literal("§e[Firmament Weaver] §7Anchor Point A expired (timeout)."), true);
            }
        }
    }

    /**
     * Dispels the nearest barrier within 5 blocks owned by the player.
     */
    public static boolean dispelNearestBarrier(Player player) {
        Level level = player.level();
        Vec3 eyePos = player.getEyePosition();
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                eyePos.x - 5.0, eyePos.y - 5.0, eyePos.z - 5.0,
                eyePos.x + 5.0, eyePos.y + 5.0, eyePos.z + 5.0
        );
        List<ForcefieldBarrierEntity> barriers = level.getEntitiesOfClass(
                ForcefieldBarrierEntity.class, box,
                b -> b.isAlive() && b.isActive()
        );

        ForcefieldBarrierEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (ForcefieldBarrierEntity b : barriers) {
            UUID owner = b.getOwnerUUID().orElse(null);
            boolean isCreator = owner != null && owner.equals(player.getUUID());
            if (isCreator || player.isCreative()) {
                if (b.isBossEncounter() && !player.isCreative()) continue;
                double dist = b.distanceToSqr(eyePos);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = b;
                }
            }
        }

        if (closest != null) {
            closest.dispelByCreator(player);
            return true;
        }
        return false;
    }

    public static BarrierShape getShape(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        int ord = customData.copyTag().getInt(TAG_ACTIVE_SHAPE).orElse(0);
        return BarrierShape.fromOrdinal(ord);
    }

    public static void setShape(ItemStack stack, BarrierShape shape) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putInt(TAG_ACTIVE_SHAPE, shape.ordinal());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static BarrierShape cycleShape(ItemStack stack) {
        return cycleShape(stack, 1);
    }

    public static BarrierShape cycleShape(ItemStack stack, int direction) {
        BarrierShape current = getShape(stack);
        int count = BarrierShape.values().length;
        int nextOrd = (current.ordinal() + direction) % count;
        if (nextOrd < 0) nextOrd += count;
        BarrierShape next = BarrierShape.fromOrdinal(nextOrd);
        setShape(stack, next);
        return next;
    }

    public static BarrierFilterMode getFilter(ItemStack stack) {
        return BarrierFilterMode.ALL_ENTITIES;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();

        if (player != null && player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Vec3 clickLoc = context.getClickLocation();

        if (!hasAnchor(stack)) {
            // First Click (Point A)
            if (!level.isClientSide()) {
                setAnchor(stack, clickLoc, level);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, clickLoc.x, clickLoc.y, clickLoc.z, 15, 0.1, 0.1, 0.1, 0.05);
                }
                level.playSound(null, clickLoc.x, clickLoc.y, clickLoc.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
                if (player != null) {
                    player.displayClientMessage(Component.literal("§d[Firmament Weaver] §fAnchor Point A set. Click Point B to span barrier."), true);
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            // Second Click (Point B) - Stretch & Span
            Optional<Vec3> optA = getAnchor(stack);
            if (optA.isEmpty()) {
                clearAnchor(stack);
                return InteractionResult.PASS;
            }

            Vec3 pointA = optA.get();
            Vec3 pointB = clickLoc;

            double dist = pointA.distanceTo(pointB);
            if (dist < 1.0) {
                if (!level.isClientSide() && player != null) {
                    player.displayClientMessage(Component.literal("§c[Firmament Weaver] §7Points are too close (Minimum span: 1.0 block)."), true);
                }
                return InteractionResult.SUCCESS;
            }

            if (dist > MAX_SPAN_DISTANCE) {
                if (!level.isClientSide() && player != null) {
                    player.displayClientMessage(Component.literal("§c[Firmament Weaver] §7Points exceed maximum 32m span."), true);
                }
                return InteractionResult.SUCCESS;
            }

            double dx = pointB.x - pointA.x;
            double dy = pointB.y - pointA.y;
            double dz = pointB.z - pointA.z;

            Vec3 center = pointA.add(pointB).scale(0.5);
            float width = (float) Math.min(32.0, Math.max(1.0, Math.sqrt(dx * dx + dz * dz)));
            float height = (float) Math.min(32.0, Math.max(1.0, Math.abs(dy)));

            if (Math.abs(dy) < 0.5) {
                height = 3.5F;
                center = new Vec3(center.x, pointA.y + height * 0.5, center.z);
            }

            float yaw;
            if (Math.abs(dx) < 1e-4 && Math.abs(dz) < 1e-4) {
                yaw = player != null ? player.getYRot() : 0.0F;
            } else {
                yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI);
            }

            float pitch = 0.0F;

            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                SnapResult snap = BarrierFieldManager.findSnapAlignment(level, center, yaw, width, height, SNAP_TOLERANCE);
                if (snap.isSnapped() && snap.snappedPos() != null
                        && Double.isFinite(snap.snappedPos().x)
                        && Double.isFinite(snap.snappedPos().y)
                        && Double.isFinite(snap.snappedPos().z)) {
                    center = snap.snappedPos();
                    yaw = snap.snappedYaw();
                    pitch = snap.snappedPitch();
                }

                ForcefieldBarrierEntity barrier = new ForcefieldBarrierEntity(ModEntityTypes.FORCEFIELD_BARRIER.get(), level);
                barrier.setPos(center.x, center.y, center.z);
                barrier.setYRot(yaw);
                barrier.setXRot(pitch);
                barrier.setShape(BarrierShape.PLANAR_QUAD);
                barrier.setWidth(width);
                barrier.setHeight(height);
                barrier.setFilterMode(getFilter(stack));
                if (player != null) {
                    barrier.setOwnerUUID(player.getUUID());
                    barrier.setOwnerName(player.getName().getString());
                }

                serverLevel.addFreshEntity(barrier);
                clearAnchor(stack);

                level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.2F);
                level.playSound(null, center.x, center.y, center.z, ModSounds.FORCEFIELD_BOUNCE.get(), SoundSource.PLAYERS, 0.8F, 1.1F);
                serverLevel.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 25, width * 0.3, height * 0.3, width * 0.3, 0.05);

                if (player != null) {
                    if (snap.isSnapped()) {
                        player.displayClientMessage(
                                Component.literal("§d[Firmament Weaver] §aSeamless Edge-Fused §8(§b" + snap.snapType().getDisplayName() + "§8)"),
                                true
                        );
                    } else {
                        player.displayClientMessage(Component.literal("§d[Firmament Weaver] §fSpanned Planar Barrier between Point A and B."), true);
                    }
                }
            } else {
                clearAnchor(stack);
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (hasAnchor(stack)) {
                // Cancel Point A
                clearAnchor(stack);
                if (!level.isClientSide()) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                    player.displayClientMessage(Component.literal("§e[Firmament Weaver] §7Cancelled anchor Point A."), true);
                }
                return InteractionResult.SUCCESS;
            } else {
                // Cycle active shape mode
                BarrierShape next = cycleShape(stack);
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.literal("§d[Firmament Weaver] §fShape: §b" + next.getDisplayName()), true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 1.4F);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (hasAnchor(stack)) {
            // Cancel Point A on air click as well to prevent getting stuck
            clearAnchor(stack);
            if (!level.isClientSide()) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
                player.displayClientMessage(Component.literal("§e[Firmament Weaver] §7Cancelled anchor Point A."), true);
            }
            return InteractionResult.SUCCESS;
        }

        // Raycast up to 6 blocks
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 targetPos = eyePos.add(lookVec.scale(6.0));

        BlockHitResult blockHit = level.clip(new ClipContext(eyePos, targetPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() == HitResult.Type.MISS && !player.isCreative()) {
            // Looking at empty sky/air: do not accidentally spawn a barrier in front of player's face
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("§d[Firmament Weaver] §7Click a block to anchor Point A, or Crouch + Right-Click to cycle shapes."), true);
            }
            return InteractionResult.PASS;
        }

        Vec3 spawnPos = (blockHit.getType() != HitResult.Type.MISS) ? blockHit.getLocation() : eyePos.add(lookVec.scale(3.5));

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            BarrierShape currentShape = getShape(stack);
            BarrierFilterMode currentFilter = getFilter(stack);

            float width = 4.0F;
            float height = 3.5F;
            float radius = 3.5F;
            float yaw = player.getYRot();
            float pitch = (currentShape == BarrierShape.PLANAR_QUAD) ? 0.0F : player.getXRot();

            // Check edge-fusing snapping for free air placement
            SnapResult snap = BarrierFieldManager.findSnapAlignment(level, spawnPos, yaw, pitch, width, height, currentShape, 0.5);
            if (snap.isSnapped()) {
                spawnPos = snap.snappedPos();
                yaw = snap.snappedYaw();
                pitch = snap.snappedPitch();
            }

            ForcefieldBarrierEntity barrier = new ForcefieldBarrierEntity(ModEntityTypes.FORCEFIELD_BARRIER.get(), level);
            barrier.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            barrier.setYRot(yaw);
            barrier.setXRot(pitch);
            barrier.setShape(currentShape);
            barrier.setFilterMode(currentFilter);
            barrier.setOwnerUUID(player.getUUID());
            barrier.setOwnerName(player.getName().getString());

            switch (currentShape) {
                case PLANAR_QUAD -> { barrier.setWidth(width); barrier.setHeight(height); }
                case CIRCULAR_DISC -> barrier.setRadius(radius);
                case HEMISPHERICAL_DOME -> barrier.setRadius(5.0F);
                case SPHERICAL_BUBBLE -> barrier.setRadius(4.0F);
                case CYLINDER -> { barrier.setRadius(3.0F); barrier.setHeight(6.0F); }
                case CONVEX_POLYGON -> { barrier.setWidth(5.0F); barrier.setHeight(4.0F); }
            }

            serverLevel.addFreshEntity(barrier);

            level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.2F);
            serverLevel.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y, spawnPos.z, 20, 0.5, 0.5, 0.5, 0.05);

            if (snap.isSnapped()) {
                player.displayClientMessage(
                        Component.literal("§d[Firmament Weaver] §aSeamless Edge-Fused §8(§b" + snap.snapType().getDisplayName() + "§8)"),
                        true
                );
            } else {
                player.displayClientMessage(
                        Component.literal("§d[Firmament Weaver] §fFormed §b" + currentShape.getDisplayName() + " §8(§e" + currentFilter.getDisplayName() + "§8)"),
                        true
                );
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("§7Weaves paper-thin forcefield barriers in mid-air."));
        tooltipComponents.accept(Component.literal("§eRight-Click Block§7: Set Point A, then Point B to span barrier (max 32m)"));
        tooltipComponents.accept(Component.literal("§eRight-Click Air§7: Cancel Point A / Prompt"));
        tooltipComponents.accept(Component.literal("§eShift + Scroll / Right-Click Air§7: Cycle barrier shape"));
        tooltipComponents.accept(Component.literal("§cLeft-Click Barrier§7: Dispel (creator only)"));
        tooltipComponents.accept(Component.literal("§cShift + Left-Click§7: Proximity dispel nearest barrier (5m)"));
        tooltipComponents.accept(Component.literal("§8Does not occupy block space. Bounces entities on arrival."));
    }
}
