package io.github.zephia_sero.better_balanced_shields;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ShieldBlockEvent extends Event {
    private final DamageSource source;
    private float damage;
    private final Entity entity;
    public ShieldBlockEvent(Entity entity, DamageSource source, float damage)
    {
        this.source = source;
        this.damage = damage;
        this.entity = entity;
    }
    public DamageSource getDamageSource()
    {
        return source;
    }
    public Entity getEntity()
    {
        return entity;
    }
    public float getBlockedDamage()
    {
        return damage;
    }

    public void setBlockedDamage(float damage)
    {
        this.damage = damage;
    }
}