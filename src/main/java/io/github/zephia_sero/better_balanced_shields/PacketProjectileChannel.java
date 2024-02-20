package io.github.zephia_sero.better_balanced_shields;

import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketProjectileChannel {
    private static SimpleNetworkWrapper INSTANCE; // = NetworkRegistry.INSTANCE.newSimpleChannel("better_balanced_shields");
    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register()
    {
        INSTANCE.registerMessage(PacketProjectileHandler.class, PacketProjectile.class, id(), Side.CLIENT);
    }

    public static void sendToNearby(EntityThrowable throwable)
    {
        INSTANCE.sendToAllAround(new PacketProjectile(throwable), new NetworkRegistry.TargetPoint(
                throwable.dimension,
                throwable.posX,
                throwable.posY,
                throwable.posZ,
                16 * 8 // 8 chunks radius
        ));
    }
}
