package net.minecraft.util;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;

public interface RandomSource {

    /** @deprecated */
    @Deprecated
    double GAUSSIAN_SPREAD_FACTOR = 2.297D;

    static RandomSource create() {
        return create(RandomSupport.generateUniqueSeed());
    }

    /** @deprecated */
    @Deprecated
    static RandomSource createThreadSafe() {
        return new ThreadSafeLegacyRandomSource(RandomSupport.generateUniqueSeed());
    }

    static RandomSource create(long i) {
        return new LegacyRandomSource(i);
    }

    static RandomSource createNewThreadLocalInstance() {
        return new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong());
    }

    RandomSource fork();

    PositionalRandomFactory forkPositional();

    void setSeed(long i);

    int nextInt();

    int nextInt(int i);

    default int nextIntBetweenInclusive(int i, int j) {
        return this.nextInt(j - i + 1) + i;
    }

    long nextLong();

    boolean nextBoolean();

    float nextFloat();

    double nextDouble();

    double nextGaussian();

    default double triangle(double d0, double d1) {
        return d0 + d1 * (this.nextDouble() - this.nextDouble());
    }

    default float triangle(float f, float f1) {
        return f + f1 * (this.nextFloat() - this.nextFloat());
    }

    default void consumeCount(int i) {
        for (int j = 0; j < i; ++j) {
            this.nextInt();
        }

    }

    default int nextInt(int i, int j) {
        if (i >= j) {
            throw new IllegalArgumentException("bound - origin is non positive");
        } else {
            return i + this.nextInt(j - i);
        }
    }
}
