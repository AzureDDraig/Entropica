package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import java.util.Optional;

/**
 * Programmatic 3D generator for Biome #11's core-free Structure 6:
 * {@code Auroral Vapor Observatory} ({@code 22 x 22 x 32}, {@code x = -11..10, y = -9..22, z = -11..10}
 * relative to {@code baseY} — strictly zero Shrine Cores or Anchors):
 * <ul>
 *   <li><b>1. Storm-Carved Iceberg Base Crown ({@code dy = -9..0, r <= 10.5})</b>:
 *       Solid {@link Blocks#BLUE_ICE} &amp; {@link Blocks#PACKED_ICE} keel anchored deep into ocean
 *       water/ice, rimmed with {@link Blocks#SNOW_BLOCK} ledges, {@code entropica:fulgurite_ice}
 *       lightning streaks, and {@code entropica:zephyr_glass_grass} tufts on snow/zephyr-ice cells.</li>
 *   <li><b>2. Ground Floor — Engine &amp; Steam Room ({@code dy = 1..5, r <= 6.6})</b>:
 *       {@code entropica:fulgurite_block}, {@link Blocks#DEEPSLATE_BRICKS}, and {@link Blocks#CUT_COPPER}
 *       cylindrical masonry with arched North/West doorways, {@code 4} {@link Blocks#COPPER_BLOCK} +
 *       {@code entropica:vapor_pneumatic_pipe_copper} + {@link Blocks#LIGHTNING_ROD} steam/charge
 *       conduits, and the central {@code entropica:zephyr_updraft_grate} at {@code (0, 0, 0)} beneath
 *       an unobstructed {@code 3x3} vertical wind-lift shaft ({@code dy = 1..11}).</li>
 *   <li><b>3. Main Deck — Silver Pine Map &amp; Chart Gallery ({@code dy = 6..11, r <= 7.6})</b>:
 *       {@code entropica:silver_pine_planks} deck around the open air-lift well,
 *       {@code entropica:silver_pine_log} pillars, {@link Blocks#TINTED_GLASS} panoramic storm windows,
 *       {@code 2} heated {@link Blocks#WATER} side planters ({@code entropica:steaming_spring_stone} +
 *       {@code entropica:auroral_lily_pad}), {@link Blocks#LECTERN} chart stand, {@code 2} loot chests
 *       ({@code entropica:chests/structure_6}), and {@code 4} {@code entropica:simurgh_wind_turbine}s
 *       bolted onto corner {@link Blocks#CUT_COPPER} outrigger brackets.</li>
 *   <li><b>4. Rotating Observatory Dome, Giant Copper Sky-Telescope &amp; Storm Collector Mast ({@code dy = 12..22})</b>:
 *       Upper {@code silver_pine_planks} deck with an upper {@code zephyr_updraft_grate} landing collar
 *       and {@code entropica:stationary_brass_telescope}, hemispherical {@link Blocks#CUT_COPPER} &amp;
 *       {@code entropica:zephyr_ice} dome with an open {@code (-X, +Z)} shutter slit, {@code 7}-step
 *       diagonal {@link Blocks#COPPER_BLOCK} + {@link Blocks#CUT_COPPER} Sky-Telescope barrel capped with
 *       {@code entropica:auroral_lens_block} &amp; {@link Blocks#TINTED_GLASS}, and a pure copper +
 *       stacked {@link Blocks#LIGHTNING_ROD} Storm Collector Mast at {@code (0, 18..22, 0)}.</li>
 * </ul>
 */
public class AuroralVaporObservatoryPiece extends StructurePiece {

    private static final int[][] ENGINE_CONDUIT_OFFSETS = {
            {-3, -3}, {3, -3}, {-3, 3}, {3, 3}
    };

    private static final int[][] GALLERY_LOG_PILLARS = {
            {-6, -2}, {-6, 2}, {6, -2}, {6, 2},
            {-2, -6}, {2, -6}, {-2, 6}, {2, 6},
            {-5, -5}, {5, -5}, {-5, 5}, {5, 5}
    };

