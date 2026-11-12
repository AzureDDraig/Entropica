package ddraig.net.entropica.effect;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;

/**
 * Abrasive Armor Erosion status effect (Spellcrafting System Guide Section 6.18 — Dust Materia):
 * reduces target Armor and Armor Toughness by 5% per level (up to Level 20 = 100% total armor reduction).
 */
public class ArmorErosionEffect extends MobEffect {

    public static final ResourceLocation MOD_ARMOR_ID =
            ResourceLocation.fromNamespaceAndPath("entropica", "armor_erosion_armor");
    public static final ResourceLocation MOD_TOUGHNESS_ID =
            ResourceLocation.fromNamespaceAndPath("entropica", "armor_erosion_toughness");

    public ArmorErosionEffect() {
        super(MobEffectCategory.HARMFUL, 0xC2B28F);
        this.addAttributeModifier(
                Attributes.ARMOR,
                MOD_ARMOR_ID,
                -0.05D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
                Attributes.ARMOR_TOUGHNESS,
                MOD_TOUGHNESS_ID,
                -0.05D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                entity.getX(),
                entity.getY(0.65D),
                entity.getZ(),
                2,
                0.22D,
                0.25D,
                0.22D,
                0.01D
        );
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 25 == 0;
    }
}
