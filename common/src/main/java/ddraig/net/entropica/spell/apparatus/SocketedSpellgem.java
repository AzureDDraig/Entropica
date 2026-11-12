package ddraig.net.entropica.spell.apparatus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/**
 * Represents one Spellgem socket inside a {@link CastingApparatusState}, tracking
 * workstation vs. emergency field-swap penalties (Section 8.5.3):
 * <ul>
 *     <li>Workstation swap: 0-tick lockout, 0% first-cast backfire.</li>
 *     <li>In-field swap: 60-second (1,200 ticks) lockout + 45% backfire chance on the first cast after lockout ends!</li>
 * </ul>
 */
public record SocketedSpellgem(
        Optional<SpellAssembly> spell,
        long lockoutUntilGameTime,
        boolean pendingFieldBackfireRoll
) {
    public static final long FIELD_SWAP_LOCKOUT_TICKS = 1200L; // 60 seconds @ 20 TPS
    public static final float FIELD_SWAP_FIRST_CAST_BACKFIRE_CHANCE = 0.45F;

    public static final SocketedSpellgem EMPTY = new SocketedSpellgem(Optional.empty(), 0L, false);

    public static final Codec<SocketedSpellgem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SpellAssembly.CODEC.optionalFieldOf("spell").forGetter(SocketedSpellgem::spell),
            Codec.LONG.optionalFieldOf("lockout_until", 0L).forGetter(SocketedSpellgem::lockoutUntilGameTime),
            Codec.BOOL.optionalFieldOf("pending_field_backfire", false).forGetter(SocketedSpellgem::pendingFieldBackfireRoll)
    ).apply(instance, SocketedSpellgem::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SocketedSpellgem> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(SpellAssembly.STREAM_CODEC), SocketedSpellgem::spell,
            ByteBufCodecs.VAR_LONG, SocketedSpellgem::lockoutUntilGameTime,
            ByteBufCodecs.BOOL, SocketedSpellgem::pendingFieldBackfireRoll,
            SocketedSpellgem::new
    );

    public boolean isEmpty() {
        return this.spell.isEmpty();
    }

    public boolean isLockedOut(long currentGameTime) {
        return currentGameTime < this.lockoutUntilGameTime;
    }

    public long getRemainingLockoutTicks(long currentGameTime) {
        return Math.max(0L, this.lockoutUntilGameTime - currentGameTime);
    }

    public static SocketedSpellgem socketAtWorkstation(SpellAssembly assembly) {
        return new SocketedSpellgem(Optional.of(assembly), 0L, false);
    }

    public static SocketedSpellgem socketInField(SpellAssembly assembly, long currentGameTime) {
        return new SocketedSpellgem(Optional.of(assembly), currentGameTime + FIELD_SWAP_LOCKOUT_TICKS, true);
    }

    public SocketedSpellgem clearPendingBackfire() {
        if (!this.pendingFieldBackfireRoll) return this;
        return new SocketedSpellgem(this.spell, this.lockoutUntilGameTime, false);
    }
}
