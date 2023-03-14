/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.server_bound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.entity.TileEntityRedCtrl;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.net.Packet;
import net.montoyo.wd.utilities.Util;
import net.montoyo.wd.utilities.Vector3i;

public class C2SMessageRedstoneCtrl extends Packet implements Runnable {
	private Player player;
	private Vector3i pos;
	private String risingEdgeURL;
	private String fallingEdgeURL;
	
	public C2SMessageRedstoneCtrl() {
	}
	
	public C2SMessageRedstoneCtrl(Vector3i p, String r, String f) {
		pos = p;
		risingEdgeURL = r;
		fallingEdgeURL = f;
	}
	
	public C2SMessageRedstoneCtrl(FriendlyByteBuf buf) {
		super(buf);
		pos = new Vector3i(buf);
		risingEdgeURL = buf.readUtf();
		fallingEdgeURL = buf.readUtf();
	}
	
	@Override
	public void run() {
		Level world = player.level;
		BlockPos blockPos = pos.toBlock();
		final double maxRange = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
		
		if (player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()) > maxRange * maxRange)
			return;
		
		BlockEntity te = world.getBlockEntity(blockPos);
		if (te == null || !(te instanceof TileEntityRedCtrl))
			return;
		
		TileEntityRedCtrl redCtrl = (TileEntityRedCtrl) te;
		if (!redCtrl.isScreenChunkLoaded()) {
			Util.toast(player, "chunkUnloaded");
			return;
		}
		
		TileEntityScreen tes = redCtrl.getConnectedScreen();
		if (tes == null)
			return;
		
		if ((tes.getScreen(redCtrl.getScreenSide()).rightsFor(player) & ScreenRights.CHANGE_URL) == 0)
			return;
		
		redCtrl.setURLs(risingEdgeURL, fallingEdgeURL);
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		pos.writeTo(buf);
		buf.writeUtf(risingEdgeURL);
		buf.writeUtf(fallingEdgeURL);
	}
	
	public void handle(NetworkEvent.Context ctx) {
		if (checkServer(ctx)) {
			player = ctx.getSender();
			ctx.enqueueWork(this);
			ctx.setPacketHandled(true);
		}
	}
}
