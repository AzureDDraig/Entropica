package ddraig.net.entropica.item.biome8;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

/**
 * Rime-Cap Spores — harvested from Rime-Cap Mushrooms in 2.8 Barrow-Mound Tundra.
 * Right-click throws a chilling spore cloud ahead, or brew into Barrow Ward potions.
 */
public class RimeCapSporesItem extends Item {

    public RimeCapSporesItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(stack, 15);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition(1.0F);
            Vec3 lookVec = player.getViewVector(1.0F);
            Vec3 maxReach = eyePos.add(lookVec.scale(8.0D));

            BlockHitResult blockHit = serverLevel.clip(new ClipContext(
                    eyePos, maxReach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
            ));
            double maxEntityDistSq = blockHit.getType() != HitResult.Type.MISS
                    ? eyePos.distanceToSqr(blockHit.getLocation())
                    : 64.0D;

            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    player,
                    eyePos,
                    maxReach,
                    player.getBoundingBox().expandTowards(lookVec.scale(8.0D)).inflate(1.0D),
                    e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e != player,
                    maxEntityDistSq
            );

            Vec3 burstPos;
            if (entityHit != null) {
                burstPos = entityHit.getLocation();
            } else if (blockHit.getType() != HitResult.Type.MISS) {
                burstPos = blockHit.getLocation().subtract(lookVec.scale(0.25D));
            } else {
                burstPos = eyePos.add(lookVec.scale(4.5D));
            }

            // Short visual frost trail from thrower toward burst
            for (int step = 1; step <= 4; step++) {
                double t = step / 5.0D;
                Vec3 pt = eyePos.lerp(burstPos, t);
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, pt.x, pt.y, pt.z, 2, 0.05D, 0.05D, 0.05D, 0.01D);
            }

            serverLevel.playSound(null, burstPos.x, burstPos.y, burstPos.z,
                    SoundEvents.SNOW_BREAK, SoundSource.PLAYERS, 0.85F, 1.15F);
            serverLevel.playSound(null, burstPos.x, burstPos.y, burstPos.z,
                    SoundEvents.PUFFER_FISH_BLOW_OUT, SoundSource.PLAYERS, 0.8F, 1.2F);

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    burstPos.x, burstPos.y, burstPos.z,
                    24, 0.65D, 0.45D, 0.65D, 0.06D);
            serverLevel.sendParticles(new DustParticleOptions(0x38BDF8, 1.0F),
                    burstPos.x, burstPos.y, burstPos.z,
                    16, 0.65D, 0.45D, 0.65D, 0.05D);

            double radius = 3.5D;
            double maxDistSq = radius * radius;
            AABB box = new AABB(
                    burstPos.x - radius, burstPos.y - radius, burstPos.z - radius,
                    burstPos.x + radius, burstPos.y + radius, burstPos.z + radius
            );
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player
                            && e.isAlive()
                            && !e.isSpectator()
                            && !(e instanceof Player p && p.isCreative())
                            && !(e instanceof TamableAnimal tamable && tamable.isOwnedBy(player))
                            && e.position().distanceToSqr(burstPos) <= maxDistSq);

            for (LivingEntity target : targets) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 1), player);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0), player);
                int maxFreeze = target.getTicksRequiredToFreeze() + 100;
                target.setTicksFrozen(Math.min(maxFreeze, target.getTicksFrozen() + 60));
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Right-Click: Throws a freezing spore cloud (Slowness II, Weakness I, and frost buildup).")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.literal("Brewing: Brew into Awkward Potions to craft Potions of Barrow Ward.")
                .withStyle(ChatFormatting.GRAY));
    }
}
