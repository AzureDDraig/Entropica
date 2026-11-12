package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.GreaterMateriaBlessingBlock;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class MateriaBlessingBlockEntity extends BlockEntity {

    private EssenceType ambientAffinity = EssenceType.REGULAR;
    private float rotationAngle = 0;
    private float bobPhase = 0;

    /** Maximum distance to search for an Entropic Core node (configurable). */
    private static final int NODE_SEARCH_RADIUS = 64;

    public MateriaBlessingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATERIA_BLESSING_BE.get(), pos, state);
    }

    /**
     * Returns true if this block entity belongs to a Greater Materia Blessing.
     */
    public boolean isGreater() {
        return getBlockState().getBlock() instanceof GreaterMateriaBlessingBlock;
    }

    // ─────── Size-Aware Effect Parameters ───────

    private int getAuraRadius() {
        return isGreater() ? 24 : 16;
    }

    private int getInjectionRadius() {
        return isGreater() ? 12 : 8;
    }

    private int getInjectionInterval() {
        return isGreater() ? 60 : 100; // ticks
    }

    private float getHealAmount() {
        return isGreater() ? 2.0f : 1.0f;
    }

    // ─────── Server Tick ───────

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        // Update affinity every 20 ticks (1 second)
        if (level.getGameTime() % 20 == 0) {
            EssenceType oldAffinity = ambientAffinity;

            // Priority 1: Nearest Entropic Core node within NODE_SEARCH_RADIUS
            EssenceType nodeType = getDominantEssenceFromNearestNode(level, pos);
            if (nodeType != null) {
                ambientAffinity = nodeType;
            } else {
                // Priority 2: Biome-based attunement
                ambientAffinity = getEssenceTypeForBiome(level, pos);
            }

            // Sync to client when affinity changes
            if (oldAffinity != ambientAffinity) {
                setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        int injectionInterval = getInjectionInterval();
        if (level.getGameTime() % injectionInterval == 0) {
            int radius = getInjectionRadius();
            BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        scanPos.setWithOffset(pos, x, y, z);
                        BlockEntity be = level.getBlockEntity(scanPos);

                        if (be instanceof RitualBowlBlockEntity bowl) {
                            if (bowl.getVariant() == ddraig.net.entropica.block.RitualBowlBlock.BowlVariant.GRANITE) {
                                ItemStack essence = new ItemStack(ModItems.WEAK_ESSENCE.get(), 1);
                                EssenceItem.setEssenceType(essence, ambientAffinity);
                                injectIntoBowl(bowl, essence);
                            }
                        }
                    }
                }
            }
        }

        if (level.getGameTime() % 2 == 0) {
            int auraRadius = getAuraRadius();
            AABB playerArea = new AABB(pos).inflate(auraRadius);
            List<Player> players = level.getEntitiesOfClass(Player.class, playerArea);
            for (Player player : players) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, true, false));

                // Greater variant also grants Regeneration I
                if (isGreater()) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false));
                }

                if (player.getHealth() < player.getMaxHealth() && level.getGameTime() % 40 == 0) {
                    player.heal(getHealAmount());
                }
            }
        }
    }

    // ─────── Node Detection (Entropic Core) ───────

    /**
     * Searches for the nearest Entropic Core within NODE_SEARCH_RADIUS and returns
     * its dominant EssenceType (the type with the highest pool value).
     * Returns null if no core is found or the core has no essence.
     */
    private EssenceType getDominantEssenceFromNearestNode(Level level, BlockPos pos) {
        EntropicCoreBlockEntity nearestCore = null;
        double nearestDistSq = Double.MAX_VALUE;

        // Scan in a cube for Entropic Core block entities
        // Optimization: only scan every 3rd block to reduce checks, then verify exact position
        BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();
        for (int x = -NODE_SEARCH_RADIUS; x <= NODE_SEARCH_RADIUS; x += 3) {
            for (int y = -NODE_SEARCH_RADIUS; y <= NODE_SEARCH_RADIUS; y += 3) {
                for (int z = -NODE_SEARCH_RADIUS; z <= NODE_SEARCH_RADIUS; z += 3) {
                    // Check the 3×3×3 neighborhood around this skip point
                    for (int dx = 0; dx < 3 && (x + dx) <= NODE_SEARCH_RADIUS; dx++) {
                        for (int dy = 0; dy < 3 && (y + dy) <= NODE_SEARCH_RADIUS; dy++) {
                            for (int dz = 0; dz < 3 && (z + dz) <= NODE_SEARCH_RADIUS; dz++) {
                                scanPos.setWithOffset(pos, x + dx, y + dy, z + dz);
                                double distSq = pos.distSqr(scanPos);
                                if (distSq > (long) NODE_SEARCH_RADIUS * NODE_SEARCH_RADIUS) continue;
                                if (distSq >= nearestDistSq) continue;

                                BlockEntity be = level.getBlockEntity(scanPos);
                                if (be instanceof EntropicCoreBlockEntity core) {
                                    nearestCore = core;
                                    nearestDistSq = distSq;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (nearestCore != null) {
            Map<EssenceType, Integer> pool = nearestCore.getEssencePool();
            EssenceType dominant = null;
            int maxAmount = 0;
            for (Map.Entry<EssenceType, Integer> entry : pool.entrySet()) {
                if (entry.getValue() > maxAmount) {
                    maxAmount = entry.getValue();
                    dominant = entry.getKey();
                }
            }
            return dominant; // may be null if pool is empty
        }
        return null;
    }

    // ─────── Client Tick ───────

    public void clientTick(Level level, BlockPos pos, BlockState state) {
        float rotSpeed = isGreater() ? 0.6f : 0.5f;
        rotationAngle += rotSpeed;
        bobPhase += 0.05f;

        // Get RGB color matching the crystal's current ambient/node attunement
        int[] rgb = ambientAffinity.getCurrentRGB(level.getGameTime() + (long) bobPhase);
        float r = rgb[0] / 255.0f;
        float g = rgb[1] / 255.0f;
        float b = rgb[2] / 255.0f;

        // Target center of the floating crystal
        double centerX = pos.getX() + (isGreater() ? 1.5 : 1.0);
        double centerY = pos.getY() + (isGreater() ? 2.0 : 1.5) + Math.sin(bobPhase * 0.05) * 0.15;
        double centerZ = pos.getZ() + (isGreater() ? 1.5 : 1.0);

        // Particle spawn parameters based on crystal size
        double minRadius = isGreater() ? 2.0 : 1.2;
        double maxRadius = isGreater() ? 4.5 : 3.0;
        int particleFrequency = isGreater() ? 2 : 1;

        for (int i = 0; i < particleFrequency; i++) {
            if (level.random.nextInt(2) == 0) {
                // Random spherical position around crystal
                double angle = level.random.nextDouble() * Math.PI * 2.0;
                double elevation = (level.random.nextDouble() - 0.5) * (isGreater() ? 3.0 : 2.0);
                double dist = minRadius + level.random.nextDouble() * (maxRadius - minRadius);

                double spawnX = centerX + Math.cos(angle) * dist;
                double spawnY = centerY + elevation;
                double spawnZ = centerZ + Math.sin(angle) * dist;

                // Velocity vector pointing TOWARDS the crystal center
                double dx = centerX - spawnX;
                double dy = centerY - spawnY;
                double dz = centerZ - spawnZ;
                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (len > 0.01) {
                    double speed = 0.04 + level.random.nextDouble() * 0.03;
                    double vx = (dx / len) * speed;
                    double vy = (dy / len) * speed;
                    double vz = (dz / len) * speed;

                    // Tinted entity effect particle (glowing orb) flowing inward
                    level.addParticle(
                            ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b),
                            spawnX, spawnY, spawnZ,
                            vx, vy, vz
                    );

                    // Additional tinted dust particle for crisp sparkling effect
                    if (level.random.nextBoolean()) {
                        int colorInt = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                        level.addParticle(
                                new DustParticleOptions(colorInt, 1.0f),
                                spawnX, spawnY, spawnZ,
                                vx * 0.5, vy * 0.5, vz * 0.5
                        );
                    }
                }
            }
        }
    }

    // ─────── Bowl Injection ───────

    private void injectIntoBowl(RitualBowlBlockEntity bowl, ItemStack stack) {
        for (int i = 1; i <= 4; i++) {
            ItemStack current = bowl.inventory.getItem(i);
            if (current.isEmpty()) {
                bowl.inventory.setItem(i, stack);
                break;
            } else if (ItemStack.isSameItemSameComponents(current, stack) && current.getCount() < current.getMaxStackSize()) {
                current.grow(1);
                break;
            }
        }
    }

    // ─────── Biome → EssenceType Mapping ───────

    private EssenceType getEssenceTypeForBiome(Level level, BlockPos pos) {
        Holder<Biome> biomeHolder = level.getBiome(pos);
        if (biomeHolder.isBound()) {
            ResourceLocation biomeKey = biomeHolder.unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
            if (biomeKey != null) {
                String path = biomeKey.getPath();
                if (path.contains("nether") || path.contains("basalt") || path.contains("crimson") || path.contains("warped")) return EssenceType.NETHER;
                if (path.contains("end_") || path.contains("the_end")) return EssenceType.VOID;
                if (path.contains("desert") || path.contains("badlands") || path.contains("savanna")) return EssenceType.ARID;
                if (path.contains("snow") || path.contains("frozen") || path.contains("ice")) return EssenceType.FROZEN;
                if (path.contains("ocean") || path.contains("river") || path.contains("beach")) return EssenceType.WATER;
                if (path.contains("forest") || path.contains("jungle") || path.contains("plains") || path.contains("meadow") || path.contains("cherry")) return EssenceType.NATURE;
                if (path.contains("swamp") || path.contains("mangrove")) return EssenceType.UNDEAD;
                if (path.contains("slopes") || path.contains("peaks") || path.contains("windswept") || path.contains("jagged")) return EssenceType.AIR;
            }
        }
        return EssenceType.REGULAR;
    }

    // ─────── NBT Persistence ───────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("AmbientAffinity", this.ambientAffinity.name());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String affinityName = input.getStringOr("AmbientAffinity", "REGULAR");
        try {
            this.ambientAffinity = EssenceType.valueOf(affinityName);
        } catch (IllegalArgumentException e) {
            this.ambientAffinity = EssenceType.REGULAR;
        }
    }

    // ─────── Client Sync ───────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ─────── Accessors ───────

    public EssenceType getAmbientAffinity() {
        return ambientAffinity;
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public float getBobPhase() {
        return bobPhase;
    }
}
