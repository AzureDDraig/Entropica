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
 * Biome #17 ({@code 2.17 Astral Skerries}) Core-Free &amp; Anchor-Free Signature Observatory:
 * {@code Stardust Skerry Orrery} ({@code 20 x 20 x 22}).
 *
 * <p>Contains strictly ZERO Life-Force Core blocks and ZERO Life-Force Anchor blocks.
 * Anchors on rocky/starlight-ice skerries or shallow cosmic channels with a tapered
 * {@code Astral Impactite}, {@code Starlight Ice}, and {@code Coarse Gravel} skerry foundation
 * extending down to the solid seafloor so the observatory never floats awkwardly over water.</p>
 */
public class StardustSkerryOrreryStructure extends Structure {

    public static final MapCodec<StardustSkerryOrreryStructure> CODEC =
            simpleCodec(StardustSkerryOrreryStructure::new);

    public StardustSkerryOrreryStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int rawSeaLevel = context.chunkGenerator().getSeaLevel();
        int seaLevel = rawSeaLevel > 0 ? rawSeaLevel : 63;

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

        // Place on skerry islets or shallow/moderate archipelago channels; skip extreme cliffs or deep abysses
        if (maxSurface > seaLevel + 14 || minOceanFloor < seaLevel - 20) {
            return Optional.empty();
        }

        int baseY = Math.max(seaLevel - 1, Math.min(surfaceCenter, seaLevel + 4));
        if (baseY + 19 >= context.heightAccessor().getMaxY()
                || baseY - 22 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new StardustSkerryOrreryPiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.STARDUST_SKERRY_ORRERY.isPresent()) {
            return ModStructures.STARDUST_SKERRY_ORRERY.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "stardust_skerry_orrery"));
    }
}
