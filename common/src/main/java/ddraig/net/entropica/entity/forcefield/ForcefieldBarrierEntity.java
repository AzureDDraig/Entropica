package ddraig.net.entropica.entity.forcefield;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.forcefield.*;
import ddraig.net.entropica.item.SoapFilmWeaverItem;
import ddraig.net.entropica.network.BarrierImpactPayload;
import ddraig.net.entropica.registry.ModSounds;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Set<UUID> whitelist = new CopyOnWriteArraySet<>();
    private UUID bossEntityUUID = null;

    // Client-side ripple rendering queue: (impactVec, triggerTick, intensity)
    public record RippleImpact(Vec3 pos, long tick, float intensity) {}
    private final List<RippleImpact> activeRipples = Collections.synchronizedList(new ArrayList<>());

    public ForcefieldBarrierEntity(EntityType<? extends ForcefieldBarrierEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SHAPE, BarrierShape.PLANAR_QUAD.ordinal());
        builder.define(DATA_WIDTH, 4.0F);
        builder.define(DATA_HEIGHT, 4.0F);
        builder.define(DATA_RADIUS, 5.0F);
        builder.define(DATA_FILTER_MODE, BarrierFilterMode.ALL_ENTITIES.ordinal());
        builder.define(DATA_PREDATOR_THEME, ApexPredatorTheme.STANDARD.ordinal());
        builder.define(DATA_BOUNCE_ELASTICITY, 1.0F);
        builder.define(DATA_IS_BOSS_ENCOUNTER, false);
        builder.define(DATA_OWNER_UUID, "");
        builder.define(DATA_OWNER_NAME, "");
    }

    @Override
    public void tick() {
        super.tick();

        // Register in spatial level manager
        BarrierFieldManager.registerBarrier(this);

        // Periodically refresh bounding box to encompass active shape & size
        if (this.tickCount % 20 == 0) {
            updateBoundingBox();
        }

        // Clean up stale ripples on client
        if (this.level().isClientSide() && !activeRipples.isEmpty()) {
            long current = this.level().getGameTime();
            activeRipples.removeIf(r -> (current - r.tick()) > 40);
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

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        BarrierFieldManager.unregisterBarrier(this);
    }

    public void updateBoundingBox() {
        double maxDim = Math.max(Math.max(getWidth(), getHeight()), getRadius()) + 2.0;
        Vec3 pos = this.position();
        this.setBoundingBox(new AABB(
                pos.x - maxDim, pos.y - maxDim, pos.z - maxDim,
                pos.x + maxDim, pos.y + maxDim, pos.z + maxDim
        ));
    }

    /**
     * Continuous collision resolution when an entity moves from startPos to endPos.
     * Checks swept-ray manifold intersection, verifies permissions, and applies side-relative bounce.
     */
    public boolean handleEntityCollision(Entity entity, Vec3 startPos, Vec3 endPos) {
        if (!isAlive()) return false;

        UUID owner = getOwnerUUID().orElse(null);
        if (!getFilterMode().isBlocked(entity, owner, whitelist)) {
            return false; // Permitted, passes freely
        }

        double entityRadius = Math.max(0.15, entity.getBbWidth() * 0.5);
        BarrierRaycastHit hit = BarrierGeometry.intersect(
                getShape(),
                this.position(),
                this.getYRot(),
                this.getXRot(),
                getWidth(),
                getHeight(),
                getRadius(),
                startPos,
                endPos,
                entityRadius
        );

        if (!hit.hit()) {
            return false;
        }

        // --- Calculate Side-Relative Normal & Elastic Bounce ---
        Vec3 approachDir = endPos.subtract(startPos);
        if (approachDir.lengthSqr() < 1e-6) {
            approachDir = entity.getDeltaMovement();
        }

        Vec3 nEff = hit.getEffectiveNormal(approachDir);
        double elasticity = (double) getBounceElasticity();

        Vec3 v = entity.getDeltaMovement();
        double dot = v.dot(nEff);

        // Reflection formula: v' = v - (1 + e) * (v . n) * n
        Vec3 reflected = v.subtract(nEff.scale((1.0 + elasticity) * dot));

        // Ensure minimum bounce velocity along nEff so slow-walking entities rebound cleanly
        double minImpulse = 0.35 * Math.max(1.0, elasticity);
        if (reflected.dot(nEff) < minImpulse) {
            reflected = reflected.add(nEff.scale(minImpulse - Math.max(0.0, reflected.dot(nEff))));
        }

        // Reposition entity safely outside the membrane on the side they approached from
        Vec3 safePos = hit.impactPoint().add(nEff.scale(entityRadius + 0.08));
        entity.setPos(safePos.x, safePos.y, safePos.z);
        entity.setDeltaMovement(reflected);
        entity.resetFallDistance();
        entity.hasImpulse = true;

        // If projectile, update flight rotation
        if (entity instanceof Projectile projectile) {
            projectile.shoot(reflected.x, reflected.y, reflected.z, (float) reflected.length(), 0.0F);
        }

        // Trigger audio chime and visual ripple effects
        onBounceImpact(hit.impactPoint(), nEff);

        return true;
    }

    public void onBounceImpact(Vec3 impactPos, Vec3 normal) {
        // Play soap-film rebound sound
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
                    12,
                    normal.x * 0.1, normal.y * 0.1, normal.z * 0.1,
                    0.08
            );

            // Sync ripple packet to nearby tracking clients
            BarrierImpactPayload payload = new BarrierImpactPayload(
                    this.getId(),
                    (float) impactPos.x,
                    (float) impactPos.y,
                    (float) impactPos.z,
                    1.0F
            );
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(impactPos) <= 64.0 * 64.0) {
                    NetworkManager.sendToPlayer(player, payload);
                }
            }
        } else {
            addRipple(impactPos, 1.0F);
        }
    }

    public void addRipple(Vec3 pos, float intensity) {
        activeRipples.add(new RippleImpact(pos, this.level().getGameTime(), intensity));
    }

    public List<RippleImpact> getActiveRipples() {
        return activeRipples;
    }

    public void dissolveInGlitter() {
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();
            serverLevel.sendParticles(ParticleTypes.FIREWORK, pos.x, pos.y, pos.z, 50, 2.0, 2.0, 2.0, 0.15);
            serverLevel.sendParticles(ParticleTypes.GLOW, pos.x, pos.y, pos.z, 30, 2.0, 2.0, 2.0, 0.1);
        }
        this.discard();
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
        if (held.getItem() instanceof SoapFilmWeaverItem) {
            if (isCreator || isCreative) {
                if (player.isShiftKeyDown()) {
                    BarrierFilterMode next = BarrierFilterMode.fromOrdinal((getFilterMode().ordinal() + 1) % BarrierFilterMode.values().length);
                    setFilterMode(next);
                    player.displayClientMessage(Component.literal("§d[Soap-Film Weaver] §fFilter updated: §e" + next.getDisplayName()), true);
                } else {
                    dissolveInGlitter();
                    player.displayClientMessage(Component.literal("§7[Soap-Film Weaver] §fDispelled barrier."), true);
                    this.level().playSound(null, getX(), getY(), getZ(), net.minecraft.sounds.SoundEvents.BUBBLE_POP, SoundSource.PLAYERS, 1.0F, 1.2F);
                }
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal("§cOnly the creator can modify or dispel this barrier!"), true);
                return InteractionResult.CONSUME;
            }
        }

        if (isCreator || isCreative) {
            if (player.isShiftKeyDown()) {
                // Creator sneak-interact: open configuration info
                player.displayClientMessage(Component.literal("§d[Barrier] §fShape: §b" + getShape().getDisplayName() + " §8| §fFilter: §e" + getFilterMode().getDisplayName()), true);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) {
            if (player.getMainHandItem().getItem() instanceof SoapFilmWeaverItem) {
                UUID owner = getOwnerUUID().orElse(null);
                boolean isCreator = owner != null && owner.equals(player.getUUID());
                if (isCreator || player.isCreative()) {
                    dissolveInGlitter();
                    player.displayClientMessage(Component.literal("§7[Soap-Film Weaver] §fDispelled barrier."), true);
                    level.playSound(null, getX(), getY(), getZ(), net.minecraft.sounds.SoundEvents.BUBBLE_POP, SoundSource.PLAYERS, 1.0F, 1.2F);
                    return true;
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
        this.entityData.set(DATA_WIDTH, Math.max(0.5F, width));
        updateBoundingBox();
    }

    public float getHeight() {
        return this.entityData.get(DATA_HEIGHT);
    }

    public void setHeight(float height) {
        this.entityData.set(DATA_HEIGHT, Math.max(0.5F, height));
        updateBoundingBox();
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void setRadius(float radius) {
        this.entityData.set(DATA_RADIUS, Math.max(0.5F, radius));
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
        this.entityData.set(DATA_BOUNCE_ELASTICITY, Math.max(0.1F, Math.min(3.0F, elasticity)));
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

        getOwnerUUID().ifPresent(uuid -> output.store("OwnerUUID", Codec.STRING, uuid.toString()));
        output.store("OwnerName", Codec.STRING, getOwnerName());

        if (bossEntityUUID != null) {
            output.store("BossUUID", Codec.STRING, bossEntityUUID.toString());
        }

        if (!whitelist.isEmpty()) {
            List<String> list = whitelist.stream().map(UUID::toString).toList();
            output.store("Whitelist", Codec.STRING.listOf(), list);
        }
    }
}
