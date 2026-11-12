package ddraig.net.entropica.entity.luminoth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class LuminothEntity extends PathfinderMob implements FlyingAnimal {

    public final AnimationState flyAnimationState = new AnimationState();

    public LuminothEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LuminothFlyGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.flyAnimationState.startIfStopped(this.tickCount);

            // Bioluminescent sparkling particles
            if (this.random.nextFloat() < 0.3F) {
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        this.getY() + this.random.nextDouble() * 0.5D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        0.0D, -0.02D, 0.0D);
            }
        } else {
            // Provide light blessing to nearby players
            if (this.tickCount % 60 == 0) {
                List<Player> nearby = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(3.5D));
                for (Player player : nearby) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 140, 0, true, false, true));
                }
            }
        }
    }

    static class LuminothFlyGoal extends Goal {
        private final LuminothEntity luminoth;

        public LuminothFlyGoal(LuminothEntity luminoth) {
            this.luminoth = luminoth;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.luminoth.getNavigation().isInProgress() && this.luminoth.getRandom().nextInt(15) == 0;
        }

        @Override
        public void start() {
            Vec3 pos = this.luminoth.position();
            double targetX = pos.x + (this.luminoth.getRandom().nextDouble() - 0.5D) * 12.0D;
            double targetY = Math.min(pos.y + (this.luminoth.getRandom().nextDouble() - 0.3D) * 6.0D, this.luminoth.level().getMaxY() - 10);
            double targetZ = pos.z + (this.luminoth.getRandom().nextDouble() - 0.5D) * 12.0D;

            // Bias towards light sources if found
            BlockPos current = this.luminoth.blockPosition();
            for (int dx = -4; dx <= 4; dx += 2) {
                for (int dy = -2; dy <= 2; dy += 2) {
                    for (int dz = -4; dz <= 4; dz += 2) {
                        BlockPos check = current.offset(dx, dy, dz);
                        if (this.luminoth.level().getMaxLocalRawBrightness(check) > 10) {
                            targetX = check.getX() + 0.5D;
                            targetY = check.getY() + 1.2D;
                            targetZ = check.getZ() + 0.5D;
                            break;
                        }
                    }
                }
            }

            this.luminoth.getNavigation().moveTo(targetX, targetY, targetZ, 1.0D);
        }
    }
}
