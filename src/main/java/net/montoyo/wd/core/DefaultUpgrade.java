/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.core;

import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.config.ModConfig;
import net.montoyo.wd.init.ItemInit;

public enum DefaultUpgrade {

    LASER_MOUSE("lasermouse", "Laser_Sensor"),
    REDSTONE_INPUT("redinput", "Redstone_Input_Port"),
    REDSTONE_OUTPUT("redoutput", "Redstone_Output_Port"),
    GPS("gps", "GPS_Module");

    private final String name;
    private final String wikiName;

    DefaultUpgrade(String n, String wn) {
        name = n;
        wikiName = wn;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean matches(ItemStack is) {
        return is.getItem() == ItemInit.itemUpgrade.get();
    }
}
