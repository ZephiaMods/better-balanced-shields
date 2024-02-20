package io.github.zephia_sero.better_balanced_shields;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketProjectileHandler implements IMessageHandler<PacketProjectile, IMessage> {
    @Override
    public IMessage onMessage(PacketProjectile message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            Entity ent = player.getServerWorld().getEntityFromUuid(message.entityID);
            if (ent != null) {
                ent.motionX = message.deltaSpeed.x;
                ent.motionY = message.deltaSpeed.y;
                ent.motionZ = message.deltaSpeed.z;
            }
        });
        return null;
    }
}
