package net.minecraft.world.item;

import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;

public class ItemLiquidUtil {

    public ItemLiquidUtil() {}

    public static EnumInteractionResult startUsingInstantly(World world, EntityHuman entityhuman, EnumHand enumhand) {
        entityhuman.startUsingItem(enumhand);
        return EnumInteractionResult.CONSUME;
    }

    public static ItemStack createFilledResult(ItemStack itemstack, EntityHuman entityhuman, ItemStack itemstack1, boolean flag) {
        boolean flag1 = entityhuman.hasInfiniteMaterials();

        if (flag && flag1) {
            if (!entityhuman.getInventory().contains(itemstack1)) {
                entityhuman.getInventory().add(itemstack1);
            }

            return itemstack;
        } else {
            itemstack.consume(1, entityhuman);
            if (itemstack.isEmpty()) {
                return itemstack1;
            } else {
                if (!entityhuman.getInventory().add(itemstack1)) {
                    entityhuman.drop(itemstack1, false);
                }

                return itemstack;
            }
        }
    }

    public static ItemStack createFilledResult(ItemStack itemstack, EntityHuman entityhuman, ItemStack itemstack1) {
        return createFilledResult(itemstack, entityhuman, itemstack1, true);
    }

    public static void onContainerDestroyed(EntityItem entityitem, Iterable<ItemStack> iterable) {
        World world = entityitem.level();

        if (!world.isClientSide) {
            iterable.forEach((itemstack) -> {
                world.addFreshEntity(new EntityItem(world, entityitem.getX(), entityitem.getY(), entityitem.getZ(), itemstack));
            });
        }
    }
}
