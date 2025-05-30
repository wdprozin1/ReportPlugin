package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryFileCodec;

public record SoundEffect(MinecraftKey location, Optional<Float> fixedRange) {

    public static final Codec<SoundEffect> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("sound_id").forGetter(SoundEffect::location), Codec.FLOAT.lenientOptionalFieldOf("range").forGetter(SoundEffect::fixedRange)).apply(instance, SoundEffect::create);
    });
    public static final Codec<Holder<SoundEffect>> CODEC = RegistryFileCodec.create(Registries.SOUND_EVENT, SoundEffect.DIRECT_CODEC);
    public static final StreamCodec<ByteBuf, SoundEffect> DIRECT_STREAM_CODEC = StreamCodec.composite(MinecraftKey.STREAM_CODEC, SoundEffect::location, ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), SoundEffect::fixedRange, SoundEffect::create);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SoundEffect>> STREAM_CODEC = ByteBufCodecs.holder(Registries.SOUND_EVENT, SoundEffect.DIRECT_STREAM_CODEC);

    private static SoundEffect create(MinecraftKey minecraftkey, Optional<Float> optional) {
        return (SoundEffect) optional.map((ofloat) -> {
            return createFixedRangeEvent(minecraftkey, ofloat);
        }).orElseGet(() -> {
            return createVariableRangeEvent(minecraftkey);
        });
    }

    public static SoundEffect createVariableRangeEvent(MinecraftKey minecraftkey) {
        return new SoundEffect(minecraftkey, Optional.empty());
    }

    public static SoundEffect createFixedRangeEvent(MinecraftKey minecraftkey, float f) {
        return new SoundEffect(minecraftkey, Optional.of(f));
    }

    public float getRange(float f) {
        return (Float) this.fixedRange.orElse(f > 1.0F ? 16.0F * f : 16.0F);
    }
}
