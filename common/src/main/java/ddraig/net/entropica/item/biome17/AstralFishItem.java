package ddraig.net.entropica.item.biome17;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * AstralFishItem — shared item class for the 12 celestial Star-Fishing catches and cooked delicacies
 * of 2.17 Astral Skerries, handling custom nutrition, saturation, post-consumption Materia buffs, and tooltips.
 */
public class AstralFishItem extends Item {

    public enum FishProfile {
        NEBULA_DARTER(2, 0.2F, false,
                "Raw Star-Fish: Caught in the starlit waters of Astral Skerries; feeds Starlight Nautiluses."),
        COOKED_NEBULA_DARTER(6, 0.7F, true,
                "Star-Fish Delicacy: Grants Starlight Grace (30s)."),
        STARWING_FLYING_FISH(2, 0.2F, false,
                "Raw Star-Fish: Gliding pelagic fish of Astral Skerries; feeds Starlight Nautiluses."),
        COOKED_STARWING_FLYING_FISH(6, 0.7F, true,
                "Star-Fish Delicacy: Grants Jump Boost I & Slow Falling (30s)."),
        STARFALL_RAY(3, 0.3F, false,
                "Raw Star-Fish: Broad-winged constellation ray of Astral Skerries; feeds Starlight Nautiluses."),
        COOKED_STARFALL_RAY(7, 0.8F, true,
                "Star-Fish Delicacy: Grants Slow Falling & Starlight Grace (45s)."),
        COMET_RIBBONFISH(2, 0.2F, false,
                "Raw Star-Fish: Swift silver-tailed ribbonfish of Astral Skerries; feeds Starlight Nautiluses."),
        COOKED_COMET_RIBBONFISH(6, 0.7F, true,
                "Star-Fish Delicacy: Grants Speed I & Starlight Grace (30s)."),
        ZENITH_MOONFISH(3, 0.3F, false,
                "Raw Star-Fish: Luminous disc-bodied moonfish of Astral Skerries; feeds Starlight Nautiluses."),
        COOKED_ZENITH_MOONFISH(7, 0.8F, true,
                "Star-Fish Delicacy: Purges Darkness & Blindness and grants Starlight Grace (45s)."),
        SUPERNOVA_PUFFER(1, 0.1F, true,
                "Radiant Star-Fish: Grants Water Breathing & Glowing (15s); feeds Starlight Nautiluses."),
        ASTRAL_LANTERNFISH(3, 0.4F, true,
                "Abyssal Star-Fish: Grants Night Vision & Starlight Grace (2:00); feeds Starlight Nautiluses.");

        private final int nutrition;
        private final float saturation;
        private final boolean alwaysEdible;
        private final String tooltip;

        FishProfile(int nutrition, float saturation, boolean alwaysEdible, String tooltip) {
            this.nutrition = nutrition;
            this.saturation = saturation;
            this.alwaysEdible = alwaysEdible;
            this.tooltip = tooltip;
        }

        public FoodProperties buildFoodProperties() {
            FoodProperties.Builder builder = new FoodProperties.Builder()
                    .nutrition(this.nutrition)
                    .saturationModifier(this.saturation);
            if (this.alwaysEdible) {
                builder.alwaysEdible();
            }
            return builder.build();
        }
    }

    private final FishProfile profile;

    public AstralFishItem(Properties properties, FishProfile profile) {
        super(properties.food(profile.buildFoodProperties()));
        this.profile = profile;
    }

    public FishProfile getProfile() {
        return this.profile;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()) {
            applyPostEatEffects(entity);
        }
        return result;
    }

    private void applyPostEatEffects(LivingEntity entity) {
        Holder<MobEffect> graceHolder = StarlightDraughtAmpouleItem.resolveStarlightGraceHolder();
        switch (this.profile) {
            case COOKED_NEBULA_DARTER -> {
                if (graceHolder != null) {
                    entity.addEffect(new MobEffectInstance(graceHolder, 600, 0));
                }
            }
            case COOKED_STARWING_FLYING_FISH -> {
                entity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 600, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0));
            }
            case COOKED_STARFALL_RAY -> {
                entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 900, 0));
                if (graceHolder != null) {
                    entity.addEffect(new MobEffectInstance(graceHolder, 900, 0));
                }
            }
            case COOKED_COMET_RIBBONFISH -> {
                entity.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 0));
                if (graceHolder != null) {
                    entity.addEffect(new MobEffectInstance(graceHolder, 600, 0));
                }
            }
            case COOKED_ZENITH_MOONFISH -> {
                if (entity.hasEffect(MobEffects.DARKNESS)) {
                    entity.removeEffect(MobEffects.DARKNESS);
                }
                if (entity.hasEffect(MobEffects.BLINDNESS)) {
                    entity.removeEffect(MobEffects.BLINDNESS);
                }
                if (graceHolder != null) {
                    entity.addEffect(new MobEffectInstance(graceHolder, 900, 0));
                }
            }
            case SUPERNOVA_PUFFER -> {
                entity.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0));
            }
            case ASTRAL_LANTERNFISH -> {
                entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2400, 0));
                if (graceHolder != null) {
                    entity.addEffect(new MobEffectInstance(graceHolder, 2400, 0));
                }
            }
            default -> {
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        ChatFormatting color = this.profile.alwaysEdible ? ChatFormatting.AQUA : ChatFormatting.GRAY;
        tooltipComponents.accept(Component.literal(this.profile.tooltip).withStyle(color));
    }
}
