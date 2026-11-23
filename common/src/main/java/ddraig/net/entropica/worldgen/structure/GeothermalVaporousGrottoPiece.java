package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModStructures;
import ddraig.net.entropica.worldgen.feature.VaporWeaverLichenPatchFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.material.Fluids;

/**
 * Programmatic generator for Structure 4: Geothermal Vaporous Grotto ({@code 20 x 20 x 18}):
 * <ul>
 *   <li>Domed Deepslate Brick, Chiseled Deepslate, and Steaming Spring Stone cavern capped overhead
 *       with a translucent Blue Ice & Packed Ice pressurized greenhouse skylight dome ({@code dy = 10..14}).</li>
 *   <li>Draped {@code entropica:vapor_weaver_lichen} on inner columns/walls and {@code entropica:mist_veil_fern}
 *       beside warm basalt/spring-stone vents.</li>
 *   <li>Two parallel shallow thermal water bath pools ({@code steaming_spring_stone} + {@code steam_vent} beds)
 *       divided down the center by an {@code entropica:amber_planks} & {@code entropica:amber_fence} bridge.</li>
 *   <li>3-Copper-Plate Steam Puzzle ({@code I, II, III}): oxidized copper center plate leads safely to the
 *       {@code entropica:chests/structure_4} chest, while trapped decoy plates trigger hidden wall dispensers
 *       stocked with Splash Potions of Blindness and Fire Charges beside floor {@code steam_vent} hazards.</li>
 *   <li>Rear active {@code entropica:steam_vent} beneath a 12-block chimney updraft shaft rising to
 *       an upper {@code amber_planks} balcony observation & secondary loot nook.</li>
 * </ul>
 */
public class GeothermalVaporousGrottoPiece extends StructurePiece {

    private static final int[][] BATH_VENT_OFFSETS = {
            {-4, 0}, {-5, 3}, {-3, 4},
            {4, 0}, {5, 3}, {3, 4}
    };

    private static final int[][] INNER_RIB_COLUMNS = {
            {-6, 3}, {6, 3}, {-6, -3}, {6, -3}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;

    public GeothermalVaporousGrottoPiece(int originX, int baseY, int originZ) {
        // Exact 20 (W: -10..+9) x 18 (H: -3..+14) x 20 (L: -10..+9) bounding box
        super(ModStructures.GEOTHERMAL_VAPOROUS_GROTTO_PIECE.get(), 0,
                new BoundingBox(originX - 10, baseY - 3, originZ - 10, originX + 9, baseY + 14, originZ + 9));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public GeothermalVaporousGrottoPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.GEOTHERMAL_VAPOROUS_GROTTO_PIECE.get(), tag);
        this.originX = tag.getIntOr("OriginX", (this.boundingBox.minX() + this.boundingBox.maxX() + 1) / 2);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 3);
        this.originZ = tag.getIntOr("OriginZ", (this.boundingBox.minZ() + this.boundingBox.maxZ() + 1) / 2);
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

    public BlockPos getGeyserAnchorPos() {
        return new BlockPos(this.originX, this.baseY, this.originZ - 7);
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
        buildCavernDomeAndSkylight(level, chunkBox);
        buildParallelThermalBathsAndAmberBridge(level, chunkBox);
        buildCopperPlateSteamPuzzleAndTreasure(level, chunkBox, random);
        buildRearGeyserChimneyAndUpperBalcony(level, chunkBox, random);
        decorateInnerColumnsLichenAndFerns(level, chunkBox);
    }

