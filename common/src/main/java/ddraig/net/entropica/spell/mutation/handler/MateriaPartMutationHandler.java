package ddraig.net.entropica.spell.mutation.handler;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Per-Materia mutation contract separated strictly by Spell Part:
 * <ul>
 *     <li><b>Flow Part</b> hooks run ONLY when {@code spell.flowPart().mutationEnabled()} is {@code true}
 *         and {@code flowMateria == materiaType()}.</li>
 *     <li><b>Form Part</b> hooks run ONLY when {@code slot.formPart().mutationEnabled()} is {@code true}
 *         and {@code formMateria == materiaType()}.</li>
 *     <li><b>Core Effect Part</b> hooks run ONLY when {@code effectPart.mutationEnabled()} is {@code true}
 *         and {@code effectMateria == materiaType()} (independently for Primary and Dual-Verb Secondary effects).</li>
 * </ul>
 */
public interface MateriaPartMutationHandler {

    EssenceType materiaType();

    // --- Canonical Signature Titles (Spellcrafting System Guide Section 6) ---

    @Nullable
    default String resolveFlowTitle(SpellFlowModifier flow) {
        return null;
    }

    @Nullable
    default String resolveFormTitle(SpellDeliveryForm form) {
        return null;
    }

    @Nullable
    default String resolveEffectTitle(SpellCoreEffect effect) {
        return null;
    }

    // --- Part 1: Flow Mutations ---

    default void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
    }

    default void onProjectileTickFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            SpellFlowModifier flow
    ) {
    }

    /**
     * Invoked at the caster's origin when a multi-projectile / bifurcating Flow splits into multiple directions
     * (e.g. Magma's Pyroclastic Split Flow erupting in a volcanic pop at each split point).
     */
    default void onFlowMultiCastOrigin(
            ServerLevel level,
            LivingEntity caster,
            SpellFlowModifier flow,
            int branchCount,
            SpellAssembly spell
    ) {
    }

    /**
     * Invoked when a primary (non-child) spell projectile impacts a target/block or reaches maximum apex range
     * under a mutated Flow (e.g. Taiga Permafrost Fork Split, Oasis Succulent Bloom Split, Vitae Cellular Mitosis Split).
     */
    default void onProjectileImpactOrExpireFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 splitOrigin,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
    }

    /**
     * Optional Materia Advantage bonus granted by a mutated Flow modifier (e.g. Viscous Fluid Mass +0.6,
     * Dense Tectonic Mass +0.4, Permafrost Creep +0.4, Creeping Tomb-Dust +0.4).
     */
    default float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        return 0.0F;
    }

    // --- Part 2: Delivery Form Mutations (All 9 Forms) ---

    default void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
    }

    default void onProjectileTickFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            SubSpellSlot slot
    ) {
    }

    default void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
    }

    default void onProjectileHitBlockFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
    }

    default void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
    }

    default void onBeamFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double maxReach,
            List<LivingEntity> hitEntities,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
    }

    default void onTouchStrikeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            List<LivingEntity> hitEntities,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
    }

    default void onRadialBurstFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 center,
            double radius,
            Vec3 direction,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
    }

    default void onAuraFormCast(
            ServerLevel level,
            LivingEntity caster,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
    }

    /**
     * @return {@code true} if the mutated Glyph form planted a custom lingering proximity trap
     *         instead of immediate baseline radial detonation.
     */
    default boolean onGlyphFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 glyphCenter,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        return false;
    }

    default void onConeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double coneRange,
            List<LivingEntity> hitTargets,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
    }

    /**
     * @return {@code true} if the mutated Skyfall form spawned its own multi-tick localized tempest/vortex.
     */
    default boolean onSkyfallFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 impactCenter,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        return false;
    }

    default void onSummonFormSpawned(
            ServerLevel level,
            LivingEntity caster,
            Entity summonEntity,
            Vec3 spawnPos,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
    }

    // --- Part 3: Core Effect Mutations (Primary & Secondary Independent) ---

    default void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
    }

    /**
     * @return {@code true} if this Materia's mutated Transmute verb transformed {@code state} at {@code pos}.
     */
    default boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        return false;
    }

    /**
     * @return {@code true} if this Materia's mutated Transmute verb transformed a floating {@link net.minecraft.world.entity.item.ItemEntity}.
     */
    default boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            net.minecraft.world.entity.item.ItemEntity itemEntity,
            int potencyTier
    ) {
        return false;
    }

    /**
     * Called once every 20 ticks onhexed entities afflicted with this Materia's Persistent Hex.
     *
     * @return {@code true} if this handler applied custom elemental DoT/debuff ticking, or {@code false} for default.
     */
    default boolean onPersistentHexTick(
            ServerLevel level,
            LivingEntity target,
            int potencyTier
    ) {
        return false;
    }

    default void onEffectAppliedToEntity(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            Vec3 direction,
            SpellCoreEffect effect,
            int potencyTier,
            SpellAssembly spell
    ) {
    }
}
