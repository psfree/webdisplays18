/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Supplier;

public class CMessageScreenUpdate {

    public static final int UPDATE_URL = 0;
    public static final int UPDATE_RESOLUTION = 1;
    public static final int UPDATE_DELETE = 2;
    public static final int UPDATE_MOUSE = 3;
    public static final int UPDATE_TYPE = 4;
    public static final int UPDATE_UPGRADES = 5;
    public static final int UPDATE_JS_REDSTONE = 6;
    public static final int UPDATE_OWNER = 7;
    public static final int UPDATE_ROTATION = 8;
    public static final int UPDATE_RUN_JS = 9;
    public static final int UPDATE_AUTO_VOL = 10;

    public static final int MOUSE_CLICK = 0;
    public static final int MOUSE_UP = 1;
    public static final int MOUSE_MOVE = 2;
    public static final int MOUSE_DOWN = 3;

    private Vector3i pos;
    private BlockSide side;
    private int action;
    private String string;
    private Vector2i vec2i;
    private int mouseEvent;
    private ItemStack[] upgrades;
    private int redstoneLevel;
    private NameUUIDPair owner;
    private Rotation rotation;
    private boolean autoVolume;

    public CMessageScreenUpdate() {
    }

    public static CMessageScreenUpdate setURL(TileEntityScreen tes, BlockSide side, String url) {
        CMessageScreenUpdate ret = new CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_URL;
        ret.string = url;

        return ret;
    }

    public static CMessageScreenUpdate setResolution(TileEntityScreen tes, BlockSide side, Vector2i res) {
        CMessageScreenUpdate ret = new CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_RESOLUTION;
        ret.vec2i = res;

        return ret;
    }

    public static CMessageScreenUpdate click(TileEntityScreen tes, BlockSide side, int mouseEvent, @Nullable Vector2i pos) {
        CMessageScreenUpdate ret = new CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_MOUSE;
        ret.mouseEvent = mouseEvent;
        ret.vec2i = pos;

        return ret;
    }

    public CMessageScreenUpdate(TileEntityScreen tes, BlockSide side) {
        pos = new Vector3i(tes.getBlockPos());
        this.side = side;
        action = UPDATE_DELETE;
    }

    public static CMessageScreenUpdate type(TileEntityScreen tes, BlockSide side, String text) {
        CMessageScreenUpdate ret = new CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.string = text;
        ret.action = UPDATE_TYPE;

        return ret;
    }

    public static CMessageScreenUpdate js(TileEntityScreen tes, BlockSide side, String code) {
        CMessageScreenUpdate ret = new CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.string = code;
        ret.action = UPDATE_RUN_JS;

        return ret;
    }

    public static CMessageScreenUpdate upgrade(TileEntityScreen tes, BlockSide side) {
        CMessageScreenUpdate ret = new CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_UPGRADES;

        ArrayList<ItemStack> upgrades = tes.getScreen(side).upgrades;
        ret.upgrades = new ItemStack[upgrades.size()];

        for(int i = 0; i < upgrades.size(); i++)
            ret.upgrades[i] = upgrades.get(i).copy();

        return ret;
    }

    public static CMessageScreenUpdate jsRedstone(TileEntityScreen tes, BlockSide side, Vector2i vec, int level) {
        CMessageScreenUpdate ret = new CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_JS_REDSTONE;
        ret.vec2i = vec;
        ret.redstoneLevel = level;

        return ret;
    }

    public static CMessageScreenUpdate owner(TileEntityScreen tes, BlockSide side, NameUUIDPair owner) {
        CMessageScreenUpdate ret = new CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_OWNER;
        ret.owner = owner;

        return ret;
    }

    public static CMessageScreenUpdate rotation(TileEntityScreen tes, BlockSide side, Rotation rot) {
        CMessageScreenUpdate ret = new CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_ROTATION;
        ret.rotation = rot;

        return ret;
    }

    public static CMessageScreenUpdate autoVolume(TileEntityScreen tes, BlockSide side, boolean av) {
        CMessageScreenUpdate ret = new CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_AUTO_VOL;
        ret.autoVolume = av;

        return ret;
    }

