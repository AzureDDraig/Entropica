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
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import java.util.Optional;

/**
 * Programmatic 3D generator for Biome #13's core-free landmark:
 * {@code Fata Morgana Mirage Arch-Sanctuary} ({@code 22 x 22 x 20},
 * {@code dx = -11..10, dy = 0..19, dz = -11..10} — strictly ZERO Shrine Cores or Anchors):
 * <ul>
 *   <li><b>1. Central Glacier Courtyard &amp; Sunken Oasis Mirror-Pool ({@code dy = 0..3})</b>:
 *       {@link Blocks#BLUE_ICE} and {@code entropica:mirage_ice} courtyard cradling a sunken unfrozen
 *       {@link Blocks#WATER} oasis mirror-pool ({@code dy = 2}) edged in {@code entropica:glass_sandstone}
 *       &amp; {@code entropica:glass_sandstone_slab}.</li>
 *   <li><b>2. Sub-Glacier Refraction Alcove ({@code dx = -2..2, dz = -8..-4, dy = 1..3})</b>:
 *       Walkable crystal grotto descending beside the North side of the mirror-pool with a
 *       {@code entropica:mirage_glass} underwater window, refractive skylight ceiling, and tucked
 *       reliquary treasure chest ({@code entropica:chests/fata_morgana_sanctuary}).</li>
 *   <li><b>3. Flanked East &amp; West Desert-Glacier Dunes ({@code dy = 0..6})</b>:
 *       Sculpted {@code entropica:glass_sandstone}-backed {@code entropica:glass_sand} dunes sprouting
 *       {@code entropica:vitreous_cactus} pillars on isolated sand crests.</li>
 *   <li><b>4. Sweeping Intersecting 14-Block-High Parabolic Bridge-Arch ({@code dy = 4..17})</b>:
 *       Intersecting N-S &amp; E-W parabolic arches of seamless {@code entropica:mirage_ice},
 *       translucent {@code entropica:mirage_glass}, and {@code entropica:mirage_glass_pane} fins,
 *       with phasic {@code mirage_ice} sky-steps ascending smoothly ({@code +1} block per step) to the
 *       arch-crown treasure chest at {@code (0, 18, 0)}, lit and shielded from heat-haze by {@code 4}
 *       {@code entropica:mirage_prism_lantern}s!</li>
 * </ul>
 */
public class FataMorganaSanctuaryPiece extends StructurePiece {

    private final int originX;
    private final int baseY;
    private final int originZ;

