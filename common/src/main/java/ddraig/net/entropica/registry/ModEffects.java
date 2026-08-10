package ddraig.net.entropica.registry;

import ddraig.net.entropica.effect.MateriaToxicityEffect;
import ddraig.net.entropica.effect.VoidTearEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create("entropica", net.minecraft.resources.ResourceKey.createRegistryKey(net.minecraft.resources.ResourceLocation.withDefaultNamespace("mob_effect")));

    // Registers the effect as Harmful with a toxic cyan particle color
    public static final RegistrySupplier<MobEffect> MATERIA_TOXICITY = EFFECTS.register("materia_toxicity",
            () -> new MateriaToxicityEffect(MobEffectCategory.HARMFUL, 0x1AE5D4));

    // Registers the new Void Tear effect with a dark abyssal purple color
    public static final RegistrySupplier<MobEffect> VOID_TEAR = EFFECTS.register("void_tear",
            VoidTearEffect::new);

    // Registers the new Haze effect with a custom color
    public static final RegistrySupplier<MobEffect> HAZE = EFFECTS.register("haze",
            ddraig.net.entropica.effect.HazeEffect::new);

    // Registers the new Paralyzed effect with a purple electric color
    public static final RegistrySupplier<MobEffect> PARALYZED = EFFECTS.register("paralyzed",
            () -> new ddraig.net.entropica.effect.ParalyzedEffect(MobEffectCategory.HARMFUL, 0x9D4EDD));

    // Registers the new Bleeding effect with a deep blood crimson color
    public static final RegistrySupplier<MobEffect> BLEEDING = EFFECTS.register("bleeding",
            () -> new ddraig.net.entropica.effect.BleedingEffect(MobEffectCategory.HARMFUL, 0x991B1B));
}