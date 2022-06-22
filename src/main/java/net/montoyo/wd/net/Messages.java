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
import net.montoyo.wd.net.client.CMessageACResult;

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
        INSTANCE.registerMessage(index++, CMessageACResult.class, CMessageACResult::encode, CMessageACResult::decode, CMessageACResult::handle);

    }

    @SubscribeEvent
    public static void registryNetworkPackets (FMLCommonSetupEvent event) {

    }
}
