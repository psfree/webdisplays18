/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.data.SetURLData;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Util;

public class TileEntityRCtrl extends TileEntityPeripheralBase {

    public TileEntityRCtrl(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
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

        (new SetURLData(screenPos, screenSide, scr.url, getBlockPos())).sendTo((ServerPlayer) player);
        return true;
    }

}
