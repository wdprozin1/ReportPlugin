package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.Particles;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockTorchWall extends BlockTorch {

    public static final MapCodec<BlockTorchWall> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockTorchWall.PARTICLE_OPTIONS_FIELD.forGetter((blocktorchwall) -> {
            return blocktorchwall.flameParticle;
        }), propertiesCodec()).apply(instance, BlockTorchWall::new);
    });
    public static final BlockStateEnum<EnumDirection> FACING = BlockFacingHorizontal.FACING;
    protected static final float AABB_OFFSET = 2.5F;
    private static final Map<EnumDirection, VoxelShape> AABBS = Maps.newEnumMap(ImmutableMap.of(EnumDirection.NORTH, Block.box(5.5D, 3.0D, 11.0D, 10.5D, 13.0D, 16.0D), EnumDirection.SOUTH, Block.box(5.5D, 3.0D, 0.0D, 10.5D, 13.0D, 5.0D), EnumDirection.WEST, Block.box(11.0D, 3.0D, 5.5D, 16.0D, 13.0D, 10.5D), EnumDirection.EAST, Block.box(0.0D, 3.0D, 5.5D, 5.0D, 13.0D, 10.5D)));

    @Override
    public MapCodec<BlockTorchWall> codec() {
        return BlockTorchWall.CODEC;
    }

    protected BlockTorchWall(ParticleType particletype, BlockBase.Info blockbase_info) {
        super(particletype, blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockTorchWall.FACING, EnumDirection.NORTH));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return getShape(iblockdata);
    }

    public static VoxelShape getShape(IBlockData iblockdata) {
        return (VoxelShape) BlockTorchWall.AABBS.get(iblockdata.getValue(BlockTorchWall.FACING));
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return canSurvive(iworldreader, blockposition, (EnumDirection) iblockdata.getValue(BlockTorchWall.FACING));
    }

    public static boolean canSurvive(IWorldReader iworldreader, BlockPosition blockposition, EnumDirection enumdirection) {
        BlockPosition blockposition1 = blockposition.relative(enumdirection.getOpposite());
        IBlockData iblockdata = iworldreader.getBlockState(blockposition1);

        return iblockdata.isFaceSturdy(iworldreader, blockposition1, enumdirection);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = this.defaultBlockState();
        World world = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        EnumDirection[] aenumdirection = blockactioncontext.getNearestLookingDirections();
        EnumDirection[] aenumdirection1 = aenumdirection;
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection1[j];

            if (enumdirection.getAxis().isHorizontal()) {
                EnumDirection enumdirection1 = enumdirection.getOpposite();

                iblockdata = (IBlockData) iblockdata.setValue(BlockTorchWall.FACING, enumdirection1);
                if (iblockdata.canSurvive(world, blockposition)) {
                    return iblockdata;
                }
            }
        }

        return null;
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        return enumdirection.getOpposite() == iblockdata.getValue(BlockTorchWall.FACING) && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : iblockdata;
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockTorchWall.FACING);
        double d0 = (double) blockposition.getX() + 0.5D;
        double d1 = (double) blockposition.getY() + 0.7D;
        double d2 = (double) blockposition.getZ() + 0.5D;
        double d3 = 0.22D;
        double d4 = 0.27D;
        EnumDirection enumdirection1 = enumdirection.getOpposite();

        world.addParticle(Particles.SMOKE, d0 + 0.27D * (double) enumdirection1.getStepX(), d1 + 0.22D, d2 + 0.27D * (double) enumdirection1.getStepZ(), 0.0D, 0.0D, 0.0D);
        world.addParticle(this.flameParticle, d0 + 0.27D * (double) enumdirection1.getStepX(), d1 + 0.22D, d2 + 0.27D * (double) enumdirection1.getStepZ(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockTorchWall.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockTorchWall.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockTorchWall.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockTorchWall.FACING);
    }
}
