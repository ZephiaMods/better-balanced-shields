package io.github.zephia_sero.better_balanced_shields;

import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraftforge.common.config.Config;

import java.util.Random;


@Config(modid = BetterBalancedShields.MODID)
public class BBSConfig
{
    @Config.Comment({
            "You may notice none of these options have ranges, so changing things can get wacky if you don't stick to what the comments suggest. Have fun!",
            "",
            "Potion stuff!",
            "Whether to block potions, without reflectablePotions too, this means the potion will just disappear when it hits a shield",
    })
    public static boolean blockablePotions = true;

    @Config.Comment("Whether to reflect potions (implies blockablePotions)")
    public static boolean reflectablePotions = true;

    @Config.Comment({
            "Velocity multiplier to reflected potions",
            "Positive numbers reflect away from you, recommend keeping the number at its default, or higher",
    })
    public static double potionReflectMultiplier = 0.75;

    @Config.Comment({
            "Minimum value of the dot product between the player's facing direction and the inverse of the projectile's normalized motion in order to count as a shield hit",
            "In other words, -1 means you reflect arrows completely opposite which direction you're looking, 1 means you have to be looking the exact same direction",
            "Values below -1 behave identically to -1, and values above 1 behave identically to 1",
            "Note, this is the lower part of the range, ideally this should be less than 1",
            "With the default POTION_DOT_MAXIMUM, think of this as bigger number = harder to block",
    })
    public static double potionDotMinimum = 0.25;

    @Config.Comment({
            "For the nerd stuff, check out POTION_DOT_MINIMUM",
            "Note, this is the maximum part of the range, ideally this should just be 1",
    })
    public static double potionDotMaximum = 1.0;

    @Config.Comment("Damage dealt to shield when reflecting/blocking a potion")
    public static double potionShieldDamage = 2.0;

    @Config.Comment("Just multiply potion velocity, don't redirect to player's facing direction")
    public static boolean potionAutoaim = false;

    @Config.Comment("Disables this mod's logic for calculating and reflecting potions, overrides all the above potion settings")
    public static boolean vanillaPotions = false;

    @Config.Comment({
            "Arrow stuff!",
            "Whether to block arrows, without reflectableArrows, this means the arrow will disappear when it hits a shield",
    })
    public static boolean blockableArrows = true;

    @Config.Comment("Whether to reflect arrows (implies blockableArrows)")
    public static boolean reflectableArrows = true;

    @Config.Comment({
            "Velocity multiplier to reflected arrow",
            "Positive numbers reflect away from you",
    })
    public static double arrowReflectMultiplier = 0.25;

    @Config.Comment("See POTION_DOT_MINIMUM")
    public static double arrowDotMinimum = 0.125;

    @Config.Comment("See POTION_DOT_MAXIMUM")
    public static double arrowDotMaximum = 1.0;

    @Config.Comment("Damage dealt to shield when reflecting/blocking an arrow")
    public static double arrowShieldDamage = 2.0;

    @Config.Comment("Just multiply arrow velocity, don't redirect to player's facing direction")
    public static boolean arrowAutoaim = false;

    @Config.Comment("Arrow reflect will crit or not, simple as that")
    public static boolean arrowReflectCrits = true;

    @Config.Comment("Disables this mod's logic for calculating and reflecting arrows, overrides all the above arrow settings")
    public static boolean vanillaArrows = false;

    @Config.Comment({
            "Melee stuff!",
            "Percentage (default of 0.75 = 75%) of damage taken if shield is up. If this number is negative, you are healed by the negated percentage.",
    })
    public static double meleeDamageMultiplier = 0.75;

    @Config.Comment("Percentage (default of 0.00 = 0%) of damage reflected (think thorns enchant) if shield is up. If this number is negative, the assailant is healed by the negated percentage.")
    public static double meleeDamageReflection = 0.0;

    @Config.Comment("Whether or not meleeShieldDamage is a percentage of damage taken applied to a shield (true), or a constant (false)")
    public static boolean meleeScaledShieldDamage = true;

    @Config.Comment({
            "Read above.",
            "If it's true, damage before any modifiers is multiplied by this number and applied to the shield",
            "If it's false, just this number is applied",
    })
    public static double meleeShieldDamage = 0.5;

    @Config.Comment("Nullifies above melee settings")
    public static boolean vanillaMelee  = false;

    @Config.Comment("Minimum volume when this mod plays the SHIELD_BLOCK sound effect")
    public static double shieldBlockMinVolume = 1.0;

    @Config.Comment("Maximum volume when this mod plays the SHIELD_BLOCK sound effect")
    public static double shieldBlockMaxVolume = 1.0;

    @Config.Comment("Minimum pitch when this mod plays the SHIELD_BLOCK sound effect")
    public static double shieldBlockMinPitch = 0.8;

    @Config.Comment("Maximum pitch when this mod plays the SHIELD_BLOCK sound effect")
    public static double shieldBlockMaxPitch = 1.2;

    @Config.Comment("Minimum volume when this mod plays the SHIELD_BREAK sound effect")
    public static double shieldBreakMinVolume = 1.0;

    @Config.Comment("Maximum volume when this mod plays the SHIELD_BREAK sound effect")
    public static double shieldBreakMaxVolume = 1.0;

    @Config.Comment("Minimum pitch when this mod plays the SHIELD_BREAK sound effect")
    public static double shieldBreakMinPitch = 0.8;

    @Config.Comment("Maximum pitch when this mod plays the SHIELD_BREAK sound effect")
    public static double shieldBreakMaxPitch = 1.2;

    protected static boolean projectile_blocked(IProjectile proj, double dot)
    {
        if (proj instanceof EntityArrow)
            return (dot >= arrowDotMinimum && dot <= arrowDotMaximum);
        if (proj instanceof EntityPotion)
            return (dot >= potionDotMinimum && dot <= potionDotMaximum);
        BetterBalancedShields.logger.warn("Hey! I found a type that doesn't work! ", proj);
        return false;
    }

    protected static float block_volume(Random random)
    {
        return (float) (shieldBlockMinVolume + random.nextFloat() * (shieldBlockMaxVolume - shieldBlockMinVolume));
    }

    protected static float block_pitch(Random random)
    {
        return (float) (shieldBlockMinPitch + random.nextFloat() * (shieldBlockMaxPitch - shieldBlockMinPitch));
    }

    protected static float break_volume(Random random)
    {
        return (float) (shieldBreakMinVolume + random.nextFloat() * (shieldBreakMaxVolume - shieldBreakMinVolume));
    }

    protected static float break_pitch(Random random)
    {
        return (float) (shieldBreakMinPitch + random.nextFloat() * (shieldBreakMaxPitch - shieldBreakMinPitch));
    }
    protected static double get_shield_damage(IProjectile proj)
    {
        if (proj instanceof EntityArrow)
            return arrowShieldDamage;
        if (proj instanceof EntityPotion)
            return potionShieldDamage;
        BetterBalancedShields.logger.warn("Unknown projectile type", proj);
        return 0;
    }
}
