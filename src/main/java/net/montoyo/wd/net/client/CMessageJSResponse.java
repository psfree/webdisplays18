/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.core.JSServerRequest;
import net.montoyo.wd.utilities.Log;

import java.util.function.Supplier;

public class CMessageJSResponse {

    private int id;
    private JSServerRequest type;
    private boolean success;
    private byte[] data;
    private int errCode;
    private String errString;

    public CMessageJSResponse(int id, JSServerRequest t, byte[] d) {
        this.id = id;
        type = t;
        success = true;
        data = d;
    }

    public CMessageJSResponse(int id, JSServerRequest t, int code, String err) {
        this.id = id;
        type = t;
        success = false;
        errCode = code;
        errString = err;
    }

    public static CMessageJSResponse decode(FriendlyByteBuf buf) {
        int id = buf.readInt();
        JSServerRequest type = JSServerRequest.fromID(buf.readByte());
        boolean success = buf.readBoolean();

        byte[] data = null;

        int errCode;
        String errString;

        if(success) {
            data = new byte[buf.readByte()];
            buf.readBytes(data);

            return new CMessageJSResponse(id, type, data);
        } else {
            errCode = buf.readInt();
            errString = buf.readUtf();
            return new CMessageJSResponse(id, type, errCode, errString);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeByte(type.ordinal());
        buf.writeBoolean(success);

        if(success) {
            buf.writeByte(data.length);
            buf.writeBytes(data); //TODO: Eventually compress this data
        } else {
            buf.writeInt(errCode);
            buf.writeUtf(errString);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            try {
                if (success)
                    WebDisplays.PROXY.handleJSResponseSuccess(id, type, data);
                else
                    WebDisplays.PROXY.handleJSResponseError(id, type, errCode, errString);
            } catch (Throwable t) {
                Log.warningEx("Could not handle JS response", t);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
