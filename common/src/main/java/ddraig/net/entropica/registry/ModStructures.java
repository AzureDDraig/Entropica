package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.worldgen.structure.FrostVeiledRunicSpirePiece;
import ddraig.net.entropica.worldgen.structure.FrostVeiledRunicSpireStructure;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.LootTable;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Entropica.MODID, Registries.STRUCTURE_TYPE);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Entropica.MODID, Registries.STRUCTURE_PIECE);

    public static final ResourceKey<Structure> FROST_VEILED_GLACIAL_RUNIC_SPIRE_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_veiled_glacial_runic_spire"));

    public static final ResourceKey<LootTable> STRUCTURE_1_CHEST_LOOT =
            ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chests/structure_1"));

    public static final RegistrySupplier<StructureType<FrostVeiledRunicSpireStructure>> FROST_VEILED_GLACIAL_RUNIC_SPIRE =
            STRUCTURE_TYPES.register("frost_veiled_glacial_runic_spire",
                    () -> () -> FrostVeiledRunicSpireStructure.CODEC);

    public static final RegistrySupplier<StructurePieceType> FROST_VEILED_GLACIAL_RUNIC_SPIRE_PIECE =
            STRUCTURE_PIECES.register("frost_veiled_glacial_runic_spire_piece",
                    () -> FrostVeiledRunicSpirePiece::new);

    private ModStructures() {}

    public static void register() {
        STRUCTURE_TYPES.register();
        STRUCTURE_PIECES.register();
    }
}
