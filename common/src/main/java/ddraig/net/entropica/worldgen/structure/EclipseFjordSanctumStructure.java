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
 * Biome #5 ({@code 2.5 Silent Fjord}) Structure: Eclipse Fjord Watch-Sanctum ({@code 15 x 17 x 16}
 * Nordic basalt-column &amp; Deepslate-brick cliff-terrace watch-chapel anchored above sea level
 * ({@code Y = 64..128}), featuring twin hexagonal Basalt Column watch-obelisks on a Blue Frozen Moss
 * forecourt, a steep gabled Eclipse Nave with a glowing Chiseled Eclipse Basalt rose oculus and
 * Amethyst Ritual Pedestal, and a sunken tripwire-guarded Shadow Crypt).
 */
public class EclipseFjordSanctumStructure extends Structure {

    public static final MapCodec<EclipseFjordSanctumStructure> CODEC =
            simpleCodec(EclipseFjordSanctumStructure::new);

    public EclipseFjordSanctumStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int oceanFloorY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        // Require solid dry cliff terrace/ledge above sea level
        if (oceanFloorY < 63 || surfaceY - oceanFloorY > 3) {
            return Optional.empty();
        }

        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 6, centerZ - 7, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 6, centerZ - 7, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 6, centerZ + 7, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 6, centerZ + 7, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int minCornerY = Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE));
        int maxCornerY = Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE));

        if (minCornerY < 60 || maxCornerY - minCornerY > 22) {
            return Optional.empty();
        }

        int baseY = Mth.clamp(Math.min(surfaceY, (minCornerY + maxCornerY) / 2), 64, 128);
        if (baseY + 12 >= context.heightAccessor().getMaxY() || baseY - 6 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new EclipseFjordSanctumPiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.ECLIPSE_FJORD_SANCTUM.isPresent()) {
            return ModStructures.ECLIPSE_FJORD_SANCTUM.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eclipse_fjord_sanctum"));
    }
}
