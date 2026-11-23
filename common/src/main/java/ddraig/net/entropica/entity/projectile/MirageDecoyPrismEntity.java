package ddraig.net.entropica.entity.projectile;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataAfterimageEntity;
import ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataSpriteEntity;
import ddraig.net.entropica.entity.silica_glass_scarab.SilicaGlassScarabEntity;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * Mirage Decoy Prism — throwable optical refraction prism from 2.13 Mirage Glacier.
 * Shatters on impact into a dazzling solar-mirage flare, spawning a temporary MirageFataAfterimageEntity
 * decoy at +0.5Y, redirecting all nearby hostile Monsters within 12 blocks toward the decoy spot, and
 * inflicting Blindness (80t), Slowness II (100t), and Double Vision (160t) on foes within 4.0 blocks.
 */
public class MirageDecoyPrismEntity extends ThrowableItemProjectile {

    private static final ResourceLocation MIRAGE_DECOY_PRISM_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_decoy_prism");
    private static final ResourceLocation DOUBLE_VISION_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "double_vision");

    private static final DustParticleOptions MIRAGE_CYAN_DUST = new DustParticleOptions(0x38BDF8, 0.65F);
    private static final DustParticleOptions SUN_AMBER_DUST = new DustParticleOptions(0xFB923C, 0.65F);

    public MirageDecoyPrismEntity(EntityType<? extends MirageDecoyPrismEntity> type, Level level) {
        super(type, level);
    }

    public MirageDecoyPrismEntity(Level level, LivingEntity shooter) {
        this(level, shooter, new ItemStack(resolveDefaultPrismItem()));
    }

    public MirageDecoyPrismEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(resolveEntityType(), shooter, level, stack);
    }

    public MirageDecoyPrismEntity(Level level, double x, double y, double z) {
        this(level, x, y, z, new ItemStack(resolveDefaultPrismItem()));
    }

    public MirageDecoyPrismEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(resolveEntityType(), x, y, z, level, stack);
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends MirageDecoyPrismEntity> resolveEntityType() {
        if (ModEntityTypes.MIRAGE_DECOY_PRISM.isPresent()) {
            return ModEntityTypes.MIRAGE_DECOY_PRISM.get();
        }
        return (EntityType<? extends MirageDecoyPrismEntity>) BuiltInRegistries.ENTITY_TYPE
                .getValue(MIRAGE_DECOY_PRISM_ID);
    }

    private static Item resolveDefaultPrismItem() {
        return BuiltInRegistries.ITEM.getOptional(MIRAGE_DECOY_PRISM_ID).orElse(Items.AMETHYST_SHARD);
    }

    @Override
    protected Item getDefaultItem() {
        return resolveDefaultPrismItem();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    0.0D,
                    0.005D,
                    0.0D
            );
            DustParticleOptions dust = (this.tickCount % 2 == 0) ? MIRAGE_CYAN_DUST : SUN_AMBER_DUST;
            this.level().addParticle(
                    dust,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.16D,
                    this.getY() + (this.random.nextDouble() - 0.5D) * 0.16D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.16D,
                    0.0D,
                    0.01D,
                    0.0D
            );
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 hitPos = result.getLocation();
            double x = hitPos.x;
            double y = hitPos.y;
            double z = hitPos.z;

            serverLevel.playSound(null, x, y, z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL, 1.0F, 1.25F);
            serverLevel.playSound(null, x, y, z, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.NEUTRAL, 1.0F, 1.35F);

            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 0.35D, z, 28, 0.95D, 0.65D, 0.95D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y + 0.35D, z, 28, 0.85D, 0.55D, 0.85D, 0.06D);

            Entity owner = this.getOwner();

            // 1. Spawn temporary MirageFataAfterimageEntity decoy at impact +0.5 Y
            MirageFataAfterimageEntity decoy = null;
            if (ModEntityTypes.MIRAGE_FATA_AFTERIMAGE.isPresent()) {
                decoy = ModEntityTypes.MIRAGE_FATA_AFTERIMAGE.get().create(serverLevel, EntitySpawnReason.TRIGGERED);
                if (decoy != null) {
                    decoy.setPos(x, y + 0.5D, z);
                    decoy.setYRot(this.random.nextFloat() * 360.0F);
                    decoy.activateStandaloneDecoy(160);
                    if (!serverLevel.addFreshEntity(decoy)) {
                        decoy = null;
                    }
                }
            }

            // 2. Redirect all nearby hostile Monsters / aggressive Mobs within 12 blocks toward the decoy spot
            AABB lureBounds = new AABB(x - 12.0D, y - 12.0D, z - 12.0D, x + 12.0D, y + 12.0D, z + 12.0D);
            List<Mob> nearbyMobs = serverLevel.getEntitiesOfClass(
                    Mob.class,
                    lureBounds,
                    m -> m.isAlive() && !(m instanceof MirageFataSpriteEntity)
            );
            for (Mob mob : nearbyMobs) {
                if (mob instanceof Monster || mob instanceof Enemy || (owner != null && mob.getTarget() == owner)) {
                    if (decoy != null) {
                        mob.setTarget(decoy);
                    } else {
                        mob.setTarget(null);
                    }
                    mob.getNavigation().moveTo(x, y + 0.5D, z, 1.15D);
                }
            }

            // 3. Inflict Blindness (80t), Slowness II (100t, amp 1), and Double Vision (160t) on foes within 4.0 blocks
            Optional<MobEffect> doubleVisionOpt = BuiltInRegistries.MOB_EFFECT.getOptional(DOUBLE_VISION_ID);
            Holder<MobEffect> doubleVisionHolder = doubleVisionOpt.map(BuiltInRegistries.MOB_EFFECT::wrapAsHolder).orElse(null);

            AABB dazzleBounds = new AABB(x - 4.0D, y - 4.0D, z - 4.0D, x + 4.0D, y + 4.0D, z + 4.0D);
            List<LivingEntity> dazzleTargets = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    dazzleBounds,
                    LivingEntity::isAlive
            );
            for (LivingEntity target : dazzleTargets) {
                if ((owner != null && target == owner)
                        || target == decoy
                        || target instanceof MirageFataSpriteEntity) {
                    continue;
                }
                if (target.distanceToSqr(x, y, z) > 16.0D) {
                    continue;
                }
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0), owner);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1), owner);
                if (doubleVisionHolder != null && !SilicaGlassScarabEntity.isWearingMirageVeilGoggles(target)) {
                    target.addEffect(new MobEffectInstance(doubleVisionHolder, 160, 0), owner);
                }
            }

            this.discard();
        }
    }
}
