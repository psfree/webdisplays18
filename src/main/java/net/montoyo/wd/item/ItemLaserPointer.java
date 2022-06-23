/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.WebDisplays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemLaserPointer extends Item implements WDItem {

    public ItemLaserPointer(Properties properties) {
        super(properties
                        //setRegistryName("laserpointer")
                .stacksTo(1)
                .tab(WebDisplays.CREATIVE_TAB));
    }

    @Nullable
    @Override
    public String getWikiName(@Nonnull ItemStack is) {
        return is.getItem().getName(is).getString();
    }
}
