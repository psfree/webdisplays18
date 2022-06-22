/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.client;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.utilities.BlockSide;

import java.util.Arrays;
import java.util.function.Supplier;

public class CMessageCloseGui {

    private BlockPos blockPos;
    private BlockSide blockSide;

    public CMessageCloseGui(BlockPos bp) {
        blockPos = bp;
        blockSide = null;
    }

    public CMessageCloseGui(BlockPos bp, BlockSide side) {
        blockPos = bp;
        blockSide = side;
    }

    public void decode(FriendlyByteBuf buf) {
        int x, y, z, side;
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        side = buf.readByte();

        blockPos = new BlockPos(x, y, z);
        if(side <= 0)
            blockSide = null;
        else
            blockSide = BlockSide.values()[side - 1];
    }

    public CMessageCloseGui encode(FriendlyByteBuf buf) {
        buf.writeInt(blockPos.getX());
        buf.writeInt(blockPos.getY());
        buf.writeInt(blockPos.getZ());

        if(blockSide == null) {
            buf.writeByte(0);
        } else {
            buf.writeByte(blockSide.ordinal() + 1);
        }

        return this;
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            if (blockSide == null)
                Arrays.stream(BlockSide.values()).forEach(s -> WebDisplays.PROXY.closeGui(blockPos, s));
            else
                WebDisplays.PROXY.closeGui(blockPos, blockSide);
        });
    }
}
