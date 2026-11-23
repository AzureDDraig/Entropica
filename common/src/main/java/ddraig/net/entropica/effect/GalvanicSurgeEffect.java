package ddraig.net.entropica.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Galvanic Surge — beneficial Static Materia status effect brewed from Static Frost Powder.
 * Zaps melee attackers back with electric shock and frostbite ticks when struck.
 */
public class GalvanicSurgeEffect extends MobEffect {
    public GalvanicSurgeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x22D3EE);
    }
}
