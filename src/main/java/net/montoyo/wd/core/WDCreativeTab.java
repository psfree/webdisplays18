/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.WebDisplays;

import javax.annotation.Nonnull;

public class WDCreativeTab extends CreativeModeTab {

    public WDCreativeTab() {
        super("webdisplays");
    }

    @Override
    public ItemStack makeIcon() {
        return WebDisplays.INSTANCE.blockScreen.getItem();
    }
}
