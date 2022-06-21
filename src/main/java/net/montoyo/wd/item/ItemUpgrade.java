/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.core.DefaultUpgrade;
import net.montoyo.wd.core.IUpgrade;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemUpgrade extends ItemMulti implements IUpgrade {

    public ItemUpgrade() {
        super(DefaultUpgrade.class);
        setUnlocalizedName("webdisplays.upgrade");
        setRegistryName("upgrade");
        setCreativeTab(WebDisplays.CREATIVE_TAB);
    }

    @Override
    public void onInstall(@Nonnull TileEntityScreen tes, @Nonnull BlockSide screenSide, @Nullable Player player, @Nonnull ItemStack is) {
    }

    @Override
    public boolean onRemove(@Nonnull TileEntityScreen tes, @Nonnull BlockSide screenSide, @Nullable Player player, @Nonnull ItemStack is) {
        if(DefaultUpgrade.LASER_MOUSE.matches(is))
            tes.clearLaserUser(screenSide);

        return false;
    }

    @Override
    public boolean isSameUpgrade(@Nonnull ItemStack myStack, @Nonnull ItemStack otherStack) {
        return otherStack.getItem() == this && otherStack.getTag() == myStack.getTag();
    }

    @Override
    public String getJSName(@Nonnull ItemStack is) {
        if(is.isEmpty())
            return "webdisplays:wtf";
        else
            return "webdisplays:" + is;
    }

    /*@Nullable
    @Override
    public String getWikiName(@Nonnull ItemStack is) {
        return DefaultUpgrade.getWikiName(is.getItem().getName(is).toString());
    }*/

}
