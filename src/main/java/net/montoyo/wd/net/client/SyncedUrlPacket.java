package net.montoyo.wd.net.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.net.Messages;

import java.util.function.Supplier;

public class SyncedUrlPacket {
    private String url;
    private boolean isDefault;

    public SyncedUrlPacket(String url, boolean isDefault) {
        this.url = url;
        this.isDefault = isDefault;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(url);
        buffer.writeBoolean(isDefault);
    }

    public static SyncedUrlPacket decode(FriendlyByteBuf buffer) {
        String url = buffer.readUtf();
        boolean isDefault = buffer.readBoolean();
        return new SyncedUrlPacket(url, isDefault);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (url != null && !url.isEmpty() && !isDefault) {
            Messages.sendUrlToPlayer(url);
        }
        context.setPacketHandled(true);
    }
}