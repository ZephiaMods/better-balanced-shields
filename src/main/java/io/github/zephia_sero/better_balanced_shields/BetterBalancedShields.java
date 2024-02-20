package io.github.zephia_sero.better_balanced_shields;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(modid = BetterBalancedShields.MODID, name = BetterBalancedShields.NAME, version = BetterBalancedShields.VERSION)
public class BetterBalancedShields
{
    public static final String MODID = "better_balanced_shields";
    public static final String NAME = "Better Balanced Shields";
    public static final String VERSION = "0.1.1";

    static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {

    }

    private static void take_shield_damage(EntityPlayer pe, double dmg)
    {
        ItemStack used = pe.getActiveItemStack();
        if (used.isEmpty() || !(used.getItem() instanceof ItemShield) || dmg <= 0f)
            return;
        int d = (int)Math.ceil(dmg);
        used.setItemDamage(used.getItemDamage() + d);
        if (used.isEmpty()) {
            EnumHand hand = pe.getActiveHand();
            ForgeEventFactory.onPlayerDestroyItem(pe, used, hand);
            if (hand == EnumHand.MAIN_HAND)
                pe.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
            else
                pe.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
            Random random = pe.world.rand;
            float volume = BBSConfig.break_volume(random);
            float pitch = BBSConfig.break_pitch(random);
            pe.playSound(SoundEvents.ITEM_SHIELD_BREAK, volume, pitch);
        }
    }

