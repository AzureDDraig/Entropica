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
 * Biome #12 ({@code 2.12 Spore-Glazed Pack}) Core-Free Landmark:
 * {@code Glazed Mycelial Conservatory} ({@code 20 x 20 x 18} coastal pack-ice biodome).
 *
 * <p>Contains strictly ZERO Shrine Cores or Anchors. Anchors cleanly onto sheltered coastal
 * bay margins, pack-ice shelves, or shallow sea-level bay mouths.</p>
 */
public class GlazedMycelialConservatoryStructure extends Structure {

    public static final MapCodec<GlazedMycelialConservatoryStructure> CODEC =
            simpleCodec(GlazedMycelialConservatoryStructure::new);

    public GlazedMycelialConservatoryStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        int seaLevel = context.chunkGenerator().getSeaLevel();
        int waterTopY = Math.max(62, seaLevel - 1);

        int centerSurfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int centerFloorY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        // Reject deep ocean trenches while allowing shallow coastal bay shelves & shoreline margins
        if (centerFloorY < waterTopY - 9 || centerSurfaceY > 96) {
            return Optional.empty();
        }

        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ - 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ - 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ + 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ + 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int minSurface = Math.min(centerSurfaceY, Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE)));
        int maxSurface = Math.max(centerSurfaceY, Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE)));
        if (maxSurface - minSurface > 8) {
            return Optional.empty();
        }

        int avgSurface = (minSurface + maxSurface) / 2;
        int baseY = Math.max(waterTopY, avgSurface - 1);

        if (baseY + 18 >= context.heightAccessor().getMaxY()
                || baseY - 8 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new GlazedMycelialConservatoryPiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.GLAZED_MYCELIAL_CONSERVATORY.isPresent()) {
            return ModStructures.GLAZED_MYCELIAL_CONSERVATORY.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glazed_mycelial_conservatory"));
    }
}
