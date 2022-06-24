/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.data;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.montoyo.wd.client.gui.GuiKeyboard;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.utilities.Vector3i;

public class KeyboardData extends GuiData {

    public Vector3i pos;
    public BlockSide side;
    public int kbX;
    public int kbY;
    public int kbZ;

    public KeyboardData() {
    }

    public KeyboardData(TileEntityScreen tes, BlockSide side, BlockPos kbPos) {
        pos = new Vector3i(tes.getBlockPos());
        this.side = side;
        kbX = kbPos.getX();
        kbY = kbPos.getY();
        kbZ = kbPos.getZ();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Screen createGui(Screen old, Level world) {
        BlockEntity te = world.getBlockEntity(pos.toBlock());
        if(te == null || !(te instanceof TileEntityScreen)) {
            Log.error("TileEntity at %s is not a screen; can't open keyboard!", pos.toString());
            return null;
        }

        return new GuiKeyboard((TileEntityScreen) te, side, new BlockPos(kbX, kbY, kbZ));
    }

    @Override
    public String getName() {
        return "Keyboard";
    }

}
