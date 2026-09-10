package ddraig.net.entropica.inventory.barrier;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.ApexPredatorTheme;
import ddraig.net.entropica.forcefield.BarrierFilterMode;
import ddraig.net.entropica.forcefield.BarrierShape;
import ddraig.net.entropica.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container menu for the Firmament Forcefield Barrier creator configuration GUI.
 * Facilitates permission validation, boundary checks, and parameter synchronization.
 */
public class BarrierConfigMenu extends AbstractContainerMenu {

    private final @Nullable ForcefieldBarrierEntity barrier;
    private final int barrierEntityId;
    private final List<String> whitelistUsernames = new ArrayList<>();

    /**
     * Client-side constructor invoked by MenuRegistry.ofExtended via network buffer.
     */
    public BarrierConfigMenu(int containerId, Inventory playerInv, FriendlyByteBuf data) {
        super(ModMenuTypes.BARRIER_CONFIG_MENU.get(), containerId);
        this.barrierEntityId = data.readVarInt();
        Entity entity = playerInv.player.level().getEntity(this.barrierEntityId);
        if (entity instanceof ForcefieldBarrierEntity b) {
            this.barrier = b;
        } else {
            this.barrier = null;
        }

        int count = data.readVarInt();
        for (int i = 0; i < count; i++) {
            this.whitelistUsernames.add(data.readUtf(64));
        }
    }

    /**
     * Server-side constructor with explicit barrier entity and whitelist usernames list.
     */
    public BarrierConfigMenu(int containerId, Inventory playerInv, @Nullable ForcefieldBarrierEntity barrier, List<String> whitelistUsernames) {
        super(ModMenuTypes.BARRIER_CONFIG_MENU.get(), containerId);
        this.barrier = barrier;
        this.barrierEntityId = barrier != null ? barrier.getId() : -1;
        if (whitelistUsernames != null) {
            this.whitelistUsernames.addAll(whitelistUsernames);
        }
    }

    /**
     * Server-side constructor defaulting whitelist usernames from the barrier instance.
     */
    public BarrierConfigMenu(int containerId, Inventory playerInv, @Nullable ForcefieldBarrierEntity barrier) {
        this(containerId, playerInv, barrier, barrier != null ? barrier.getWhitelistUsernames() : List.of());
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.barrier == null || !this.barrier.isAlive() || this.barrier.isRemoved()) {
            return false;
        }
        if (this.barrier.level() != player.level()) {
            return false;
        }
        if (this.barrier.distanceToSqr(player) > 64.0 * 64.0) {
            return false;
        }
        boolean isOwner = this.barrier.getOwnerUUID().map(u -> u.equals(player.getUUID())).orElse(false);
        if (!isOwner && !player.isCreative()) {
            return false;
        }
        if (this.barrier.isBossEncounter() && !player.isCreative()) {
            return false;
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public @Nullable ForcefieldBarrierEntity getBarrierEntity() {
        return this.barrier;
    }

    public @Nullable ForcefieldBarrierEntity getBarrier() {
        return this.barrier;
    }

    public int getBarrierEntityId() {
        return this.barrierEntityId;
    }

    public BarrierShape getShape() {
        return this.barrier != null ? this.barrier.getShape() : BarrierShape.PLANAR_QUAD;
    }

    public float getWidth() {
        return this.barrier != null ? this.barrier.getWidth() : 4.0F;
    }

    public float getHeight() {
        return this.barrier != null ? this.barrier.getHeight() : 4.0F;
    }

    public float getRadius() {
        return this.barrier != null ? this.barrier.getRadius() : 5.0F;
    }

    public BarrierFilterMode getFilterMode() {
        return this.barrier != null ? this.barrier.getFilterMode() : BarrierFilterMode.ALL_ENTITIES;
    }

    public ApexPredatorTheme getTheme() {
        return this.barrier != null ? this.barrier.getPredatorTheme() : ApexPredatorTheme.STANDARD;
    }

    public float getElasticity() {
        return this.barrier != null ? this.barrier.getBounceElasticity() : 1.0F;
    }

    public boolean isOneWay() {
        return this.barrier != null && this.barrier.isOneWay();
    }

    public int getRedstoneMode() {
        return this.barrier != null ? this.barrier.getRedstoneMode() : 0;
    }

    public int getColorTintRaw() {
        return this.barrier != null ? this.barrier.getColorTintRaw() : 0;
    }

    public List<String> getWhitelistUsernames() {
        return Collections.unmodifiableList(this.whitelistUsernames);
    }
}
