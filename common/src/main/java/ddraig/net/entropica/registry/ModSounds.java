package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Entropica.MODID, net.minecraft.resources.ResourceKey.createRegistryKey(net.minecraft.resources.ResourceLocation.withDefaultNamespace("sound_event")));

    // Veil Fox Sounds
    public static final RegistrySupplier<SoundEvent> VEIL_FOX_BLINK = registerSoundEvent("veil_fox_blink");
    public static final RegistrySupplier<SoundEvent> VEIL_FOX_AMBIENT = registerSoundEvent("veil_fox_ambient");
    public static final RegistrySupplier<SoundEvent> VEIL_FOX_HURT = registerSoundEvent("veil_fox_hurt");
    public static final RegistrySupplier<SoundEvent> VEIL_FOX_DEATH = registerSoundEvent("veil_fox_death");
    public static final RegistrySupplier<SoundEvent> VEIL_FOX_CHORUS = registerSoundEvent("veil_fox_chorus");

    // Forcefield & Gravity Sounds
    public static final RegistrySupplier<SoundEvent> FORCEFIELD_BOUNCE = registerSoundEvent("forcefield_bounce");
    public static final RegistrySupplier<SoundEvent> BOUNCEPAD_LAUNCH = registerSoundEvent("bouncepad_launch");

    private static RegistrySupplier<SoundEvent> registerSoundEvent(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, name)));
    }
}