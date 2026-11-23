package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModPotions;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.material.Fluids;

/**
 * Programmatic generator for Structure 1: Frost-Veiled Glacial Runic Spire (18 x 18 x 45 above ground
 * plus -7..-1 secret Runic Vault basement). Zero external binary .nbt files required.
 */
public class FrostVeiledRunicSpirePiece extends StructurePiece {

    private static final ResourceKey<Potion> FREEZING_POTION_KEY =
            ResourceKey.create(Registries.POTION, ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "freezing"));

    // 8-step clockwise ring around (0, 0) for the central spiral staircase
    private static final int[][] SPIRAL_RING = {
            {0, 1}, {-1, 1}, {-1, 0}, {-1, -1},
            {0, -1}, {1, -1}, {1, 0}, {1, 1}
    };
    private static final Direction[] SPIRAL_STEP_FACING = {
            Direction.WEST, Direction.NORTH, Direction.NORTH, Direction.EAST,
            Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.WEST
    };

    // 3 flying buttress angles (60 deg SE, 180 deg N, 300 deg SW) symmetrically framing the South archway
    private static final double[] BUTTRESS_ANGLES_RAD = {
            Math.toRadians(60.0),
            Math.toRadians(180.0),
            Math.toRadians(300.0)
    };

    private final int originX;
    private final int baseY;
    private final int originZ;

    public FrostVeiledRunicSpirePiece(int originX, int baseY, int originZ) {
        super(ModStructures.FROST_VEILED_GLACIAL_RUNIC_SPIRE_PIECE.get(), 0,
                new BoundingBox(originX - 9, baseY - 7, originZ - 9, originX + 8, baseY + 46, originZ + 8));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public FrostVeiledRunicSpirePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.FROST_VEILED_GLACIAL_RUNIC_SPIRE_PIECE.get(), tag);
        this.originX = tag.getIntOr("OriginX", (this.boundingBox.minX() + this.boundingBox.maxX() + 1) / 2);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 7);
        this.originZ = tag.getIntOr("OriginZ", (this.boundingBox.minZ() + this.boundingBox.maxZ() + 1) / 2);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OriginX", this.originX);
        tag.putInt("BaseY", this.baseY);
        tag.putInt("OriginZ", this.originZ);
    }

    public BlockPos getSummitAnchorPos() {
        return new BlockPos(this.originX, this.baseY + 42, this.originZ);
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
        buildFoundationAndHexagonalTower(level, chunkBox);
        buildFlyingButtressesAndScaffolding(level, chunkBox);
        buildSecretRunicVaultBasement(level, chunkBox, random);
        buildGroundFloorArchwayAndWaterfall(level, chunkBox);
        buildCentralSpiralStaircase(level, chunkBox);
        buildFrostScriptoriumMidTower(level, chunkBox, random);
        buildSummitAnchorAndFloatingPrism(level, chunkBox);
    }

    private void buildFoundationAndHexagonalTower(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState runicBlueIce = ModBlocks.RUNIC_BLUE_ICE.get().defaultBlockState();
        BlockState basaltY = ModBlocks.RUNED_BASALT_PILLAR.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState air = Blocks.AIR.defaultBlockState();

        // Downward foundation columns beneath the hexagonal base so sloped coastal cliffs never leave gaps
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                double hDist = hexDistance(dx, dz);
                if (hDist <= 7.45D) {
                    int startDy = (Math.abs(dx) <= 6 && Math.abs(dz) <= 6) ? -8 : -1;
                    BlockState colState = isHexVertexColumn(dx, dz, 7.2D) ? basaltY : packedIce;
                    fillFoundationDown(level, chunkBox, dx, startDy, dz, colState);
                }
            }
        }

        // Main 45-block tapering hexagonal tower (dy = 0..41 enclosed shaft + summit deck)
        for (int dy = 0; dy <= 41; dy++) {
            double outerR = towerOuterRadius(dy);
            double innerR = outerR - 1.18D;
            boolean isRunicBand = (dy == 6 || dy == 13 || dy == 19 || dy == 20 || dy == 21 || dy == 22 || dy == 23 || dy == 25 || dy == 33 || dy == 41);

            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    double hDist = hexDistance(dx, dz);
                    if (hDist > outerR) {
                        continue;
                    }

                    if (dy == 0) {
                        // Ground floor slab inlaid with hexagonal Runic Blue Ice geometry
                        boolean runicInlay = Math.abs(hDist - 3.5D) < 0.55D || (dx == 0 || dz == 0);
                        setBlock(level, chunkBox, dx, 0, dz, runicInlay ? runicBlueIce : blueIce);
                        continue;
                    }

                    if (dy == 41) {
                        // Summit platform floor (leaving stairwell exit opening at (0, 41, 1) and (-1, 41, 1))
                        if ((dx == 0 && dz == 1) || (dx == -1 && dz == 1)) {
                            setBlock(level, chunkBox, dx, 41, dz, air);
                        } else {
                            boolean centerAltar = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
                            setBlock(level, chunkBox, dx, 41, dz, centerAltar ? runicBlueIce : blueIce);
                        }
                        continue;
                    }

                    if (hDist <= innerR) {
                        // Clear hollow interior shaft
                        setBlock(level, chunkBox, dx, dy, dz, air);
                    } else {
                        // Hexagonal exterior shell
                        if (isHexVertexColumn(dx, dz, outerR)) {
                            setBlock(level, chunkBox, dx, dy, dz, basaltY);
                        } else if (isLancetWindow(dx, dy, dz)) {
                            setBlock(level, chunkBox, dx, dy, dz, air);
                        } else if (isRunicBand && ((dx + dz + dy) & 1) == 0) {
                            setBlock(level, chunkBox, dx, dy, dz, runicBlueIce);
                        } else {
                            // Deterministic compressed ice pattern (Blue Ice dominant with Packed Ice courses)
                            int hash = mixHash(dx, dy, dz);
                            setBlock(level, chunkBox, dx, dy, dz, (hash & 3) == 0 ? packedIce : blueIce);
                        }
                    }
                }
            }
        }
    }

    private void buildFlyingButtressesAndScaffolding(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState basaltY = ModBlocks.RUNED_BASALT_PILLAR.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState pineSlabTop = ModBlocks.SILVER_PINE_SLAB.get().defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.TOP);
        BlockState pineSlabBottom = ModBlocks.SILVER_PINE_SLAB.get().defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState pineFence = ModBlocks.SILVER_PINE_FENCE.get().defaultBlockState();

        // 3 Packed Ice flying buttresses sweeping inward from r=8.2 (dy=0) to tower wall (dy=17)
        for (double angle : BUTTRESS_ANGLES_RAD) {
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            // Outer pier foot at r = 8
            int pierX = Mth.clamp((int) Math.round(cos * 8.0D), -9, 8);
            int pierZ = Mth.clamp((int) Math.round(sin * 8.0D), -9, 8);
            fillFoundationDown(level, chunkBox, pierX, -1, pierZ, basaltY);
            for (int py = 0; py <= 6; py++) {
                setBlock(level, chunkBox, pierX, py, pierZ, py <= 1 || py == 6 ? basaltY : packedIce);
            }
            setBlock(level, chunkBox, pierX, 7, pierZ, pineSlabBottom);

            // Arched flying rib rising from (r=8, dy=5) to (r=5.8, dy=17) with open arch gap underneath
            for (int step = 0; step <= 16; step++) {
                double t = step / 16.0D;
                double r = Mth.lerp(t, 8.0D, 5.8D);
                int archY = Mth.floor(5.0D + 12.0D * Math.pow(t, 0.78D));
                int bx = Mth.clamp((int) Math.round(cos * r), -9, 8);
                int bz = Mth.clamp((int) Math.round(sin * r), -9, 8);

                setBlock(level, chunkBox, bx, archY, bz, packedIce);
                setBlock(level, chunkBox, bx, archY + 1, bz, (step & 1) == 0 ? packedIce : blueIce);
                if (step % 4 == 0) {
                    setBlock(level, chunkBox, bx, archY + 2, bz, pineSlabBottom);
                }
            }
        }

        // Silver Pine scaffolding trim rings around exterior at dy=11 and dy=20
        int[] trimHeights = {11, 20};
        for (int ty : trimHeights) {
            double ringR = towerOuterRadius(ty) + 0.85D;
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    // Leave South entrance front clear on lower scaffolding
                    if (ty == 11 && dz >= 5 && Math.abs(dx) <= 2) {
                        continue;
                    }
                    double hDist = hexDistance(dx, dz);
                    if (hDist > towerOuterRadius(ty) && hDist <= ringR) {
                        BlockPos target = new BlockPos(this.originX + dx, this.baseY + ty, this.originZ + dz);
                        if (chunkBox.isInside(target) && level.isEmptyBlock(target)) {
                            if (((dx + dz) & 1) == 0) {
                                Direction inward = inwardHorizontalDirection(dx, dz);
                                BlockState stairTrim = ModBlocks.SILVER_PINE_STAIRS.get().defaultBlockState()
                                        .setValue(StairBlock.FACING, inward)
                                        .setValue(StairBlock.HALF, Half.TOP);
                                level.setBlock(target, stairTrim, 2);
                            } else {
                                level.setBlock(target, pineSlabTop, 2);
                            }
                            if (ty == 20 && ((dx ^ dz) & 3) == 0) {
                                setBlock(level, chunkBox, dx, ty + 1, dz, pineFence);
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildSecretRunicVaultBasement(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState runicBlueIce = ModBlocks.RUNIC_BLUE_ICE.get().defaultBlockState();
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState chiseledStone = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        BlockState basaltY = ModBlocks.RUNED_BASALT_PILLAR.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState rimebloom = ModBlocks.RIMEBLOOM.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // Outer shell of the basement (-6..+6 X, -7..-1 Y, -6..+6 Z)
        for (int dy = -7; dy <= -1; dy++) {
            for (int dx = -6; dx <= 6; dx++) {
                for (int dz = -6; dz <= 6; dz++) {
                    boolean isSubFloor = (dy == -7);
                    boolean isCeiling = (dy == -1);
                    boolean isPerimeterWall = (Math.abs(dx) == 6 || Math.abs(dz) == 6);

                    if (isSubFloor) {
                        setBlock(level, chunkBox, dx, dy, dz, packedIce);
                    } else if (dy == -6 && !isPerimeterWall) {
                        // Vault floor: narrow safe central Stone Bricks path + L-connector from stairs; Blue Ice on flanks
                        boolean onSafePath = (dx == 0 && dz >= -3 && dz <= 4)
                                || (dz == -3 && dx >= -4 && dx <= 0);
                        setBlock(level, chunkBox, dx, -6, dz, onSafePath ? stoneBricks : blueIce);
                    } else if (isPerimeterWall) {
                        boolean cornerPillar = Math.abs(dx) == 6 && Math.abs(dz) == 6;
                        if (cornerPillar) {
                            setBlock(level, chunkBox, dx, dy, dz, basaltY);
                        } else {
                            setBlock(level, chunkBox, dx, dy, dz, (dy == -4 || dy == -3) ? runicBlueIce : packedIce);
                        }
                    } else if (isCeiling) {
                        setBlock(level, chunkBox, dx, -1, dz, packedIce);
                    } else {
                        setBlock(level, chunkBox, dx, dy, dz, air);
                    }
                }
            }
        }

        // Dry hidden stairwell behind the waterfall niche at (0, 0, -5) descending West then South into Vault
        setBlock(level, chunkBox, 0, 0, -5, stoneBricks);
        setBlock(level, chunkBox, 0, 1, -5, air);
        setBlock(level, chunkBox, 0, 2, -5, air);

        for (int step = 1; step <= 3; step++) {
            int sx = -step;
            int sy = -step;
            BlockState stair = Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.EAST)
                    .setValue(StairBlock.HALF, Half.BOTTOM);
            setBlock(level, chunkBox, sx, sy - 1, -5, stoneBricks);
            setBlock(level, chunkBox, sx, sy, -5, stair);
            setBlock(level, chunkBox, sx, sy + 1, -5, air);
            setBlock(level, chunkBox, sx, sy + 2, -5, air);
            setBlock(level, chunkBox, sx, sy + 3, -5, air);
        }
        // Bottom corner landing at (-4, -4, -5) stepping South onto (-4, -5, -4) -> Vault floor (-4, -6, -3)
        setBlock(level, chunkBox, -4, -4, -5, chiseledStone);
        setBlock(level, chunkBox, -4, -3, -5, air);
        setBlock(level, chunkBox, -4, -2, -5, air);
        setBlock(level, chunkBox, -4, -1, -5, air);

        BlockState finalStair = Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH)
                .setValue(StairBlock.HALF, Half.BOTTOM);
        setBlock(level, chunkBox, -4, -6, -4, stoneBricks);
        setBlock(level, chunkBox, -4, -5, -4, finalStair);
        setBlock(level, chunkBox, -4, -4, -4, air);
        setBlock(level, chunkBox, -4, -3, -4, air);
        setBlock(level, chunkBox, -4, -2, -4, air);

        // Line flanking Blue Ice floor with Rimebloom flowers
        int[][] rimebloomCoords = {
                {-5, -2}, {-3, -2}, {-2, -1}, {-4, 1}, {-2, 3}, {-5, 4},
                {5, -2}, {3, -2}, {2, -1}, {4, 1}, {2, 3}, {5, 4},
                {-3, 4}, {3, 4}
        };
        for (int[] rc : rimebloomCoords) {
            setBlock(level, chunkBox, rc[0], -5, rc[1], rimebloom);
        }

        // Flanking Trap Line 1 (dz = 0): Tripwires on West & East ice (leaving safe dx=0 Stone Bricks untouched!)
        // Anchor bollards flanking the central path at (-1, -5, 0) and (+1, -5, 0) and wall blocks at (-6, -5, 0) / (+6, -5, 0)
        setBlock(level, chunkBox, -1, -5, 0, chiseledStone);
        setBlock(level, chunkBox, 1, -5, 0, chiseledStone);
        setBlock(level, chunkBox, -6, -5, 0, chiseledStone);
        setBlock(level, chunkBox, 6, -5, 0, chiseledStone);

        setBlock(level, chunkBox, -5, -5, 0, Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(TripWireHookBlock.FACING, Direction.EAST)
                .setValue(TripWireHookBlock.ATTACHED, true));
        setBlock(level, chunkBox, -2, -5, 0, Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(TripWireHookBlock.FACING, Direction.WEST)
                .setValue(TripWireHookBlock.ATTACHED, true));
        BlockState connectedWire = Blocks.TRIPWIRE.defaultBlockState()
                .setValue(TripWireBlock.ATTACHED, true)
                .setValue(TripWireBlock.EAST, true)
                .setValue(TripWireBlock.WEST, true);
        setBlock(level, chunkBox, -4, -5, 0, connectedWire);
        setBlock(level, chunkBox, -3, -5, 0, connectedWire);
        // Note: re-place inner anchor at -1? Hook at -2 attaches to solid block at -1! Let's verify:
        // TripWireHook facing WEST at (-2, -5, 0) attaches to (-1, -5, 0) (its opposite face = EAST)! Exact!

        setBlock(level, chunkBox, 5, -5, 0, Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(TripWireHookBlock.FACING, Direction.WEST)
                .setValue(TripWireHookBlock.ATTACHED, true));
        setBlock(level, chunkBox, 2, -5, 0, Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(TripWireHookBlock.FACING, Direction.EAST)
                .setValue(TripWireHookBlock.ATTACHED, true));
        setBlock(level, chunkBox, 3, -5, 0, connectedWire);
        setBlock(level, chunkBox, 4, -5, 0, connectedWire);

        // Hidden wall Dispensers directly above the outer Tripwire Hook anchor blocks at (-6, -4, 0) and (+6, -4, 0)
        // Wait: hook at (-5, -5, 0) powers (-6, -5, 0), which strongly powers Dispenser at (-6, -4, 0) with open slot at (-5, -4, 0)!
        placeAndStockTrapDispenser(level, chunkBox, -6, -4, 0, Direction.EAST);
        setBlock(level, chunkBox, -5, -4, 0, air);
        placeAndStockTrapDispenser(level, chunkBox, 6, -4, 0, Direction.WEST);
        setBlock(level, chunkBox, 5, -4, 0, air);

        // Flanking Trap Line 2 (dz = 2): Stone Pressure Plates on flanking ice wired via Redstone to hidden Dispensers
        setBlock(level, chunkBox, -3, -6, 2, chiseledStone);
        setBlock(level, chunkBox, -4, -6, 2, chiseledStone);
        setBlock(level, chunkBox, -5, -6, 2, chiseledStone);
        setBlock(level, chunkBox, -3, -5, 2, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
        setBlock(level, chunkBox, -4, -5, 2, Blocks.REDSTONE_WIRE.defaultBlockState());
        setBlock(level, chunkBox, -5, -5, 2, Blocks.REDSTONE_WIRE.defaultBlockState());
        placeAndStockTrapDispenser(level, chunkBox, -6, -5, 2, Direction.EAST);

        setBlock(level, chunkBox, 3, -6, 2, chiseledStone);
        setBlock(level, chunkBox, 4, -6, 2, chiseledStone);
        setBlock(level, chunkBox, 5, -6, 2, chiseledStone);
        setBlock(level, chunkBox, 3, -5, 2, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
        setBlock(level, chunkBox, 4, -5, 2, Blocks.REDSTONE_WIRE.defaultBlockState());
        setBlock(level, chunkBox, 5, -5, 2, Blocks.REDSTONE_WIRE.defaultBlockState());
        placeAndStockTrapDispenser(level, chunkBox, 6, -5, 2, Direction.WEST);

        // Runic Vault Treasure Altar & Chest at (0, -5, +4)
        setBlock(level, chunkBox, -1, -5, 5, basaltY);
        setBlock(level, chunkBox, 1, -5, 5, basaltY);
        setBlock(level, chunkBox, 0, -5, 5, runicBlueIce);
        setBlock(level, chunkBox, 0, -4, 5, ModBlocks.AEGIS_CRYSTAL_BLOCK.get().defaultBlockState());
        setBlock(level, chunkBox, 0, -6, 4, chiseledStone);
        placeLootChest(level, chunkBox, random, 0, -5, 4, Direction.NORTH);
    }

    private void buildGroundFloorArchwayAndWaterfall(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState runicBlueIce = ModBlocks.RUNIC_BLUE_ICE.get().defaultBlockState();
        BlockState basaltY = ModBlocks.RUNED_BASALT_PILLAR.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState basaltX = ModBlocks.RUNED_BASALT_PILLAR.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState heartRoot = ModBlocks.GLACIAL_HEART_ROOT.get().defaultBlockState();

        // Carve South open entrance archway (dx = -2..2, dz = 4..8, dy = 1..5)
        for (int dz = 4; dz <= 8; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                int archTop = Math.abs(dx) == 2 ? 3 : (Math.abs(dx) == 1 ? 4 : 5);
                for (int dy = 1; dy <= archTop; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, air);
                }
            }
            // Frame archway with Runed Basalt Pillars and Blue Ice arch voussoirs
            if (dz >= 5 && dz <= 7) {
                for (int dy = 1; dy <= 4; dy++) {
                    setBlock(level, chunkBox, -3, dy, dz, basaltY);
                    setBlock(level, chunkBox, 3, dy, dz, basaltY);
                }
                setBlock(level, chunkBox, -2, 4, dz, blueIce);
                setBlock(level, chunkBox, 2, 4, dz, blueIce);
                setBlock(level, chunkBox, -1, 5, dz, runicBlueIce);
                setBlock(level, chunkBox, 1, 5, dz, runicBlueIce);
                setBlock(level, chunkBox, 0, 6, dz, basaltX);

                // Drape archway ceiling with hanging Glacial Heart-Root
                setBlock(level, chunkBox, -2, 3, dz, heartRoot);
                setBlock(level, chunkBox, 2, 3, dz, heartRoot);
                if (dz == 6) {
                    setBlock(level, chunkBox, -1, 4, dz, heartRoot);
                    setBlock(level, chunkBox, 1, 4, dz, heartRoot);
                    setBlock(level, chunkBox, 0, 5, dz, heartRoot);
                }
            }
        }
        // Welcoming entrance steps at dz = 8
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, chunkBox, dx, 0, 8, ModBlocks.SILVER_PINE_STAIRS.get().defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH)
                    .setValue(StairBlock.HALF, Half.BOTTOM));
        }

        // North interior wall concealed Waterfall Curtain niche at (0, 0..3, -4) hiding the Runic Vault entrance
        for (int dy = 0; dy <= 3; dy++) {
            setBlock(level, chunkBox, -1, dy, -4, basaltY);
            setBlock(level, chunkBox, 1, dy, -4, basaltY);
            setBlock(level, chunkBox, -1, dy, -5, runicBlueIce);
            setBlock(level, chunkBox, 1, dy, -5, runicBlueIce);
        }
        // Enclose source pocket at (0, 3, -4) on all 4 horizontal sides + top so water streams straight down
        setBlock(level, chunkBox, 0, 4, -4, runicBlueIce);
        setBlock(level, chunkBox, 0, 3, -5, runicBlueIce);
        setBlock(level, chunkBox, 0, 3, -3, ModBlocks.SILVER_PINE_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH)
                .setValue(StairBlock.HALF, Half.TOP));

        // 1x1x1 catch-pit at (0, 0, -4) surrounded by solid blocks on all 4 sides + bottom (-1) -> zero flood spillover!
        setBlock(level, chunkBox, 0, -1, -4, runicBlueIce);
        setBlock(level, chunkBox, 0, 0, -3, blueIce);

        BlockPos sourcePos = new BlockPos(this.originX, this.baseY + 3, this.originZ - 4);
        if (chunkBox.isInside(sourcePos)) {
            level.setBlock(sourcePos, Blocks.WATER.defaultBlockState(), 2);
            level.scheduleTick(sourcePos, Fluids.WATER, 2);
        }
        BlockState fallingWater = Fluids.FLOWING_WATER.getFlowing(8, true).createLegacyBlock();
        setBlock(level, chunkBox, 0, 2, -4, fallingWater);
        setBlock(level, chunkBox, 0, 1, -4, fallingWater);
        setBlock(level, chunkBox, 0, 0, -4, fallingWater);
    }

    private void buildCentralSpiralStaircase(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState basaltY = ModBlocks.RUNED_BASALT_PILLAR.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // Central spine column from dy = 1 up to dy = 40
        for (int dy = 1; dy <= 40; dy++) {
            setBlock(level, chunkBox, 0, dy, 0, (dy % 5 == 0) ? ModBlocks.RUNIC_BLUE_ICE.get().defaultBlockState() : basaltY);
        }

        // Unbroken spiral staircase from dy = 1 up to dy = 40 (emerging onto Summit deck at dy = 41)
        for (int dy = 1; dy <= 40; dy++) {
            int idx = (dy - 1) & 7;
            int rx = SPIRAL_RING[idx][0];
            int rz = SPIRAL_RING[idx][1];
            Direction facing = SPIRAL_STEP_FACING[idx];

            boolean isCorner = Math.abs(rx) == 1 && Math.abs(rz) == 1;
            if (isCorner) {
                setBlock(level, chunkBox, rx, dy, rz, packedIce);
            } else {
                BlockState stepState = ModBlocks.SILVER_PINE_STAIRS.get().defaultBlockState()
                        .setValue(StairBlock.FACING, facing)
                        .setValue(StairBlock.HALF, Half.BOTTOM);
                setBlock(level, chunkBox, rx, dy, rz, stepState);
                // Solid Packed Ice corbel directly underneath non-ground stair steps for structural look & easy walking
                if (dy > 1) {
                    setBlock(level, chunkBox, rx, dy - 1, rz, packedIce);
                }
            }

            // Guarantee 3 blocks of clear air headroom directly above each step
            for (int h = 1; h <= 3 && dy + h <= 40; h++) {
                setBlock(level, chunkBox, rx, dy + h, rz, air);
            }
        }
    }

    private void buildFrostScriptoriumMidTower(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState runicBlueIce = ModBlocks.RUNIC_BLUE_ICE.get().defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState pineSlabBottom = ModBlocks.SILVER_PINE_SLAB.get().defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState pineFence = ModBlocks.SILVER_PINE_FENCE.get().defaultBlockState();

        // Mid-tower circular gallery floor at dy = 19 around the 3x3 central stairwell
        double innerWallR = towerOuterRadius(20) - 1.18D;
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    continue; // Keep central spiral stairwell open
                }
                double hDist = hexDistance(dx, dz);
                if (hDist <= innerWallR) {
                    boolean runicRing = Math.abs(hDist - 3.6D) < 0.55D;
                    setBlock(level, chunkBox, dx, 19, dz, runicRing ? runicBlueIce : blueIce);
                }
            }
        }

        // Scriptorium glowing Runic Blue Ice wall panels at dy = 20..22
        int[][] cardinalPanels = {{0, -5}, {0, 5}, {-5, 0}, {5, 0}};
        for (int[] panel : cardinalPanels) {
            setBlock(level, chunkBox, panel[0], 20, panel[1], runicBlueIce);
            setBlock(level, chunkBox, panel[0], 22, panel[1], runicBlueIce);
        }

        // Lecterns facing inward + Ice Benches on East, West, and South gallery quadrants (dy = 20)
        setBlock(level, chunkBox, -3, 20, 0, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.EAST));
        setBlock(level, chunkBox, -4, 20, -1, packedIce);
        setBlock(level, chunkBox, -4, 20, 1, packedIce);
        setBlock(level, chunkBox, -4, 20, 0, pineSlabBottom);

        setBlock(level, chunkBox, 3, 20, 0, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.WEST));
        setBlock(level, chunkBox, 4, 20, -1, packedIce);
        setBlock(level, chunkBox, 4, 20, 1, packedIce);
        setBlock(level, chunkBox, 4, 20, 0, pineSlabBottom);

        setBlock(level, chunkBox, 0, 20, 3, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.NORTH));
        setBlock(level, chunkBox, -1, 20, 4, pineSlabBottom);
        setBlock(level, chunkBox, 1, 20, 4, pineSlabBottom);

        // Safety railing posts at stairwell corners (except entry/exit steps)
        setBlock(level, chunkBox, -2, 20, -2, pineFence);
        setBlock(level, chunkBox, 2, 20, -2, pineFence);
        setBlock(level, chunkBox, -2, 20, 2, pineFence);
        setBlock(level, chunkBox, 2, 20, 2, pineFence);

        // Frost Scriptorium Treasure Chest at (0, 20, -4) facing South
        placeLootChest(level, chunkBox, random, 0, 20, -4, Direction.SOUTH);
    }

    private void buildSummitAnchorAndFloatingPrism(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState basaltY = ModBlocks.RUNED_BASALT_PILLAR.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState pineSlabBottom = ModBlocks.SILVER_PINE_SLAB.get().defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState pineFence = ModBlocks.SILVER_PINE_FENCE.get().defaultBlockState();
        BlockState crystalBlock = ModBlocks.AEGIS_CRYSTAL_BLOCK.get().defaultBlockState();
        BlockState baseCluster = ModBlocks.AEGIS_CRYSTAL_CLUSTER.get().defaultBlockState()
                .setValue(AmethystClusterBlock.WATERLOGGED, false);

        // Summit battlements & finials around perimeter at dy = 42..43
        double summitOuter = towerOuterRadius(41);
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double hDist = hexDistance(dx, dz);
                if (hDist > summitOuter - 1.05D && hDist <= summitOuter) {
                    if (isHexVertexColumn(dx, dz, summitOuter)) {
                        setBlock(level, chunkBox, dx, 42, dz, basaltY);
                        setBlock(level, chunkBox, dx, 43, dz, baseCluster.setValue(AmethystClusterBlock.FACING, Direction.UP));
                    } else {
                        setBlock(level, chunkBox, dx, 42, dz, ((dx + dz) & 1) == 0 ? pineSlabBottom : pineFence);
                    }
                }
            }
        }

        // Summit Aegis Crystal pedestal block at exact Y+42 (originX, baseY + 42, originZ)
        setBlock(level, chunkBox, 0, 42, 0, ModBlocks.AEGIS_CRYSTAL_BLOCK.get().defaultBlockState());
        setBlock(level, chunkBox, 0, 43, 0, Blocks.AIR.defaultBlockState());

        // Floating Aegis Crystal octahedral prism hovering above the summit at dy = 44..46
        setBlock(level, chunkBox, 0, 44, 0, crystalBlock);
        setBlock(level, chunkBox, 0, 45, 0, crystalBlock);
        setBlock(level, chunkBox, -1, 45, 0, crystalBlock);
        setBlock(level, chunkBox, 1, 45, 0, crystalBlock);
        setBlock(level, chunkBox, 0, 45, -1, crystalBlock);
        setBlock(level, chunkBox, 0, 45, 1, crystalBlock);
        setBlock(level, chunkBox, 0, 46, 0, crystalBlock);

        // Faceted Aegis Crystal Cluster tips on the floating prism
        setBlock(level, chunkBox, -2, 45, 0, baseCluster.setValue(AmethystClusterBlock.FACING, Direction.WEST));
        setBlock(level, chunkBox, 2, 45, 0, baseCluster.setValue(AmethystClusterBlock.FACING, Direction.EAST));
        setBlock(level, chunkBox, 0, 45, -2, baseCluster.setValue(AmethystClusterBlock.FACING, Direction.NORTH));
        setBlock(level, chunkBox, 0, 45, 2, baseCluster.setValue(AmethystClusterBlock.FACING, Direction.SOUTH));
    }

    private void placeLootChest(
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
        BlockState chestState = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
        level.setBlock(pos, chestState, 2);
        boolean inSilentFjord = level.getBiome(pos).is(ddraig.net.entropica.config.EntropicaBiome.SILENT_FJORD.resourceKey());
        RandomizableContainer.setBlockEntityLootTable(
                level,
                random,
                pos,
                inSilentFjord ? ModStructures.STRUCTURE_1_FJORD_CHEST_LOOT : ModStructures.STRUCTURE_1_CHEST_LOOT
        );
    }

    private void placeAndStockTrapDispenser(
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
            Holder<Potion> freezingHolder = resolveFreezingPotionHolder(level);
            for (int slot = 0; slot < dispenser.getContainerSize(); slot++) {
                Holder<Potion> chosen = (slot % 2 == 0) ? freezingHolder : Potions.SLOWNESS;
                ItemStack splashStack = PotionContents.createItemStack(Items.SPLASH_POTION, chosen);
                dispenser.setItem(slot, splashStack);
            }
        }
    }

    private static Holder<Potion> resolveFreezingPotionHolder(WorldGenLevel level) {
        var regOpt = level.registryAccess().lookup(Registries.POTION);
        if (regOpt.isPresent()) {
            var freezingRef = regOpt.get().get(FREEZING_POTION_KEY);
            if (freezingRef.isPresent()) {
                return freezingRef.get();
            }
        }
        if (ModPotions.FREEZING.isBound()) {
            return ModPotions.FREEZING;
        }
        return Potions.SLOWNESS;
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
        int minAllowedY = Math.max(level.getMinY() + 2, this.baseY + startDy - 16);
        for (int wy = this.baseY + startDy; wy >= minAllowedY; wy--) {
            cursor.set(wx, wy, wz);
            BlockState existing = level.getBlockState(cursor);
            if (existing.isAir()
                    || existing.is(Blocks.WATER)
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

    private static double towerOuterRadius(int dy) {
        if (dy <= 20) {
            return Mth.lerp(dy / 20.0D, 7.25D, 6.25D);
        }
        return Mth.lerp((dy - 20) / 21.0D, 6.25D, 4.35D);
    }

    private static double hexDistance(int dx, int dz) {
        double ax = Math.abs(dx);
        double az = Math.abs(dz);
        return Math.max(ax, 0.5D * ax + 0.8660254037844386D * az);
    }

    private static boolean isHexVertexColumn(int dx, int dz, double outerR) {
        for (int v = 0; v < 6; v++) {
            double rad = Math.toRadians(v * 60.0D);
            int vx = (int) Math.round(Math.cos(rad) * (outerR - 0.35D));
            int vz = (int) Math.round(Math.sin(rad) * (outerR - 0.35D));
            if (dx == vx && dz == vz) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLancetWindow(int dx, int dy, int dz) {
        boolean cardinalCenter = (dx == 0 && Math.abs(dz) >= 4) || (dz == 0 && Math.abs(dx) >= 4);
        if (!cardinalCenter) {
            return false;
        }
        return (dy >= 9 && dy <= 11) || (dy == 21) || (dy >= 30 && dy <= 32);
    }

    private static Direction inwardHorizontalDirection(int dx, int dz) {
        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx > 0 ? Direction.WEST : Direction.EAST;
        }
        return dz > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    private static int mixHash(int x, int y, int z) {
        int h = x * 374761393 + y * 668265263 + z * 2147483647;
        h = (h ^ (h >> 13)) * 1274126177;
        return h ^ (h >> 16);
    }
}
