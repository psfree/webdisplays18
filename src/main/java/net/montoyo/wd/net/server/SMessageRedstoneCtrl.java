/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.entity.TileEntityRedCtrl;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.net.Message;
import net.montoyo.wd.utilities.Util;
import net.montoyo.wd.utilities.Vector3i;

import java.util.function.Supplier;

public class SMessageRedstoneCtrl implements Runnable {

    private Player player;
    private ResourceLocation dimension;
    private Vector3i pos;
    private String risingEdgeURL;
    private String fallingEdgeURL;

    public SMessageRedstoneCtrl() {
    }

    public SMessageRedstoneCtrl(ResourceLocation d, Vector3i p, String r, String f) {
        dimension = d;
        pos = p;
        risingEdgeURL = r;
        fallingEdgeURL = f;
    }

    @Override
    public void run() {
        Level world = player.level;
        BlockPos blockPos = pos.toBlock();
        final double maxRange = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();

        if(!world.dimension().location().equals(dimension) || player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()) > maxRange * maxRange)
            return;

        BlockEntity te = world.getBlockEntity(blockPos);
        if(te == null || !(te instanceof TileEntityRedCtrl))
            return;

        TileEntityRedCtrl redCtrl = (TileEntityRedCtrl) te;
        if(!redCtrl.isScreenChunkLoaded()) {
            Util.toast(player, "chunkUnloaded");
            return;
        }

        TileEntityScreen tes = redCtrl.getConnectedScreen();
        if(tes == null)
            return;

        if((tes.getScreen(redCtrl.getScreenSide()).rightsFor(player) & ScreenRights.CHANGE_URL) == 0)
            return;

        redCtrl.setURLs(risingEdgeURL, fallingEdgeURL);
    }

    public static SMessageRedstoneCtrl decode(FriendlyByteBuf buf) {
        return new SMessageRedstoneCtrl(buf.readResourceLocation(), new Vector3i(buf), buf.readUtf(), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension);
        pos.writeTo(buf);
        buf.writeUtf(risingEdgeURL);
        buf.writeUtf(fallingEdgeURL);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        player = contextSupplier.get().getSender();
        contextSupplier.get().enqueueWork(this);
    }
}
