/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.utilities.Vector3i;

import java.util.function.Supplier;

public class SMessageRequestTEData implements Runnable {

    private ResourceLocation dim;
    private Vector3i pos;
    private ServerPlayer player;

    public SMessageRequestTEData() {
    }

    public SMessageRequestTEData(BlockEntity te) {
        dim = te.getLevel().dimension().location();
        pos = new Vector3i(te.getBlockPos());
    }

    public SMessageRequestTEData(ResourceLocation dim, Vector3i pos) {
        this.dim = dim;
        this.pos = pos;
    }

    public static SMessageRequestTEData decode(FriendlyByteBuf buf) {
        return new SMessageRequestTEData(buf.readResourceLocation(), new Vector3i(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dim);
        pos.writeTo(buf);
    }

    @Override
    public void run() {
        if(!player.level.dimension().location().equals(dim))
            return;

        BlockPos bp = pos.toBlock();
        if(player.distanceToSqr(bp.getX(), bp.getY(), bp.getZ()) > 512.0 * 512.0)
            return;

        BlockEntity te = player.level.getBlockEntity(bp);
        if(te == null) {
            Log.error("MesageRequestTEData: Can't request data of null tile entity at %s", pos.toString());
            return;
        }

        if(te instanceof TileEntityScreen)
            ((TileEntityScreen) te).requestData(player);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        player = contextSupplier.get().getSender();
        contextSupplier.get().enqueueWork(this);
    }
}
