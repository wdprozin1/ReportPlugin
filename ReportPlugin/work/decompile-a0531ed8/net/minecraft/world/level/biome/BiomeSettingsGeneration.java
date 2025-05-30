package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.WorldGenStage;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverWrapper;
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured;
import net.minecraft.world.level.levelgen.feature.WorldGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;

public class BiomeSettingsGeneration {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BiomeSettingsGeneration EMPTY = new BiomeSettingsGeneration(HolderSet.direct(), List.of());
    public static final MapCodec<BiomeSettingsGeneration> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        Codec codec = WorldGenCarverWrapper.LIST_CODEC;
        Logger logger = BiomeSettingsGeneration.LOGGER;

        Objects.requireNonNull(logger);
        RecordCodecBuilder recordcodecbuilder = codec.promotePartial(SystemUtils.prefix("Carver: ", logger::error)).fieldOf("carvers").forGetter((biomesettingsgeneration) -> {
            return biomesettingsgeneration.carvers;
        });
        Codec codec1 = PlacedFeature.LIST_OF_LISTS_CODEC;
        Logger logger1 = BiomeSettingsGeneration.LOGGER;

        Objects.requireNonNull(logger1);
        return instance.group(recordcodecbuilder, codec1.promotePartial(SystemUtils.prefix("Features: ", logger1::error)).fieldOf("features").forGetter((biomesettingsgeneration) -> {
            return biomesettingsgeneration.features;
        })).apply(instance, BiomeSettingsGeneration::new);
    });
    private final HolderSet<WorldGenCarverWrapper<?>> carvers;
    private final List<HolderSet<PlacedFeature>> features;
    private final Supplier<List<WorldGenFeatureConfigured<?, ?>>> flowerFeatures;
    private final Supplier<Set<PlacedFeature>> featureSet;

    BiomeSettingsGeneration(HolderSet<WorldGenCarverWrapper<?>> holderset, List<HolderSet<PlacedFeature>> list) {
        this.carvers = holderset;
        this.features = list;
        this.flowerFeatures = Suppliers.memoize(() -> {
            return (List) list.stream().flatMap(HolderSet::stream).map(Holder::value).flatMap(PlacedFeature::getFeatures).filter((worldgenfeatureconfigured) -> {
                return worldgenfeatureconfigured.feature() == WorldGenerator.FLOWER;
            }).collect(ImmutableList.toImmutableList());
        });
        this.featureSet = Suppliers.memoize(() -> {
            return (Set) list.stream().flatMap(HolderSet::stream).map(Holder::value).collect(Collectors.toSet());
        });
    }

    public Iterable<Holder<WorldGenCarverWrapper<?>>> getCarvers() {
        return this.carvers;
    }

    public List<WorldGenFeatureConfigured<?, ?>> getFlowerFeatures() {
        return (List) this.flowerFeatures.get();
    }

    public List<HolderSet<PlacedFeature>> features() {
        return this.features;
    }

    public boolean hasFeature(PlacedFeature placedfeature) {
        return ((Set) this.featureSet.get()).contains(placedfeature);
    }

    public static class a extends BiomeSettingsGeneration.b {

        private final HolderGetter<PlacedFeature> placedFeatures;
        private final HolderGetter<WorldGenCarverWrapper<?>> worldCarvers;

        public a(HolderGetter<PlacedFeature> holdergetter, HolderGetter<WorldGenCarverWrapper<?>> holdergetter1) {
            this.placedFeatures = holdergetter;
            this.worldCarvers = holdergetter1;
        }

        public BiomeSettingsGeneration.a addFeature(WorldGenStage.Decoration worldgenstage_decoration, ResourceKey<PlacedFeature> resourcekey) {
            this.addFeature(worldgenstage_decoration.ordinal(), this.placedFeatures.getOrThrow(resourcekey));
            return this;
        }

        public BiomeSettingsGeneration.a addCarver(ResourceKey<WorldGenCarverWrapper<?>> resourcekey) {
            this.addCarver((Holder) this.worldCarvers.getOrThrow(resourcekey));
            return this;
        }
    }

    public static class b {

        private final List<Holder<WorldGenCarverWrapper<?>>> carvers = new ArrayList();
        private final List<List<Holder<PlacedFeature>>> features = new ArrayList();

        public b() {}

        public BiomeSettingsGeneration.b addFeature(WorldGenStage.Decoration worldgenstage_decoration, Holder<PlacedFeature> holder) {
            return this.addFeature(worldgenstage_decoration.ordinal(), holder);
        }

        public BiomeSettingsGeneration.b addFeature(int i, Holder<PlacedFeature> holder) {
            this.addFeatureStepsUpTo(i);
            ((List) this.features.get(i)).add(holder);
            return this;
        }

        public BiomeSettingsGeneration.b addCarver(Holder<WorldGenCarverWrapper<?>> holder) {
            this.carvers.add(holder);
            return this;
        }

        private void addFeatureStepsUpTo(int i) {
            while (this.features.size() <= i) {
                this.features.add(Lists.newArrayList());
            }

        }

        public BiomeSettingsGeneration build() {
            return new BiomeSettingsGeneration(HolderSet.direct(this.carvers), (List) this.features.stream().map(HolderSet::direct).collect(ImmutableList.toImmutableList()));
        }
    }
}
