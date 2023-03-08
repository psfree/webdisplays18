package net.montoyo.wd.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.montoyo.wd.client.renderers.ScreenRenderer;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.utilities.SyncedUrl;

@Mod.EventBusSubscriber(modid = "webdisplays", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Send current URL to player
        String url = SyncedUrl.getUrl();

        if(event.getEntity() instanceof ServerPlayer serverPlayer) {
            new ScreenRenderer(url);
            Messages.sendUrlToPlayer(serverPlayer, url);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // Re-sync URL when player logs back in
        String url = SyncedUrl.getUrl();
        Messages.sendUrlUpdate(url);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // Send current URL to player
        String url = SyncedUrl.getUrl();
        if(event.getEntity() instanceof ServerPlayer serverPlayer) {
            new ScreenRenderer(url);
            Messages.sendUrlToPlayer(serverPlayer, url);
        }
    }
}