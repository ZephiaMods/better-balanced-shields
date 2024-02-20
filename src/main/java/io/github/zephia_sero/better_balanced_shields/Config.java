package io.github.zephia_sero.better_balanced_shields;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = BetterBalancedShields.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder()
            .comment("You may notice none of these options have ranges, so changing things can get wacky if you don't stick to what the comments suggest. Have fun!");

    private static final ForgeConfigSpec.BooleanValue BLOCKABLE_POTIONS = BUILDER
            .comment("Potion stuff!")
            .comment("Whether to block potions, without REFLECTABLE_POTIONS too, this means the potion will just disappear when it hits a shield")
            .define("BLOCKABLE_POTIONS", true);
    private static final ForgeConfigSpec.BooleanValue REFLECTABLE_POTIONS = BUILDER
            .comment("Whether to reflect potions (implies BLOCKABLE_POTIONS)")
            .define("REFLECTABLE_POTIONS", true);
    private static final ForgeConfigSpec.ConfigValue<Double> POTION_REFLECT_MULTIPLIER = BUILDER
            .comment("Velocity multiplier to reflected potions")
            .comment("Positive numbers reflect away from you, recommend keeping the number at its default, or higher")
            .define("POTION_REFLECT_MULTIPLIER", 0.75);
    private static final ForgeConfigSpec.ConfigValue<Double> POTION_DOT_MINIMUM = BUILDER
            .comment("Minimum value of the dot product between the player's facing direction and the inverse of the projectile's normalized motion in order to count as a shield hit")
            .comment("In other words, -1 means you reflect arrows completely opposite which direction you're looking, 1 means you have to be looking the exact same direction")
            .comment("Values below -1 behave identically to -1, and values above 1 behave identically to 1")
            .comment("Note, this is the lower part of the range, ideally this should be less than 1")
            .comment("With the default POTION_DOT_MAXIMUM, think of this as bigger number = harder to block")
            .define("POTION_DOT_MINIMUM", 0.25);
    private static final ForgeConfigSpec.ConfigValue<Double> POTION_DOT_MAXIMUM = BUILDER
            .comment("For the nerd stuff, check out POTION_DOT_MINIMUM")
            .comment("Note, this is the maximum part of the range, ideally this should just be 1")
            .define("POTION_DOT_MAXIMUM", 1.0);
    private static final ForgeConfigSpec.ConfigValue<Double> POTION_SHIELD_DAMAGE = BUILDER
            .comment("Damage dealt to shield when reflecting/blocking a potion")
            .define("POTION_SHIELD_DAMAGE", 2.0);
    private static final ForgeConfigSpec.BooleanValue POTION_AUTOAIM = BUILDER
            .comment("Just multiply potion velocity, don't redirect to player's facing direction")
            .define("POTION_AUTOAIM", false);
    private static final ForgeConfigSpec.BooleanValue VANILLA_POTIONS = BUILDER
            .comment("Disables this mod's logic for calculating and reflecting potions, overrides all the above potion settings")
            .define("VANILLA_POTIONS", false);

    private static final ForgeConfigSpec.BooleanValue BLOCKABLE_ARROWS = BUILDER
            .comment("Arrow stuff!")
            .comment("Whether to block arrows, without REFLECTABLE_ARROWS, this means the arrow will disappear when it hits a shield")
            .define("BLOCKABLE_ARROWS", true);
    private static final ForgeConfigSpec.BooleanValue REFLECTABLE_ARROWS = BUILDER
            .comment("Whether to reflect arrows (implies BLOCKABLE_ARROWS)")
            .define("REFLECTABLE_ARROWS", true);
    private static final ForgeConfigSpec.ConfigValue<Double> ARROW_REFLECT_MULTIPLIER = BUILDER
            .comment("Velocity multiplier to reflected arrow")
            .comment("Positive numbers reflect away from you")
            .define("ARROW_REFLECT_MULTIPLIER", 0.25);
    private static final ForgeConfigSpec.ConfigValue<Double> ARROW_DOT_MINIMUM = BUILDER
            .comment("See POTION_DOT_MINIMUM")
            .define("ARROW_DOT_MINIMUM", 0.125);
    private static final ForgeConfigSpec.ConfigValue<Double> ARROW_DOT_MAXIMUM = BUILDER
            .comment("See POTION_DOT_MAXIMUM")
            .define("ARROW_DOT_MAXIMUM", 1.0);
    private static final ForgeConfigSpec.ConfigValue<Double> ARROW_SHIELD_DAMAGE = BUILDER
            .comment("Damage dealt to shield when reflecting/blocking an arrow")
            .define("ARROW_SHIELD_DAMAGE", 2.0);
    private static final ForgeConfigSpec.BooleanValue ARROW_AUTOAIM = BUILDER
            .comment("Just multiply arrow velocity, don't redirect to player's facing direction")
            .define("ARROW_AUTOAIM", false);
    private static final ForgeConfigSpec.BooleanValue ARROW_REFLECT_CRITS = BUILDER
            .comment("Arrow reflect will crit or not, simple as that")
            .define("ARROW_REFLECT_CRITS", true);
    private static final ForgeConfigSpec.BooleanValue VANILLA_ARROWS = BUILDER
            .comment("Disables this mod's logic for calculating and reflecting arrows, overrides all the above arrow settings")
            .define("VANILLA_ARROWS", false);

    private static final ForgeConfigSpec.ConfigValue<Double> MELEE_DAMAGE_MULTIPLIER = BUILDER
            .comment("Melee stuff!")
            .comment("Percentage (default of 0.75 = 75%) of damage taken if shield is up. If this number is negative, you are healed by the negated percentage.")
            .define("MELEE_DAMAGE_MULTIPLIER", 0.75);
    private static final ForgeConfigSpec.ConfigValue<Double> MELEE_DAMAGE_REFLECTION = BUILDER
            .comment("Percentage (default of 0.00 = 0%) of damage reflected (think thorns enchant) if shield is up. If this number is negative, the assailant is healed by the negated percentage.")
            .define("MELEE_DAMAGE_REFLECTION", 0.0);
    private static final ForgeConfigSpec.BooleanValue MELEE_SCALED_SHIELD_DAMAGE = BUILDER
            .comment("Whether or not MELEE_SHIELD_DAMAGE is a percentage of damage taken applied to a shield (true), or a constant (false)")
            .define("MELEE_SCALED_SHIELD_DAMAGE", true);
    private static final ForgeConfigSpec.ConfigValue<Double> MELEE_SHIELD_DAMAGE = BUILDER
            .comment("Read above.")
            .comment("If it's true, damage before any modifiers is multiplied by this number and applied to the shield")
            .comment("If it's false, just this number is applied")
            .define("MELEE_SHIELD_DAMAGE", 0.5);
    private static final ForgeConfigSpec.BooleanValue VANILLA_MELEE  = BUILDER
            .comment("Nullifies above melee settings")
            .define("VANILLA_MELEE", false);

    private static final ForgeConfigSpec.ConfigValue<Double> SHIELD_BLOCK_MIN_VOLUME = BUILDER
            .comment("Minimum volume when this mod plays the SHIELD_BLOCK sound effect")
            .define("SHIELD_BLOCK_MIN_VOLUME", 1.0);
    private static final ForgeConfigSpec.ConfigValue<Double> SHIELD_BLOCK_MAX_VOLUME = BUILDER
            .comment("Maximum volume when this mod plays the SHIELD_BLOCK sound effect")
            .define("SHIELD_BLOCK_MAX_VOLUME", 1.0);
    private static final ForgeConfigSpec.ConfigValue<Double> SHIELD_BLOCK_MIN_PITCH = BUILDER
            .comment("Minimum pitch when this mod plays the SHIELD_BLOCK sound effect")
            .define("SHIELD_BLOCK_MIN_PITCH", 0.8);
    private static final ForgeConfigSpec.ConfigValue<Double> SHIELD_BLOCK_MAX_PITCH = BUILDER
            .comment("Maximum pitch when this mod plays the SHIELD_BLOCK sound effect")
            .define("SHIELD_BLOCK_MAX_PITCH", 1.2);

    private static final ForgeConfigSpec.ConfigValue<Double> SHIELD_BREAK_MIN_VOLUME = BUILDER
            .comment("Minimum volume when this mod plays the SHIELD_BREAK sound effect")
            .define("SHIELD_BREAK_MIN_VOLUME", 1.0);
    private static final ForgeConfigSpec.ConfigValue<Double> SHIELD_BREAK_MAX_VOLUME = BUILDER
            .comment("Maximum volume when this mod plays the SHIELD_BREAK sound effect")
            .define("SHIELD_BREAK_MAX_VOLUME", 1.0);
    private static final ForgeConfigSpec.ConfigValue<Double> SHIELD_BREAK_MIN_PITCH = BUILDER
            .comment("Minimum pitch when this mod plays the SHIELD_BREAK sound effect")
            .define("SHIELD_BREAK_MIN_PITCH", 0.8);
    private static final ForgeConfigSpec.ConfigValue<Double> SHIELD_BREAK_MAX_PITCH = BUILDER
            .comment("Maximum pitch when this mod plays the SHIELD_BREAK sound effect")
            .define("SHIELD_BREAK_MAX_PITCH", 1.2);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean blockablePotions, reflectablePotions, blockableArrows, reflectableArrows;
    public static double potionShieldDamage, arrowShieldDamage;
    public static boolean potionAutoaim, arrowAutoaim, arrowReflectCrits;
    public static boolean vanillaPotions, vanillaArrows, vanillaMelee;

    public static double potionDotMinimum, potionDotMaximum, potionReflectMultiplier;
    public static double arrowDotMinimum, arrowDotMaximum, arrowReflectMultiplier;
    public static boolean meleeScaledShieldDamage;
    public static double meleeDamageMultiplier, meleeDamageReflection, meleeShieldDamage;

    public static double shieldBlockMinVolume, shieldBlockMaxVolume, shieldBlockMinPitch, shieldBlockMaxPitch;
    public static double shieldBreakMinVolume, shieldBreakMaxVolume, shieldBreakMinPitch, shieldBreakMaxPitch;
    public static double shieldBlockVolumeDiff, shieldBlockPitchDiff, shieldBreakVolumeDiff, shieldBreakPitchDiff;
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        vanillaPotions = VANILLA_POTIONS.get();
        reflectablePotions = (!vanillaPotions) && REFLECTABLE_POTIONS.get();
        blockablePotions = (!vanillaPotions) && (reflectablePotions || BLOCKABLE_POTIONS.get());
        if (!vanillaPotions) {
            potionReflectMultiplier = POTION_REFLECT_MULTIPLIER.get();
            potionDotMinimum = POTION_DOT_MINIMUM.get();
            potionDotMaximum = POTION_DOT_MAXIMUM.get();
            potionShieldDamage = POTION_SHIELD_DAMAGE.get();
            potionAutoaim = POTION_AUTOAIM.get();
        }

        vanillaArrows = VANILLA_ARROWS.get();
        reflectableArrows = (!vanillaArrows) && REFLECTABLE_ARROWS.get();
        blockableArrows = (!vanillaArrows) && (reflectableArrows || BLOCKABLE_ARROWS.get());
        if (!vanillaArrows) {
            arrowReflectMultiplier = ARROW_REFLECT_MULTIPLIER.get();
            arrowDotMinimum = ARROW_DOT_MINIMUM.get();
            arrowDotMaximum = ARROW_DOT_MAXIMUM.get();
            arrowShieldDamage = ARROW_SHIELD_DAMAGE.get();
            arrowAutoaim = ARROW_AUTOAIM.get();
            arrowReflectCrits = ARROW_REFLECT_CRITS.get();
        }

        vanillaMelee = VANILLA_MELEE.get();
        if (!vanillaMelee) {
            meleeDamageMultiplier = MELEE_DAMAGE_MULTIPLIER.get();
            meleeDamageReflection = MELEE_DAMAGE_REFLECTION.get();
            meleeScaledShieldDamage = MELEE_SCALED_SHIELD_DAMAGE.get();
            meleeShieldDamage = MELEE_SHIELD_DAMAGE.get();
        }

        shieldBlockMinVolume = SHIELD_BLOCK_MIN_VOLUME.get();
        shieldBlockMaxVolume = SHIELD_BLOCK_MAX_VOLUME.get();
        shieldBlockMinPitch = SHIELD_BLOCK_MIN_PITCH.get();
        shieldBlockMaxPitch = SHIELD_BLOCK_MAX_PITCH.get();

        shieldBreakMinVolume = SHIELD_BREAK_MIN_VOLUME.get();
        shieldBreakMaxVolume = SHIELD_BREAK_MAX_VOLUME.get();
        shieldBreakMinPitch = SHIELD_BREAK_MIN_PITCH.get();
        shieldBreakMaxPitch = SHIELD_BREAK_MAX_PITCH.get();

        shieldBlockVolumeDiff = shieldBlockMaxVolume - shieldBlockMinVolume;
        shieldBlockPitchDiff = shieldBlockMaxPitch - shieldBlockMinPitch;
        shieldBreakVolumeDiff = shieldBreakMaxVolume - shieldBreakMinVolume;
        shieldBreakPitchDiff = shieldBreakMaxPitch - shieldBreakMinPitch;
    }

    protected static boolean projectile_blocked(Projectile proj, double dot)
    {
        if (proj instanceof Arrow)
            return (dot >= arrowDotMinimum && dot <= arrowDotMaximum);
        if (proj instanceof ThrownPotion)
            return (dot >= potionDotMinimum && dot <= potionDotMaximum);
        BetterBalancedShields.LOGGER.warn("Hey! I found a type that doesn't work! ", proj);
        return false;
    }

    protected static float block_volume(RandomSource random)
    {
        return (float) (shieldBlockMinVolume + random.nextFloat() * shieldBlockVolumeDiff);
    }

    protected static float block_pitch(RandomSource random)
    {
        return (float) (shieldBlockMinPitch + random.nextFloat() * shieldBlockPitchDiff);
    }

    protected static float break_volume(RandomSource random)
    {
        return (float) (shieldBreakMinVolume + random.nextFloat() * shieldBreakVolumeDiff);
    }

    protected static float break_pitch(RandomSource random)
    {
        return (float) (shieldBreakMinPitch + random.nextFloat() * shieldBreakPitchDiff);
    }
    protected static double get_shield_damage(Projectile proj)
    {
        if (proj instanceof Arrow)
            return arrowShieldDamage;
        if (proj instanceof ThrownPotion)
            return potionShieldDamage;
        BetterBalancedShields.LOGGER.warn("Unknown projectile type", proj);
        return 0;
    }
}
