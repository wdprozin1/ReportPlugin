package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class SolidBucketItem extends ItemBlock implements DispensibleContainerItem {

    private final SoundEffect placeSound;

    public SolidBucketItem(Block block, SoundEffect soundeffect, Item.Info item_info) {
        super(block, item_info);
        this.placeSound = soundeffect;
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        EnumInteractionResult enuminteractionresult = super.useOn(itemactioncontext);
        EntityHuman entityhuman = itemactioncontext.getPlayer();

        if (enuminteractionresult.consumesAction() && entityhuman != null) {
            entityhuman.setItemInHand(itemactioncontext.getHand(), ItemBucket.getEmptySuccessItem(itemactioncontext.getItemInHand(), entityhuman));
        }

        return enuminteractionresult;
    }

    @Override
    protected SoundEffect getPlaceSound(IBlockData iblockdata) {
        return this.placeSound;
    }

    @Override
    public boolean emptyContents(@Nullable EntityHuman entityhuman, World world, BlockPosition blockposition, @Nullable MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isInWorldBounds(blockposition) && world.isEmptyBlock(blockposition)) {
            if (!world.isClientSide) {
                world.setBlock(blockposition, this.getBlock().defaultBlockState(), 3);
            }

            world.gameEvent((Entity) entityhuman, (Holder) GameEvent.FLUID_PLACE, blockposition);
            world.playSound(entityhuman, blockposition, this.placeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }
}
