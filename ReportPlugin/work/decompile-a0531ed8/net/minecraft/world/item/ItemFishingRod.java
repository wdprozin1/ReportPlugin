package net.minecraft.world.item;

import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityFishingHook;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemFishingRod extends Item {

    public ItemFishingRod(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (entityhuman.fishing != null) {
            if (!world.isClientSide) {
                int i = entityhuman.fishing.retrieve(itemstack);

                itemstack.hurtAndBreak(i, entityhuman, EntityLiving.getSlotForHand(enumhand));
            }

            world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            entityhuman.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;
                int j = (int) (EnchantmentManager.getFishingTimeReduction(worldserver, itemstack, entityhuman) * 20.0F);
                int k = EnchantmentManager.getFishingLuckBonus(worldserver, itemstack, entityhuman);

                IProjectile.spawnProjectile(new EntityFishingHook(entityhuman, world, k, j), worldserver, itemstack);
            }

            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            entityhuman.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return EnumInteractionResult.SUCCESS;
    }
}
