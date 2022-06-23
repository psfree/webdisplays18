/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.data.RedstoneCtrlData;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Util;

import javax.annotation.Nonnull;

public class TileEntityRedCtrl extends TileEntityPeripheralBase {

    private String risingEdgeURL = "";
    private String fallingEdgeURL = "";
    private boolean state = false;

    public TileEntityRedCtrl(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);

        risingEdgeURL = tag.getString("RisingEdgeURL");
        fallingEdgeURL = tag.getString("FallingEdgeURL");
        state = tag.getBoolean("Powered");
    }

    @Override
    @Nonnull
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        super.serializeNBT();

        tag.putString("RisingEdgeURL", risingEdgeURL);
        tag.putString("FallingEdgeURL", fallingEdgeURL);
        tag.putBoolean("Powered", state);
        return tag;
    }

    @Override
    public boolean onRightClick(Player player, InteractionHand hand, BlockSide side) {
        if(level.isClientSide)
            return true;

        if(!isScreenChunkLoaded()) {
            Util.toast(player, "chunkUnloaded");
            return true;
        }

        TileEntityScreen tes = getConnectedScreen();
        if(tes == null) {
            Util.toast(player, "notLinked");
            return true;
        }

        TileEntityScreen.Screen scr = tes.getScreen(screenSide);
        if((scr.rightsFor(player) & ScreenRights.CHANGE_URL) == 0) {
            Util.toast(player, "restrictions");
            return true;
        }

        (new RedstoneCtrlData(level.dimension().location(), getBlockPos(), risingEdgeURL, fallingEdgeURL)).sendTo((ServerPlayer) player);
        return true;
    }

    @Override
    public void onNeighborChange(Block neighborType, BlockPos neighborPos) {
        boolean hasPower = (level.hasNeighborSignal(getBlockPos()) || level.hasNeighborSignal(getBlockPos().above())); //Same as dispenser

        if(hasPower != state) {
            state = hasPower;

            if(state) //Rising edge
                changeURL(risingEdgeURL);
            else //Falling edge
                changeURL(fallingEdgeURL);
        }
    }

    public void setURLs(String r, String f) {
        risingEdgeURL = r.trim();
        fallingEdgeURL = f.trim();
        setChanged();
    }

    private void changeURL(String url) {
        if(level.isClientSide || url.isEmpty())
            return;

        if(isScreenChunkLoaded()) {
            TileEntityScreen tes = getConnectedScreen();

            if(tes != null)
                tes.setScreenURL(screenSide, url);
        }
    }

}
