package ddraig.net.entropica.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.mojang.serialization.Codec;

public class SporeDrifterEntity extends AmbientCreature {
    private int sporeTimer = 0;
    private int seedingTimer = 0;

    public SporeDrifterEntity(EntityType<? extends AmbientCreature> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            } else {
                this.moveRelative(this.getSpeed(), travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
            }
        }
        this.calculateEntityAnimation(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.FLYING_SPEED, 0.15D)
                .add(Attributes.MOVEMENT_SPEED, 0.12D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SporeDrifterFloatGoal(this));
        this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.sporeTimer = input.read("SporeTimer", Codec.INT).orElse(0);
        this.seedingTimer = input.read("SeedingTimer", Codec.INT).orElse(0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("SporeTimer", Codec.INT, this.sporeTimer);
        output.store("SeedingTimer", Codec.INT, this.seedingTimer);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            this.sporeTimer++;
            if (this.sporeTimer >= 600 + this.random.nextInt(600)) { // 30-60 seconds
                this.sporeTimer = 0;
                SporeCloudEntity cloud = new SporeCloudEntity(this.level(), this.getX(), this.getY() - 1.5D, this.getZ());
                this.level().addFreshEntity(cloud);
            }

            // Fungal Seeding
            this.seedingTimer++;
            if (this.seedingTimer >= 200) {
                this.seedingTimer = 0;
                seedMushroomsBelow();
            }

            // Keep floating 2-4 blocks above ground
            adjustFlightHeight();
        }

        // Bioluminescence glow particles on client
        if (this.level().isClientSide() && this.random.nextFloat() < 0.1f) {
            this.level().addParticle(ParticleTypes.GLOW, this.getRandomX(0.5D), this.getRandomY() - 0.5D, this.getRandomZ(0.5D), 0.0D, -0.02D, 0.0D);
        }
    }

    private void adjustFlightHeight() {
        if (this.getNavigation().isInProgress()) {
            return; // Let the navigation handle the movement smoothly
        }

        BlockPos below = this.blockPosition().below();
        while (below.getY() > this.level().getMinY() && this.level().isEmptyBlock(below)) {
            below = below.below();
        }
        int groundY = below.getY();
        double currentY = this.getY();
        double targetY = groundY + 3.0D; // float 3 blocks high

        Vec3 delta = this.getDeltaMovement();
        if (currentY < targetY - 1.0D) {
            this.setDeltaMovement(delta.x, delta.y * 0.5D + 0.03D, delta.z);
        } else if (currentY > targetY + 1.0D) {
            this.setDeltaMovement(delta.x, delta.y * 0.5D - 0.02D, delta.z);
        } else {
            // Very gentle floating wave sway
            double sway = Math.sin(this.tickCount * 0.05D) * 0.01D;
            this.setDeltaMovement(delta.x * 0.8D, sway, delta.z * 0.8D);
        }
    }

    private void seedMushroomsBelow() {
        BlockPos below = this.blockPosition().below();
        while (below.getY() > this.level().getMinY() && this.level().isEmptyBlock(below)) {
            below = below.below();
        }
        BlockPos groundPos = below.above();
        if (this.level().isEmptyBlock(groundPos)) {
            BlockState underState = this.level().getBlockState(below);
            if (underState.is(Blocks.MYCELIUM) || underState.is(Blocks.MOSS_BLOCK) || underState.is(Blocks.GRASS_BLOCK) || underState.is(Blocks.DIRT)) {
                BlockState mushroom = this.random.nextBoolean() ? Blocks.BROWN_MUSHROOM.defaultBlockState() : Blocks.RED_MUSHROOM.defaultBlockState();
                this.level().setBlockAndUpdate(groundPos, mushroom);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false; // Attacks pass through
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
        // Intangible: do not push
    }

    public static class SporeDrifterFloatGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final SporeDrifterEntity drifter;

        public SporeDrifterFloatGoal(SporeDrifterEntity drifter) {
            this.drifter = drifter;
            this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            PathNavigation navigation = drifter.getNavigation();
            if (navigation.isInProgress()) {
                return false;
            } else {
                return drifter.getRandom().nextInt(40) == 0;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return drifter.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            net.minecraft.util.RandomSource random = drifter.getRandom();
            double x = drifter.getX() + (random.nextFloat() * 2.0F - 1.0F) * 8.0F;
            double z = drifter.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 8.0F;

            BlockPos pos = BlockPos.containing(x, drifter.getY(), z);
            while (pos.getY() > drifter.level().getMinY() && drifter.level().isEmptyBlock(pos.below())) {
                pos = pos.below();
            }
            double y = pos.getY() + 3.0D + random.nextInt(3);

            drifter.getNavigation().moveTo(x, y, z, 1.0D);
        }
    }
}
