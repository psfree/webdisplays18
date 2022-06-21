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
import net.montoyo.wd.client.gui.GuiSetURL2;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.utilities.Vector3i;

public class SetURLData extends GuiData {

    public Vector3i pos;
    public BlockSide side;
    public String url;
    public boolean isRemote;
    public Vector3i remoteLocation;

    public SetURLData() {
    }

    public SetURLData(Vector3i pos, BlockSide side, String url) {
        this.pos = pos;
        this.side = side;
        this.url = url;
        isRemote = false;
        remoteLocation = new Vector3i();
    }

    public SetURLData(Vector3i pos, BlockSide side, String url, BlockPos rl) {
        this.pos = pos;
        this.side = side;
        this.url = url;
        isRemote = true;
        remoteLocation = new Vector3i(rl);
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public Screen createGui(Screen old, Level world) {
        BlockEntity te = world.getBlockEntity(pos.toBlock());
        if(te == null || !(te instanceof TileEntityScreen)) {
            Log.error("TileEntity at %s is not a screen; can't open gui!", pos.toString());
            return null;
        }

        return new GuiSetURL2((TileEntityScreen) te, side, url, isRemote ? remoteLocation : null);
    }

    @Override
    public String getName() {
        return "SetURL";
    }

}
