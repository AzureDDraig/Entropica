package ddraig.net.entropica.entity;

import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.mojang.serialization.Codec;

import java.util.List;

public class SporeCloudEntity extends Entity {
    private int age = 0;
    private static final int MAX_AGE = 200; // 10 seconds

    public SporeCloudEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SporeCloudEntity(Level level, double x, double y, double z) {
        this(ModEntityTypes.SPORE_CLOUD.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synched data needed
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.age = input.read("Age", Codec.INT).orElse(0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("Age", Codec.INT, this.age);
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;
        if (this.age >= MAX_AGE) {
            this.discard();
            return;
        }

        // Spawn glowing spores particles on client/server
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.random.nextFloat() < 0.3f) {
                serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(), this.getZ(), 3, 0.5, 0.5, 0.5, 0.05);
            }
            if (this.random.nextFloat() < 0.15f) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(), this.getZ(), 2, 0.3, 0.3, 0.3, 0.02);
            }
        }

        // Apply haze effect to players inside cloud
        if (!this.level().isClientSide() && this.age % 10 == 0) {
            AABB bounds = this.getBoundingBox().inflate(0.5D);
            List<Player> players = this.level().getEntitiesOfClass(Player.class, bounds);
            for (Player player : players) {
                if (!player.isSpectator()) {
                    player.addEffect(new MobEffectInstance(
                        net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(
                            ddraig.net.entropica.registry.ModEffects.HAZE.get()
                        ), 300, 0)); // 15 seconds
                }
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.GLASS_BOTTLE)) {
            if (!this.level().isClientSide()) {
                held.shrink(1);
                ItemStack spores = new ItemStack(ModItems.LIVING_SPORES.get());
                if (!player.getInventory().add(spores)) {
                    player.drop(spores, false);
                }
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.PLAYERS, 1.0F, 1.0F);
                this.discard();
            }
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public boolean isPickable() {
        return true;
    }
}
