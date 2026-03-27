package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Entropica.MODID);

    // Veil Fox Sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> VEIL_FOX_BLINK = registerSoundEvent("veil_fox_blink");
    public static final DeferredHolder<SoundEvent, SoundEvent> VEIL_FOX_AMBIENT = registerSoundEvent("veil_fox_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> VEIL_FOX_HURT = registerSoundEvent("veil_fox_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> VEIL_FOX_DEATH = registerSoundEvent("veil_fox_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> VEIL_FOX_CHORUS = registerSoundEvent("veil_fox_chorus");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, name)));
    }
}