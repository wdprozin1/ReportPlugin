package net.minecraft.world.item.crafting.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.flag.FeatureFlagSet;

public interface RecipeDisplay {

    Codec<RecipeDisplay> CODEC = BuiltInRegistries.RECIPE_DISPLAY.byNameCodec().dispatch(RecipeDisplay::type, RecipeDisplay.a::codec);
    StreamCodec<RegistryFriendlyByteBuf, RecipeDisplay> STREAM_CODEC = ByteBufCodecs.registry(Registries.RECIPE_DISPLAY).dispatch(RecipeDisplay::type, RecipeDisplay.a::streamCodec);

    SlotDisplay result();

    SlotDisplay craftingStation();

    RecipeDisplay.a<? extends RecipeDisplay> type();

    default boolean isEnabled(FeatureFlagSet featureflagset) {
        return this.result().isEnabled(featureflagset) && this.craftingStation().isEnabled(featureflagset);
    }

    public static record a<T extends RecipeDisplay>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {

    }
}
