package io.github.zephia_sero.better_balanced_shields;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Channel {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register()
    {
        INSTANCE = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(BetterBalancedShields.MOD_ID, "main_channel"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        INSTANCE.messageBuilder(PacketPotionProjectile.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketPotionProjectile::new)
                .encoder(PacketPotionProjectile::to_bytes)
                .consumerMainThread(PacketPotionProjectile::handle)
                .add();
    }

    public static <T> void sendToServer(T msg)
    {
        INSTANCE.sendToServer(msg);
    }
    public static <T> void sendToNearby(T msg, ServerPlayer who)
    {
        int r = 16*4; // 4 chunk radius
        PacketDistributor.TargetPoint point = new PacketDistributor.TargetPoint(
                who.getX(), who.getY(), who.getZ(),
                r*r,
                who.level().dimension());
        INSTANCE.send(PacketDistributor.NEAR.with(() -> point), msg);
    }
}
