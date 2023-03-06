/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.core.DefaultUpgrade;
import net.montoyo.wd.core.IUpgrade;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemUpgrade extends ItemMulti implements IUpgrade, WDItem {

    public ItemUpgrade() {
        super(DefaultUpgrade.class, new Properties().tab(WebDisplays.CREATIVE_TAB));
    }

    @Override
    public void onInstall(@Nonnull TileEntityScreen tes, @Nonnull BlockSide screenSide, @Nullable Player player, @Nonnull ItemStack is) {
    }

    @Override
    public boolean onRemove(@Nonnull TileEntityScreen tes, @Nonnull BlockSide screenSide, @Nullable Player player, @Nonnull ItemStack is) {
        if(DefaultUpgrade.LASERMOUSE.matches(is))
            tes.clearLaserUser(screenSide);

        return false;
    }

    @Override
    public boolean isSameUpgrade(@Nonnull ItemStack myStack, @Nonnull ItemStack otherStack) {
        return otherStack.getItem() == this && otherStack == myStack;
    }

    @Override
    public String getJSName(@Nonnull ItemStack is) {
        ItemStack meta = is;
        DefaultUpgrade[] upgrades = DefaultUpgrade.values();

        if(meta.isEmpty())
            return "webdisplays:wtf";
        else
            return "webdisplays:" + is;
    }

    @Override
    public String getWikiName(@NotNull ItemStack is) {
        return is.getItem().getName(is).getString();
    }
}