    @SubscribeEvent
    public void onEntityAttacked(LivingAttackEvent event)
    {
        if (event == null)
            return;
        Entity entity = event.getEntity();
        if (entity == null)
            return;
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack item = player.getActiveItemStack();
            if (player.isActiveItemStackBlocking() && item.getItem() instanceof ItemShield) {
                MinecraftForge.EVENT_BUS.post(new ShieldBlockEvent(
                        player,
                        event.getSource(),
                        (float) event.getAmount()
                ));
            }
        }
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event)
    {
        RayTraceResult result = event.getRayTraceResult();
        Entity proj = event.getEntity();
        if (proj instanceof EntityArrow && (!BBSConfig.blockableArrows || BBSConfig.vanillaArrows)) return;
        if (proj instanceof EntityPotion && (!BBSConfig.blockablePotions || BBSConfig.vanillaPotions)) return;
        if (!(proj instanceof EntityArrow) && !(proj instanceof EntityPotion)) return;

        if (result.entityHit != null) {
            if (result.entityHit instanceof EntityPlayer) {
                EntityPlayer pl = (EntityPlayer) result.entityHit;
                if (pl.getActiveItemStack().getItem() instanceof ItemShield) {
                    Vec3d projDir = new Vec3d(proj.motionX, proj.motionY, proj.motionZ).scale(-1);
                    Vec3d plDir = pl.getForward();

                    double dot = projDir.normalize().dotProduct(plDir);
                    if (BBSConfig.projectile_blocked((IProjectile) proj, dot)) {
                        if (event.isCancelable()) {
                            event.setCanceled(true);
                        }
                        World level = pl.getEntityWorld();
                        //try (Level level = pl.level()) {
                        DamageSource src;
                        if (proj instanceof EntityPotion) {
                            EntityPotion potion = (EntityPotion) proj;
                            src = DamageSource.causeIndirectMagicDamage(potion, potion.getThrower());
                        } else {
                            EntityArrow arrow = (EntityArrow) proj;
                            src = DamageSource.causeArrowDamage(arrow, arrow.shootingEntity);
                        }
                        float volume = BBSConfig.block_volume(level.rand);
                        float pitch = BBSConfig.block_pitch(level.rand);
                        pl.playSound(SoundEvents.ITEM_SHIELD_BLOCK, volume, pitch);
                        pl.stopActiveHand();
                        double shieldDamage = BBSConfig.get_shield_damage((IProjectile) proj);
                        take_shield_damage(pl, shieldDamage);
                        MinecraftForge.EVENT_BUS.post(new ShieldBlockEvent(
                                pl,
                                src,
                                (float) shieldDamage
                        ));
                        //} catch (IOException e) {
                        //    LOGGER.error(e.getLocalizedMessage());
                        //}
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onShieldBlock(ShieldBlockEvent event) {
        DamageSource source = event.getDamageSource();
        logger.warn("got to shield block event");
        logger.warn(source.getImmediateSource());
        logger.warn(source.getTrueSource());
        logger.warn(BBSConfig.vanillaPotions);
        if (source.getImmediateSource() instanceof EntityArrow && !BBSConfig.vanillaArrows) {
            logger.warn("shieldevent arrow");
            EntityArrow arrow = (EntityArrow) source.getImmediateSource();
            if (BBSConfig.reflectableArrows) {
                if (BBSConfig.arrowAutoaim) {
                    arrow.setVelocity(
                            arrow.motionX * -BBSConfig.arrowReflectMultiplier,
                            arrow.motionY * -BBSConfig.arrowReflectMultiplier,
                            arrow.motionZ * -BBSConfig.arrowReflectMultiplier
                    );
                } else {
                    Entity e = event.getEntity();
                    double vel = new Vec3d(arrow.motionX, arrow.motionY, arrow.motionZ).lengthVector() * BBSConfig.arrowReflectMultiplier;
                    Vec3d forward = e.getForward().scale(vel);
                    arrow.setVelocity(forward.x, forward.y, forward.z);
                    arrow.rotationPitch = e.rotationPitch;
                    arrow.rotationYaw = e.getRotationYawHead();
                }
                arrow.setIsCritical(BBSConfig.arrowReflectCrits);
            } else if (BBSConfig.blockableArrows)
                arrow.setDead();
        } else if ((source.getImmediateSource() instanceof EntityMob || source.getImmediateSource() instanceof EntityPlayer) && !BBSConfig.vanillaMelee) {
            logger.warn("shieldevent melee");
            float orig = event.getBlockedDamage();
            float dmg = (float) (1.0 - BBSConfig.meleeDamageMultiplier) * orig;
            event.setBlockedDamage(dmg);
            float thornsDmg = (float) (BBSConfig.meleeDamageReflection * orig);
            Entity en = event.getEntity();
            World level = en.getEntityWorld();

            //try () {
            if (thornsDmg > 0) {
                DamageSource dmgSrc = DamageSource.causeThornsDamage(en);
                source.getImmediateSource().attackEntityFrom(dmgSrc, thornsDmg);
            }
            if (en instanceof EntityPlayer) {
                EntityPlayer pl = (EntityPlayer) en;
                float volume = BBSConfig.block_volume(level.rand);
                float pitch = BBSConfig.block_pitch(level.rand);
                pl.playSound(SoundEvents.ITEM_SHIELD_BLOCK, volume, pitch);
                if (BBSConfig.meleeScaledShieldDamage) {
                    take_shield_damage(pl, BBSConfig.meleeShieldDamage * orig);
                } else {
                    take_shield_damage(pl, BBSConfig.meleeShieldDamage);
                }
                pl.stopActiveHand();
            }
            //} catch (IOException e) {
            //    LOGGER.error(e.getLocalizedMessage());
            //}
        } else if (source.getImmediateSource() instanceof EntityPotion && !BBSConfig.vanillaPotions) {
            EntityPotion pot = (EntityPotion) source.getImmediateSource();
            logger.warn("shieldevent potion");
            if (BBSConfig.reflectablePotions) {
                logger.warn("potion reflectig");
                if (BBSConfig.potionAutoaim)
                    pot.setVelocity(
                            pot.motionX * -BBSConfig.potionReflectMultiplier,
                            pot.motionY * -BBSConfig.potionReflectMultiplier,
                            pot.motionZ * -BBSConfig.potionReflectMultiplier
                    );
                else {
                    Entity e = event.getEntity();
                    double vel = new Vec3d(pot.motionX, pot.motionY, pot.motionZ).lengthVector() * BBSConfig.potionReflectMultiplier;
                    Vec3d forward = e.getForward().scale(vel);
                    pot.setVelocity(forward.x, forward.y, forward.z);
                }
            } else if (BBSConfig.blockablePotions)
                pot.setDead();
        }
    }
}
