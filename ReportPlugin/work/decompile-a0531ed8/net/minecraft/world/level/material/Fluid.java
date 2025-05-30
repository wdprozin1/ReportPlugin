package net.minecraft.world.level.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.IBlockDataHolder;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class Fluid extends IBlockDataHolder<FluidType, Fluid> {

    public static final Codec<Fluid> CODEC = codec(BuiltInRegistries.FLUID.byNameCodec(), FluidType::defaultFluidState).stable();
    public static final int AMOUNT_MAX = 9;
    public static final int AMOUNT_FULL = 8;

    public Fluid(FluidType fluidtype, Reference2ObjectArrayMap<IBlockState<?>, Comparable<?>> reference2objectarraymap, MapCodec<Fluid> mapcodec) {
        super(fluidtype, reference2objectarraymap, mapcodec);
    }

    public FluidType getType() {
        return (FluidType) this.owner;
    }

    public boolean isSource() {
        return this.getType().isSource(this);
    }

    public boolean isSourceOfType(FluidType fluidtype) {
        return this.owner == fluidtype && ((FluidType) this.owner).isSource(this);
    }

    public boolean isEmpty() {
        return this.getType().isEmpty();
    }

    public float getHeight(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return this.getType().getHeight(this, iblockaccess, blockposition);
    }

    public float getOwnHeight() {
        return this.getType().getOwnHeight(this);
    }

    public int getAmount() {
        return this.getType().getAmount(this);
    }

    public boolean shouldRenderBackwardUpFace(IBlockAccess iblockaccess, BlockPosition blockposition) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                BlockPosition blockposition1 = blockposition.offset(i, 0, j);
                Fluid fluid = iblockaccess.getFluidState(blockposition1);

                if (!fluid.getType().isSame(this.getType()) && !iblockaccess.getBlockState(blockposition1).isSolidRender()) {
                    return true;
                }
            }
        }

        return false;
    }

    public void tick(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata) {
        this.getType().tick(worldserver, blockposition, iblockdata, this);
    }

    public void animateTick(World world, BlockPosition blockposition, RandomSource randomsource) {
        this.getType().animateTick(world, blockposition, this, randomsource);
    }

    public boolean isRandomlyTicking() {
        return this.getType().isRandomlyTicking();
    }

    public void randomTick(WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        this.getType().randomTick(worldserver, blockposition, this, randomsource);
    }

    public Vec3D getFlow(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return this.getType().getFlow(iblockaccess, blockposition, this);
    }

    public IBlockData createLegacyBlock() {
        return this.getType().createLegacyBlock(this);
    }

    @Nullable
    public ParticleParam getDripParticle() {
        return this.getType().getDripParticle();
    }

    public boolean is(TagKey<FluidType> tagkey) {
        return this.getType().builtInRegistryHolder().is(tagkey);
    }

    public boolean is(HolderSet<FluidType> holderset) {
        return holderset.contains(this.getType().builtInRegistryHolder());
    }

    public boolean is(FluidType fluidtype) {
        return this.getType() == fluidtype;
    }

    public float getExplosionResistance() {
        return this.getType().getExplosionResistance();
    }

    public boolean canBeReplacedWith(IBlockAccess iblockaccess, BlockPosition blockposition, FluidType fluidtype, EnumDirection enumdirection) {
        return this.getType().canBeReplacedWith(this, iblockaccess, blockposition, fluidtype, enumdirection);
    }

    public VoxelShape getShape(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return this.getType().getShape(this, iblockaccess, blockposition);
    }

    public Holder<FluidType> holder() {
        return ((FluidType) this.owner).builtInRegistryHolder();
    }

    public Stream<TagKey<FluidType>> getTags() {
        return ((FluidType) this.owner).builtInRegistryHolder().tags();
    }
}
