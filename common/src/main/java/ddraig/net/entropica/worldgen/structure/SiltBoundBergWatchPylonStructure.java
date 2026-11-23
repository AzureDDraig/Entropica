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
 * Biome #15 ({@code 2.15 Dust-Crusted Bergs}) Core-Free Sanctuary:
 * {@code Silt-Bound Berg Watch-Pylon} ({@code 20 x 20 x 22}).
 *
 * <p>Contains strictly ZERO Shrine Cores or Life-Force Anchors. Anchors at
 * {@code Math.max(seaLevel, surfaceY)} so it sits cleanly on open polar ocean water
 * OR wind-scoured coastal shelves without ever floating.</p>
 */
public class SiltBoundBergWatchPylonStructure extends Structure {

    public static final MapCodec<SiltBoundBergWatchPylonStructure> CODEC =
            simpleCodec(SiltBoundBergWatchPylonStructure::new);

    public SiltBoundBergWatchPylonStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int seaLevel = context.chunkGenerator().getSeaLevel();
        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ - 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ - 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 8, centerZ + 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 8, centerZ + 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int minSurface = Math.min(surfaceY, Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE)));
        int maxSurface = Math.max(surfaceY, Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE)));
        if (maxSurface > seaLevel + 14 || maxSurface - minSurface > 10) {
            return Optional.empty();
        }

        int anchorY = Math.max(seaLevel, surfaceY);
        // When spawning on ocean water or low sea-level ice, elevate the flat table-iceberg plateau
        // to seaLevel + 2 (Y=65) so sheer iceberg cliffs rise above water and dy = -7 reaches seaLevel - 5 (Y=58)!
        int plateauY = Math.max(seaLevel + 2, anchorY);

        if (plateauY + 14 >= context.heightAccessor().getMaxY()
                || plateauY - 12 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, anchorY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new SiltBoundBergWatchPylonPiece(centerX, plateauY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.SILT_BOUND_BERG_WATCH_PYLON.isPresent()) {
            return ModStructures.SILT_BOUND_BERG_WATCH_PYLON.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silt_bound_berg_watch_pylon"));
    }
}
