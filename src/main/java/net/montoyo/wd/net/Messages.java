/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.AttributeKey;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.montoyo.wd.net.client.*;
import net.montoyo.wd.net.server.*;
import net.montoyo.wd.utilities.SyncedUrl;
import org.jline.utils.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class Messages {

    private static final String CHANNEL_NAME = "webdisplays:packetsystem";

    private static final String PROTOCOL_VERSION = "1";
    private static int index = 0;
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("webdisplays", "packetsystem"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    @SubscribeEvent
    public static void registryNetworkPackets (FMLCommonSetupEvent event) {
        INSTANCE.registerMessage(index++, CMessageACResult.class, CMessageACResult::encode, CMessageACResult::decode, CMessageACResult::handle);
        INSTANCE.registerMessage(index++, CMessageAddScreen.class, CMessageAddScreen::encode, CMessageAddScreen::decode, CMessageAddScreen::handle);
        INSTANCE.registerMessage(index++, CMessageCloseGui.class, CMessageCloseGui::encode, CMessageCloseGui::decode, CMessageCloseGui::handle);
        INSTANCE.registerMessage(index++, CMessageJSResponse.class, CMessageJSResponse::encode, CMessageJSResponse::decode, CMessageJSResponse::handle);
        INSTANCE.registerMessage(index++, CMessageMiniservKey.class, CMessageMiniservKey::encode, CMessageMiniservKey::decode, CMessageMiniservKey::handle);
        INSTANCE.registerMessage(index++, CMessageScreenUpdate.class, CMessageScreenUpdate::encode, CMessageScreenUpdate::decode, CMessageScreenUpdate::handle);
        INSTANCE.registerMessage(index++, CMessageServerInfo.class, CMessageServerInfo::encode, CMessageServerInfo::decode, CMessageServerInfo::handle);
        INSTANCE.registerMessage(index++, SMessageACQuery.class, SMessageACQuery::encode, SMessageACQuery::decode, SMessageACQuery::handle);
        INSTANCE.registerMessage(index++, SMessageMiniservConnect.class, SMessageMiniservConnect::encode, SMessageMiniservConnect::decode, SMessageMiniservConnect::handle);
        INSTANCE.registerMessage(index++, SMessageRedstoneCtrl.class, SMessageRedstoneCtrl::encode, SMessageRedstoneCtrl::decode, SMessageRedstoneCtrl::handle);
        INSTANCE.registerMessage(index++, SMessageRequestTEData.class, SMessageRequestTEData::encode, SMessageRequestTEData::decode, SMessageRequestTEData::handle);
        INSTANCE.registerMessage(index++, SMessageScreenCtrl.class, SMessageScreenCtrl::encode, SMessageScreenCtrl::decode, SMessageScreenCtrl::handle);
        INSTANCE.registerMessage(index++, SMessagePadCtrl.class, SMessagePadCtrl::encode, SMessagePadCtrl::decode, SMessagePadCtrl::handle);
        INSTANCE.registerMessage(index++, CMessageOpenGui.class, CMessageOpenGui::encode, CMessageOpenGui::decode, CMessageOpenGui::handle);
        INSTANCE.registerMessage(index++, SyncedUrlPacket.class, SyncedUrlPacket::encode, SyncedUrlPacket::decode, SyncedUrlPacket::handle);
    }

    public static void sendUrlUpdate(String newUrl) {
        if (newUrl != null && Minecraft.getInstance().getConnection() != null) {
            Messages.INSTANCE.sendToServer(new SyncedUrlPacket(newUrl));
        }
    }

    public static void sendUrlToPlayer(ServerPlayer player, String url) {
        if(url == null) {
            url = "https://www.google.com";
        }
        SyncedUrl.setUrl(url);
        Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncedUrlPacket(url));
    }

    @SubscribeEvent
    public static void onPacketReceived(NetworkEvent.ServerCustomPayloadEvent event) {
        if (event.getSource().get().getDirection().getReceptionSide() == LogicalSide.SERVER && CHANNEL_NAME.equals(event.getPayload().readUtf())) {
            String newUrl = event.getPayload().readUtf();
            SyncedUrl.updateUrl(newUrl);
            Log.debug("Received URL update from client: {}", newUrl);
        }
    }
}
