package ddraig.net.entropica.entity.veil_fox;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class VeilFoxAfterimageEntity extends Mob {
    private static final EntityDataAccessor<Float> SYNC_Y_BODY_ROT = SynchedEntityData.defineId(VeilFoxAfterimageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SYNC_Y_HEAD_ROT = SynchedEntityData.defineId(VeilFoxAfterimageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SYNC_X_ROT = SynchedEntityData.defineId(VeilFoxAfterimageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SYNC_SITTING = SynchedEntityData.defineId(VeilFoxAfterimageEntity.class, EntityDataSerializers.BOOLEAN);

    public VeilFoxAfterimageEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SYNC_Y_BODY_ROT, 0.0f);
        builder.define(SYNC_Y_HEAD_ROT, 0.0f);
        builder.define(SYNC_X_ROT, 0.0f);
        builder.define(SYNC_SITTING, false);
    }

    public void setPoses(float yBodyRot, float yHeadRot, float xRot, boolean isSitting) {
        this.entityData.set(SYNC_Y_BODY_ROT, yBodyRot);
        this.entityData.set(SYNC_Y_HEAD_ROT, yHeadRot);
        this.entityData.set(SYNC_X_ROT, xRot);
        this.entityData.set(SYNC_SITTING, isSitting);
    }

    public float getYBodyRot() { return this.entityData.get(SYNC_Y_BODY_ROT); }
    public float getYHeadRot() { return this.entityData.get(SYNC_Y_HEAD_ROT); }
    public float getXRot() { return this.entityData.get(SYNC_X_ROT); }
    public boolean isSitting() { return this.entityData.get(SYNC_SITTING); }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > 20) {
            this.discard();
        }
    }
}