package net.montoyo.wd.net.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class URLMessage {

        private String url;
        
        public URLMessage() {}

        public URLMessage(String url) {
            this.url = url;
        }

        public String getURL() {
            return url;
        }

        public static URLMessage decode(FriendlyByteBuf buf) {
            URLMessage message = new URLMessage();
            message.url = buf.readUtf();
            return message;
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(url);
        }

        public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().setPacketHandled(true);
        }
    }