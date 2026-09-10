package ddraig.net.entropica.api.starlight;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.core.BlockPos;

public interface IWirelessFluxReceiver {

    /**
     * Receives a pulse of Materia flux either from an adjacent optic fiber cable or wirelessly from a Materia-Flux Distributor.
     *
     * @param starName Active celestial body / star name
     * @param essence  Attuned Materia essence type
     */
    void receiveFluxPulse(String starName, EssenceType essence);

    /**
     * @return True if the block is currently energized by active Materia flux.
     */
    boolean isFluxPowered();

    /**
     * @return Ticks remaining on the current active pulse decay buffer.
     */
    int getPulseTicksRemaining();

    /**
     * @return Block position of this receiver in the world.
     */
    BlockPos getReceiverPos();
}
