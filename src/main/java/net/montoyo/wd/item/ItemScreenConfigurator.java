/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.item;

import net.minecraft.world.item.Item;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.block.BlockScreen;
import net.montoyo.wd.data.ScreenConfigData;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Multiblock;
import net.montoyo.wd.utilities.Util;
import net.montoyo.wd.utilities.Vector3i;

import javax.annotation.Nonnull;

public class ItemScreenConfigurator extends Item implements WDItem {

    public ItemScreenConfigurator() {
        setUnlocalizedName("webdisplays.screencfg");
        setRegistryName("screencfg");
        setMaxStackSize(1);
        setCreativeTab(WebDisplays.CREATIVE_TAB);
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side_, float hitX, float hitY, float hitZ) {
        if(player.isSneaking() || !(world.getBlockState(pos).getBlock() instanceof BlockScreen))
            return EnumActionResult.PASS;

        if(world.isRemote)
            return EnumActionResult.SUCCESS;

        Vector3i origin = new Vector3i(pos);
        BlockSide side = BlockSide.values()[side_.ordinal()];

        Multiblock.findOrigin(world, origin, side, null);
        TileEntity te = world.getTileEntity(origin.toBlock());

        if(te == null || !(te instanceof TileEntityScreen)) {
            Util.toast(player, "turnOn");
            return EnumActionResult.SUCCESS;
        }

        TileEntityScreen.Screen scr = ((TileEntityScreen) te).getScreen(side);
        if(scr == null)
            Util.toast(player, "turnOn");
        else
            (new ScreenConfigData(origin, side, scr)).sendTo((EntityPlayerMP) player);

        return EnumActionResult.SUCCESS;
    }

}
