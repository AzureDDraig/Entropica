package ddraig.net.entropica.registry;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Entropica.MODID);

    // Stores the name of the EssenceType that caused the toxicity. Defaults to "UNKNOWN"
    public static final Supplier<AttachmentType<String>> TOXICITY_SOURCE = ATTACHMENT_TYPES.register(
            "toxicity_source",
            () -> AttachmentType.builder(() -> "UNKNOWN")
                    // FIXED: Converted the primitive Codec into a MapCodec to satisfy the 1.21.10 Builder!
                    .serialize(Codec.STRING.optionalFieldOf("source", "UNKNOWN"))
                    .build()
    );
}