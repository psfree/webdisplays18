/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.data;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.net.client.CMessageOpenGui;

import java.util.HashMap;

public abstract class GuiData {

    private static final HashMap<String, Class<? extends GuiData>> dataTable = new HashMap<>();
    static {
        dataTable.put("SetURL", SetURLData.class);
        dataTable.put("ScreenConfig", ScreenConfigData.class);
        dataTable.put("Keyboard", KeyboardData.class);
        dataTable.put("RedstoneCtrl", RedstoneCtrlData.class);
        dataTable.put("Server", ServerData.class);
    }

    public static Class<? extends GuiData> classOf(String name) {
        return dataTable.get(name);
    }

    @OnlyIn(Dist.CLIENT)
    public abstract Screen createGui(Screen old, Level world);
    public abstract String getName();

    public void sendTo(ServerPlayer player) {
        WebDisplays.NET_HANDLER.sendTo(new CMessageOpenGui(this), player);
    }

}
