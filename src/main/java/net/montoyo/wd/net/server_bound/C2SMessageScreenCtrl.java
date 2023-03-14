/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.server_bound;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.controls.ScreenControl;
import net.montoyo.wd.controls.ScreenControlRegistry;
import net.montoyo.wd.controls.builtin.*;
import net.montoyo.wd.core.JSServerRequest;
import net.montoyo.wd.core.MissingPermissionException;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.net.BufferUtils;
import net.montoyo.wd.net.Packet;
import net.montoyo.wd.utilities.*;

// TODO: this is a mess; a registry based approach would likely be more readable
public class C2SMessageScreenCtrl extends Packet {
	@Deprecated(forRemoval = true)
	public static final int CTRL_LASER_MOVE = 0;
	@Deprecated(forRemoval = true)
	public static final int CTRL_LASER_UP = 0;
	@Deprecated(forRemoval = true)
	public static final int CTRL_LASER_DOWN = 0;
	@Deprecated(forRemoval = true)
	public static final int CTRL_SET_RESOLUTION = 0;
	
	ScreenControl control;
	BlockPos pos;
	BlockSide side;
	
	public C2SMessageScreenCtrl() {
	}
	
	public C2SMessageScreenCtrl(TileEntityScreen screen, BlockSide side, ScreenControl control) {
		this.pos = screen.getBlockPos();
		this.side = side;
		this.control = control;
	}
	
	protected static C2SMessageScreenCtrl base(TileEntityScreen screen, BlockSide side) {
		C2SMessageScreenCtrl packet = new C2SMessageScreenCtrl();
		packet.pos = screen.getBlockPos();
		packet.side = side;
		return packet;
	}
	
	@Deprecated(forRemoval = true)
	public static C2SMessageScreenCtrl setURL(TileEntityScreen tes, BlockSide side, String url, Vector3i remoteLocation) {
		C2SMessageScreenCtrl ret = base(tes, side);
		ret.control = new SetURLControl(url, remoteLocation);
		return ret;
	}
	
	@Deprecated(forRemoval = true)
	public C2SMessageScreenCtrl(TileEntityScreen tes, BlockSide side, NameUUIDPair friend, boolean del) {
		this(tes, side, new ModifyFriendListControl(friend, !del));
	}
	
	@Deprecated(forRemoval = true)
	public C2SMessageScreenCtrl(TileEntityScreen tes, BlockSide side, int fr, int or) {
		this(tes, side, new ManageRightsAndUpdgradesControl(fr, or));
	}
	
	@Deprecated(forRemoval = true)
	public C2SMessageScreenCtrl(TileEntityScreen tes, BlockSide side, ItemStack toRem) {
		this(tes, side, new ManageRightsAndUpdgradesControl(toRem));
	}
	
	@Deprecated(forRemoval = true)
	public C2SMessageScreenCtrl(TileEntityScreen tes, BlockSide side, Rotation rot) {
		this(tes, side, new ScreenModifyControl(rot));
	}
	
	@Deprecated(forRemoval = true)
	public static C2SMessageScreenCtrl vec2(TileEntityScreen tes, BlockSide side, int ctrl, Vector2i vec) {
		throw new RuntimeException("Moved: look into ScreenControlRegistry");
	}
	
	@Deprecated(forRemoval = true)
	public static C2SMessageScreenCtrl resolution(TileEntityScreen tes, BlockSide side, Vector2i vec) {
		C2SMessageScreenCtrl ret = base(tes, side);
		ret.control = new ScreenModifyControl(vec);
		return ret;
	}
	
	@Deprecated(forRemoval = true)
	public static C2SMessageScreenCtrl type(TileEntityScreen tes, BlockSide side, String text, BlockPos soundPos) {
		C2SMessageScreenCtrl ret = base(tes, side);
		ret.control = new KeyTypedControl(text, soundPos);
		return ret;
	}
	
	public static C2SMessageScreenCtrl laserMove(TileEntityScreen tes, BlockSide side, Vector2i vec) {
		C2SMessageScreenCtrl ret = base(tes, side);
		ret.control = new LaserControl(LaserControl.ControlType.MOVE, vec);
		return ret;
	}
	
	public static C2SMessageScreenCtrl laserDown(TileEntityScreen tes, BlockSide side, Vector2i vec) {
		C2SMessageScreenCtrl ret = base(tes, side);
		ret.control = new LaserControl(LaserControl.ControlType.MOVE, vec);
		return ret;
	}
	
	@Deprecated(forRemoval = true)
	public static C2SMessageScreenCtrl laserUp(TileEntityScreen tes, BlockSide side) {
		C2SMessageScreenCtrl ret = base(tes, side);
		ret.control = new LaserControl(LaserControl.ControlType.UP, null);
		return ret;
	}
	
	@Deprecated(forRemoval = true)
	public static C2SMessageScreenCtrl jsRequest(TileEntityScreen tes, BlockSide side, int reqId, JSServerRequest reqType, Object... data) {
		C2SMessageScreenCtrl ret = base(tes, side);
		ret.control = new JSRequestControl(reqId, reqType, data);
		return ret;
	}
	
	@Deprecated(forRemoval = true)
	public static C2SMessageScreenCtrl autoVol(TileEntityScreen tes, BlockSide side, boolean av) {
		C2SMessageScreenCtrl ret = base(tes, side);
		ret.control = new AutoVolumeControl(av);
		return ret;
	}
	
	public C2SMessageScreenCtrl(FriendlyByteBuf buf) {
		super(buf);
		
		pos = buf.readBlockPos();
		side = (BlockSide) BufferUtils.readEnum(buf, (i) -> BlockSide.values()[i], (byte) 1);
		
		this.control = ScreenControlRegistry.parse(buf);
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(pos);
		BufferUtils.writeEnum(buf, side, (byte) 1);
		
		buf.writeUtf(control.getId().toString());
		control.write(buf);
	}
	
	public void checkPermission(ServerPlayer sender, TileEntityScreen scr, int right) throws MissingPermissionException {
		int prights = scr.getScreen(side).rightsFor(sender);
		if ((prights & right) == 0)
			throw new MissingPermissionException(right, sender);
	}
	
	public void handle(NetworkEvent.Context ctx) {
		if (checkServer(ctx)) {
			ctx.enqueueWork(() -> {
				try {
					Level level = (Level) WebDisplays.PROXY.getWorld(ctx);
					BlockEntity be = level.getBlockEntity(pos);
					if (be instanceof TileEntityScreen tes) {
						control.handleServer(pos, side, tes, ctx, (perm) -> {
							try {
								checkPermission(ctx.getSender(), tes, perm);
								return true;
							} catch (Throwable ignored) {
								return false;
							}
						});
					}
				} catch (MissingPermissionException e) {
				}
			});
			ctx.setPacketHandled(true);
		}
	}
}
