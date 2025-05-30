package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockFluids;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public class ItemMonsterEgg extends Item {

    private static final Map<EntityTypes<? extends EntityInsentient>, ItemMonsterEgg> BY_ID = Maps.newIdentityHashMap();
    private final EntityTypes<?> defaultType;

    public ItemMonsterEgg(EntityTypes<? extends EntityInsentient> entitytypes, Item.Info item_info) {
        super(item_info);
        this.defaultType = entitytypes;
        ItemMonsterEgg.BY_ID.put(entitytypes, this);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();

        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            ItemStack itemstack = itemactioncontext.getItemInHand();
            BlockPosition blockposition = itemactioncontext.getClickedPos();
            EnumDirection enumdirection = itemactioncontext.getClickedFace();
            IBlockData iblockdata = world.getBlockState(blockposition);
            TileEntity tileentity = world.getBlockEntity(blockposition);
            EntityTypes entitytypes;

            if (tileentity instanceof Spawner) {
                Spawner spawner = (Spawner) tileentity;

                entitytypes = this.getType(world.registryAccess(), itemstack);
                spawner.setEntityId(entitytypes, world.getRandom());
                world.sendBlockUpdated(blockposition, iblockdata, iblockdata, 3);
                world.gameEvent((Entity) itemactioncontext.getPlayer(), (Holder) GameEvent.BLOCK_CHANGE, blockposition);
                itemstack.shrink(1);
                return EnumInteractionResult.SUCCESS;
            } else {
                BlockPosition blockposition1;

                if (iblockdata.getCollisionShape(world, blockposition).isEmpty()) {
                    blockposition1 = blockposition;
                } else {
                    blockposition1 = blockposition.relative(enumdirection);
                }

                entitytypes = this.getType(world.registryAccess(), itemstack);
                if (entitytypes.spawn((WorldServer) world, itemstack, itemactioncontext.getPlayer(), blockposition1, EntitySpawnReason.SPAWN_ITEM_USE, true, !Objects.equals(blockposition, blockposition1) && enumdirection == EnumDirection.UP) != null) {
                    itemstack.shrink(1);
                    world.gameEvent((Entity) itemactioncontext.getPlayer(), (Holder) GameEvent.ENTITY_PLACE, blockposition);
                }

                return EnumInteractionResult.SUCCESS;
            }
        }
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        MovingObjectPositionBlock movingobjectpositionblock = getPlayerPOVHitResult(world, entityhuman, RayTrace.FluidCollisionOption.SOURCE_ONLY);

        if (movingobjectpositionblock.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            return EnumInteractionResult.PASS;
        } else if (world instanceof WorldServer) {
            WorldServer worldserver = (WorldServer) world;
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();

            if (!(world.getBlockState(blockposition).getBlock() instanceof BlockFluids)) {
                return EnumInteractionResult.PASS;
            } else if (world.mayInteract(entityhuman, blockposition) && entityhuman.mayUseItemAt(blockposition, movingobjectpositionblock.getDirection(), itemstack)) {
                EntityTypes<?> entitytypes = this.getType(worldserver.registryAccess(), itemstack);
                Entity entity = entitytypes.spawn(worldserver, itemstack, entityhuman, blockposition, EntitySpawnReason.SPAWN_ITEM_USE, false, false);

                if (entity == null) {
                    return EnumInteractionResult.PASS;
                } else {
                    itemstack.consume(1, entityhuman);
                    entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
                    world.gameEvent((Entity) entityhuman, (Holder) GameEvent.ENTITY_PLACE, entity.position());
                    return EnumInteractionResult.SUCCESS;
                }
            } else {
                return EnumInteractionResult.FAIL;
            }
        } else {
            return EnumInteractionResult.SUCCESS;
        }
    }

    public boolean spawnsEntity(HolderLookup.a holderlookup_a, ItemStack itemstack, EntityTypes<?> entitytypes) {
        return Objects.equals(this.getType(holderlookup_a, itemstack), entitytypes);
    }

    @Nullable
    public static ItemMonsterEgg byId(@Nullable EntityTypes<?> entitytypes) {
        return (ItemMonsterEgg) ItemMonsterEgg.BY_ID.get(entitytypes);
    }

    public static Iterable<ItemMonsterEgg> eggs() {
        return Iterables.unmodifiableIterable(ItemMonsterEgg.BY_ID.values());
    }

    public EntityTypes<?> getType(HolderLookup.a holderlookup_a, ItemStack itemstack) {
        CustomData customdata = (CustomData) itemstack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);

        if (!customdata.isEmpty()) {
            EntityTypes<?> entitytypes = (EntityTypes) customdata.parseEntityType(holderlookup_a, Registries.ENTITY_TYPE);

            if (entitytypes != null) {
                return entitytypes;
            }
        }

        return this.defaultType;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.defaultType.requiredFeatures();
    }

    public Optional<EntityInsentient> spawnOffspringFromSpawnEgg(EntityHuman entityhuman, EntityInsentient entityinsentient, EntityTypes<? extends EntityInsentient> entitytypes, WorldServer worldserver, Vec3D vec3d, ItemStack itemstack) {
        if (!this.spawnsEntity(worldserver.registryAccess(), itemstack, entitytypes)) {
            return Optional.empty();
        } else {
            Object object;

            if (entityinsentient instanceof EntityAgeable) {
                object = ((EntityAgeable) entityinsentient).getBreedOffspring(worldserver, (EntityAgeable) entityinsentient);
            } else {
                object = (EntityInsentient) entitytypes.create(worldserver, EntitySpawnReason.SPAWN_ITEM_USE);
            }

            if (object == null) {
                return Optional.empty();
            } else {
                ((EntityInsentient) object).setBaby(true);
                if (!((EntityInsentient) object).isBaby()) {
                    return Optional.empty();
                } else {
                    ((EntityInsentient) object).moveTo(vec3d.x(), vec3d.y(), vec3d.z(), 0.0F, 0.0F);
                    worldserver.addFreshEntityWithPassengers((Entity) object);
                    ((EntityInsentient) object).setCustomName((IChatBaseComponent) itemstack.get(DataComponents.CUSTOM_NAME));
                    itemstack.consume(1, entityhuman);
                    return Optional.of(object);
                }
            }
        }
    }

    @Override
    public boolean shouldPrintOpWarning(ItemStack itemstack, @Nullable EntityHuman entityhuman) {
        if (entityhuman != null && entityhuman.getPermissionLevel() >= 2) {
            CustomData customdata = (CustomData) itemstack.get(DataComponents.ENTITY_DATA);

            if (customdata != null) {
                EntityTypes<?> entitytypes = (EntityTypes) customdata.parseEntityType(entityhuman.level().registryAccess(), Registries.ENTITY_TYPE);

                return entitytypes != null && entitytypes.onlyOpCanSetNbt();
            }
        }

        return false;
    }
}
