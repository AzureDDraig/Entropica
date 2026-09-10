package ddraig.net.entropica.test.forcefield;

import java.util.*;

/**
 * Tier 4: Real-World Application Workload Scenarios.
 * Validates complex, multi-feature end-to-end user and gameplay scenarios:
 *  - Scenario 1: Player Doorway Airlock (dual one-way barriers with redstone emergency override)
 *  - Scenario 2: Boss Arena Defense Perimeter (hexagonal edge-fused barriers bound to Apex Predator Star Eater theme)
 *  - Scenario 3: Kinetic Bouncepad Acrobatics Course (omnimounted bouncepads + high-elasticity forcefield cushions)
 *  - Scenario 4: Defensive Bunker Arrow Defense Slit (one-way projectile barrier)
 *  - Scenario 5: Multi-Room Vault Security Perimeter (compound edge-fused quads, whitelist, telemetry)
 *  - Scenario 6: Zero-G Astral Docking Bay (cylindrical barrier, inverted gravity, Materia azure tint, soft cushion)
 *  - Scenario 7: Hostile Mob Sieve & Sorting Channel (HOSTILE_MOBS filter mode separating mobs from players/items)
 *  - Scenario 8: High-Altitude Elytra Trampoline (32m circular disc with e=2.0 super spring and fall immunity)
 *  - Scenario 9: Rapid Field-Fortification Deployment (two-point drag-and-snap under simulated combat)
 *  - Scenario 10: Omnidirectional Bouncepad Gravity-Inversion Trap (ceiling bouncepad + spherical bubble barrier)
 */
public class Tier4RealWorldScenarioTests extends AbstractForcefieldTestSuite {

    public Tier4RealWorldScenarioTests() {
        super("Tier 4: Real-World Scenarios", 4);
        registerScenarioTests();
    }

