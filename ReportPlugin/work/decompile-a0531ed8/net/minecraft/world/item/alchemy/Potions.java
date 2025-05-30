package net.minecraft.world.item.alchemy;

import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public class Potions {

    public static final Holder<PotionRegistry> WATER = register("water", new PotionRegistry("water", new MobEffect[0]));
    public static final Holder<PotionRegistry> MUNDANE = register("mundane", new PotionRegistry("mundane", new MobEffect[0]));
    public static final Holder<PotionRegistry> THICK = register("thick", new PotionRegistry("thick", new MobEffect[0]));
    public static final Holder<PotionRegistry> AWKWARD = register("awkward", new PotionRegistry("awkward", new MobEffect[0]));
    public static final Holder<PotionRegistry> NIGHT_VISION = register("night_vision", new PotionRegistry("night_vision", new MobEffect[]{new MobEffect(MobEffects.NIGHT_VISION, 3600)}));
    public static final Holder<PotionRegistry> LONG_NIGHT_VISION = register("long_night_vision", new PotionRegistry("night_vision", new MobEffect[]{new MobEffect(MobEffects.NIGHT_VISION, 9600)}));
    public static final Holder<PotionRegistry> INVISIBILITY = register("invisibility", new PotionRegistry("invisibility", new MobEffect[]{new MobEffect(MobEffects.INVISIBILITY, 3600)}));
    public static final Holder<PotionRegistry> LONG_INVISIBILITY = register("long_invisibility", new PotionRegistry("invisibility", new MobEffect[]{new MobEffect(MobEffects.INVISIBILITY, 9600)}));
    public static final Holder<PotionRegistry> LEAPING = register("leaping", new PotionRegistry("leaping", new MobEffect[]{new MobEffect(MobEffects.JUMP, 3600)}));
    public static final Holder<PotionRegistry> LONG_LEAPING = register("long_leaping", new PotionRegistry("leaping", new MobEffect[]{new MobEffect(MobEffects.JUMP, 9600)}));
    public static final Holder<PotionRegistry> STRONG_LEAPING = register("strong_leaping", new PotionRegistry("leaping", new MobEffect[]{new MobEffect(MobEffects.JUMP, 1800, 1)}));
    public static final Holder<PotionRegistry> FIRE_RESISTANCE = register("fire_resistance", new PotionRegistry("fire_resistance", new MobEffect[]{new MobEffect(MobEffects.FIRE_RESISTANCE, 3600)}));
    public static final Holder<PotionRegistry> LONG_FIRE_RESISTANCE = register("long_fire_resistance", new PotionRegistry("fire_resistance", new MobEffect[]{new MobEffect(MobEffects.FIRE_RESISTANCE, 9600)}));
    public static final Holder<PotionRegistry> SWIFTNESS = register("swiftness", new PotionRegistry("swiftness", new MobEffect[]{new MobEffect(MobEffects.MOVEMENT_SPEED, 3600)}));
    public static final Holder<PotionRegistry> LONG_SWIFTNESS = register("long_swiftness", new PotionRegistry("swiftness", new MobEffect[]{new MobEffect(MobEffects.MOVEMENT_SPEED, 9600)}));
    public static final Holder<PotionRegistry> STRONG_SWIFTNESS = register("strong_swiftness", new PotionRegistry("swiftness", new MobEffect[]{new MobEffect(MobEffects.MOVEMENT_SPEED, 1800, 1)}));
    public static final Holder<PotionRegistry> SLOWNESS = register("slowness", new PotionRegistry("slowness", new MobEffect[]{new MobEffect(MobEffects.MOVEMENT_SLOWDOWN, 1800)}));
    public static final Holder<PotionRegistry> LONG_SLOWNESS = register("long_slowness", new PotionRegistry("slowness", new MobEffect[]{new MobEffect(MobEffects.MOVEMENT_SLOWDOWN, 4800)}));
    public static final Holder<PotionRegistry> STRONG_SLOWNESS = register("strong_slowness", new PotionRegistry("slowness", new MobEffect[]{new MobEffect(MobEffects.MOVEMENT_SLOWDOWN, 400, 3)}));
    public static final Holder<PotionRegistry> TURTLE_MASTER = register("turtle_master", new PotionRegistry("turtle_master", new MobEffect[]{new MobEffect(MobEffects.MOVEMENT_SLOWDOWN, 400, 3), new MobEffect(MobEffects.DAMAGE_RESISTANCE, 400, 2)}));
    public static final Holder<PotionRegistry> LONG_TURTLE_MASTER = register("long_turtle_master", new PotionRegistry("turtle_master", new MobEffect[]{new MobEffect(MobEffects.MOVEMENT_SLOWDOWN, 800, 3), new MobEffect(MobEffects.DAMAGE_RESISTANCE, 800, 2)}));
    public static final Holder<PotionRegistry> STRONG_TURTLE_MASTER = register("strong_turtle_master", new PotionRegistry("turtle_master", new MobEffect[]{new MobEffect(MobEffects.MOVEMENT_SLOWDOWN, 400, 5), new MobEffect(MobEffects.DAMAGE_RESISTANCE, 400, 3)}));
    public static final Holder<PotionRegistry> WATER_BREATHING = register("water_breathing", new PotionRegistry("water_breathing", new MobEffect[]{new MobEffect(MobEffects.WATER_BREATHING, 3600)}));
    public static final Holder<PotionRegistry> LONG_WATER_BREATHING = register("long_water_breathing", new PotionRegistry("water_breathing", new MobEffect[]{new MobEffect(MobEffects.WATER_BREATHING, 9600)}));
    public static final Holder<PotionRegistry> HEALING = register("healing", new PotionRegistry("healing", new MobEffect[]{new MobEffect(MobEffects.HEAL, 1)}));
    public static final Holder<PotionRegistry> STRONG_HEALING = register("strong_healing", new PotionRegistry("healing", new MobEffect[]{new MobEffect(MobEffects.HEAL, 1, 1)}));
    public static final Holder<PotionRegistry> HARMING = register("harming", new PotionRegistry("harming", new MobEffect[]{new MobEffect(MobEffects.HARM, 1)}));
    public static final Holder<PotionRegistry> STRONG_HARMING = register("strong_harming", new PotionRegistry("harming", new MobEffect[]{new MobEffect(MobEffects.HARM, 1, 1)}));
    public static final Holder<PotionRegistry> POISON = register("poison", new PotionRegistry("poison", new MobEffect[]{new MobEffect(MobEffects.POISON, 900)}));
    public static final Holder<PotionRegistry> LONG_POISON = register("long_poison", new PotionRegistry("poison", new MobEffect[]{new MobEffect(MobEffects.POISON, 1800)}));
    public static final Holder<PotionRegistry> STRONG_POISON = register("strong_poison", new PotionRegistry("poison", new MobEffect[]{new MobEffect(MobEffects.POISON, 432, 1)}));
    public static final Holder<PotionRegistry> REGENERATION = register("regeneration", new PotionRegistry("regeneration", new MobEffect[]{new MobEffect(MobEffects.REGENERATION, 900)}));
    public static final Holder<PotionRegistry> LONG_REGENERATION = register("long_regeneration", new PotionRegistry("regeneration", new MobEffect[]{new MobEffect(MobEffects.REGENERATION, 1800)}));
    public static final Holder<PotionRegistry> STRONG_REGENERATION = register("strong_regeneration", new PotionRegistry("regeneration", new MobEffect[]{new MobEffect(MobEffects.REGENERATION, 450, 1)}));
    public static final Holder<PotionRegistry> STRENGTH = register("strength", new PotionRegistry("strength", new MobEffect[]{new MobEffect(MobEffects.DAMAGE_BOOST, 3600)}));
    public static final Holder<PotionRegistry> LONG_STRENGTH = register("long_strength", new PotionRegistry("strength", new MobEffect[]{new MobEffect(MobEffects.DAMAGE_BOOST, 9600)}));
    public static final Holder<PotionRegistry> STRONG_STRENGTH = register("strong_strength", new PotionRegistry("strength", new MobEffect[]{new MobEffect(MobEffects.DAMAGE_BOOST, 1800, 1)}));
    public static final Holder<PotionRegistry> WEAKNESS = register("weakness", new PotionRegistry("weakness", new MobEffect[]{new MobEffect(MobEffects.WEAKNESS, 1800)}));
    public static final Holder<PotionRegistry> LONG_WEAKNESS = register("long_weakness", new PotionRegistry("weakness", new MobEffect[]{new MobEffect(MobEffects.WEAKNESS, 4800)}));
    public static final Holder<PotionRegistry> LUCK = register("luck", new PotionRegistry("luck", new MobEffect[]{new MobEffect(MobEffects.LUCK, 6000)}));
    public static final Holder<PotionRegistry> SLOW_FALLING = register("slow_falling", new PotionRegistry("slow_falling", new MobEffect[]{new MobEffect(MobEffects.SLOW_FALLING, 1800)}));
    public static final Holder<PotionRegistry> LONG_SLOW_FALLING = register("long_slow_falling", new PotionRegistry("slow_falling", new MobEffect[]{new MobEffect(MobEffects.SLOW_FALLING, 4800)}));
    public static final Holder<PotionRegistry> WIND_CHARGED = register("wind_charged", new PotionRegistry("wind_charged", new MobEffect[]{new MobEffect(MobEffects.WIND_CHARGED, 3600)}));
    public static final Holder<PotionRegistry> WEAVING = register("weaving", new PotionRegistry("weaving", new MobEffect[]{new MobEffect(MobEffects.WEAVING, 3600)}));
    public static final Holder<PotionRegistry> OOZING = register("oozing", new PotionRegistry("oozing", new MobEffect[]{new MobEffect(MobEffects.OOZING, 3600)}));
    public static final Holder<PotionRegistry> INFESTED = register("infested", new PotionRegistry("infested", new MobEffect[]{new MobEffect(MobEffects.INFESTED, 3600)}));

    public Potions() {}

    private static Holder<PotionRegistry> register(String s, PotionRegistry potionregistry) {
        return IRegistry.registerForHolder(BuiltInRegistries.POTION, MinecraftKey.withDefaultNamespace(s), potionregistry);
    }

    public static Holder<PotionRegistry> bootstrap(IRegistry<PotionRegistry> iregistry) {
        return Potions.WATER;
    }
}
