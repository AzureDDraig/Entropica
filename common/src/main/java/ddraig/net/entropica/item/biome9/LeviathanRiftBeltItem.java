package ddraig.net.entropica.item.biome9;

import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Leviathan Rift-Belt — Void-Sea Serpent scale sash that blinks the wearer up to 7 blocks
 * forward, cushions the landing, and halves projectile damage for 3 seconds.
 */
public class LeviathanRiftBeltItem extends Item {
    public static final String TAG_RIFT_DASH_UNTIL = "EntropicaRiftDashUntil";
    public static final int COOLDOWN_TICKS = 80;
    public static final double MAX_BLINK_DISTANCE = 7.0D;

    public LeviathanRiftBeltItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 startPos = player.position();
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            Vec3 rayEnd = eyePos.add(look.scale(MAX_BLINK_DISTANCE));

            BlockHitResult hit = serverLevel.clip(new ClipContext(
                    eyePos,
                    rayEnd,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            ));

            double hitDist = hit.getType() == HitResult.Type.MISS
                    ? MAX_BLINK_DISTANCE
                    : Math.max(0.0D, eyePos.distanceTo(hit.getLocation()) - 0.35D);

            Vec3 destination = startPos;
            double eyeOffset = player.getEyeHeight();
            for (double d = hitDist; d >= 0.5D; d -= 0.5D) {
                Vec3 candidateEye = eyePos.add(look.scale(d));
                Vec3 candidateFeet = new Vec3(candidateEye.x, candidateEye.y - eyeOffset, candidateEye.z);
                if (serverLevel.noCollision(player, player.getDimensions(player.getPose()).makeBoundingBox(candidateFeet))) {
                    destination = candidateFeet;
                    break;
                }
            }

            // Spawn rift trail along the dash path
            int steps = 14;
            DustParticleOptions magentaDust = new DustParticleOptions(0xD946EF, 1.0F);
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / (double) steps;
                double px = startPos.x + (destination.x - startPos.x) * t;
                double py = startPos.y + 0.9D + (destination.y - startPos.y) * t;
                double pz = startPos.z + (destination.z - startPos.z) * t;
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, px, py, pz, 2, 0.12D, 0.12D, 0.12D, 0.02D);
                serverLevel.sendParticles(magentaDust, px, py, pz, 1, 0.08D, 0.08D, 0.08D, 0.01D);
            }

            serverLevel.playSound(null, startPos.x, startPos.y, startPos.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9F, 1.15F);

            player.teleportTo(destination.x, destination.y, destination.z);
            Vec3 momentum = look.scale(0.45D);
            player.setDeltaMovement(momentum.x, Math.max(momentum.y * 0.6D, 0.08D), momentum.z);
            player.hasImpulse = true;
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.hurtMarked = true;
            }
            player.resetFallDistance();
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 50, 0, false, true, true));

            EntityHelper.getPersistentData(player).putLong(TAG_RIFT_DASH_UNTIL, serverLevel.getGameTime() + 60L);
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

            serverLevel.playSound(null, destination.x, destination.y, destination.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.25F);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Right-Click: Blink up to 7 blocks forward with Slow Falling (4s cooldown).")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.literal("Phase Veil: Halves incoming projectile damage for 3s after dashing.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
