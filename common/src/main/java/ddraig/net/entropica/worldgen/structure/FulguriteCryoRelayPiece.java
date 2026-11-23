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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import java.util.Optional;

/**
 * Programmatic 3D generator for Biome #7 Signature Structure: Fulgurite Cryo-Relay ({@code 17 x 17 x 24}):
 * <ul>
 *   <li>Exterior {@code fulgurite_ice} &amp; {@code blue_ice} grounding moat set into {@code snow_block}
 *       ({@code Y+0}), accented outside by a lightning-struck {@code fulgurite_pine_log} +
 *       {@code fulgurite_pine_leaves} tree and {@code cryo_static_shrub} clusters.</li>
 *   <li>Octagonal {@code deepslate_bricks} plinth ({@code Y+1}) crossed by radial {@code cut_copper}
 *       grounding bus-bars, {@code 4} cardinal {@code deepslate_brick_stairs} entrances, and
 *       {@code static_conductive_mat} grounding pads ({@code Y+2}).</li>
 *   <li>{@code 4} corner Faraday coil pylons ({@code chiseled_deepslate} -&gt; {@code basalt_column} -&gt;
 *       {@code cut_copper} -&gt; {@code exposed_cut_copper} -&gt; {@code fulgurite_ice} -&gt;
 *       {@code weathered_cut_copper} -&gt; {@code oxidized_cut_copper} -&gt; {@code lightning_rod})
 *       bridged via diagonal {@code exposed_cut_copper} strut arches into the central stepped copper Faraday dome.</li>
 *   <li>Open-ribbed Faraday rotunda ({@code basalt_column} &amp; {@code exposed_copper} pillars) housing
 *       the central {@code chiseled_deepslate} + {@code biomass_capacitor} relay + overhead inverted
 *       {@code lightning_rod}, {@code 2} flanking {@code biomass_capacitor}s, hanging {@code soul_lantern}s, and
 *       the Relay Chest ({@code chests/fulgurite_cryo_relay}).</li>
 *   <li>{@code 20}-block storm-needle spire ({@code weathered_cut_copper}, {@code fulgurite_ice} ringed
 *       with {@code weathered_cut_copper_slab} corona fins, {@code oxidized_copper},
 *       {@code aegis_crystal_block}, and apex {@code lightning_rod}).</li>
 *   <li>Underground Capacitor Vault ({@code Y-5..-1}) reached via interior {@code deepslate_brick_stairs},
 *       featuring checkerboard {@code copper_block} floor grates, a safe {@code static_conductive_mat}
 *       walkway, a {@code biomass_capacitor} + {@code lever}, a tripwire-armed {@code dispenser} loaded
 *       with Splash Potions of Slowness, the Vault Chest, and a {@code silver_pine_barrel}.</li>
 * </ul>
 */
public class FulguriteCryoRelayPiece extends StructurePiece {

    private static final int[][] ROTUNDA_RIB_PILLARS = {
            {-4, -2}, {-4, 2}, {4, -2}, {4, 2},
            {-2, -4}, {2, -4}, {-2, 4}, {2, 4}
    };

    private static final int[][] CORNER_COIL_PYLONS = {
            {-5, -5}, {5, -5}, {-5, 5}, {5, 5}
    };

    private static final int[][] PLINTH_GROUNDING_MATS = {
            {0, -5}, {0, -4}, {0, 5}, {0, 4},
            {-5, 0}, {-4, 0}, {4, 0}, {5, 0}
    };

    private static final int[][] VAULT_SAFE_MAT_PATH = {
            {2, -2}, {2, -1}, {2, 0}, {2, 1}, {2, 2}, {1, 2}, {0, 2}
    };

