package io.github.zephia_sero.better_balanced_shields;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

import java.io.IOException;

@Mod(BetterBalancedShields.MOD_ID)
public class BetterBalancedShields
{
    public static final String MOD_ID = "better_balanced_shields";
    protected static final Logger LOGGER = LogUtils.getLogger();

    public BetterBalancedShields()
    {
        //IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void take_shield_damage(Player pe, double dmg)
    {
        ItemStack used = pe.getUseItem();
        if (used.isEmpty() || !(used.getItem() instanceof ShieldItem) || dmg <= 0f)
            return;
        int d = (int)Math.ceil(dmg);
        used.setDamageValue(used.getDamageValue() + d);
        if (used.isEmpty()) {
            InteractionHand hand = pe.getUsedItemHand();
            ForgeEventFactory.onPlayerDestroyItem(pe, used, hand);
            if (hand == InteractionHand.MAIN_HAND)
                pe.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            else
                pe.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            RandomSource random = pe.level().random;
            float volume = Config.break_volume(random);
            float pitch = Config.break_pitch(random);
            pe.playSound(SoundEvents.SHIELD_BREAK, volume, pitch);
        }
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event)
    {
        HitResult result = event.getRayTraceResult();
        Projectile proj = event.getProjectile();
        if (proj instanceof Arrow && !Config.blockableArrows) return;
        if (proj instanceof ThrownPotion && !Config.blockablePotions) return;
        if (!(proj instanceof Arrow) && !(proj instanceof ThrownPotion)) return;

        if (result instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof Player pl) {
                if (pl.isBlocking()) {
                    Vec3 projDir = proj.getDeltaMovement().reverse();
                    Vec3 plDir = pl.getForward();

                    double dot = projDir.normalize().dot(plDir);
                    if (Config.projectile_blocked(proj, dot)) {
                        if (event.isCancelable()) {
                            event.setCanceled(true);
                            Level level = pl.level();
                            //try (Level level = pl.level()) {
                                Holder<DamageType> magicType = level.registryAccess()
                                        .registryOrThrow(Registries.DAMAGE_TYPE)
                                        .getHolderOrThrow(DamageTypes.MAGIC);
                                float volume = Config.block_volume(level.random);
                                float pitch = Config.block_pitch(level.random);
                                pl.playSound(SoundEvents.SHIELD_BLOCK, volume, pitch);
                                pl.stopUsingItem();
                                double shieldDamage = Config.get_shield_damage(proj);
                                take_shield_damage(pl, shieldDamage);
                                MinecraftForge.EVENT_BUS.post(new ShieldBlockEvent(
                                        pl,
                                        new DamageSource(
                                                magicType,
                                                proj,
                                                proj.getOwner(),
                                                result.getLocation()
                                        ),
                                        (float)shieldDamage
                                ));
                            //} catch (IOException e) {
                            //    LOGGER.error(e.getLocalizedMessage());
                            //}
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onShieldBlock(ShieldBlockEvent event) {
        DamageSource source = event.getDamageSource();
        if (source.getDirectEntity() instanceof Arrow arrow && !Config.vanillaArrows) {
            if (Config.reflectableArrows) {
                if (Config.arrowAutoaim)
                    arrow.setDeltaMovement(arrow.getDeltaMovement().scale(-Config.arrowReflectMultiplier));
                else {
                    Entity e = event.getEntity();
                    double vel = arrow.getDeltaMovement().length() * Config.arrowReflectMultiplier;
                    Vec3 forward = e.getForward();
                    arrow.setDeltaMovement(forward.scale(vel));
                    arrow.setXRot(e.getXRot());
                    arrow.setYRot(e.getYRot());
                }
                arrow.setCritArrow(Config.arrowReflectCrits);
            } else if (Config.blockableArrows)
                arrow.kill();
        } else if ((source.getDirectEntity() instanceof Mob || source.getDirectEntity() instanceof Player) && !Config.vanillaMelee) {
            float orig = event.getBlockedDamage();
            float dmg = (float) (1.0 - Config.meleeDamageMultiplier) * orig;
            event.setBlockedDamage(dmg);
            float thornsDmg = (float) (Config.meleeDamageReflection * orig);
            Entity en = event.getEntity();
            Level level = en.level();
            //try () {
            if (thornsDmg > 0) {
                Holder<DamageType> dmgType = level
                        .registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.THORNS);
                DamageSource dmgSrc = new DamageSource(dmgType, en);
                source.getDirectEntity().hurt(dmgSrc, thornsDmg);
            }
            if (en instanceof Player pl) {
                float volume = Config.block_volume(level.random);
                float pitch = Config.block_pitch(level.random);
                pl.playSound(SoundEvents.SHIELD_BLOCK, volume, pitch);
                if (Config.meleeScaledShieldDamage) {
                    take_shield_damage(pl, Config.meleeShieldDamage * orig);
                } else {
                    take_shield_damage(pl, Config.meleeShieldDamage);
                }
                pl.stopUsingItem();
            }
            //} catch (IOException e) {
            //    LOGGER.error(e.getLocalizedMessage());
            //}
        } else if (source.getDirectEntity() instanceof ThrownPotion pot && !Config.vanillaPotions) {
            if (Config.reflectablePotions) {
                if (Config.potionAutoaim)
                    pot.setDeltaMovement(pot.getDeltaMovement().scale(-Config.potionReflectMultiplier));
                else {
                    Entity e = event.getEntity();
                    double vel = pot.getDeltaMovement().length() * Config.potionReflectMultiplier;
                    pot.setDeltaMovement(e.getForward().scale(vel));
                    pot.setXRot(e.getXRot());
                    pot.setYRot(e.getYRot());
                }
            } else if (Config.blockablePotions)
                pot.kill();
        }
    }
}
