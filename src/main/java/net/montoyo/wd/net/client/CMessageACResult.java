/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.utilities.NameUUIDPair;

import java.util.function.Supplier;

public class CMessageACResult {

    private static NameUUIDPair[] result;

    public CMessageACResult(NameUUIDPair[] pairs) {
        result = pairs;
    }

    public static CMessageACResult decode(FriendlyByteBuf buf) {
        int cnt = buf.readByte();
        result = new NameUUIDPair[cnt];

        for(int i = 0; i < cnt; i++)
            result[i] = new NameUUIDPair(buf);

        return new CMessageACResult(result);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(result.length);

        for(NameUUIDPair pair : result)
            pair.writeTo(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            WebDisplays.PROXY.onAutocompleteResult(result);
        });
        contextSupplier.get().setPacketHandled(true);
    }

}
