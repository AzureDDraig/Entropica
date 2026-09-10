package ddraig.net.entropica.entity.amber_weeping_stag;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class AmberWeepingStagEntity extends Animal {

    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(AmberWeepingStagEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    public AmberWeepingStagEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 45.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, AmberStagVariant.AMBER.getId());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.15D, stack -> stack.is(Items.GLOW_BERRIES) || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.AMETHYST_SHARD), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.GLOW_BERRIES) || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.AMETHYST_SHARD);
    }

    public AmberStagVariant getVariant() {
        return AmberStagVariant.byId(this.entityData.get(VARIANT));
    }

    public void setVariant(AmberStagVariant variant) {
        this.entityData.set(VARIANT, variant.getId());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Variant", Codec.INT, this.getVariant().getId());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(AmberStagVariant.byId(input.read("Variant", Codec.INT).orElse(0)));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        // Variant determination by temperature / biome
        float temp = level.getBiome(this.blockPosition()).value().getBaseTemperature();
        if (temp < 0.2F) {
            this.setVariant(AmberStagVariant.FROST);
        } else if (temp > 1.0F) {
            this.setVariant(AmberStagVariant.SOLAR);
        } else {
            this.setVariant(AmberStagVariant.AMBER);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean canFreeze() {
        return this.getVariant() != AmberStagVariant.FROST && super.canFreeze();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }

            // Weeping tear particles based on variant
            if (this.random.nextFloat() < 0.25F) {
                AmberStagVariant var = this.getVariant();
                double ox = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
                double oy = this.getY() + 1.2D + (this.random.nextDouble() - 0.5D) * 0.4D;
                double oz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;

                if (var == AmberStagVariant.FROST) {
                    this.level().addParticle(ParticleTypes.SNOWFLAKE, ox, oy, oz, 0, -0.04D, 0);
                } else if (var == AmberStagVariant.SOLAR) {
                    this.level().addParticle(ParticleTypes.WAX_OFF, ox, oy, oz, 0, -0.02D, 0);
                } else {
                    this.level().addParticle(ParticleTypes.DRIPPING_HONEY, ox, oy, oz, 0, -0.03D, 0);
                }
            }
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        AmberWeepingStagEntity child = new AmberWeepingStagEntity((EntityType<? extends Animal>) this.getType(), level);
        if (otherParent instanceof AmberWeepingStagEntity otherStag) {
            child.setVariant(this.random.nextBoolean() ? this.getVariant() : otherStag.getVariant());
        }
        return child;
    }
}
