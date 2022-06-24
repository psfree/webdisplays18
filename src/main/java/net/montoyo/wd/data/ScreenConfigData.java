/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.data;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.client.gui.GuiScreenConfig;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.net.client.CMessageOpenGui;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.utilities.NameUUIDPair;
import net.montoyo.wd.utilities.Vector3i;

public class ScreenConfigData extends GuiData {

    public boolean onlyUpdate;
    public Vector3i pos;
    public BlockSide side;
    public NameUUIDPair[] friends;
    public int friendRights;
    public int otherRights;

    public ScreenConfigData() {
    }

    public ScreenConfigData(Vector3i pos, BlockSide side, TileEntityScreen.Screen scr) {
        this.pos = pos;
        this.side = side;
        friends = scr.friends.toArray(new NameUUIDPair[0]);
        friendRights = scr.friendRights;
        otherRights = scr.otherRights;
        onlyUpdate = false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Screen createGui(Screen old, Level world) {
        if(old != null && old instanceof GuiScreenConfig) {
            GuiScreenConfig gsc = (GuiScreenConfig) old;

            if(gsc.isForBlock(pos.toBlock(), side)) {
                gsc.updateFriends(friends);
                gsc.updateFriendRights(friendRights);
                gsc.updateOtherRights(otherRights);
                gsc.updateMyRights();

                return null;
            }
        }

        if(onlyUpdate)
            return null;

        BlockEntity te = world.getBlockEntity(pos.toBlock());
        if(te == null || !(te instanceof TileEntityScreen)) {
            Log.error("TileEntity at %s is not a screen; can't open gui!", pos.toString());
            return null;
        }

        return new GuiScreenConfig(Component.nullToEmpty(""), (TileEntityScreen) te, side, friends, friendRights, otherRights);
    }

    @Override
    public String getName() {
        return "ScreenConfig";
    }

    public ScreenConfigData updateOnly() {
        onlyUpdate = true;
        return this;
    }

    public void sendTo(PacketDistributor.TargetPoint tp) {
        Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> tp), new CMessageOpenGui(this));
    }

}
