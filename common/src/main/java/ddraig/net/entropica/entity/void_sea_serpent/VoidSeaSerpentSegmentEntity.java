package ddraig.net.entropica.entity.void_sea_serpent;

import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/**
 * Physical hitbox segment entity for the Void Sea Serpent.
 * Placed at key joints along the 16-segment body spine and routes damage directly to the parent.
 */
public class VoidSeaSerpentSegmentEntity extends Entity {

    private UUID parentUUID;
    private VoidSeaSerpentEntity parent;
    private int segmentIndex = 0;
    private final EntityDimensions customDimensions;

    public VoidSeaSerpentSegmentEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.customDimensions = type.getDimensions();
    }

    public VoidSeaSerpentSegmentEntity(VoidSeaSerpentEntity parent, float width, float height, int segmentIndex) {
        super(ModEntityTypes.VOID_SEA_SERPENT_SEGMENT.get(), parent.level());
        this.parent = parent;
        this.parentUUID = parent.getUUID();
        this.segmentIndex = segmentIndex;
        this.noPhysics = true;
        this.customDimensions = EntityDimensions.scalable(width, height);
        this.setPos(parent.getX(), parent.getY(), parent.getZ());
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.customDimensions != null ? this.customDimensions : super.getDimensions(pose);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synced entity data needed for invisible proxy hitboxes
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerable()) {
            return false;
        }

        VoidSeaSerpentEntity p = this.getParent();
        if (p != null && p.isAlive()) {
            // Prevent serpent from harming its own body segments
            if (source.getEntity() == p || source.getDirectEntity() == p) {
                return false;
            }
            return p.hurtServer(level, source, amount);
        }
        return false;
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    public VoidSeaSerpentEntity getParent() {
        if (this.parent == null && this.parentUUID != null && this.level() instanceof ServerLevel sl) {
            Entity found = sl.getEntity(this.parentUUID);
            if (found instanceof VoidSeaSerpentEntity serpent) {
                this.parent = serpent;
            }
        }
        return this.parent;
    }

    public void setParent(VoidSeaSerpentEntity parent) {
        this.parent = parent;
        if (parent != null) {
            this.parentUUID = parent.getUUID();
        }
    }

    public int getSegmentIndex() {
        return this.segmentIndex;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            VoidSeaSerpentEntity p = this.getParent();
            if (p == null || !p.isAlive() || p.isRemoved()) {
                this.discard();
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.read("ParentUUID", net.minecraft.core.UUIDUtil.CODEC).ifPresent(uuid -> this.parentUUID = uuid);
        input.read("SegmentIndex", com.mojang.serialization.Codec.INT).ifPresent(idx -> this.segmentIndex = idx);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.parentUUID != null) {
            output.store("ParentUUID", net.minecraft.core.UUIDUtil.CODEC, this.parentUUID);
        }
        output.store("SegmentIndex", com.mojang.serialization.Codec.INT, this.segmentIndex);
    }
}
