/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.client.ClientProxy;
import net.montoyo.wd.miniserv.client.Client;
import net.montoyo.wd.utilities.Log;

import java.util.function.Supplier;

public class CMessageMiniservKey {

    private byte[] encryptedKey;

    public CMessageMiniservKey(byte[] key) {
        encryptedKey = key;
    }

    public static CMessageMiniservKey decode(FriendlyByteBuf buf) {
        byte[] encryptedKey = new byte[buf.readShort() & 0xFFFF];
        buf.readBytes(encryptedKey);
        return new CMessageMiniservKey(encryptedKey);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeShort(encryptedKey.length);
        buf.writeBytes(encryptedKey);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            if (Client.getInstance().decryptKey(encryptedKey)) {
                Log.info("Successfully received and decrypted key, starting miniserv client...");
                if(WebDisplays.PROXY instanceof ClientProxy proxy) {
                    proxy.startMiniservClient();
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