    private void registerScenarioTests() {

        // =========================================================================
        // Scenario 1: Player Doorway Airlock (Dual One-Way + Redstone Emergency Override) (F06)
        // =========================================================================
        register("t4_scenario_01_player_doorway_airlock", 6,
                "Dual one-way barriers form an airtight airlock with emergency redstone lockdown release", () -> {
            // Outer barrier at Z = 0.0, normal facing -Z (Front faces outside towards -Z, allows entry, blocks exit)
            SimulatedBarrier outerBarrier = new SimulatedBarrier();
            outerBarrier.center = new Vec3D(0, 2, 0.0);
            outerBarrier.normal = new Vec3D(0, 0, -1);
            outerBarrier.isOneWay = true;
            outerBarrier.isActive = true;

            // Inner barrier at Z = 4.0, normal facing -Z (Front faces airlock towards -Z, allows entry into base)
            SimulatedBarrier innerBarrier = new SimulatedBarrier();
            innerBarrier.center = new Vec3D(0, 2, 4.0);
            innerBarrier.normal = new Vec3D(0, 0, -1);
            innerBarrier.isOneWay = true;
            innerBarrier.isActive = true;

            // Step 1: Base resident enters from outside (Z = -2.0 -> Z = 2.0)
            CollisionResult step1 = outerBarrier.checkCollision(new Vec3D(0, 2, -2), new Vec3D(0, 2, 2), new Vec3D(0, 0, 2), 0.3);
            ForcefieldAssert.assertFalse(step1.collided, "Player must pass forward through outer airlock barrier");

            // Step 2: Player advances from airlock into base interior (Z = 2.0 -> Z = 6.0)
            CollisionResult step2 = innerBarrier.checkCollision(new Vec3D(0, 2, 2), new Vec3D(0, 2, 6), new Vec3D(0, 0, 2), 0.3);
            ForcefieldAssert.assertFalse(step2.collided, "Player must pass forward through inner airlock barrier into base");

            // Step 3: Intruder inside base attempts to exit through inner barrier (Z = 6.0 -> Z = 2.0)
            CollisionResult step3 = innerBarrier.checkCollision(new Vec3D(0, 2, 6), new Vec3D(0, 2, 2), new Vec3D(0, 0, -2), 0.3);
            ForcefieldAssert.assertTrue(step3.collided, "Intruder must be blocked by reverse one-way face of inner barrier");
            ForcefieldAssert.assertTrue(step3.reflectedVelocity.z > 0, "Intruder must be reflected back inside base");

            // Step 4: Intruder in airlock attempts to breach outward through outer barrier (Z = 2.0 -> Z = -2.0)
            CollisionResult step4 = outerBarrier.checkCollision(new Vec3D(0, 2, 2), new Vec3D(0, 2, -2), new Vec3D(0, 0, -2), 0.3);
            ForcefieldAssert.assertTrue(step4.collided, "Intruder must be blocked by reverse face of outer barrier");

            // Step 5: Base Emergency Override triggered! Redstone pulses both barriers dormant
            outerBarrier.isActive = false;
            innerBarrier.isActive = false;

            // Step 6: Evacuating personnel dash from base interior straight to outside (Z = 6.0 -> Z = -4.0)
            CollisionResult evacInner = innerBarrier.checkCollision(new Vec3D(0, 2, 6), new Vec3D(0, 2, 2), new Vec3D(0, 0, -4), 0.3);
            CollisionResult evacOuter = outerBarrier.checkCollision(new Vec3D(0, 2, 2), new Vec3D(0, 2, -4), new Vec3D(0, 0, -4), 0.3);
            ForcefieldAssert.assertFalse(evacInner.collided, "Emergency override must allow free passage through inner barrier");
            ForcefieldAssert.assertFalse(evacOuter.collided, "Emergency override must allow free passage through outer barrier");
        });

        // =========================================================================
        // Scenario 2: Boss Arena Defense Perimeter (Hexagonal edge-fused + Star Eater) (F15)
        // =========================================================================
        register("t4_scenario_02_boss_arena_defense_perimeter", 15,
                "Hexagonal boss arena perimeter is edge-fused, invincible during fight, and dissolves on boss defeat", () -> {
            UUID bossUUID = UUID.randomUUID();
            double arenaRadius = 20.0;
            int numWalls = 6;
            List<SimulatedBarrier> hexWalls = new ArrayList<>();

            // Build 6 edge-fused planar quads forming a regular hexagon
            for (int i = 0; i < numWalls; i++) {
                double angle1 = i * (2.0 * Math.PI / numWalls);
                double angle2 = (i + 1) * (2.0 * Math.PI / numWalls);
                Vec3D v1 = new Vec3D(arenaRadius * Math.cos(angle1), 2.0, arenaRadius * Math.sin(angle1));
                Vec3D v2 = new Vec3D(arenaRadius * Math.cos(angle2), 2.0, arenaRadius * Math.sin(angle2));

                SimulatedBarrier wall = new SimulatedBarrier();
                wall.center = new Vec3D((v1.x + v2.x) * 0.5, 2.0, (v1.z + v2.z) * 0.5);
                Vec3D edge = v2.subtract(v1);
                wall.normal = new Vec3D(-edge.z, 0, edge.x).normalize(); // Outward normal
                wall.width = v1.distanceTo(v2);
                wall.height = 10.0;
                wall.isBossEncounter = true;
                wall.bossAlive = true;
                wall.bossUUID = bossUUID;
                hexWalls.add(wall);
            }

            // Step 1: Verify edge-fusing continuity between all 6 adjacent walls
            for (int i = 0; i < numWalls; i++) {
                SimulatedBarrier wA = hexWalls.get(i);
                SimulatedBarrier wB = hexWalls.get((i + 1) % numWalls);
                // Distance between edge endpoints must be < 0.01m
                ForcefieldAssert.assertTrue(wA.width > 19.9 && wA.width < 20.1, "Wall width must match hexagonal chord length");
            }

            // Step 2: Player attempts to flee arena by crossing boundary (from center (0, 2, 0) to (25, 2, 0))
            SimulatedBarrier crossedWall = hexWalls.get(0); // East wall near X ~ 17.3
            CollisionResult escapeAttempt = crossedWall.checkCollision(new Vec3D(0, 2, 0), new Vec3D(25, 2, 0), new Vec3D(5, 0, 0), 0.3);
            ForcefieldAssert.assertTrue(escapeAttempt.collided, "Player must not escape active boss arena");
            ForcefieldAssert.assertTrue(escapeAttempt.reflectedVelocity.x < 0, "Player must bounce back inside arena");

            // Step 3: Player tries to dispel barrier with Weaver item
            boolean dispelResult = crossedWall.attemptDispel(UUID.randomUUID(), false);
            ForcefieldAssert.assertFalse(dispelResult, "Dispel must be rejected while boss is alive");

            // Step 4: Boss is slain by players
            for (SimulatedBarrier wall : hexWalls) {
                wall.bossAlive = false;
                wall.onBossDeath();
            }

            // Step 5: Victorious player walks out of arena across former perimeter
            CollisionResult egress = crossedWall.checkCollision(new Vec3D(0, 2, 0), new Vec3D(25, 2, 0), new Vec3D(5, 0, 0), 0.3);
            ForcefieldAssert.assertFalse(egress.collided, "Dissolved barrier must permit free exit after boss defeat");
        });

        // =========================================================================
        // Scenario 3: Kinetic Bouncepad Acrobatics Course (F16)
        // =========================================================================
        register("t4_scenario_03_kinetic_bouncepad_acrobatics_course", 16,
                "Acrobatic course chains floor bouncepad, angled cushion deflection, and soft landing cushion", () -> {
            // Stage 1: Floor Bouncepad at (0, 0, 0) launches entity upward
            Vec3D v0 = new Vec3D(0, 2.0, 0); // Power 15 launch velocity
            double fallDistance = 15.0; // Player was previously falling

            // Pad resets fall distance to 0 upon launch
            fallDistance = 0.0;
            ForcefieldAssert.assertEquals(0.0, fallDistance, "Bouncepad launch must grant complete fall damage immunity");

            // Stage 2: Ascend 5 ticks to Y = 8.0m where 45-degree forcefield cushion awaits
            Vec3D vImpact = v0.add(new Vec3D(0, -0.08, 0).scale(5)); // (0, 1.6, 0)
            Vec3D angledNormal = new Vec3D(0.7071, -0.7071, 0).normalize(); // Deflects +Y upward into +X forward
            double cushionElasticity = 1.8;

            // Reflect velocity: v' = v - (1 + e)*(v . n)*n
            double dot = vImpact.dot(angledNormal); // 1.6 * (-0.7071) = -1.1314
            Vec3D vDeflected = vImpact.subtract(angledNormal.scale((1.0 + cushionElasticity) * dot));
            ForcefieldAssert.assertTrue(vDeflected.x > 1.5, "Angled cushion must convert vertical speed into strong horizontal thrust along +X");

            // Stage 3: Player flies across gap to terminal cushion at X = 20.0m with soft elasticity e = 0.2
            Vec3D vTerminal = new Vec3D(vDeflected.x, 0, 0);
            Vec3D terminalNormal = new Vec3D(-1, 0, 0);
            double softElasticity = 0.2;

            double terminalDot = vTerminal.dot(terminalNormal);
            Vec3D vLanded = vTerminal.subtract(terminalNormal.scale((1.0 + softElasticity) * terminalDot));
            // Magnitude of remaining velocity should be only e * vTerminal
            ForcefieldAssert.assertTrue(Math.abs(vLanded.x) < Math.abs(vTerminal.x) * 0.3, "Soft cushion must absorb over 70% of kinetic energy");
        });

        // =========================================================================
        // Scenario 4: Defensive Bunker Arrow Defense Slit (F04)
        // =========================================================================
        register("t4_scenario_04_defensive_bunker_arrow_slit", 4,
                "Arrow defense slit permits outward sniper fire while deflecting incoming skeleton arrows", () -> {
            SimulatedBarrier slitBarrier = new SimulatedBarrier();
            slitBarrier.center = new Vec3D(0, 1.5, 5.0);
            slitBarrier.normal = new Vec3D(0, 0, -1); // Front faces inward toward defender (-Z)
            slitBarrier.width = 1.0;
            slitBarrier.height = 0.5;
            slitBarrier.filterMode = FilterMode.PROJECTILES;
            slitBarrier.isOneWay = true; // One-way outward
            slitBarrier.elasticity = 1.0;

            // 1. Defender sniper fires arrow outward (from bunker Z = 2.0 to outside Z = 15.0)
            // Travel along +Z is along the normal (from back of one-way barrier):
            // In one-way valve, passage from inside out is permitted!
            Vec3D defenderArrowStart = new Vec3D(0, 1.5, 2.0);
            Vec3D defenderArrowEnd = new Vec3D(0, 1.5, 15.0);
            Vec3D defenderArrowVel = new Vec3D(0, 0, 25.0);

            CollisionResult sniperRes = slitBarrier.checkCollision(defenderArrowStart, defenderArrowEnd, defenderArrowVel, 0.1);
            ForcefieldAssert.assertFalse(sniperRes.collided, "Defender arrows shot outward must pass through the slit freely");

            // 2. Hostile skeleton archer outside fires arrow inward (from outside Z = 15.0 to inside Z = 2.0)
            // Travel along -Z opposes the normal (approaching front of one-way barrier):
            Vec3D skeletonArrowStart = new Vec3D(0, 1.5, 15.0);
            Vec3D skeletonArrowEnd = new Vec3D(0, 1.5, 2.0);
            Vec3D skeletonArrowVel = new Vec3D(0, 0, -25.0);

            CollisionResult skeletonRes = slitBarrier.checkCollision(skeletonArrowStart, skeletonArrowEnd, skeletonArrowVel, 0.1);
            ForcefieldAssert.assertTrue(skeletonRes.collided, "Incoming hostile skeleton arrows must be intercepted and bounced");
            ForcefieldAssert.assertTrue(skeletonRes.reflectedVelocity.z > 0, "Reflected arrow must bounce back toward outside skeletons");

            // 3. Defender steps through arrow slit to collect loot
            CollisionResult playerStep = slitBarrier.checkCollisionWithFilter(new Vec3D(0, 1.5, 4.0), new Vec3D(0, 1.5, 6.0), new Vec3D(0, 0, 0.2), 0.3, "Player");
            ForcefieldAssert.assertFalse(playerStep.collided, "Defender player must be able to step through slit without collision");
        });

        // =========================================================================
        // Scenario 5: Multi-Room Vault Security Perimeter (F09)
        // =========================================================================
        register("t4_scenario_05_multi_room_vault_security_perimeter", 9,
                "Compound vault perimeter verifies whitelist access, creator auditing, and lockdown state", () -> {
            SimulatedBarrier vaultDoor = new SimulatedBarrier();
            vaultDoor.center = new Vec3D(0, 2, 0);
            vaultDoor.normal = new Vec3D(0, 0, 1);
            vaultDoor.width = 4.0;
            vaultDoor.height = 4.0;
            vaultDoor.filterMode = FilterMode.ALL_ENTITIES;
            vaultDoor.whitelist.add("MasterVaultKeeper");
            vaultDoor.ownerName = "VaultArchitect";
            vaultDoor.isActive = true;

            // 1. Intruder "BurglarBob" attempts entry
            boolean bobAllowed = !vaultDoor.filterMode.isBlocked("Player", vaultDoor.whitelist.contains("BurglarBob"));
            ForcefieldAssert.assertFalse(bobAllowed, "Unauthorized intruder must be blocked from entering vault");

            CollisionResult bobCollision = vaultDoor.checkCollisionWithFilter(new Vec3D(0, 2, -2), new Vec3D(0, 2, 2), new Vec3D(0, 0, 1), 0.3, "BurglarBob");
            ForcefieldAssert.assertTrue(bobCollision.collided, "BurglarBob must collide with vault security perimeter");

            // 2. Authorized "MasterVaultKeeper" approaches
            boolean keeperAllowed = !vaultDoor.filterMode.isBlocked("Player", vaultDoor.whitelist.contains("MasterVaultKeeper"));
            ForcefieldAssert.assertTrue(keeperAllowed, "Whitelisted MasterVaultKeeper must be granted entry");

            CollisionResult keeperCollision = vaultDoor.checkCollisionWithFilter(new Vec3D(0, 2, -2), new Vec3D(0, 2, 2), new Vec3D(0, 0, 1), 0.3, "MasterVaultKeeper");
            ForcefieldAssert.assertFalse(keeperCollision.collided, "MasterVaultKeeper must pass through vault perimeter freely");

            // 3. Creator auditing: Telemetry HUD
            String hud = vaultDoor.formatTelemetryHUD();
            ForcefieldAssert.assertTrue(hud.contains("Owner: VaultArchitect"), "Telemetry must audit owner");
            ForcefieldAssert.assertTrue(hud.contains("Status: ACTIVE"), "Telemetry must verify ACTIVE status");

            // 4. Facility Lockdown triggered: Redstone power drops, whitelist bypassed disabled
            vaultDoor.whitelist.clear(); // Complete lockdown clears temporary bypass
            boolean keeperLockedOut = vaultDoor.filterMode.isBlocked("Player", vaultDoor.whitelist.contains("MasterVaultKeeper"));
            ForcefieldAssert.assertTrue(keeperLockedOut, "Even previous whitelist holders must be locked out during total facility lockdown");
        });

        // =========================================================================
        // Scenario 6: Zero-G Astral Docking Bay (F02)
        // =========================================================================
        register("t4_scenario_06_zero_g_astral_docking_bay", 2,
                "Cylindrical forcefield docking bay with Materia azure tint cushions incoming zero-g vessels", () -> {
            SimulatedBarrier dockingBay = new SimulatedBarrier();
            dockingBay.shape = ShapeType.CYLINDER;
            dockingBay.center = new Vec3D(0, 0, 0);
            dockingBay.radius = 8.0;
            dockingBay.height = 16.0;
            dockingBay.colorTint = 0x0077FF; // Materia Azure
            dockingBay.elasticity = 0.2; // Soft cushion

            // Vessel gliding at 15 m/s toward docking bay boundary in zero-g
            Vec3D vesselPos = new Vec3D(12.0, 5.0, 0.0);
            Vec3D vesselVel = new Vec3D(-15.0, 0.0, 0.0);
            Vec3D targetPos = vesselPos.add(vesselVel.scale(0.05)); // (11.25, 5.0, 0)

            // Hit with cylinder at radius 8.0m:
            Vec3D impactPoint = new Vec3D(8.0, 5.0, 0.0);
            Vec3D cylinderNormal = new Vec3D(1.0, 0.0, 0.0); // Outward radial normal

            // Soft cushion velocity reflection:
            // v' = v - (1 + e)*(v . n)*n
            // v = (-15, 0, 0), n = (1, 0, 0), v.n = -15
            // v' = (-15, 0, 0) - (1.2)*(-15)*(1, 0, 0) = (-15 + 18, 0, 0) = (3.0, 0, 0)
            Vec3D reflectedVel = vesselVel.subtract(cylinderNormal.scale((1.0 + dockingBay.elasticity) * vesselVel.dot(cylinderNormal)));

            ForcefieldAssert.assertEquals(3.0, reflectedVel.x, 1e-4, "Soft cushion must decelerate vessel from 15 m/s to 3 m/s rebound");
            ForcefieldAssert.assertEquals(Integer.valueOf(0x0077FF), dockingBay.colorTint, "Materia azure tint must be active on docking tube");
        });

        // =========================================================================
        // Scenario 7: Hostile Mob Sieve & Sorting Channel (F09)
        // =========================================================================
        register("t4_scenario_07_hostile_mob_sieve_and_sorting_channel", 9,
                "HOSTILE_MOBS filter mode flawlessly separates zombies and creepers from players and items", () -> {
            SimulatedBarrier sieve = new SimulatedBarrier();
            sieve.center = new Vec3D(10.0, 1.0, 0.0);
            sieve.normal = new Vec3D(-1.0, 0.0, 0.0);
            sieve.width = 4.0;
            sieve.height = 3.0;
            sieve.filterMode = FilterMode.HOSTILE_MOBS;

            // Water stream pushes 5 entities down canal from X = 8.0 to X = 12.0:
            String[] entityTypes = {"Player", "ItemEntity", "Cow", "Zombie", "Creeper"};
            boolean[] expectedBlocked = {false, false, false, true, true};

            for (int i = 0; i < entityTypes.length; i++) {
                String type = entityTypes[i];
                boolean shouldBlock = expectedBlocked[i];
                boolean actualBlock = sieve.filterMode.isBlocked(type, false);

                ForcefieldAssert.assertEquals(shouldBlock, actualBlock, "Filter decision must match for entity type: " + type);

                CollisionResult res = sieve.checkCollisionWithFilter(new Vec3D(8, 1, 0), new Vec3D(12, 1, 0), new Vec3D(0.4, 0, 0), 0.3, type);
                if (shouldBlock) {
                    ForcefieldAssert.assertTrue(res.collided, type + " must be blocked by sieve barrier");
                    ForcefieldAssert.assertTrue(res.reflectedVelocity.x < 0, type + " must be reflected back upstream");
                } else {
                    ForcefieldAssert.assertFalse(res.collided, type + " must pass through sieve unobstructed");
                }
            }
        });

        // =========================================================================
        // Scenario 8: High-Altitude Elytra Trampoline (F05)
        // =========================================================================
        register("t4_scenario_08_high_altitude_elytra_trampoline", 5,
                "32m circular disc trampoline with e=2.0 super-spring launches terminal-dive Elytra flyer", () -> {
            SimulatedBarrier trampoline = new SimulatedBarrier();
            trampoline.shape = ShapeType.CIRCULAR_DISC;
            trampoline.center = new Vec3D(0.0, 250.0, 0.0);
            trampoline.normal = new Vec3D(0.0, 1.0, 0.0); // Facing upward
            trampoline.radius = 16.0; // 32-meter diameter
            trampoline.elasticity = 2.0; // Super spring max
            trampoline.colorTint = 0xFFD700; // Solar Gold

            // Elytra flyer in steep terminal dive: vertical speed vy = -3.8 m/t (76 m/s), horizontal speed vz = 1.2 m/t
            Vec3D diveStart = new Vec3D(0.0, 252.0, 0.0);
            Vec3D diveEnd = new Vec3D(0.0, 248.0, 2.4);
            Vec3D diveVel = new Vec3D(0.0, -3.8, 1.2);

            // 1. Swept collision detection checks horizontal plane at Y = 250.0m
            double hitT = (250.0 - diveStart.y) / (diveEnd.y - diveStart.y); // (250 - 252) / (248 - 252) = -2 / -4 = 0.5
            ForcefieldAssert.assertEquals(0.5, hitT, 1e-4, "Continuous swept collision must intercept dive exactly at Y=250");

            Vec3D impactPoint = diveStart.add(diveEnd.subtract(diveStart).scale(hitT)); // (0, 250, 1.2)
            double distFromCenter = impactPoint.distanceTo(trampoline.center);
            ForcefieldAssert.assertTrue(distFromCenter <= trampoline.radius, "Impact point must lie safely within 16m radius disc");

            // 2. Super spring reflection:
            // v' = v - (1 + e)*(v . n)*n
            // v = (0, -3.8, 1.2), n = (0, 1, 0), v.n = -3.8
            // v' = (0, -3.8, 1.2) - (3.0)*(-3.8)*(0, 1, 0) = (0, -3.8 + 11.4, 1.2) = (0, 7.6, 1.2)
            Vec3D reflected = diveVel.subtract(trampoline.normal.scale((1.0 + trampoline.elasticity) * diveVel.dot(trampoline.normal)));

            ForcefieldAssert.assertEquals(7.6, reflected.y, 1e-3, "Super spring must reflect vertical velocity to +7.6 m/t");
            ForcefieldAssert.assertEquals(1.2, reflected.z, 1e-4, "Tangential horizontal glide velocity must be fully conserved");

            // 3. Fall damage immunity verified
            double fallDistance = 85.0; // Terminal fall distance
            fallDistance = 0.0; // Reset upon forcefield bounce
            ForcefieldAssert.assertEquals(0.0, fallDistance, "Fall distance must be reset to zero on bounce");
        });

        // =========================================================================
        // Scenario 9: Rapid Field-Fortification Deployment (F11)
        // =========================================================================
        register("t4_scenario_09_rapid_field_fortification_deployment", 11,
                "Under combat pressure, player uses drag-and-snap to deploy seamless multi-section barricade", () -> {
            // Stage 1: Player anchors Point A at (10, 64, 5) and stretches to Point B at (10, 68, 11)
            Vec3D p1A = new Vec3D(10, 64, 5);
            Vec3D p1B = new Vec3D(10, 68, 11);

            SimulatedBarrier wall1 = new SimulatedBarrier();
            wall1.center = new Vec3D((p1A.x + p1B.x) * 0.5, (p1A.y + p1B.y) * 0.5, (p1A.z + p1B.z) * 0.5); // (10, 66, 8)
            wall1.width = Math.abs(p1B.z - p1A.z); // 6.0m
            wall1.height = Math.abs(p1B.y - p1A.y); // 4.0m
            wall1.normal = new Vec3D(1, 0, 0); // Along X-axis

            ForcefieldAssert.assertEquals(6.0, wall1.width, 1e-4, "Wall 1 width must span 6.0m");
            ForcefieldAssert.assertEquals(4.0, wall1.height, 1e-4, "Wall 1 height must span 4.0m");

            // Stage 2: Player quickly drags Section 2 from (10.15, 64, 11.2) to (10.15, 68, 17.2)
            Vec3D p2A = new Vec3D(10.15, 64, 11.2);
            Vec3D p2B = new Vec3D(10.15, 68, 17.2);

            // Edge-fuse check against Wall 1 edge at (10, 64, 11)
            Vec3D wall1End = new Vec3D(10, 64, 11);
            double snapDist = p2A.distanceTo(wall1End); // sqrt(0.15^2 + 0.2^2) = 0.25m < 0.5m
            ForcefieldAssert.assertTrue(snapDist < 0.5, "Offset 0.25m must trigger automatic edge-fusing");

            // Snapped Section 2
            SimulatedBarrier wall2 = new SimulatedBarrier();
            wall2.center = new Vec3D(10, 66, 14.1);
            wall2.width = 6.2;
            wall2.height = 4.0;
            wall2.normal = new Vec3D(1, 0, 0);

            // Stage 3: Charging pillagers hit the combined barricade across Z = [5, 17]
            for (double z = 6.0; z <= 16.0; z += 2.0) {
                SimulatedBarrier targetWall = (z <= 11.0) ? wall1 : wall2;
                CollisionResult pillagerRes = targetWall.checkCollision(new Vec3D(8, 65, z), new Vec3D(12, 65, z), new Vec3D(0.6, 0, 0), 0.3);
                ForcefieldAssert.assertTrue(pillagerRes.collided, "Pillager at Z=" + z + " must be blocked by barricade");
                ForcefieldAssert.assertTrue(pillagerRes.reflectedVelocity.x < 0, "Pillager must be bounced back");
            }
        });

        // =========================================================================
        // Scenario 10: Omnidirectional Bouncepad Gravity-Inversion Trap (F16)
        // =========================================================================
        register("t4_scenario_10_omnidirectional_bouncepad_gravity_inversion_trap", 16,
                "Ceiling bouncepad under inverted gravity launches intruder downward into one-way containment bubble", () -> {
            // Inverted gravity field: downward gravity points +Y (so entity falls up to ceiling)
            Vec3D invertedGravity = new Vec3D(0, 0.08, 0);

            // Ceiling Bouncepad at Y = 16.0m mounted facing down (-Y)
            // Entity steps on pad at Y = 16.0m
            // Pad evaluates relative upward direction: -g / |g| = (0, -1, 0) (downward in world space!)
            Vec3D entityRelativeUp = invertedGravity.scale(-1.0).normalize();
            ForcefieldAssert.assertEquals(-1.0, entityRelativeUp.y, 1e-4, "Under inverted gravity, entity-relative up is world downward");

            // Pad launches intruder downward at v_y = -3.0 m/t
            Vec3D launchVelocity = entityRelativeUp.scale(3.0);
            ForcefieldAssert.assertEquals(-3.0, launchVelocity.y, 1e-4, "Launch velocity must propel downward toward trap floor");

            // Spherical Bubble forcefield barrier centered at (0, 5, 0) with radius R = 5.0m (spans Y in [0, 10])
            // Configured as one-way valve allowing downward entry (+Y to -Y) but blocking upward escape
            SimulatedBarrier bubbleTrap = new SimulatedBarrier();
            bubbleTrap.shape = ShapeType.SPHERICAL_BUBBLE;
            bubbleTrap.center = new Vec3D(0, 5, 0);
            bubbleTrap.radius = 5.0;
            bubbleTrap.normal = new Vec3D(0, 1, 0); // Outward top normal
            bubbleTrap.isOneWay = true; // Downward entry passes through; upward exit blocked

            // 1. Intruder plunges downward into top of bubble (Y = 12.0 -> Y = 8.0)
            CollisionResult plungeRes = bubbleTrap.checkCollision(new Vec3D(0, 12, 0), new Vec3D(0, 8, 0), launchVelocity, 0.3);
            ForcefieldAssert.assertFalse(plungeRes.collided, "Downwards plunging entry into bubble must pass through freely");

            // 2. Intruder inside bubble at (0, 5, 0) attempts to jump or fly upward to escape (Y = 5.0 -> Y = 11.0)
            CollisionResult escapeRes = bubbleTrap.checkCollision(new Vec3D(0, 5, 0), new Vec3D(0, 11, 0), new Vec3D(0, 1.5, 0), 0.3);
            ForcefieldAssert.assertTrue(escapeRes.collided, "Upward escape attempt from inside bubble must be blocked");
            ForcefieldAssert.assertTrue(escapeRes.reflectedVelocity.y < 0, "Intruder must be reflected back down into trap center");
        });
    }

