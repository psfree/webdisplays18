/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.core;

import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.config.ModConfig;
import net.montoyo.wd.init.ItemInit;

public enum DefaultUpgrade {

    LASERMOUSE("lasermouse", "LaserMouse"),
    REDINPUT("redinput", "RedInput"),
    REDOUTPUT("redoutput", "RedOutput"),
    GPS("gps", "GPS");

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

    public boolean matchesLaserMouse(ItemStack is) {
        return is.getItem() == ItemInit.laserMouse.get();
    }

    public boolean matchesRedInput(ItemStack is) {
        return is.getItem() == ItemInit.redInput.get();
    }

    public boolean matchesRedOutput(ItemStack is) {
        return is.getItem() == ItemInit.redOutput.get();
    }

    public boolean matchesGps(ItemStack is) {
        return is.getItem() == ItemInit.gps.get();
    }
}
