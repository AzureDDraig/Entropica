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
 * Biome #9 ({@code 2.9 Null-Fringe Bay}) Legendary Sanctuary:
 * {@code Null-Fringe Rift Bastion} ({@code 24 x 26 x 24} spatial rift bastion anchored at
 * bay sea level / shoreline, featuring the Causeway Pier &amp; Sunken {@code Void Glass}
 * Reliquary Vault, Four Crystal-Crowned Obsidian Buttress Pylons, Low-Gravity Floating
 * Stepping-Stone Spiral, and the Central Sanctuary Dais &amp; Rift Crown housing a floating
 * {@code void_crystal_block} focus beneath an apex {@code spatial_phase_anchor}).
 */
public class NullFringeRiftBastionStructure extends Structure {

    public static final MapCodec<NullFringeRiftBastionStructure> CODEC =
            simpleCodec(NullFringeRiftBastionStructure::new);

    public NullFringeRiftBastionStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        int seaLevel = context.chunkGenerator().getSeaLevel();

        int sampledY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int oceanFloorY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        // Seat in coastal bays & shorelines (reject abyssal ocean trenches or high inland mountain cliffs)
        if (oceanFloorY < seaLevel - 18 || sampledY > seaLevel + 8) {
            return Optional.empty();
        }

        int[][] sampleCorners = {{-10, -10}, {10, -10}, {-10, 10}, {10, 10}};
        int minCornerFloor = oceanFloorY;
        int maxCornerSurface = sampledY;
        for (int[] corner : sampleCorners) {
            int cx = centerX + corner[0];
            int cz = centerZ + corner[1];
            int cSurface = context.chunkGenerator().getFirstOccupiedHeight(
                    cx, cz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            int cFloor = context.chunkGenerator().getFirstOccupiedHeight(
                    cx, cz, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
            if (cFloor < seaLevel - 20 || cSurface > seaLevel + 10) {
                return Optional.empty();
            }
            minCornerFloor = Math.min(minCornerFloor, cFloor);
            maxCornerSurface = Math.max(maxCornerSurface, cSurface);
        }

        if (maxCornerSurface - minCornerFloor > 22) {
            return Optional.empty();
        }

        int surfaceY = Math.max(seaLevel, sampledY);
        if (surfaceY + 21 >= context.heightAccessor().getMaxY()
                || surfaceY - 8 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, surfaceY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new NullFringeRiftBastionPiece(centerX, surfaceY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.NULL_FRINGE_RIFT_BASTION.isPresent()) {
            return ModStructures.NULL_FRINGE_RIFT_BASTION.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "null_fringe_rift_bastion"));
    }
}
