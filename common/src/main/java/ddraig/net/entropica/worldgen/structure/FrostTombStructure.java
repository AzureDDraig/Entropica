package ddraig.net.entropica.worldgen.structure;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Biome #8 ({@code 2.8 Barrow-Mound Tundra}) Signature Structure 5:
 * Frost-Tomb of the Aegis Wardens ({@code 26 x 15 x 32} earthen barrow mound
 * embedded ~4 blocks into solid tundra ground, housing the Recessed Tomb Portal,
 * Sarcophagus Antechamber, Central Crypt with Crypt-Siphon Lantern dais + 3 Runed Braziers,
 * and the breakable-wall Hidden Warden's Vault).
 */
public class FrostTombStructure extends Structure {

    public static final MapCodec<FrostTombStructure> CODEC =
            simpleCodec(FrostTombStructure::new);

    public FrostTombStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        int minSolidY = Math.max(62, context.chunkGenerator().getSeaLevel());

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int oceanFloorY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        // Require dry inland tundra above sea level (reject lakes, rivers, and oceans)
        if (oceanFloorY < minSolidY || surfaceY - oceanFloorY > 2) {
            return Optional.empty();
        }

        int[][] sampleCorners = {{-11, -13}, {11, -13}, {-11, 13}, {11, 13}};
        int minCornerY = oceanFloorY;
        int maxCornerY = oceanFloorY;
        for (int[] corner : sampleCorners) {
            int cx = centerX + corner[0];
            int cz = centerZ + corner[1];
            int cornerSurface = context.chunkGenerator().getFirstOccupiedHeight(
                    cx, cz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            int cornerFloor = context.chunkGenerator().getFirstOccupiedHeight(
                    cx, cz, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
            if (cornerFloor < minSolidY || cornerSurface - cornerFloor > 2) {
                return Optional.empty();
            }
            minCornerY = Math.min(minCornerY, cornerFloor);
            maxCornerY = Math.max(maxCornerY, cornerFloor);
        }

        // Seat the 26x32 barrow mound cleanly into rolling tundra hills
        if (maxCornerY - minCornerY > 12) {
            return Optional.empty();
        }

        int anchorSurfaceY = Mth.clamp(Math.min(surfaceY, (minCornerY + maxCornerY) / 2), minSolidY + 1, 180);
        if (anchorSurfaceY + 12 >= context.heightAccessor().getMaxY()
                || anchorSurfaceY - 8 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, anchorSurfaceY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new FrostTombPiece(centerX, anchorSurfaceY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.FROST_TOMB_OF_THE_AEGIS_WARDENS.isPresent()) {
            return ModStructures.FROST_TOMB_OF_THE_AEGIS_WARDENS.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_tomb_of_the_aegis_wardens"));
    }
}
