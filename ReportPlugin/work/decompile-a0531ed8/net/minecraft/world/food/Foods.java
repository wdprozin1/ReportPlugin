package net.minecraft.world.food;

public class Foods {

    public static final FoodInfo APPLE = (new FoodInfo.a()).nutrition(4).saturationModifier(0.3F).build();
    public static final FoodInfo BAKED_POTATO = (new FoodInfo.a()).nutrition(5).saturationModifier(0.6F).build();
    public static final FoodInfo BEEF = (new FoodInfo.a()).nutrition(3).saturationModifier(0.3F).build();
    public static final FoodInfo BEETROOT = (new FoodInfo.a()).nutrition(1).saturationModifier(0.6F).build();
    public static final FoodInfo BEETROOT_SOUP = stew(6).build();
    public static final FoodInfo BREAD = (new FoodInfo.a()).nutrition(5).saturationModifier(0.6F).build();
    public static final FoodInfo CARROT = (new FoodInfo.a()).nutrition(3).saturationModifier(0.6F).build();
    public static final FoodInfo CHICKEN = (new FoodInfo.a()).nutrition(2).saturationModifier(0.3F).build();
    public static final FoodInfo CHORUS_FRUIT = (new FoodInfo.a()).nutrition(4).saturationModifier(0.3F).alwaysEdible().build();
    public static final FoodInfo COD = (new FoodInfo.a()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodInfo COOKED_BEEF = (new FoodInfo.a()).nutrition(8).saturationModifier(0.8F).build();
    public static final FoodInfo COOKED_CHICKEN = (new FoodInfo.a()).nutrition(6).saturationModifier(0.6F).build();
    public static final FoodInfo COOKED_COD = (new FoodInfo.a()).nutrition(5).saturationModifier(0.6F).build();
    public static final FoodInfo COOKED_MUTTON = (new FoodInfo.a()).nutrition(6).saturationModifier(0.8F).build();
    public static final FoodInfo COOKED_PORKCHOP = (new FoodInfo.a()).nutrition(8).saturationModifier(0.8F).build();
    public static final FoodInfo COOKED_RABBIT = (new FoodInfo.a()).nutrition(5).saturationModifier(0.6F).build();
    public static final FoodInfo COOKED_SALMON = (new FoodInfo.a()).nutrition(6).saturationModifier(0.8F).build();
    public static final FoodInfo COOKIE = (new FoodInfo.a()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodInfo DRIED_KELP = (new FoodInfo.a()).nutrition(1).saturationModifier(0.3F).build();
    public static final FoodInfo ENCHANTED_GOLDEN_APPLE = (new FoodInfo.a()).nutrition(4).saturationModifier(1.2F).alwaysEdible().build();
    public static final FoodInfo GOLDEN_APPLE = (new FoodInfo.a()).nutrition(4).saturationModifier(1.2F).alwaysEdible().build();
    public static final FoodInfo GOLDEN_CARROT = (new FoodInfo.a()).nutrition(6).saturationModifier(1.2F).build();
    public static final FoodInfo HONEY_BOTTLE = (new FoodInfo.a()).nutrition(6).saturationModifier(0.1F).alwaysEdible().build();
    public static final FoodInfo MELON_SLICE = (new FoodInfo.a()).nutrition(2).saturationModifier(0.3F).build();
    public static final FoodInfo MUSHROOM_STEW = stew(6).build();
    public static final FoodInfo MUTTON = (new FoodInfo.a()).nutrition(2).saturationModifier(0.3F).build();
    public static final FoodInfo POISONOUS_POTATO = (new FoodInfo.a()).nutrition(2).saturationModifier(0.3F).build();
    public static final FoodInfo PORKCHOP = (new FoodInfo.a()).nutrition(3).saturationModifier(0.3F).build();
    public static final FoodInfo POTATO = (new FoodInfo.a()).nutrition(1).saturationModifier(0.3F).build();
    public static final FoodInfo PUFFERFISH = (new FoodInfo.a()).nutrition(1).saturationModifier(0.1F).build();
    public static final FoodInfo PUMPKIN_PIE = (new FoodInfo.a()).nutrition(8).saturationModifier(0.3F).build();
    public static final FoodInfo RABBIT = (new FoodInfo.a()).nutrition(3).saturationModifier(0.3F).build();
    public static final FoodInfo RABBIT_STEW = stew(10).build();
    public static final FoodInfo ROTTEN_FLESH = (new FoodInfo.a()).nutrition(4).saturationModifier(0.1F).build();
    public static final FoodInfo SALMON = (new FoodInfo.a()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodInfo SPIDER_EYE = (new FoodInfo.a()).nutrition(2).saturationModifier(0.8F).build();
    public static final FoodInfo SUSPICIOUS_STEW = stew(6).alwaysEdible().build();
    public static final FoodInfo SWEET_BERRIES = (new FoodInfo.a()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodInfo GLOW_BERRIES = (new FoodInfo.a()).nutrition(2).saturationModifier(0.1F).build();
    public static final FoodInfo TROPICAL_FISH = (new FoodInfo.a()).nutrition(1).saturationModifier(0.1F).build();

    public Foods() {}

    private static FoodInfo.a stew(int i) {
        return (new FoodInfo.a()).nutrition(i).saturationModifier(0.6F);
    }
}
