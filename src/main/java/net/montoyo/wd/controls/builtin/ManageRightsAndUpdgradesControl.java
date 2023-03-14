package net.montoyo.wd.controls.builtin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.controls.ScreenControl;
import net.montoyo.wd.core.MissingPermissionException;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;

import java.util.function.Function;

/**
 * TODO: I'm considering merging this with {@link ModifyFriendListControl} to make ManageScreenControl
 */
@Deprecated
public class ManageRightsAndUpdgradesControl extends ScreenControl {
	public static final ResourceLocation id = new ResourceLocation("webdisplays:mod_rights_upgrades");
	
	public static enum ControlType {
		RIGHTS, UPGRADES
	}
	
	ControlType type;
	ItemStack toRemove;
	
	private int friendRights;
	private int otherRights;
	
	public ManageRightsAndUpdgradesControl(ItemStack toRemove) {
		super(id);
		type = ControlType.UPGRADES;
		this.toRemove = toRemove;
	}
	
	public ManageRightsAndUpdgradesControl(int friendRights, int otherRights) {
		super(id);
		this.friendRights = friendRights;
		this.otherRights = otherRights;
	}
	
	public ManageRightsAndUpdgradesControl(FriendlyByteBuf buf) {
		super(id);
		type = ControlType.values()[buf.readByte()];
		switch (type) {
			case UPGRADES -> toRemove = buf.readItem();
			case RIGHTS -> {
				friendRights = buf.readInt();
				otherRights = buf.readInt();
			}
		}
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeByte(type.ordinal());
		switch (type) {
			case UPGRADES -> buf.writeItem(toRemove);
			case RIGHTS -> {
				buf.writeInt(friendRights);
				buf.writeInt(otherRights);
			}
		}
	}
	
	@Override
	public void handleServer(BlockPos pos, BlockSide side, TileEntityScreen tes, NetworkEvent.Context ctx, Function<Integer, Boolean> permissionChecker) throws MissingPermissionException {
		ServerPlayer player = ctx.getSender();
		switch (type) {
			case UPGRADES -> {
				checkPerms(ScreenRights.MANAGE_UPGRADES, permissionChecker, ctx.getSender());
				tes.removeUpgrade(side, toRemove, player);
			}
			case RIGHTS -> {
				TileEntityScreen.Screen scr = tes.getScreen(side);
				
				int fr = scr.owner.uuid.equals(player.getGameProfile().getId()) ? friendRights : scr.friendRights;
				int or = (scr.rightsFor(player) & ScreenRights.MANAGE_OTHER_RIGHTS) == 0 ? scr.otherRights : otherRights;
				
				if(scr.friendRights != fr || scr.otherRights != or)
					tes.setRights(player, side, fr, or);
			}
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleClient(BlockPos pos, BlockSide side, TileEntityScreen tes, NetworkEvent.Context ctx) {
		throw new RuntimeException("TODO");
	}
}
