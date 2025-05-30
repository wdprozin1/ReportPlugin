package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class LootItemFunctionSetAttribute extends LootItemFunctionConditional {

    public static final MapCodec<LootItemFunctionSetAttribute> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(LootItemFunctionSetAttribute.b.CODEC.listOf().fieldOf("modifiers").forGetter((lootitemfunctionsetattribute) -> {
            return lootitemfunctionsetattribute.modifiers;
        }), Codec.BOOL.optionalFieldOf("replace", true).forGetter((lootitemfunctionsetattribute) -> {
            return lootitemfunctionsetattribute.replace;
        }))).apply(instance, LootItemFunctionSetAttribute::new);
    });
    private final List<LootItemFunctionSetAttribute.b> modifiers;
    private final boolean replace;

    LootItemFunctionSetAttribute(List<LootItemCondition> list, List<LootItemFunctionSetAttribute.b> list1, boolean flag) {
        super(list);
        this.modifiers = List.copyOf(list1);
        this.replace = flag;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionSetAttribute> getType() {
        return LootItemFunctions.SET_ATTRIBUTES;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return (Set) this.modifiers.stream().flatMap((lootitemfunctionsetattribute_b) -> {
            return lootitemfunctionsetattribute_b.amount.getReferencedContextParams().stream();
        }).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (this.replace) {
            itemstack.set(DataComponents.ATTRIBUTE_MODIFIERS, this.updateModifiers(loottableinfo, ItemAttributeModifiers.EMPTY));
        } else {
            itemstack.update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, (itemattributemodifiers) -> {
                return this.updateModifiers(loottableinfo, itemattributemodifiers);
            });
        }

        return itemstack;
    }

    private ItemAttributeModifiers updateModifiers(LootTableInfo loottableinfo, ItemAttributeModifiers itemattributemodifiers) {
        RandomSource randomsource = loottableinfo.getRandom();

        LootItemFunctionSetAttribute.b lootitemfunctionsetattribute_b;
        EquipmentSlotGroup equipmentslotgroup;

        for (Iterator iterator = this.modifiers.iterator(); iterator.hasNext(); itemattributemodifiers = itemattributemodifiers.withModifierAdded(lootitemfunctionsetattribute_b.attribute, new AttributeModifier(lootitemfunctionsetattribute_b.id, (double) lootitemfunctionsetattribute_b.amount.getFloat(loottableinfo), lootitemfunctionsetattribute_b.operation), equipmentslotgroup)) {
            lootitemfunctionsetattribute_b = (LootItemFunctionSetAttribute.b) iterator.next();
            equipmentslotgroup = (EquipmentSlotGroup) SystemUtils.getRandom(lootitemfunctionsetattribute_b.slots, randomsource);
        }

        return itemattributemodifiers;
    }

    public static LootItemFunctionSetAttribute.c modifier(MinecraftKey minecraftkey, Holder<AttributeBase> holder, AttributeModifier.Operation attributemodifier_operation, NumberProvider numberprovider) {
        return new LootItemFunctionSetAttribute.c(minecraftkey, holder, attributemodifier_operation, numberprovider);
    }

    public static LootItemFunctionSetAttribute.a setAttributes() {
        return new LootItemFunctionSetAttribute.a();
    }

    private static record b(MinecraftKey id, Holder<AttributeBase> attribute, AttributeModifier.Operation operation, NumberProvider amount, List<EquipmentSlotGroup> slots) {

        private static final Codec<List<EquipmentSlotGroup>> SLOTS_CODEC = ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(EquipmentSlotGroup.CODEC));
        public static final Codec<LootItemFunctionSetAttribute.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(MinecraftKey.CODEC.fieldOf("id").forGetter(LootItemFunctionSetAttribute.b::id), AttributeBase.CODEC.fieldOf("attribute").forGetter(LootItemFunctionSetAttribute.b::attribute), AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(LootItemFunctionSetAttribute.b::operation), NumberProviders.CODEC.fieldOf("amount").forGetter(LootItemFunctionSetAttribute.b::amount), LootItemFunctionSetAttribute.b.SLOTS_CODEC.fieldOf("slot").forGetter(LootItemFunctionSetAttribute.b::slots)).apply(instance, LootItemFunctionSetAttribute.b::new);
        });
    }

    public static class c {

        private final MinecraftKey id;
        private final Holder<AttributeBase> attribute;
        private final AttributeModifier.Operation operation;
        private final NumberProvider amount;
        private final Set<EquipmentSlotGroup> slots = EnumSet.noneOf(EquipmentSlotGroup.class);

        public c(MinecraftKey minecraftkey, Holder<AttributeBase> holder, AttributeModifier.Operation attributemodifier_operation, NumberProvider numberprovider) {
            this.id = minecraftkey;
            this.attribute = holder;
            this.operation = attributemodifier_operation;
            this.amount = numberprovider;
        }

        public LootItemFunctionSetAttribute.c forSlot(EquipmentSlotGroup equipmentslotgroup) {
            this.slots.add(equipmentslotgroup);
            return this;
        }

        public LootItemFunctionSetAttribute.b build() {
            return new LootItemFunctionSetAttribute.b(this.id, this.attribute, this.operation, this.amount, List.copyOf(this.slots));
        }
    }

    public static class a extends LootItemFunctionConditional.a<LootItemFunctionSetAttribute.a> {

        private final boolean replace;
        private final List<LootItemFunctionSetAttribute.b> modifiers;

        public a(boolean flag) {
            this.modifiers = Lists.newArrayList();
            this.replace = flag;
        }

        public a() {
            this(false);
        }

        @Override
        protected LootItemFunctionSetAttribute.a getThis() {
            return this;
        }

        public LootItemFunctionSetAttribute.a withModifier(LootItemFunctionSetAttribute.c lootitemfunctionsetattribute_c) {
            this.modifiers.add(lootitemfunctionsetattribute_c.build());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new LootItemFunctionSetAttribute(this.getConditions(), this.modifiers, this.replace);
        }
    }
}
