package net.minecraft.world.level.chunk;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;

public abstract class IChunkProvider implements ILightAccess, AutoCloseable {

    public IChunkProvider() {}

    @Nullable
    public Chunk getChunk(int i, int j, boolean flag) {
        return (Chunk) this.getChunk(i, j, ChunkStatus.FULL, flag);
    }

    @Nullable
    public Chunk getChunkNow(int i, int j) {
        return this.getChunk(i, j, false);
    }

    @Nullable
    @Override
    public LightChunk getChunkForLighting(int i, int j) {
        return this.getChunk(i, j, ChunkStatus.EMPTY, false);
    }

    public boolean hasChunk(int i, int j) {
        return this.getChunk(i, j, ChunkStatus.FULL, false) != null;
    }

    @Nullable
    public abstract IChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag);

    public abstract void tick(BooleanSupplier booleansupplier, boolean flag);

    public void onSectionEmptinessChanged(int i, int j, int k, boolean flag) {}

    public abstract String gatherStats();

    public abstract int getLoadedChunksCount();

    public void close() throws IOException {}

    public abstract LevelLightEngine getLightEngine();

    public void setSpawnSettings(boolean flag) {}

    public void updateChunkForced(ChunkCoordIntPair chunkcoordintpair, boolean flag) {}
}
