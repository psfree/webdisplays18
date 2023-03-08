package net.montoyo.wd.net.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.net.Messages;

import java.util.function.Supplier;

public class SyncedUrlPacket {
    private String url;

    public SyncedUrlPacket(String url) {
        this.url = url;
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.url);
    }
    
    public static SyncedUrlPacket decode(FriendlyByteBuf buffer) {
        return new SyncedUrlPacket(buffer.readUtf());
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        Messages.sendUrlUpdate(this.url);
        context.setPacketHandled(true);
    }
}