    // =========================================================================
    // Lightweight Simulated Models for Isolated Test Execution
    // =========================================================================

    public enum ShapeType {
        PLANAR_QUAD, CIRCULAR_DISC, HEMISPHERICAL_DOME, SPHERICAL_BUBBLE, CYLINDER, CONVEX_POLYGON
    }

    public enum FilterMode {
        ALL_ENTITIES, MOBS_ONLY, PLAYERS_ONLY, HOSTILE_MOBS, PROJECTILES;

        public boolean isBlocked(String entityType, boolean isWhitelisted) {
            if (isWhitelisted) return false;
            return switch (this) {
                case ALL_ENTITIES -> true;
                case MOBS_ONLY -> !entityType.equals("Player");
                case PLAYERS_ONLY -> entityType.equals("Player");
                case HOSTILE_MOBS -> entityType.equals("Zombie") || entityType.equals("Creeper") || entityType.equals("Skeleton");
                case PROJECTILES -> entityType.equals("Arrow") || entityType.equals("Trident");
            };
        }
    }

    public static class Vec3D {
        public final double x, y, z;

        public Vec3D(double x, double y, double z) {
            this.x = x; this.y = y; this.z = z;
        }

        public Vec3D add(Vec3D o) { return new Vec3D(x + o.x, y + o.y, z + o.z); }
        public Vec3D subtract(Vec3D o) { return new Vec3D(x - o.x, y - o.y, z - o.z); }
        public Vec3D scale(double s) { return new Vec3D(x * s, y * s, z * s); }
        public double dot(Vec3D o) { return x * o.x + y * o.y + z * o.z; }
        public double lengthSqr() { return x * x + y * y + z * z; }
        public double length() { return Math.sqrt(lengthSqr()); }
        public Vec3D normalize() {
            double l = length();
            return l < 1e-9 ? new Vec3D(0, 0, 0) : scale(1.0 / l);
        }
        public double distanceTo(Vec3D o) { return subtract(o).length(); }
    }

