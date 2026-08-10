package ddraig.net.entropica.registry;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.particle.FumeParticleOption;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Entropica.MODID, net.minecraft.resources.ResourceKey.createRegistryKey(net.minecraft.resources.ResourceLocation.withDefaultNamespace("particle_type")));

    public static final RegistrySupplier<ParticleType<FumeParticleOption>> FUME_PARTICLE = PARTICLES.register("fume", () -> new ParticleType<FumeParticleOption>(false) {
        @Override
        public MapCodec<FumeParticleOption> codec() {
            return FumeParticleOption.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, FumeParticleOption> streamCodec() {
            return FumeParticleOption.STREAM_CODEC;
        }
    });

    public static final RegistrySupplier<SimpleParticleType> SPECTRUM_SPARKLE = PARTICLES.register("spectrum_sparkle", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> GALE_SWIRL_PUFF = PARTICLES.register("gale_swirl_puff", () -> new SimpleParticleType(false) {});

    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_SPARKLE = PARTICLES.register("tintable_sparkle", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_SPORE = PARTICLES.register("tintable_spore", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_MIST = PARTICLES.register("tintable_mist", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_DRIP = PARTICLES.register("tintable_drip", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_EMBER = PARTICLES.register("tintable_ember", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_RUNE = PARTICLES.register("tintable_rune", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_BLOOD = PARTICLES.register("tintable_blood", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_VORTEX = PARTICLES.register("tintable_vortex", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_SHIELD = PARTICLES.register("tintable_shield", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_ARC = PARTICLES.register("tintable_arc", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_LEAF = PARTICLES.register("tintable_leaf", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_WISP = PARTICLES.register("tintable_wisp", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_CRYSTAL = PARTICLES.register("tintable_crystal", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_BUBBLE = PARTICLES.register("tintable_bubble", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_SINGULARITY = PARTICLES.register("tintable_singularity", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_SIGIL = PARTICLES.register("tintable_sigil", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_SUNBEAM = PARTICLES.register("tintable_sunbeam", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> TINTABLE_ACID = PARTICLES.register("tintable_acid", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_FEHU = PARTICLES.register("rune_fehu", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_URUZ = PARTICLES.register("rune_uruz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_THURISAZ = PARTICLES.register("rune_thurisaz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_ANSUZ = PARTICLES.register("rune_ansuz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_RAIDO = PARTICLES.register("rune_raido", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_KENAZ = PARTICLES.register("rune_kenaz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_GEBO = PARTICLES.register("rune_gebo", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_WUNJO = PARTICLES.register("rune_wunjo", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_HAGALAZ = PARTICLES.register("rune_hagalaz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_NAUTHIZ = PARTICLES.register("rune_nauthiz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_ISA = PARTICLES.register("rune_isa", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_JERA = PARTICLES.register("rune_jera", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_EIHWAZ = PARTICLES.register("rune_eihwaz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_PERTHRO = PARTICLES.register("rune_perthro", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_ALGIZ = PARTICLES.register("rune_algiz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_SOWILO = PARTICLES.register("rune_sowilo", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_TIWAZ = PARTICLES.register("rune_tiwaz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_BERKANO = PARTICLES.register("rune_berkano", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_EHWAZ = PARTICLES.register("rune_ehwaz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_MANNAZ = PARTICLES.register("rune_mannaz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_LAGUZ = PARTICLES.register("rune_laguz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_INGWAZ = PARTICLES.register("rune_ingwaz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_DAGAZ = PARTICLES.register("rune_dagaz", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
    public static final RegistrySupplier<ParticleType<TimedTintableParticleOption>> RUNE_OTHALA = PARTICLES.register("rune_othala", () -> new ParticleType<TimedTintableParticleOption>(false) {
        @Override
        public MapCodec<TimedTintableParticleOption> codec() {
            return TimedTintableParticleOption.codec(this);
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec() {
            return TimedTintableParticleOption.streamCodec(this);
        }
    });
}
