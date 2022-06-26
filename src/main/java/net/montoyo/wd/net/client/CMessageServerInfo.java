/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.miniserv.client.Client;
import net.montoyo.wd.net.Messages;

import java.util.function.Supplier;

public class CMessageServerInfo {

    private int miniservPort;

    public CMessageServerInfo(int msPort) {
        miniservPort = msPort;
    }

    public static CMessageServerInfo decode(FriendlyByteBuf buf) {
        return new CMessageServerInfo(buf.readShort() & 0xFFFF);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeShort(miniservPort);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            WebDisplays.PROXY.setMiniservClientPort(miniservPort);
            if (miniservPort > 0)
                Messages.INSTANCE.sendToServer(Client.getInstance().beginConnection());
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
