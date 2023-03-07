package net.montoyo.wd.net.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.montoyo.wd.net.Messages;

import java.util.Objects;
import java.util.function.Supplier;

public class SMessageGetUrl {
    String url;

    public SMessageGetUrl() {
    }

    public SMessageGetUrl(String url) {
        this.url = url;
    }

    public static SMessageGetUrl decode(FriendlyByteBuf buf) {
        SMessageGetUrl message = new SMessageGetUrl();
        message.url = buf.readUtf();
        return message;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(url);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            int connectTime = Objects.requireNonNull(Objects.requireNonNull(contextSupplier.get().getSender()).getServer()).getTickCount() - Objects.requireNonNull(contextSupplier.get().getSender()).connection.player.tickCount;
            if(Objects.requireNonNull(contextSupplier.get().getSender()).connection.getConnection().isConnected() && connectTime > 20) {
                Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> contextSupplier.get().getSender()), new URLMessage(url));
            }
            Messages.INSTANCE.send(PacketDistributor.ALL.noArg(), new URLMessage(url));
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
