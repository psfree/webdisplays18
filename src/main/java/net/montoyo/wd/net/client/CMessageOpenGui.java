/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.data.GuiData;
import net.montoyo.wd.utilities.Util;

import java.util.function.Supplier;

public class CMessageOpenGui implements Runnable {

    private GuiData data;

    public CMessageOpenGui(GuiData data) {
        this.data = data;
    }

    public void decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        Class<? extends GuiData> cls = GuiData.classOf(name);

        if(cls == null) {
            Log.error("Could not create GuiData of type %s because it doesn't exist!", name);
            return;
        }

        data = (GuiData) Util.unserialize(buf, cls);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(data.getName());
        Util.serialize(buf, data);
    }

    @Override
    public void run() {
        WebDisplays.PROXY.displayGui(data);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(this);
        contextSupplier.get().setPacketHandled(true);
    }
}
