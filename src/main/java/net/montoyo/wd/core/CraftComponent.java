/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.core;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.WebDisplays;

public enum CraftComponent {

    STONE_KEY("stonekey", "Stone_Key"),
    BLANK_UPGRADE("upgrade", "Blank_Upgrade"),
    PERIPHERAL_BASE("peripheral", "Peripheral_Base"),
    BATTERY_CELL("batcell", "Battery_Cell"),
    BATTERY_PACK("batpack", "Battery_Pack"),
    LASER_DIODE("laserdiode", "Laser_Diode"),
    BACKLIGHT("backlight", "Backlight"),
    EXTENSION_CARD("extcard", "Blank_Upgrade"),
    BAD_EXTENSION_CARD("badextcard", "Bad_Extension_Card");

    private final String name;
    private final String wikiName;

    CraftComponent(String n, String wikiName) {
        name = n;
        this.wikiName = wikiName;
    }

    @Override
    public String toString() {
        return name;
    }

    public ItemStack makeItemStack(CompoundTag compoundTag) {
        return new ItemStack(WebDisplays.INSTANCE.itemCraftComp, 1, compoundTag);
    }

}
