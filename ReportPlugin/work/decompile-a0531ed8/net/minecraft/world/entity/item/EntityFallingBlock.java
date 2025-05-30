package net.minecraft.world.entity.item;

import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.EntityTrackerEntry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.item.context.BlockActionContextDirectional;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockAnvil;
import net.minecraft.world.level.block.BlockConcretePowder;
import net.minecraft.world.level.block.BlockFalling;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class EntityFallingBlock extends Entity {

    private static final Logger LOGGER = LogUtils.getLogger();
    private IBlockData blockState;
    public int time;
    public boolean dropItem;
    public boolean cancelDrop;
    public boolean hurtEntities;
    public int fallDamageMax;
    public float fallDamagePerDistance;
    @Nullable
    public NBTTagCompound blockData;
    public boolean forceTickAfterTeleportToDuplicate;
    protected static final DataWatcherObject<BlockPosition> DATA_START_POS = DataWatcher.defineId(EntityFallingBlock.class, DataWatcherRegistry.BLOCK_POS);

    public EntityFallingBlock(EntityTypes<? extends EntityFallingBlock> entitytypes, World world) {
        super(entitytypes, world);
        this.blockState = Blocks.SAND.defaultBlockState();
        this.dropItem = true;
        this.fallDamageMax = 40;
    }

    private EntityFallingBlock(World world, double d0, double d1, double d2, IBlockData iblockdata) {
        this(EntityTypes.FALLING_BLOCK, world);
        this.blockState = iblockdata;
        this.blocksBuilding = true;
        this.setPos(d0, d1, d2);
        this.setDeltaMovement(Vec3D.ZERO);
        this.xo = d0;
        this.yo = d1;
        this.zo = d2;
        this.setStartPos(this.blockPosition());
    }

    public static EntityFallingBlock fall(World world, BlockPosition blockposition, IBlockData iblockdata) {
        EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, (double) blockposition.getX() + 0.5D, (double) blockposition.getY(), (double) blockposition.getZ() + 0.5D, iblockdata.hasProperty(BlockProperties.WATERLOGGED) ? (IBlockData) iblockdata.setValue(BlockProperties.WATERLOGGED, false) : iblockdata);

        world.setBlock(blockposition, iblockdata.getFluidState().createLegacyBlock(), 3);
        world.addFreshEntity(entityfallingblock);
        return entityfallingblock;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public final boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        if (!this.isInvulnerableToBase(damagesource)) {
            this.markHurt();
        }

        return false;
    }

    public void setStartPos(BlockPosition blockposition) {
        this.entityData.set(EntityFallingBlock.DATA_START_POS, blockposition);
    }

    public BlockPosition getStartPos() {
        return (BlockPosition) this.entityData.get(EntityFallingBlock.DATA_START_POS);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityFallingBlock.DATA_START_POS, BlockPosition.ZERO);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04D;
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
        } else {
            Block block = this.blockState.getBlock();

            ++this.time;
            this.applyGravity();
            this.move(EnumMoveType.SELF, this.getDeltaMovement());
            this.applyEffectsFromBlocks();
            this.handlePortal();
            World world = this.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                if (this.isAlive() || this.forceTickAfterTeleportToDuplicate) {
                    BlockPosition blockposition = this.blockPosition();
                    boolean flag = this.blockState.getBlock() instanceof BlockConcretePowder;
                    boolean flag1 = flag && this.level().getFluidState(blockposition).is(TagsFluid.WATER);
                    double d0 = this.getDeltaMovement().lengthSqr();

                    if (flag && d0 > 1.0D) {
                        MovingObjectPositionBlock movingobjectpositionblock = this.level().clip(new RayTrace(new Vec3D(this.xo, this.yo, this.zo), this.position(), RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.SOURCE_ONLY, this));

                        if (movingobjectpositionblock.getType() != MovingObjectPosition.EnumMovingObjectType.MISS && this.level().getFluidState(movingobjectpositionblock.getBlockPos()).is(TagsFluid.WATER)) {
                            blockposition = movingobjectpositionblock.getBlockPos();
                            flag1 = true;
                        }
                    }

                    if (!this.onGround() && !flag1) {
                        if (this.time > 100 && (blockposition.getY() <= this.level().getMinY() || blockposition.getY() > this.level().getMaxY()) || this.time > 600) {
                            if (this.dropItem && worldserver.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                this.spawnAtLocation(worldserver, (IMaterial) block);
                            }

                            this.discard();
                        }
                    } else {
                        IBlockData iblockdata = this.level().getBlockState(blockposition);

                        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
                        if (!iblockdata.is(Blocks.MOVING_PISTON)) {
                            if (!this.cancelDrop) {
                                boolean flag2 = iblockdata.canBeReplaced((BlockActionContext) (new BlockActionContextDirectional(this.level(), blockposition, EnumDirection.DOWN, ItemStack.EMPTY, EnumDirection.UP)));
                                boolean flag3 = BlockFalling.isFree(this.level().getBlockState(blockposition.below())) && (!flag || !flag1);
                                boolean flag4 = this.blockState.canSurvive(this.level(), blockposition) && !flag3;

                                if (flag2 && flag4) {
                                    if (this.blockState.hasProperty(BlockProperties.WATERLOGGED) && this.level().getFluidState(blockposition).getType() == FluidTypes.WATER) {
                                        this.blockState = (IBlockData) this.blockState.setValue(BlockProperties.WATERLOGGED, true);
                                    }

                                    if (this.level().setBlock(blockposition, this.blockState, 3)) {
                                        ((WorldServer) this.level()).getChunkSource().chunkMap.broadcast(this, new PacketPlayOutBlockChange(blockposition, this.level().getBlockState(blockposition)));
                                        this.discard();
                                        if (block instanceof Fallable) {
                                            ((Fallable) block).onLand(this.level(), blockposition, this.blockState, iblockdata, this);
                                        }

                                        if (this.blockData != null && this.blockState.hasBlockEntity()) {
                                            TileEntity tileentity = this.level().getBlockEntity(blockposition);

                                            if (tileentity != null) {
                                                NBTTagCompound nbttagcompound = tileentity.saveWithoutMetadata(this.level().registryAccess());
                                                Iterator iterator = this.blockData.getAllKeys().iterator();

                                                while (iterator.hasNext()) {
                                                    String s = (String) iterator.next();

                                                    nbttagcompound.put(s, this.blockData.get(s).copy());
                                                }

                                                try {
                                                    tileentity.loadWithComponents(nbttagcompound, this.level().registryAccess());
                                                } catch (Exception exception) {
                                                    EntityFallingBlock.LOGGER.error("Failed to load block entity from falling block", exception);
                                                }

                                                tileentity.setChanged();
                                            }
                                        }
                                    } else if (this.dropItem && worldserver.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                        this.discard();
                                        this.callOnBrokenAfterFall(block, blockposition);
                                        this.spawnAtLocation(worldserver, (IMaterial) block);
                                    }
                                } else {
                                    this.discard();
                                    if (this.dropItem && worldserver.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                        this.callOnBrokenAfterFall(block, blockposition);
                                        this.spawnAtLocation(worldserver, (IMaterial) block);
                                    }
                                }
                            } else {
                                this.discard();
                                this.callOnBrokenAfterFall(block, blockposition);
                            }
                        }
                    }
                }
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        }
    }

    public void callOnBrokenAfterFall(Block block, BlockPosition blockposition) {
        if (block instanceof Fallable) {
            ((Fallable) block).onBrokenAfterFall(this.level(), blockposition, this);
        }

    }

    @Override
    public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
        if (!this.hurtEntities) {
            return false;
        } else {
            int i = MathHelper.ceil(f - 1.0F);

            if (i < 0) {
                return false;
            } else {
                Predicate<Entity> predicate = IEntitySelector.NO_CREATIVE_OR_SPECTATOR.and(IEntitySelector.LIVING_ENTITY_STILL_ALIVE);
                Block block = this.blockState.getBlock();
                DamageSource damagesource1;

                if (block instanceof Fallable) {
                    Fallable fallable = (Fallable) block;

                    damagesource1 = fallable.getFallDamageSource(this);
                } else {
                    damagesource1 = this.damageSources().fallingBlock(this);
                }

                DamageSource damagesource2 = damagesource1;
                float f2 = (float) Math.min(MathHelper.floor((float) i * this.fallDamagePerDistance), this.fallDamageMax);

                this.level().getEntities((Entity) this, this.getBoundingBox(), predicate).forEach((entity) -> {
                    entity.hurt(damagesource2, f2);
                });
                boolean flag = this.blockState.is(TagsBlock.ANVIL);

                if (flag && f2 > 0.0F && this.random.nextFloat() < 0.05F + (float) i * 0.05F) {
                    IBlockData iblockdata = BlockAnvil.damage(this.blockState);

                    if (iblockdata == null) {
                        this.cancelDrop = true;
                    } else {
                        this.blockState = iblockdata;
                    }
                }

                return false;
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        nbttagcompound.put("BlockState", GameProfileSerializer.writeBlockState(this.blockState));
        nbttagcompound.putInt("Time", this.time);
        nbttagcompound.putBoolean("DropItem", this.dropItem);
        nbttagcompound.putBoolean("HurtEntities", this.hurtEntities);
        nbttagcompound.putFloat("FallHurtAmount", this.fallDamagePerDistance);
        nbttagcompound.putInt("FallHurtMax", this.fallDamageMax);
        if (this.blockData != null) {
            nbttagcompound.put("TileEntityData", this.blockData);
        }

        nbttagcompound.putBoolean("CancelDrop", this.cancelDrop);
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        this.blockState = GameProfileSerializer.readBlockState(this.level().holderLookup(Registries.BLOCK), nbttagcompound.getCompound("BlockState"));
        this.time = nbttagcompound.getInt("Time");
        if (nbttagcompound.contains("HurtEntities", 99)) {
            this.hurtEntities = nbttagcompound.getBoolean("HurtEntities");
            this.fallDamagePerDistance = nbttagcompound.getFloat("FallHurtAmount");
            this.fallDamageMax = nbttagcompound.getInt("FallHurtMax");
        } else if (this.blockState.is(TagsBlock.ANVIL)) {
            this.hurtEntities = true;
        }

        if (nbttagcompound.contains("DropItem", 99)) {
            this.dropItem = nbttagcompound.getBoolean("DropItem");
        }

        if (nbttagcompound.contains("TileEntityData", 10)) {
            this.blockData = nbttagcompound.getCompound("TileEntityData").copy();
        }

        this.cancelDrop = nbttagcompound.getBoolean("CancelDrop");
        if (this.blockState.isAir()) {
            this.blockState = Blocks.SAND.defaultBlockState();
        }

    }

    public void setHurtsEntities(float f, int i) {
        this.hurtEntities = true;
        this.fallDamagePerDistance = f;
        this.fallDamageMax = i;
    }

    public void disableDrop() {
        this.cancelDrop = true;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportSystemDetails crashreportsystemdetails) {
        super.fillCrashReportCategory(crashreportsystemdetails);
        crashreportsystemdetails.setDetail("Immitating BlockState", (Object) this.blockState.toString());
    }

    public IBlockData getBlockState() {
        return this.blockState;
    }

    @Override
    protected IChatBaseComponent getTypeName() {
        return IChatBaseComponent.translatable("entity.minecraft.falling_block_type", this.blockState.getBlock().getName());
    }

    @Override
    public Packet<PacketListenerPlayOut> getAddEntityPacket(EntityTrackerEntry entitytrackerentry) {
        return new PacketPlayOutSpawnEntity(this, entitytrackerentry, Block.getId(this.getBlockState()));
    }

    @Override
    public void recreateFromPacket(PacketPlayOutSpawnEntity packetplayoutspawnentity) {
        super.recreateFromPacket(packetplayoutspawnentity);
        this.blockState = Block.stateById(packetplayoutspawnentity.getData());
        this.blocksBuilding = true;
        double d0 = packetplayoutspawnentity.getX();
        double d1 = packetplayoutspawnentity.getY();
        double d2 = packetplayoutspawnentity.getZ();

        this.setPos(d0, d1, d2);
        this.setStartPos(this.blockPosition());
    }

    @Nullable
    @Override
    public Entity teleport(TeleportTransition teleporttransition) {
        ResourceKey<World> resourcekey = teleporttransition.newLevel().dimension();
        ResourceKey<World> resourcekey1 = this.level().dimension();
        boolean flag = (resourcekey1 == World.END || resourcekey == World.END) && resourcekey1 != resourcekey;
        Entity entity = super.teleport(teleporttransition);

        this.forceTickAfterTeleportToDuplicate = entity != null && flag;
        return entity;
    }
}
