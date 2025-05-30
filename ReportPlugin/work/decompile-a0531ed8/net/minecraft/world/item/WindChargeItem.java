package net.minecraft.world.item;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.phys.Vec3D;

public class WindChargeItem extends Item implements ProjectileItem {

    public static float PROJECTILE_SHOOT_POWER = 1.5F;

    public WindChargeItem(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (world instanceof WorldServer worldserver) {
            IProjectile.spawnProjectileFromRotation((worldserver1, entityliving, itemstack1) -> {
                return new WindCharge(entityhuman, world, entityhuman.position().x(), entityhuman.getEyePosition().y(), entityhuman.position().z());
            }, worldserver, itemstack, entityhuman, 0.0F, WindChargeItem.PROJECTILE_SHOOT_POWER, 1.0F);
        }

        world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.WIND_CHARGE_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
        itemstack.consume(1, entityhuman);
        return EnumInteractionResult.SUCCESS;
    }

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        RandomSource randomsource = world.getRandom();
        double d0 = randomsource.triangle((double) enumdirection.getStepX(), 0.11485000000000001D);
        double d1 = randomsource.triangle((double) enumdirection.getStepY(), 0.11485000000000001D);
        double d2 = randomsource.triangle((double) enumdirection.getStepZ(), 0.11485000000000001D);
        Vec3D vec3d = new Vec3D(d0, d1, d2);
        WindCharge windcharge = new WindCharge(world, iposition.x(), iposition.y(), iposition.z(), vec3d);

        windcharge.setDeltaMovement(vec3d);
        return windcharge;
    }

    @Override
    public void shoot(IProjectile iprojectile, double d0, double d1, double d2, float f, float f1) {}

    @Override
    public ProjectileItem.a createDispenseConfig() {
        return ProjectileItem.a.builder().positionFunction((sourceblock, enumdirection) -> {
            return BlockDispenser.getDispensePosition(sourceblock, 1.0D, Vec3D.ZERO);
        }).uncertainty(6.6666665F).power(1.0F).overrideDispenseEvent(1051).build();
    }
}
