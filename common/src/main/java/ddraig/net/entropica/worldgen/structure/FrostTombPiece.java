package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

import java.util.List;

/**
 * Programmatic 3D generator for Structure 5: Frost-Tomb of the Aegis Wardens
 * ({@code 26} wide {@code X: 0..25}, {@code 15} tall {@code Y: 0..14}, {@code 32} long {@code Z: 0..31},
 * embedded {@code 4} blocks into solid tundra hillside with downward foundation anchoring):
 * <ul>
 *   <li><b>Outer Earthen Barrow Mound</b>: Rounded {@code 26 x 32} elliptical dome of {@code necrotic_soil}
 *       + {@code snow_block} mantle + {@code grave_moss} patches + weathered basalt/barrow standing stones
 *       flanking the recessed entrance.</li>
 *   <li><b>Recessed Tomb Portal ({@code Z = 0..4})</b>: Heavy {@code polished_basalt} &amp;
 *       {@code barrow_stone_bricks} arch with {@code barrow_sentinel_totem}s and {@code chiseled_deepslate}
 *       lintel leading down broad stone steps into the tomb.</li>
 *   <li><b>Room 1 — Sarcophagus Antechamber ({@code Z = 5..12})</b>: Vaulted {@code barrow_stone_bricks} /
 *       {@code mossy_barrow_bricks} hall with {@code 3} stone-slab sarcophagi ({@code 2} side alcoves housing
 *       {@code chests/structure_5} loot chests beneath slab lids + {@code 1} central honor sarcophagus)
 *       and {@code rime_cap_mushroom} clusters along the damp walls.</li>
 *   <li><b>Room 2 — Central Crypt ({@code Z = 13..24})</b>: Grand {@code 18}-wide octagonal/vaulted
 *       chamber with {@code 4} {@code cracked_barrow_bricks} pillars, {@code crypt_grave_root} snares across
 *       the floor, the central raised Dais holding {@code crypt_siphon_lantern} above a
 *       {@code polished_basalt} &amp; {@code blue_ice} altar, {@code 3} unlit
 *       {@code runed_brazier (lit=false)} blocks on corner pedestals,
 *       and {@code 1} skeleton/stray {@code spawner} beneath the central dais.</li>
 *   <li><b>Room 3 — Hidden Warden's Vault ({@code Z = 25..30})</b>: Sealed behind a {@code 2x3} breakable
 *       wall of {@code cracked_barrow_bricks} at the back of the Central Crypt, containing an Aegis-warded
 *       {@code aegis_quartz_block} / {@code barrow_stone_bricks} shrine, hanging {@code crypt_siphon_lantern}s,
 *       and the main Warden's {@code chests/structure_5} treasure chest.</li>
 * </ul>
 */
public class FrostTombPiece extends StructurePiece {

    public static final int WIDTH_X = 26;
    public static final int HEIGHT_Y = 15;
    public static final int LENGTH_Z = 32;

    private static final int[][] CRYPT_CRACKED_PILLARS = {
            {6, 14}, {19, 14}, {6, 22}, {19, 22}
    };

    private static final int[][] RUNED_BRAZIER_PEDESTALS = {
            {8, 15}, {18, 15}, {13, 23}
    };

    private static final int[][] CRYPT_GRAVE_ROOT_SNARES = {
            {6, 16}, {7, 18}, {6, 20}, {9, 19},
            {19, 16}, {18, 18}, {19, 20}, {16, 19},
            {10, 14}, {15, 14}, {10, 22}, {15, 22}
    };

    private static final int[][] ANTECHAMBER_MUSHROOM_SPOTS = {
            {8, 6}, {17, 6}, {8, 11}, {17, 11}, {9, 11}, {16, 11}
    };

    private static final int[][] KERB_STANDING_STONES = {
            {3, 6}, {22, 6}, {2, 16}, {23, 16}, {4, 26}, {21, 26}
    };

    private final int startX;
    private final int startY;
    private final int startZ;

    public FrostTombPiece(int centerX, int surfaceY, int centerZ) {
        // Exact 26 (X: 0..25) x 15 (Y: 0..14, embedded 4 blocks below surfaceY) x 32 (Z: 0..31)
        super(resolvePieceType(), 0, new BoundingBox(
                centerX - 13,
                surfaceY - 4,
                centerZ - 16,
                centerX + 12,
                surfaceY + 10,
                centerZ + 15
        ));
        this.startX = centerX - 13;
        this.startY = surfaceY - 4;
        this.startZ = centerZ - 16;
    }

