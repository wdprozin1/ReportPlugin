package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTameableAnimal;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;

public class MaceItem extends Item {

    private static final int DEFAULT_ATTACK_DAMAGE = 5;
    private static final float DEFAULT_ATTACK_SPEED = -3.4F;
    public static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5F;
    private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0F;
    public static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 3.5F;
    private static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.7F;

    public MaceItem(Item.Info item_info) {
        super(item_info);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder().add(GenericAttributes.ATTACK_DAMAGE, new AttributeModifier(MaceItem.BASE_ATTACK_DAMAGE_ID, 5.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(GenericAttributes.ATTACK_SPEED, new AttributeModifier(MaceItem.BASE_ATTACK_SPEED_ID, -3.4000000953674316D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0F, 2);
    }

    @Override
    public boolean canAttackBlock(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman) {
        return !entityhuman.isCreative();
    }

    @Override
    public boolean hurtEnemy(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1) {
        if (canSmashAttack(entityliving1)) {
            WorldServer worldserver = (WorldServer) entityliving1.level();

            entityliving1.setDeltaMovement(entityliving1.getDeltaMovement().with(EnumDirection.EnumAxis.Y, 0.009999999776482582D));
            EntityPlayer entityplayer;

            if (entityliving1 instanceof EntityPlayer) {
                entityplayer = (EntityPlayer) entityliving1;
                entityplayer.currentImpulseImpactPos = this.calculateImpactPosition(entityplayer);
                entityplayer.setIgnoreFallDamageFromCurrentImpulse(true);
                entityplayer.connection.send(new PacketPlayOutEntityVelocity(entityplayer));
            }

            if (entityliving.onGround()) {
                if (entityliving1 instanceof EntityPlayer) {
                    entityplayer = (EntityPlayer) entityliving1;
                    entityplayer.setSpawnExtraParticlesOnFall(true);
                }

                SoundEffect soundeffect = entityliving1.fallDistance > 5.0F ? SoundEffects.MACE_SMASH_GROUND_HEAVY : SoundEffects.MACE_SMASH_GROUND;

                worldserver.playSound((EntityHuman) null, entityliving1.getX(), entityliving1.getY(), entityliving1.getZ(), soundeffect, entityliving1.getSoundSource(), 1.0F, 1.0F);
            } else {
                worldserver.playSound((EntityHuman) null, entityliving1.getX(), entityliving1.getY(), entityliving1.getZ(), SoundEffects.MACE_SMASH_AIR, entityliving1.getSoundSource(), 1.0F, 1.0F);
            }

            knockback(worldserver, entityliving1, entityliving);
        }

        return true;
    }

    private Vec3D calculateImpactPosition(EntityPlayer entityplayer) {
        return entityplayer.isIgnoringFallDamageFromCurrentImpulse() && entityplayer.currentImpulseImpactPos != null && entityplayer.currentImpulseImpactPos.y <= entityplayer.position().y ? entityplayer.currentImpulseImpactPos : entityplayer.position();
    }

    @Override
    public void postHurtEnemy(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1) {
        itemstack.hurtAndBreak(1, entityliving1, EnumItemSlot.MAINHAND);
        if (canSmashAttack(entityliving1)) {
            entityliving1.resetFallDistance();
        }

    }

    @Override
    public float getAttackDamageBonus(Entity entity, float f, DamageSource damagesource) {
        Entity entity1 = damagesource.getDirectEntity();

        if (entity1 instanceof EntityLiving entityliving) {
            if (!canSmashAttack(entityliving)) {
                return 0.0F;
            } else {
                float f1 = 3.0F;
                float f2 = 8.0F;
                float f3 = entityliving.fallDistance;
                float f4;

                if (f3 <= 3.0F) {
                    f4 = 4.0F * f3;
                } else if (f3 <= 8.0F) {
                    f4 = 12.0F + 2.0F * (f3 - 3.0F);
                } else {
                    f4 = 22.0F + f3 - 8.0F;
                }

                World world = entityliving.level();

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    return f4 + EnchantmentManager.modifyFallBasedDamage(worldserver, entityliving.getWeaponItem(), entity, damagesource, 0.0F) * f3;
                } else {
                    return f4;
                }
            }
        } else {
            return 0.0F;
        }
    }

    private static void knockback(World world, Entity entity, Entity entity1) {
        world.levelEvent(2013, entity1.getOnPos(), 750);
        world.getEntitiesOfClass(EntityLiving.class, entity1.getBoundingBox().inflate(3.5D), knockbackPredicate(entity, entity1)).forEach((entityliving) -> {
            Vec3D vec3d = entityliving.position().subtract(entity1.position());
            double d0 = getKnockbackPower(entity, entityliving, vec3d);
            Vec3D vec3d1 = vec3d.normalize().scale(d0);

            if (d0 > 0.0D) {
                entityliving.push(vec3d1.x, 0.699999988079071D, vec3d1.z);
                if (entityliving instanceof EntityPlayer) {
                    EntityPlayer entityplayer = (EntityPlayer) entityliving;

                    entityplayer.connection.send(new PacketPlayOutEntityVelocity(entityplayer));
                }
            }

        });
    }

    private static Predicate<EntityLiving> knockbackPredicate(Entity entity, Entity entity1) {
        return (entityliving) -> {
            boolean flag;
            boolean flag1;
            boolean flag2;
            boolean flag3;
            label62:
            {
                flag = !entityliving.isSpectator();
                flag1 = entityliving != entity && entityliving != entity1;
                flag2 = !entity.isAlliedTo((Entity) entityliving);
                if (entityliving instanceof EntityTameableAnimal entitytameableanimal) {
                    if (entitytameableanimal.isTame() && entity.getUUID().equals(entitytameableanimal.getOwnerUUID())) {
                        flag3 = true;
                        break label62;
                    }
                }

                flag3 = false;
            }

            boolean flag4;
            label55:
            {
                flag4 = !flag3;
                if (entityliving instanceof EntityArmorStand entityarmorstand) {
                    if (entityarmorstand.isMarker()) {
                        flag3 = false;
                        break label55;
                    }
                }

                flag3 = true;
            }

            boolean flag5 = flag3;
            boolean flag6 = entity1.distanceToSqr((Entity) entityliving) <= Math.pow(3.5D, 2.0D);

            return flag && flag1 && flag2 && flag4 && flag5 && flag6;
        };
    }

    private static double getKnockbackPower(Entity entity, EntityLiving entityliving, Vec3D vec3d) {
        return (3.5D - vec3d.length()) * 0.699999988079071D * (double) (entity.fallDistance > 5.0F ? 2 : 1) * (1.0D - entityliving.getAttributeValue(GenericAttributes.KNOCKBACK_RESISTANCE));
    }

    public static boolean canSmashAttack(EntityLiving entityliving) {
        return entityliving.fallDistance > 1.5F && !entityliving.isFallFlying();
    }

    @Nullable
    @Override
    public DamageSource getDamageSource(EntityLiving entityliving) {
        return canSmashAttack(entityliving) ? entityliving.damageSources().mace(entityliving) : super.getDamageSource(entityliving);
    }
}
