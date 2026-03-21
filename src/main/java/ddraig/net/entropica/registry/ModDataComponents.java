package ddraig.net.entropica.registry;

import ddraig.net.entropica.component.EssenceCombatStats;
import ddraig.net.entropica.component.VisWeaponState;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "entropica");

    // Component for Armor/Curios handling raw Vis Resistances
    public static final Supplier<DataComponentType<EssenceCombatStats>> ESSENCE_STATS = COMPONENTS.register("essence_stats",
            () -> DataComponentType.<EssenceCombatStats>builder()
                    .persistent(EssenceCombatStats.CODEC)
                    .networkSynchronized(EssenceCombatStats.STREAM_CODEC)
                    .build());

    // Component for Weapons handling Base Types, Active Profiles, and Slotted Fragments
    public static final Supplier<DataComponentType<VisWeaponState>> VIS_WEAPON_STATE = COMPONENTS.register("vis_weapon_state",
            () -> DataComponentType.<VisWeaponState>builder()
                    .persistent(VisWeaponState.CODEC)
                    .networkSynchronized(VisWeaponState.STREAM_CODEC)
                    .build());

    // Component for Wire Spools tracking the starting connection node
    public static final Supplier<DataComponentType<GlobalPos>> SPOOL_BINDING = COMPONENTS.register("spool_binding",
            () -> DataComponentType.<GlobalPos>builder()
                    .persistent(GlobalPos.CODEC)
                    .networkSynchronized(GlobalPos.STREAM_CODEC)
                    .build());
}