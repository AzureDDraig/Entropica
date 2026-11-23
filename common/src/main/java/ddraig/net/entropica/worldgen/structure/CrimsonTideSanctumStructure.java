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
 * Biome #16 ({@code 2.16 Blood-Ice Shallows}) Core-Free Sanctuary:
 * {@code Crimson Tide Sanctum} ({@code 20 x 20 x 18}).
 *
 * <p>Contains strictly ZERO Shrine Cores and ZERO Anchors. Anchors at sea level
 * ({@code Y = 62} coastal bay margin / shallow water) with {@code Red Clay Sand} and
 * {@code Deepslate Tiles} pilings extending downward to seal into the seabed so the
 * sanctum never floats.</p>
 */
public class CrimsonTideSanctumStructure extends Structure {

    public static final MapCodec<CrimsonTideSanctumStructure> CODEC =
            simpleCodec(CrimsonTideSanctumStructure::new);

    public CrimsonTideSanctumStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int rawSeaLevel = context.chunkGenerator().getSeaLevel();
        int seaLevel = rawSeaLevel > 0 ? rawSeaLevel : 63;
        int waterTopY = seaLevel - 1; // Y = 62 sea-level water surface

        int surfaceCenter = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ - 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ - 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ + 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ + 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int floorCenter = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int fNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ - 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int fNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ - 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int fSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ + 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int fSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ + 8, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int maxSurface = Math.max(surfaceCenter, Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE)));
        int minOceanFloor = Math.min(floorCenter, Math.min(Math.min(fNW, fNE), Math.min(fSW, fSE)));

        // Keep strictly in shallow coastal bay waters or low sea-level ice shelves (avoid tall hills or deep trenches)
        if (maxSurface > waterTopY + 8 || minOceanFloor < waterTopY - 14) {
            return Optional.empty();
        }

        if (waterTopY + 15 >= context.heightAccessor().getMaxY()
                || waterTopY - 16 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, waterTopY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new CrimsonTideSanctumPiece(centerX, waterTopY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.CRIMSON_TIDE_SANCTUM.isPresent()) {
            return ModStructures.CRIMSON_TIDE_SANCTUM.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crimson_tide_sanctum"));
    }
}
