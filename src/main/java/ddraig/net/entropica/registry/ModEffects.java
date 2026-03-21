package ddraig.net.entropica.registry;

import ddraig.net.entropica.effect.VisToxicityEffect;
import ddraig.net.entropica.effect.VoidTearEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, "entropica");

    // Registers the effect as Harmful with a toxic cyan particle color
    public static final DeferredHolder<MobEffect, VisToxicityEffect> VIS_TOXICITY = EFFECTS.register("vis_toxicity",
            () -> new VisToxicityEffect(MobEffectCategory.HARMFUL, 0x1AE5D4));

    // Registers the new Void Tear effect with a dark abyssal purple color
    public static final DeferredHolder<MobEffect, VoidTearEffect> VOID_TEAR = EFFECTS.register("void_tear",
            VoidTearEffect::new);
}