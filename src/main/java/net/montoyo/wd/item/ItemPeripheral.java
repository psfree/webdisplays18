/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.core.WDCreativeTab;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemPeripheral extends Item implements WDItem {

    public ItemPeripheral() {
        super(new Properties().tab(WDCreativeTab.TAB_REDSTONE));
    }

    //TODO what was this!
    /*@Override
    public boolean canPlaceBlock(Level world, @Nonnull BlockPos pos_, @Nonnull EnumFacing side, EntityPlayer player, ItemStack stack) {
        if(stack.getMetadata() != 0) //Keyboard
            return true;

        //Special checks for the keyboard
        BlockPos pos = pos_.add(side.getDirectionVec());
        if(world.isAirBlock(pos.down()) || !BlockKeyboardRight.checkNeighborhood(world, pos, null))
            return false;

        int f = MathHelper.floor(((double) (player.rotationYaw * 4.0f / 360.0f)) + 2.5) & 3;
        Vec3i dir = EnumFacing.getHorizontal(f).rotateY().getDirectionVec();
        BlockPos left = pos.add(dir);
        BlockPos right = pos.subtract(dir);

        if(world.isAirBlock(right) && !world.isAirBlock(right.down()) && BlockKeyboardRight.checkNeighborhood(world, right, null))
            return true;
        else
            return world.isAirBlock(left) && !world.isAirBlock(left.down()) && BlockKeyboardRight.checkNeighborhood(world, left, null);
    } */

    @Nullable
    @Override
    public String getWikiName(@Nonnull ItemStack is) {
        return is.getItem().getName(is).getString();
    }

}
