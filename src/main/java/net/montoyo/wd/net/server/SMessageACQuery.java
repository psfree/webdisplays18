/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.net.client.CMessageACResult;
import net.montoyo.wd.utilities.NameUUIDPair;

import java.util.Arrays;
import java.util.function.Supplier;

public class SMessageACQuery implements Runnable {

    private ServerPlayer player;
    private String beginning;
    private boolean matchExact;

    public SMessageACQuery(String beg, boolean exact) {
        beginning = beg;
        matchExact = exact;
    }

    public static SMessageACQuery decode(FriendlyByteBuf buf) {
        return new SMessageACQuery(buf.readUtf(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(beginning);
        buf.writeBoolean(matchExact);
    }

    @Override
    public void run() {
        GameProfile[] profiles = WebDisplays.PROXY.getOnlineGameProfiles();
        NameUUIDPair[] result;

        if(matchExact)
            result = Arrays.stream(profiles).filter(gp -> gp.getName().equalsIgnoreCase(beginning)).map(NameUUIDPair::new).toArray(NameUUIDPair[]::new);
        else {
            final String lBeg = beginning.toLowerCase();
            result = Arrays.stream(profiles).filter(gp -> gp.getName().toLowerCase().startsWith(lBeg)).map(NameUUIDPair::new).toArray(NameUUIDPair[]::new);
        }

        Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new CMessageACResult(result));
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        player = contextSupplier.get().getSender();
        contextSupplier.get().enqueueWork(this);
        contextSupplier.get().setPacketHandled(true);
    }

}