    private static final int[][] CORNER_SIGNS = {
            {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;

    public AuroralVaporObservatoryPiece(int originX, int baseY, int originZ) {
        // Exact 22 (W: -11..+10) x 32 (H: -9..+22) x 22 (L: -11..+10) bounding box
        super(resolvePieceType(), 0, new BoundingBox(
                originX - 11,
                baseY - 9,
                originZ - 11,
                originX + 10,
                baseY + 22,
                originZ + 10
        ));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public AuroralVaporObservatoryPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", this.boundingBox.minX() + 11);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 9);
        this.originZ = tag.getIntOr("OriginZ", this.boundingBox.minZ() + 11);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.AURORAL_VAPOR_OBSERVATORY_PIECE.isPresent()) {
            return ModStructures.AURORAL_VAPOR_OBSERVATORY_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Entropica.MODID, "auroral_vapor_observatory_piece");
        return BuiltInRegistries.STRUCTURE_PIECE.getOptional(id).orElseThrow();
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OriginX", this.originX);
        tag.putInt("BaseY", this.baseY);
        tag.putInt("OriginZ", this.originZ);
    }

    public int getOriginX() {
        return this.originX;
    }

    public int getBaseY() {
        return this.baseY;
    }

    public int getOriginZ() {
        return this.originZ;
    }

    public BlockPos getCentralUpdraftGratePos() {
        return toWorld(0, 0, 0);
    }

    public BlockPos getWestGalleryChestPos() {
        return toWorld(-4, 7, 0);
    }

    public BlockPos getNorthGalleryChestPos() {
        return toWorld(2, 7, -4);
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBox,
            ChunkPos chunkPos,
            BlockPos pivot
    ) {
        buildStormCarvedIcebergBaseCrown(level, chunkBox);
        buildGroundFloorEngineAndSteamRoom(level, chunkBox);
        buildMainDeckChartGalleryAndTurbines(level, chunkBox, random);
        buildObservatoryDomeTelescopeAndMast(level, chunkBox);
    }

    private void buildStormCarvedIcebergBaseCrown(WorldGenLevel level, BoundingBox chunkBox) {
        Block zephyrIceBlock = resolveBlock("zephyr_ice", Blocks.PACKED_ICE);
        Block fulguriteIceBlock = resolveBlock("fulgurite_ice", Blocks.BLUE_ICE);
        Block fulguriteStoneBlock = resolveBlock("fulgurite_block", Blocks.DEEPSLATE_BRICKS);
        Block updraftGrateBlock = resolveBlock("zephyr_updraft_grate", Blocks.CUT_COPPER);
        Block springStoneBlock = resolveBlock("steaming_spring_stone", Blocks.COPPER_BLOCK);
        Optional<Block> zephyrGrassOpt = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_glass_grass"));

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState snowBlock = Blocks.SNOW_BLOCK.defaultBlockState();
        BlockState zephyrIce = zephyrIceBlock.defaultBlockState();
        BlockState fulguriteIce = fulguriteIceBlock.defaultBlockState();
        BlockState fulguriteBlock = fulguriteStoneBlock.defaultBlockState();
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState cutCopper = Blocks.CUT_COPPER.defaultBlockState();
        BlockState updraftGrate = nonWaterlogged(updraftGrateBlock.defaultBlockState());
        BlockState springStone = springStoneBlock.defaultBlockState();

        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 10.5D) {
                    continue;
                }

                // Clear sky headroom above the iceberg crown (dy = 1..22)
                for (int dy = 1; dy <= 22; dy++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }

                // Tapered Blue-Ice & Packed-Ice Keel (dy = -9..-1)
                for (int dy = -9; dy <= -1; dy++) {
                    double keelFrac = (dy + 9.0D) / 8.0D; // 0.0 at dy = -9 -> 1.0 at dy = -1
                    double flute = 0.90D + 0.12D * Math.sin(Math.atan2(dz, dx) * 4.0D + dy * 0.4D);
                    double maxKeelR = (3.4D + 7.0D * Math.pow(keelFrac, 0.65D)) * flute;
                    if (r <= maxKeelR) {
                        BlockState keelState = (r <= maxKeelR * 0.70D || dy >= -2) ? blueIce : packedIce;
                        setRel(level, chunkBox, dx, dy, dz, keelState);
                        if (dy == -9 && r <= 4.5D) {
                            fillKeelDown(level, chunkBox, dx, -10, dz, blueIce);
                        }
                    }
                }

                // Crown Surface (dy = 0)
                if (dx == 0 && dz == 0) {
                    setRel(level, chunkBox, 0, -1, 0, springStone);
                    setRel(level, chunkBox, 0, 0, 0, updraftGrate);
                } else if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    setRel(level, chunkBox, dx, 0, dz, cutCopper);
                } else if (r <= 6.6D) {
                    BlockState floorState = (((dx + dz) & 1) == 0) ? deepslateBricks : fulguriteBlock;
                    setRel(level, chunkBox, dx, 0, dz, floorState);
                } else {
                    boolean northApproach = (dz <= -7 && dz >= -9 && Math.abs(dx) <= 1);
                    boolean westApproach = (dx <= -7 && dx >= -9 && Math.abs(dz) <= 1);
                    if (northApproach || westApproach) {
                        setRel(level, chunkBox, dx, 0, dz, (((dx + dz) & 1) == 0) ? cutCopper : deepslateBricks);
                    } else {
                        int hash = Math.abs(dx * 31 + dz * 17);
                        BlockState rimState;
                        if (hash % 7 == 0) {
                            rimState = fulguriteIce;
                        } else if (hash % 4 == 0) {
                            rimState = zephyrIce;
                        } else if (r > 9.4D && hash % 3 == 0) {
                            rimState = blueIce;
                        } else {
                            rimState = snowBlock;
                        }
                        setRel(level, chunkBox, dx, 0, dz, rimState);

                        // Sprinkle Zephyr-Glass Grass tufts strictly on Snow Block or Zephyr Ice ledges
                        if (zephyrGrassOpt.isPresent()
                                && (rimState.is(Blocks.SNOW_BLOCK) || rimState.is(zephyrIceBlock))
                                && r >= 7.6D && r <= 9.8D
                                && hash % 5 == 0) {
                            BlockState grassState = nonWaterlogged(zephyrGrassOpt.get().defaultBlockState());
                            setRel(level, chunkBox, dx, 1, dz, grassState);
                        }
                    }
                }
            }
        }
    }

    private void buildGroundFloorEngineAndSteamRoom(WorldGenLevel level, BoundingBox chunkBox) {
        Block fulguriteStoneBlock = resolveBlock("fulgurite_block", Blocks.DEEPSLATE);
        Block copperPipeBlock = resolveBlock("vapor_pneumatic_pipe_copper", Blocks.LIGHTNING_ROD);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState fulguriteBlock = fulguriteStoneBlock.defaultBlockState();
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState cutCopper = Blocks.CUT_COPPER.defaultBlockState();
        BlockState copperBlock = Blocks.COPPER_BLOCK.defaultBlockState();
        BlockState tintedGlass = Blocks.TINTED_GLASS.defaultBlockState();
        BlockState verticalRod = verticalLightningRod();
        BlockState verticalPipe = makeVerticalCopperPipe(copperPipeBlock);

        // 1. Cylindrical Engine Room Wall (5.4 <= r <= 6.6, dy = 1..5)
        for (int dy = 1; dy <= 5; dy++) {
            for (int dx = -7; dx <= 7; dx++) {
                for (int dz = -7; dz <= 7; dz++) {
                    double r = Math.hypot(dx, dz);
                    if (r > 6.6D) {
                        continue;
                    }
                    if (r < 5.4D) {
                        setRel(level, chunkBox, dx, dy, dz, air);
                        continue;
                    }

                    // Arched North (dz < 0) and West (dx < 0) doorways
                    boolean inNorthDoor = (dz < -4 && Math.abs(dx) <= 1 && dy <= 3);
                    boolean inWestDoor = (dx < -4 && Math.abs(dz) <= 1 && dy <= 3);
                    if (inNorthDoor || inWestDoor) {
                        if (dy == 3 && inNorthDoor && Math.abs(dx) == 1 && dz == -6) {
                            Direction archFace = (dx < 0) ? Direction.EAST : Direction.WEST;
                            setRel(level, chunkBox, dx, 3, dz, deepslateBrickStair(archFace, Half.TOP));
                        } else if (dy == 3 && inWestDoor && Math.abs(dz) == 1 && dx == -6) {
                            Direction archFace = (dz < 0) ? Direction.SOUTH : Direction.NORTH;
                            setRel(level, chunkBox, dx, 3, dz, deepslateBrickStair(archFace, Half.TOP));
                        } else {
                            setRel(level, chunkBox, dx, dy, dz, air);
                        }
                        continue;
                    }

                    // East & South storm-glass portholes (dy = 2..3)
                    boolean eastPorthole = (dx >= 5 && Math.abs(dz) <= 1 && (dy == 2 || dy == 3));
                    boolean southPorthole = (dz >= 5 && Math.abs(dx) <= 1 && (dy == 2 || dy == 3));
                    if (eastPorthole || southPorthole) {
                        setRel(level, chunkBox, dx, dy, dz, tintedGlass);
                        continue;
                    }

                    BlockState wallState;
                    if (dy == 1) {
                        wallState = fulguriteBlock;
                    } else if (dy == 3) {
                        wallState = (((dx + dz) & 1) == 0) ? cutCopper : deepslateBricks;
                    } else if (dy == 5) {
                        wallState = (r >= 6.0D) ? cutCopper : deepslateBricks;
                    } else {
                        wallState = (((dx * 3 + dz + dy) & 3) == 0) ? fulguriteBlock : deepslateBricks;
                    }
                    setRel(level, chunkBox, dx, dy, dz, wallState);
                }
            }
        }

        // 2. 4 Steam & Galvanic Charge Conduits at (+-3, 1..5, +-3)
        for (int[] offset : ENGINE_CONDUIT_OFFSETS) {
            int cx = offset[0];
            int cz = offset[1];
            setRel(level, chunkBox, cx, 1, cz, copperBlock);
            setRel(level, chunkBox, cx, 2, cz, verticalPipe);
            setRel(level, chunkBox, cx, 3, cz, verticalPipe);
            setRel(level, chunkBox, cx, 4, cz, cutCopper);
            setRel(level, chunkBox, cx, 5, cz, verticalRod);
        }

        // Ensure central 3x3 updraft shaft (-1..1, 1..5, -1..1) stays completely clear
        for (int dy = 1; dy <= 5; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }
            }
        }
    }

    private void buildMainDeckChartGalleryAndTurbines(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block silverPinePlanksBlock = resolveBlock("silver_pine_planks", Blocks.SPRUCE_PLANKS);
        Block silverPineLogBlock = resolveBlock("silver_pine_log", Blocks.SPRUCE_LOG);
        Block springStoneBlock = resolveBlock("steaming_spring_stone", Blocks.MAGMA_BLOCK);
        Block auroralLilyPadBlock = resolveBlock("auroral_lily_pad", Blocks.LILY_PAD);
        Block windTurbineBlock = resolveBlock("simurgh_wind_turbine", Blocks.LIGHTNING_ROD);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState silverPinePlanks = silverPinePlanksBlock.defaultBlockState();
        BlockState silverPineLogY = logWithAxis(silverPineLogBlock, Direction.Axis.Y);
        BlockState cutCopper = Blocks.CUT_COPPER.defaultBlockState();
        BlockState copperBlock = Blocks.COPPER_BLOCK.defaultBlockState();
        BlockState tintedGlass = Blocks.TINTED_GLASS.defaultBlockState();
        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState lilyPad = nonWaterlogged(auroralLilyPadBlock.defaultBlockState());
        BlockState hangingLantern = Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false);

        // 1. Main Deck Floor at dy = 6 (r <= 7.5, keeping 3x3 central air-lift well open!)
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 7.5D) {
                    continue;
                }
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    setRel(level, chunkBox, dx, 6, dz, air);
                } else if (Math.max(Math.abs(dx), Math.abs(dz)) == 2) {
                    setRel(level, chunkBox, dx, 6, dz, cutCopper);
                } else if (r >= 6.8D) {
                    setRel(level, chunkBox, dx, 6, dz, (((dx + dz) & 1) == 0) ? cutCopper : silverPinePlanks);
                } else {
                    setRel(level, chunkBox, dx, 6, dz, silverPinePlanks);
                }

                for (int dy = 7; dy <= 11; dy++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }
            }
        }

        // Hang Engine Room ceiling lanterns underneath the newly laid dy = 6 gallery floor
        setRel(level, chunkBox, 3, 5, 0, hangingLantern);
        setRel(level, chunkBox, 0, 5, 3, hangingLantern);

        // 2. Panoramic Tinted-Glass Storm Windows & Perimeter Framing (6.3 <= r <= 7.4, dy = 7..11)
        for (int dy = 7; dy <= 11; dy++) {
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    double r = Math.hypot(dx, dz);
                    if (r < 6.3D || r > 7.4D) {
                        continue;
                    }
                    if (dy == 7) {
                        setRel(level, chunkBox, dx, dy, dz, silverPinePlanks);
                    } else if (dy <= 10) {
                        setRel(level, chunkBox, dx, dy, dz, tintedGlass);
                    } else {
                        setRel(level, chunkBox, dx, dy, dz, (r >= 6.9D) ? cutCopper : silverPinePlanks);
                    }
                }
            }
        }

        // 3. Silver Pine Log Structural Pillars around the Gallery (dy = 7..11)
        for (int[] pillar : GALLERY_LOG_PILLARS) {
            int px = pillar[0];
            int pz = pillar[1];
            for (int dy = 7; dy <= 11; dy++) {
                setRel(level, chunkBox, px, dy, pz, silverPineLogY);
            }
        }

        // 4. 2 Small Heated Water Side Planters with Steaming Spring Stone below & Auroral Lily-Pad on top
        int[][] planterSpots = {{4, 0}, {0, 4}};
        for (int[] spot : planterSpots) {
            int wx = spot[0];
            int wz = spot[1];
            // Seal all 4 sides around the recessed water pocket at dy = 6
            for (Direction horiz : Direction.Plane.HORIZONTAL) {
                setRel(level, chunkBox, wx + horiz.getStepX(), 6, wz + horiz.getStepZ(), cutCopper);
            }
            setRel(level, chunkBox, wx, 5, wz, springStone);
            setRel(level, chunkBox, wx, 6, wz, water);
            setRel(level, chunkBox, wx, 7, wz, lilyPad);
        }

        // 5. Lectern Chart Stand, Cartography Table & 2 Loot Chests (entropica:chests/structure_6)
        BlockState lectern = Blocks.LECTERN.defaultBlockState()
                .setValue(LecternBlock.FACING, Direction.SOUTH);
        setRel(level, chunkBox, 0, 7, -4, lectern);
        setRel(level, chunkBox, -2, 7, -4, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());

        placeObservatoryChest(level, chunkBox, random, -4, 7, 0, Direction.EAST);
        placeObservatoryChest(level, chunkBox, random, 2, 7, -4, Direction.SOUTH);

        // 6. 4 Simurgh Wind Turbines mounted on Cut-Copper Outrigger Brackets bolted to Gallery Corners
        for (int[] sign : CORNER_SIGNS) {
            int sx = sign[0];
            int sz = sign[1];
            Direction outwardFacing = (sz < 0) ? Direction.NORTH : Direction.SOUTH;

            setRel(level, chunkBox, sx * 6, 7, sz * 6, copperBlock);
            setRel(level, chunkBox, sx * 6, 8, sz * 6, cutCopper);
            setRel(level, chunkBox, sx * 7, 8, sz * 7, cutCopper);
            setRel(level, chunkBox, sx * 7, 9, sz * 7, orientTurbine(windTurbineBlock, outwardFacing));
        }

        // Keep central 3x3 shaft (-1..1, 6..11, -1..1) strictly unobstructed
        for (int dy = 6; dy <= 11; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }
            }
        }
    }

    private void buildObservatoryDomeTelescopeAndMast(WorldGenLevel level, BoundingBox chunkBox) {
        Block silverPinePlanksBlock = resolveBlock("silver_pine_planks", Blocks.SPRUCE_PLANKS);
        Block zephyrIceBlock = resolveBlock("zephyr_ice", Blocks.TINTED_GLASS);
        Block updraftGrateBlock = resolveBlock("zephyr_updraft_grate", Blocks.CUT_COPPER);
        Block auroralLensBlock = resolveBlock("auroral_lens_block", Blocks.TINTED_GLASS);
        Optional<Block> brassTelescopeOpt = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "stationary_brass_telescope"));

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState silverPinePlanks = silverPinePlanksBlock.defaultBlockState();
        BlockState cutCopper = Blocks.CUT_COPPER.defaultBlockState();
        BlockState copperBlock = Blocks.COPPER_BLOCK.defaultBlockState();
        BlockState zephyrIce = nonWaterlogged(zephyrIceBlock.defaultBlockState());
        BlockState updraftGrate = nonWaterlogged(updraftGrateBlock.defaultBlockState());
        BlockState auroralLens = nonWaterlogged(auroralLensBlock.defaultBlockState());
        BlockState tintedGlass = Blocks.TINTED_GLASS.defaultBlockState();
        BlockState verticalRod = verticalLightningRod();
        BlockState hangingLantern = Blocks.LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false);

        // 1. Upper Observatory Deck Floor at dy = 12 (r <= 6.8)
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 6.8D) {
                    continue;
                }
                // Central emergence opening at (0,12,0), (-1,12,0), (0,12,-1) so wind lift carries players straight up
                if ((dx == 0 && dz == 0) || (dx == -1 && dz == 0) || (dx == 0 && dz == -1)) {
                    setRel(level, chunkBox, dx, 12, dz, air);
                } else if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    // Upper zephyr_updraft_grate landing collar adjacent to the shaft
                    setRel(level, chunkBox, dx, 12, dz, updraftGrate);
                } else if (r >= 5.6D) {
                    setRel(level, chunkBox, dx, 12, dz, cutCopper);
                } else {
                    setRel(level, chunkBox, dx, 12, dz, silverPinePlanks);
                }
            }
        }

        // Hang 4 Gallery Lanterns underneath the dy = 12 Upper Observatory Deck
        for (int[] sign : CORNER_SIGNS) {
            setRel(level, chunkBox, sign[0] * 3, 11, sign[1] * 3, hangingLantern);
        }

        // Place Stationary Brass Telescope on Upper Deck aiming out the (-X, +Z) slit
        if (brassTelescopeOpt.isPresent()) {
            BlockState telescopeState = nonWaterlogged(brassTelescopeOpt.get().defaultBlockState());
            if (telescopeState.hasProperty(HorizontalDirectionalBlock.FACING)) {
                telescopeState = telescopeState.setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH);
            }
            setRel(level, chunkBox, -2, 13, 1, telescopeState);
        }

        // 2. Hemispherical Dome (dy = 13..17) of Cut-Copper Ribs & Zephyr-Ice Panels with (-X, +Z) Slit
        for (int dy = 13; dy <= 17; dy++) {
            double yTerm = (dy - 12.0D) * (dy - 12.0D) * 1.35D;
            for (int dx = -7; dx <= 7; dx++) {
                for (int dz = -7; dz <= 7; dz++) {
                    double sphereR = Math.sqrt(dx * dx + yTerm + dz * dz);
                    boolean inShell = (dy == 17) ? (sphereR <= 6.5D) : (sphereR >= 5.3D && sphereR <= 6.5D);
                    if (!inShell) {
                        continue;
                    }

                    // Open observation shutter slit on the (-X, +Z) quadrant
                    boolean inShutterSlit = (dx <= -1 && dz >= 1 && Math.abs(dx + dz) <= 1 && dy <= 16);
                    if (inShutterSlit) {
                        setRel(level, chunkBox, dx, dy, dz, air);
                        continue;
                    }

                    boolean isStructuralRib = (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz));
                    setRel(level, chunkBox, dx, dy, dz, isStructuralRib ? cutCopper : zephyrIce);
                }
            }
        }

        // 3. Giant Angled Copper-Block + Cut-Copper Sky-Telescope Barrel (7 steps, dy = 13..19, (-X, +Z))
        setRel(level, chunkBox, -2, 12, 2, copperBlock);
        setRel(level, chunkBox, -2, 13, 2, cutCopper);
        for (int step = 0; step < 7; step++) {
            int tx = -1 - step; // -1 .. -7
            int ty = 13 + step; // 13 .. 19
            int tz = 1 + step;  //  1 ..  7

            if (step < 5) {
                BlockState tubeBlock = ((step & 1) == 0) ? copperBlock : cutCopper;
                setRel(level, chunkBox, tx, ty, tz, tubeBlock);
                if (step >= 1) {
                    // Reinforcing underside tube cradle so diagonal steps connect seamlessly
                    setRel(level, chunkBox, tx, ty - 1, tz, cutCopper);
                }
            } else if (step == 5) {
                // Inner condenser ring & Auroral Lens Block at (-6, 18, 6)
                setRel(level, chunkBox, tx, ty - 1, tz, cutCopper);
                setRel(level, chunkBox, tx - 1, ty, tz, cutCopper);
                setRel(level, chunkBox, tx, ty, tz + 1, cutCopper);
                setRel(level, chunkBox, tx, ty, tz, auroralLens);
            } else {
                // Outer objective hood & Tinted-Glass / Auroral Lens cap at (-7, 19, 7)
                setRel(level, chunkBox, tx, ty - 1, tz, cutCopper);
                setRel(level, chunkBox, tx - 1, ty, tz, cutCopper);
                setRel(level, chunkBox, tx, ty, tz + 1, cutCopper);
                setRel(level, chunkBox, tx, ty, tz, tintedGlass);
            }
        }

        // 4. Pure Copper Storm Collector Mast on Dome Apex at (0, 17..22, 0) — ZERO Shrine Cores or Anchors!
        setRel(level, chunkBox, 0, 17, 0, copperBlock);
        setRel(level, chunkBox, 0, 18, 0, cutCopper);
        setRel(level, chunkBox, 0, 19, 0, cutCopper);
        setRel(level, chunkBox, 0, 20, 0, verticalRod);
        setRel(level, chunkBox, 0, 21, 0, verticalRod);
        setRel(level, chunkBox, 0, 22, 0, verticalRod);
    }

    private void placeObservatoryChest(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random,
            int dx,
            int dy,
            int dz,
            Direction facing
    ) {
        BlockPos pos = toWorld(dx, dy, dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState chestState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, facing)
                .setValue(ChestBlock.WATERLOGGED, false);
        level.setBlock(pos, chestState, 2);
        RandomizableContainer.setBlockEntityLootTable(
                level, random, pos, ModStructures.STRUCTURE_6_CHEST_LOOT);
    }

    private static BlockState verticalLightningRod() {
        return Blocks.LIGHTNING_ROD.defaultBlockState()
                .setValue(LightningRodBlock.FACING, Direction.UP)
                .setValue(LightningRodBlock.WATERLOGGED, false);
    }

    private static BlockState makeVerticalCopperPipe(Block pipeBlock) {
        BlockState state = nonWaterlogged(pipeBlock.defaultBlockState());
        if (state.hasProperty(BlockStateProperties.UP) && state.hasProperty(BlockStateProperties.DOWN)) {
            return state.setValue(BlockStateProperties.UP, true)
                    .setValue(BlockStateProperties.DOWN, true);
        }
        if (state.hasProperty(BlockStateProperties.FACING)
                && BlockStateProperties.FACING.getPossibleValues().contains(Direction.UP)) {
            return state.setValue(BlockStateProperties.FACING, Direction.UP);
        }
        return state;
    }

    private static BlockState orientTurbine(Block turbineBlock, Direction outwardHoriz) {
        BlockState state = nonWaterlogged(turbineBlock.defaultBlockState());
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, outwardHoriz);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            if (BlockStateProperties.FACING.getPossibleValues().contains(outwardHoriz)) {
                return state.setValue(BlockStateProperties.FACING, outwardHoriz);
            }
            if (BlockStateProperties.FACING.getPossibleValues().contains(Direction.UP)) {
                return state.setValue(BlockStateProperties.FACING, Direction.UP);
            }
        }
        return state;
    }

    private static BlockState deepslateBrickStair(Direction facing, Half half) {
        return Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half)
                .setValue(StairBlock.WATERLOGGED, false);
    }

    private static BlockState logWithAxis(Block logBlock, Direction.Axis axis) {
        BlockState state = logBlock.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            return state.setValue(RotatedPillarBlock.AXIS, axis);
        }
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            return state.setValue(BlockStateProperties.AXIS, axis);
        }
        return state;
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }

    private static Block resolveBlock(String path, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private BlockPos toWorld(int dx, int dy, int dz) {
        return new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
    }

    private void setRel(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, BlockState state) {
        BlockPos pos = toWorld(dx, dy, dz);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void fillKeelDown(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int startDy,
            int dz,
            BlockState state
    ) {
        int wx = this.originX + dx;
        int wz = this.originZ + dz;
        if (wx < chunkBox.minX() || wx > chunkBox.maxX() || wz < chunkBox.minZ() || wz > chunkBox.maxZ()) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minAllowedY = Math.max(level.getMinY() + 2, this.baseY + startDy - 16);
        for (int wy = this.baseY + startDy; wy >= minAllowedY; wy--) {
            cursor.set(wx, wy, wz);
            BlockState existing = level.getBlockState(cursor);
            if (existing.isAir()
                    || existing.is(Blocks.WATER)
                    || existing.is(Blocks.BUBBLE_COLUMN)
                    || existing.is(Blocks.SNOW)
                    || existing.is(Blocks.POWDER_SNOW)
                    || existing.is(Blocks.ICE)
                    || existing.canBeReplaced()) {
                level.setBlock(cursor, state, 2);
            } else {
                break;
            }
        }
    }
}
