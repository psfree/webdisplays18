/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.client_bound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.net.Packet;
import net.montoyo.wd.utilities.*;

import java.io.IOException;
import java.util.ArrayList;

import static net.montoyo.wd.block.BlockScreen.hasTE;

public class S2CMessageAddScreen extends Packet {
	private boolean clear;
	private Vector3i pos;
	private TileEntityScreen.Screen[] screens;
	
	public S2CMessageAddScreen(TileEntityScreen tes) {
		clear = true;
		pos = new Vector3i(tes.getBlockPos());
		screens = new TileEntityScreen.Screen[tes.screenCount()];
		
		for (int i = 0; i < tes.screenCount(); i++)
			screens[i] = tes.getScreen(i);
	}
	
	public S2CMessageAddScreen(TileEntityScreen tes, TileEntityScreen.Screen... toSend) {
		clear = false;
		pos = new Vector3i(tes.getBlockPos());
		screens = toSend;
	}
	
	public S2CMessageAddScreen(boolean clear, Vector3i pos, TileEntityScreen.Screen[] screens) {
		this.clear = clear;
		this.pos = pos;
		this.screens = screens;
	}
	
	public S2CMessageAddScreen(FriendlyByteBuf buf) {
		super(buf);
		
		clear = buf.readBoolean();
		pos = new Vector3i(buf);
		
		int cnt = buf.readByte() & 7;
		
		screens = new TileEntityScreen.Screen[cnt];
		for (int i = 0; i < cnt; i++) {
			screens[i] = new TileEntityScreen.Screen();
			screens[i].side = BlockSide.values()[buf.readByte()];
			screens[i].size = new Vector2i(buf);
			screens[i].url = buf.readUtf();
			screens[i].resolution = new Vector2i(buf);
			screens[i].rotation = Rotation.values()[buf.readByte() & 3];
			screens[i].owner = new NameUUIDPair(buf);
			screens[i].upgrades = new ArrayList<>();
			
			int numUpgrades = buf.readByte();
			for (int j = 0; j < numUpgrades; j++)
				screens[i].upgrades.add(buf.readItem());
		}
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(clear);
		pos.writeTo(buf);
		buf.writeByte(screens.length);
		
		for (TileEntityScreen.Screen scr : screens) {
			buf.writeByte(scr.side.ordinal());
			scr.size.writeTo(buf);
			buf.writeUtf(scr.url);
			scr.resolution.writeTo(buf);
			buf.writeByte(scr.rotation.ordinal());
			scr.owner.writeTo(buf);
			buf.writeByte(scr.upgrades.size());
			
			for (ItemStack is : scr.upgrades)
				buf.writeItem(is);
		}
	}
	
	public void handle(NetworkEvent.Context ctx) {
		if (checkClient(ctx)) {
			ctx.enqueueWork(() -> {
				Level lvl = (Level) WebDisplays.PROXY.getWorld(ctx);
				BlockEntity te = lvl.getBlockEntity(pos.toBlock());
				if (!(te instanceof TileEntityScreen)) {
					lvl.setBlockAndUpdate(pos.toBlock(), lvl.getBlockState(pos.toBlock()).setValue(hasTE, true));
					te = lvl.getBlockEntity(pos.toBlock());
					
					if (!(te instanceof TileEntityScreen)) {
						if (clear)
							Log.error("CMessageAddScreen: Can't add screen to invalid tile entity at %s", pos.toString());
						
						return;
					}
				}
				
				TileEntityScreen tes = (TileEntityScreen) te;
				if (clear)
					tes.clear();
				
				for (TileEntityScreen.Screen entry : screens) {
					TileEntityScreen.Screen scr = tes.addScreen(entry.side, entry.size, entry.resolution, null, false);
					scr.rotation = entry.rotation;
					String webUrl;
					
					try {
						webUrl = TileEntityScreen.url(entry.url);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					
					scr.url = webUrl;
					scr.owner = entry.owner;
					scr.upgrades = entry.upgrades;
					
					if (scr.browser != null)
						scr.browser.loadURL(webUrl);
				}
			});
			
			ctx.setPacketHandled(true);
		}
	}
}
