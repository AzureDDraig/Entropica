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
 * Biome #13 ({@code 2.13 Mirage Glacier}) Core-Free Landmark:
 * {@code Fata Morgana Mirage Arch-Sanctuary} ({@code 22 x 22 x 20} inland desert-glacier oasis &amp; parabolic sky-arch).
 *
 * <p>Contains strictly ZERO Shrine Cores or Anchors. Anchors cleanly onto U-shaped inland
 * glacier valleys and far-inland desert-glacier shelf pockets ({@code groundY >= 62}).</p>
 */
public class FataMorganaSanctuaryStructure extends Structure {

    public static final MapCodec<FataMorganaSanctuaryStructure> CODEC =
            simpleCodec(FataMorganaSanctuaryStructure::new);

    public FataMorganaSanctuaryStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int centerSurfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int centerFloorY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        if (centerSurfaceY < 62 || centerFloorY < 60) {
            return Optional.empty();
        }

        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 9, centerZ - 9, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 9, centerZ - 9, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 9, centerZ + 9, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 9, centerZ + 9, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int fNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 9, centerZ - 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int fNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 9, centerZ - 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int fSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 9, centerZ + 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int fSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 9, centerZ + 9, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int minOceanFloor = Math.min(centerFloorY, Math.min(Math.min(fNW, fNE), Math.min(fSW, fSE)));
        if (minOceanFloor < 59) {
            return Optional.empty();
        }

        int minSurface = Math.min(centerSurfaceY, Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE)));
        int maxSurface = Math.max(centerSurfaceY, Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE)));
        if (minSurface < 60 || maxSurface - minSurface > 10) {
            return Optional.empty();
        }

        int groundY = Math.max(62, (minSurface + maxSurface) / 2);
        // dy = 3 inside the 20-block-tall piece sits flush at groundY; dy = 0..2 houses the sub-glacier refraction alcove & pool basin
        int baseY = groundY - 3;

        if (baseY + 20 >= context.heightAccessor().getMaxY()
                || baseY - 8 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, groundY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new FataMorganaSanctuaryPiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.FATA_MORGANA_SANCTUARY.isPresent()) {
            return ModStructures.FATA_MORGANA_SANCTUARY.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fata_morgana_sanctuary"));
    }
}
