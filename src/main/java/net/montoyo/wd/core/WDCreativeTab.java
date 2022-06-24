/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.core;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.init.BlockInit;

public class WDCreativeTab extends CreativeModeTab {

    public WDCreativeTab() {
        super("webdisplays");
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(BlockInit.blockScreen.get());
    }
}
