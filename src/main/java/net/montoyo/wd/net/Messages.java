/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.montoyo.wd.net.client.*;
import net.montoyo.wd.net.server.*;

public class Messages {

    private static final String PROTOCOL_VERSION = "1";
    private static int index = 0;
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("webdisplays", "packetsystem"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registryNetworkPackets (FMLClientSetupEvent event) {
    }

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
    }
}
