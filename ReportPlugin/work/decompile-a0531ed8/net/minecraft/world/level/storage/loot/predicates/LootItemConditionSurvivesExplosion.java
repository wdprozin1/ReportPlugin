package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public class LootItemConditionSurvivesExplosion implements LootItemCondition {

    private static final LootItemConditionSurvivesExplosion INSTANCE = new LootItemConditionSurvivesExplosion();
    public static final MapCodec<LootItemConditionSurvivesExplosion> CODEC = MapCodec.unit(LootItemConditionSurvivesExplosion.INSTANCE);

    private LootItemConditionSurvivesExplosion() {}

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.SURVIVES_EXPLOSION;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParameters.EXPLOSION_RADIUS);
    }

    public boolean test(LootTableInfo loottableinfo) {
        Float ofloat = (Float) loottableinfo.getOptionalParameter(LootContextParameters.EXPLOSION_RADIUS);

        if (ofloat != null) {
            RandomSource randomsource = loottableinfo.getRandom();
            float f = 1.0F / ofloat;

            return randomsource.nextFloat() <= f;
        } else {
            return true;
        }
    }

    public static LootItemCondition.a survivesExplosion() {
        return () -> {
            return LootItemConditionSurvivesExplosion.INSTANCE;
        };
    }
}
