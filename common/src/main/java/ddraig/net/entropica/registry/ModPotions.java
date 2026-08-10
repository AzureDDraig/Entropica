package ddraig.net.entropica.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create("entropica", Registries.POTION);

    // Vitae Nectar Potion: Regeneration I (15s) + Health Boost I (30s)
    public static final RegistrySupplier<Potion> VITAE_NECTAR = POTIONS.register("vitae_nectar",
            () -> new Potion("vitae_nectar",
                    new MobEffectInstance(MobEffects.REGENERATION, 300, 0),
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 600, 0)
            )
    );

    // Pyre Nectar Potion: Fire Resistance I (3m)
    public static final RegistrySupplier<Potion> PYRE_NECTAR = POTIONS.register("pyre_nectar",
            () -> new Potion("pyre_nectar",
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600, 0)
            )
    );
}
