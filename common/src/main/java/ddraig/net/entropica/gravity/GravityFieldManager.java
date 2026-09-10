package ddraig.net.entropica.gravity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GravityFieldManager {

    private static final Map<ResourceLocation, GravityField> FIELDS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<ResourceLocation>> ENTITY_AFFECTED_FIELDS = new ConcurrentHashMap<>();

    public static void registerField(GravityField field) {
        FIELDS.put(field.getId(), field);
    }

    public static void unregisterField(ResourceLocation id) {
        FIELDS.remove(id);
    }

    public static Collection<GravityField> getAllFields() {
        return FIELDS.values();
    }

    public static GravityField getField(ResourceLocation id) {
        return FIELDS.get(id);
    }

    /**
     * Finds the nearest active singularity field to a given position within maxDistance.
     */
    public static GravityField getNearestSingularity(Vec3 pos, double maxDistance) {
        return getNearestSingularity(null, pos, maxDistance);
    }

    /**
     * Finds the nearest active singularity field to a given position within maxDistance in the given level.
     */
    public static GravityField getNearestSingularity(Level level, Vec3 pos, double maxDistance) {
        GravityField nearest = null;
        double bestDistSqr = maxDistance * maxDistance;

        for (GravityField field : FIELDS.values()) {
            if (field.getMode() == GravityField.Mode.SINGULARITY) {
                if (level != null && field.getDimension() != null && !field.getDimension().equals(level.dimension())) {
                    continue;
                }
                double dSqr = field.getCenterVec().distanceToSqr(pos);
                if (dSqr <= bestDistSqr) {
                    bestDistSqr = dSqr;
                    nearest = field;
                }
            }
        }
        return nearest;
    }

    /**
     * Ticks in-world gravitational fields for a server level.
     */
    public static void tickLevel(ServerLevel level) {
        Iterator<Map.Entry<ResourceLocation, GravityField>> it = FIELDS.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<ResourceLocation, GravityField> entry = it.next();
            GravityField field = entry.getValue();

            if (field.getDimension() != null && !field.getDimension().equals(level.dimension())) {
                continue;
            }

            field.tickAge();
            if (field.isExpired()) {
                it.remove();
                continue;
            }

            AABB box = field.getBoundingBox();
            List<Entity> entities = level.getEntities((Entity) null, box, e -> e.isAlive() || e instanceof ItemEntity || e instanceof Projectile);

            for (Entity entity : entities) {
                if (!field.contains(entity.position())) continue;

                if (entity instanceof LivingEntity living) {
                    if (GravityApi.isAnchored(living)) continue;

                    switch (field.getMode()) {
                        case ZERO_G -> GravityApi.setGravity(living, field.getId(), GravityApi.ZERO_GRAVITY);
                        case LUNAR -> GravityApi.setGravity(living, field.getId(), GravityApi.MOON_GRAVITY);
                        case INVERSION -> GravityApi.setGravity(living, field.getId(), GravityApi.INVERTED_GRAVITY);
                        case SINGULARITY -> {
                            // Inward vortex pull on living entity
                            Vec3 toCenter = field.getCenterVec().subtract(living.position());
                            double dist = toCenter.length();
                            if (dist > 0.5) {
                                double pull = 0.08 * field.getStrength() * (1.0 - (dist / field.getRadius()));
                                living.setDeltaMovement(living.getDeltaMovement().add(toCenter.normalize().scale(pull)));
                                living.hasImpulse = true;
                            }
                        }
                        case GRAV_LIFT -> {
                            // Directional propulsion beam
                            GravityApi.setGravity(living, field.getId(), GravityApi.ZERO_GRAVITY);
                            Direction dir = field.getLiftDirection();
                            Vec3 normal = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
                            // Apply smooth travel velocity along the beam axis
                            living.setDeltaMovement(normal.scale(0.35 * field.getStrength()));
                            living.resetFallDistance();
                            living.hasImpulse = true;
                        }
                        case REPULSOR -> {
                            // Outward repulsion shockwave
                            Vec3 away = living.position().subtract(field.getCenterVec());
                            double dist = away.length();
                            if (dist > 0.1) {
                                double push = 0.12 * field.getStrength() * (1.0 - (dist / field.getRadius()));
                                living.setDeltaMovement(living.getDeltaMovement().add(away.normalize().scale(push)));
                                living.hasImpulse = true;
                            }
                        }
                        case TIDAL_PULSE -> {
                            // 80-tick respiration cycle: 60 ticks float (lunar), 20 ticks heavy crush slam
                            int cycleTick = field.getAgeTicks() % 80;
                            if (cycleTick < 60) {
                                GravityApi.setGravity(living, field.getId(), GravityApi.MOON_GRAVITY);
                            } else {
                                GravityApi.setGravity(living, field.getId(), GravityApi.CRUSH_GRAVITY);
                                living.setDeltaMovement(living.getDeltaMovement().add(0, -0.15, 0));
                                living.hasImpulse = true;
                            }
                        }
                    }

                    // Track affected fields for cleanup
                    ENTITY_AFFECTED_FIELDS.computeIfAbsent(living.getUUID(), k -> ConcurrentHashMap.newKeySet()).add(field.getId());

                } else if (entity instanceof Projectile projectile) {
                    if (field.getMode() == GravityField.Mode.SINGULARITY) {
                        // Projectile Lensing: bends trajectory towards singularity center
                        Vec3 toCenter = field.getCenterVec().subtract(projectile.position());
                        double dist = toCenter.length();
                        if (dist > 0.2) {
                            double pullForce = 0.14 * field.getStrength() * (1.0 - (dist / field.getRadius()));
                            Vec3 bent = projectile.getDeltaMovement().add(toCenter.normalize().scale(pullForce));
                            projectile.setDeltaMovement(bent);
                            projectile.hasImpulse = true;
                        }
                    } else if (field.getMode() == GravityField.Mode.REPULSOR) {
                        // Repulsor Dome: Deflects projectiles outward
                        Vec3 away = projectile.position().subtract(field.getCenterVec()).normalize().scale(0.8 * field.getStrength());
                        projectile.setDeltaMovement(away);
                        projectile.hasImpulse = true;
                    }

                } else if (entity instanceof ItemEntity item) {
                    if (field.getMode() == GravityField.Mode.ZERO_G) {
                        // Items float and bob gently in Zero-G: counteract vanilla -0.04 downward gravity
                        Vec3 m = item.getDeltaMovement();
                        double bob = Math.sin((item.tickCount + item.getId()) * 0.1) * 0.015;
                        item.setDeltaMovement(m.x * 0.92, 0.04 + bob, m.z * 0.92);
                        item.resetFallDistance();
                        item.hasImpulse = true;
                    } else if (field.getMode() == GravityField.Mode.SINGULARITY) {
                        // Items sucked toward the accretion core
                        Vec3 toCenter = field.getCenterVec().subtract(item.position());
                        double dist = toCenter.length();
                        if (dist > 0.2) {
                            item.setDeltaMovement(item.getDeltaMovement().add(toCenter.normalize().scale(0.08)));
                            item.hasImpulse = true;
                        }
                    }
                }
            }
        }

        // Apply graviton soles traction / fall dampening for all active players in this level
        level.players().forEach(GravityApi::tickGravitonSoles);
    }

    /**
     * Cleans up gravity overrides on entities that have exited field perimeters.
     */
    public static void cleanupExitedEntities(ServerLevel level) {
        Iterator<Map.Entry<UUID, Set<ResourceLocation>>> it = ENTITY_AFFECTED_FIELDS.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, Set<ResourceLocation>> entry = it.next();
            UUID entityId = entry.getKey();
            Entity entity = level.getEntity(entityId);

            if (entity instanceof LivingEntity living) {
                Iterator<ResourceLocation> fieldIt = entry.getValue().iterator();
                while (fieldIt.hasNext()) {
                    ResourceLocation fieldId = fieldIt.next();
                    GravityField field = FIELDS.get(fieldId);
                    if (field == null || !field.getDimension().equals(level.dimension()) || !field.contains(living.position())) {
                        GravityApi.resetGravity(living, fieldId);
                        fieldIt.remove();
                    }
                }
                if (entry.getValue().isEmpty()) {
                    it.remove();
                }
            } else if (entity == null) {
                // Only purge if entity does not exist in any loaded dimension on the server
                boolean existsInOtherDimension = false;
                if (level.getServer() != null) {
                    for (ServerLevel otherLevel : level.getServer().getAllLevels()) {
                        if (otherLevel != level && otherLevel.getEntity(entityId) != null) {
                            existsInOtherDimension = true;
                            break;
                        }
                    }
                }
                if (!existsInOtherDimension) {
                    it.remove();
                }
            } else if (!entity.isAlive()) {
                it.remove();
            }
        }
    }
}