    public FrostTombPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.startX = tag.getIntOr("StartX", this.boundingBox.minX());
        this.startY = tag.getIntOr("StartY", this.boundingBox.minY());
        this.startZ = tag.getIntOr("StartZ", this.boundingBox.minZ());
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.FROST_TOMB_OF_THE_AEGIS_WARDENS_PIECE.isPresent()) {
            return ModStructures.FROST_TOMB_OF_THE_AEGIS_WARDENS_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Entropica.MODID, "frost_tomb_of_the_aegis_wardens_piece");
        return BuiltInRegistries.STRUCTURE_PIECE.getOptional(id).orElseThrow();
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("StartX", this.startX);
        tag.putInt("StartY", this.startY);
        tag.putInt("StartZ", this.startZ);
    }

    public List<BlockPos> getRunedBrazierPositions() {
        return List.of(
                toWorld(RUNED_BRAZIER_PEDESTALS[0][0], 3, RUNED_BRAZIER_PEDESTALS[0][1]),
                toWorld(RUNED_BRAZIER_PEDESTALS[1][0], 3, RUNED_BRAZIER_PEDESTALS[1][1]),
                toWorld(RUNED_BRAZIER_PEDESTALS[2][0], 3, RUNED_BRAZIER_PEDESTALS[2][1])
        );
    }

    public BlockPos getSpawnerPos() {
        return toWorld(13, 1, 18);
    }

    public BlockPos getAntechamberWestChestPos() {
        return toWorld(7, 1, 8);
    }

    public BlockPos getAntechamberEastChestPos() {
        return toWorld(18, 1, 8);
    }

    public BlockPos getWardenVaultChestPos() {
        return toWorld(13, 2, 28);
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
        buildOuterEarthenBarrowMound(level, chunkBox);
        buildSarcophagusAntechamber(level, chunkBox, random);
        buildCentralHeartCrypt(level, chunkBox, random);
        buildHiddenWardensVault(level, chunkBox, random);
        buildRecessedTombPortalAndStandingStones(level, chunkBox);
    }

    private void buildOuterEarthenBarrowMound(WorldGenLevel level, BoundingBox chunkBox) {
        Block necroticSoilBlock = resolveBlock("necrotic_soil", Blocks.PODZOL);
        Block barrowMossBlock = resolveBlock("barrow_moss", Blocks.MOSS_BLOCK);
        Block graveMossBlock = resolveBlock("grave_moss", Blocks.MOSS_CARPET);
        Block barrowBricksBlock = resolveBlock("barrow_stone_bricks", Blocks.DEEPSLATE_BRICKS);

        BlockState necroticSoil = necroticSoilBlock.defaultBlockState();
        BlockState barrowMoss = barrowMossBlock.defaultBlockState();
        BlockState graveMoss = graveMossBlock.defaultBlockState();
        BlockState barrowBricks = barrowBricksBlock.defaultBlockState();
        BlockState coarseDirt = Blocks.COARSE_DIRT.defaultBlockState();
        BlockState snowBlock = Blocks.SNOW_BLOCK.defaultBlockState();
        BlockState snowLayer1 = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1);
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int lx = 0; lx < WIDTH_X; lx++) {
            for (int lz = 0; lz < LENGTH_Z; lz++) {
                // Clear headroom above surface (ly = 5..14) before sculpting the barrow dome
                for (int ly = 5; ly < HEIGHT_Y; ly++) {
                    setLocal(level, chunkBox, lx, ly, lz, air);
                }

                // Elliptical 26x32 mound profile centered around (12.5, 16.5), slightly taller over Central Crypt
                double nx = (lx - 12.5D) / 12.6D;
                double nz = (lz - 16.5D) / 15.3D;
                double normR = Math.hypot(nx, nz);

                if (normR > 1.02D) {
                    continue;
                }

                // Anchor downward into hillside/terrain so zero mound edges ever float over slopes or hollows
                boolean innerCore = (lx >= 3 && lx <= 22 && lz >= 3 && lz <= 30);
                fillFoundationDown(level, chunkBox, lx, innerCore ? -1 : 3, lz, innerCore ? barrowBricks : coarseDirt);

                double clampedR = Mth.clamp(normR, 0.0D, 1.0D);
                // Smooth cosine-bell dome rising from ly = 4 (surfaceY) at rim to ly = 14 (surfaceY + 10) at crest
                double domeFactor = Math.pow(Math.cos(clampedR * (Math.PI * 0.5D)), 0.85D);
                int moundTopY = Mth.clamp(4 + (int) Math.round(10.0D * domeFactor), 4, 14);

                for (int ly = 0; ly <= moundTopY; ly++) {
                    if (ly == 0) {
                        setLocal(level, chunkBox, lx, 0, lz, innerCore ? barrowBricks : coarseDirt);
                    } else if (ly < moundTopY) {
                        BlockState earthFill = (((lx * 5 + ly + lz * 3) & 3) == 0) ? coarseDirt : necroticSoil;
                        setLocal(level, chunkBox, lx, ly, lz, earthFill);
                    } else {
                        // Mound top crust: wind-drifted snow_block mantle mottled with necrotic_soil & barrow_moss
                        int hash = (lx * 7 + lz * 13) & 7;
                        BlockState topCrust;
                        if (moundTopY >= 10 && hash <= 4) {
                            topCrust = snowBlock;
                        } else if (hash == 0) {
                            topCrust = barrowMoss;
                        } else if (hash <= 3) {
                            topCrust = snowBlock;
                        } else {
                            topCrust = necroticSoil;
                        }
                        setLocal(level, chunkBox, lx, ly, lz, topCrust);
                    }
                }

                // Surface grave_moss patches & drifted snow layers on mound slopes (below ly = 14 ceiling)
                if (moundTopY < 14) {
                    int accentHash = (lx * 11 + lz * 5) & 7;
                    if (accentHash == 1 || accentHash == 4) {
                        placeLocalIfSurvives(level, chunkBox, lx, moundTopY + 1, lz, graveMoss);
                    } else if (accentHash == 2 || accentHash == 6) {
                        placeLocalIfSurvives(level, chunkBox, lx, moundTopY + 1, lz, snowLayer1);
                    }
                }
            }
        }
    }

    private void buildRecessedTombPortalAndStandingStones(WorldGenLevel level, BoundingBox chunkBox) {
        Block barrowBricksBlock = resolveBlock("barrow_stone_bricks", Blocks.DEEPSLATE_BRICKS);
        Block crackedBarrowBlock = resolveBlock("cracked_barrow_bricks", Blocks.CRACKED_DEEPSLATE_BRICKS);
        Block mossyBarrowBlock = resolveBlock("mossy_barrow_bricks", Blocks.MOSSY_STONE_BRICKS);
        Block sentinelTotemBlock = resolveBlock("barrow_sentinel_totem", Blocks.CHISELED_DEEPSLATE);
        Block necroticSoilBlock = resolveBlock("necrotic_soil", Blocks.PODZOL);
        Block graveMossBlock = resolveBlock("grave_moss", Blocks.MOSS_CARPET);

        BlockState barrowBricks = barrowBricksBlock.defaultBlockState();
        BlockState crackedBricks = crackedBarrowBlock.defaultBlockState();
        BlockState mossyBricks = mossyBarrowBlock.defaultBlockState();
        BlockState totemNorth = withHorizontalFacing(sentinelTotemBlock.defaultBlockState(), Direction.NORTH);
        BlockState polishedBasaltY = Blocks.POLISHED_BASALT.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState polishedBasaltX = Blocks.POLISHED_BASALT.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState basaltY = Blocks.BASALT.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState necroticSoil = necroticSoilBlock.defaultBlockState();
        BlockState graveMoss = graveMossBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Carve Recessed Tomb Forecourt & Stepped Descending Portal Trench (Z = 0..4, X = 9..16)
        for (int lz = 0; lz <= 4; lz++) {
            int wallTopY = (lz <= 1) ? 5 : ((lz == 2) ? 6 : 7);
            for (int lx = 9; lx <= 16; lx++) {
                fillFoundationDown(level, chunkBox, lx, 0, lz, barrowBricks);
                setLocal(level, chunkBox, lx, 0, lz, barrowBricks);

                boolean isCheekWall = (lx == 9 || lx == 16);
                if (isCheekWall) {
                    for (int ly = 1; ly <= wallTopY; ly++) {
                        BlockState cheekState = (lz >= 3 || ly == wallTopY)
                                ? polishedBasaltY
                                : (((ly + lz) & 1) == 0 ? barrowBricks : mossyBricks);
                        setLocal(level, chunkBox, lx, ly, lz, cheekState);
                    }
                    // Clear mound soil above outer forecourt cheek walls (Z = 0..2) so portal opens cleanly to sky
                    if (lz <= 2) {
                        for (int ly = wallTopY + 1; ly < HEIGHT_Y; ly++) {
                            setLocal(level, chunkBox, lx, ly, lz, air);
                        }
                    }
                } else {
                    // Clear open sky over forecourt steps (Z = 0..2) and vaulted portal tunnel (Z = 3..4)
                    int clearTop = (lz <= 2) ? (HEIGHT_Y - 1) : 5;
                    for (int ly = 1; ly <= clearTop; ly++) {
                        setLocal(level, chunkBox, lx, ly, lz, air);
                    }
                }
            }
        }

        // 2. Heavy Polished Basalt & Barrow Stone Bricks Portal Arch at Z = 3..4
        for (int lz = 3; lz <= 4; lz++) {
            for (int ly = 1; ly <= 6; ly++) {
                setLocal(level, chunkBox, 10, ly, lz, (lz == 3 && ly == 4) ? totemNorth : polishedBasaltY);
                setLocal(level, chunkBox, 15, ly, lz, (lz == 3 && ly == 4) ? totemNorth : polishedBasaltY);
            }
            // Heavy stone & basalt lintel beam across X = 10..15 at Y = 6..7
            for (int lx = 10; lx <= 15; lx++) {
                BlockState lintel = (lz == 3 && (lx == 12 || lx == 13)) ? chiseledDeepslate : polishedBasaltX;
                setLocal(level, chunkBox, lx, 6, lz, lintel);
                setLocal(level, chunkBox, lx, 7, lz, barrowBricks);
            }
            // Corbel arch shoulders under the lintel at X = 11 and X = 14 (Y = 5)
            setLocal(level, chunkBox, 11, 5, lz, stair(Direction.WEST, Half.TOP));
            setLocal(level, chunkBox, 14, 5, lz, stair(Direction.EAST, Half.TOP));
        }

        // Central Barrow Sentinel Totem keystone above the outer portal arch at (12..13, 7, 3)
        setLocal(level, chunkBox, 12, 7, 3, totemNorth);
        setLocal(level, chunkBox, 13, 7, 3, totemNorth);

        // 3. Broad Stone Steps descending from surface threshold (Z = 0, Y = 4) to Antechamber level (Z = 4, Y = 1)
        for (int lx = 10; lx <= 15; lx++) {
            // Z = 0: Surface stone threshold
            setLocal(level, chunkBox, lx, 1, 0, barrowBricks);
            setLocal(level, chunkBox, lx, 2, 0, barrowBricks);
            setLocal(level, chunkBox, lx, 3, 0, barrowBricks);
            setLocal(level, chunkBox, lx, 4, 0, (lx == 10 || lx == 15) ? polishedBasaltY : mossyBricks);
        }
        for (int lx = 11; lx <= 14; lx++) {
            // Z = 1: Step at Y = 4 descending South (FACING = NORTH)
            setLocal(level, chunkBox, lx, 1, 1, barrowBricks);
            setLocal(level, chunkBox, lx, 2, 1, barrowBricks);
            setLocal(level, chunkBox, lx, 3, 1, barrowBricks);
            setLocal(level, chunkBox, lx, 4, 1, stair(Direction.NORTH, Half.BOTTOM));

            // Z = 2: Step at Y = 3 descending South
            setLocal(level, chunkBox, lx, 1, 2, barrowBricks);
            setLocal(level, chunkBox, lx, 2, 2, barrowBricks);
            setLocal(level, chunkBox, lx, 3, 2, stair(Direction.NORTH, Half.BOTTOM));

            // Z = 3: Step at Y = 2 descending South under the heavy portal lintel
            setLocal(level, chunkBox, lx, 1, 3, barrowBricks);
            setLocal(level, chunkBox, lx, 2, 3, stair(Direction.NORTH, Half.BOTTOM));

            // Z = 4: Tomb threshold floor at Y = 1
            setLocal(level, chunkBox, lx, 1, 4, (lx == 12 || lx == 13) ? chiseledDeepslate : barrowBricks);
        }
        // Flanking stepped cheek ledges at X = 10 & X = 15 along Z = 1..2
        for (int sideX : new int[]{10, 15}) {
            setLocal(level, chunkBox, sideX, 1, 1, barrowBricks);
            setLocal(level, chunkBox, sideX, 2, 1, barrowBricks);
            setLocal(level, chunkBox, sideX, 3, 1, barrowBricks);
            setLocal(level, chunkBox, sideX, 4, 1, polishedBasaltY);
            setLocal(level, chunkBox, sideX, 1, 2, barrowBricks);
            setLocal(level, chunkBox, sideX, 2, 2, barrowBricks);
            setLocal(level, chunkBox, sideX, 3, 2, polishedBasaltY);
        }

        // 4. Weathered Basalt & Barrow Standing Stones flanking the North Tomb Entrance (X = 6 & X = 19, Z = 1)
        for (int menhirX : new int[]{6, 19}) {
            fillFoundationDown(level, chunkBox, menhirX, 3, 1, basaltY);
            setLocal(level, chunkBox, menhirX, 4, 1, polishedBasaltY);
            setLocal(level, chunkBox, menhirX, 5, 1, barrowBricks);
            setLocal(level, chunkBox, menhirX, 6, 1, polishedBasaltY);
            setLocal(level, chunkBox, menhirX, 7, 1, crackedBricks);
            setLocal(level, chunkBox, menhirX, 8, 1, totemNorth);

            // Basal buttress stones & grave-moss collar
            setLocal(level, chunkBox, menhirX, 4, 2, mossyBricks);
            setLocal(level, chunkBox, menhirX, 5, 2, basaltY);
            int outX = menhirX < 13 ? menhirX - 1 : menhirX + 1;
            setLocal(level, chunkBox, outX, 4, 1, necroticSoil);
            placeLocalIfSurvives(level, chunkBox, outX, 5, 1, graveMoss);
        }

        // Perimeter kerb monoliths ringing the earthen barrow mound
        for (int[] kerb : KERB_STANDING_STONES) {
            int kx = kerb[0];
            int kz = kerb[1];
            fillFoundationDown(level, chunkBox, kx, 3, kz, basaltY);
            setLocal(level, chunkBox, kx, 4, kz, basaltY);
            setLocal(level, chunkBox, kx, 5, kz, mossyBricks);
            setLocal(level, chunkBox, kx, 6, kz, crackedBricks);
        }
    }

    private void buildSarcophagusAntechamber(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block barrowBricksBlock = resolveBlock("barrow_stone_bricks", Blocks.DEEPSLATE_BRICKS);
        Block crackedBarrowBlock = resolveBlock("cracked_barrow_bricks", Blocks.CRACKED_DEEPSLATE_BRICKS);
        Block mossyBarrowBlock = resolveBlock("mossy_barrow_bricks", Blocks.MOSSY_STONE_BRICKS);
        Block necroticSoilBlock = resolveBlock("necrotic_soil", Blocks.PODZOL);
        Block rimeCapBlock = resolveBlock("rime_cap_mushroom", Blocks.BROWN_MUSHROOM);

        BlockState barrowBricks = barrowBricksBlock.defaultBlockState();
        BlockState crackedBricks = crackedBarrowBlock.defaultBlockState();
        BlockState mossyBricks = mossyBarrowBlock.defaultBlockState();
        BlockState necroticSoil = necroticSoilBlock.defaultBlockState();
        BlockState rimeCap = rimeCapBlock.defaultBlockState();
        BlockState polishedBasaltY = Blocks.POLISHED_BASALT.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState sarcophagusSlab = Blocks.DEEPSLATE_BRICK_SLAB.defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.BOTTOM)
                .setValue(SlabBlock.WATERLOGGED, false);
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Vaulted Barrow Stone Bricks & Mossy Barrow Bricks Hall (Z = 5..12, X = 6..19, Y = 1..7)
        for (int lz = 5; lz <= 12; lz++) {
            boolean isVaultRib = (lz == 5 || lz == 8 || lz == 12);
            for (int lx = 6; lx <= 19; lx++) {
                setLocal(level, chunkBox, lx, 0, lz, barrowBricks);

                // Floor at Y = 1
                boolean centerRunner = (lx >= 11 && lx <= 14);
                BlockState floorTile = centerRunner
                        ? (((lx + lz) & 1) == 0 ? barrowBricks : chiseledDeepslate)
                        : (((lx * 3 + lz) & 3) == 0 ? necroticSoil : mossyBricks);
                setLocal(level, chunkBox, lx, 1, lz, floorTile);

                // Vaulted ceiling arch profile: side walls at X = 6, 19 (Y = 2..5), stepping in to Y = 7 crown
                for (int ly = 2; ly <= 7; ly++) {
                    boolean isOuterWall = (lx == 6 || lx == 19)
                            || (lz == 5 && (lx < 11 || lx > 14))
                            || (ly == 6 && (lx <= 7 || lx >= 18))
                            || (ly == 7);
                    if (isOuterWall) {
                        BlockState masonry = (isVaultRib && (lx == 6 || lx == 19 || ly == 7))
                                ? polishedBasaltY
                                : pickTombWall(lx, ly, lz, barrowBricks, mossyBricks, crackedBricks);
                        setLocal(level, chunkBox, lx, ly, lz, masonry);
                    } else {
                        setLocal(level, chunkBox, lx, ly, lz, air);
                    }
                }
            }
        }

        // Ensure open vaulted portal entry at Z = 5 (X = 11..14, Y = 2..5)
        for (int lx = 11; lx <= 14; lx++) {
            for (int ly = 2; ly <= 5; ly++) {
                if ((lx == 11 || lx == 14) && ly == 5) {
                    setLocal(level, chunkBox, lx, ly, 5,
                            stair(lx == 11 ? Direction.WEST : Direction.EAST, Half.TOP));
                } else {
                    setLocal(level, chunkBox, lx, ly, 5, air);
                }
            }
        }

        // 2. Sarcophagus 1 (West Side Alcove, X = 7, Z = 7..9) with STRUCTURE_5_CHEST_LOOT under slab lid
        setLocal(level, chunkBox, 6, 2, 8, chiseledDeepslate);
        setLocal(level, chunkBox, 6, 3, 8, chiseledDeepslate);
        setLocal(level, chunkBox, 7, 1, 7, mossyBricks);
        setLocal(level, chunkBox, 7, 1, 9, mossyBricks);
        placeTombChest(level, chunkBox, random, 7, 1, 8, Direction.EAST);
        for (int sz = 7; sz <= 9; sz++) {
            setLocal(level, chunkBox, 7, 2, sz, sarcophagusSlab);
        }

        // 3. Sarcophagus 2 (East Side Alcove, X = 18, Z = 7..9) with STRUCTURE_5_CHEST_LOOT under slab lid
        setLocal(level, chunkBox, 19, 2, 8, chiseledDeepslate);
        setLocal(level, chunkBox, 19, 3, 8, chiseledDeepslate);
        setLocal(level, chunkBox, 18, 1, 7, mossyBricks);
        setLocal(level, chunkBox, 18, 1, 9, mossyBricks);
        placeTombChest(level, chunkBox, random, 18, 1, 8, Direction.WEST);
        for (int sz = 7; sz <= 9; sz++) {
            setLocal(level, chunkBox, 18, 2, sz, sarcophagusSlab);
        }

        // 4. Sarcophagus 3 (Central Honor Sarcophagus Plinth, X = 12..13, Z = 8..10)
        for (int sx = 12; sx <= 13; sx++) {
            setLocal(level, chunkBox, sx, 1, 8, chiseledDeepslate);
            setLocal(level, chunkBox, sx, 1, 9, barrowBricks);
            setLocal(level, chunkBox, sx, 1, 10, chiseledDeepslate);
            setLocal(level, chunkBox, sx, 2, 8, stair(Direction.NORTH, Half.BOTTOM));
            setLocal(level, chunkBox, sx, 2, 9, sarcophagusSlab);
            setLocal(level, chunkBox, sx, 2, 10, stair(Direction.SOUTH, Half.BOTTOM));
        }

        // 5. Damp wall Rime-Cap Mushroom clusters & Soul Lanterns
        for (int[] spot : ANTECHAMBER_MUSHROOM_SPOTS) {
            setLocal(level, chunkBox, spot[0], 1, spot[1], necroticSoil);
            placeLocalFlora(level, chunkBox, spot[0], 2, spot[1], rimeCap);
        }

        BlockState hangingSoulLantern = Blocks.SOUL_LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false);
        setLocal(level, chunkBox, 10, 6, 6, hangingSoulLantern);
        setLocal(level, chunkBox, 15, 6, 11, hangingSoulLantern);
    }

    private void buildCentralHeartCrypt(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block barrowBricksBlock = resolveBlock("barrow_stone_bricks", Blocks.DEEPSLATE_BRICKS);
        Block crackedBarrowBlock = resolveBlock("cracked_barrow_bricks", Blocks.CRACKED_DEEPSLATE_BRICKS);
        Block mossyBarrowBlock = resolveBlock("mossy_barrow_bricks", Blocks.MOSSY_STONE_BRICKS);
        Block necroticSoilBlock = resolveBlock("necrotic_soil", Blocks.PODZOL);
        Block graveRootBlock = resolveBlock("crypt_grave_root", Blocks.DEAD_BUSH);
        Block centralLanternBlock = resolveBlock("crypt_siphon_lantern", Blocks.CRYING_OBSIDIAN);
        Block brazierBlock = resolveBlock("runed_brazier", Blocks.SOUL_CAMPFIRE);

        BlockState barrowBricks = barrowBricksBlock.defaultBlockState();
        BlockState crackedBricks = crackedBarrowBlock.defaultBlockState();
        BlockState mossyBricks = mossyBarrowBlock.defaultBlockState();
        BlockState necroticSoil = necroticSoilBlock.defaultBlockState();
        BlockState graveRoot = graveRootBlock.defaultBlockState();
        BlockState centralLantern = withBooleanProperty(
                withBooleanProperty(centralLanternBlock.defaultBlockState(), "hanging", false),
                "waterlogged", false);
        BlockState unlitBrazier = withBooleanProperty(brazierBlock.defaultBlockState(), "lit", false);
        unlitBrazier = withBooleanProperty(unlitBrazier, "waterlogged", false);

        BlockState polishedBasaltY = Blocks.POLISHED_BASALT.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Grand 18-Wide Octagonal/Vaulted Crypt Shell (X = 4..21 -> 18 wide, Z = 13..24, Y = 1..10)
        for (int lz = 13; lz <= 24; lz++) {
            for (int lx = 3; lx <= 22; lx++) {
                // Octagonal corner bevels at the 4 corners of the crypt
                int cornerTrim = Math.max(0, 15 - lz) + Math.max(0, lz - 22);
                int minWallX = 3 + cornerTrim;
                int maxWallX = 22 - cornerTrim;
                if (lx < minWallX || lx > maxWallX) {
                    continue;
                }

                setLocal(level, chunkBox, lx, 0, lz, barrowBricks);

                boolean isPerimeterWall = (lx == minWallX || lx == maxWallX || lz == 13 || lz == 24);
                // Floor at Y = 1
                boolean onAltarWalkway = (lx >= 11 && lx <= 14) || (lz >= 17 && lz <= 19);
                BlockState floorState = isPerimeterWall ? barrowBricks
                        : (onAltarWalkway ? barrowBricks
                        : ((((lx + lz) & 3) == 0) ? necroticSoil : mossyBricks));
                setLocal(level, chunkBox, lx, 1, lz, floorState);

                // Vaulted octagonal dome ceiling peaking at Y = 10
                for (int ly = 2; ly <= 10; ly++) {
                    int domeInset = (ly <= 6) ? 0 : (ly - 6);
                    boolean isShell = isPerimeterWall
                            || (lx <= minWallX + domeInset)
                            || (lx >= maxWallX - domeInset)
                            || (ly == 10);
                    if (isShell) {
                        boolean structuralRib = (lx == 12 || lx == 13 || lz == 18);
                        BlockState shellBlock = structuralRib && ly >= 6
                                ? polishedBasaltY
                                : pickTombWall(lx, ly, lz, barrowBricks, mossyBricks, crackedBricks);
                        setLocal(level, chunkBox, lx, ly, lz, shellBlock);
                    } else {
                        setLocal(level, chunkBox, lx, ly, lz, air);
                    }
                }
            }
        }

        // Carve grand connecting archway from Room 1 into Room 2 across Z = 12..13 (X = 10..15, Y = 2..5)
        for (int lz = 12; lz <= 13; lz++) {
            for (int lx = 10; lx <= 15; lx++) {
                for (int ly = 2; ly <= 5; ly++) {
                    if ((lx == 10 || lx == 15) && ly <= 5) {
                        setLocal(level, chunkBox, lx, ly, lz, polishedBasaltY);
                    } else if ((lx == 11 || lx == 14) && ly == 5) {
                        setLocal(level, chunkBox, lx, ly, lz,
                                stair(lx == 11 ? Direction.WEST : Direction.EAST, Half.TOP));
                    } else {
                        setLocal(level, chunkBox, lx, ly, lz, air);
                    }
                }
            }
        }

        // 2. Four Cracked Barrow Bricks Pillars supporting the Central Crypt Vault
        for (int[] p : CRYPT_CRACKED_PILLARS) {
            int px = p[0];
            int pz = p[1];
            setLocal(level, chunkBox, px, 1, pz, chiseledDeepslate);
            for (int ly = 2; ly <= 8; ly++) {
                BlockState pillarBlock = (ly == 2 || ly == 8) ? barrowBricks : crackedBricks;
                setLocal(level, chunkBox, px, ly, pz, pillarBlock);
            }
        }

        // 3. Crypt-Grave Root Snares across necrotic_soil floor tiles
        for (int[] snare : CRYPT_GRAVE_ROOT_SNARES) {
            setLocal(level, chunkBox, snare[0], 1, snare[1], necroticSoil);
            placeLocalFlora(level, chunkBox, snare[0], 2, snare[1], graveRoot);
        }

        // 4. Central Raised Dais (X = 11..14, Z = 17..20), Spawner underneath (13, 1, 18),
        //    Polished Basalt & Blue Ice Altar (Y = 2), and Central Crypt-Siphon Lantern (13, 4, 18)
        for (int dx = 11; dx <= 14; dx++) {
            for (int dz = 17; dz <= 20; dz++) {
                boolean isOuterStep = (dx == 11 || dx == 14 || dz == 17 || dz == 20);
                if (isOuterStep) {
                    setLocal(level, chunkBox, dx, 1, dz, barrowBricks);
                    Direction stepFace = (dz == 17) ? Direction.SOUTH
                            : ((dz == 20) ? Direction.NORTH
                            : ((dx == 11) ? Direction.EAST : Direction.WEST));
                    setLocal(level, chunkBox, dx, 2, dz, stair(stepFace, Half.BOTTOM));
                } else {
                    // Inner 2x2 altar core (X = 12..13, Z = 18..19): Blue Ice & Polished Basalt top at Y = 2
                    if (dx == 13 && dz == 18) {
                        // Skeleton/Stray spawner directly beneath the central altar heart!
                        placeUndeadSpawner(level, chunkBox, random, 13, 1, 18);
                        setLocal(level, chunkBox, 13, 2, 18, blueIce);
                    } else if (dx == 12 && dz == 19) {
                        setLocal(level, chunkBox, dx, 1, dz, polishedBasaltY);
                        setLocal(level, chunkBox, dx, 2, dz, blueIce);
                    } else {
                        setLocal(level, chunkBox, dx, 1, dz, polishedBasaltY);
                        setLocal(level, chunkBox, dx, 2, dz, polishedBasaltY);
                    }
                }
            }
        }
        // Iron-bar spawner vents in the dais sides so skeletons/strays sense players & spawn freely
        setLocal(level, chunkBox, 13, 1, 17, Blocks.IRON_BARS.defaultBlockState()
                .setValue(IronBarsBlock.EAST, true).setValue(IronBarsBlock.WEST, true));
        setLocal(level, chunkBox, 14, 1, 18, Blocks.IRON_BARS.defaultBlockState()
                .setValue(IronBarsBlock.NORTH, true).setValue(IronBarsBlock.SOUTH, true));

        // Place Chiseled Deepslate plinth at (13, 3, 18) and Crypt-Siphon Lantern on the central dais at (13, 4, 18)
        setLocal(level, chunkBox, 13, 3, 18, chiseledDeepslate);
        setLocal(level, chunkBox, 13, 4, 18, centralLantern);
        scheduleLocalTick(level, chunkBox, 13, 4, 18, centralLanternBlock);

        // 5. Three Unlit Runed Braziers (lit=false) on Pedestals within 5.1-5.9 blocks of (13, 4, 18)
        for (int[] ped : RUNED_BRAZIER_PEDESTALS) {
            int bx = ped[0];
            int bz = ped[1];
            setLocal(level, chunkBox, bx, 1, bz, polishedBasaltY);
            setLocal(level, chunkBox, bx, 2, bz, chiseledDeepslate);
            setLocal(level, chunkBox, bx, 3, bz, unlitBrazier);
            setLocal(level, chunkBox, bx, 4, bz, air);
        }
    }

    private void buildHiddenWardensVault(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block barrowBricksBlock = resolveBlock("barrow_stone_bricks", Blocks.DEEPSLATE_BRICKS);
        Block crackedBarrowBlock = resolveBlock("cracked_barrow_bricks", Blocks.CRACKED_DEEPSLATE_BRICKS);
        Block aegisQuartzBlock = resolveBlock("aegis_quartz_block", Blocks.QUARTZ_BLOCK);
        Block aegisClusterBlock = resolveBlock("aegis_quartz_cluster", Blocks.AMETHYST_CLUSTER);
        Block siphonLanternBlock = resolveBlock("crypt_siphon_lantern", Blocks.SOUL_LANTERN);
        Block sentinelTotemBlock = resolveBlock("barrow_sentinel_totem", Blocks.CHISELED_DEEPSLATE);

        BlockState barrowBricks = barrowBricksBlock.defaultBlockState();
        BlockState crackedBricks = crackedBarrowBlock.defaultBlockState();
        BlockState aegisQuartz = aegisQuartzBlock.defaultBlockState();
        BlockState aegisClusterUp = withUpFacing(aegisClusterBlock.defaultBlockState());
        BlockState hangingSiphonLantern = makeHangingLantern(siphonLanternBlock);
        BlockState totemNorth = withHorizontalFacing(sentinelTotemBlock.defaultBlockState(), Direction.NORTH);
        BlockState polishedBasaltY = Blocks.POLISHED_BASALT.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Sealed 2x3 Breakable Cracked Barrow Bricks Wall at Z = 24 (X = 12..13, Y = 2..4)
        //    framed with Chiseled Deepslate & Polished Basalt so keen-eyed tomb raiders spot the fracture!
        for (int ly = 2; ly <= 5; ly++) {
            setLocal(level, chunkBox, 11, ly, 24, (ly == 5) ? chiseledDeepslate : polishedBasaltY);
            setLocal(level, chunkBox, 14, ly, 24, (ly == 5) ? chiseledDeepslate : polishedBasaltY);
        }
        setLocal(level, chunkBox, 12, 5, 24, chiseledDeepslate);
        setLocal(level, chunkBox, 13, 5, 24, chiseledDeepslate);
        setLocal(level, chunkBox, 12, 1, 24, barrowBricks);
        setLocal(level, chunkBox, 13, 1, 24, barrowBricks);
        for (int lx = 12; lx <= 13; lx++) {
            for (int ly = 2; ly <= 4; ly++) {
                setLocal(level, chunkBox, lx, ly, 24, crackedBricks);
            }
        }

        // 2. Hidden Warden's Vault Chamber (Z = 25..30, X = 9..16, Y = 1..6)
        for (int lz = 25; lz <= 30; lz++) {
            for (int lx = 9; lx <= 16; lx++) {
                setLocal(level, chunkBox, lx, 0, lz, barrowBricks);
                boolean isWall = (lx == 9 || lx == 16 || lz == 30);

                // Floor inlaid with Aegis Quartz Block & Barrow Stone Bricks
                boolean aegisInlay = !isWall && ((lx == 12 || lx == 13) || (lz == 27 && lx >= 11 && lx <= 14));
                setLocal(level, chunkBox, lx, 1, lz, aegisInlay ? aegisQuartz : barrowBricks);

                for (int ly = 2; ly <= 6; ly++) {
                    boolean isVaultShell = isWall
                            || (ly == 5 && (lx == 10 || lx == 15))
                            || (ly == 6);
                    if (isVaultShell) {
                        boolean aegisRib = (ly == 6 && (lx == 12 || lx == 13))
                                || (lz == 30 && (lx == 12 || lx == 13) && ly <= 4);
                        setLocal(level, chunkBox, lx, ly, lz, aegisRib ? aegisQuartz : barrowBricks);
                    } else {
                        setLocal(level, chunkBox, lx, ly, lz, air);
                    }
                }
            }
        }

        // 3. Aegis-Warded Shrine Dais, Sentinel Totems, Hanging Crypt-Siphon Lanterns & Main Warden's Treasure Chest
        setLocal(level, chunkBox, 12, 2, 28, aegisQuartz);
        setLocal(level, chunkBox, 13, 1, 28, aegisQuartz);
        setLocal(level, chunkBox, 11, 2, 28, stair(Direction.EAST, Half.BOTTOM));
        setLocal(level, chunkBox, 14, 2, 28, stair(Direction.WEST, Half.BOTTOM));

        // Main Warden's Treasure Chest at (13, 2, 28) facing NORTH toward the vault entrance
        setLocal(level, chunkBox, 13, 1, 27, aegisQuartz);
        setLocal(level, chunkBox, 12, 2, 28, chiseledDeepslate);
        setLocal(level, chunkBox, 13, 1, 28, aegisQuartz);
        placeTombChest(level, chunkBox, random, 13, 2, 28, Direction.NORTH);

        // Flanking Aegis Quartz Clusters & Barrow Sentinel Totems
        setLocal(level, chunkBox, 10, 2, 28, aegisQuartz);
        placeLocalIfSurvives(level, chunkBox, 10, 3, 28, aegisClusterUp);
        setLocal(level, chunkBox, 15, 2, 28, aegisQuartz);
        placeLocalIfSurvives(level, chunkBox, 15, 3, 28, aegisClusterUp);

        setLocal(level, chunkBox, 11, 2, 29, polishedBasaltY);
        setLocal(level, chunkBox, 11, 3, 29, totemNorth);
        setLocal(level, chunkBox, 14, 2, 29, polishedBasaltY);
        setLocal(level, chunkBox, 14, 3, 29, totemNorth);

        // Hanging Crypt-Siphon Lanterns overhead at (12, 5, 27) and (13, 5, 27)
        setLocal(level, chunkBox, 12, 6, 27, aegisQuartz);
        setLocal(level, chunkBox, 13, 6, 27, aegisQuartz);
        setLocal(level, chunkBox, 12, 5, 27, hangingSiphonLantern);
        setLocal(level, chunkBox, 13, 5, 27, hangingSiphonLantern);
    }

    private void placeTombChest(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random,
            int lx,
            int ly,
            int lz,
            Direction facing
    ) {
        BlockPos pos = toWorld(lx, ly, lz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState chestState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, facing)
                .setValue(ChestBlock.WATERLOGGED, false);
        level.setBlock(pos, chestState, 2);
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.STRUCTURE_5_CHEST_LOOT);
    }

    private void placeUndeadSpawner(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random,
            int lx,
            int ly,
            int lz
    ) {
        BlockPos pos = toWorld(lx, ly, lz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SpawnerBlockEntity spawner) {
            EntityType<?> undeadType = random.nextBoolean() ? EntityType.STRAY : EntityType.SKELETON;
            spawner.setEntityId(undeadType, random);
        }
    }

    private void placeLocalFlora(WorldGenLevel level, BoundingBox chunkBox, int lx, int ly, int lz, BlockState flora) {
        BlockPos pos = toWorld(lx, ly, lz);
        if (!chunkBox.isInside(pos) || !level.getBlockState(pos).isAir()) {
            return;
        }
        if (flora.canSurvive(level, pos)) {
            level.setBlock(pos, flora, 2);
        } else {
            BlockPos below = pos.below();
            level.setBlock(below, Blocks.COARSE_DIRT.defaultBlockState(), 2);
            if (flora.canSurvive(level, pos)) {
                level.setBlock(pos, flora, 2);
            }
        }
    }

    private void placeLocalIfSurvives(WorldGenLevel level, BoundingBox chunkBox, int lx, int ly, int lz, BlockState state) {
        BlockPos pos = toWorld(lx, ly, lz);
        if (chunkBox.isInside(pos) && level.getBlockState(pos).isAir() && state.canSurvive(level, pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void scheduleLocalTick(WorldGenLevel level, BoundingBox chunkBox, int lx, int ly, int lz, Block block) {
        BlockPos pos = toWorld(lx, ly, lz);
        if (chunkBox.isInside(pos)) {
            level.scheduleTick(pos, block, 2);
        }
    }

    private BlockPos toWorld(int lx, int ly, int lz) {
        return new BlockPos(this.startX + lx, this.startY + ly, this.startZ + lz);
    }

    private void setLocal(WorldGenLevel level, BoundingBox chunkBox, int lx, int ly, int lz, BlockState state) {
        BlockPos pos = toWorld(lx, ly, lz);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void fillFoundationDown(WorldGenLevel level, BoundingBox chunkBox, int lx, int startLy, int lz, BlockState state) {
        int wx = this.startX + lx;
        int wz = this.startZ + lz;
        if (wx < chunkBox.minX() || wx > chunkBox.maxX() || wz < chunkBox.minZ() || wz > chunkBox.maxZ()) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int topWy = this.startY + startLy;
        int minAllowedY = Math.max(level.getMinY() + 2, topWy - 20);
        for (int wy = topWy; wy >= minAllowedY; wy--) {
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

    private static BlockState pickTombWall(
            int lx,
            int ly,
            int lz,
            BlockState barrowBricks,
            BlockState mossyBricks,
            BlockState crackedBricks
    ) {
        int h = (lx * 7 + ly * 13 + lz * 5) & 7;
        if (h == 0 || h == 1) {
            return mossyBricks;
        }
        if (h == 2) {
            return crackedBricks;
        }
        return barrowBricks;
    }

    private static BlockState stair(Direction facing, Half half) {
        return Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half)
                .setValue(StairBlock.WATERLOGGED, false);
    }

    private static BlockState makeHangingLantern(Block block) {
        BlockState state = block.defaultBlockState();
        state = withBooleanProperty(state, "hanging", true);
        if (state.hasProperty(LanternBlock.HANGING)) {
            state = state.setValue(LanternBlock.HANGING, true);
        }
        state = withBooleanProperty(state, "waterlogged", false);
        return state;
    }

    private static BlockState withUpFacing(BlockState state) {
        if (state.hasProperty(AmethystClusterBlock.FACING)) {
            state = state.setValue(AmethystClusterBlock.FACING, Direction.UP);
        } else if (state.hasProperty(BlockStateProperties.FACING)) {
            state = state.setValue(BlockStateProperties.FACING, Direction.UP);
        }
        return withBooleanProperty(state, "waterlogged", false);
    }

    private static BlockState withHorizontalFacing(BlockState state, Direction dir) {
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.setValue(HorizontalDirectionalBlock.FACING, dir);
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, dir);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.setValue(BlockStateProperties.FACING, dir);
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
}
