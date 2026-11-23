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
 * Biome #11 ({@code 2.11 Storm-Swept Icebergs}) Core-Free Landmark:
 * {@code Structure 6: Auroral Vapor Observatory}.
 *
 * <p>Queries {@link Heightmap.Types#WORLD_SURFACE_WG} at chunk center and clamps
 * {@code baseY = Math.max(seaLevel, surfaceY)} so when generated in open ocean water
 * it builds its own floating Blue-Ice &amp; Packed-Ice iceberg crown at {@code Y=63},
 * and when generated on a tall storm iceberg it perches proudly atop the peak.</p>
 */
public class AuroralVaporObservatoryStructure extends Structure {

    public static final MapCodec<AuroralVaporObservatoryStructure> CODEC =
            simpleCodec(AuroralVaporObservatoryStructure::new);

    public AuroralVaporObservatoryStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int baseY = Math.max(context.chunkGenerator().getSeaLevel(), surfaceY);

        if (baseY + 24 >= context.heightAccessor().getMaxY()
                || baseY - 10 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new AuroralVaporObservatoryPiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.AURORAL_VAPOR_OBSERVATORY.isPresent()) {
            return ModStructures.AURORAL_VAPOR_OBSERVATORY.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_vapor_observatory"));
    }
}
