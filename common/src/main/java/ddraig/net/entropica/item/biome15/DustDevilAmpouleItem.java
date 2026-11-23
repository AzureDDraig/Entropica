package ddraig.net.entropica.item.biome15;

import ddraig.net.entropica.block.biome15.WindBaffleLanternBlock;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

/**
 * Dust-Devil Ampoule — compressed vortex of Grey Sand, Dry Fibers, and Dust-Core energy sealed in a Medium Ampoule.
 * Uncorking launches the user skyward on a swirling dust devil, grants Zephyr Weight (8s), blasts back nearby foes
 * with blinding grit, and returns an empty Medium Ampoule.
 */
public class DustDevilAmpouleItem extends Item {
    public static final int COOLDOWN_TICKS = 25;
    public static final double BLAST_RADIUS = 4.5D;

    public DustDevilAmpouleItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

        Vec3 v = player.getDeltaMovement();
        player.setDeltaMovement(v.x * 1.15D, Math.max(0.92D, v.y + 0.82D), v.z * 1.15D);
        player.hurtMarked = true;
        player.resetFallDistance();

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Holder<MobEffect> zephyrHolder = resolveZephyrWeightHolder();
            if (zephyrHolder != null) {
                player.addEffect(new MobEffectInstance(zephyrHolder, 160, 0));
            }

            // Swirling ascending dust-devil vortex particles
            for (int i = 0; i < 18; i++) {
                double progress = i / 18.0D;
                double angle = progress * Math.PI * 4.0D;
                double radius = 0.35D + progress * 1.35D;
                double px = player.getX() + Math.cos(angle) * radius;
                double py = player.getY() + 0.1D + progress * 2.1D;
                double pz = player.getZ() + Math.sin(angle) * radius;
                serverLevel.sendParticles(ParticleTypes.CLOUD, px, py, pz, 1, 0.08D, 0.06D, 0.08D, 0.03D);
                serverLevel.sendParticles(ParticleTypes.ASH, px, py, pz, 3, 0.14D, 0.1D, 0.14D, 0.025D);
            }

            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 1.05F);
            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.55F, 1.35F);
            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.85F, 1.15F);

            double maxDistSq = BLAST_RADIUS * BLAST_RADIUS;
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(BLAST_RADIUS),
                    other -> other != player
                            && other.isAlive()
                            && !other.isSpectator()
                            && other.distanceToSqr(player) <= maxDistSq
                            && !isAlliedOrPet(player, other)
            );

            for (LivingEntity target : targets) {
                double dx = target.getX() - player.getX();
                double dz = target.getZ() - player.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len < 1.0E-4D) {
                    dx = 1.0D;
                    dz = 0.0D;
                } else {
                    dx /= len;
                    dz /= len;
                }
                target.knockback(0.95D, -dx, -dz);
                Vec3 tv = target.getDeltaMovement();
                target.setDeltaMovement(tv.x, Math.max(0.32D, tv.y + 0.22D), tv.z);
                target.hurtMarked = true;

                if (!WindBaffleLanternBlock.isProtectedFromDust(serverLevel, target)) {
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                }
            }

            if (!player.getAbilities().instabuild) {
                ItemStack emptyAmpoule = ModItems.MEDIUM_AMPOULE.isBound()
                        ? new ItemStack(ModItems.MEDIUM_AMPOULE.get())
                        : ItemStack.EMPTY;
                stack.shrink(1);
                if (!emptyAmpoule.isEmpty()) {
                    if (stack.isEmpty()) {
                        player.setItemInHand(hand, emptyAmpoule);
                    } else if (!player.getInventory().add(emptyAmpoule)) {
                        player.drop(emptyAmpoule, false);
                    }
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    private static boolean isAlliedOrPet(Player player, LivingEntity other) {
        if (player.isAlliedTo(other)) {
            return true;
        }
        if (other instanceof TamableAnimal tamable && tamable.isTame()) {
            return true;
        }
        if (other instanceof OwnableEntity ownable) {
            LivingEntity owner = ownable.getRootOwner();
            if (owner == player || (owner != null && player.isAlliedTo(owner))) {
                return true;
            }
        }
        return false;
    }

    private static Holder<MobEffect> resolveZephyrWeightHolder() {
        if (ModEffects.ZEPHYR_WEIGHT.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ZEPHYR_WEIGHT.get());
        }
        MobEffect fallback = BuiltInRegistries.MOB_EFFECT
                .getOptional(ResourceLocation.fromNamespaceAndPath("entropica", "zephyr_weight"))
                .orElse(null);
        return fallback != null ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(fallback) : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Right-Click: Uncork an updraft vortex and gain Zephyr Weight (8s).")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Grit Blast: Knocks back and blinds nearby foes within 4.5 blocks; returns Medium Ampoule.")
                .withStyle(ChatFormatting.AQUA));
    }
}
