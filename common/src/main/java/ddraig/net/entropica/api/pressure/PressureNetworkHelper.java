package ddraig.net.entropica.api.pressure;

import ddraig.net.entropica.api.materia.*;
import ddraig.net.entropica.registry.ModAttachments;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class PressureNetworkHelper {

    public static int getConduitCapacity(BlockState state, int divisor) {
        if (state == null) return ddraig.net.entropica.config.EntropicaConfig.CONDUIT_BASE_SAFE_CAPACITY.get();
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        boolean charged = path.contains("charged");
        int base = ddraig.net.entropica.config.EntropicaConfig.CONDUIT_BASE_SAFE_CAPACITY.get();
        if (charged) {
            double mult = ddraig.net.entropica.config.EntropicaConfig.CHARGED_CONDUIT_CAPACITY_MULTIPLIER.get();
            return (int) (base * mult);
        }
        return base;
    }

    public static int getConduitAbsoluteCapacity(BlockState state, int divisor) {
        if (state == null) return ddraig.net.entropica.config.EntropicaConfig.CONDUIT_BASE_SAFE_CAPACITY.get();
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        boolean charged = path.contains("charged");
        
        double tierMultiplier = 1.0; // T4 default
        if (path.contains("voltaic")) { // T5
            tierMultiplier = 1.2;
        } else if (path.contains("viscous") || path.contains("agitator")) { // T6
            tierMultiplier = 1.4;
        } else if (path.contains("sanguine")) { // T7
            tierMultiplier = 1.6;
        } else if (path.contains("equilibrium")) { // T8
            tierMultiplier = 1.8;
        } else if (path.contains("pristine")) { // T9
            tierMultiplier = 2.0;
        } else if (path.contains("athanor")) { // T10
            tierMultiplier = 2.5;
        }
        
        int baseSafe = ddraig.net.entropica.config.EntropicaConfig.CONDUIT_BASE_SAFE_CAPACITY.get();
        double absoluteUncharged = baseSafe * tierMultiplier;
        
        if (charged) {
            double mult = ddraig.net.entropica.config.EntropicaConfig.CHARGED_CONDUIT_CAPACITY_MULTIPLIER.get();
            return (int) (absoluteUncharged * mult);
        }
        return (int) absoluteUncharged;
    }

    public static int getConduitTransferRate(BlockState state, int divisor, int currentAmount) {
        if (state == null) return ddraig.net.entropica.config.EntropicaConfig.CONDUIT_UNCHARGED_TRANSFER_RATE.get();
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        boolean charged = path.contains("charged");
        
        int baseRate = charged ? 
            ddraig.net.entropica.config.EntropicaConfig.CONDUIT_CHARGED_TRANSFER_RATE.get() : 
            ddraig.net.entropica.config.EntropicaConfig.CONDUIT_UNCHARGED_TRANSFER_RATE.get();
            
        int safeCap = getConduitCapacity(state, divisor);
        int absCap = getConduitAbsoluteCapacity(state, divisor);
        
        if (currentAmount > safeCap && absCap > safeCap) {
            float ratio = (float) (currentAmount - safeCap) / (absCap - safeCap);
            ratio = Math.max(0.0f, Math.min(1.0f, ratio));
            return baseRate + (int) (ratio * baseRate);
        }
        
        return baseRate;
    }

    public static float getConduitResistance(BlockState state, float baseResistance) {
        if (state == null) return baseResistance;
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (path.contains("charged")) {
            return baseResistance * 0.1f; // 90% reduction
        }
        return baseResistance;
    }

    public static void checkPressureThresholds(Level level, BlockPos pos, IPressureHandler handler) {
        if (level.isClientSide()) return;

        MateriaStack stack = handler.getMateriaInTank();
        if (stack.isEmpty()) return;

        float pressure = handler.getPressure();
        if (pressure > 3.0f) {
            triggerExplosion(level, pos, handler, stack);
        } else if (pressure > 1.0f) {
            float overpressureRatio = (pressure - 1.0f) / 2.0f;
            if (level.random.nextFloat() < overpressureRatio * 0.15f) {
                triggerLeak(level, pos, handler, stack);
            }
        }
    }

    private static void triggerLeak(Level level, BlockPos pos, IPressureHandler handler, MateriaStack stack) {
        int color = stack.getType().getColorInt();
        
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new DustParticleOptions(color, 1.5F), pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.1);
            applyToxicity(serverLevel, pos, stack, 3.0, 200);
            
            // Random leak sound
            if (level.random.nextInt(3) == 0) {
                level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 0.8F + level.random.nextFloat() * 0.4F);
            }
        }
    }

    private static void triggerExplosion(Level level, BlockPos pos, IPressureHandler handler, MateriaStack stack) {
        level.destroyBlock(pos, true);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.7F, 0.8F + level.random.nextFloat() * 0.4F);

        int color = stack.getType().getColorInt();
        
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new DustParticleOptions(color, 2.0F), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.2);
            
            int amount = stack.getAmount();
            int tier = getMateriaTier(stack);
            
            // Spawn lingering toxicity AreaEffectCloud
            int duration = 100 + (int) (amount * 0.5f * tier);
            AreaEffectCloud cloud = new AreaEffectCloud(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            cloud.setRadius(2.5f);
            cloud.setRadiusOnUse(-0.1f);
            cloud.setWaitTime(10);
            cloud.setDuration(duration);
            cloud.setRadiusPerTick(-0.005f);
            cloud.setCustomParticle(new DustParticleOptions(color, 1.5F));
            
            // Apply effect to the cloud
            cloud.addEffect(new MobEffectInstance(ModEffects.MATERIA_TOXICITY, 200, 0));
            level.addFreshEntity(cloud);
            
            // Immediate toxic burst
            applyToxicity(serverLevel, pos, stack, 5.0, duration);
        }
    }

    public static void applyToxicity(ServerLevel level, BlockPos pos, MateriaStack stack, double range, int duration) {
        AABB aabb = new AABB(pos).inflate(range);
        List<Player> players = level.getEntitiesOfClass(Player.class, aabb);
        String toxicitySource = stack.isEmpty() ? "UNKNOWN" : stack.getType().name();
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(ModEffects.MATERIA_TOXICITY, duration, 0));
            ModAttachments.setToxicitySource(player, toxicitySource);
        }
    }

    public static int getMateriaTier(MateriaStack stack) {
        if (stack instanceof MateriaFumusStack) return 2;
        if (stack instanceof MateriaSublimataStack) return 3;
        if (stack instanceof MateriaLiquidaStack) return 4;
        if (stack instanceof MateriaVolatilisStack) return 5;
        if (stack instanceof MateriaCoagulataStack) return 6;
        if (stack instanceof MateriaIchorStack) return 7;
        if (stack instanceof MateriaTransmutataStack) return 8;
        if (stack instanceof MateriaPerfectaStack) return 9;
        if (stack instanceof MateriaLiminaliaStack) return 10;
        return 2;
    }

    /** Fallback local ticking logic for unmanaged/loading nodes. Performs depth-3 BFS. */
    public static void tickLocalEqualization(Level level, BlockPos pos, IPressureHandler host) {
        if (level.isClientSide() || host.getMateriaInTank().isEmpty()) return;

        // Verify thresholds
        checkPressureThresholds(level, pos, host);

        MateriaStack stack = host.getMateriaInTank();
        if (stack.isEmpty()) return;

        // BFS up to depth 3, capped at 12 nodes to avoid performance hit
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Map<BlockPos, Float> pathResistance = new HashMap<>();

        queue.add(pos);
        visited.add(pos);
        pathResistance.put(pos, 0.0f);

        List<BlockPos> targetNodes = new ArrayList<>();

        while (!queue.isEmpty() && visited.size() < 12) {
            BlockPos current = queue.poll();
            BlockEntity currentBE = level.getBlockEntity(current);
            if (!(currentBE instanceof IPressureHandler currentHandler)) continue;

            float currentRes = pathResistance.get(current);
            BlockState state = currentBE.getBlockState();

            for (Direction dir : Direction.values()) {
                BooleanProperty prop = getDirectionProperty(dir);
                if (state.hasProperty(prop) && state.getValue(prop)) {
                    BlockPos neighborPos = current.relative(dir);
                    if (visited.contains(neighborPos)) continue;

                    BlockEntity neighborBE = level.getBlockEntity(neighborPos);
                    if (neighborBE instanceof IPressureHandler neighborHandler) {
                        float totalRes = currentRes + neighborHandler.getResistance();

                        visited.add(neighborPos);
                        pathResistance.put(neighborPos, totalRes);
                        queue.add(neighborPos);

                        if (neighborHandler.getPressure() < host.getPressure()) {
                            targetNodes.add(neighborPos);
                        }
                    }
                }
            }
        }

        if (targetNodes.isEmpty()) return;

        // Sort target nodes by pressure (lowest first)
        targetNodes.sort(Comparator.comparingDouble(p -> ((IPressureHandler) level.getBlockEntity(p)).getPressure()));

        int amountToDistribute = stack.getAmount();
        float myPressure = host.getPressure();

        for (BlockPos targetPos : targetNodes) {
            BlockEntity targetBE = level.getBlockEntity(targetPos);
            if (!(targetBE instanceof IPressureHandler target)) continue;

            MateriaStack targetMateria = target.getMateriaInTank();
            if (!targetMateria.isEmpty() && targetMateria.getType() != stack.getType()) continue;

            // Flow math
            float res = pathResistance.get(targetPos);
            float scale = 1.0f / (1.0f + res);
            float deltaP = myPressure - target.getPressure();

            if (deltaP > 1e-6f) {
                int amountB = targetMateria.getAmount();
                boolean canFlow = (amountB == 0) || (host.getActiveBoostStrength() > 0.0f) || (amountToDistribute - amountB >= 2);
                if (canFlow) {
                    int flow = (int) (amountToDistribute * 0.5f * deltaP * scale);
                    flow = Math.min(flow, getTransferRate((BlockEntity) host));
                    flow = Math.min(flow, amountToDistribute);
                    flow = Math.max(1, flow);

                    if (flow > 0) {
                        MateriaStack toSend = stack.copy();
                        toSend.setAmount(flow);
                        int accepted = target.fill(toSend, false);
                        if (accepted > 0) {
                            host.drain(accepted, false);
                            amountToDistribute -= accepted;
                            myPressure = host.getPressure();
                            if (amountToDistribute <= 0) break;
                        }
                    }
                }
            }
        }
    }

    private static BooleanProperty getDirectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> BlockStateProperties.NORTH;
            case SOUTH -> BlockStateProperties.SOUTH;
            case EAST -> BlockStateProperties.EAST;
            case WEST -> BlockStateProperties.WEST;
            case UP -> BlockStateProperties.UP;
            case DOWN -> BlockStateProperties.DOWN;
        };
    }

    private static int getTransferRate(BlockEntity be) {
        try {
            var method = be.getClass().getMethod("getTransferRate");
            return (int) method.invoke(be);
        } catch (Exception ignored) {}
        return 20;
    }
}
