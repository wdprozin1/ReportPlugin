package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.BehaviorAttack;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetForget;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetSet;
import net.minecraft.world.entity.ai.behavior.BehaviorFollowAdult;
import net.minecraft.world.entity.ai.behavior.BehaviorGate;
import net.minecraft.world.entity.ai.behavior.BehaviorGateSingle;
import net.minecraft.world.entity.ai.behavior.BehaviorLook;
import net.minecraft.world.entity.ai.behavior.BehaviorLookWalk;
import net.minecraft.world.entity.ai.behavior.BehaviorMakeLoveAnimal;
import net.minecraft.world.entity.ai.behavior.BehaviorPosition;
import net.minecraft.world.entity.ai.behavior.BehaviorRemoveMemory;
import net.minecraft.world.entity.ai.behavior.BehaviorStrollRandomUnconstrained;
import net.minecraft.world.entity.ai.behavior.BehaviorUtil;
import net.minecraft.world.entity.ai.behavior.BehaviorWalkAwayOutOfRange;
import net.minecraft.world.entity.ai.behavior.BehavorMove;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.TryFindWater;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;

public class AxolotlAi {

    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 0.2F;
    private static final float SPEED_MULTIPLIER_ON_LAND = 0.15F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING_IN_WATER = 0.5F;
    private static final float SPEED_MULTIPLIER_WHEN_CHASING_IN_WATER = 0.6F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT_IN_WATER = 0.6F;

    public AxolotlAi() {}

    protected static BehaviorController<?> makeBrain(BehaviorController<Axolotl> behaviorcontroller) {
        initCoreActivity(behaviorcontroller);
        initIdleActivity(behaviorcontroller);
        initFightActivity(behaviorcontroller);
        initPlayDeadActivity(behaviorcontroller);
        behaviorcontroller.setCoreActivities(ImmutableSet.of(Activity.CORE));
        behaviorcontroller.setDefaultActivity(Activity.IDLE);
        behaviorcontroller.useDefaultActivity();
        return behaviorcontroller;
    }

    private static void initPlayDeadActivity(BehaviorController<Axolotl> behaviorcontroller) {
        behaviorcontroller.addActivityAndRemoveMemoriesWhenStopped(Activity.PLAY_DEAD, ImmutableList.of(Pair.of(0, new PlayDead()), Pair.of(1, BehaviorRemoveMemory.create(BehaviorUtil::isBreeding, MemoryModuleType.PLAY_DEAD_TICKS))), ImmutableSet.of(Pair.of(MemoryModuleType.PLAY_DEAD_TICKS, MemoryStatus.VALUE_PRESENT)), ImmutableSet.of(MemoryModuleType.PLAY_DEAD_TICKS));
    }

    private static void initFightActivity(BehaviorController<Axolotl> behaviorcontroller) {
        behaviorcontroller.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0, ImmutableList.of(BehaviorAttackTargetForget.create(Axolotl::onStopAttacking), BehaviorWalkAwayOutOfRange.create(AxolotlAi::getSpeedModifierChasing), BehaviorAttack.create(20), BehaviorRemoveMemory.create(BehaviorUtil::isBreeding, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initCoreActivity(BehaviorController<Axolotl> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.CORE, 0, ImmutableList.of(new BehaviorLook(45, 90), new BehavorMove(), ValidatePlayDead.create(), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(BehaviorController<Axolotl> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.IDLE, ImmutableList.of(Pair.of(0, SetEntityLookTargetSometimes.create(EntityTypes.PLAYER, 6.0F, UniformInt.of(30, 60))), Pair.of(1, new BehaviorMakeLoveAnimal(EntityTypes.AXOLOTL, 0.2F, 2)), Pair.of(2, new BehaviorGateSingle<>(ImmutableList.of(Pair.of(new FollowTemptation(AxolotlAi::getSpeedModifier), 1), Pair.of(BehaviorFollowAdult.create(AxolotlAi.ADULT_FOLLOW_RANGE, AxolotlAi::getSpeedModifierFollowingAdult), 1)))), Pair.of(3, BehaviorAttackTargetSet.create(AxolotlAi::findNearestValidAttackTarget)), Pair.of(3, TryFindWater.create(6, 0.15F)), Pair.of(4, new BehaviorGate<>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableSet.of(), BehaviorGate.Order.ORDERED, BehaviorGate.Execution.TRY_ALL, ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.swim(0.5F), 2), Pair.of(BehaviorStrollRandomUnconstrained.stroll(0.15F, false), 2), Pair.of(BehaviorLookWalk.create(AxolotlAi::canSetWalkTargetFromLookTarget, AxolotlAi::getSpeedModifier, 3), 3), Pair.of(BehaviorBuilder.triggerIf(Entity::isInWaterOrBubble), 5), Pair.of(BehaviorBuilder.triggerIf(Entity::onGround), 5))))));
    }

    private static boolean canSetWalkTargetFromLookTarget(EntityLiving entityliving) {
        World world = entityliving.level();
        Optional<BehaviorPosition> optional = entityliving.getBrain().getMemory(MemoryModuleType.LOOK_TARGET);

        if (optional.isPresent()) {
            BlockPosition blockposition = ((BehaviorPosition) optional.get()).currentBlockPosition();

            return world.isWaterAt(blockposition) == entityliving.isInWaterOrBubble();
        } else {
            return false;
        }
    }

    public static void updateActivity(Axolotl axolotl) {
        BehaviorController<Axolotl> behaviorcontroller = axolotl.getBrain();
        Activity activity = (Activity) behaviorcontroller.getActiveNonCoreActivity().orElse((Object) null);

        if (activity != Activity.PLAY_DEAD) {
            behaviorcontroller.setActiveActivityToFirstValid(ImmutableList.of(Activity.PLAY_DEAD, Activity.FIGHT, Activity.IDLE));
            if (activity == Activity.FIGHT && behaviorcontroller.getActiveNonCoreActivity().orElse((Object) null) != Activity.FIGHT) {
                behaviorcontroller.setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, 2400L);
            }
        }

    }

    private static float getSpeedModifierChasing(EntityLiving entityliving) {
        return entityliving.isInWaterOrBubble() ? 0.6F : 0.15F;
    }

    private static float getSpeedModifierFollowingAdult(EntityLiving entityliving) {
        return entityliving.isInWaterOrBubble() ? 0.6F : 0.15F;
    }

    private static float getSpeedModifier(EntityLiving entityliving) {
        return entityliving.isInWaterOrBubble() ? 0.5F : 0.15F;
    }

    private static Optional<? extends EntityLiving> findNearestValidAttackTarget(WorldServer worldserver, Axolotl axolotl) {
        return BehaviorUtil.isBreeding(axolotl) ? Optional.empty() : axolotl.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
    }

    public static Predicate<ItemStack> getTemptations() {
        return (itemstack) -> {
            return itemstack.is(TagsItem.AXOLOTL_FOOD);
        };
    }
}
