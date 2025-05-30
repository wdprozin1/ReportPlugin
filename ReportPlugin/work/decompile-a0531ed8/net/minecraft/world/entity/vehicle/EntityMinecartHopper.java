package net.minecraft.world.entity.vehicle;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerHopper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.IHopper;
import net.minecraft.world.level.block.entity.TileEntityHopper;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyTrackPosition;

public class EntityMinecartHopper extends EntityMinecartContainer implements IHopper {

    private boolean enabled = true;
    private boolean consumedItemThisFrame = false;

    public EntityMinecartHopper(EntityTypes<? extends EntityMinecartHopper> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    public IBlockData getDefaultDisplayBlockState() {
        return Blocks.HOPPER.defaultBlockState();
    }

    @Override
    public int getDefaultDisplayOffset() {
        return 1;
    }

    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public void activateMinecart(int i, int j, int k, boolean flag) {
        boolean flag1 = !flag;

        if (flag1 != this.isEnabled()) {
            this.setEnabled(flag1);
        }

    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean flag) {
        this.enabled = flag;
    }

    @Override
    public double getLevelX() {
        return this.getX();
    }

    @Override
    public double getLevelY() {
        return this.getY() + 0.5D;
    }

    @Override
    public double getLevelZ() {
        return this.getZ();
    }

    @Override
    public boolean isGridAligned() {
        return false;
    }

    @Override
    public void tick() {
        this.consumedItemThisFrame = false;
        super.tick();
        this.tryConsumeItems();
    }

    @Override
    protected double makeStepAlongTrack(BlockPosition blockposition, BlockPropertyTrackPosition blockpropertytrackposition, double d0) {
        double d1 = super.makeStepAlongTrack(blockposition, blockpropertytrackposition, d0);

        this.tryConsumeItems();
        return d1;
    }

    private void tryConsumeItems() {
        if (!this.level().isClientSide && this.isAlive() && this.isEnabled() && !this.consumedItemThisFrame && this.suckInItems()) {
            this.consumedItemThisFrame = true;
            this.setChanged();
        }

    }

    public boolean suckInItems() {
        if (TileEntityHopper.suckInItems(this.level(), this)) {
            return true;
        } else {
            List<EntityItem> list = this.level().getEntitiesOfClass(EntityItem.class, this.getBoundingBox().inflate(0.25D, 0.0D, 0.25D), IEntitySelector.ENTITY_STILL_ALIVE);
            Iterator iterator = list.iterator();

            EntityItem entityitem;

            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                entityitem = (EntityItem) iterator.next();
            } while (!TileEntityHopper.addItem(this, entityitem));

            return true;
        }
    }

    @Override
    protected Item getDropItem() {
        return Items.HOPPER_MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.HOPPER_MINECART);
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putBoolean("Enabled", this.enabled);
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.enabled = nbttagcompound.contains("Enabled") ? nbttagcompound.getBoolean("Enabled") : true;
    }

    @Override
    public Container createMenu(int i, PlayerInventory playerinventory) {
        return new ContainerHopper(i, playerinventory, this);
    }
}