    public static CMessageScreenUpdate decode(FriendlyByteBuf buf) {
        Vector3i pos = new Vector3i(buf);
        BlockSide side = BlockSide.values()[buf.readByte()];
        byte action = buf.readByte();

        CMessageScreenUpdate message = new CMessageScreenUpdate();
        message.pos = pos;
        message.side = side;
        message.action = action;

        switch (action) {
            case UPDATE_URL, UPDATE_TYPE, UPDATE_RUN_JS -> message.string = buf.readUtf();
            case UPDATE_MOUSE -> {
                message.mouseEvent = buf.readByte();
                if (message.mouseEvent != MOUSE_UP)
                    message.vec2i = new Vector2i(buf);
            }
            case UPDATE_RESOLUTION -> message.vec2i = new Vector2i(buf);
            case UPDATE_UPGRADES -> {
                message.upgrades = new ItemStack[buf.readByte()];
                for (int i = 0; i < message.upgrades.length; i++)
                    message.upgrades[i] = buf.readItem();
            }
            case UPDATE_JS_REDSTONE -> {
                message.vec2i = new Vector2i(buf);
                message.redstoneLevel = buf.readByte();
            }
            case UPDATE_OWNER -> message.owner = new NameUUIDPair(buf);
            case UPDATE_ROTATION -> message.rotation = Rotation.values()[buf.readByte() & 3];
            case UPDATE_AUTO_VOL -> message.autoVolume = buf.readBoolean();
        }

        return message;
    }


    public CMessageScreenUpdate encode(FriendlyByteBuf buf) {
        pos.writeTo(buf);
        buf.writeByte(side.ordinal());
        buf.writeByte(action);

        if(action == UPDATE_URL || action == UPDATE_TYPE || action == UPDATE_RUN_JS)
            buf.writeUtf(string);
        else if(action == UPDATE_MOUSE) {
            buf.writeByte(mouseEvent);

            if(mouseEvent != MOUSE_UP)
                vec2i.writeTo(buf);
        } else if(action == UPDATE_RESOLUTION)
            vec2i.writeTo(buf);
        else if(action == UPDATE_UPGRADES) {
            buf.writeByte(upgrades.length);

            for(ItemStack is: upgrades)
                buf.writeItem(is);
        } else if(action == UPDATE_JS_REDSTONE) {
            vec2i.writeTo(buf);
            buf.writeByte(redstoneLevel);
        } else if(action == UPDATE_OWNER)
            owner.writeTo(buf);
        else if(action == UPDATE_ROTATION)
            buf.writeByte(rotation.ordinal());
        else if(action == UPDATE_AUTO_VOL)
            buf.writeBoolean(autoVolume);
        return new CMessageScreenUpdate();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
        BlockEntity te = WebDisplays.PROXY.getWorld(Level.OVERWORLD).getBlockEntity(pos.toBlock());
        if(!(te instanceof TileEntityScreen)) {
            Log.error("CMessageScreenUpdate: TileEntity at %s is not a screen!", pos.toString());
            return;
        }

        TileEntityScreen tes = (TileEntityScreen) te;

            switch (action) {
                case UPDATE_URL -> tes.setScreenURL(side, string);
                case UPDATE_MOUSE -> tes.handleMouseEvent(side, mouseEvent, vec2i);
                case UPDATE_DELETE -> tes.removeScreen(side);
                case UPDATE_RESOLUTION -> tes.setResolution(side, vec2i);
                case UPDATE_TYPE -> tes.type(side, string, null);
                case UPDATE_RUN_JS -> tes.evalJS(side, string);
                case UPDATE_UPGRADES -> tes.updateUpgrades(side, upgrades);
                case UPDATE_JS_REDSTONE -> tes.updateJSRedstone(side, vec2i, redstoneLevel);
                case UPDATE_OWNER -> {
                    TileEntityScreen.Screen scr = tes.getScreen(side);
                    if (scr != null)
                        scr.owner = owner;
                }
                case UPDATE_ROTATION -> tes.setRotation(side, rotation);
                case UPDATE_AUTO_VOL -> tes.setAutoVolume(side, autoVolume);
                default -> Log.warning("Caught invalid CMessageScreenUpdate with action ID %d", action);
            }
        });

        contextSupplier.get().setPacketHandled(true);
    }
}
