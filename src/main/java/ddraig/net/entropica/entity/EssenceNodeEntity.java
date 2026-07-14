package ddraig.net.entropica.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.item.AethericVisionItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import java.util.List;

public class EssenceNodeEntity extends Entity {

    private static final EntityDataAccessor<String> ESSENCE_TYPE_NAME = SynchedEntityData.defineId(EssenceNodeEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> CAPACITY = SynchedEntityData.defineId(EssenceNodeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_CAPACITY = SynchedEntityData.defineId(EssenceNodeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_ARTIFICIAL = SynchedEntityData.defineId(EssenceNodeEntity.class, EntityDataSerializers.BOOLEAN);

    private int ageTicks = 0;
    private boolean isFizzling = false;
    private int fizzleTicks = 0;

    public EssenceNodeEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ESSENCE_TYPE_NAME, EssenceType.AIR.name());
        builder.define(CAPACITY, 100);
        builder.define(MAX_CAPACITY, 100);
        builder.define(IS_ARTIFICIAL, false);
    }

    public void initializeNode() {
        if (!this.level().isClientSide()) {
            if (!this.isArtificial()) {
                this.setEssenceType(determineElementBasedOnLocation(this.level(), this.blockPosition()));
            }

            int min = EntropicaConfig.NODE_MIN_CAPACITY.get();
            int max = EntropicaConfig.NODE_MAX_CAPACITY.get();
            int cap = min + this.random.nextInt(Math.max(1, max - min + 1));

            this.setMaxCapacity(cap);
            this.setCapacity(cap);
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return true; // Crucial for making sure the node persists through world reloads!
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        // ==========================================
        // OBSTRUCTION CHECK
        // If a block is placed over the node, or a fluid flows into it,
        // it instantly collapses and respawns somewhere else.
        // ==========================================
        if (!this.level().getBlockState(this.blockPosition()).isAir()) {
            if (!this.isArtificial() && EntropicaConfig.NODE_RESPAWN_ENABLED.get()) {
                this.respawnInChunk();
            }
            this.discard();
            return;
        }

        if (this.isFizzling) {
            this.fizzleTicks++;
            if (this.fizzleTicks > 40) {
                if (!this.isArtificial() && EntropicaConfig.NODE_RESPAWN_ENABLED.get()) {
                    this.respawnInChunk();
                }
                this.discard();
            }
            return;
        }

        this.ageTicks++;
        boolean shouldFizzle = false;

        if (!this.isArtificial() && EntropicaConfig.NODE_LIFETIME_ENABLED.get() && this.ageTicks >= EntropicaConfig.NODE_LIFETIME_TICKS.get()) {
            shouldFizzle = true;
        }

        if (!EntropicaConfig.NODE_INFINITE_CAPACITY.get() && this.getCapacity() <= 0) {
            shouldFizzle = true;
        }

        if (shouldFizzle) {
            this.isFizzling = true;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false;
    }

    private void respawnInChunk() {
        int chunkX = this.blockPosition().getX() >> 4;
        int chunkZ = this.blockPosition().getZ() >> 4;
        RandomSource rand = this.level().getRandom();

        int minBuildHeight = this.level().dimensionType().minY();
        int maxBuildHeight = this.level().dimensionType().height();

        for (int i = 0; i < 15; i++) {
            int spawnX = (chunkX << 4) + rand.nextInt(16);
            int spawnZ = (chunkZ << 4) + rand.nextInt(16);
            int spawnY = minBuildHeight + 30 + rand.nextInt(maxBuildHeight - 60);

            BlockPos targetPos = new BlockPos(spawnX, spawnY, spawnZ);

            if (this.level().getBlockState(targetPos).isAir()) {
                EssenceNodeEntity newNode = (EssenceNodeEntity) this.getType().create(this.level(), EntitySpawnReason.NATURAL);

                if (newNode != null) {
                    newNode.setPos(spawnX + 0.5, spawnY + 0.5, spawnZ + 0.5);
                    newNode.initializeNode();
                    this.level().addFreshEntity(newNode);
                    return;
                }
            }
        }
    }

    public int extractEssence(int amount) {
        if (EntropicaConfig.NODE_INFINITE_CAPACITY.get()) {
            return amount;
        }

        int current = this.getCapacity();
        int extracted = Math.min(current, amount);
        this.setCapacity(current - extracted);

        return extracted;
    }

    private EssenceType determineElementBasedOnLocation(Level level, BlockPos pos) {
        var biomeHolder = level.getBiome(pos);
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);

        if (blockLight < 3 && skyLight < 3 && pos.getY() < 20) {
            return EssenceType.UMBRAL;
        }

        if (level.dimension() == Level.NETHER) {
            return EssenceType.NETHER;
        }
        if (level.dimension() == Level.END) {
            return EssenceType.VOID;
        }

        if (level.isThundering() && pos.getY() >= level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY()) {
            return EssenceType.LIGHTNING;
        }
        if (pos.getY() > 130) {
            return EssenceType.AIR;
        }
        if (pos.getY() < 40) {
            return EssenceType.EARTH;
        }

        if (biomeHolder.is(BiomeTags.IS_OCEAN) || biomeHolder.is(BiomeTags.IS_RIVER)) {
            return EssenceType.WATER;
        }
        if (biomeHolder.is(BiomeTags.IS_FOREST) || biomeHolder.is(BiomeTags.IS_JUNGLE)) {
            return EssenceType.NATURE;
        }
        if (biomeHolder.is(BiomeTags.IS_BADLANDS)) {
            return EssenceType.ARID;
        }
        if (biomeHolder.is(BiomeTags.IS_TAIGA)) {
            return EssenceType.FROZEN;
        }

        return EssenceType.AIR;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public EssenceType getEssenceType() {
        return EssenceType.valueOf(this.entityData.get(ESSENCE_TYPE_NAME));
    }

    public void setEssenceType(EssenceType type) {
        this.entityData.set(ESSENCE_TYPE_NAME, type.name());
    }

    public int getCapacity() {
        return this.entityData.get(CAPACITY);
    }

    public void setCapacity(int cap) {
        this.entityData.set(CAPACITY, cap);
    }

    public int getMaxCapacity() {
        return this.entityData.get(MAX_CAPACITY);
    }

    public void setMaxCapacity(int max) {
        this.entityData.set(MAX_CAPACITY, max);
    }

    public boolean isArtificial() {
        return this.entityData.get(IS_ARTIFICIAL);
    }

    public void setArtificial(boolean art) {
        this.entityData.set(IS_ARTIFICIAL, art);
    }

    public float getScale() {
        if (this.isFizzling) {
            return Math.max(0.0f, 1.0f - (this.fizzleTicks / 40.0f));
        }
        if (EntropicaConfig.NODE_INFINITE_CAPACITY.get()) {
            return 1.0f;
        }
        return Math.max(0.2f, (float) this.getCapacity() / (float) Math.max(1, this.getMaxCapacity()));
    }

    // ==========================================
    // SAVE & LOAD DATA (1.21.10 ValueInput/ValueOutput)
    // ==========================================

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        String essenceTypeName = input.read("EssenceType", Codec.STRING).orElse(EssenceType.AIR.name());
        try {
            this.setEssenceType(EssenceType.valueOf(essenceTypeName));
        } catch (IllegalArgumentException e) {
            this.setEssenceType(EssenceType.AIR);
        }
        this.setCapacity(input.read("Capacity", Codec.INT).orElse(100));
        this.setMaxCapacity(input.read("MaxCapacity", Codec.INT).orElse(100));
        this.setArtificial(input.read("IsArtificial", Codec.BOOL).orElse(false));
        this.ageTicks = input.read("AgeTicks", Codec.INT).orElse(0);
        this.isFizzling = input.read("IsFizzling", Codec.BOOL).orElse(false);
        this.fizzleTicks = input.read("FizzleTicks", Codec.INT).orElse(0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("EssenceType", Codec.STRING, this.getEssenceType().name());
        output.store("Capacity", Codec.INT, this.getCapacity());
        output.store("MaxCapacity", Codec.INT, this.getMaxCapacity());
        output.store("IsArtificial", Codec.BOOL, this.isArtificial());
        output.store("AgeTicks", Codec.INT, this.ageTicks);
        output.store("IsFizzling", Codec.BOOL, this.isFizzling);
        output.store("FizzleTicks", Codec.INT, this.fizzleTicks);
    }

    // ==========================================
    // ARCANE PLACEMENT PROTECTION HELPER
    // ==========================================

    public static boolean checkPlacementProtection(Level level, BlockPos pos, Player player) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof AethericVisionItem) {
            AABB placementBox = new AABB(pos);
            List<EssenceNodeEntity> nodes = level.getEntitiesOfClass(EssenceNodeEntity.class, placementBox);
            if (!nodes.isEmpty()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(
                            Component.literal("§cThe dense arcane energy repels the block!"), true
                    );
                }
                return true; // Cancel placement
            }
        }
        return false;
    }
}