    private void buildCavernDomeAndSkylight(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState crackedBricks = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState basaltY = Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState springStone = resolveBlock("steaming_spring_stone", Blocks.BASALT).defaultBlockState();
        BlockState steamingIce = resolveBlock("steaming_ice", Blocks.BLUE_ICE).defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Sub-floor watertight foundation & outer craggy cavern shell + overhead Blue-Ice skylight dome
        for (int dx = -10; dx <= 9; dx++) {
            for (int dz = -10; dz <= 9; dz++) {
                // Center dome symmetrically around (0, 0) while seating outer buttress rock out to -10
                double hDist = Math.hypot(dx, dz);
                if (hDist > 9.95D) {
                    continue;
                }

                // Foundation slab (dy = -3..-1) and downward terrain anchoring
                BlockState subState = (hDist > 8.6D) ? basaltY
                        : ((((dx + dz) & 3) == 0) ? springStone : deepslateBricks);
                for (int dy = -3; dy <= 0; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, subState);
                }
                fillFoundationDown(level, chunkBox, dx, -4, dz, (hDist > 8.4D) ? basaltY : deepslateBricks);

                // Clear interior dome & build spherical cavern vault + Blue-Ice greenhouse skylight (dy = 1..14)
                for (int dy = 1; dy <= 14; dy++) {
                    double normY = Math.max(0.0D, (dy - 2.0D) / 11.8D);
                    double ellipsoidalR = Math.hypot(hDist / 9.45D, normY);

                    if (ellipsoidalR > 1.06D) {
                        continue;
                    }

                    if (ellipsoidalR >= 0.89D) {
                        // Cavern shell or overhead pressurized greenhouse skylight dome
                        boolean onStructuralRib = (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz));
                        if (dy >= 10 && hDist <= 6.6D) {
                            // Overhead translucent Blue Ice & Packed Ice greenhouse skylight dome
                            if (onStructuralRib && dy <= 11) {
                                setBlock(level, chunkBox, dx, dy, dz, chiseledDeepslate);
                            } else {
                                int pattern = (dx * 3 + dz * 5 + dy) & 7;
                                BlockState iceCap = (pattern == 0) ? steamingIce
                                        : ((pattern <= 4) ? blueIce : packedIce);
                                setBlock(level, chunkBox, dx, dy, dz, iceCap);
                            }
                        } else {
                            // Dark Deepslate Bricks, Chiseled Deepslate & Steaming Spring Stone vault walls
                            BlockState wallState;
                            if (onStructuralRib && (dy == 4 || dy == 8)) {
                                wallState = chiseledDeepslate;
                            } else if (dy <= 2 && (((dx * 5 + dz * 3) & 3) == 0)) {
                                wallState = springStone;
                            } else if (((dx + dy + dz) & 5) == 0) {
                                wallState = crackedBricks;
                            } else {
                                wallState = deepslateBricks;
                            }
                            setBlock(level, chunkBox, dx, dy, dz, wallState);
                        }
                    } else {
                        // Hollow interior cavern headroom
                        setBlock(level, chunkBox, dx, dy, dz, air);
                    }
                }
            }
        }

        // 2. Carve Grand South Entrance Archway (dz = +7..+9, dx = -2..+2, dy = 0..+4)
        for (int dz = 7; dz <= 9; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                setBlock(level, chunkBox, dx, 0, dz, (Math.abs(dx) == 2) ? chiseledDeepslate : deepslateBricks);
                if (Math.abs(dx) == 2) {
                    for (int dy = 1; dy <= 4; dy++) {
                        setBlock(level, chunkBox, dx, dy, dz, (dy == 2 || dy == 4) ? chiseledDeepslate : deepslateBricks);
                    }
                } else {
                    int archTop = (dx == 0) ? 4 : 3;
                    for (int dy = 1; dy <= archTop; dy++) {
                        setBlock(level, chunkBox, dx, dy, dz, air);
                    }
                    setBlock(level, chunkBox, dx, archTop + 1, dz,
                            (dx == 0) ? chiseledDeepslate : Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                                    .setValue(StairBlock.FACING, dx < 0 ? Direction.WEST : Direction.EAST)
                                    .setValue(StairBlock.HALF, Half.TOP));
                }
            }
        }
        // Welcoming exterior approach stairs at dz = +9
        for (int dx = -1; dx <= 1; dx++) {
            setBlock(level, chunkBox, dx, 0, 9, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH)
                    .setValue(StairBlock.HALF, Half.BOTTOM));
        }
    }

    private void buildParallelThermalBathsAndAmberBridge(WorldGenLevel level, BoundingBox chunkBox) {
        Block springStoneBlock = resolveBlock("steaming_spring_stone", Blocks.BASALT);
        Block steamingIceBlock = resolveBlock("steaming_ice", Blocks.BLUE_ICE);
        Block steamVentBlock = resolveBlock("steam_vent", Blocks.MAGMA_BLOCK);
        Block amberPlanksBlock = resolveBlock("amber_planks", Blocks.SPRUCE_PLANKS);
        Block amberFenceBlock = resolveBlock("amber_fence", Blocks.SPRUCE_FENCE);
        Block amberSlabBlock = resolveBlock("amber_slab", Blocks.SPRUCE_SLAB);

        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState steamingIce = steamingIceBlock.defaultBlockState();
        BlockState steamVent = steamVentBlock.defaultBlockState();
        BlockState amberPlanks = amberPlanksBlock.defaultBlockState();
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState basaltY = Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Carve West Bath (dx = -6..-2, dz = -1..+5) & East Bath (dx = +2..+6, dz = -1..+5)
        for (int side : new int[]{-1, 1}) {
            for (int ax = 2; ax <= 6; ax++) {
                int dx = side * ax;
                for (int dz = -1; dz <= 5; dz++) {
                    if (Math.hypot(dx, dz) > 7.6D) {
                        continue;
                    }
                    boolean outerBank = (ax == 6 || dz == 5 || dz == -1);
                    if (outerBank && ((ax == 6 && (dz == -1 || dz == 5)))) {
                        // Rounded pool corners of steaming_ice & basalt
                        setBlock(level, chunkBox, dx, -2, dz, deepslateBricks);
                        setBlock(level, chunkBox, dx, -1, dz, springStone);
                        setBlock(level, chunkBox, dx, 0, dz, ((dx + dz) & 1) == 0 ? steamingIce : basaltY);
                    } else {
                        // Shallow warm bath pool: steaming_spring_stone bed at dy = -2, clear water at dy = -1, open air at dy = 0
                        setBlock(level, chunkBox, dx, -3, dz, deepslateBricks);
                        setBlock(level, chunkBox, dx, -2, dz, springStone);
                        setBlock(level, chunkBox, dx, -1, dz, water);
                        setBlock(level, chunkBox, dx, 0, dz, air);
                    }
                }
            }

            // Warm perimeter shore shelf along dx = +/-7 (dz = -1..+4) for Mist-Veil Ferns & steaming fissures
            int bankX = side * 7;
            for (int dz = -1; dz <= 4; dz++) {
                if (Math.hypot(bankX, dz) <= 8.0D) {
                    setBlock(level, chunkBox, bankX, -1, dz, springStone);
                    BlockState bankTop = ((dz & 1) == 0) ? basaltY : steamingIce;
                    setBlock(level, chunkBox, bankX, 0, dz, bankTop);
                    setBlock(level, chunkBox, bankX, 1, dz, air);
                }
            }
        }

        // Embed bubbling steam_vent blocks into the floor of both parallel thermal pools
        for (int[] v : BATH_VENT_OFFSETS) {
            BlockPos ventPos = new BlockPos(this.originX + v[0], this.baseY - 2, this.originZ + v[1]);
            if (chunkBox.isInside(ventPos)) {
                level.setBlock(ventPos, steamVent, 2);
                level.scheduleTick(ventPos, steamVentBlock, 2);
                level.scheduleTick(ventPos.above(), Fluids.WATER, 2);
            }
        }

        // 2. Central Amber-Wood Bridge Walkway (dx = -1..+1, dz = -1..+6) dividing the two steaming baths
        for (int dz = -1; dz <= 6; dz++) {
            boolean hasBridgePier = (dz == -1 || dz == 2 || dz == 6);
            for (int dx = -1; dx <= 1; dx++) {
                setBlock(level, chunkBox, dx, -3, dz, deepslateBricks);
                if (hasBridgePier) {
                    setBlock(level, chunkBox, dx, -2, dz, deepslateBricks);
                    setBlock(level, chunkBox, dx, -1, dz, (dx == 0) ? chiseledDeepslate : deepslateBricks);
                } else {
                    // Open warm water arch beneath the bridge connecting the twin thermal springs
                    setBlock(level, chunkBox, dx, -2, dz, springStone);
                    setBlock(level, chunkBox, dx, -1, dz, water);
                }

                // Bridge deck at dy = 0
                BlockState deckState = amberPlanks;
                if (dx != 0 && dz >= 0 && dz <= 5 && amberSlabBlock.defaultBlockState().hasProperty(SlabBlock.TYPE)) {
                    deckState = amberSlabBlock.defaultBlockState()
                            .setValue(SlabBlock.TYPE, SlabType.TOP)
                            .setValue(SlabBlock.WATERLOGGED, false);
                }
                setBlock(level, chunkBox, dx, 0, dz, deckState);
                setBlock(level, chunkBox, dx, 1, dz, air);
            }
        }

        // Amber Fence railings along the bridge edges (dx = -1 and +1, dz = 0..5, leaving dz = -1 & +6 open to baths)
        for (int side : new int[]{-1, 1}) {
            for (int dz = 0; dz <= 5; dz++) {
                boolean connectNorth = (dz > 0);
                boolean connectSouth = (dz < 5);
                BlockState fenceState = connectFence(amberFenceBlock.defaultBlockState(), connectNorth, connectSouth, false, false);
                setBlock(level, chunkBox, side, 1, dz, fenceState);
            }
            // Corner lantern/newel caps on bridge ends (dz = 0 and dz = 5)
            setBlock(level, chunkBox, side, 2, 5, Blocks.LANTERN.defaultBlockState());

            // Submerged waterlogged steps at dz = +5 (dx = +/-2) so bathers step smoothly into & out of the pools
            setBlock(level, chunkBox, side * 2, -1, 5, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, side < 0 ? Direction.EAST : Direction.WEST)
                    .setValue(StairBlock.HALF, Half.BOTTOM)
                    .setValue(StairBlock.WATERLOGGED, true));
        }
    }

    private void buildCopperPlateSteamPuzzleAndTreasure(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block steamVentBlock = resolveBlock("steam_vent", Blocks.MAGMA_BLOCK);
        Block amberFenceBlock = resolveBlock("amber_fence", Blocks.SPRUCE_FENCE);
        Block vaporLiftMatBlock = resolveBlock("vapor_lift_mat", Blocks.MOSS_CARPET);

        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState springStone = resolveBlock("steaming_spring_stone", Blocks.BASALT).defaultBlockState();
        BlockState steamVent = steamVentBlock.defaultBlockState();
        BlockState vaporLiftMat = vaporLiftMatBlock.defaultBlockState();
        BlockState waxedCopper = Blocks.WAXED_COPPER_BLOCK.defaultBlockState();
        BlockState waxedExposedCopper = Blocks.WAXED_EXPOSED_COPPER.defaultBlockState();
        BlockState waxedOxidizedCopper = Blocks.WAXED_OXIDIZED_COPPER.defaultBlockState();
        BlockState goldCopperPlate = Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Solid Puzzle Plaza & Treasure Dais floor (dx = -4..+4, dz = -6..-2, dy = 0)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -6; dz <= -2; dz++) {
                setBlock(level, chunkBox, dx, -2, dz, deepslateBricks);
                setBlock(level, chunkBox, dx, -1, dz, deepslateBricks);
                BlockState floorTile = ((Math.abs(dx) + Math.abs(dz)) % 3 == 0) ? springStone : deepslateBricks;
                setBlock(level, chunkBox, dx, 0, dz, floorTile);
                for (int dy = 1; dy <= 4; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, air);
                }
            }
        }

        // 2. Three-Lane Copper-Plate Threshold Gate at dz = -3:
        //    Dividing mullions at dx = -1 and dx = +1 force players to choose Lane I (-2), Lane II (0), or Lane III (+2)
        for (int mullionX : new int[]{-1, 1}) {
            setBlock(level, chunkBox, mullionX, 1, -3, chiseledDeepslate);
            setBlock(level, chunkBox, mullionX, 2, -3, connectFence(amberFenceBlock.defaultBlockState(), false, false, false, false));
            setBlock(level, chunkBox, mullionX, 3, -3, chiseledDeepslate);
        }
        // Outer gate piers at dx = -3 and dx = +3 (housing the hidden Steam Trap Dispensers at dy = 1!)
        for (int outerX : new int[]{-3, 3}) {
            setBlock(level, chunkBox, outerX, 2, -3, chiseledDeepslate);
            setBlock(level, chunkBox, outerX, 3, -3, deepslateBricks);
            // Backing blocks around dispenser so it looks seamlessly built into the deepslate bulkhead
            int backX = outerX + Integer.signum(outerX);
            setBlock(level, chunkBox, backX, 1, -3, deepslateBricks);
            setBlock(level, chunkBox, backX, 2, -3, deepslateBricks);
            setBlock(level, chunkBox, outerX, 1, -4, deepslateBricks);
            setBlock(level, chunkBox, outerX, 2, -4, deepslateBricks);
        }

        // Overhead lintel across dz = -3 (dy = 4) with Copper Gauge Markers I, II, III at dy = 3:
        //   Lane I (-2): Waxed Copper (superheated/unvented -> TRAP)
        //   Lane II (0): Waxed Oxidized Copper (cooled patina -> SAFE, matches Treasure Pedestal!)
        //   Lane III (+2): Waxed Exposed Copper (pressurized -> TRAP)
        for (int dx = -3; dx <= 3; dx++) {
            setBlock(level, chunkBox, dx, 4, -3, (dx == 0) ? waxedOxidizedCopper : deepslateBricks);
        }
        setBlock(level, chunkBox, -2, 3, -3, waxedCopper);
        setBlock(level, chunkBox, 0, 3, -3, waxedOxidizedCopper);
        setBlock(level, chunkBox, 2, 3, -3, waxedExposedCopper);

        // Lane I (-2, -3): Trapped Decoy Plate + floor steam_vent hazard + Hidden East-facing Dispenser
        setBlock(level, chunkBox, -2, 0, -3, waxedCopper);
        setBlock(level, chunkBox, -2, 1, -3, goldCopperPlate);
        setBlock(level, chunkBox, -2, 0, -2, steamVent);
        scheduleBlockTick(level, chunkBox, -2, 0, -2, steamVentBlock);
        placeAndStockSteamTrapDispenser(level, chunkBox, -3, 1, -3, Direction.EAST);

        // Lane II (0, -3): Safe Oxidized Copper Center Plate + Vapor Lift Mat runner to the Treasure Chest!
        setBlock(level, chunkBox, 0, 0, -3, waxedOxidizedCopper);
        setBlock(level, chunkBox, 0, 1, -3, goldCopperPlate);
        setBlock(level, chunkBox, 0, 0, -4, chiseledDeepslate);
        if (vaporLiftMat.canSurvive(level, new BlockPos(this.originX, this.baseY + 1, this.originZ - 4))) {
            setBlock(level, chunkBox, 0, 1, -4, vaporLiftMat);
        }

        // Lane III (+2, -3): Trapped Decoy Plate + floor steam_vent hazard + Hidden West-facing Dispenser
        setBlock(level, chunkBox, 2, 0, -3, waxedExposedCopper);
        setBlock(level, chunkBox, 2, 1, -3, goldCopperPlate);
        setBlock(level, chunkBox, 2, 0, -2, steamVent);
        scheduleBlockTick(level, chunkBox, 2, 0, -2, steamVentBlock);
        placeAndStockSteamTrapDispenser(level, chunkBox, 3, 1, -3, Direction.WEST);

        // 3. Structure 4 Treasure Chest Pedestal at (0, 1, -5) (matching Waxed Oxidized Copper safe emblem!)
        setBlock(level, chunkBox, 0, 0, -5, waxedOxidizedCopper);
        placeStructure4Chest(level, chunkBox, random, 0, 1, -5, Direction.SOUTH);
        setBlock(level, chunkBox, -1, 0, -5, chiseledDeepslate);
        setBlock(level, chunkBox, 1, 0, -5, chiseledDeepslate);
    }

    private void buildRearGeyserChimneyAndUpperBalcony(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block geyserVentBlock = resolveBlock("steam_vent", Blocks.MAGMA_BLOCK);
        Block steamVentBlock = resolveBlock("steam_vent", Blocks.MAGMA_BLOCK);
        Block springStoneBlock = resolveBlock("steaming_spring_stone", Blocks.BASALT);
        Block amberPlanksBlock = resolveBlock("amber_planks", Blocks.SPRUCE_PLANKS);
        Block amberFenceBlock = resolveBlock("amber_fence", Blocks.SPRUCE_FENCE);
        Block amberStairsBlock = resolveBlock("amber_stairs", Blocks.SPRUCE_STAIRS);
        Block vaporLiftMatBlock = resolveBlock("vapor_lift_mat", Blocks.MOSS_CARPET);
        Block vaporCondenserBlock = resolveBlock("vapor_condenser", Blocks.CAULDRON);
        Block pottedFernBlock = resolveBlock("potted_mist_veil_fern", Blocks.POTTED_FERN);
        Block amberBarrelBlock = resolveBlock("amber_barrel", Blocks.BARREL);

        BlockState activeVent = geyserVentBlock.defaultBlockState();
        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState amberPlanks = amberPlanksBlock.defaultBlockState();
        BlockState vaporLiftMat = vaporLiftMatBlock.defaultBlockState();
        BlockState vaporCondenser = vaporCondenserBlock.defaultBlockState();
        BlockState pottedFern = pottedFernBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Rear Geyser Shrine Floor (dx = -2..+2, dz = -8..-6, dy = 0) & Steam Vent at (0, 0, -7)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -8; dz <= -6; dz++) {
                setBlock(level, chunkBox, dx, -2, dz, deepslateBricks);
                setBlock(level, chunkBox, dx, -1, dz, springStone);
                BlockState collar = (Math.abs(dx) <= 1 && Math.abs(dz + 7) <= 1) ? springStone : chiseledDeepslate;
                setBlock(level, chunkBox, dx, 0, dz, collar);
                for (int dy = 1; dy <= 4; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, air);
                }
            }
        }

        // Steam Vent at (0, 0, -7) + warm steam_vent accents at (-2, 0, -7) & (+2, 0, -7)
        setBlock(level, chunkBox, 0, 0, -7, activeVent);
        scheduleBlockTick(level, chunkBox, 0, 0, -7, geyserVentBlock);
        setBlock(level, chunkBox, -2, 0, -7, steamVentBlock.defaultBlockState());
        scheduleBlockTick(level, chunkBox, -2, 0, -7, steamVentBlock);
        setBlock(level, chunkBox, 2, 0, -7, steamVentBlock.defaultBlockState());
        scheduleBlockTick(level, chunkBox, 2, 0, -7, steamVentBlock);

        // 2. Unobstructed 12-Block Vertical Geothermal Chimney Shaft above (0, 0, -7) (dy = 1..12)
        for (int dy = 1; dy <= 12; dy++) {
            // Keep the 1x1 updraft core column (0, dy, -7) 100% clear air!
            setBlock(level, chunkBox, 0, dy, -7, air);
            // Also keep the south exit slot (0, dy, -6) open from dy = 9..12 so players step straight onto the dy = 8 balcony!
            if (dy >= 9) {
                setBlock(level, chunkBox, 0, dy, -6, air);
            }

            // Frame West, East, and North chimney walls from dy = 5..12
            if (dy >= 5) {
                BlockState flueWall = ((dy & 1) == 0) ? springStone : deepslateBricks;
                setBlock(level, chunkBox, -1, dy, -7, flueWall);
                setBlock(level, chunkBox, 1, dy, -7, flueWall);
                setBlock(level, chunkBox, 0, dy, -8, (dy == 8 || dy == 11) ? chiseledDeepslate : flueWall);
                setBlock(level, chunkBox, -1, dy, -8, deepslateBricks);
                setBlock(level, chunkBox, 1, dy, -8, deepslateBricks);
            }
        }
        // Translucent pressurized Blue-Ice skylight cap sealing the top of the chimney at dy = 13
        setBlock(level, chunkBox, 0, 13, -7, blueIce);

        // 3. Upper Amber-Planks Balcony Ledge & Secondary Loot/Observation Nook at dy = 8 (dz = -6..-4, dx = -3..+3)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -6; dz <= -4; dz++) {
                // Support corbels underneath the balcony front edge
                if (dz == -4 && (Math.abs(dx) == 3 || dx == 0) && amberStairsBlock.defaultBlockState().hasProperty(StairBlock.FACING)) {
                    setBlock(level, chunkBox, dx, 7, -4, amberStairsBlock.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH)
                            .setValue(StairBlock.HALF, Half.TOP)
                            .setValue(StairBlock.WATERLOGGED, false));
                }
                setBlock(level, chunkBox, dx, 8, dz, amberPlanks);
                // Ensure comfortable 3-block standing headroom on the upper balcony (dy = 9..11)
                for (int dy = 9; dy <= 11; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, air);
                }
            }
        }

        // Amber Fence overlook railing along the south edge (dz = -4, dx = -3..+3) and balcony sides (dx = +/-3, dz = -5)
        for (int dx = -3; dx <= 3; dx++) {
            boolean connW = (dx > -3);
            boolean connE = (dx < 3);
            boolean connN = (Math.abs(dx) == 3);
            setBlock(level, chunkBox, dx, 9, -4,
                    connectFence(amberFenceBlock.defaultBlockState(), connN, false, connE, connW));
        }
        setBlock(level, chunkBox, -3, 9, -5,
                connectFence(amberFenceBlock.defaultBlockState(), true, true, false, false));
        setBlock(level, chunkBox, 3, 9, -5,
                connectFence(amberFenceBlock.defaultBlockState(), true, true, false, false));
        setBlock(level, chunkBox, 0, 10, -4, Blocks.LANTERN.defaultBlockState());

        // Cushioned Vapor Lift Mat landing pad right in front of the chimney updraft exit at (0, 9, -5)
        if (vaporLiftMat.canSurvive(level, new BlockPos(this.originX, this.baseY + 9, this.originZ - 5))) {
            setBlock(level, chunkBox, 0, 9, -5, vaporLiftMat);
        }

        // Secondary Loot Barrel at (-2, 9, -6) + Vapor Condenser & Potted Mist-Veil Fern observation nook at (+2, 9, -6)
        placeSecondaryLootBarrel(level, chunkBox, random, amberBarrelBlock, -2, 9, -6);
        setBlock(level, chunkBox, 2, 9, -6, vaporCondenser);
        setBlock(level, chunkBox, 1, 9, -6, pottedFern);
    }

    private void decorateInnerColumnsLichenAndFerns(WorldGenLevel level, BoundingBox chunkBox) {
        Block lichenBlock = resolveBlock("vapor_weaver_lichen", Blocks.GLOW_LICHEN);
        Block mistVeilFernBlock = resolveBlock("mist_veil_fern", Blocks.FERN);
        Block springStoneBlock = resolveBlock("steaming_spring_stone", Blocks.BASALT);

        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState basaltY = Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState mistVeilFern = mistVeilFernBlock.defaultBlockState();

        // 1. Four Inner Cavern Columns supporting the dome vault, draped with Vapor-Weaver Lichen
        for (int[] col : INNER_RIB_COLUMNS) {
            int cx = col[0];
            int cz = col[1];
            Direction towardCenterX = (cx < 0) ? Direction.EAST : Direction.WEST;
            Direction towardCenterZ = (cz < 0) ? Direction.SOUTH : Direction.NORTH;

            for (int dy = -2; dy <= 8; dy++) {
                BlockState colState = (dy == 0 || dy == 4 || dy == 8) ? chiseledDeepslate
                        : ((dy == 1) ? springStone : deepslateBricks);
                setBlock(level, chunkBox, cx, dy, cz, colState);
            }

            // Drape Vapor-Weaver Lichen along the inward vertical faces of each column (dy = 2..6)
            for (int dy = 2; dy <= 6; dy += 2) {
                placeLichenAgainstHost(level, chunkBox, lichenBlock,
                        cx + towardCenterX.getStepX(), dy, cz, towardCenterX.getOpposite());
                placeLichenAgainstHost(level, chunkBox, lichenBlock,
                        cx, dy + 1, cz + towardCenterZ.getStepZ(), towardCenterZ.getOpposite());
            }
        }

        // Additional Vapor-Weaver Lichen clinging inside the rear chimney shaft (dy = 5, 7)
        placeLichenAgainstHost(level, chunkBox, lichenBlock, 0, 5, -7, Direction.NORTH);
        placeLichenAgainstHost(level, chunkBox, lichenBlock, 0, 7, -7, Direction.WEST);

        // 2. Lush Mist-Veil Ferns growing beside the warm steaming vents along the West & East bath banks
        int[][] fernSpots = {
                {-7, 0}, {-7, 2}, {-7, 4},
                {7, 0}, {7, 2}, {7, 4},
                {-4, -2}, {4, -2}
        };
        for (int[] f : fernSpots) {
            int fx = f[0];
            int fz = f[1];
            setBlock(level, chunkBox, fx, 0, fz, basaltY);
            BlockPos fernPos = new BlockPos(this.originX + fx, this.baseY + 1, this.originZ + fz);
            if (chunkBox.isInside(fernPos) && level.getBlockState(fernPos).isAir() && mistVeilFern.canSurvive(level, fernPos)) {
                level.setBlock(fernPos, mistVeilFern, 2);
            }
        }
    }

    private void placeLichenAgainstHost(
            WorldGenLevel level,
            BoundingBox chunkBox,
            Block lichenBlock,
            int dx,
            int dy,
            int dz,
            Direction dirTowardHost
    ) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState existing = level.getBlockState(pos);
        boolean inWater = existing.is(Blocks.WATER);
        if (!existing.isAir() && !inWater) {
            return;
        }
        BlockState lichen = VaporWeaverLichenPatchFeature.blankMultifaceState(lichenBlock, inWater);
        BooleanProperty faceProp = VaporWeaverLichenPatchFeature.facePropertyFor(dirTowardHost);
        if (lichen.hasProperty(faceProp)) {
            lichen = lichen.setValue(faceProp, true);
            if (lichen.canSurvive(level, pos)) {
                level.setBlock(pos, lichen, 2);
            }
        }
    }

    private void placeStructure4Chest(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random,
            int dx,
            int dy,
            int dz,
            Direction facing
    ) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState chestState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, facing)
                .setValue(ChestBlock.WATERLOGGED, false);
        level.setBlock(pos, chestState, 2);
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.STRUCTURE_4_CHEST_LOOT);
    }

    private void placeSecondaryLootBarrel(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random,
            Block barrelBlock,
            int dx,
            int dy,
            int dz
    ) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState barrelState = barrelBlock.defaultBlockState();
        if (barrelState.hasProperty(BarrelBlock.FACING)) {
            barrelState = barrelState.setValue(BarrelBlock.FACING, Direction.UP);
        }
        level.setBlock(pos, barrelState, 2);
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.STRUCTURE_4_CHEST_LOOT);
    }

    private void placeAndStockSteamTrapDispenser(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int dy,
            int dz,
            Direction facing
    ) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState dispenserState = Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, facing);
        level.setBlock(pos, dispenserState, 2);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DispenserBlockEntity dispenser) {
            for (int slot = 0; slot < dispenser.getContainerSize(); slot++) {
                if (slot % 2 == 0) {
                    ItemStack splashBlindness = new ItemStack(Items.SPLASH_POTION);
                    splashBlindness.set(DataComponents.POTION_CONTENTS,
                            PotionContents.EMPTY.withEffectAdded(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0)));
                    dispenser.setItem(slot, splashBlindness);
                } else {
                    dispenser.setItem(slot, new ItemStack(Items.FIRE_CHARGE, 2));
                }
            }
        }
    }

    private void scheduleBlockTick(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, Block block) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (chunkBox.isInside(pos)) {
            level.scheduleTick(pos, block, 2);
        }
    }

    private static BlockState connectFence(BlockState base, boolean north, boolean south, boolean east, boolean west) {
        BlockState s = base;
        if (s.hasProperty(CrossCollisionBlock.NORTH)) {
            s = s.setValue(CrossCollisionBlock.NORTH, north);
        }
        if (s.hasProperty(CrossCollisionBlock.SOUTH)) {
            s = s.setValue(CrossCollisionBlock.SOUTH, south);
        }
        if (s.hasProperty(CrossCollisionBlock.EAST)) {
            s = s.setValue(CrossCollisionBlock.EAST, east);
        }
        if (s.hasProperty(CrossCollisionBlock.WEST)) {
            s = s.setValue(CrossCollisionBlock.WEST, west);
        }
        if (s.hasProperty(CrossCollisionBlock.WATERLOGGED)) {
            s = s.setValue(CrossCollisionBlock.WATERLOGGED, false);
        }
        return s;
    }

    private static BlockState withBooleanProperty(BlockState state, String propName, boolean value) {
        Property<?> prop = state.getBlock().getStateDefinition().getProperty(propName);
        if (prop instanceof BooleanProperty boolProp && state.hasProperty(boolProp)) {
            return state.setValue(boolProp, value);
        }
        return state;
    }

    private static Block resolveBlock(String path, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private void setBlock(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, BlockState state) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void fillFoundationDown(WorldGenLevel level, BoundingBox chunkBox, int dx, int startDy, int dz, BlockState state) {
        int wx = this.originX + dx;
        int wz = this.originZ + dz;
        if (wx < chunkBox.minX() || wx > chunkBox.maxX() || wz < chunkBox.minZ() || wz > chunkBox.maxZ()) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minAllowedY = Math.max(level.getMinY() + 2, this.baseY + startDy - 18);
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
