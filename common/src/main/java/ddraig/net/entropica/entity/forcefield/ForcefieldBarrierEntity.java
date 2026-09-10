package ddraig.net.entropica.entity.forcefield;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.forcefield.*;
import ddraig.net.entropica.item.FirmamentWeaverItem;
import ddraig.net.entropica.network.BarrierImpactPayload;
import ddraig.net.entropica.registry.ModSounds;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import ddraig.net.entropica.item.SpectralDyeItem;
import ddraig.net.entropica.inventory.barrier.BarrierConfigMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Paper-thin, non-block forcefield barrier entity.
 * Does NOT occupy Minecraft block space—blocks can be placed freely within or around it.
 * Supports arbitrary geometric shapes (quads, discs, domes, spheres, cylinders),
 * continuous swept collision detection, side-relative elastic bounce reflection,
 * creator permissions & whitelisting, and the 6 Apex Predator themes.
 */
public class ForcefieldBarrierEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_SHAPE =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_WIDTH =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_FILTER_MODE =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PREDATOR_THEME =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_BOUNCE_ELASTICITY =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_BOSS_ENCOUNTER =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_OWNER_UUID =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_OWNER_NAME =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_IS_ACTIVE =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ONE_WAY =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLOR_TINT =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REDSTONE_MODE =
            SynchedEntityData.defineId(ForcefieldBarrierEntity.class, EntityDataSerializers.INT);

    private final Set<UUID> whitelist = new CopyOnWriteArraySet<>();
    private final List<String> whitelistUsernames = new CopyOnWriteArrayList<>();
    private UUID bossEntityUUID = null;
    private int deactivationDebounce = 0;

    // Client-side ripple rendering queue: (impactVec, triggerTick, intensity)
    public record RippleImpact(Vec3 pos, long tick, float intensity) {}
    private final List<RippleImpact> activeRipples = Collections.synchronizedList(new ArrayList<>());

    public ForcefieldBarrierEntity(EntityType<? extends ForcefieldBarrierEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        BarrierFieldManager.registerBarrier(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SHAPE, BarrierShape.PLANAR_QUAD.ordinal());
        builder.define(DATA_WIDTH, 4.0F);
        builder.define(DATA_HEIGHT, 4.0F);
        builder.define(DATA_RADIUS, 5.0F);
        builder.define(DATA_FILTER_MODE, BarrierFilterMode.ALL_ENTITIES.ordinal());
        builder.define(DATA_PREDATOR_THEME, ApexPredatorTheme.STANDARD.ordinal());
        builder.define(DATA_BOUNCE_ELASTICITY, 1.25F);
        builder.define(DATA_IS_BOSS_ENCOUNTER, false);
        builder.define(DATA_OWNER_UUID, "");
        builder.define(DATA_OWNER_NAME, "");
        builder.define(DATA_IS_ACTIVE, true);
        builder.define(DATA_ONE_WAY, false);
        builder.define(DATA_COLOR_TINT, 0);
        builder.define(DATA_REDSTONE_MODE, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_SHAPE.equals(key) || DATA_WIDTH.equals(key) || DATA_HEIGHT.equals(key) || DATA_RADIUS.equals(key)) {
            updateBoundingBox();
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Register in spatial level manager
        BarrierFieldManager.registerBarrier(this);

        // Ensure bounding box matches current position
        if (this.tickCount % 100 == 0) {
            updateBoundingBox();
        }

        // Clean up stale ripples on client
        if (this.level().isClientSide() && !activeRipples.isEmpty()) {
            long current = this.level().getGameTime();
            activeRipples.removeIf(r -> (current - r.tick()) > 40);
        }

        // Redstone & Materia Switchability sampling on server
        if (!this.level().isClientSide()) {
            int mode = getRedstoneMode();
            if (mode != 0) {
                boolean receivingPower = checkRedstonePower();
                boolean shouldBeActive = (mode == 1) ? !receivingPower : receivingPower;
                if (shouldBeActive) {
                    deactivationDebounce = 2;
                    if (!isActive()) {
                        setActive(true);
                    }
                } else {
                    if (deactivationDebounce > 0) {
                        deactivationDebounce--;
                    } else if (isActive()) {
                        setActive(false);
                    }
                }
            }
        }

        // Boss encounter tracking: if the boss dies or despawns, dissolve the barrier
        if (!this.level().isClientSide() && isBossEncounter() && bossEntityUUID != null && this.tickCount % 20 == 0) {
            if (this.level() instanceof ServerLevel serverLevel) {
                Entity boss = serverLevel.getEntity(bossEntityUUID);
                if (boss == null || !boss.isAlive()) {
                    dissolveInGlitter();
                }
            }
        }
    }

    public boolean checkRedstonePower() {
        Level lvl = this.level();
        BlockPos center = this.blockPosition();
        if (lvl.hasNeighborSignal(center)) return true;
        AABB box = this.getBoundingBox();
        if (box.getXsize() > 1.5 || box.getYsize() > 1.5 || box.getZsize() > 1.5) {
            BlockPos base = BlockPos.containing(this.getX(), box.minY, this.getZ());
            if (!base.equals(center) && lvl.hasNeighborSignal(base)) return true;
        }
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        BarrierFieldManager.unregisterBarrier(this);
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        updateBoundingBox();
        BarrierFieldManager.registerBarrier(this);
    }

    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        if (this.entityData != null) {
            BarrierShapeHandler handler = BarrierShapeRegistry.get(getShape().ordinal());
            if (handler != null) {
                return handler.computeBoundingBox(
                        pos, this.getYRot(), this.getXRot(),
                        getWidth(), getHeight(), getRadius()
                );
            }
        }
        return super.makeBoundingBox(pos);
    }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pose) {
        float maxDim = Math.max(Math.max(getWidth(), getHeight()), getRadius() * 2.0F);
        return net.minecraft.world.entity.EntityDimensions.scalable(Math.max(1.0F, maxDim), Math.max(1.0F, maxDim));
    }

    public void updateBoundingBox() {
        this.setBoundingBox(this.makeBoundingBox(this.position()));
    }

    /**
     * Continuous collision resolution when an entity moves from startPos to endPos.
     * Checks swept-ray manifold intersection, verifies permissions, and applies side-relative bounce.
     */
    public boolean handleEntityCollision(Entity entity, Vec3 startPos, Vec3 endPos) {
        if (!isAlive() || !isActive()) return false;

        UUID owner = getOwnerUUID().orElse(null);
        if (!getFilterMode().isBlocked(entity, owner, whitelist)) {
            return false; // Permitted, passes freely
        }

        double entityRadius = Math.max(0.15, entity.getBbWidth() * 0.5);
        BarrierShapeHandler handler = BarrierShapeRegistry.get(getShape().ordinal());
        BarrierRaycastHit hit;
        if (handler != null) {
            hit = handler.intersect(
                    this.position(), this.getYRot(), this.getXRot(),
                    getWidth(), getHeight(), getRadius(),
                    startPos, endPos, entityRadius, this
            );
        } else {
            hit = BarrierGeometry.intersect(
                    getShape(), this.position(), this.getYRot(), this.getXRot(),
                    getWidth(), getHeight(), getRadius(),
                    startPos, endPos, entityRadius
            );
        }

        if (!hit.hit() || hit.t() < 0.0 || hit.t() > 1.0) {
            return false;
        }

        return handleCollisionHit(entity, startPos, endPos, hit, entityRadius);
    }

    /**
     * Resolves physical impact, velocity reflection, safe displacement, and audio/visual ripples for a verified hit.
     */
    public boolean handleCollisionHit(Entity entity, Vec3 startPos, Vec3 endPos, BarrierRaycastHit hit, double entityRadius) {
        if (!isAlive() || !isActive()) return false;

        UUID owner = getOwnerUUID().orElse(null);
        if (!getFilterMode().isBlocked(entity, owner, whitelist)) {
            return false; // Permitted, passes freely
        }

        if (hit == null || !hit.hit()) {
            return false;
        }

        Vec3 approachDir = endPos.subtract(startPos);
        if (approachDir.lengthSqr() < 1e-6) {
            approachDir = entity.getDeltaMovement();
        }

        // --- One-Way Directional Valve ---
        // Forward approach (Front -> Back, approachDir . normal <= 0) passes through freely without displacement or impulse.
        // Reverse approach (Back -> Front, approachDir . normal > 0) triggers full elastic reflection.
        if (isOneWay()) {
            if (approachDir.lengthSqr() < 1e-6) {
                Vec3 posRel = startPos.subtract(position());
                if (posRel.dot(hit.surfaceNormal()) < 0.0) {
                    // Stationary entity on reverse side is blocked, proceed to bounce/displacement
                } else {
                    return false; // Permitted on front side
                }
            } else if (approachDir.dot(hit.surfaceNormal()) <= 0.0) {
                return false;
            }
        }

        // --- Calculate Side-Relative Normal & Elastic Bounce ---
        Vec3 nEff = hit.getEffectiveNormal(approachDir);
        float rawElasticity = getBounceElasticity();
        double elasticity = (!Float.isFinite(rawElasticity)) ? 1.0 : Math.max(0.0, (double) rawElasticity);

        Vec3 v = entity.getDeltaMovement();
        double dot = v.dot(nEff);

        // Reflection formula: v' = v - (1 + e) * (v . n) * n
        Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * dot));

        // Ensure minimum bounce velocity along nEff so slow-walking entities rebound cleanly
        double minImpulse = 0.45 * Math.max(1.0, elasticity);
        if (reflected.dot(nEff) < minImpulse) {
            reflected = reflected.add(nEff.scale(minImpulse - Math.max(0.0, reflected.dot(nEff))));
        }

        // Reposition entity safely outside the membrane on the side they approached from
        Vec3 safePos = hit.impactPoint().add(nEff.scale(entityRadius + 0.12));
        if (Math.abs(nEff.y) < 0.2) {
            safePos = new Vec3(safePos.x, entity.getY(), safePos.z);
        }
        entity.setPos(safePos.x, safePos.y, safePos.z);
        entity.setDeltaMovement(reflected);
        entity.resetFallDistance();
        entity.hasImpulse = true;
        entity.hurtMarked = true;

        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            mob.getNavigation().stop();
        }

        // If projectile, update flight heading and rotation
        if (entity instanceof Projectile projectile) {
            projectile.shoot(reflected.x, reflected.y, reflected.z, (float) reflected.length(), 0.0F);
        }

        // Trigger audio chime and visual ripple effects scaling with impact velocity
        float speed = (float) Math.abs(dot);
        float intensity = (float) Math.min(2.0, Math.max(0.2, speed * 0.1));
        onBounceImpact(hit.impactPoint(), nEff, intensity);

        return true;
    }

    public void onBounceImpact(Vec3 impactPos, Vec3 normal, float intensity) {
        // Play forcefield rebound sound
        this.level().playSound(
                null,
                impactPos.x, impactPos.y, impactPos.z,
                ModSounds.FORCEFIELD_BOUNCE.get(),
                SoundSource.BLOCKS,
                1.0F,
                0.9F + (this.random.nextFloat() * 0.2F)
        );

        if (this.level() instanceof ServerLevel serverLevel) {
            // Spawn starlight spark particles at the impact point
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    impactPos.x, impactPos.y, impactPos.z,
                    Math.max(6, (int) (12 * intensity)),
                    normal.x * 0.1, normal.y * 0.1, normal.z * 0.1,
                    0.08
            );

            // Sync ripple packet to nearby tracking clients
            BarrierImpactPayload payload = new BarrierImpactPayload(
                    this.getId(),
                    (float) impactPos.x,
                    (float) impactPos.y,
                    (float) impactPos.z,
                    intensity
            );
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(impactPos) <= 64.0 * 64.0) {
                    NetworkManager.sendToPlayer(player, payload);
                }
            }
        } else {
            addRipple(impactPos, intensity);
        }
    }

    public void onBounceImpact(Vec3 impactPos, Vec3 normal) {
        onBounceImpact(impactPos, normal, 1.0F);
    }

    public void addRipple(Vec3 pos, float intensity) {
        activeRipples.add(new RippleImpact(pos, this.level().getGameTime(), intensity));
    }

    public List<RippleImpact> getActiveRipples() {
        return activeRipples;
    }

    /**
     * Dispels the barrier quietly on creator or creative operator manual action (e.g. left-click with Firmament Weaver).
     * Plays a crisp, gentle bubble pop audio cue and subtle particle burst, completely decoupled from
     * the celebratory boss defeat fanfare (UI_TOAST_CHALLENGE_COMPLETE).
     *
     * @param player The player dispelling the barrier, or null if triggered programmatically
     */
    public void dispelByCreator(@Nullable Player player) {
        Level lvl = this.level();
        Vec3 pos = this.position();

        // 1. Crisp Bubble Pop Audio Cue (Decoupled from victory fanfare)
        lvl.playSound(null, pos.x, pos.y, pos.z, net.minecraft.sounds.SoundEvents.BUBBLE_POP, SoundSource.PLAYERS, 1.0F, 1.2F);

        // 2. Gentle Starlight & Smoke Puff Burst
        if (lvl instanceof ServerLevel serverLevel) {
            double spread = Math.min(1.0, Math.max(getWidth(), getRadius()) * 0.25);
            double hSpread = Math.min(1.0, getHeight() * 0.25);
            double centerY = pos.y + Math.min(1.0, getHeight() * 0.5);

            serverLevel.sendParticles(ParticleTypes.POOF, pos.x, centerY, pos.z, 12, spread, hSpread, spread, 0.05);
            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.x, centerY, pos.z, 8, spread, hSpread, spread, 0.04);
        }

        // 3. User Feedback Message
        if (player != null) {
            player.displayClientMessage(Component.literal("§7[Firmament Weaver] §fDispelled barrier."), true);
        }

        // 4. Entity Discard
        this.discard();
    }

    public void dissolveInGlitter() {
        Level lvl = this.level();
        Vec3 pos = this.position();

        // 1. Acoustic Fanfare
        lvl.playSound(null, pos.x, pos.y, pos.z, net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.2F, 1.0F);
        lvl.playSound(null, pos.x, pos.y, pos.z, net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.7F, 1.4F);
        lvl.playSound(null, pos.x, pos.y, pos.z, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5F, 1.2F);

        // 2. Multi-Tier Celestial Particles
        if (lvl instanceof ServerLevel serverLevel) {
            double spread = Math.max(2.0, Math.max(getWidth(), getRadius()) * 0.5);
            double hSpread = Math.max(1.5, getHeight() * 0.5);

            // Tier 1: Core Flash & Fireworks
            serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 1.0F, 1.0F), pos.x, pos.y + 1.0, pos.z, 2, 0.5, 0.5, 0.5, 0.0);
            serverLevel.sendParticles(ParticleTypes.FIREWORK, pos.x, pos.y + 1.0, pos.z, 75, spread, hSpread, spread, 0.22);

            // Tier 2: Radiating Starlight Beams & Totem Ascension
            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0, pos.z, 50, spread, hSpread, spread, 0.12);
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.x, pos.y + 0.5, pos.z, 40, spread, hSpread, spread, 0.25);

            // Tier 3: Celestial Shimmer
            serverLevel.sendParticles(ParticleTypes.GLOW, pos.x, pos.y + 1.0, pos.z, 50, spread, hSpread, spread, 0.05);
        }

        this.discard();
    }

    public static @Nullable Integer resolveColorTint(ItemStack held) {
        if (held.isEmpty()) return null;
        if (held.is(Items.WET_SPONGE)) {
            return 0; // Cleanser signal
        }
        if (held.is(Items.POTION)) {
            PotionContents contents = held.get(DataComponents.POTION_CONTENTS);
            if (contents != null && contents.is(Potions.WATER)) {
                return 0; // Cleanser signal
            }
        }
        if (held.getItem() instanceof DyeItem dyeItem) {
            return dyeItem.getDyeColor().getTextureDiffuseColor() & 0xFFFFFF;
        }
        if (held.getItem() instanceof SpectralDyeItem spectralDye) {
            return spectralDye.getColor() & 0xFFFFFF;
        }
        String itemId = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
        if (itemId.contains("astral_crystal")) {
            return 0x38BDF8;
        }
        if (itemId.contains("aeterium")) {
            return 0x67E8F9;
        }
        if (itemId.contains("ignisite")) {
            return 0xF59E0B;
        }
        if (itemId.contains("mortisite")) {
            return 0xDC2626;
        }
        return null;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        UUID owner = getOwnerUUID().orElse(null);
        boolean isCreator = owner != null && owner.equals(player.getUUID());
        boolean isCreative = player.isCreative();

        if (isBossEncounter() && !isCreative) {
            player.displayClientMessage(Component.literal("§cThe firmament barrier is bound to an active Apex Predator and cannot be dismantled!"), true);
            return InteractionResult.CONSUME;
        }

        ItemStack held = player.getItemInHand(hand);

        // 1. Materia and Dye Color Tinting / Cleansing
        Integer newTint = resolveColorTint(held);
        if (newTint != null) {
            if (!isCreator && !isCreative) {
                player.displayClientMessage(Component.literal("§cOnly the creator can dye this barrier!"), true);
                return InteractionResult.CONSUME;
            }

            if (newTint == 0) {
                // Reset to default iridescent sheen
                setColorTint(null);
                this.level().playSound(null, getX(), getY(), getZ(), net.minecraft.sounds.SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.displayClientMessage(Component.literal("§b[Firmament] §7Restored natural iridescent sheen."), true);
            } else {
                setColorTint(newTint);
                if (!isCreative) {
                    held.shrink(1);
                }
                this.level().playSound(null, getX(), getY(), getZ(), net.minecraft.sounds.SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.displayClientMessage(Component.literal("§d[Firmament] §fAttuned Materia color tint."), true);

                if (this.level() instanceof ServerLevel serverLevel) {
                    float r = ((newTint >> 16) & 0xFF) / 255.0F;
                    float g = ((newTint >> 8) & 0xFF) / 255.0F;
                    float b = (newTint & 0xFF) / 255.0F;
                    serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b),
                            getX(), getY(), getZ(), 25, Math.max(0.5, getWidth() * 0.3), Math.max(0.5, getHeight() * 0.3), Math.max(0.5, getWidth() * 0.3), 0.05);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 2. Weaver Interaction
        if (held.getItem() instanceof FirmamentWeaverItem) {
            if (isCreator || isCreative) {
                if (isBossEncounter() && !isCreative) {
                    player.displayClientMessage(Component.literal("§cThis barrier is bound to an active Apex Predator and cannot be modified!"), true);
                    return InteractionResult.CONSUME;
                }

                if (player.isShiftKeyDown()) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        dev.architectury.registry.menu.MenuRegistry.openExtendedMenu(
                                serverPlayer,
                                new dev.architectury.registry.menu.ExtendedMenuProvider() {
                                    @Override
                                    public void saveExtraData(FriendlyByteBuf buf) {
                                        buf.writeVarInt(getId());
                                        buf.writeVarInt(whitelistUsernames.size());
                                        for (String u : whitelistUsernames) {
                                            buf.writeUtf(u, 64);
                                        }
                                    }

                                    @Override
                                    public Component getDisplayName() {
                                        return Component.literal("Barrier Configuration");
                                    }

                                    @Override
                                    public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                                        return new BarrierConfigMenu(id, inv, ForcefieldBarrierEntity.this, new ArrayList<>(whitelistUsernames));
                                    }
                                }
                        );
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    player.displayClientMessage(Component.literal("§d[Firmament Weaver] §7Sneak + Right-Click to open Configuration GUI. Left-Click to dispel."), true);
                    return InteractionResult.SUCCESS;
                }
            } else {
                player.displayClientMessage(Component.literal("§cOnly the creator can modify or dispel this barrier!"), true);
                return InteractionResult.CONSUME;
            }
        }

        // 3. Creator Sneak-Interact Info
        if (isCreator || isCreative) {
            if (player.isShiftKeyDown()) {
                player.displayClientMessage(Component.literal("§d[Barrier] §fShape: §b" + getShape().getDisplayName() + " §8| §fFilter: §e" + getFilterMode().getDisplayName()), true);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) {
            if (isBossEncounter() && !player.isCreative()) {
                player.displayClientMessage(Component.literal("§cThe firmament barrier is bound to an active Apex Predator and cannot be dismantled!"), true);
                level.playSound(null, getX(), getY(), getZ(), net.minecraft.sounds.SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F);
                return false;
            }

            if (player.getMainHandItem().getItem() instanceof FirmamentWeaverItem) {
                UUID owner = getOwnerUUID().orElse(null);
                boolean isCreator = owner != null && owner.equals(player.getUUID());
                if (isCreator || player.isCreative()) {
                    dispelByCreator(player);
                    return true;
                } else {
                    player.displayClientMessage(Component.literal("§cOnly the creator can modify or dispel this barrier!"), true);
                    return false;
                }
            }
        }
        return false; // Immune to conventional damage
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    // --- Getters and Setters ---

    public BarrierShape getShape() {
        return BarrierShape.fromOrdinal(this.entityData.get(DATA_SHAPE));
    }

    public void setShape(BarrierShape shape) {
        this.entityData.set(DATA_SHAPE, shape.ordinal());
        updateBoundingBox();
    }

    public float getWidth() {
        return this.entityData.get(DATA_WIDTH);
    }

    public void setWidth(float width) {
        float val = (!Float.isFinite(width)) ? 4.0F : Math.max(0.5F, width);
        this.entityData.set(DATA_WIDTH, val);
        updateBoundingBox();
    }

    public float getHeight() {
        return this.entityData.get(DATA_HEIGHT);
    }

    public void setHeight(float height) {
        float val = (!Float.isFinite(height)) ? 4.0F : Math.max(0.5F, height);
        this.entityData.set(DATA_HEIGHT, val);
        updateBoundingBox();
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void setRadius(float radius) {
        float val = (!Float.isFinite(radius)) ? 4.0F : Math.max(0.5F, radius);
        this.entityData.set(DATA_RADIUS, val);
        updateBoundingBox();
    }

    public BarrierFilterMode getFilterMode() {
        return BarrierFilterMode.fromOrdinal(this.entityData.get(DATA_FILTER_MODE));
    }

    public void setFilterMode(BarrierFilterMode mode) {
        this.entityData.set(DATA_FILTER_MODE, mode.ordinal());
    }

    public ApexPredatorTheme getPredatorTheme() {
        return ApexPredatorTheme.fromOrdinal(this.entityData.get(DATA_PREDATOR_THEME));
    }

    public void setPredatorTheme(ApexPredatorTheme theme) {
        this.entityData.set(DATA_PREDATOR_THEME, theme.ordinal());
    }

    public float getBounceElasticity() {
        return this.entityData.get(DATA_BOUNCE_ELASTICITY);
    }

    public void setBounceElasticity(float elasticity) {
        float val = (!Float.isFinite(elasticity)) ? 1.0F : Math.max(0.0F, Math.min(3.0F, elasticity));
        this.entityData.set(DATA_BOUNCE_ELASTICITY, val);
    }

    public boolean isBossEncounter() {
        return this.entityData.get(DATA_IS_BOSS_ENCOUNTER);
    }

    public void setBossEncounter(boolean bossEncounter) {
        this.entityData.set(DATA_IS_BOSS_ENCOUNTER, bossEncounter);
    }

    public Optional<UUID> getOwnerUUID() {
        String str = this.entityData.get(DATA_OWNER_UUID);
        if (str == null || str.isEmpty()) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(str));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, uuid != null ? uuid.toString() : "");
    }

    public String getOwnerName() {
        return this.entityData.get(DATA_OWNER_NAME);
    }

    public void setOwnerName(String name) {
        this.entityData.set(DATA_OWNER_NAME, name != null ? name : "");
    }

    public Set<UUID> getWhitelist() {
        return whitelist;
    }

    public List<String> getWhitelistUsernames() {
        return Collections.unmodifiableList(this.whitelistUsernames);
    }

    public void setWhitelistUsernames(List<String> usernames, @Nullable MinecraftServer server) {
        this.whitelistUsernames.clear();
        this.whitelist.clear();
        for (String name : usernames) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                this.whitelistUsernames.add(trimmed);
                UUID resolvedUUID = null;
                if (server != null && server.getPlayerList() != null) {
                    ServerPlayer online = server.getPlayerList().getPlayerByName(trimmed);
                    if (online != null) {
                        resolvedUUID = online.getUUID();
                    }
                }
                if (resolvedUUID == null) {
                    resolvedUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + trimmed).getBytes(StandardCharsets.UTF_8));
                }
                this.whitelist.add(resolvedUUID);
            }
        }
    }

    public void addWhitelist(UUID playerUUID) {
        whitelist.add(playerUUID);
    }

    public void removeWhitelist(UUID playerUUID) {
        whitelist.remove(playerUUID);
    }

    public UUID getBossEntityUUID() {
        return bossEntityUUID;
    }

    public void setBossEntityUUID(UUID uuid) {
        this.bossEntityUUID = uuid;
    }

    public boolean isActive() {
        return this.entityData.get(DATA_IS_ACTIVE);
    }

    public void setActive(boolean active) {
        this.entityData.set(DATA_IS_ACTIVE, active);
    }

    public boolean isOneWay() {
        return this.entityData.get(DATA_ONE_WAY);
    }

    public void setOneWay(boolean oneWay) {
        this.entityData.set(DATA_ONE_WAY, oneWay);
    }

    public @Nullable Integer getColorTint() {
        int tint = this.entityData.get(DATA_COLOR_TINT);
        return (tint != 0 && (tint & 0x00FFFFFF) != 0) ? tint : null;
    }

    public void setColorTint(@Nullable Integer colorTint) {
        this.entityData.set(DATA_COLOR_TINT, colorTint != null ? colorTint : 0);
    }

    public int getColorTintRaw() {
        return this.entityData.get(DATA_COLOR_TINT);
    }

    public int getRedstoneMode() {
        return this.entityData.get(DATA_REDSTONE_MODE);
    }

    public void setRedstoneMode(int mode) {
        this.entityData.set(DATA_REDSTONE_MODE, Math.max(0, Math.min(2, mode)));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setShape(BarrierShape.fromOrdinal(input.read("Shape", Codec.INT).orElse(0)));
        setWidth(input.read("Width", Codec.FLOAT).orElse(4.0F));
        setHeight(input.read("Height", Codec.FLOAT).orElse(3.5F));
        setRadius(input.read("Radius", Codec.FLOAT).orElse(3.0F));
        setFilterMode(BarrierFilterMode.fromOrdinal(input.read("FilterMode", Codec.INT).orElse(0)));
        setPredatorTheme(ApexPredatorTheme.fromOrdinal(input.read("PredatorTheme", Codec.INT).orElse(0)));
        setBounceElasticity(input.read("Elasticity", Codec.FLOAT).orElse(1.25F));
        setBossEncounter(input.read("IsBossEncounter", Codec.BOOL).orElse(false));
        setActive(input.read("IsActive", Codec.BOOL).orElse(true));
        setOneWay(input.read("IsOneWay", Codec.BOOL).orElse(false));
        setColorTint(input.read("ColorTint", Codec.INT).orElse(0));
        setRedstoneMode(input.read("RedstoneMode", Codec.INT).orElse(0));

        input.read("OwnerUUID", Codec.STRING).ifPresent(str -> {
            try {
                setOwnerUUID(UUID.fromString(str));
            } catch (Exception ignored) {}
        });
        input.read("OwnerName", Codec.STRING).ifPresent(this::setOwnerName);
        input.read("BossUUID", Codec.STRING).ifPresent(str -> {
            try {
                setBossEntityUUID(UUID.fromString(str));
            } catch (Exception ignored) {}
        });

        whitelist.clear();
        List<String> list = input.read("Whitelist", Codec.STRING.listOf()).orElse(List.of());
        for (String s : list) {
            try {
                whitelist.add(UUID.fromString(s));
            } catch (Exception ignored) {}
        }

        whitelistUsernames.clear();
        List<String> uList = input.read("WhitelistUsernames", Codec.STRING.listOf()).orElse(List.of());
        whitelistUsernames.addAll(uList);

        updateBoundingBox();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("Shape", Codec.INT, getShape().ordinal());
        output.store("Width", Codec.FLOAT, getWidth());
        output.store("Height", Codec.FLOAT, getHeight());
        output.store("Radius", Codec.FLOAT, getRadius());
        output.store("FilterMode", Codec.INT, getFilterMode().ordinal());
        output.store("PredatorTheme", Codec.INT, getPredatorTheme().ordinal());
        output.store("Elasticity", Codec.FLOAT, getBounceElasticity());
        output.store("IsBossEncounter", Codec.BOOL, isBossEncounter());
        output.store("IsActive", Codec.BOOL, isActive());
        output.store("IsOneWay", Codec.BOOL, isOneWay());
        output.store("ColorTint", Codec.INT, this.entityData.get(DATA_COLOR_TINT));
        output.store("RedstoneMode", Codec.INT, getRedstoneMode());

        getOwnerUUID().ifPresent(uuid -> output.store("OwnerUUID", Codec.STRING, uuid.toString()));
        output.store("OwnerName", Codec.STRING, getOwnerName());

        if (bossEntityUUID != null) {
            output.store("BossUUID", Codec.STRING, bossEntityUUID.toString());
        }

        if (!whitelist.isEmpty()) {
            List<String> list = whitelist.stream().map(UUID::toString).toList();
            output.store("Whitelist", Codec.STRING.listOf(), list);
        }

        if (!whitelistUsernames.isEmpty()) {
            output.store("WhitelistUsernames", Codec.STRING.listOf(), new ArrayList<>(whitelistUsernames));
        }
    }
}
