package ddraig.net.entropica.block;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.EntropicCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class VisExhaustBlock extends Block {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public VisExhaustBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            boolean isActive = state.getValue(ACTIVE);
            boolean newState = !isActive;

            level.setBlock(pos, state.setValue(ACTIVE, newState), 3);

            if (newState) {
                level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.scheduleTick(pos, this, 5);
            } else {
                level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Only sizzle if the vent is actually open
        if (state.getValue(ACTIVE)) {

            // Randomly play the lava sizzle/pop sound so it feels organic, not like a looped track
            if (random.nextInt(3) == 0) {
                level.playLocalSound(
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        SoundEvents.LAVA_AMBIENT,
                        SoundSource.BLOCKS,
                        0.3F + random.nextFloat() * 0.2F, // Keep the volume relatively low
                        0.8F + random.nextFloat() * 0.4F, // Randomize the pitch slightly for a bubbling effect
                        false
                );
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) return;

        // Trace down to find ANY piece of the core
        EntropicCoreBlockEntity core = findCoreBelow(level, pos);

        if (core != null) {
            // Master/Slave Handling: Reroute all logic to the true Master block!
            EntropicCoreBlockEntity master = core.getMaster();

            // Safety Check: Only vent if the multiblock is actually formed
            if (master != null && master.isFormed()) {

                EssenceType typeToVent = null;
                int maxMana = 0;

                for (Map.Entry<EssenceType, Integer> entry : master.getManaPool().entrySet()) {
                    if (entry.getValue() > maxMana) {
                        maxMana = entry.getValue();
                        typeToVent = entry.getKey();
                    }
                }

                if (typeToVent != null && maxMana > 0) {
                    // Calculate how full the core is (0.0 to 1.0)
                    double fillPercentage = Math.min(1.0, (double) maxMana / (double) master.getMaxMana());

                    int extracted = master.extractMana(typeToVent, 50);

                    if (extracted > 0) {
                        // Scale sound pitch based on pressure (higher pressure = higher pitch squeal)
                        float pitch = 0.8F + (float)(fillPercentage * 0.8F);
                        level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, pitch);

                        // Scale particle upward speed and spread dynamically!
                        double upwardSpeed = 0.15D + (fillPercentage * 0.6D);
                        double ySpread = 0.5D + (fillPercentage * 2.0D);

                        DustParticleOptions dust = new DustParticleOptions(typeToVent.getColorInt(), 2.0F);
                        level.sendParticles(dust,
                                pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                                25, 0.2D, ySpread, 0.2D, upwardSpeed);

                        // Scale the elevator reach and push force!
                        double elevatorHeight = 3.0D + (fillPercentage * 12.0D); // Reaches 3 to 15 blocks high
                        double pushForce = 0.3D + (fillPercentage * 0.6D);
                        double maxVerticalSpeed = 0.5D + (fillPercentage * 0.9D);

                        AABB exhaustArea = new AABB(pos).move(0, 1, 0).expandTowards(0, elevatorHeight, 0);
                        List<Entity> entities = level.getEntitiesOfClass(Entity.class, exhaustArea);

                        for (Entity e : entities) {
                            Vec3 movement = e.getDeltaMovement();
                            e.setDeltaMovement(movement.x, Math.max(movement.y + pushForce, maxVerticalSpeed), movement.z);
                            e.hurtMarked = true;
                        }

                        level.scheduleTick(pos, this, 5);
                        return;
                    }
                }
            }
        }

        // Auto-shutoff if the core is empty, unformed, or broken
        level.setBlock(pos, state.setValue(ACTIVE, false), 3);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.5F);
    }

    private EntropicCoreBlockEntity findCoreBelow(Level level, BlockPos startPos) {
        for (int i = 1; i <= 10; i++) {
            BlockPos checkPos = startPos.below(i);
            if (level.getBlockEntity(checkPos) instanceof EntropicCoreBlockEntity core) {
                return core; // We return whichever core block we find; getMaster() handles the rest.
            }
        }
        return null;
    }
}