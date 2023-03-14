/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.net.client_bound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.block.BlockScreen;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.net.Packet;
import net.montoyo.wd.utilities.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;

// TODO: use registry based approach
public class S2CMessageScreenUpdate extends Packet  {
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
    
    public S2CMessageScreenUpdate() {
    }

    public static S2CMessageScreenUpdate setURL(TileEntityScreen tes, BlockSide side, String url) {
        S2CMessageScreenUpdate ret = new S2CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_URL;
        ret.string = url;

        return ret;
    }

    public static S2CMessageScreenUpdate setResolution(TileEntityScreen tes, BlockSide side, Vector2i res) {
        S2CMessageScreenUpdate ret = new S2CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_RESOLUTION;
        ret.vec2i = res;

        return ret;
    }

    public static S2CMessageScreenUpdate click(TileEntityScreen tes, BlockSide side, int mouseEvent, @Nullable Vector2i pos) {
        S2CMessageScreenUpdate ret = new S2CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_MOUSE;
        ret.mouseEvent = mouseEvent;
        ret.vec2i = pos;

        return ret;
    }

    public S2CMessageScreenUpdate(TileEntityScreen tes, BlockSide side) {
        pos = new Vector3i(tes.getBlockPos());
        this.side = side;
        action = UPDATE_DELETE;
    }

    public static S2CMessageScreenUpdate type(TileEntityScreen tes, BlockSide side, String text) {
        S2CMessageScreenUpdate ret = new S2CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.string = text;
        ret.action = UPDATE_TYPE;

        return ret;
    }

    public static S2CMessageScreenUpdate js(TileEntityScreen tes, BlockSide side, String code) {
        S2CMessageScreenUpdate ret = new S2CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.string = code;
        ret.action = UPDATE_RUN_JS;

        return ret;
    }

    public static S2CMessageScreenUpdate upgrade(TileEntityScreen tes, BlockSide side) {
        S2CMessageScreenUpdate ret = new S2CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_UPGRADES;

        ArrayList<ItemStack> upgrades = tes.getScreen(side).upgrades;
        ret.upgrades = new ItemStack[upgrades.size()];

        for(int i = 0; i < upgrades.size(); i++)
            ret.upgrades[i] = upgrades.get(i).copy();

        return ret;
    }

    public static S2CMessageScreenUpdate jsRedstone(TileEntityScreen tes, BlockSide side, Vector2i vec, int level) {
        S2CMessageScreenUpdate ret = new S2CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_JS_REDSTONE;
        ret.vec2i = vec;
        ret.redstoneLevel = level;

        return ret;
    }

    public static S2CMessageScreenUpdate owner(TileEntityScreen tes, BlockSide side, NameUUIDPair owner) {
        S2CMessageScreenUpdate ret = new S2CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_OWNER;
        ret.owner = owner;

        return ret;
    }

    public static S2CMessageScreenUpdate rotation(TileEntityScreen tes, BlockSide side, Rotation rot) {
        S2CMessageScreenUpdate ret = new S2CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_ROTATION;
        ret.rotation = rot;

        return ret;
    }

    public static S2CMessageScreenUpdate autoVolume(TileEntityScreen tes, BlockSide side, boolean av) {
        S2CMessageScreenUpdate ret = new S2CMessageScreenUpdate();
        ret.pos = new Vector3i(tes.getBlockPos());
        ret.side = side;
        ret.action = UPDATE_AUTO_VOL;
        ret.autoVolume = av;

        return ret;
    }
    
    public S2CMessageScreenUpdate(FriendlyByteBuf buf) {
        super(buf);
        
        Vector3i pos = new Vector3i(buf);
        BlockSide side = BlockSide.values()[buf.readByte()];
        byte action = buf.readByte();

        S2CMessageScreenUpdate message = this;
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
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
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
    }

    public void handle(NetworkEvent.Context ctx) {
        if (checkClient(ctx)) {
            ctx.enqueueWork(() -> {
                BlockGetter level = WebDisplays.PROXY.getWorld(ctx);
                if (level instanceof Level level1)
                    // ensure that the TE exists
                    level1.setBlock(
                            pos.toBlock(),
                            level.getBlockState(pos.toBlock()).setValue(BlockScreen.hasTE, true),11
                    );
                BlockEntity te = level.getBlockEntity(pos.toBlock());
                if(!(te instanceof TileEntityScreen)) {
                    Log.error("CMessageScreenUpdate: TileEntity at %s is not a screen!", pos.toString());
                    return;
                }
        
                TileEntityScreen tes = (TileEntityScreen) te;
        
                switch (action) {
                    case UPDATE_URL -> {
                        try {
                            tes.setScreenURL(side, string);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
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
    
            ctx.setPacketHandled(true);
        }
    }
}
