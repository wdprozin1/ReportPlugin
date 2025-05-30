package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
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
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockCoralFanWallAbstract extends BlockCoralFanAbstract {

    public static final MapCodec<BlockCoralFanWallAbstract> CODEC = simpleCodec(BlockCoralFanWallAbstract::new);
    public static final BlockStateEnum<EnumDirection> FACING = BlockFacingHorizontal.FACING;
    private static final Map<EnumDirection, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(EnumDirection.NORTH, Block.box(0.0D, 4.0D, 5.0D, 16.0D, 12.0D, 16.0D), EnumDirection.SOUTH, Block.box(0.0D, 4.0D, 0.0D, 16.0D, 12.0D, 11.0D), EnumDirection.WEST, Block.box(5.0D, 4.0D, 0.0D, 16.0D, 12.0D, 16.0D), EnumDirection.EAST, Block.box(0.0D, 4.0D, 0.0D, 11.0D, 12.0D, 16.0D)));

    @Override
    public MapCodec<? extends BlockCoralFanWallAbstract> codec() {
        return BlockCoralFanWallAbstract.CODEC;
    }

    protected BlockCoralFanWallAbstract(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockCoralFanWallAbstract.FACING, EnumDirection.NORTH)).setValue(BlockCoralFanWallAbstract.WATERLOGGED, true));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) BlockCoralFanWallAbstract.SHAPES.get(iblockdata.getValue(BlockCoralFanWallAbstract.FACING));
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockCoralFanWallAbstract.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockCoralFanWallAbstract.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockCoralFanWallAbstract.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockCoralFanWallAbstract.FACING, BlockCoralFanWallAbstract.WATERLOGGED);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockCoralFanWallAbstract.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return enumdirection.getOpposite() == iblockdata.getValue(BlockCoralFanWallAbstract.FACING) && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : iblockdata;
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockCoralFanWallAbstract.FACING);
        BlockPosition blockposition1 = blockposition.relative(enumdirection.getOpposite());
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition1);

        return iblockdata1.isFaceSturdy(iworldreader, blockposition1, enumdirection);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = super.getStateForPlacement(blockactioncontext);
        World world = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        EnumDirection[] aenumdirection = blockactioncontext.getNearestLookingDirections();
        EnumDirection[] aenumdirection1 = aenumdirection;
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection1[j];

            if (enumdirection.getAxis().isHorizontal()) {
                iblockdata = (IBlockData) iblockdata.setValue(BlockCoralFanWallAbstract.FACING, enumdirection.getOpposite());
                if (iblockdata.canSurvive(world, blockposition)) {
                    return iblockdata;
                }
            }
        }

        return null;
    }
}
