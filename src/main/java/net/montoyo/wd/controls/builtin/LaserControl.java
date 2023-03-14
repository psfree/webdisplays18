package net.montoyo.wd.controls.builtin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.controls.ScreenControl;
import net.montoyo.wd.core.MissingPermissionException;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Vector2i;

import java.util.function.Function;

public class LaserControl extends ScreenControl {
	public static final ResourceLocation id = new ResourceLocation("webdisplays:laser");
	
	public static enum ControlType {
		MOVE, DOWN, UP
	}
	
	ControlType type;
	Vector2i coord;
	
	public LaserControl(ControlType type, Vector2i coord) {
		super(id);
		this.type = type;
		this.coord = coord;
	}
	
	public LaserControl(FriendlyByteBuf buf) {
		super(id);
		type = ControlType.values()[buf.readByte()];
		if (!type.equals(ControlType.UP))
			coord = new Vector2i(buf);
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeByte(type.ordinal());
		if (coord != null) coord.writeTo(buf);
	}
	
	@Override
	public void handleServer(BlockPos pos, BlockSide side, TileEntityScreen tes, NetworkEvent.Context ctx, Function<Integer, Boolean> permissionChecker) throws MissingPermissionException {
		// feel like this makes sense, but I wanna get opinions first
//		checkPerms(ScreenRights.INTERACT, permissionChecker, ctx.getSender());
		ServerPlayer sender = ctx.getSender();
		switch (type) {
			case UP -> tes.laserUp(side, sender);
			case DOWN -> tes.laserDownMove(side, sender, coord, true);
			case MOVE -> tes.laserDownMove(side, sender, coord, false);
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleClient(BlockPos pos, BlockSide side, TileEntityScreen tes, NetworkEvent.Context ctx) {
		throw new RuntimeException("TODO");
	}
}