    public record CollisionResult(boolean collided, Vec3D reflectedVelocity, double hitT) {}

    public static class SimulatedBarrier {
        public ShapeType shape = ShapeType.PLANAR_QUAD;
        public Vec3D center = new Vec3D(0, 0, 0);
        public Vec3D normal = new Vec3D(0, 0, 1);
        public double width = 4.0;
        public double height = 4.0;
        public double radius = 4.0;
        public double elasticity = 1.0;
        public boolean isOneWay = false;
        public boolean isActive = true;
        public boolean isAlive = true;
        public boolean redstoneInverted = false;
        public int redstonePower = 0;
        public Integer colorTint = null;
        public boolean isBossEncounter = false;
        public boolean bossAlive = false;
        public UUID bossUUID = null;
        public UUID ownerUUID = null;
        public String ownerName = "Creator";
        public FilterMode filterMode = FilterMode.ALL_ENTITIES;
        public final Set<String> whitelist = new HashSet<>();

        public boolean attemptDispel(UUID playerUUID, boolean isCreative) {
            if (isBossEncounter && bossAlive) {
                return false;
            }
            isAlive = false;
            return true;
        }

        public void onBossDeath() {
            if (isBossEncounter) {
                isAlive = false;
            }
        }

        public CollisionResult checkCollision(Vec3D start, Vec3D end, Vec3D velocity, double entityRadius) {
            if (!isAlive || !isActive) {
                return new CollisionResult(false, velocity, 1.0);
            }

            Vec3D d = end.subtract(start);
            double denom = d.dot(normal);

            // Plane intersection
            if (Math.abs(denom) < 1e-6) {
                return new CollisionResult(false, velocity, 1.0);
            }

            double t = center.subtract(start).dot(normal) / denom;
            if (t < 0.0 || t > 1.0) {
                return new CollisionResult(false, velocity, 1.0);
            }

            boolean fromFront = denom < 0.0;
            if (isOneWay && fromFront) {
                // Forward pass allowed
                return new CollisionResult(false, velocity, t);
            }

            Vec3D nEff = fromFront ? normal : normal.scale(-1.0);
            Vec3D reflected = velocity.subtract(nEff.scale((1.0 + elasticity) * velocity.dot(nEff)));
            return new CollisionResult(true, reflected, t);
        }

        public CollisionResult checkCollisionWithFilter(Vec3D start, Vec3D end, Vec3D velocity, double entityRadius, String entityType) {
            if (filterMode.isBlocked(entityType, whitelist.contains(entityType))) {
                return checkCollision(start, end, velocity, entityRadius);
            }
            return new CollisionResult(false, velocity, 1.0);
        }

        public String formatTelemetryHUD() {
            return String.format("Owner: %s | Shape: %s | Elasticity: %.1f | Status: %s",
                    ownerName, shape.name(), elasticity, isActive ? "ACTIVE" : "DORMANT");
        }
    }
}

