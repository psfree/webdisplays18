/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.server;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.miniserv.server.ClientManager;
import net.montoyo.wd.miniserv.server.Server;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.net.client.CMessageMiniservKey;

import java.util.Objects;
import java.util.function.Supplier;

public class SMessageMiniservConnect {

    private byte[] modulus;
    private byte[] exponent;

    public SMessageMiniservConnect() {
    }

    public SMessageMiniservConnect(byte[] mod, byte[] exp) {
        modulus = mod;
        exponent = exp;
    }

    public static SMessageMiniservConnect decode(FriendlyByteBuf buf) {
        int sz = buf.readShort() & 0xFFFF;
        byte[] modulus = new byte[sz];
        buf.readBytes(modulus);

        sz = buf.readShort() & 0xFFFF;
        byte[] exponent = new byte[sz];
        buf.readBytes(exponent);

        return new SMessageMiniservConnect(modulus, exponent);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeShort(modulus.length);
        buf.writeBytes(modulus);
        buf.writeShort(exponent.length);
        buf.writeBytes(exponent);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        ClientManager cliMgr = Server.getInstance().getClientManager();
        byte[] encKey = cliMgr.encryptClientKey(Objects.requireNonNull(contextSupplier.get().getSender()).getGameProfile().getId(), modulus, exponent);

        if (encKey != null) {
            Messages.INSTANCE.sendTo(new CMessageMiniservKey(encKey), new Connection(PacketFlow.SERVERBOUND), NetworkDirection.LOGIN_TO_SERVER);
        }

        contextSupplier.get().setPacketHandled(true);
    }
}
