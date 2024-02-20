package io.github.zephia_sero.better_balanced_shields;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import java.util.UUID;

public class PacketProjectile implements IMessage {
    UUID entityID;
    Vec3d deltaSpeed;


    public PacketProjectile() {
    }

    public PacketProjectile(UUID entityID, Vec3d deltaSpeed)
    {
        this.entityID = entityID;
        this.deltaSpeed = deltaSpeed;
    }

    public PacketProjectile(EntityThrowable throwable)
    {
        this.entityID = throwable.getUniqueID();
        this.deltaSpeed = new Vec3d(throwable.motionX, throwable.motionY, throwable.motionZ);
    }

    public PacketProjectile(ByteBuf buf)
    {
        this.fromBytes(buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityID = new UUID(buf.readLong(), buf.readLong());
        deltaSpeed = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(entityID.getMostSignificantBits());
        buf.writeLong(entityID.getLeastSignificantBits());
        buf.writeDouble(deltaSpeed.x);
        buf.writeDouble(deltaSpeed.y);
        buf.writeDouble(deltaSpeed.z);
    }
}
