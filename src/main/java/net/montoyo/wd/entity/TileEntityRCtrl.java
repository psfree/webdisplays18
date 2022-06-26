/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.data.SetURLData;
import net.montoyo.wd.init.TileInit;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Util;

public class TileEntityRCtrl extends TileEntityPeripheralBase {
    private static BlockPos blockPos;
    private static BlockState blockState;

    public TileEntityRCtrl(BlockPos arg2, BlockState arg3) {
            super(TileInit.REMOTE_CONTROLLER.get(), arg2, arg3);
            blockPos = arg2;
            blockState = arg3;
    }

    public static Block getBlockFromTE() {
        if(blockPos != null && blockState != null) {
            return new TileEntityKeyboard(blockPos, blockState).getBlockState().getBlock();
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public InteractionResult onRightClick(Player player, InteractionHand hand) {
        if(level.isClientSide)
            return InteractionResult.SUCCESS;

        if(!isScreenChunkLoaded()) {
            Util.toast(player, "chunkUnloaded");
            return InteractionResult.SUCCESS;
        }

        TileEntityScreen tes = getConnectedScreen();
        if(tes == null) {
            Util.toast(player, "notLinked");
            return InteractionResult.SUCCESS;
        }

        TileEntityScreen.Screen scr = tes.getScreen(screenSide);
        if((scr.rightsFor(player) & ScreenRights.CHANGE_URL) == 0) {
            Util.toast(player, "restrictions");
            return InteractionResult.SUCCESS;
        }

        (new SetURLData(screenPos, screenSide, scr.url, getBlockPos())).sendTo((ServerPlayer) player);
        return InteractionResult.SUCCESS;
    }

}
