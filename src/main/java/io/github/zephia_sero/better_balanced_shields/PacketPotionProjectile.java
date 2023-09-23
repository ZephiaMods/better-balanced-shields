package io.github.zephia_sero.better_balanced_shields;

import com.google.common.graph.Network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3f;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketPotionProjectile {
    private final int entityID;
    private final Vec3 deltaSpeed;

    public PacketPotionProjectile(int entityID, Vec3 deltaSpeed)
    {
        this.entityID = entityID;
        this.deltaSpeed = deltaSpeed;
    }

    public PacketPotionProjectile(FriendlyByteBuf buf)
    {
        entityID = buf.readInt();
        deltaSpeed = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public void to_bytes(FriendlyByteBuf buf)
    {
        buf.writeInt(entityID);
        buf.writeDouble(deltaSpeed.x);
        buf.writeDouble(deltaSpeed.y);
        buf.writeDouble(deltaSpeed.z);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context ctx = supplier.get();
        ServerPlayer player = ctx.getSender();
        assert (player.level().isClientSide);
        if (player.level().getEntity(entityID) instanceof ThrownPotion ent) {
            ent.setDeltaMovement(deltaSpeed);
        }
        return true;
    }
}
