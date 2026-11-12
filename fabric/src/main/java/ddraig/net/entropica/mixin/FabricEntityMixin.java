package ddraig.net.entropica.mixin;

import ddraig.net.entropica.util.PersistentDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Mixin(Entity.class)
public class FabricEntityMixin implements PersistentDataHolder {
    @Unique
    private CompoundTag entropica$persistentData;

    @Unique
    @Override
    public CompoundTag entropica$getPersistentData() {
        if (this.entropica$persistentData == null) {
            this.entropica$persistentData = new CompoundTag();
        }
        return this.entropica$persistentData;
    }

    @Inject(method = "saveWithoutId", at = @At("HEAD"))
    private void savePersistentData(ValueOutput output, CallbackInfo info) {
        if (this.entropica$persistentData != null && !this.entropica$persistentData.isEmpty()) {
            output.store("EntropicaData", CompoundTag.CODEC, this.entropica$persistentData);
        }
    }

    @Inject(method = "load", at = @At("HEAD"))
    private void loadPersistentData(ValueInput input, CallbackInfo info) {
        input.read("EntropicaData", CompoundTag.CODEC).ifPresent(c -> this.entropica$persistentData = c);
    }
}
