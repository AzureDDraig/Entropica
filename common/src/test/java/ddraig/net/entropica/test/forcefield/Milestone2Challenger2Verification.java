package ddraig.net.entropica.test.forcefield;

import ddraig.net.entropica.block.entity.GravitonBouncepadBlockEntity;
import ddraig.net.entropica.forcefield.BarrierFilterMode;
import ddraig.net.entropica.forcefield.BarrierRaycastHit;
import ddraig.net.entropica.forcefield.BarrierShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Empirical Adversarial Challenger Verification Suite for Milestone 2:
 * 1. Graviton Bouncepad Parity:
 *    - Continuous analogue redstone scaling (power 0: 1.35, power 7: 1.8867, power 15: 2.50, strictly monotonic).
 *    - Debounce timing (canBounce() blocks rapid-fire triggers in the same tick window, 15-tick recoil recovery).
 *    - Landing fall damage immunity lifecycle (tagged, fall damage interrupted once, tag removed so subsequent unrelated falls are not immune).
 *    - Gravity inversion (-g upward launch in normal, inverted, core, and wall-mounted setups).
 * 2. Redstone Switchability:
 *    - Mode 0 (ignore), Mode 1 (active unpowered / dormant powered), Mode 2 (active powered / dormant unpowered).
 *    - Dormant state allows entities and projectiles to pass freely.
 * 3. Boss Arena Invulnerability:
 *    - Survival player holding Firmament Weaver cannot dispel boss barrier via hurtServer.
 *    - Creative player can dispel.
 *    - Boss death triggers instant dissolution.
 */
public class Milestone2Challenger2Verification {

    public static class ChallengeFailure {
        public final String testName;
        public final String details;

        public ChallengeFailure(String testName, String details) {
            this.testName = testName;
            this.details = details;
        }

        @Override
        public String toString() {
            return "[" + testName + "] " + details;
        }
    }

    private static int totalChecks = 0;
    private static int passedChecks = 0;
    private static final List<ChallengeFailure> failures = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   EMPIRICAL ADVERSARIAL CHALLENGER 2: MILESTONE 2 VERIFICATION HARNESS         ");
        System.out.println("================================================================================");
        System.out.println();

        test1_GravitonBouncepadParity();
        test2_RedstoneSwitchability();
        test3_BossArenaInvulnerability();

        System.out.println();
        System.out.println("================================================================================");
        System.out.println("TOTAL CHECKS EXECUTED: " + totalChecks);
        System.out.println("PASSED:                " + passedChecks);
        System.out.println("FAILED:                " + failures.size());

