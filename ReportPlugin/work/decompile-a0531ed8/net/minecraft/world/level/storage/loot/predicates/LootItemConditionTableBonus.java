package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public record LootItemConditionTableBonus(Holder<Enchantment> enchantment, List<Float> values) implements LootItemCondition {

    public static final MapCodec<LootItemConditionTableBonus> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Enchantment.CODEC.fieldOf("enchantment").forGetter(LootItemConditionTableBonus::enchantment), ExtraCodecs.nonEmptyList(Codec.FLOAT.listOf()).fieldOf("chances").forGetter(LootItemConditionTableBonus::values)).apply(instance, LootItemConditionTableBonus::new);
    });

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.TABLE_BONUS;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParameters.TOOL);
    }

    public boolean test(LootTableInfo loottableinfo) {
        ItemStack itemstack = (ItemStack) loottableinfo.getOptionalParameter(LootContextParameters.TOOL);
        int i = itemstack != null ? EnchantmentManager.getItemEnchantmentLevel(this.enchantment, itemstack) : 0;
        float f = (Float) this.values.get(Math.min(i, this.values.size() - 1));

        return loottableinfo.getRandom().nextFloat() < f;
    }

    public static LootItemCondition.a bonusLevelFlatChance(Holder<Enchantment> holder, float... afloat) {
        List<Float> list = new ArrayList(afloat.length);
        float[] afloat1 = afloat;
        int i = afloat.length;

        for (int j = 0; j < i; ++j) {
            float f = afloat1[j];

            list.add(f);
        }

        return () -> {
            return new LootItemConditionTableBonus(holder, list);
        };
    }
}
