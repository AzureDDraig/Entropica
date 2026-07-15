package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;
import java.util.stream.Collectors;

public class CreativeParticleGeneratorBlockEntity extends BlockEntity {

    private List<ResourceLocation> validParticles;
    private int currentIndex = 0;
    private ResourceLocation currentParticle = null;

    public CreativeParticleGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_PARTICLE_GENERATOR_BE.get(), pos, state);
        initParticleList();
    }

    private void initParticleList() {
        // FIXED: We unwrap the Optional<Reference<ParticleType<?>>> using .isPresent() and .get().value()
        this.validParticles = BuiltInRegistries.PARTICLE_TYPE.keySet().stream()
                .filter(rl -> {
                    var opt = BuiltInRegistries.PARTICLE_TYPE.get(rl);
                    return opt.isPresent() && opt.get().value() instanceof SimpleParticleType;
                })
                .sorted()
                .collect(Collectors.toList());

        if (!validParticles.isEmpty() && currentParticle == null) {
            currentParticle = validParticles.get(0);
        }
    }

    public void cycleParticle(int delta) {
        if (validParticles == null || validParticles.isEmpty()) return;

        currentIndex = (currentIndex + delta) % validParticles.size();
        if (currentIndex < 0) currentIndex += validParticles.size();

        currentParticle = validParticles.get(currentIndex);

        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ResourceLocation getCurrentParticle() {
        return currentParticle;
    }

    // Helper method used by the Renderer to draw the previous/next 2 items in the UI scroll list
    public String getParticleNameOffset(int offset) {
        if (validParticles == null || validParticles.isEmpty()) return "None";
        int idx = (currentIndex + offset) % validParticles.size();
        if (idx < 0) idx += validParticles.size();
        return validParticles.get(idx).toString();
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide() && currentParticle != null) {
            // Spawns a nice, continuous drifting cloud of the currently selected particle
            if (level.getGameTime() % 2 == 0) {

                // FIXED: Extracting the actual ParticleType from the Optional Reference
                var opt = BuiltInRegistries.PARTICLE_TYPE.get(currentParticle);

                if (opt.isPresent() && opt.get().value() instanceof SimpleParticleType simpleType) {
                    double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
                    double y = pos.getY() + 1.2 + (level.random.nextDouble() * 0.5);
                    double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.5;

                    double dx = (level.random.nextDouble() - 0.5) * 0.02;
                    double dy = 0.02 + level.random.nextDouble() * 0.03;
                    double dz = (level.random.nextDouble() - 0.5) * 0.02;

                    level.addParticle((ParticleOptions) simpleType, x, y, z, dx, dy, dz);
                }
            }
        }
    }

    // =========================================================================
    // NBT & SYNCING (Modern ValueInput/ValueOutput API)
    // =========================================================================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (currentParticle != null) {
            output.putString("SelectedParticle", currentParticle.toString());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String rl = input.getStringOr("SelectedParticle", "");
        if (!rl.isEmpty()) {
            currentParticle = ResourceLocation.tryParse(rl);
            if (validParticles != null && currentParticle != null) {
                currentIndex = validParticles.indexOf(currentParticle);
                if (currentIndex == -1 && !validParticles.isEmpty()) {
                    currentIndex = 0;
                    currentParticle = validParticles.get(0);
                }
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        return this.saveWithoutMetadata(p);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}