package ddraig.net.entropica.item.biome13;

import ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataAfterimageEntity;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

/**
 * Shimmering Focus — handheld Mirage Materia refraction focus crafted with Silica Glass Plate,
 * Shimmering Mirage Aura, Vitreous Needles, gold, and Arcanite.
 * Cloaks the caster in Invisibility, Speed II, and Mirage Phasing while blinding nearby foes
 * and leaving behind a shimmering illusory afterimage.
 */
public class ShimmeringFocusItem extends Item {

    public ShimmeringFocusItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.85F, 1.35F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.90F, 1.15F);

        player.getCooldowns().addCooldown(stack, 360);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, false, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 1, false, true, true));
            if (ModEffects.MIRAGE_PHASING.isBound()) {
                player.addEffect(new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.MIRAGE_PHASING.get()),
                        200, 0, false, true, true
                ));
            }

            List<Mob> nearbyMobs = serverLevel.getEntitiesOfClass(
                    Mob.class,
                    player.getBoundingBox().inflate(12.0D),
                    mob -> mob.isAlive() && !(mob instanceof MirageFataAfterimageEntity)
            );
            for (Mob mob : nearbyMobs) {
                mob.setTarget(null);
            }

            List<Monster> nearbyMonsters = serverLevel.getEntitiesOfClass(
                    Monster.class,
                    player.getBoundingBox().inflate(8.0D),
                    monster -> monster.isAlive()
            );
            for (Monster monster : nearbyMonsters) {
                monster.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, true, true));
                if (ModEffects.DOUBLE_VISION.isBound()) {
                    monster.addEffect(new MobEffectInstance(
                            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.DOUBLE_VISION.get()),
                            120, 0, false, true, true
                    ));
                }
            }

            boolean spawnedAfterimage = false;
            if (ModEntityTypes.MIRAGE_FATA_AFTERIMAGE.isPresent()) {
                MirageFataAfterimageEntity afterimage = new MirageFataAfterimageEntity(
                        ModEntityTypes.MIRAGE_FATA_AFTERIMAGE.get(), serverLevel) {
                    private int decoyLifetime = 200;

                    @Override
                    public boolean shouldBeSaved() {
                        return false;
                    }

                    @Override
                    public boolean hurtServer(ServerLevel sl, DamageSource source, float amount) {
                        if (!this.isAlive()) {
                            return false;
                        }
                        sl.playSound(null, this.getX(), this.getY() + 0.85D, this.getZ(),
                                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL, 0.80F, 1.45F);
                        this.startEvaporation(sl);
                        return true;
                    }

                    @Override
                    public void tick() {
                        super.tick();
                        if (!this.level().isClientSide() && this.level() instanceof ServerLevel sl && this.isAlive()) {
                            if (--this.decoyLifetime <= 0) {
                                this.startEvaporation(sl);
                            }
                        }
                    }
                };
                afterimage.setPos(player.getX(), player.getY(), player.getZ());
                afterimage.setYRot(player.getYRot());
                afterimage.yBodyRot = player.yBodyRot;
                afterimage.setYHeadRot(player.getYHeadRot());
                afterimage.setXRot(player.getXRot());
                afterimage.setIllusionActive(true);
                spawnedAfterimage = serverLevel.addFreshEntity(afterimage);
            }

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 0.9D, player.getZ(),
                    spawnedAfterimage ? 18 : 20, 0.45D, 0.55D, 0.45D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + 0.9D, player.getZ(),
                    spawnedAfterimage ? 12 : 10, 0.40D, 0.45D, 0.40D, 0.02D);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Right-Click (18s Cooldown): Vanish into Invisibility, Speed II, and Mirage Phasing for 10s.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Mirage Flash: Breaks nearby enemy lock-on, inflicts Blindness and Double Vision, and leaves a shimmering decoy.")
                .withStyle(ChatFormatting.AQUA));
    }
}
