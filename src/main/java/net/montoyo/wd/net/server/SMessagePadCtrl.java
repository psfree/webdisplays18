/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.server;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.init.ItemInit;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.utilities.SyncedUrl;

import java.io.IOException;
import java.util.function.Supplier;

public class SMessagePadCtrl implements Runnable {

    private int id;
    private String url;
    private ServerPlayer player;

    public SMessagePadCtrl() {
    }

    public SMessagePadCtrl(String url) {
        id = -1;
        this.url = url;
    }

    public SMessagePadCtrl(int id, String url) {
        this.id = id;
        this.url = url;
    }

    private boolean matchesMinePadID(ItemStack is) {
        return is.getItem() == ItemInit.itemMinePad.get() && is.getTag() != null && is.getTag().contains("PadID") && is.getTag().getInt("PadID") == id;
    }

    @Override
    public void run() {
        if(id < 0) {
            ItemStack is = player.getItemInHand(InteractionHand.MAIN_HAND);

            if(is.getItem() == ItemInit.itemMinePad.get()) {
                if(url.isEmpty())
                    is.setTag(null); //Shutdown
                else {
                    if(is.getTag() == null)
                        is.setTag(new CompoundTag());

                    if(!is.getTag().contains("PadID"))
                        is.getTag().putInt("PadID", WebDisplays.getNextAvailablePadID());

                    Messages.sendUrlUpdate(url);
                    String webUrl = SyncedUrl.getUrl();
                    is.getTag().putString("PadURL", WebDisplays.applyBlacklist(webUrl));
                }
            }
        } else {
            NonNullList<ItemStack> inv = player.getInventory().items;
            ItemStack target = null;

            for(int i = 0; i < 9; i++) {
                if(matchesMinePadID(inv.get(i))) {
                    target = inv.get(i);
                    break;
                }
            }

            if(target == null && matchesMinePadID(player.getInventory().offhand.get(0)))
                target = player.getInventory().offhand.get(0);

            if(target != null) {
                Messages.sendUrlUpdate(url);
                String webUrl = SyncedUrl.getUrl();
                target.getTag().putString("PadURL", WebDisplays.applyBlacklist(webUrl));
            }
        }
    }

    public static SMessagePadCtrl decode(FriendlyByteBuf buf) {
        SMessagePadCtrl message = new SMessagePadCtrl();
        message.id = buf.readInt();
        message.url = buf.readUtf();
        return message;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeUtf(url);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        player = contextSupplier.get().getSender();
        contextSupplier.get().enqueueWork(this);
        contextSupplier.get().setPacketHandled(true);
    }

}
