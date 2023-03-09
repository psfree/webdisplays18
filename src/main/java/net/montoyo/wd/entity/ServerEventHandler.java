package net.montoyo.wd.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.montoyo.wd.client.renderers.ScreenRenderer;
import net.montoyo.wd.miniserv.SyncPlugin;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class ServerEventHandler {
    public static final Map<ServerPlayer, ScreenRenderer> playerScreens = new HashMap<>();

    @SubscribeEvent
    public void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event) {
        // create a new instance of the ScreenRenderer class for the player
        ServerPlayer player = (ServerPlayer) event.getEntity();
        String url = SyncPlugin.getPlayerString(player);
        if(event.getEntity().getLevel().isClientSide) {
            ScreenRenderer screen = new ScreenRenderer(url);
            // store the ScreenRenderer instance in the playerScreens map
            playerScreens.put(player, screen);
        }
    }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        // remove the ScreenRenderer instance for the player
        ServerPlayer player = (ServerPlayer) event.getEntity();
        playerScreens.remove(player);
    }
}