package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public abstract class BlockAttachedEntity extends Entity {

    private static final Logger LOGGER = LogUtils.getLogger();
    private int checkInterval;
    protected BlockPosition pos;

    protected BlockAttachedEntity(EntityTypes<? extends BlockAttachedEntity> entitytypes, World world) {
        super(entitytypes, world);
    }

    protected BlockAttachedEntity(EntityTypes<? extends BlockAttachedEntity> entitytypes, World world, BlockPosition blockposition) {
        this(entitytypes, world);
        this.pos = blockposition;
    }

    protected abstract void recalculateBoundingBox();

    @Override
    public void tick() {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            this.checkBelowWorld();
            if (this.checkInterval++ == 100) {
                this.checkInterval = 0;
                if (!this.isRemoved() && !this.survives()) {
                    this.discard();
                    this.dropItem(worldserver, (Entity) null);
                }
            }
        }

    }

    public abstract boolean survives();

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        if (entity instanceof EntityHuman entityhuman) {
            return !this.level().mayInteract(entityhuman, this.pos) ? true : this.hurtOrSimulate(this.damageSources().playerAttack(entityhuman), 0.0F);
        } else {
            return false;
        }
    }

    @Override
    public boolean hurtClient(DamageSource damagesource) {
        return !this.isInvulnerableToBase(damagesource);
    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        if (this.isInvulnerableToBase(damagesource)) {
            return false;
        } else if (!worldserver.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && damagesource.getEntity() instanceof EntityInsentient) {
            return false;
        } else {
            if (!this.isRemoved()) {
                this.kill(worldserver);
                this.markHurt();
                this.dropItem(worldserver, damagesource.getEntity());
            }

            return true;
        }
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return explosion.shouldAffectBlocklikeEntities() ? super.ignoreExplosion(explosion) : true;
    }

    @Override
    public void move(EnumMoveType enummovetype, Vec3D vec3d) {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            if (!this.isRemoved() && vec3d.lengthSqr() > 0.0D) {
                this.kill(worldserver);
                this.dropItem(worldserver, (Entity) null);
            }
        }

    }

    @Override
    public void push(double d0, double d1, double d2) {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            if (!this.isRemoved() && d0 * d0 + d1 * d1 + d2 * d2 > 0.0D) {
                this.kill(worldserver);
                this.dropItem(worldserver, (Entity) null);
            }
        }

    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        BlockPosition blockposition = this.getPos();

        nbttagcompound.putInt("TileX", blockposition.getX());
        nbttagcompound.putInt("TileY", blockposition.getY());
        nbttagcompound.putInt("TileZ", blockposition.getZ());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        BlockPosition blockposition = new BlockPosition(nbttagcompound.getInt("TileX"), nbttagcompound.getInt("TileY"), nbttagcompound.getInt("TileZ"));

        if (!blockposition.closerThan(this.blockPosition(), 16.0D)) {
            BlockAttachedEntity.LOGGER.error("Block-attached entity at invalid position: {}", blockposition);
        } else {
            this.pos = blockposition;
        }
    }

    public abstract void dropItem(WorldServer worldserver, @Nullable Entity entity);

    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public void setPos(double d0, double d1, double d2) {
        this.pos = BlockPosition.containing(d0, d1, d2);
        this.recalculateBoundingBox();
        this.hasImpulse = true;
    }

    public BlockPosition getPos() {
        return this.pos;
    }

    @Override
    public void thunderHit(WorldServer worldserver, EntityLightning entitylightning) {}

    @Override
    public void refreshDimensions() {}
}
