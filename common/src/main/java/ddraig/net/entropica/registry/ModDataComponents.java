package ddraig.net.entropica.registry;

import ddraig.net.entropica.component.EssenceCombatStats;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create("entropica", Registries.DATA_COMPONENT_TYPE);

    // Component for Armor/Curios handling raw Materia Resistances
    public static final RegistrySupplier<DataComponentType<EssenceCombatStats>> ESSENCE_STATS = COMPONENTS.register("essence_stats",
            () -> DataComponentType.<EssenceCombatStats>builder()
                    .persistent(EssenceCombatStats.CODEC)
                    .networkSynchronized(EssenceCombatStats.STREAM_CODEC)
                    .build());

    // Component for Weapons handling Base Types, Active Profiles, and Slotted Fragments
    public static final RegistrySupplier<DataComponentType<VisWeaponState>> VIS_WEAPON_STATE = COMPONENTS.register("vis_weapon_state",
            () -> DataComponentType.<VisWeaponState>builder()
                    .persistent(VisWeaponState.CODEC)
                    .networkSynchronized(VisWeaponState.STREAM_CODEC)
                    .build());

    // Component for Wire Spools tracking the starting connection node
    public static final RegistrySupplier<DataComponentType<GlobalPos>> SPOOL_BINDING = COMPONENTS.register("spool_binding",
            () -> DataComponentType.<GlobalPos>builder()
                    .persistent(GlobalPos.CODEC)
                    .networkSynchronized(GlobalPos.STREAM_CODEC)
                    .build());

    // Component for Inscribed Spellgems & Spell Parchments storing modular multi-Materia spell assemblies
    public static final RegistrySupplier<DataComponentType<SpellAssembly>> SPELL_ASSEMBLY = COMPONENTS.register("spell_assembly",
            () -> DataComponentType.<SpellAssembly>builder()
                    .persistent(SpellAssembly.CODEC)
                    .networkSynchronized(SpellAssembly.STREAM_CODEC)
                    .build());

    // Component for Casting Apparatuses (Focus, Wand, Staff, Grimoire) and Engraved Weapons/Tools
    public static final RegistrySupplier<DataComponentType<CastingApparatusState>> CASTING_APPARATUS_STATE = COMPONENTS.register("casting_apparatus_state",
            () -> DataComponentType.<CastingApparatusState>builder()
                    .persistent(CastingApparatusState.CODEC)
                    .networkSynchronized(CastingApparatusState.STREAM_CODEC)
                    .build());
}