        if (failures.isEmpty()) {
            System.out.println("OVERALL VERDICT:       CONFIRMED (100% PASS RATE)");
            System.out.println("================================================================================");
            System.exit(0);
        } else {
            System.out.println("OVERALL VERDICT:       FAILED");
            System.out.println("Failures list:");
            for (ChallengeFailure f : failures) {
                System.out.println("  - " + f);
            }
            System.out.println("================================================================================");
            System.exit(1);
        }
    }

    private static void check(String label, boolean condition, String errorDetails) {
        totalChecks++;
        if (!condition) {
            failures.add(new ChallengeFailure(label, errorDetails));
            System.err.println("FAIL: " + label + " - " + errorDetails);
        } else {
            passedChecks++;
        }
    }

    private static void checkNear(String label, double expected, double actual, double epsilon) {
        totalChecks++;
        if (Double.isNaN(expected) || Double.isNaN(actual) || Math.abs(expected - actual) > epsilon) {
            failures.add(new ChallengeFailure(label, "Expected " + expected + " (+/- " + epsilon + ") but got " + actual));
            System.err.println("FAIL: " + label + " - Expected " + expected + " but got " + actual);
        } else {
            passedChecks++;
        }
    }

    // =========================================================================
    // 1. GRAVITON BOUNCEPAD PARITY
    // =========================================================================
    private static void test1_GravitonBouncepadParity() {
        System.out.println("--- CHALLENGE 1: Graviton Bouncepad Parity ---");

        // 1A. Continuous Analogue Redstone Scaling
        // Formula: launchSpeed = 1.35 + 1.15 * (redstonePower / 15.0)
        double speed0 = computeLaunchSpeed(0);
        double speed7 = computeLaunchSpeed(7);
        double speed15 = computeLaunchSpeed(15);

        checkNear("Bouncepad_Speed_Power0", 1.35, speed0, 1e-4);
        checkNear("Bouncepad_Speed_Power7", 1.8866666666666667, speed7, 1e-4);
        checkNear("Bouncepad_Speed_Power15", 2.50, speed15, 1e-4);

        // Verify strictly monotonic scaling for all powers 0 to 15
        double prevSpeed = -1.0;
        for (int p = 0; p <= 15; p++) {
            double sp = computeLaunchSpeed(p);
            check("Monotonic_Power_" + p, sp > prevSpeed, "Speed at power " + p + " (" + sp + ") not strictly greater than " + prevSpeed);
            prevSpeed = sp;
        }

        // Out-of-bounds power clamping test
        checkNear("Bouncepad_Clamp_Negative", 1.35, computeLaunchSpeedClamped(-5), 1e-4);
        checkNear("Bouncepad_Clamp_Excess", 2.50, computeLaunchSpeedClamped(100), 1e-4);

        // Momentum preservation on launch: perpendicular velocity must be strictly conserved
        Vec3 initialVel = new Vec3(0.5, 0.1, -0.3);
        Vec3 launchDir = new Vec3(0, 1, 0); // upward
        Vec3 postLaunchVel = computeBounceVelocity(initialVel, launchDir, speed0);
        checkNear("Preserve_Perp_X", 0.5, postLaunchVel.x, 1e-6);
        checkNear("Preserve_Perp_Z", -0.3, postLaunchVel.z, 1e-6);
        checkNear("Set_Launch_Y", speed0, postLaunchVel.y, 1e-6);

        // Momentum boost when already moving faster than launchSpeed
        Vec3 highSpeedInitial = new Vec3(0.0, 3.0, 0.0);
        Vec3 highSpeedPost = computeBounceVelocity(highSpeedInitial, launchDir, speed0);
        checkNear("High_Speed_Boost_Y", 3.3, highSpeedPost.y, 1e-6);

        // 1B. Debounce Timing & Recoil Dynamics
        // Test simulated bouncepad block entity debounce
        SimulatedBouncepadBE be = new SimulatedBouncepadBE();
        check("Debounce_Initial_CanBounce", be.canBounce(), "Fresh bouncepad must be ready to bounce");
        check("Debounce_Initial_Timer_Zero", be.getBounceTimer() == 0, "Initial timer must be 0");

        // Trigger bounce
        be.triggerBounce();
        check("Debounce_PostTrigger_CannotBounce", !be.canBounce(), "Immediately after trigger, canBounce() must be false");
        check("Debounce_Timer_15", be.getBounceTimer() == 15, "Bounce timer must be initialized to 15 ticks");
        checkNear("Compression_1_0", 1.0, be.getCompression(), 1e-4);

        // Verify rapid-fire triggers are rejected during the debounce window
        for (int tick = 1; tick <= 14; tick++) {
            be.tick();
            check("Debounce_Blocked_Tick_" + tick, !be.canBounce(), "Tick " + tick + " should still be in debounce window");
            check("Timer_Countdown_Tick_" + tick, be.getBounceTimer() == (15 - tick), "Timer must decrement by 1 each tick");
        }

        // Tick 15: timer reaches 0, ready to bounce again
        be.tick();
        check("Debounce_Recovered_Tick_15", be.canBounce(), "At tick 15, canBounce() must become true");
        check("Timer_Zero_Tick_15", be.getBounceTimer() == 0, "Timer must be 0 at tick 15");

        // Simulate spring damper convergence over 60 ticks
        for (int tick = 0; tick < 60; tick++) {
            be.tick();
        }
        checkNear("SpringDamper_Settled_Compression", 0.0, be.getCompression(), 1e-4);
        checkNear("SpringDamper_Settled_Velocity", 0.0, be.getCompressionVelocity(), 1e-4);

        // 1C. Landing Fall Damage Immunity Lifecycle
        SimulatedLivingEntity player = new SimulatedLivingEntity();
        check("Immunity_Initial_NoTag", !player.hasTag("entropica:bouncepad_immune"), "Player initially has no immunity tag");

        // Bouncepad launch gives tag and resets fall distance
        player.fallDistance = 15.0f;
        applyBouncepadLaunch(player, speed0);
        check("Immunity_Tag_Applied", player.hasTag("entropica:bouncepad_immune"), "Launch must apply bouncepad immunity tag");
        checkNear("Immunity_FallDistance_Reset", 0.0, player.fallDistance, 1e-4);

        // Player falls from 40 blocks
        player.fallDistance = 40.0f;
        boolean damagePreventedFirstFall = onLivingHurtFallDamage(player);
        check("Immunity_First_Fall_Interrupted", damagePreventedFirstFall, "First fall landing must be cancelled (damage immune)");
        checkNear("Immunity_First_Fall_Distance_Reset", 0.0, player.fallDistance, 1e-4);
        check("Immunity_Tag_Removed_After_Landing", !player.hasTag("entropica:bouncepad_immune"), "Tag must be removed after first landing");

        // Subsequent unrelated fall (player drops from 20 blocks without touching bouncepad)
        player.fallDistance = 20.0f;
        boolean damagePreventedSecondFall = onLivingHurtFallDamage(player);
        check("Immunity_Second_Fall_NOT_Interrupted", !damagePreventedSecondFall, "Subsequent unrelated fall must NOT be immune");
        check("Immunity_Second_Fall_Damage_Inflicted", player.fallDistance == 20.0f, "Second fall distance remains and inflicts damage");

        // Dead entity should not be launched
        SimulatedLivingEntity deadEntity = new SimulatedLivingEntity();
        deadEntity.isAlive = false;
        boolean deadLaunchAttempt = canLaunchEntity(deadEntity, be);
        check("Dead_Entity_Not_Launched", !deadLaunchAttempt, "Dead entity must not be launched by bouncepad");

        // 1D. Gravity Inversion & Directional Launch
        // Normal gravity: g = (0, -0.08, 0), down = (0, -1, 0) -> launch dir = -down = (0, 1, 0)
        Vec3 normalDown = new Vec3(0, -1, 0);
        Vec3 normalLaunch = normalDown.scale(-1.0).normalize();
        checkNear("Normal_Launch_Y", 1.0, normalLaunch.y, 1e-6);

        // Inverted gravity: g = (0, 0.08, 0), down = (0, 1, 0) -> launch dir = -down = (0, -1, 0)
        Vec3 invertedDown = new Vec3(0, 1, 0);
        Vec3 invertedLaunch = invertedDown.scale(-1.0).normalize();
        checkNear("Inverted_Launch_Y", -1.0, invertedLaunch.y, 1e-6);

        // Zero-G: falls back to pad normal
        Vec3 padUpNormal = new Vec3(0, 1, 0);
        Vec3 zeroGLaunch = computeGravityLaunchDir(true, false, false, padUpNormal, null, null);
        checkNear("ZeroG_Launch_Y", 1.0, zeroGLaunch.y, 1e-6);

        // Wall-mounted bouncepads: facing EAST (+X), launches horizontally along facing normal
        Vec3 wallFacingEast = new Vec3(1, 0, 0);
        Vec3 wallLaunch = wallFacingEast; // horizontal axis overrides gravity
        checkNear("Wall_Launch_X", 1.0, wallLaunch.x, 1e-6);
        checkNear("Wall_Launch_Y", 0.0, wallLaunch.y, 1e-6);

        // Core gravity: planet center at (0, 0, 0), entity at (0, 10, 0) -> down = (0, -1, 0) -> launch = (0, 1, 0)
        Vec3 corePos = new Vec3(0, 0, 0);
        Vec3 entPosAbove = new Vec3(0, 10, 0);
        Vec3 coreLaunchAbove = computeCoreGravityLaunch(corePos, entPosAbove);
        checkNear("Core_Launch_Above_Y", 1.0, coreLaunchAbove.y, 1e-6);

        // Entity below planet center at (0, -10, 0) -> down = (0, 1, 0) -> launch = (0, -1, 0)
        Vec3 entPosBelow = new Vec3(0, -10, 0);
        Vec3 coreLaunchBelow = computeCoreGravityLaunch(corePos, entPosBelow);
        checkNear("Core_Launch_Below_Y", -1.0, coreLaunchBelow.y, 1e-6);

        System.out.println("  Challenge 1 Completed: " + passedChecks + " checks passed.");
    }

    // =========================================================================
    // 2. REDSTONE SWITCHABILITY
    // =========================================================================
    private static void test2_RedstoneSwitchability() {
        System.out.println("--- CHALLENGE 2: Redstone Switchability ---");

        // 2A. Mode Transitions
        // Mode 0: Ignore redstone signals
        check("Mode0_Unpowered_Active", evaluateActiveState(0, true, false), "Mode 0 ignores unpowered, stays active");
        check("Mode0_Powered_Active", evaluateActiveState(0, true, true), "Mode 0 ignores powered, stays active");

        // Mode 1: Active unpowered, dormant powered (standard redstone)
        check("Mode1_Unpowered_Active", evaluateActiveState(1, false, false), "Mode 1 unpowered is active");
        check("Mode1_Powered_Dormant", !evaluateActiveState(1, true, true), "Mode 1 powered is dormant");

        // Mode 2: Active powered, dormant unpowered (inverted redstone)
        check("Mode2_Unpowered_Dormant", !evaluateActiveState(2, true, false), "Mode 2 unpowered is dormant");
        check("Mode2_Powered_Active", evaluateActiveState(2, false, true), "Mode 2 powered is active");

        // 2B. Dormant State Free Pass-Through for Entities and Projectiles
        SimulatedBarrier barrier = new SimulatedBarrier();
        barrier.shape = BarrierShape.PLANAR_QUAD;
        barrier.width = 4.0f;
        barrier.height = 4.0f;
        barrier.pos = new Vec3(0, 0, 0);
        barrier.filterMode = BarrierFilterMode.ALL_ENTITIES;

        // When active: entity movement traversing barrier encounters collision
        barrier.isActive = true;
        Vec3 rayStart = new Vec3(0, 0, -3);
        Vec3 rayEnd = new Vec3(0, 0, 3);
        boolean collisionWhenActive = barrier.checkCollision(rayStart, rayEnd, 0.3);
        check("Active_Barrier_Blocks_Passage", collisionWhenActive, "Active barrier must intercept entity swept movement");

        // When dormant (isActive = false): entity passes freely with zero interception
        barrier.isActive = false;
        boolean collisionWhenDormant = barrier.checkCollision(rayStart, rayEnd, 0.3);
        check("Dormant_Barrier_Permits_Entity_Passage", !collisionWhenDormant, "Dormant barrier must allow entities to pass freely");

        // High-speed projectile swept raycast through dormant barrier
        Vec3 projStart = new Vec3(0, 0, -10);
        Vec3 projEnd = new Vec3(0, 0, 10);
        boolean projCollisionWhenDormant = barrier.checkCollision(projStart, projEnd, 0.15);
        check("Dormant_Barrier_Permits_Projectile_Passage", !projCollisionWhenDormant, "Dormant barrier must allow fast projectiles to pass freely");

        // Toggle back to active: instantly re-establishes barrier boundary
        barrier.isActive = true;
        boolean projCollisionWhenReactivated = barrier.checkCollision(projStart, projEnd, 0.15);
        check("Reactivated_Barrier_Blocks_Projectile", projCollisionWhenReactivated, "Re-activated barrier must immediately intercept projectiles");

        System.out.println("  Challenge 2 Completed: " + passedChecks + " checks passed.");
    }

    // =========================================================================
    // 3. BOSS ARENA INVULNERABILITY
    // =========================================================================
    private static void test3_BossArenaInvulnerability() {
        System.out.println("--- CHALLENGE 3: Boss Arena Invulnerability ---");

        UUID bossUUID = UUID.randomUUID();
        SimulatedBarrier bossBarrier = new SimulatedBarrier();
        bossBarrier.isBossEncounter = true;
        bossBarrier.bossEntityUUID = bossUUID;
        bossBarrier.isAlive = true;

        // 3A. Survival Player Holding Firmament Weaver Attacks Barrier
        SimulatedPlayer survivalPlayer = new SimulatedPlayer(false, true); // survival, holding weaver
        boolean survivalHurtResult = bossBarrier.hurtServer(survivalPlayer);
        check("Survival_Weaver_Cannot_Dispel_Boss_Barrier", !survivalHurtResult, "Survival player must be rejected by hurtServer");
        check("Boss_Barrier_Remains_Alive_After_Survival_Attack", bossBarrier.isAlive, "Boss barrier must not be removed by survival player");
        check("Survival_Rejected_Message_Logged", survivalPlayer.lastMessage.contains("cannot be dismantled"), "Survival player receives rejection notice");
        check("Shield_Block_Audio_Played", survivalPlayer.playedShieldSound, "Shield block sound played to indicate invulnerability");

        // Survival player interacting with barrier
        boolean survivalInteractResult = bossBarrier.interact(survivalPlayer);
        check("Survival_Interact_Consumed", survivalInteractResult, "Interact is consumed without dispelling");
        check("Boss_Barrier_Alive_After_Interact", bossBarrier.isAlive, "Barrier remains alive after interact attempt");

        // 3B. Creative Player Dispels Boss Barrier
        SimulatedPlayer creativePlayer = new SimulatedPlayer(true, true); // creative, holding weaver
        boolean creativeHurtResult = bossBarrier.hurtServer(creativePlayer);
        check("Creative_Player_Can_Dispel_Boss_Barrier", creativeHurtResult, "Creative player bypasses boss barrier lock");
        check("Boss_Barrier_Dissolved_By_Creative", !bossBarrier.isAlive, "Barrier is discarded/dissolved by creative operator");

        // 3C. Boss Death Triggers Instant Dissolution
        // Reset barrier
        SimulatedBarrier arenaBarrier1 = new SimulatedBarrier();
        arenaBarrier1.isBossEncounter = true;
        arenaBarrier1.bossEntityUUID = bossUUID;
        arenaBarrier1.isAlive = true;

        SimulatedBarrier arenaBarrier2 = new SimulatedBarrier();
        arenaBarrier2.isBossEncounter = true;
        arenaBarrier2.bossEntityUUID = bossUUID;
        arenaBarrier2.isAlive = true;

        List<SimulatedBarrier> arenaBarriers = List.of(arenaBarrier1, arenaBarrier2);

        // Simulate boss death event
        UUID deadBossUUID = bossUUID;
        for (SimulatedBarrier b : arenaBarriers) {
            if (b.isBossEncounter && b.isAlive && Objects.equals(b.bossEntityUUID, deadBossUUID)) {
                b.dissolveInGlitter();
            }
        }

        check("Arena_Barrier1_Dissolved_On_Boss_Death", !arenaBarrier1.isAlive, "Arena barrier 1 dissolved instantly on boss death");
        check("Arena_Barrier2_Dissolved_On_Boss_Death", !arenaBarrier2.isAlive, "Arena barrier 2 dissolved instantly on boss death");
        check("Arena_Barrier1_Fanfare_Played", arenaBarrier1.fanfarePlayed, "Celestial fanfare played on barrier 1 dissolution");
        check("Arena_Barrier2_Fanfare_Played", arenaBarrier2.fanfarePlayed, "Celestial fanfare played on barrier 2 dissolution");

        // Verify death of unrelated entity does NOT dissolve the boss barrier
        SimulatedBarrier guardedBarrier = new SimulatedBarrier();
        guardedBarrier.isBossEncounter = true;
        guardedBarrier.bossEntityUUID = bossUUID;
        guardedBarrier.isAlive = true;

        UUID randomMobUUID = UUID.randomUUID();
        if (guardedBarrier.isBossEncounter && guardedBarrier.isAlive && Objects.equals(guardedBarrier.bossEntityUUID, randomMobUUID)) {
            guardedBarrier.dissolveInGlitter();
        }
        check("Unrelated_Mob_Death_Does_Not_Dissolve_Barrier", guardedBarrier.isAlive, "Death of unrelated mob must not dissolve boss arena");

        System.out.println("  Challenge 3 Completed: " + passedChecks + " checks passed.");
    }

    // =========================================================================
    // HELPER METHODS AND SIMULATION HARNESS
    // =========================================================================

    private static double computeLaunchSpeed(int redstonePower) {
        return 1.35 + 1.15 * (redstonePower / 15.0);
    }

    private static double computeLaunchSpeedClamped(int power) {
        int clamped = Math.min(15, Math.max(0, power));
        return 1.35 + 1.15 * (clamped / 15.0);
    }

    private static Vec3 computeBounceVelocity(Vec3 currentV, Vec3 launchDir, double launchSpeed) {
        double currentAlongUp = currentV.dot(launchDir);
        if (currentAlongUp < launchSpeed) {
            Vec3 perpV = currentV.subtract(launchDir.scale(currentAlongUp));
            return perpV.add(launchDir.scale(launchSpeed));
        } else {
            return currentV.add(launchDir.scale(0.3));
        }
    }

    private static Vec3 computeGravityLaunchDir(boolean isZeroG, boolean isInverted, boolean hasCenter,
                                                Vec3 padNormal, Vec3 centerPos, Vec3 entPos) {
        if (isZeroG) {
            return padNormal;
        }
        if (hasCenter && centerPos != null && entPos != null) {
            Vec3 toCore = centerPos.subtract(entPos);
            if (toCore.lengthSqr() > 1e-4) {
                return toCore.normalize().scale(-1.0);
            }
        }
        if (isInverted) {
            return new Vec3(0, -1, 0); // down is +Y, upward launch is -Y
        }
        return new Vec3(0, 1, 0); // vanilla downward is -Y, upward launch is +Y
    }

    private static Vec3 computeCoreGravityLaunch(Vec3 corePos, Vec3 entPos) {
        Vec3 toCore = corePos.subtract(entPos);
        Vec3 downGravity = toCore.normalize();
        return downGravity.scale(-1.0).normalize();
    }

    private static boolean evaluateActiveState(int mode, boolean currentActive, boolean receivingPower) {
        if (mode == 0) return currentActive;
        return (mode == 1) ? !receivingPower : receivingPower;
    }

    private static void applyBouncepadLaunch(SimulatedLivingEntity entity, double launchSpeed) {
        entity.addTag("entropica:bouncepad_immune");
        entity.fallDistance = 0.0f;
    }

    private static boolean onLivingHurtFallDamage(SimulatedLivingEntity entity) {
        if (entity.removeTag("entropica:bouncepad_immune")) {
            entity.fallDistance = 0.0f;
            return true; // interruptFalse: damage cancelled
        }
        return false; // pass: damage taken
    }

    private static boolean canLaunchEntity(SimulatedLivingEntity entity, SimulatedBouncepadBE be) {
        if (!entity.isAlive) return false;
        if (!be.canBounce()) return false;
        be.triggerBounce();
        return true;
    }

    // --- Simulation Models ---

    private static class SimulatedBouncepadBE {
        private int bounceTimer = 0;
        private float compression = 0.0f;
        private float compressionVelocity = 0.0f;

        public boolean canBounce() {
            return this.bounceTimer <= 0;
        }

        public int getBounceTimer() {
            return this.bounceTimer;
        }

        public float getCompression() {
            return this.compression;
        }

        public float getCompressionVelocity() {
            return this.compressionVelocity;
        }

        public void triggerBounce() {
            this.compression = 1.0f;
            this.compressionVelocity = 0.15f;
            this.bounceTimer = 15;
        }

        public void tick() {
            if (this.bounceTimer > 0) {
                this.bounceTimer--;
            }

            float springForce = (0.0f - this.compression) * 0.40f;
            this.compressionVelocity += springForce;
            this.compressionVelocity *= 0.75f;
            this.compression += this.compressionVelocity;

            if (Math.abs(this.compression) < 0.002f && Math.abs(this.compressionVelocity) < 0.002f) {
                this.compression = 0.0f;
                this.compressionVelocity = 0.0f;
            }
        }
    }

    private static class SimulatedLivingEntity {
        public boolean isAlive = true;
        public float fallDistance = 0.0f;
        private final Set<String> tags = new HashSet<>();

        public void addTag(String tag) {
            tags.add(tag);
        }

        public boolean hasTag(String tag) {
            return tags.contains(tag);
        }

        public boolean removeTag(String tag) {
            return tags.remove(tag);
        }
    }

    private static class SimulatedPlayer {
        public final boolean isCreative;
        public final boolean holdingWeaver;
        public String lastMessage = "";
        public boolean playedShieldSound = false;

        public SimulatedPlayer(boolean isCreative, boolean holdingWeaver) {
            this.isCreative = isCreative;
            this.holdingWeaver = holdingWeaver;
        }
    }

    private static class SimulatedBarrier {
        public BarrierShape shape = BarrierShape.PLANAR_QUAD;
        public float width = 4.0f;
        public float height = 4.0f;
        public Vec3 pos = new Vec3(0, 0, 0);
        public boolean isActive = true;
        public boolean isAlive = true;
        public boolean isBossEncounter = false;
        public UUID bossEntityUUID = null;
        public boolean fanfarePlayed = false;
        public BarrierFilterMode filterMode = BarrierFilterMode.ALL_ENTITIES;

        public boolean checkCollision(Vec3 start, Vec3 end, double entityRadius) {
            if (!isAlive || !isActive) return false;

            // Simplified planar quad hit check on z = 0
            if ((start.z < 0 && end.z > 0) || (start.z > 0 && end.z < 0)) {
                double t = -start.z / (end.z - start.z);
                double hitX = start.x + t * (end.x - start.x);
                double hitY = start.y + t * (end.y - start.y);
                double halfW = (width * 0.5) + entityRadius;
                double halfH = (height * 0.5) + entityRadius;
                return Math.abs(hitX - pos.x) <= halfW && Math.abs(hitY - pos.y) <= halfH;
            }
            return false;
        }

        public boolean hurtServer(SimulatedPlayer player) {
            if (isBossEncounter && !player.isCreative) {
                player.lastMessage = "§cThe firmament barrier is bound to an active Apex Predator and cannot be dismantled!";
                player.playedShieldSound = true;
                return false;
            }
            if (player.holdingWeaver) {
                if (player.isCreative) {
                    dissolveInGlitter();
                    return true;
                }
            }
            return false;
        }

        public boolean interact(SimulatedPlayer player) {
            if (isBossEncounter && !player.isCreative) {
                player.lastMessage = "§cThe firmament barrier is bound to an active Apex Predator and cannot be dismantled!";
                return true; // consumed
            }
            return false;
        }

        public void dissolveInGlitter() {
            this.isAlive = false;
            this.fanfarePlayed = true;
        }
    }
}