    public FataMorganaSanctuaryPiece(int originX, int baseY, int originZ) {
        // Exact 22 (X: -11..+10) x 20 (Y: 0..+19) x 22 (Z: -11..+10) bounding box
        super(resolvePieceType(), 0, new BoundingBox(
                originX - 11,
                baseY,
                originZ - 11,
                originX + 10,
                baseY + 19,
                originZ + 10
        ));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public FataMorganaSanctuaryPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", this.boundingBox.minX() + 11);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY());
        this.originZ = tag.getIntOr("OriginZ", this.boundingBox.minZ() + 11);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.FATA_MORGANA_SANCTUARY_PIECE.isPresent()) {
            return ModStructures.FATA_MORGANA_SANCTUARY_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Entropica.MODID, "fata_morgana_sanctuary_piece");
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
        buildGlacierCourtyardAndOasisMirrorPool(level, chunkBox);
        buildEastAndWestSculptedDunes(level, chunkBox);
        buildSubGlacierRefractionAlcove(level, chunkBox, random);
        buildIntersectingParabolicSkyArchAndChests(level, chunkBox, random);
    }

    private void buildGlacierCourtyardAndOasisMirrorPool(WorldGenLevel level, BoundingBox chunkBox) {
        Block mirageIceBlock = resolveBlock("mirage_ice", Blocks.BLUE_ICE);
        Block mirageGlassBlock = resolveBlock("mirage_glass", Blocks.TINTED_GLASS);
        Block glassSandstoneBlock = resolveBlock("glass_sandstone", Blocks.SANDSTONE);
        Block glassSandstoneSlabBlock = resolveBlock("glass_sandstone_slab", Blocks.SANDSTONE_SLAB);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState mirageIce = mirageIceState(mirageIceBlock, false);
        BlockState mirageGlass = nonWaterlogged(mirageGlassBlock.defaultBlockState());
        BlockState glassSandstone = glassSandstoneBlock.defaultBlockState();
        BlockState bottomSlab = bottomSlabState(glassSandstoneSlabBlock);

        // Clear sky headroom across the entire 22 x 22 x 20 footprint
        for (int dx = -11; dx <= 10; dx++) {
            for (int dz = -11; dz <= 10; dz++) {
                for (int dy = 1; dy <= 19; dy++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }
            }
        }

        // 1. Sub-base & Central Glacier Courtyard (dy = 0..3)
        for (int dx = -10; dx <= 9; dx++) {
            for (int dz = -10; dz <= 9; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 10.6D) {
                    continue;
                }

                boolean inDuneWing = Math.abs(dx) >= 6 && Math.abs(dz) <= 8;
                BlockState deepBase = inDuneWing ? glassSandstone : ((Math.abs(dx + dz) % 2 == 0) ? blueIce : glassSandstone);
                setRel(level, chunkBox, dx, 0, dz, deepBase);
                fillFoundationDown(level, chunkBox, dx, -1, dz, inDuneWing ? glassSandstone : packedIce);

                for (int dy = 1; dy <= 2; dy++) {
                    setRel(level, chunkBox, dx, dy, dz, inDuneWing ? glassSandstone : packedIce);
                }

                if (!inDuneWing) {
                    // Courtyard pavement at dy = 3: geometric mosaic of Blue Ice & solid Mirage Ice
                    int pattern = Math.abs(dx * 7 + dz * 13) % 5;
                    BlockState courtTop;
                    if (Math.abs(dz) >= 9 && Math.abs(dx) <= 2) {
                        courtTop = (Math.abs(dz) == 10) ? bottomSlab : glassSandstone;
                    } else if (pattern == 0 || pattern == 2) {
                        courtTop = mirageIce;
                    } else {
                        courtTop = blueIce;
                    }
                    setRel(level, chunkBox, dx, 3, dz, courtTop);
                }
            }
        }

        // 2. Sunken Unfrozen Oasis Mirror-Pool (ZERO Cores or Anchors!)
        //    - Edged around dx = -4..4, dz = -4..4 in glass_sandstone & glass_sandstone_slab
        //    - Filled at dy = 2 across dx = -3..3, dz = -3..3 (octagonal basin) with unfrozen Blocks.WATER
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                if (ax == 4 && az == 4) {
                    continue;
                }

                boolean insideWaterBasin = (ax <= 3 && az <= 3) && !(ax == 3 && az == 3);
                if (insideWaterBasin) {
                    // Refractive prism pool bed at dy = 1
                    BlockState bedState = ((ax + az) % 2 == 0) ? mirageGlass : ((ax <= 1 && az <= 1) ? mirageIce : blueIce);
                    setRel(level, chunkBox, dx, 1, dz, bedState);
                    setRel(level, chunkBox, dx, 2, dz, water);
                    setRel(level, chunkBox, dx, 3, dz, air);
                } else {
                    // Watertight rim wall at dy = 1..2 + glass_sandstone & glass_sandstone_slab edging at dy = 3
                    setRel(level, chunkBox, dx, 1, dz, glassSandstone);
                    setRel(level, chunkBox, dx, 2, dz, glassSandstone);
                    boolean isCornerPier = (ax == 3 && az == 3);
                    setRel(level, chunkBox, dx, 3, dz, isCornerPier ? glassSandstone : bottomSlab);
                }
            }
        }
    }

    private void buildEastAndWestSculptedDunes(WorldGenLevel level, BoundingBox chunkBox) {
        Block glassSandBlock = resolveBlock("glass_sand", Blocks.SAND);
        Block glassSandstoneBlock = resolveBlock("glass_sandstone", Blocks.SANDSTONE);
        Optional<Block> vitreousCactusOpt = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitreous_cactus"));
        Optional<Block> shimmerpetalOpt = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "shimmerpetal"));

        BlockState glassSand = glassSandBlock.defaultBlockState();
        BlockState glassSandstone = glassSandstoneBlock.defaultBlockState();

        // Sculpted East & West desert-glacier dunes (dx = -10..-6 and dx = 6..9, dz = -8..8)
        int[] sides = {-1, 1};
        for (int side : sides) {
            for (int magX = 6; magX <= 10; magX++) {
                int dx = side * magX;
                if (dx < -10 || dx > 9) {
                    continue;
                }
                for (int dz = -8; dz <= 8; dz++) {
                    double r = Math.hypot(dx, dz);
                    if (r > 10.5D) {
                        continue;
                    }
                    // Keep E-W arch abutment corridor (dz = -1..1) compact at dy = 4
                    double ridgeWave = Math.cos((dz + side * 1.5D) * 0.55D);
                    int crestDy;
                    if (Math.abs(dz) <= 1) {
                        crestDy = (magX >= 8) ? 4 : 3;
                    } else if (magX == 8 && Math.abs(dz) >= 3 && Math.abs(dz) <= 6) {
                        crestDy = 5;
                    } else if (magX >= 7 && magX <= 9 && ridgeWave > 0.15D) {
                        crestDy = 4;
                    } else {
                        crestDy = 3;
                    }

                    // Solid glass_sandstone core up to crestDy - 1 so glass_sand NEVER falls
                    for (int dy = 1; dy < crestDy; dy++) {
                        setRel(level, chunkBox, dx, dy, dz, glassSandstone);
                    }
                    setRel(level, chunkBox, dx, crestDy, dz, glassSand);
                }
            }
        }

        // Flatten isolated sand pedestals for Vitreous Cactus pillars so all 4 horizontal neighbors at cactus height are AIR
        int[][] cactusAnchors = {
                {-8, -5, 3},
                {-8, 5, 2},
                {8, -5, 2},
                {8, 5, 3}
        };
        Block cactusBlock = vitreousCactusOpt.orElse(Blocks.CACTUS);
        BlockState cactusState = cactusBlock.defaultBlockState();

        for (int[] anchor : cactusAnchors) {
            int cx = anchor[0];
            int cz = anchor[1];
            int height = anchor[2];

            // Ensure flat 3x3 sand/sandstone pad at dy = 5 (with air above neighbors at dy >= 6)
            setRel(level, chunkBox, cx, 4, cz, glassSandstone);
            setRel(level, chunkBox, cx, 5, cz, glassSand);
            for (Direction horiz : Direction.Plane.HORIZONTAL) {
                int nx = cx + horiz.getStepX();
                int nz = cz + horiz.getStepZ();
                for (int clearY = 6; clearY <= 5 + height; clearY++) {
                    setRel(level, chunkBox, nx, clearY, nz, Blocks.AIR.defaultBlockState());
                }
            }
            for (int h = 1; h <= height; h++) {
                setRel(level, chunkBox, cx, 5 + h, cz, cactusState);
            }
        }

        // Accent dune margins with Shimmerpetal blooms (or dead bushes as fallback)
        BlockState duneBloom = shimmerpetalOpt
                .map(b -> nonWaterlogged(b.defaultBlockState()))
                .orElse(Blocks.DEAD_BUSH.defaultBlockState());
        int[][] bloomSpots = {
                {-7, -3}, {-7, 3}, {7, -3}, {7, 3}
        };
        for (int[] spot : bloomSpots) {
            setRel(level, chunkBox, spot[0], 3, spot[1], glassSandstone);
            setRel(level, chunkBox, spot[0], 4, spot[1], glassSand);
            setRel(level, chunkBox, spot[0], 5, spot[1], duneBloom);
        }
    }

    private void buildSubGlacierRefractionAlcove(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block mirageIceBlock = resolveBlock("mirage_ice", Blocks.BLUE_ICE);
        Block mirageGlassBlock = resolveBlock("mirage_glass", Blocks.TINTED_GLASS);
        Block glassSandstoneBlock = resolveBlock("glass_sandstone", Blocks.SANDSTONE);
        Block glassSandstoneSlabBlock = resolveBlock("glass_sandstone_slab", Blocks.SANDSTONE_SLAB);
        Block prismLanternBlock = resolveBlock("mirage_prism_lantern", Blocks.SEA_LANTERN);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState mirageIce = mirageIceState(mirageIceBlock, false);
        BlockState mirageGlass = nonWaterlogged(mirageGlassBlock.defaultBlockState());
        BlockState glassSandstone = glassSandstoneBlock.defaultBlockState();
        BlockState bottomSlab = bottomSlabState(glassSandstoneSlabBlock);
        BlockState lanternState = lanternBlockState(prismLanternBlock, false);

        // Enclosing Blue Ice & Mirage Ice walls around dx = -3..3, dz = -8..-4, dy = 1..3
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -8; dz <= -4; dz++) {
                setRel(level, chunkBox, dx, 0, dz, ((dx + dz) % 2 == 0) ? glassSandstone : blueIce);
                if (Math.abs(dx) == 3) {
                    for (int dy = 1; dy <= 3; dy++) {
                        setRel(level, chunkBox, dx, dy, dz, (dy == 2) ? mirageIce : blueIce);
                    }
                }
            }
        }

        // Carve sub-glacier alcove chamber (dx = -2..2, dz = -6..-5, dy = 1..3) + raised refractive skylight vault at dy = 4
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -6; dz <= -5; dz++) {
                for (int dy = 1; dy <= 3; dy++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }
                BlockState vaultRoof = (Math.abs(dx) <= 1) ? mirageGlass : mirageIce;
                setRel(level, chunkBox, dx, 4, dz, vaultRoof);
            }
        }

        // Underwater Mirage Glass refraction window wall at dz = -4 separating the alcove from the oasis mirror-pool!
        for (int dx = -2; dx <= 2; dx++) {
            if (Math.abs(dx) <= 1) {
                setRel(level, chunkBox, dx, 1, -4, glassSandstone);
                setRel(level, chunkBox, dx, 2, -4, mirageGlass); // Looks straight into sunken WATER at dz = -3, dy = 2!
                setRel(level, chunkBox, dx, 3, -4, mirageGlass);
            } else {
                setRel(level, chunkBox, dx, 1, -4, blueIce);
                setRel(level, chunkBox, dx, 2, -4, mirageIce);
                setRel(level, chunkBox, dx, 3, -4, blueIce);
            }
            setRel(level, chunkBox, dx, 4, -4, bottomSlab);
        }

        // Walkable stepped descent from North courtyard (dz = -8 -> dz = -7 -> alcove floor dy = 1 at dz = -6)
        for (int dx = -1; dx <= 1; dx++) {
            // dz = -8: upper step at dy = 2 + bottomSlab at dy = 3
            setRel(level, chunkBox, dx, 1, -8, glassSandstone);
            setRel(level, chunkBox, dx, 2, -8, glassSandstone);
            setRel(level, chunkBox, dx, 3, -8, bottomSlab);
            setRel(level, chunkBox, dx, 4, -8, air);

            // dz = -7: middle step at dy = 1 + bottomSlab at dy = 2
            setRel(level, chunkBox, dx, 1, -7, glassSandstone);
            setRel(level, chunkBox, dx, 2, -7, bottomSlab);
            setRel(level, chunkBox, dx, 3, -7, air);
            setRel(level, chunkBox, dx, 4, -7, air);
        }
        // Frame entrance side cheeks at dx = ±2, dz = -8..-7
        for (int dz = -8; dz <= -7; dz++) {
            setRel(level, chunkBox, -2, 1, dz, blueIce);
            setRel(level, chunkBox, -2, 2, dz, mirageIce);
            setRel(level, chunkBox, -2, 3, dz, bottomSlab);
            setRel(level, chunkBox, 2, 1, dz, blueIce);
            setRel(level, chunkBox, 2, 2, dz, mirageIce);
            setRel(level, chunkBox, 2, 3, dz, bottomSlab);
        }

        // Lantern #1: Mirage Prism Lantern illuminating & protecting the sub-glacier refraction alcove
        setRel(level, chunkBox, -2, 1, -5, glassSandstone);
        setRel(level, chunkBox, -2, 2, -5, lanternState);

        // Reliquary Chest #2 tucked inside the sub-glacier refraction alcove beside the mirror pool
        placeSanctuaryChest(level, chunkBox, random, 2, 1, -5, Direction.WEST);
    }

    private void buildIntersectingParabolicSkyArchAndChests(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block mirageIceBlock = resolveBlock("mirage_ice", Blocks.BLUE_ICE);
        Block mirageGlassBlock = resolveBlock("mirage_glass", Blocks.TINTED_GLASS);
        Block mirageGlassPaneBlock = resolveBlock("mirage_glass_pane", Blocks.GLASS_PANE);
        Block glassSandstoneBlock = resolveBlock("glass_sandstone", Blocks.SANDSTONE);
        Block prismLanternBlock = resolveBlock("mirage_prism_lantern", Blocks.SEA_LANTERN);

        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState solidMirageIce = mirageIceState(mirageIceBlock, false);
        BlockState phasedMirageIce = mirageIceState(mirageIceBlock, true);
        BlockState mirageGlass = nonWaterlogged(mirageGlassBlock.defaultBlockState());
        BlockState glassSandstone = glassSandstoneBlock.defaultBlockState();

        // 1. Secondary E-W Sweeping Parabolic Arch (dx = -9..-2 and dx = 2..9, dz = -1..1)
        //    Rises 14 blocks above the dy = 3 courtyard to meet at the dy = 17 crown!
        int[] signs = {-1, 1};
        for (int sign : signs) {
            int prevTopY = 17;
            for (int magX = 2; magX <= 9; magX++) {
                int dx = sign * magX;
                double t = (magX - 1.0D) / 8.5D; // 0..0.94
                int topY = Math.max(5, 3 + (int) Math.round(14.0D * (1.0D - t * t)));
                int bottomY = Math.max(4, Math.min(topY - 1, prevTopY - 1));

                for (int dy = bottomY; dy <= topY; dy++) {
                    if (magX >= 8) {
                        // Abutment piers anchored into the East/West desert-glacier dunes
                        setRel(level, chunkBox, dx, dy, -1, blueIce);
                        setRel(level, chunkBox, dx, dy, 0, (dy == topY) ? solidMirageIce : glassSandstone);
                        setRel(level, chunkBox, dx, dy, 1, blueIce);
                    } else {
                        setRel(level, chunkBox, dx, dy, -1, solidMirageIce);
                        setRel(level, chunkBox, dx, dy, 0, (dy == topY && magX <= 4) ? solidMirageIce : mirageGlass);
                        setRel(level, chunkBox, dx, dy, 1, solidMirageIce);
                    }
                }
                // Anchor E-W abutment piers down to dy = 3
                if (magX == 9) {
                    for (int dy = 3; dy < bottomY; dy++) {
                        setRel(level, chunkBox, dx, dy, -1, blueIce);
                        setRel(level, chunkBox, dx, dy, 0, glassSandstone);
                        setRel(level, chunkBox, dx, dy, 1, blueIce);
                    }
                }
                prevTopY = topY;
            }
        }

        // 2. North Parabolic Span (dz = -9..-2) keeping North ground entrance (dx = -1..1, dy = 3..6) open!
        //    Twin Blue Ice & Mirage Ice abutment piers at dx = +-2, dz = -9 converge onto dx = -1..1 at dz = -8..2
        for (int dy = 3; dy <= 7; dy++) {
            setRel(level, chunkBox, -2, dy, -9, (dy <= 5) ? blueIce : solidMirageIce);
            setRel(level, chunkBox, 2, dy, -9, (dy <= 5) ? blueIce : solidMirageIce);
        }
        int prevNorthY = 17;
        for (int magZ = 2; magZ <= 8; magZ++) {
            int dz = -magZ;
            double t = (magZ - 1.0D) / 8.0D;
            int topY = Math.max(8, 3 + (int) Math.round(14.0D * (1.0D - t * t)));
            int bottomY = Math.max(7, Math.min(topY - 1, prevNorthY - 1));
            for (int dy = bottomY; dy <= topY; dy++) {
                setRel(level, chunkBox, -1, dy, dz, solidMirageIce);
                setRel(level, chunkBox, 0, dy, dz, mirageGlass);
                setRel(level, chunkBox, 1, dy, dz, solidMirageIce);
            }
            prevNorthY = topY;
        }

        // 3. South Parabolic Bridge-Arch + Walkable Phasic Mirage Ice Sky-Steps (dz = 2..10, exact +1 block per step!)
        //    Twin flanking approach sky-steps rise from courtyard (dy = 3) at (±5, 4, 8) -> (0, 10, 9),
        //    leaving the South ground entrance (dx = -1..1, dz = 9..10, dy = 3..8) wide open!
        int[][] southApproachSteps = {
                {-5, 4, 8}, {5, 4, 8},
                {-5, 5, 9}, {5, 5, 9},
                {-4, 6, 10}, {4, 6, 10},
                {-3, 7, 10}, {3, 7, 10},
                {-2, 8, 10}, {2, 8, 10},
                {-1, 9, 10}, {1, 9, 10}
        };
        // Supporting Blue Ice buttress piers at (±5, 3, 8..9) and (±4, 3..5, 10)
        for (int sx : signs) {
            setRel(level, chunkBox, sx * 5, 3, 8, blueIce);
            setRel(level, chunkBox, sx * 5, 3, 9, blueIce);
            setRel(level, chunkBox, sx * 5, 4, 9, blueIce);
            for (int dy = 3; dy <= 5; dy++) {
                setRel(level, chunkBox, sx * 4, dy, 10, (dy <= 4) ? blueIce : solidMirageIce);
            }
        }
        for (int[] step : southApproachSteps) {
            // Solid walkable Mirage Ice tread + decorative phasic Mirage Ice underside shimmer rib
            setRel(level, chunkBox, step[0], step[1], step[2], solidMirageIce);
            if (step[1] >= 6) {
                setRel(level, chunkBox, step[0], step[1] - 1, step[2], phasedMirageIce);
            }
        }
        // Central landing at dz = 9, dy = 10 bridging the twin approach stairs
        setRel(level, chunkBox, -1, 10, 9, solidMirageIce);
        setRel(level, chunkBox, 0, 10, 9, solidMirageIce);
        setRel(level, chunkBox, 1, 10, 9, solidMirageIce);
        setRel(level, chunkBox, 0, 9, 9, mirageGlass);

        // Ascending South Parabolic Spine: dz = 8 (dy = 11) -> dz = 3 (dy = 16) -> dz = 2 (dy = 16) -> crown (dy = 17)
        int[] southSpineHeights = {
                // index = dz (2..8)
                0, 0, 16, 16, 15, 14, 13, 12, 11
        };
        for (int dz = 8; dz >= 2; dz--) {
            int stepY = southSpineHeights[dz];
            // Walkable solid Mirage Ice outer & center treads + Mirage Glass & phasic Mirage Ice belly
            setRel(level, chunkBox, -1, stepY, dz, solidMirageIce);
            setRel(level, chunkBox, 0, stepY, dz, (dz % 2 == 0) ? mirageGlass : solidMirageIce);
            setRel(level, chunkBox, 1, stepY, dz, solidMirageIce);

            setRel(level, chunkBox, -1, stepY - 1, dz, phasedMirageIce);
            setRel(level, chunkBox, 0, stepY - 1, dz, mirageGlass);
            setRel(level, chunkBox, 1, stepY - 1, dz, phasedMirageIce);
        }

        // 4. 14-Block-High Intersecting Arch-Crown Platform (dx = -1..1, dy = 17, dz = -1..1) + Crown Treasure Chest
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean isKeystone = (dx == 0 && dz == 0);
                setRel(level, chunkBox, dx, 17, dz, isKeystone ? mirageGlass : solidMirageIce);
                if (!isKeystone && (dx == 0 || dz == 0)) {
                    setRel(level, chunkBox, dx, 16, dz, mirageGlass);
                }
            }
        }
        // Arch-Crown Treasure Chest #1 at (0, 18, 0) facing SOUTH toward the ascending sky-steps!
        placeSanctuaryChest(level, chunkBox, random, 0, 18, 0, Direction.SOUTH);

        // 5. Refractive Mirage Glass Pane Fins flanking both intersecting upper arches
        int[][] nsPaneFins = {
                {-2, 16, -2}, {2, 16, -2},
                {-2, 15, -3}, {2, 15, -3},
                {-2, 14, -4}, {2, 14, -4},
                {-2, 13, -5}, {2, 13, -5},
                {-2, 16, 2},  {2, 16, 2},
                {-2, 16, 3},  {2, 16, 3},
                {-2, 15, 4},  {2, 15, 4},
                {-2, 14, 5},  {2, 14, 5}
        };
        int[][] ewPaneFins = {
                {-2, 16, -2}, {-2, 16, 2},
                {-3, 15, -2}, {-3, 15, 2},
                {-4, 14, -2}, {-4, 14, 2},
                {2, 16, -2},  {2, 16, 2},
                {3, 15, -2},  {3, 15, 2},
                {4, 14, -2},  {4, 14, 2}
        };
        for (int[] fin : nsPaneFins) {
            placeConnectedPane(level, chunkBox, fin[0], fin[1], fin[2], mirageGlassPaneBlock);
        }
        for (int[] fin : ewPaneFins) {
            placeConnectedPane(level, chunkBox, fin[0], fin[1], fin[2], mirageGlassPaneBlock);
        }
        // Second pass refreshes neighbor connections across adjacent fins & arch ribs
        for (int[] fin : nsPaneFins) {
            placeConnectedPane(level, chunkBox, fin[0], fin[1], fin[2], mirageGlassPaneBlock);
        }
        for (int[] fin : ewPaneFins) {
            placeConnectedPane(level, chunkBox, fin[0], fin[1], fin[2], mirageGlassPaneBlock);
        }

        // 6. Remaining 3 Mirage Prism Lanterns (total = 4 across the sanctuary!)
        //    - Lanterns #2 & #3: West & East oasis courtyard pedestals at (-4, 4, 2) and (4, 4, 2)
        //    - Lantern #4: Suspended crown prism lantern at (0, 16, 0) beneath the (0, 17, 0) keystone!
        BlockState standingLantern = lanternBlockState(prismLanternBlock, false);
        BlockState hangingLantern = lanternBlockState(prismLanternBlock, true);

        setRel(level, chunkBox, -4, 3, 2, glassSandstone);
        setRel(level, chunkBox, -4, 4, 2, standingLantern);

        setRel(level, chunkBox, 4, 3, 2, glassSandstone);
        setRel(level, chunkBox, 4, 4, 2, standingLantern);

        setRel(level, chunkBox, 0, 16, 0, hangingLantern);
    }

    private void placeSanctuaryChest(
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
                level, random, pos, ModStructures.FATA_MORGANA_SANCTUARY_CHEST_LOOT);
    }

    private void placeConnectedPane(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int dy,
            int dz,
            Block paneBlock
    ) {
        BlockPos pos = toWorld(dx, dy, dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState state = nonWaterlogged(paneBlock.defaultBlockState());
        for (Direction horiz : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(horiz);
            if (!chunkBox.isInside(neighborPos)) {
                continue;
            }
            BlockState neighbor = level.getBlockState(neighborPos);
            boolean connects = neighbor.is(paneBlock)
                    || (!neighbor.isAir() && neighbor.isFaceSturdy(level, neighborPos, horiz.getOpposite()))
                    || BuiltInRegistries.BLOCK.getKey(neighbor.getBlock()).getPath().contains("mirage_");
            state = setPaneSide(state, horiz, connects);
        }
        level.setBlock(pos, state, 2);
    }

    private static BlockState setPaneSide(BlockState state, Direction dir, boolean value) {
        return switch (dir) {
            case NORTH -> state.hasProperty(CrossCollisionBlock.NORTH)
                    ? state.setValue(CrossCollisionBlock.NORTH, value) : state;
            case SOUTH -> state.hasProperty(CrossCollisionBlock.SOUTH)
                    ? state.setValue(CrossCollisionBlock.SOUTH, value) : state;
            case WEST -> state.hasProperty(CrossCollisionBlock.WEST)
                    ? state.setValue(CrossCollisionBlock.WEST, value) : state;
            case EAST -> state.hasProperty(CrossCollisionBlock.EAST)
                    ? state.setValue(CrossCollisionBlock.EAST, value) : state;
            default -> state;
        };
    }

    private static BlockState mirageIceState(Block block, boolean phased) {
        BlockState state = nonWaterlogged(block.defaultBlockState());
        for (Property<?> prop : state.getProperties()) {
            if ("phased".equals(prop.getName()) && prop instanceof BooleanProperty boolProp) {
                return state.setValue(boolProp, phased);
            }
        }
        return state;
    }

    private static BlockState bottomSlabState(Block slabBlock) {
        BlockState state = nonWaterlogged(slabBlock.defaultBlockState());
        if (state.hasProperty(SlabBlock.TYPE)) {
            state = state.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        }
        return state;
    }

    private static BlockState lanternBlockState(Block lanternBlock, boolean hanging) {
        BlockState state = nonWaterlogged(lanternBlock.defaultBlockState());
        if (state.hasProperty(BlockStateProperties.HANGING)) {
            state = state.setValue(BlockStateProperties.HANGING, hanging);
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

    private void fillFoundationDown(
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
