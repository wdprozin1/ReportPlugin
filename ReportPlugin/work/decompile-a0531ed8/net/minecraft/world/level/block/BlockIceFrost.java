package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.redstone.Orientation;

public class BlockIceFrost extends BlockIce {

    public static final MapCodec<BlockIceFrost> CODEC = simpleCodec(BlockIceFrost::new);
    public static final int MAX_AGE = 3;
    public static final BlockStateInteger AGE = BlockProperties.AGE_3;
    private static final int NEIGHBORS_TO_AGE = 4;
    private static final int NEIGHBORS_TO_MELT = 2;

    @Override
    public MapCodec<BlockIceFrost> codec() {
        return BlockIceFrost.CODEC;
    }

    public BlockIceFrost(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockIceFrost.AGE, 0));
    }

    @Override
    public void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        world.scheduleTick(blockposition, (Block) this, MathHelper.nextInt(world.getRandom(), 60, 120));
    }

    @Override
    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if ((randomsource.nextInt(3) == 0 || this.fewerNeigboursThan(worldserver, blockposition, 4)) && worldserver.getMaxLocalRawBrightness(blockposition) > 11 - (Integer) iblockdata.getValue(BlockIceFrost.AGE) - iblockdata.getLightBlock() && this.slightlyMelt(iblockdata, worldserver, blockposition)) {
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
            EnumDirection[] aenumdirection = EnumDirection.values();
            int i = aenumdirection.length;

            for (int j = 0; j < i; ++j) {
                EnumDirection enumdirection = aenumdirection[j];

                blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection);
                IBlockData iblockdata1 = worldserver.getBlockState(blockposition_mutableblockposition);

                if (iblockdata1.is((Block) this) && !this.slightlyMelt(iblockdata1, worldserver, blockposition_mutableblockposition)) {
                    worldserver.scheduleTick(blockposition_mutableblockposition, (Block) this, MathHelper.nextInt(randomsource, 20, 40));
                }
            }

        } else {
            worldserver.scheduleTick(blockposition, (Block) this, MathHelper.nextInt(randomsource, 20, 40));
        }
    }

    private boolean slightlyMelt(IBlockData iblockdata, World world, BlockPosition blockposition) {
        int i = (Integer) iblockdata.getValue(BlockIceFrost.AGE);

        if (i < 3) {
            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockIceFrost.AGE, i + 1), 2);
            return false;
        } else {
            this.melt(iblockdata, world, blockposition);
            return true;
        }
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        if (block.defaultBlockState().is((Block) this) && this.fewerNeigboursThan(world, blockposition, 2)) {
            this.melt(iblockdata, world, blockposition);
        }

        super.neighborChanged(iblockdata, world, blockposition, block, orientation, flag);
    }

    private boolean fewerNeigboursThan(IBlockAccess iblockaccess, BlockPosition blockposition, int i) {
        int j = 0;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        EnumDirection[] aenumdirection = EnumDirection.values();
        int k = aenumdirection.length;

        for (int l = 0; l < k; ++l) {
            EnumDirection enumdirection = aenumdirection[l];

            blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection);
            if (iblockaccess.getBlockState(blockposition_mutableblockposition).is((Block) this)) {
                ++j;
                if (j >= i) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockIceFrost.AGE);
    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return ItemStack.EMPTY;
    }
}
