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
 * Biome #7 ({@code 2.7 Static Ice-Plains}) Signature Structure: Fulgurite Cryo-Relay
 * ({@code 17 x 17 x 24} octagonal copper-and-deepslate Faraday rotunda, 4 corner oxidized-gradient
 * coil pylons, 20-block corona-finned storm-needle spire, central Biomass Capacitor relay,
 * exterior fulgurite/blue-ice grounding moat with a lightning-struck Fulgurite Pine, and an
 * underground Capacitor Vault guarded by a Slowness splash-potion dispenser trap).
 */
public class FulguriteCryoRelayStructure extends Structure {

    public static final MapCodec<FulguriteCryoRelayStructure> CODEC =
            simpleCodec(FulguriteCryoRelayStructure::new);

    public FulguriteCryoRelayStructure(StructureSettings settings) {
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

        // Require solid inland snow/ice plains above sea level (reject lakes/rivers)
        if (oceanFloorY < 62 || surfaceY - oceanFloorY > 2) {
            return Optional.empty();
        }

        int[][] sampleCorners = {{-7, -7}, {7, -7}, {-7, 7}, {7, 7}};
        int minCornerY = oceanFloorY;
        int maxCornerY = oceanFloorY;
        for (int[] corner : sampleCorners) {
            int cx = centerX + corner[0];
            int cz = centerZ + corner[1];
            int cornerSurface = context.chunkGenerator().getFirstOccupiedHeight(
                    cx, cz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            int cornerFloor = context.chunkGenerator().getFirstOccupiedHeight(
                    cx, cz, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
            if (cornerFloor < 62 || cornerSurface - cornerFloor > 2) {
                return Optional.empty();
            }
            minCornerY = Math.min(minCornerY, cornerFloor);
            maxCornerY = Math.max(maxCornerY, cornerFloor);
        }

        // Keep the 17x17 Faraday plinth & moat seated cleanly on flat wind-swept plains
        if (maxCornerY - minCornerY > 10) {
            return Optional.empty();
        }

        int baseY = Mth.clamp(Math.min(surfaceY, (minCornerY + maxCornerY) / 2), 63, 180);
        if (baseY + 20 >= context.heightAccessor().getMaxY() || baseY - 7 <= context.heightAccessor().getMinY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new FulguriteCryoRelayPiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        if (ModStructures.FULGURITE_CRYO_RELAY.isPresent()) {
            return ModStructures.FULGURITE_CRYO_RELAY.get();
        }
        return BuiltInRegistries.STRUCTURE_TYPE.getValue(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_cryo_relay"));
    }
}
