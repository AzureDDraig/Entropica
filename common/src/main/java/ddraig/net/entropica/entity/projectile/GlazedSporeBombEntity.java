package ddraig.net.entropica.entity.projectile;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Glazed Spore-Bomb — rime-sealed fungal projectile from 2.12 Spore-Glazed Pack.
 * Trails snowflakes and spore motes in flight, shattering on impact to flash-freeze surface water,
 * chill targets, and burst a numbing cloud of Slowness III, Weakness, and Poison spores.
 */
public class GlazedSporeBombEntity extends ThrowableItemProjectile {

    private static final ResourceLocation GLAZED_SPORE_BOMB_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glazed_spore_bomb");

    public GlazedSporeBombEntity(EntityType<? extends GlazedSporeBombEntity> type, Level level) {
        super(type, level);
    }

    public GlazedSporeBombEntity(Level level, LivingEntity shooter) {
        this(level, shooter, new ItemStack(resolveDefaultBombItem()));
    }

    public GlazedSporeBombEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(resolveEntityType(), shooter, level, stack);
    }

    public GlazedSporeBombEntity(Level level, double x, double y, double z) {
        this(level, x, y, z, new ItemStack(resolveDefaultBombItem()));
    }

    public GlazedSporeBombEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(resolveEntityType(), x, y, z, level, stack);
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends GlazedSporeBombEntity> resolveEntityType() {
        if (ModEntityTypes.GLAZED_SPORE_BOMB.isPresent()) {
            return ModEntityTypes.GLAZED_SPORE_BOMB.get();
        }
        return (EntityType<? extends GlazedSporeBombEntity>) BuiltInRegistries.ENTITY_TYPE
                .getValue(GLAZED_SPORE_BOMB_ID);
    }

    private static Item resolveDefaultBombItem() {
        return BuiltInRegistries.ITEM.getOptional(GLAZED_SPORE_BOMB_ID).orElse(Items.SNOWBALL);
    }

    @Override
    protected Item getDefaultItem() {
        return resolveDefaultBombItem();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    0.0D,
                    -0.01D,
                    0.0D
            );
            this.level().addParticle(
                    ParticleTypes.SPORE_BLOSSOM_AIR,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.14D,
                    this.getY() + (this.random.nextDouble() - 0.5D) * 0.14D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.14D,
                    0.0D,
                    -0.015D,
                    0.0D
            );
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            serverLevel.playSound(null, x, y, z, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, 1.15F);
            serverLevel.playSound(null, x, y, z, SoundEvents.SPORE_BLOSSOM_FALL, SoundSource.NEUTRAL, 1.0F, 0.9F);

            serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL, x, y, z, 28, 0.9D, 0.6D, 0.9D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, x, y, z, 28, 1.35D, 0.85D, 1.35D, 0.05D);

            BlockPos center = BlockPos.containing(result.getLocation());
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (dx * dx + dy * dy + dz * dz > 4) {
                            continue;
                        }
                        BlockPos targetPos = center.offset(dx, dy, dz);
                        BlockState targetState = serverLevel.getBlockState(targetPos);
                        if (targetState.is(Blocks.WATER)
                                && targetState.getFluidState().isSource()
                                && serverLevel.getBlockState(targetPos.above()).isAir()) {
                            serverLevel.setBlockAndUpdate(targetPos, Blocks.ICE.defaultBlockState());
                        } else if (targetState.getBlock() instanceof BaseFireBlock) {
                            serverLevel.removeBlock(targetPos, false);
                        }
                    }
                }
            }

            Entity owner = this.getOwner();
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(3.5D),
                    LivingEntity::isAlive
            );
            for (LivingEntity target : targets) {
                if (owner != null && target == owner) {
                    continue;
                }
                if (target.isOnFire()) {
                    target.clearFire();
                }
                target.hurtServer(serverLevel, serverLevel.damageSources().thrown(this, owner), 3.0F);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2), owner);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0), owner);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), owner);
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 40, target.getTicksFrozen() + 90));
            }

            this.discard();
        }
    }
}
