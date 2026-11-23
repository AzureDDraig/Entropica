package ddraig.net.entropica.worldgen.structure;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Biome #3 ({@code 2.3 Frost-Bite Floes}) Structure: Icebound Blight-Wreck ({@code 16 x 22 x 14}
 * canted timber sailing vessel crushed and locked into a sea-level Blight-packed ice pressure ridge
 * at {@code Y = 63}, featuring a rime-icicle bowsprit, flooded necrotic-rot bilge with Rotten Ice
 * trap floorboards and a smuggler's barrel, snapped ice-crusted central mast, and elevated stern
 * Captain's Cabin).
 */
public class IceboundBlightWreckStructure extends Structure {

    public static final MapCodec<IceboundBlightWreckStructure> CODEC =
            simpleCodec(IceboundBlightWreckStructure::new);

    private static final int SEA_LEVEL_ANCHOR_Y = 63;

    public IceboundBlightWreckStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int centerFloor = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 7, centerZ - 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 7, centerZ - 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 7, centerZ + 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 7, centerZ + 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int minFloor = Math.min(centerFloor, Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE)));
        int maxFloor = Math.max(centerFloor, Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE)));

        if (minFloor < 45 || maxFloor > 68) {
            return Optional.empty();
        }

        int baseY = SEA_LEVEL_ANCHOR_Y;
        if (baseY + 11 >= context.heightAccessor().getMaxY() || baseY - 4 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new IceboundBlightWreckPiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.ICEBOUND_BLIGHT_WRECK.isPresent()) {
            return ModStructures.ICEBOUND_BLIGHT_WRECK.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "icebound_blight_wreck"));
    }
}
