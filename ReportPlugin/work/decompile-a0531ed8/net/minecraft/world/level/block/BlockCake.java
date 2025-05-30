package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockCake extends Block {

    public static final MapCodec<BlockCake> CODEC = simpleCodec(BlockCake::new);
    public static final int MAX_BITES = 6;
    public static final BlockStateInteger BITES = BlockProperties.BITES;
    public static final int FULL_CAKE_SIGNAL = getOutputSignal(0);
    protected static final float AABB_OFFSET = 1.0F;
    protected static final float AABB_SIZE_PER_BITE = 2.0F;
    protected static final VoxelShape[] SHAPE_BY_BITE = new VoxelShape[]{Block.box(1.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(3.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(5.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(7.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(9.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(11.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D), Block.box(13.0D, 0.0D, 1.0D, 15.0D, 8.0D, 15.0D)};

    @Override
    public MapCodec<BlockCake> codec() {
        return BlockCake.CODEC;
    }

    protected BlockCake(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockCake.BITES, 0));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockCake.SHAPE_BY_BITE[(Integer) iblockdata.getValue(BlockCake.BITES)];
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        Item item = itemstack.getItem();

        if (itemstack.is(TagsItem.CANDLES) && (Integer) iblockdata.getValue(BlockCake.BITES) == 0) {
            Block block = Block.byItem(item);

            if (block instanceof CandleBlock) {
                CandleBlock candleblock = (CandleBlock) block;

                itemstack.consume(1, entityhuman);
                world.playSound((EntityHuman) null, blockposition, SoundEffects.CAKE_ADD_CANDLE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.setBlockAndUpdate(blockposition, CandleCakeBlock.byCandle(candleblock));
                world.gameEvent((Entity) entityhuman, (Holder) GameEvent.BLOCK_CHANGE, blockposition);
                entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
                return EnumInteractionResult.SUCCESS;
            }
        }

        return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            if (eat(world, blockposition, iblockdata, entityhuman).consumesAction()) {
                return EnumInteractionResult.SUCCESS;
            }

            if (entityhuman.getItemInHand(EnumHand.MAIN_HAND).isEmpty()) {
                return EnumInteractionResult.CONSUME;
            }
        }

        return eat(world, blockposition, iblockdata, entityhuman);
    }

    protected static EnumInteractionResult eat(GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata, EntityHuman entityhuman) {
        if (!entityhuman.canEat(false)) {
            return EnumInteractionResult.PASS;
        } else {
            entityhuman.awardStat(StatisticList.EAT_CAKE_SLICE);
            entityhuman.getFoodData().eat(2, 0.1F);
            int i = (Integer) iblockdata.getValue(BlockCake.BITES);

            generatoraccess.gameEvent((Entity) entityhuman, (Holder) GameEvent.EAT, blockposition);
            if (i < 6) {
                generatoraccess.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockCake.BITES, i + 1), 3);
            } else {
                generatoraccess.removeBlock(blockposition, false);
                generatoraccess.gameEvent((Entity) entityhuman, (Holder) GameEvent.BLOCK_DESTROY, blockposition);
            }

            return EnumInteractionResult.SUCCESS;
        }
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        return enumdirection == EnumDirection.DOWN && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return iworldreader.getBlockState(blockposition.below()).isSolid();
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockCake.BITES);
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return getOutputSignal((Integer) iblockdata.getValue(BlockCake.BITES));
    }

    public static int getOutputSignal(int i) {
        return (7 - i) * 2;
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
