package net.minecraft.core.cauldron;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsFluid;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemLiquidUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockShulkerBox;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public interface CauldronInteraction {

    Map<String, CauldronInteraction.a> INTERACTIONS = new Object2ObjectArrayMap();
    Codec<CauldronInteraction.a> CODEC;
    CauldronInteraction.a EMPTY;
    CauldronInteraction.a WATER;
    CauldronInteraction.a LAVA;
    CauldronInteraction.a POWDER_SNOW;

    static CauldronInteraction.a newInteractionMap(String s) {
        Object2ObjectOpenHashMap<Item, CauldronInteraction> object2objectopenhashmap = new Object2ObjectOpenHashMap();

        object2objectopenhashmap.defaultReturnValue((iblockdata, world, blockposition, entityhuman, enumhand, itemstack) -> {
            return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
        });
        CauldronInteraction.a cauldroninteraction_a = new CauldronInteraction.a(s, object2objectopenhashmap);

        CauldronInteraction.INTERACTIONS.put(s, cauldroninteraction_a);
        return cauldroninteraction_a;
    }

    EnumInteractionResult interact(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack);

    static void bootStrap() {
        Map<Item, CauldronInteraction> map = CauldronInteraction.EMPTY.map();

        addDefaultInteractions(map);
        map.put(Items.POTION, (iblockdata, world, blockposition, entityhuman, enumhand, itemstack) -> {
            PotionContents potioncontents = (PotionContents) itemstack.get(DataComponents.POTION_CONTENTS);

            if (potioncontents != null && potioncontents.is(Potions.WATER)) {
                if (!world.isClientSide) {
                    Item item = itemstack.getItem();

                    entityhuman.setItemInHand(enumhand, ItemLiquidUtil.createFilledResult(itemstack, entityhuman, new ItemStack(Items.GLASS_BOTTLE)));
                    entityhuman.awardStat(StatisticList.USE_CAULDRON);
                    entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
                    world.setBlockAndUpdate(blockposition, Blocks.WATER_CAULDRON.defaultBlockState());
                    world.playSound((EntityHuman) null, blockposition, SoundEffects.BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.gameEvent((Entity) null, (Holder) GameEvent.FLUID_PLACE, blockposition);
                }

                return EnumInteractionResult.SUCCESS;
            } else {
                return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
            }
        });
        Map<Item, CauldronInteraction> map1 = CauldronInteraction.WATER.map();

        addDefaultInteractions(map1);
        map1.put(Items.BUCKET, (iblockdata, world, blockposition, entityhuman, enumhand, itemstack) -> {
            return fillBucket(iblockdata, world, blockposition, entityhuman, enumhand, itemstack, new ItemStack(Items.WATER_BUCKET), (iblockdata1) -> {
                return (Integer) iblockdata1.getValue(LayeredCauldronBlock.LEVEL) == 3;
            }, SoundEffects.BUCKET_FILL);
        });
        map1.put(Items.GLASS_BOTTLE, (iblockdata, world, blockposition, entityhuman, enumhand, itemstack) -> {
            if (!world.isClientSide) {
                Item item = itemstack.getItem();

                entityhuman.setItemInHand(enumhand, ItemLiquidUtil.createFilledResult(itemstack, entityhuman, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
                entityhuman.awardStat(StatisticList.USE_CAULDRON);
                entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
                LayeredCauldronBlock.lowerFillLevel(iblockdata, world, blockposition);
                world.playSound((EntityHuman) null, blockposition, SoundEffects.BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.gameEvent((Entity) null, (Holder) GameEvent.FLUID_PICKUP, blockposition);
            }

            return EnumInteractionResult.SUCCESS;
        });
        map1.put(Items.POTION, (iblockdata, world, blockposition, entityhuman, enumhand, itemstack) -> {
            if ((Integer) iblockdata.getValue(LayeredCauldronBlock.LEVEL) == 3) {
                return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
            } else {
                PotionContents potioncontents = (PotionContents) itemstack.get(DataComponents.POTION_CONTENTS);

                if (potioncontents != null && potioncontents.is(Potions.WATER)) {
                    if (!world.isClientSide) {
                        entityhuman.setItemInHand(enumhand, ItemLiquidUtil.createFilledResult(itemstack, entityhuman, new ItemStack(Items.GLASS_BOTTLE)));
                        entityhuman.awardStat(StatisticList.USE_CAULDRON);
                        entityhuman.awardStat(StatisticList.ITEM_USED.get(itemstack.getItem()));
                        world.setBlockAndUpdate(blockposition, (IBlockData) iblockdata.cycle(LayeredCauldronBlock.LEVEL));
                        world.playSound((EntityHuman) null, blockposition, SoundEffects.BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        world.gameEvent((Entity) null, (Holder) GameEvent.FLUID_PLACE, blockposition);
                    }

                    return EnumInteractionResult.SUCCESS;
                } else {
                    return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
                }
            }
        });
        map1.put(Items.LEATHER_BOOTS, CauldronInteraction::dyedItemIteration);
        map1.put(Items.LEATHER_LEGGINGS, CauldronInteraction::dyedItemIteration);
        map1.put(Items.LEATHER_CHESTPLATE, CauldronInteraction::dyedItemIteration);
        map1.put(Items.LEATHER_HELMET, CauldronInteraction::dyedItemIteration);
        map1.put(Items.LEATHER_HORSE_ARMOR, CauldronInteraction::dyedItemIteration);
        map1.put(Items.WOLF_ARMOR, CauldronInteraction::dyedItemIteration);
        map1.put(Items.WHITE_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.GRAY_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.BLACK_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.BLUE_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.BROWN_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.CYAN_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.GREEN_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.LIGHT_BLUE_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.LIGHT_GRAY_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.LIME_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.MAGENTA_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.ORANGE_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.PINK_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.PURPLE_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.RED_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.YELLOW_BANNER, CauldronInteraction::bannerInteraction);
        map1.put(Items.WHITE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.GRAY_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.BLACK_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.BLUE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.BROWN_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.CYAN_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.GREEN_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.LIGHT_BLUE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.LIGHT_GRAY_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.LIME_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.MAGENTA_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.ORANGE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.PINK_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.PURPLE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.RED_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map1.put(Items.YELLOW_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        Map<Item, CauldronInteraction> map2 = CauldronInteraction.LAVA.map();

        map2.put(Items.BUCKET, (iblockdata, world, blockposition, entityhuman, enumhand, itemstack) -> {
            return fillBucket(iblockdata, world, blockposition, entityhuman, enumhand, itemstack, new ItemStack(Items.LAVA_BUCKET), (iblockdata1) -> {
                return true;
            }, SoundEffects.BUCKET_FILL_LAVA);
        });
        addDefaultInteractions(map2);
        Map<Item, CauldronInteraction> map3 = CauldronInteraction.POWDER_SNOW.map();

        map3.put(Items.BUCKET, (iblockdata, world, blockposition, entityhuman, enumhand, itemstack) -> {
            return fillBucket(iblockdata, world, blockposition, entityhuman, enumhand, itemstack, new ItemStack(Items.POWDER_SNOW_BUCKET), (iblockdata1) -> {
                return (Integer) iblockdata1.getValue(LayeredCauldronBlock.LEVEL) == 3;
            }, SoundEffects.BUCKET_FILL_POWDER_SNOW);
        });
        addDefaultInteractions(map3);
    }

    static void addDefaultInteractions(Map<Item, CauldronInteraction> map) {
        map.put(Items.LAVA_BUCKET, CauldronInteraction::fillLavaInteraction);
        map.put(Items.WATER_BUCKET, CauldronInteraction::fillWaterInteraction);
        map.put(Items.POWDER_SNOW_BUCKET, CauldronInteraction::fillPowderSnowInteraction);
    }

    static EnumInteractionResult fillBucket(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack, ItemStack itemstack1, Predicate<IBlockData> predicate, SoundEffect soundeffect) {
        if (!predicate.test(iblockdata)) {
            return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
        } else {
            if (!world.isClientSide) {
                Item item = itemstack.getItem();

                entityhuman.setItemInHand(enumhand, ItemLiquidUtil.createFilledResult(itemstack, entityhuman, itemstack1));
                entityhuman.awardStat(StatisticList.USE_CAULDRON);
                entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
                world.setBlockAndUpdate(blockposition, Blocks.CAULDRON.defaultBlockState());
                world.playSound((EntityHuman) null, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.gameEvent((Entity) null, (Holder) GameEvent.FLUID_PICKUP, blockposition);
            }

            return EnumInteractionResult.SUCCESS;
        }
    }

    static EnumInteractionResult emptyBucket(World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack, IBlockData iblockdata, SoundEffect soundeffect) {
        if (!world.isClientSide) {
            Item item = itemstack.getItem();

            entityhuman.setItemInHand(enumhand, ItemLiquidUtil.createFilledResult(itemstack, entityhuman, new ItemStack(Items.BUCKET)));
            entityhuman.awardStat(StatisticList.FILL_CAULDRON);
            entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
            world.setBlockAndUpdate(blockposition, iblockdata);
            world.playSound((EntityHuman) null, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.gameEvent((Entity) null, (Holder) GameEvent.FLUID_PLACE, blockposition);
        }

        return EnumInteractionResult.SUCCESS;
    }

    private static EnumInteractionResult fillWaterInteraction(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack) {
        return emptyBucket(world, blockposition, entityhuman, enumhand, itemstack, (IBlockData) Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEffects.BUCKET_EMPTY);
    }

    private static EnumInteractionResult fillLavaInteraction(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack) {
        return (EnumInteractionResult) (isUnderWater(world, blockposition) ? EnumInteractionResult.CONSUME : emptyBucket(world, blockposition, entityhuman, enumhand, itemstack, Blocks.LAVA_CAULDRON.defaultBlockState(), SoundEffects.BUCKET_EMPTY_LAVA));
    }

    private static EnumInteractionResult fillPowderSnowInteraction(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack) {
        return (EnumInteractionResult) (isUnderWater(world, blockposition) ? EnumInteractionResult.CONSUME : emptyBucket(world, blockposition, entityhuman, enumhand, itemstack, (IBlockData) Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEffects.BUCKET_EMPTY_POWDER_SNOW));
    }

    private static EnumInteractionResult shulkerBoxInteraction(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack) {
        Block block = Block.byItem(itemstack.getItem());

        if (!(block instanceof BlockShulkerBox)) {
            return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
        } else {
            if (!world.isClientSide) {
                ItemStack itemstack1 = itemstack.transmuteCopy(Blocks.SHULKER_BOX, 1);

                entityhuman.setItemInHand(enumhand, ItemLiquidUtil.createFilledResult(itemstack, entityhuman, itemstack1, false));
                entityhuman.awardStat(StatisticList.CLEAN_SHULKER_BOX);
                LayeredCauldronBlock.lowerFillLevel(iblockdata, world, blockposition);
            }

            return EnumInteractionResult.SUCCESS;
        }
    }

    private static EnumInteractionResult bannerInteraction(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack) {
        BannerPatternLayers bannerpatternlayers = (BannerPatternLayers) itemstack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);

        if (bannerpatternlayers.layers().isEmpty()) {
            return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
        } else {
            if (!world.isClientSide) {
                ItemStack itemstack1 = itemstack.copyWithCount(1);

                itemstack1.set(DataComponents.BANNER_PATTERNS, bannerpatternlayers.removeLast());
                entityhuman.setItemInHand(enumhand, ItemLiquidUtil.createFilledResult(itemstack, entityhuman, itemstack1, false));
                entityhuman.awardStat(StatisticList.CLEAN_BANNER);
                LayeredCauldronBlock.lowerFillLevel(iblockdata, world, blockposition);
            }

            return EnumInteractionResult.SUCCESS;
        }
    }

    private static EnumInteractionResult dyedItemIteration(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, ItemStack itemstack) {
        if (!itemstack.is(TagsItem.DYEABLE)) {
            return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
        } else if (!itemstack.has(DataComponents.DYED_COLOR)) {
            return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
        } else {
            if (!world.isClientSide) {
                itemstack.remove(DataComponents.DYED_COLOR);
                entityhuman.awardStat(StatisticList.CLEAN_ARMOR);
                LayeredCauldronBlock.lowerFillLevel(iblockdata, world, blockposition);
            }

            return EnumInteractionResult.SUCCESS;
        }
    }

    private static boolean isUnderWater(World world, BlockPosition blockposition) {
        Fluid fluid = world.getFluidState(blockposition.above());

        return fluid.is(TagsFluid.WATER);
    }

    static {
        Function function = CauldronInteraction.a::name;
        Map map = CauldronInteraction.INTERACTIONS;

        Objects.requireNonNull(map);
        CODEC = Codec.stringResolver(function, map::get);
        EMPTY = newInteractionMap("empty");
        WATER = newInteractionMap("water");
        LAVA = newInteractionMap("lava");
        POWDER_SNOW = newInteractionMap("powder_snow");
    }

    public static record a(String name, Map<Item, CauldronInteraction> map) {

    }
}
