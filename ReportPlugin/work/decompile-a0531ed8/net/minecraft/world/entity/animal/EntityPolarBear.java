package net.minecraft.world.entity.animal;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeRange;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.IEntityAngerable;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFollowParent;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer;
import net.minecraft.world.entity.ai.goal.PathfinderGoalMeleeAttack;
import net.minecraft.world.entity.ai.goal.PathfinderGoalPanic;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomLookaround;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStroll;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalUniversalAngerReset;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.state.IBlockData;

public class EntityPolarBear extends EntityAnimal implements IEntityAngerable {

    private static final DataWatcherObject<Boolean> DATA_STANDING_ID = DataWatcher.defineId(EntityPolarBear.class, DataWatcherRegistry.BOOLEAN);
    private static final float STAND_ANIMATION_TICKS = 6.0F;
    private float clientSideStandAnimationO;
    private float clientSideStandAnimation;
    private int warningSoundTicks;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeRange.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;

    public EntityPolarBear(EntityTypes<? extends EntityPolarBear> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Nullable
    @Override
    public EntityAgeable getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        return (EntityAgeable) EntityTypes.POLAR_BEAR.create(worldserver, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return false;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new PathfinderGoalFloat(this));
        this.goalSelector.addGoal(1, new EntityPolarBear.c());
        this.goalSelector.addGoal(1, new PathfinderGoalPanic(this, 2.0D, (entitycreature) -> {
            return entitycreature.isBaby() ? DamageTypeTags.PANIC_CAUSES : DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES;
        }));
        this.goalSelector.addGoal(4, new PathfinderGoalFollowParent(this, 1.25D));
        this.goalSelector.addGoal(5, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.addGoal(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.addGoal(7, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.addGoal(1, new EntityPolarBear.b());
        this.targetSelector.addGoal(2, new EntityPolarBear.a());
        this.targetSelector.addGoal(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(4, new PathfinderGoalNearestAttackableTarget<>(this, EntityFox.class, 10, true, true, (PathfinderTargetCondition.a) null));
        this.targetSelector.addGoal(5, new PathfinderGoalUniversalAngerReset<>(this, false));
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityAnimal.createAnimalAttributes().add(GenericAttributes.MAX_HEALTH, 30.0D).add(GenericAttributes.FOLLOW_RANGE, 20.0D).add(GenericAttributes.MOVEMENT_SPEED, 0.25D).add(GenericAttributes.ATTACK_DAMAGE, 6.0D);
    }

    public static boolean checkPolarBearSpawnRules(EntityTypes<EntityPolarBear> entitytypes, GeneratorAccess generatoraccess, EntitySpawnReason entityspawnreason, BlockPosition blockposition, RandomSource randomsource) {
        Holder<BiomeBase> holder = generatoraccess.getBiome(blockposition);

        return !holder.is(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS) ? checkAnimalSpawnRules(entitytypes, generatoraccess, entityspawnreason, blockposition, randomsource) : isBrightEnoughToSpawn(generatoraccess, blockposition) && generatoraccess.getBlockState(blockposition.below()).is(TagsBlock.POLAR_BEARS_SPAWNABLE_ON_ALTERNATE);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.readPersistentAngerSaveData(this.level(), nbttagcompound);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        this.addPersistentAngerSaveData(nbttagcompound);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(EntityPolarBear.PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int i) {
        this.remainingPersistentAngerTime = i;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID uuid) {
        this.persistentAngerTarget = uuid;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return this.isBaby() ? SoundEffects.POLAR_BEAR_AMBIENT_BABY : SoundEffects.POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.POLAR_BEAR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.POLAR_BEAR_STEP, 0.15F, 1.0F);
    }

    protected void playWarningSound() {
        if (this.warningSoundTicks <= 0) {
            this.makeSound(SoundEffects.POLAR_BEAR_WARNING);
            this.warningSoundTicks = 40;
        }

    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityPolarBear.DATA_STANDING_ID, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.clientSideStandAnimation != this.clientSideStandAnimationO) {
                this.refreshDimensions();
            }

            this.clientSideStandAnimationO = this.clientSideStandAnimation;
            if (this.isStanding()) {
                this.clientSideStandAnimation = MathHelper.clamp(this.clientSideStandAnimation + 1.0F, 0.0F, 6.0F);
            } else {
                this.clientSideStandAnimation = MathHelper.clamp(this.clientSideStandAnimation - 1.0F, 0.0F, 6.0F);
            }
        }

        if (this.warningSoundTicks > 0) {
            --this.warningSoundTicks;
        }

        if (!this.level().isClientSide) {
            this.updatePersistentAnger((WorldServer) this.level(), true);
        }

    }

    @Override
    public EntitySize getDefaultDimensions(EntityPose entitypose) {
        if (this.clientSideStandAnimation > 0.0F) {
            float f = this.clientSideStandAnimation / 6.0F;
            float f1 = 1.0F + f;

            return super.getDefaultDimensions(entitypose).scale(1.0F, f1);
        } else {
            return super.getDefaultDimensions(entitypose);
        }
    }

    public boolean isStanding() {
        return (Boolean) this.entityData.get(EntityPolarBear.DATA_STANDING_ID);
    }

    public void setStanding(boolean flag) {
        this.entityData.set(EntityPolarBear.DATA_STANDING_ID, flag);
    }

    public float getStandingAnimationScale(float f) {
        return MathHelper.lerp(f, this.clientSideStandAnimationO, this.clientSideStandAnimation) / 6.0F;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        if (groupdataentity == null) {
            groupdataentity = new EntityAgeable.a(1.0F);
        }

        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, (GroupDataEntity) groupdataentity);
    }

    private class c extends PathfinderGoalMeleeAttack {

        public c() {
            super(EntityPolarBear.this, 1.25D, true);
        }

        @Override
        protected void checkAndPerformAttack(EntityLiving entityliving) {
            if (this.canPerformAttack(entityliving)) {
                this.resetAttackCooldown();
                this.mob.doHurtTarget(getServerLevel((Entity) this.mob), entityliving);
                EntityPolarBear.this.setStanding(false);
            } else if (this.mob.distanceToSqr((Entity) entityliving) < (double) ((entityliving.getBbWidth() + 3.0F) * (entityliving.getBbWidth() + 3.0F))) {
                if (this.isTimeToAttack()) {
                    EntityPolarBear.this.setStanding(false);
                    this.resetAttackCooldown();
                }

                if (this.getTicksUntilNextAttack() <= 10) {
                    EntityPolarBear.this.setStanding(true);
                    EntityPolarBear.this.playWarningSound();
                }
            } else {
                this.resetAttackCooldown();
                EntityPolarBear.this.setStanding(false);
            }

        }

        @Override
        public void stop() {
            EntityPolarBear.this.setStanding(false);
            super.stop();
        }
    }

    private class b extends PathfinderGoalHurtByTarget {

        public b() {
            super(EntityPolarBear.this);
        }

        @Override
        public void start() {
            super.start();
            if (EntityPolarBear.this.isBaby()) {
                this.alertOthers();
                this.stop();
            }

        }

        @Override
        protected void alertOther(EntityInsentient entityinsentient, EntityLiving entityliving) {
            if (entityinsentient instanceof EntityPolarBear && !entityinsentient.isBaby()) {
                super.alertOther(entityinsentient, entityliving);
            }

        }
    }

    private class a extends PathfinderGoalNearestAttackableTarget<EntityHuman> {

        public a() {
            super(EntityPolarBear.this, EntityHuman.class, 20, true, true, (PathfinderTargetCondition.a) null);
        }

        @Override
        public boolean canUse() {
            if (EntityPolarBear.this.isBaby()) {
                return false;
            } else {
                if (super.canUse()) {
                    List<EntityPolarBear> list = EntityPolarBear.this.level().getEntitiesOfClass(EntityPolarBear.class, EntityPolarBear.this.getBoundingBox().inflate(8.0D, 4.0D, 8.0D));
                    Iterator iterator = list.iterator();

                    while (iterator.hasNext()) {
                        EntityPolarBear entitypolarbear = (EntityPolarBear) iterator.next();

                        if (entitypolarbear.isBaby()) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.5D;
        }
    }
}