    private static final int[][] EXTERIOR_SHRUB_SPOTS = {
            {7, -7}, {-7, 7}, {7, 7}, {-8, 4}, {5, -8}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;

    public FulguriteCryoRelayPiece(int originX, int baseY, int originZ) {
        // Exact 17 (W: -8..+8) x 24 (H: -5..+18) x 17 (L: -8..+8) bounding box
        super(resolvePieceType(), 0,
                new BoundingBox(originX - 8, baseY - 5, originZ - 8, originX + 8, baseY + 18, originZ + 8));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public FulguriteCryoRelayPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", (this.boundingBox.minX() + this.boundingBox.maxX()) / 2);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 5);
        this.originZ = tag.getIntOr("OriginZ", (this.boundingBox.minZ() + this.boundingBox.maxZ()) / 2);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.FULGURITE_CRYO_RELAY_PIECE.isPresent()) {
            return ModStructures.FULGURITE_CRYO_RELAY_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_cryo_relay_piece");
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

    public BlockPos getRelayChestPos() {
        return new BlockPos(this.originX, this.baseY + 2, this.originZ - 3);
    }

    public BlockPos getVaultChestPos() {
        return new BlockPos(this.originX, this.baseY - 4, this.originZ + 3);
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
        buildFoundationMoatAndExteriorPine(level, chunkBox);
        buildUndergroundCapacitorVault(level, chunkBox, random);
        buildOctagonalPlinthAndCardinalEntrances(level, chunkBox);
        buildFaradayRotundaPylonsAndStormSpire(level, chunkBox, random);
        buildInteriorVaultStairwellAndMats(level, chunkBox);
    }

    private void buildFoundationMoatAndExteriorPine(WorldGenLevel level, BoundingBox chunkBox) {
        Block fulguriteIceBlock = resolveBlock("fulgurite_ice", Blocks.PACKED_ICE);
        Block pineLogBlock = resolveBlock("fulgurite_pine_log", Blocks.SPRUCE_LOG);
        Block pineLeavesBlock = resolveBlock("fulgurite_pine_leaves", Blocks.SPRUCE_LEAVES);
        Block basaltColumnBlock = resolveBlock("basalt_column", Blocks.BASALT);
        Optional<Block> shrubOpt = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_static_shrub"));

        BlockState fulguriteIce = fulguriteIceBlock.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState snowBlock = Blocks.SNOW_BLOCK.defaultBlockState();
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState cutCopper = Blocks.CUT_COPPER.defaultBlockState();
        BlockState basaltColumnY = columnWithAxis(basaltColumnBlock, Direction.Axis.Y);
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Clear above-ground volume (dy = 1..18) and anchor solid foundation downward across 17x17 footprint
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int dy = 1; dy <= 18; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, air);
                }

                int absX = Math.abs(dx);
                int absZ = Math.abs(dz);
                int maxAxis = Math.max(absX, absZ);
                int manhattan = absX + absZ;

                boolean inVaultFootprint = (absX <= 4 && absZ <= 4);
                boolean inPlinthCore = (maxAxis <= 5 && manhattan <= 8) || (absX == 5 && absZ == 5);
                boolean onCardinalCauseway = (absX <= 1 && absZ >= 6 && absZ <= 7)
                        || (absZ <= 1 && absX >= 6 && absX <= 7);
                boolean inGroundingMoat = !inPlinthCore && !onCardinalCauseway && maxAxis <= 7 && manhattan <= 12;

                if (!inVaultFootprint) {
                    BlockState subBase = inPlinthCore ? deepslateBricks
                            : ((absX == 5 || absZ == 5) ? basaltColumnY : packedIce);
                    setBlock(level, chunkBox, dx, -1, dz, subBase);
                    fillFoundationDown(level, chunkBox, dx, -2, dz, inPlinthCore ? deepslateBricks : packedIce);
                } else {
                    fillFoundationDown(level, chunkBox, dx, -6, dz, deepslateBricks);
                }

                // Ground / Moat Layer (dy = 0)
                if (inPlinthCore) {
                    setBlock(level, chunkBox, dx, 0, dz, deepslateBricks);
                } else if (onCardinalCauseway) {
                    boolean centerBus = (dx == 0 || dz == 0);
                    setBlock(level, chunkBox, dx, 0, dz, centerBus ? cutCopper : deepslateBricks);
                } else if (inGroundingMoat) {
                    boolean isFulguriteTile = ((dx + dz) & 1) == 0;
                    setBlock(level, chunkBox, dx, 0, dz, isFulguriteTile ? fulguriteIce : blueIce);
                } else {
                    setBlock(level, chunkBox, dx, 0, dz, snowBlock);
                }
            }
        }

        // 2. Lightning-Struck Fulgurite Pine Accent outside NW moat corner (-7, 1..5, -7)
        setBlock(level, chunkBox, -7, 0, -7, packedIce);
        setBlock(level, chunkBox, -6, 0, -8, fulguriteIce);
        setBlock(level, chunkBox, -8, 0, -6, fulguriteIce);

        BlockState logY = columnWithAxis(pineLogBlock, Direction.Axis.Y);
        BlockState logX = columnWithAxis(pineLogBlock, Direction.Axis.X);
        BlockState logZ = columnWithAxis(pineLogBlock, Direction.Axis.Z);
        BlockState pineLeaves = persistentLeaves(pineLeavesBlock);

        setBlock(level, chunkBox, -7, 1, -7, logY);
        setBlock(level, chunkBox, -7, 2, -7, logY);
        setBlock(level, chunkBox, -7, 3, -7, logY);
        // Lightning-split angled upper snag & branch stub
        setBlock(level, chunkBox, -6, 4, -7, logY);
        setBlock(level, chunkBox, -6, 5, -7, logY);
        setBlock(level, chunkBox, -7, 3, -6, logZ);
        setBlock(level, chunkBox, -8, 2, -7, logX);

        int[][] pineLeafOffsets = {
                {-7, 4, -7}, {-8, 3, -7}, {-7, 4, -6}, {-6, 4, -8},
                {-5, 4, -7}, {-6, 5, -6}, {-7, 5, -7}, {-6, 6, -7}
        };
        for (int[] lf : pineLeafOffsets) {
            setBlock(level, chunkBox, lf[0], lf[1], lf[2], pineLeaves);
        }

        // 3. Exterior Cryo-Static Shrub accents on perimeter snow_block tiles
        if (shrubOpt.isPresent()) {
            BlockState shrubState = shrubOpt.get().defaultBlockState();
            for (int[] spot : EXTERIOR_SHRUB_SPOTS) {
                setBlock(level, chunkBox, spot[0], 0, spot[1], snowBlock);
                placeIfSurvives(level, chunkBox, spot[0], 1, spot[1], shrubState);
            }
        }
    }

    private void buildUndergroundCapacitorVault(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block basaltColumnBlock = resolveBlock("basalt_column", Blocks.BASALT);
        Block capacitorBlock = resolveBlock("biomass_capacitor", Blocks.COPPER_BULB);
        Block silverPineBarrelBlock = resolveBlock("silver_pine_barrel", Blocks.BARREL);

        BlockState basaltColumnY = columnWithAxis(basaltColumnBlock, Direction.Axis.Y);
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState crackedBricks = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState copperBlock = Blocks.COPPER_BLOCK.defaultBlockState();
        BlockState cutCopper = Blocks.CUT_COPPER.defaultBlockState();
        BlockState capacitor = capacitorBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Vault Shell, Checkerboard Copper Floor Grates (dy = -5), and Sub-Plinth Ceiling (dy = -1)
        for (int dy = -5; dy <= -1; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    boolean isWall = (Math.abs(dx) == 4 || Math.abs(dz) == 4);
                    if (dy == -5) {
                        if (isWall) {
                            setBlock(level, chunkBox, dx, -5, dz, deepslateBricks);
                        } else {
                            // Checkerboard copper_block floor grates across interior vault floor
                            BlockState grate = (((dx + dz) & 1) == 0) ? copperBlock : deepslateBricks;
                            setBlock(level, chunkBox, dx, -5, dz, grate);
                        }
                    } else if (dy == -1) {
                        BlockState ceilingTile = (dx == 0 && dz == 0) ? cutCopper
                                : ((Math.abs(dx) <= 1 && Math.abs(dz) <= 1) ? chiseledDeepslate : deepslateBricks);
                        setBlock(level, chunkBox, dx, -1, dz, ceilingTile);
                    } else if (isWall) {
                        boolean isRib = (Math.abs(dx) == 4 && (dz == -4 || dz == 0 || dz == 4))
                                || (Math.abs(dz) == 4 && (dx == -4 || dx == 0 || dx == 4));
                        if (isRib) {
                            setBlock(level, chunkBox, dx, dy, dz, basaltColumnY);
                        } else {
                            setBlock(level, chunkBox, dx, dy, dz,
                                    (((dx + dy + dz) & 3) == 0) ? crackedBricks : deepslateBricks);
                        }
                    } else {
                        setBlock(level, chunkBox, dx, dy, dz, air);
                    }
                }
            }
        }

        // Hanging Soul Lanterns illuminating the Underground Capacitor Vault
        setBlock(level, chunkBox, -2, -2, -2, Blocks.SOUL_LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false));
        setBlock(level, chunkBox, 2, -2, 2, Blocks.SOUL_LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false));

        // 2. Biomass Capacitor + Wall Control Lever on West Vault Bulkhead (-3, -4, -1)
        setBlock(level, chunkBox, -3, -5, -1, copperBlock);
        setBlock(level, chunkBox, -3, -4, -1, capacitor);
        setBlock(level, chunkBox, -4, -3, -1, chiseledDeepslate);
        setBlock(level, chunkBox, -3, -3, -1, Blocks.LEVER.defaultBlockState()
                .setValue(LeverBlock.FACE, AttachFace.WALL)
                .setValue(LeverBlock.FACING, Direction.EAST)
                .setValue(LeverBlock.POWERED, false));

        // 3. Tripwire + Hidden Dispenser Trap (Splash Potions of Slowness) guarding bare copper grate shortcut (dz = +1)
        //    Players following the East safe static_conductive_mat path (dx = +2) bypass the tripwire cleanly!
        setBlock(level, chunkBox, -4, -4, 1, chiseledDeepslate);
        setBlock(level, chunkBox, 1, -4, 1, chiseledDeepslate);

        setBlock(level, chunkBox, -3, -4, 1, Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(TripWireHookBlock.FACING, Direction.EAST)
                .setValue(TripWireHookBlock.ATTACHED, true));
        setBlock(level, chunkBox, 0, -4, 1, Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(TripWireHookBlock.FACING, Direction.WEST)
                .setValue(TripWireHookBlock.ATTACHED, true));

        BlockState armedWire = Blocks.TRIPWIRE.defaultBlockState()
                .setValue(TripWireBlock.ATTACHED, true)
                .setValue(TripWireBlock.EAST, true)
                .setValue(TripWireBlock.WEST, true);
        setBlock(level, chunkBox, -2, -4, 1, armedWire);
        setBlock(level, chunkBox, -1, -4, 1, armedWire);

        placeAndStockSlownessDispenser(level, chunkBox, -4, -3, 1, Direction.EAST);
        setBlock(level, chunkBox, -3, -3, 1, air);

        // 4. Vault Chest at (0, -4, +3) facing NORTH & Silver-Pine Barrel at (-2, -4, +3)
        setBlock(level, chunkBox, 0, -5, 3, chiseledDeepslate);
        placeRelayChest(level, chunkBox, random, 0, -4, 3, Direction.NORTH);

        setBlock(level, chunkBox, -2, -5, 3, copperBlock);
        placeRelayBarrel(level, chunkBox, random, silverPineBarrelBlock, -2, -4, 3, Direction.UP);
    }

    private void buildOctagonalPlinthAndCardinalEntrances(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState cutCopper = Blocks.CUT_COPPER.defaultBlockState();

        // 1. Octagonal Deepslate Bricks Plinth at Y+1 crossed by radial Cut Copper grounding bus-bars
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int absX = Math.abs(dx);
                int absZ = Math.abs(dz);
                if (absX + absZ > 8) {
                    continue;
                }

                boolean onRadialBusBar = (dx == 0 || dz == 0 || (absX == absZ && absX <= 3));
                boolean isOctagonRim = (absX == 5 || absZ == 5 || absX + absZ == 8);
                BlockState deckTile;
                if (dx == 0 && dz == 0) {
                    deckTile = chiseledDeepslate;
                } else if (onRadialBusBar) {
                    deckTile = cutCopper;
                } else if (isOctagonRim) {
                    deckTile = chiseledDeepslate;
                } else {
                    deckTile = deepslateBricks;
                }
                setBlock(level, chunkBox, dx, 1, dz, deckTile);
            }
        }

        // Corner Pylon Plinth Pads at (+/-5, 1, +/-5) & bridge spurs
        for (int[] pylon : CORNER_COIL_PYLONS) {
            int px = pylon[0];
            int pz = pylon[1];
            int sx = px - Integer.signum(px); // +/-4
            int sz = pz - Integer.signum(pz); // +/-4
            setBlock(level, chunkBox, px, 1, pz, chiseledDeepslate);
            setBlock(level, chunkBox, sx, 1, pz, deepslateBricks);
            setBlock(level, chunkBox, px, 1, sz, deepslateBricks);
            setBlock(level, chunkBox, sx, 1, sz, cutCopper);
        }

        // 2. Four Cardinal Deepslate Brick Stairs Entrances stepping onto the Y+1 Plinth
        for (int span = -1; span <= 1; span++) {
            // North entrance (dz = -6 -> ascends South)
            setBlock(level, chunkBox, span, 1, -6, stair(Direction.SOUTH, Half.BOTTOM));
            // South entrance (dz = +6 -> ascends North)
            setBlock(level, chunkBox, span, 1, 6, stair(Direction.NORTH, Half.BOTTOM));
            // West entrance (dx = -6 -> ascends East)
            setBlock(level, chunkBox, -6, 1, span, stair(Direction.EAST, Half.BOTTOM));
            // East entrance (dx = +6 -> ascends West)
            setBlock(level, chunkBox, 6, 1, span, stair(Direction.WEST, Half.BOTTOM));
        }
    }

    private void buildFaradayRotundaPylonsAndStormSpire(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block basaltColumnBlock = resolveBlock("basalt_column", Blocks.BASALT);
        Block fulguriteIceBlock = resolveBlock("fulgurite_ice", Blocks.PACKED_ICE);
        Block capacitorBlock = resolveBlock("biomass_capacitor", Blocks.COPPER_BULB);
        Block centralRelayBlock = resolveBlock("biomass_capacitor", Blocks.COPPER_BLOCK);
        Block aegisCrystalBlock = resolveBlock("aegis_crystal_block", Blocks.AMETHYST_BLOCK);

        BlockState basaltColumnY = columnWithAxis(basaltColumnBlock, Direction.Axis.Y);
        BlockState fulguriteIce = fulguriteIceBlock.defaultBlockState();
        BlockState capacitor = capacitorBlock.defaultBlockState();
        BlockState chargedRelay = withBooleanProperty(centralRelayBlock.defaultBlockState(), "charged", true);
        BlockState aegisCrystal = aegisCrystalBlock.defaultBlockState();

        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState cutCopper = Blocks.CUT_COPPER.defaultBlockState();
        BlockState exposedCopper = Blocks.EXPOSED_COPPER.defaultBlockState();
        BlockState exposedCutCopper = Blocks.EXPOSED_CUT_COPPER.defaultBlockState();
        BlockState weatheredCutCopper = Blocks.WEATHERED_CUT_COPPER.defaultBlockState();
        BlockState oxidizedCopper = Blocks.OXIDIZED_COPPER.defaultBlockState();
        BlockState oxidizedCutCopper = Blocks.OXIDIZED_CUT_COPPER.defaultBlockState();
        BlockState rodUp = lightningRod(Direction.UP);
        BlockState rodDown = lightningRod(Direction.DOWN);

        // 1. Open-Ribbed Faraday Rotunda Pillars (dy = 2..5) of Basalt Column & Exposed Copper
        for (int[] rib : ROTUNDA_RIB_PILLARS) {
            int rx = rib[0];
            int rz = rib[1];
            setBlock(level, chunkBox, rx, 2, rz, basaltColumnY);
            setBlock(level, chunkBox, rx, 3, rz, exposedCopper);
            setBlock(level, chunkBox, rx, 4, rz, basaltColumnY);
            setBlock(level, chunkBox, rx, 5, rz, exposedCopper);
        }

        // 2. Four Corner Faraday Coil Pylons (+/-5, dy, +/-5) & Diagonal Exposed Cut Copper Strut Arches
        for (int[] pylon : CORNER_COIL_PYLONS) {
            int px = pylon[0];
            int pz = pylon[1];
            int stepInX = -Integer.signum(px);
            int stepInZ = -Integer.signum(pz);

            // Coil Pylon Stack: chiseled_deepslate (Y+1) -> basalt_column -> cut_copper -> exposed_cut_copper
            // -> fulgurite_ice -> weathered_cut_copper -> oxidized_cut_copper -> lightning_rod
            setBlock(level, chunkBox, px, 2, pz, basaltColumnY);
            setBlock(level, chunkBox, px, 3, pz, basaltColumnY);
            setBlock(level, chunkBox, px, 4, pz, cutCopper);
            setBlock(level, chunkBox, px, 5, pz, exposedCutCopper);
            setBlock(level, chunkBox, px, 6, pz, fulguriteIce);
            setBlock(level, chunkBox, px, 7, pz, weatheredCutCopper);
            setBlock(level, chunkBox, px, 8, pz, oxidizedCutCopper);
            setBlock(level, chunkBox, px, 9, pz, rodUp);

            // Diagonal Exposed Cut Copper Strut Arch into the central stepped Faraday dome
            setBlock(level, chunkBox, px + stepInX, 6, pz + stepInZ, exposedCutCopper); // (+/-4, 6, +/-4)
            setBlock(level, chunkBox, px + stepInX, 7, pz + stepInZ, exposedCutCopper); // (+/-4, 7, +/-4)
            setBlock(level, chunkBox, px + stepInX * 2, 7, pz + stepInZ * 2, exposedCutCopper); // (+/-3, 7, +/-3)
        }

        // 3. Stepped Copper Faraday Dome (dy = 6..8) capping the Open-Ribbed Rotunda
        // Tier 1 (dy = 6): Open-ribbed octagonal copper ring + radial conductor ribs
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int absX = Math.abs(dx);
                int absZ = Math.abs(dz);
                if (absX + absZ > 6) {
                    continue;
                }
                boolean outerRing = (absX == 4 || absZ == 4 || absX + absZ == 6);
                boolean radialRib = (dx == 0 || dz == 0 || absX == absZ);
                if (dx == 0 && dz == 0) {
                    setBlock(level, chunkBox, 0, 6, 0, chiseledDeepslate);
                } else if (outerRing) {
                    setBlock(level, chunkBox, dx, 6, dz, cutCopper);
                } else if (radialRib) {
                    setBlock(level, chunkBox, dx, 6, dz, exposedCutCopper);
                }
            }
        }

        // Tier 2 (dy = 7): Stepped Exposed Cut Copper dome ring
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > 3) {
                    continue;
                }
                setBlock(level, chunkBox, dx, 7, dz, (dx == 0 && dz == 0) ? chiseledDeepslate : exposedCutCopper);
            }
        }

        // Tier 3 (dy = 8): Weathered Cut Copper spire collar
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 1) {
                    setBlock(level, chunkBox, dx, 8, dz, weatheredCutCopper);
                }
            }
        }

        // 4. Central Biomass Capacitor Relay Assembly, Twin Biomass Capacitors, Soul Lanterns & Relay Chest
        setBlock(level, chunkBox, 0, 2, 0, chiseledDeepslate);
        setBlock(level, chunkBox, 0, 3, 0, chargedRelay);
        scheduleBlockTick(level, chunkBox, 0, 3, 0, centralRelayBlock);
        // Overhead inverted Lightning Rod pointing down at the central relay across a 1-block spark gap
        setBlock(level, chunkBox, 0, 5, 0, rodDown);

        // Two Biomass Capacitors flanking the rotunda sanctuary
        setBlock(level, chunkBox, -2, 1, -2, chiseledDeepslate);
        setBlock(level, chunkBox, -2, 2, -2, capacitor);
        setBlock(level, chunkBox, 2, 1, -2, chiseledDeepslate);
        setBlock(level, chunkBox, 2, 2, -2, capacitor);

        // Four Hanging Soul Lanterns beneath the diagonal dome ribs (+/-2, 5, +/-2)
        int[][] rotundaLanterns = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}};
        for (int[] lt : rotundaLanterns) {
            setBlock(level, chunkBox, lt[0], 5, lt[1], Blocks.SOUL_LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING, true)
                    .setValue(LanternBlock.WATERLOGGED, false));
        }

        // Relay Chest at (0, 2, -3) facing SOUTH toward the central relay
        setBlock(level, chunkBox, 0, 1, -3, chiseledDeepslate);
        placeRelayChest(level, chunkBox, random, 0, 2, -3, Direction.SOUTH);

        // 5. Tall 20-Block Storm-Needle Spire (from Y-1 vault crown through Y+18 apex lightning_rod)
        BlockState lowerCoronaSlab = weatheredCopperSlab(SlabType.BOTTOM);
        BlockState upperCoronaSlab = weatheredCopperSlab(SlabType.TOP);

        setBlock(level, chunkBox, 0, 9, 0, weatheredCutCopper);
        placeCardinalSlabs(level, chunkBox, 9, lowerCoronaSlab);
        setBlock(level, chunkBox, 0, 10, 0, weatheredCutCopper);

        // Lower Fulgurite-Ice Dielectric Ring + Weathered Cut Copper Slab Corona Fins (Y+11)
        setBlock(level, chunkBox, 0, 11, 0, fulguriteIce);
        placeCardinalSlabs(level, chunkBox, 11, upperCoronaSlab);

        setBlock(level, chunkBox, 0, 12, 0, oxidizedCopper);
        setBlock(level, chunkBox, 0, 13, 0, oxidizedCopper);

        // Upper Fulgurite-Ice Dielectric Ring + Weathered Cut Copper Slab Corona Fins (Y+14)
        setBlock(level, chunkBox, 0, 14, 0, fulguriteIce);
        placeCardinalSlabs(level, chunkBox, 14, lowerCoronaSlab);

        setBlock(level, chunkBox, 0, 15, 0, oxidizedCopper);
        setBlock(level, chunkBox, 0, 16, 0, aegisCrystal);
        setBlock(level, chunkBox, 0, 17, 0, aegisCrystal);
        setBlock(level, chunkBox, 0, 18, 0, rodUp);
    }

    private void buildInteriorVaultStairwellAndMats(WorldGenLevel level, BoundingBox chunkBox) {
        Block conductiveMatBlock = resolveBlock("static_conductive_mat", Blocks.CYAN_CARPET);

        BlockState conductiveMat = conductiveMatBlock.defaultBlockState();
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Interior East Stairwell descending along dx = +3 from Rotunda Plinth (Y+1) to Vault (Y-4)
        // Top entry step at (+3, +1, +2) entered from (+2, +2, +2)
        setBlock(level, chunkBox, 3, 0, 2, deepslateBricks);
        setBlock(level, chunkBox, 3, 1, 2, stair(Direction.WEST, Half.BOTTOM));
        for (int h = 2; h <= 4; h++) {
            setBlock(level, chunkBox, 3, h, 2, air);
        }

        // Steps descending North along dx = +3: (z=+1, y=0), (z=0, y=-1), (z=-1, y=-2), (z=-2, y=-3)
        int[][] northFlight = {{1, 0}, {0, -1}, {-1, -2}, {-2, -3}};
        for (int[] step : northFlight) {
            int sz = step[0];
            int sy = step[1];
            setBlock(level, chunkBox, 3, sy - 1, sz, deepslateBricks);
            setBlock(level, chunkBox, 3, sy, sz, stair(Direction.SOUTH, Half.BOTTOM));
            for (int h = 1; h <= 3; h++) {
                setBlock(level, chunkBox, 3, sy + h, sz, air);
            }
        }

        // Bottom corner landing (+3, -4, -3) turning West via final stair (+2, -4, -3) onto the Vault Safe Mat Path
        setBlock(level, chunkBox, 3, -5, -3, chiseledDeepslate);
        for (int h = -4; h <= -2; h++) {
            setBlock(level, chunkBox, 3, h, -3, air);
        }
        setBlock(level, chunkBox, 2, -5, -3, deepslateBricks);
        setBlock(level, chunkBox, 2, -4, -3, stair(Direction.EAST, Half.BOTTOM));
        setBlock(level, chunkBox, 2, -3, -3, air);
        setBlock(level, chunkBox, 2, -2, -3, air);

        // Safety Deepslate Brick Wall balustrade bordering the stairwell inside the Rotunda (Y+2)
        for (int dz = -1; dz <= 1; dz++) {
            setBlock(level, chunkBox, 2, 2, dz, Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState());
        }

        // 2. Plinth Cardinal Grounding Pads (Y+2) & Vault Safe Static-Conductive Mat Walkway (Y-4)
        for (int[] pad : PLINTH_GROUNDING_MATS) {
            placeIfSurvives(level, chunkBox, pad[0], 2, pad[1], conductiveMat);
        }
        for (int[] tile : VAULT_SAFE_MAT_PATH) {
            placeIfSurvives(level, chunkBox, tile[0], -4, tile[1], conductiveMat);
        }
    }

    private void placeCardinalSlabs(WorldGenLevel level, BoundingBox chunkBox, int dy, BlockState slabState) {
        setBlock(level, chunkBox, -1, dy, 0, slabState);
        setBlock(level, chunkBox, 1, dy, 0, slabState);
        setBlock(level, chunkBox, 0, dy, -1, slabState);
        setBlock(level, chunkBox, 0, dy, 1, slabState);
    }

    private void placeRelayChest(
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
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.FULGURITE_CRYO_RELAY_CHEST_LOOT);
    }

    private void placeRelayBarrel(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random,
            Block barrelBlock,
            int dx,
            int dy,
            int dz,
            Direction facing
    ) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState barrelState = barrelBlock.defaultBlockState();
        if (barrelState.hasProperty(BarrelBlock.FACING)) {
            barrelState = barrelState.setValue(BarrelBlock.FACING, facing);
        }
        level.setBlock(pos, barrelState, 2);
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.FULGURITE_CRYO_RELAY_CHEST_LOOT);
    }

    private void placeAndStockSlownessDispenser(
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
                ItemStack splashSlowness = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.SLOWNESS);
                dispenser.setItem(slot, splashSlowness);
            }
        }
    }

    private void placeIfSurvives(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, BlockState state) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (chunkBox.isInside(pos) && level.getBlockState(pos).isAir() && state.canSurvive(level, pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void scheduleBlockTick(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, Block block) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (chunkBox.isInside(pos)) {
            level.scheduleTick(pos, block, 2);
        }
    }

    private static BlockState stair(Direction facing, Half half) {
        return Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half)
                .setValue(StairBlock.WATERLOGGED, false);
    }

    private static BlockState weatheredCopperSlab(SlabType type) {
        return Blocks.WEATHERED_CUT_COPPER_SLAB.defaultBlockState()
                .setValue(SlabBlock.TYPE, type)
                .setValue(SlabBlock.WATERLOGGED, false);
    }

    private static BlockState lightningRod(Direction facing) {
        BlockState state = Blocks.LIGHTNING_ROD.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            state = state.setValue(BlockStateProperties.FACING, facing);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }

    private static BlockState columnWithAxis(Block block, Direction.Axis axis) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            return state.setValue(RotatedPillarBlock.AXIS, axis);
        }
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            return state.setValue(BlockStateProperties.AXIS, axis);
        }
        return state;
    }

    private static BlockState persistentLeaves(Block leavesBlock) {
        BlockState state = leavesBlock.defaultBlockState();
        if (state.hasProperty(LeavesBlock.PERSISTENT)) {
            state = state.setValue(LeavesBlock.PERSISTENT, true);
        } else if (state.hasProperty(BlockStateProperties.PERSISTENT)) {
            state = state.setValue(BlockStateProperties.PERSISTENT, true);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
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
        int minAllowedY = Math.max(level.getMinY() + 2, this.baseY + startDy - 20);
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
