/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import net.montoyo.mcef.api.IBrowser;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.block.BlockScreen;
import net.montoyo.wd.core.DefaultUpgrade;
import net.montoyo.wd.core.IUpgrade;
import net.montoyo.wd.core.JSServerRequest;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.data.ScreenConfigData;
import net.montoyo.wd.init.BlockInit;
import net.montoyo.wd.init.ItemInit;
import net.montoyo.wd.init.TileInit;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.net.client.CMessageAddScreen;
import net.montoyo.wd.net.client.CMessageCloseGui;
import net.montoyo.wd.net.client.CMessageJSResponse;
import net.montoyo.wd.net.client.CMessageScreenUpdate;
import net.montoyo.wd.net.server.SMessageRequestTEData;
import net.montoyo.wd.utilities.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

import static net.montoyo.wd.block.BlockPeripheral.point;

public class TileEntityScreen extends BlockEntity{

    public TileEntityScreen(BlockPos arg2, BlockState arg3) {
        super(TileInit.SCREEN_BLOCK_ENTITY.get(), arg2, arg3);
    }

    public static class Screen {

        public BlockSide side;
        public Vector2i size;
        public Vector2i resolution;
        public Rotation rotation = Rotation.ROT_0;
        public String url;
        private VideoType videoType;
        public NameUUIDPair owner;
        public ArrayList<NameUUIDPair> friends;
        public int friendRights;
        public int otherRights;
        public IBrowser browser;
        public ArrayList<ItemStack> upgrades;
        public boolean doTurnOnAnim;
        public long turnOnTime;
        public Player laserUser;
        public final Vector2i lastMousePos = new Vector2i();
        public NibbleArray redstoneStatus; //null on client
        public boolean autoVolume = true;

        public static Screen deserialize(CompoundTag tag) {
            Screen ret = new Screen();
            ret.side = BlockSide.values()[tag.getByte("Side")];
            ret.size = new Vector2i(tag.getInt("Width"), tag.getInt("Height"));
            ret.resolution = new Vector2i(tag.getInt("ResolutionX"), tag.getInt("ResolutionY"));
            ret.rotation = Rotation.values()[tag.getByte("Rotation")];
            ret.url = tag.getString("URL");
            ret.videoType = VideoType.getTypeFromURL(ret.url);

            if(ret.resolution.x <= 0 || ret.resolution.y <= 0) {
                float psx = ((float) ret.size.x) * 16.f - 4.f;
                float psy = ((float) ret.size.y) * 16.f - 4.f;
                psx *= 8.f; //TODO: Use ratio in config file
                psy *= 8.f;

                ret.resolution.x = (int) psx;
                ret.resolution.y = (int) psy;
            }

            if(tag.contains("OwnerName")) {
                String name = tag.getString("OwnerName");
                UUID uuid = tag.getUUID("OwnerUUID");
                ret.owner = new NameUUIDPair(name, uuid);
            }

            ListTag friends = tag.getList("Friends", 10);
            ret.friends = new ArrayList<>(friends.size());

            for(int i = 0; i < friends.size(); i++) {
                CompoundTag nf = friends.getCompound(i);
                NameUUIDPair pair = new NameUUIDPair(nf.getString("Name"), nf.getUUID("UUID"));
                ret.friends.add(pair);
            }

            ret.friendRights = tag.getByte("FriendRights");
            ret.otherRights = tag.getByte("OtherRights");

            ListTag upgrades = tag.getList("Upgrades", 10);
            ret.upgrades = new ArrayList<>();

            for(int i = 0; i < upgrades.size(); i++)
                ret.upgrades.add(new ItemStack((ItemLike) upgrades.getCompound(i)));

            if(tag.contains("AutoVolume"))
                ret.autoVolume = tag.getBoolean("AutoVolume");

            return ret;
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putByte("Side", (byte) side.ordinal());
            tag.putInt("Width", size.x);
            tag.putInt("Height", size.y);
            tag.putInt("ResolutionX", resolution.x);
            tag.putInt("ResolutionY", resolution.y);
            tag.putByte("Rotation", (byte) rotation.ordinal());
            tag.putString("URL", url);

            if(owner == null)
                Log.warning("Found TES with NO OWNER!!");
            else {
                tag.putString("OwnerName", owner.name);
                tag.putUUID("OwnerUUID", owner.uuid);
            }

            ListTag list = new ListTag();
            for(NameUUIDPair f: friends) {
                CompoundTag nf = new CompoundTag();
                nf.putString("Name", f.name);
                nf.putUUID("UUID", f.uuid);

                list.add(nf);
            }

            tag.put("Friends", list);
            tag.putByte("FriendRights", (byte) friendRights);
            tag.putByte("OtherRights", (byte) otherRights);

            list = new ListTag();
            for(ItemStack is: upgrades)
                list.add(is.save(new CompoundTag()));

            tag.put("Upgrades", list);
            tag.putBoolean("AutoVolume", autoVolume);
            return tag;
        }

        public int rightsFor(Player ply) {
            return rightsFor(ply.getGameProfile().getId());
        }

        public int rightsFor(UUID uuid) {
            if(owner.uuid.equals(uuid))
                return ScreenRights.ALL;

            return friends.stream().anyMatch(f -> f.uuid.equals(uuid)) ? friendRights : otherRights;
        }

        public void setupRedstoneStatus(Level world, BlockPos start) {
            if(world.isClientSide()) {
                Log.warning("Called Screen.setupRedstoneStatus() on client.");
                return;
            }

            if(redstoneStatus != null) {
                Log.warning("Called Screen.setupRedstoneStatus() on server, but redstone status is non-null");
                return;
            }

            Direction[] VALUES = Direction.values();
            redstoneStatus = new NibbleArray(size.x * size.y);
            final Direction facing = VALUES[side.reverse().ordinal()];
            final ScreenIterator it = new ScreenIterator(start, side, size);

            while(it.hasNext()) {
                int idx = it.getIndex();
                redstoneStatus.set(idx, world.getSignal(it.next(), facing));
            }
        }



        public void clampResolution() {
            if(resolution.x > WebDisplays.INSTANCE.maxResX) {
                float newY = ((float) resolution.y) * ((float) WebDisplays.INSTANCE.maxResX) / ((float) resolution.x);
                resolution.x = WebDisplays.INSTANCE.maxResX;
                resolution.y = (int) newY;
            }

            if(resolution.y > WebDisplays.INSTANCE.maxResY) {
                float newX = ((float) resolution.x) * ((float) WebDisplays.INSTANCE.maxResY) / ((float) resolution.y);
                resolution.x = (int) newX;
                resolution.y = WebDisplays.INSTANCE.maxResY;
            }
        }

    }

    public void forEachScreenBlocks(BlockSide side, Consumer<BlockPos> func) {
        Screen scr = getScreen(side);

        if(scr != null) {
            ScreenIterator it = new ScreenIterator(getBlockPos(), side, scr.size);

            while(it.hasNext())
                func.accept(it.next());
        }
    }

    private final ArrayList<Screen> screens = new ArrayList<>();
    private net.minecraft.world.phys.AABB renderBB = new net.minecraft.world.phys.AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private boolean loaded = true;
    public float ytVolume = Float.POSITIVE_INFINITY;

    public boolean isLoaded() {
        return loaded;
    }

    public void load() {
        loaded = true;
    }

    public void unload() {
        for(Screen scr: screens) {
            if(scr.browser != null) {
                scr.browser.close();
                scr.browser = null;
            }
        }

        loaded = false;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);

        ListTag list = tag.getList("WDScreens", 10);
        if(list.isEmpty())
            return;

        screens.clear();
        for(int i = 0; i < list.size(); i++)
            screens.add(Screen.deserialize(list.getCompound(i)));
    }

    @Override
    @Nonnull
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        super.serializeNBT();

        ListTag list = new ListTag();
        for(Screen scr: screens)
            list.add(scr.serialize());

        tag.put("WDScreens", list);
        return tag;
    }

    public Screen addScreen(BlockSide side, Vector2i size, @Nullable Vector2i resolution, @Nullable Player owner, boolean sendUpdate) {
        for(Screen scr: screens) {
            if(scr.side == side)
                return scr;
        }

        Screen ret = new Screen();
        ret.side = side;
        ret.size = size;
        ret.url = WebDisplays.INSTANCE.homePage;
        ret.friends = new ArrayList<>();
        ret.friendRights = ScreenRights.DEFAULTS;
        ret.otherRights = ScreenRights.DEFAULTS;
        ret.upgrades = new ArrayList<>();

        if(owner != null) {
            ret.owner = new NameUUIDPair(owner.getGameProfile());

            if(side == BlockSide.TOP || side == BlockSide.BOTTOM) {
                int rot = (int) Math.floor(((double) (owner.getYRot() * 4.0f / 360.0f)) + 2.5) & 3;

                if(side == BlockSide.TOP) {
                    if(rot == 1)
                        rot = 3;
                    else if(rot == 3)
                        rot = 1;
                }

                ret.rotation = Rotation.values()[rot];
            }
        }

        if(resolution == null || resolution.x < 1 || resolution.y < 1) {
            float psx = ((float) size.x) * 16.f - 4.f;
            float psy = ((float) size.y) * 16.f - 4.f;
            psx *= 8.f; //TODO: Use ratio in config file
            psy *= 8.f;

            ret.resolution = new Vector2i((int) psx, (int) psy);
        } else
            ret.resolution = resolution;

        ret.clampResolution();

        if(!level.isClientSide) {
            ret.setupRedstoneStatus(level, getBlockPos());

            if(sendUpdate)
                Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), new CMessageAddScreen(this, ret));
        }

        screens.add(ret);

        if(level.isClientSide)
            updateAABB();
        else
            setChanged();

        return ret;
    }

    public Screen getScreen(BlockSide side) {
        for(Screen scr: screens) {
            if(scr.side == side)
                return scr;
        }

        return null;
    }

    public int screenCount() {
        return screens.size();
    }

    public Screen getScreen(int idx) {
        return screens.get(idx);
    }

    public void clear() {
        screens.clear();

        if(!level.isClientSide)
            setChanged();
    }

    public void requestData(ServerPlayer ep) {
        if(!level.isClientSide)
            Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> ep), new CMessageAddScreen(this));
    }

    public void setScreenURL(BlockSide side, String url) {
        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Attempt to change URL of non-existing screen on side %s", side.toString());
            return;
        }

        url = WebDisplays.applyBlacklist(url);
        scr.url = url;
        scr.videoType = VideoType.getTypeFromURL(url);

        if(level.isClientSide) {
            if(scr.browser != null)
                scr.browser.loadURL(url);
        } else {
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.setURL(this, side, url));
            setChanged();
        }
    }

    public void removeScreen(BlockSide side) {
        int idx = -1;
        for(int i = 0; i < screens.size(); i++) {
            if(screens.get(i).side == side) {
                idx = i;
                break;
            }
        }

        if(idx < 0) {
            Log.error("Tried to delete non-existing screen on side %s", side.toString());
            return;
        }

        if(level.isClientSide) {
            if(screens.get(idx).browser != null) {
                screens.get(idx).browser.close();
                screens.get(idx).browser = null;
            }
        } else
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), new CMessageScreenUpdate(this, side)); //Delete the screen

        screens.remove(idx);

        if(!level.isClientSide) {
            if(screens.isEmpty()) //No more screens: remove tile entity
                level.setBlockAndUpdate(getBlockPos(), BlockInit.blockScreen.get().defaultBlockState().setValue(BlockScreen.hasTE, false));
            else
                setChanged();
        }
    }

    public void setResolution(BlockSide side, Vector2i res) {
        if(res.x < 1 || res.y < 1) {
            Log.warning("Call to TileEntityScreen.setResolution(%s) with suspicious values X=%d and Y=%d", side.toString(), res.x, res.y);
            return;
        }

        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Tried to change resolution of non-existing screen on side %s", side.toString());
            return;
        }

        scr.resolution = res;
        scr.clampResolution();

        if(level.isClientSide) {
            WebDisplays.PROXY.screenUpdateResolutionInGui(new Vector3i(getBlockPos()), side, res);

            if(scr.browser != null) {
                scr.browser.close();
                scr.browser = null; //Will be re-created by renderer
            }
        } else {
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.setResolution(this, side, res));
            setChanged();
        }
    }

    private static Player getLaserUser(Screen scr) {
        if(scr.laserUser != null) {
            if(scr.laserUser.isRemoved() || !scr.laserUser.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(ItemInit.itemLaserPointer.get()))
                scr.laserUser = null;
        }

        return scr.laserUser;
    }

    private static void checkLaserUserRights(Screen scr) {
        if(scr.laserUser != null && (scr.rightsFor(scr.laserUser) & ScreenRights.CLICK) == 0)
            scr.laserUser = null;
    }

    public void clearLaserUser(BlockSide side) {
        Screen scr = getScreen(side);

        if(scr != null)
            scr.laserUser = null;
    }

    public void click(BlockSide side, Vector2i vec) {
        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Attempt click non-existing screen of side %s", side.toString());
            return;
        }

        if(level.isClientSide)
            Log.warning("TileEntityScreen.click() from client side is useless...");
        else if(getLaserUser(scr) == null)
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.click(this, side, CMessageScreenUpdate.MOUSE_CLICK, vec));
    }

    void clickUnsafe(BlockSide side, int action, int x, int y) {
        if(level.isClientSide) {
            Vector2i vec = (action == CMessageScreenUpdate.MOUSE_UP) ? null : new Vector2i(x, y);
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.click(this, side, action, vec));
        }
    }

    public void handleMouseEvent(BlockSide side, int event, @Nullable Vector2i vec) {
        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Attempt inject mouse events on non-existing screen of side %s", side.toString());
            return;
        }

        if(scr.browser != null) {
            if(event == CMessageScreenUpdate.MOUSE_CLICK) {
                scr.browser.injectMouseMove(vec.x, vec.y, 0, false);                                            //Move to target
                scr.browser.injectMouseButton(vec.x, vec.y, 0, 1, true, 1);                              //Press
                scr.browser.injectMouseButton(vec.x, vec.y, 0, 1, false, 1);                             //Release
            } else if(event == CMessageScreenUpdate.MOUSE_DOWN) {
                scr.browser.injectMouseMove(vec.x, vec.y, 0, false);                                            //Move to target
                scr.browser.injectMouseButton(vec.x, vec.y, 0, 1, true, 1);                              //Press
            } else if(event == CMessageScreenUpdate.MOUSE_MOVE)
                scr.browser.injectMouseMove(vec.x, vec.y, 0, false);                                            //Move
            else if(event == CMessageScreenUpdate.MOUSE_UP)
                scr.browser.injectMouseButton(scr.lastMousePos.x, scr.lastMousePos.y, 0, 1, false, 1);  //Release

            if(vec != null) {
                scr.lastMousePos.x = vec.x;
                scr.lastMousePos.y = vec.y;
            }
        }
    }

    public void updateJSRedstone(BlockSide side, Vector2i vec, int redstoneLevel) {
        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Called updateJSRedstone on non-existing side %s", side.toString());
            return;
        }

        if(level.isClientSide) {
            if(scr.browser != null)
                scr.browser.runJS("if(typeof webdisplaysRedstoneCallback == \"function\") webdisplaysRedstoneCallback(" + vec.x + ", " + vec.y + ", " + redstoneLevel + ");", "");
        } else {
            boolean sendMsg = false;

            if(scr.redstoneStatus == null) {
                scr.setupRedstoneStatus(level, getBlockPos());
                sendMsg = true;
            } else {
                int idx = vec.y * scr.size.x + vec.x;

                if(scr.redstoneStatus.get(idx) != redstoneLevel) {
                    scr.redstoneStatus.set(idx, redstoneLevel);
                    sendMsg = true;
                }
            }

            if(sendMsg)
               Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.jsRedstone(this, side, vec, redstoneLevel));
        }
    }

    public void handleJSRequest(ServerPlayer src, BlockSide side, int reqId, JSServerRequest req, Object[] data) {
        if(level.isClientSide) {
            Log.error("Called handleJSRequest client-side");
            return;
        }

        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Called handleJSRequest on non-existing side %s", side.toString());
            Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> src), new CMessageJSResponse(reqId, req, 403, "Invalid side"));
            return;
        }

        if(!scr.owner.uuid.equals(src.getGameProfile().getId())) {
            Log.warning("Player %s (UUID %s) tries to use the redstone output API on a screen he doesn't own!", src.getName(), src.getGameProfile().getId().toString());
            Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> src), new CMessageJSResponse(reqId, req, 403, "Only the owner can do that"));
            return;
        }

        if(scr.upgrades.stream().noneMatch(DefaultUpgrade.REDSTONE_OUTPUT::matches)) {
            Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> src), new CMessageJSResponse(reqId, req, 403, "Missing upgrade"));
            return;
        }

        if(req == JSServerRequest.CLEAR_REDSTONE) {
            final BlockPos.MutableBlockPos mbp = new BlockPos.MutableBlockPos();
            final Vector3i vec1 = new Vector3i(getBlockPos());
            final Vector3i vec2 = new Vector3i();

            for(int y = 0; y < scr.size.y; y++) {
                vec2.set(vec1);

                for(int x = 0; x < scr.size.x; x++) {
                    vec2.toBlock(mbp);

                    BlockState bs = level.getBlockState(mbp);
                    if(bs.getValue(BlockScreen.emitting))
                        level.setBlock(mbp, bs.setValue(BlockScreen.emitting, false), Block.UPDATE_ALL_IMMEDIATE);

                    vec2.add(side.right.x, side.right.y, side.right.z);
                }

                vec1.add(side.up.x, side.up.y, side.up.z);
            }

            Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> src), new CMessageJSResponse(reqId, req, new byte[0]));
        } else if(req == JSServerRequest.SET_REDSTONE_AT) {
            int x = (Integer) data[0];
            int y = (Integer) data[1];
            boolean state = (Boolean) data[2];

            if(x < 0 || x >= scr.size.x || y < 0 || y >= scr.size.y)
                Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> src), new CMessageJSResponse(reqId, req, 403, "Out of range"));
            else {
                BlockPos bp = (new Vector3i(getBlockPos())).addMul(side.right, x).addMul(side.up, y).toBlock();
                BlockState bs = level.getBlockState(bp);

                if(!bs.getValue(BlockScreen.emitting).equals(state))
                    level.setBlockAndUpdate(bp, bs.setValue(BlockScreen.emitting, state));

                Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> src), new CMessageJSResponse(reqId, req, new byte[0]));
            }
        } else
            Messages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> src), new CMessageJSResponse(reqId, req, 400, "Invalid request"));
    }

    @Override
    public void onLoad() {
        if(level.isClientSide) {
            Messages.INSTANCE.sendToServer(new SMessageRequestTEData(this));
            WebDisplays.PROXY.trackScreen(this, true);
        }
    }

    @Override
    public void onChunkUnloaded() {
        if(level.isClientSide) {
            WebDisplays.PROXY.trackScreen(this, false);

            for(Screen scr: screens) {
                if(scr.browser != null) {
                    scr.browser.close();
                    scr.browser = null;
                }
            }
        }
    }

    private void updateAABB() {
        Vector3i origin = new Vector3i(getBlockPos());
        Vector3i tmp = new Vector3i();
        AABB aabb = new AABB(origin);

        for(Screen scr: screens) {
            tmp.set(origin);
            tmp.addMul(scr.side.right, scr.size.x);
            tmp.addMul(scr.side.up, scr.size.y);
            tmp.add(scr.side.forward);

            aabb.expand(tmp);
        }

        renderBB = aabb.toMc().expandTowards(0.1, 0.1, 0.1);
    }

    @Override
    @Nonnull
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return renderBB;
    }

    //FIXME: Not called if enableSoundDistance is false
    public void updateTrackDistance(double d, float masterVolume) {
        final WebDisplays wd = WebDisplays.INSTANCE;
        boolean needsComputation = true;
        int intPart = 0; //Need to initialize those because the compiler is stupid
        int fracPart = 0;

        for(Screen scr: screens) {
            if(scr.autoVolume && scr.videoType != null && scr.browser != null && !scr.browser.isPageLoading()) {
                if(needsComputation) {
                    float dist = (float) Math.sqrt(d);
                    float vol;

                    if(dist <= wd.avDist100)
                        vol = masterVolume * wd.ytVolume;
                    else if(dist >= wd.avDist0)
                        vol = 0.0f;
                    else
                        vol = (1.0f - (dist - wd.avDist100) / (wd.avDist0 - wd.avDist100)) * masterVolume * wd.ytVolume;

                    if(Math.abs(ytVolume - vol) < 0.5f)
                        return; //Delta is too small

                    ytVolume = vol;
                    intPart = (int) vol; //Manually convert to string, probably faster in that case...
                    fracPart = ((int) (vol * 100.0f)) - intPart * 100;
                    needsComputation = false;
                }

                scr.browser.runJS(scr.videoType.getVolumeJSQuery(intPart, fracPart), "");
            }
        }
    }

    public void updateClientSideURL(IBrowser target, String url) {
        for(Screen scr: screens) {
            if(scr.browser == target) {
                boolean blacklisted = WebDisplays.isSiteBlacklisted(url);
                scr.url = blacklisted ? WebDisplays.BLACKLIST_URL : url; //FIXME: This is an invalid fix for something that CANNOT be fixed
                scr.videoType = VideoType.getTypeFromURL(scr.url);
                ytVolume = Float.POSITIVE_INFINITY; //Force volume update

                if(blacklisted && scr.browser != null)
                    scr.browser.loadURL(WebDisplays.BLACKLIST_URL);

                break;
            }
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        if(level.isClientSide)
            onChunkUnloaded();
    }

    public void addFriend(ServerPlayer ply, BlockSide side, NameUUIDPair pair) {
        if(!level.isClientSide) {
            Screen scr = getScreen(side);
            if(scr == null) {
                Log.error("Tried to add friend to invalid screen side %s", side.toString());
                return;
            }

            if(!scr.friends.contains(pair)) {
                scr.friends.add(pair);
                (new ScreenConfigData(new Vector3i(getBlockPos()), side, scr)).updateOnly().sendTo(point(level, getBlockPos()));
                setChanged();
            }
        }
    }

    public void removeFriend(ServerPlayer ply, BlockSide side, NameUUIDPair pair) {
        if(!level.isClientSide) {
            Screen scr = getScreen(side);
            if(scr == null) {
                Log.error("Tried to remove friend from invalid screen side %s", side.toString());
                return;
            }

            if(scr.friends.remove(pair)) {
                checkLaserUserRights(scr);
                (new ScreenConfigData(new Vector3i(getBlockPos()), side, scr)).updateOnly().sendTo(point(level, getBlockPos()));
                setChanged();
            }
        }
    }

    public void setRights(ServerPlayer ply, BlockSide side, int fr, int or) {
        if(!level.isClientSide) {
            Screen scr = getScreen(side);
            if(scr == null) {
                Log.error("Tried to change rights of invalid screen on side %s", side.toString());
                return;
            }

            scr.friendRights = fr;
            scr.otherRights = or;

            checkLaserUserRights(scr);
            (new ScreenConfigData(new Vector3i(getBlockPos()), side, scr)).updateOnly().sendTo(point(level, getBlockPos()));
            setChanged();
        }
    }

    public void type(BlockSide side, String text, BlockPos soundPos) {
        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Tried to type on invalid screen on side %s", side.toString());
            return;
        }

        if(level.isClientSide) {
            if(scr.browser != null) {
                try {
                    if(text.startsWith("t")) {
                        for(int i = 1; i < text.length(); i++) {
                            char chr = text.charAt(i);
                            if(chr == 1)
                                break;

                            scr.browser.injectKeyTyped(chr, 0);
                        }
                    } else {
                        TypeData[] data = WebDisplays.GSON.fromJson(text, TypeData[].class);

                        for(TypeData ev : data) {
                            switch (ev.getAction()) {
                                case PRESS -> scr.browser.injectKeyPressedByKeyCode(ev.getKeyCode(), ev.getKeyChar(), 0);
                                case RELEASE -> scr.browser.injectKeyReleasedByKeyCode(ev.getKeyCode(), ev.getKeyChar(), 0);
                                case TYPE -> scr.browser.injectKeyTyped(ev.getKeyChar(), 0);
                                default -> throw new RuntimeException("Invalid type action '" + ev.getAction() + '\'');
                            }
                        }
                    }
                } catch(Throwable t) {
                    Log.warningEx("Suspicious keyboard type packet received...", t);
                }
            }
        } else {
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.type(this, side, text));

            if(soundPos != null)
                playSoundAt(WebDisplays.INSTANCE.soundTyping, soundPos, 0.25f, 1.f);
        }
    }

    private void playSoundAt(SoundEvent snd, BlockPos at, float vol, float pitch) {
        double x = at.getX();
        double y = at.getY();
        double z = at.getZ();

        level.playSound(null, x + 0.5, y + 0.5, z + 0.5, snd, SoundSource.BLOCKS, vol, pitch);
    }

    public void updateUpgrades(BlockSide side, ItemStack[] upgrades) {
        if(!level.isClientSide) {
            Log.error("Tried to call TileEntityScreen.updateUpgrades() from server side...");
            return;
        }

        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Tried to update upgrades on invalid screen on side %s", side.toString());
            return;
        }

        scr.upgrades.clear();
        Collections.addAll(scr.upgrades, upgrades);

        if(scr.browser != null)
            scr.browser.runJS("if(typeof webdisplaysUpgradesChanged == \"function\") webdisplaysUpgradesChanged();", "");
    }

    private static String safeName(ItemStack is) {
        ResourceLocation rl = is.getItem().getRegistryName();
        return (rl == null) ? "[NO NAME, WTF?!]" : rl.toString();
    }

    //If equal is null, no duplicate check is preformed
    public boolean addUpgrade(BlockSide side, ItemStack is, @Nullable Player player, boolean abortIfExisting) {
        if(level.isClientSide)
            return false;

        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Tried to add an upgrade on invalid screen on side %s", side.toString());
            return false;
        }

        if(!(is.getItem() instanceof IUpgrade)) {
            Log.error("Tried to add a non-upgrade item %s to screen (%s does not implement IUpgrade)", safeName(is), is.getItem().getClass().getCanonicalName());
            return false;
        }

        if(scr.upgrades.size() >= 16) {
            Log.error("Can't insert upgrade %s in screen %s at %s: too many upgrades already!", safeName(is), side.toString(), getBlockPos().toString());
            return false;
        }

        IUpgrade itemAsUpgrade = (IUpgrade) is.getItem();
        if(abortIfExisting && scr.upgrades.stream().anyMatch(otherStack -> itemAsUpgrade.isSameUpgrade(is, otherStack)))
            return false; //Upgrade already exists

        ItemStack isCopy = is.copy(); //FIXME: Duct tape fix, because the original stack will be shrinked
        isCopy.setCount(1);

        scr.upgrades.add(isCopy);
        Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.upgrade(this, side));
        itemAsUpgrade.onInstall(this, side, player, isCopy);
        playSoundAt(WebDisplays.INSTANCE.soundUpgradeAdd, getBlockPos(), 1.0f, 1.0f);
        setChanged();
        return true;
    }

    public boolean hasUpgrade(BlockSide side, ItemStack is) {
        Screen scr = getScreen(side);
        if(scr == null)
            return false;

        if(!(is.getItem() instanceof IUpgrade))
            return false;

        IUpgrade itemAsUpgrade = (IUpgrade) is.getItem();
        return scr.upgrades.stream().anyMatch(otherStack -> itemAsUpgrade.isSameUpgrade(is, otherStack));
    }

    public boolean hasUpgrade(BlockSide side, DefaultUpgrade du) {
        Screen scr = getScreen(side);
        return scr != null && scr.upgrades.stream().anyMatch(du::matches);
    }

    public void removeUpgrade(BlockSide side, ItemStack is, @Nullable Player player) {
        if(level.isClientSide)
            return;

        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Tried to remove an upgrade on invalid screen on side %s", side.toString());
            return;
        }

        if(!(is.getItem() instanceof IUpgrade)) {
            Log.error("Tried to remove a non-upgrade item %s to screen (%s does not implement IUpgrade)", safeName(is), is.getItem().getClass().getCanonicalName());
            return;
        }

        int idxToRemove = -1;
        IUpgrade itemAsUpgrade = (IUpgrade) is.getItem();

        for(int i = 0; i < scr.upgrades.size(); i++) {
            if(itemAsUpgrade.isSameUpgrade(is, scr.upgrades.get(i))) {
                idxToRemove = i;
                break;
            }
        }

        if(idxToRemove >= 0) {
            dropUpgrade(scr.upgrades.get(idxToRemove), side, player);
            scr.upgrades.remove(idxToRemove);
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.upgrade(this, side));
            playSoundAt(WebDisplays.INSTANCE.soundUpgradeDel, getBlockPos(), 1.0f, 1.0f);
            setChanged();
        } else
            Log.warning("Tried to remove non-existing upgrade %s to screen %s at %s", safeName(is), side.toString(), getBlockPos().toString());
    }

    private void dropUpgrade(ItemStack is, BlockSide side, @Nullable Player ply) {
        if(!((IUpgrade) is.getItem()).onRemove(this, side, ply, is)) { //Drop upgrade item
            boolean spawnDrop = true;

            if(ply != null) {
                if(ply.isCreative() || ply.addItem(is))
                    spawnDrop = false; //If in creative or if the item was added to the player's inventory, don't spawn drop entity
            }

            if(spawnDrop) {
                Vector3f pos = new Vector3f((float) this.getBlockPos().getX(), (float) this.getBlockPos().getY(), (float) this.getBlockPos().getZ());
                pos.addMul(side.backward.toFloat(), 1.5f);

                if(level != null) {
                    level.addFreshEntity(new ItemEntity(level, pos.x, pos.y, pos.z, is));
                }
            }
        }
    }

    private Screen getScreenForLaserOp(BlockSide side, Player ply) {
        if(level.isClientSide)
            return null;

        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Called laser operation on invalid screen on side %s", side.toString());
            return null;
        }

        if((scr.rightsFor(ply) & ScreenRights.CLICK) == 0)
            return null; //Don't output an error, it can 'legally' happen

        if(scr.upgrades.stream().noneMatch(DefaultUpgrade.LASER_MOUSE::matches)) {
            Log.error("Called laser operation on side %s, but it's missing the laser sensor upgrade", side.toString());
            return null;
        }

        return scr; //Okay, go for it...
    }

    public void laserDownMove(BlockSide side, Player ply, Vector2i pos, boolean down) {
        Screen scr = getScreenForLaserOp(side, ply);

        if(scr != null) {
            if(down) {
                //Try to acquire laser lock
                if(getLaserUser(scr) == null) {
                    scr.laserUser = ply;
                    Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.click(this, side, CMessageScreenUpdate.MOUSE_DOWN, pos));
                }
            } else if(getLaserUser(scr) == ply)
                Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.click(this, side, CMessageScreenUpdate.MOUSE_MOVE, pos));
        }
    }

    public void laserUp(BlockSide side, Player ply) {
        Screen scr = getScreenForLaserOp(side, ply);

        if(scr != null) {
            if(getLaserUser(scr) == ply) {
                scr.laserUser = null;
                Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.click(this, side, CMessageScreenUpdate.MOUSE_UP, null));
            }
        }
    }

    public void onDestroy(@Nullable Player ply) {
        for(Screen scr: screens) {
            scr.upgrades.forEach(is -> dropUpgrade(is, scr.side, ply));
            scr.upgrades.clear();
        }

        Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), new CMessageCloseGui(getBlockPos()));
    }

    public void setOwner(BlockSide side, Player newOwner) {
        if(level.isClientSide) {
            Log.error("Called TileEntityScreen.setOwner() on client...");
            return;
        }

        if(newOwner == null) {
            Log.error("Called TileEntityScreen.setOwner() with null owner");
            return;
        }

        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Called TileEntityScreen.setOwner() on invalid screen on side %s", side.toString());
            return;
        }

        scr.owner = new NameUUIDPair(newOwner.getGameProfile());
        Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.owner(this, side, scr.owner));
        checkLaserUserRights(scr);
        setChanged();
    }

    public void setRotation(BlockSide side, Rotation rot) {
        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Trying to change rotation of invalid screen on side %s", side.toString());
            return;
        }

        if(level.isClientSide) {
            boolean oldWasVertical = scr.rotation.isVertical;
            scr.rotation = rot;

            WebDisplays.PROXY.screenUpdateRotationInGui(new Vector3i(getBlockPos()), side, rot);

            if(scr.browser != null && oldWasVertical != rot.isVertical) {
                scr.browser.close();
                scr.browser = null; //Will be re-created by renderer
            }
        } else {
            scr.rotation = rot;
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.rotation(this, side, rot));
            setChanged();
        }
    }

    public void evalJS(BlockSide side, String code) {
        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Trying to run JS code on invalid screen on side %s", side.toString());
            return;
        }

        if(level.isClientSide) {
            if(scr.browser != null)
                scr.browser.runJS(code, "");
        } else
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.js(this, side, code));
    }

    public void setAutoVolume(BlockSide side, boolean av) {
        Screen scr = getScreen(side);
        if(scr == null) {
            Log.error("Trying to toggle auto-volume on invalid screen (side %s)", side.toString());
            return;
        }

        scr.autoVolume = av;

        if(level.isClientSide)
            WebDisplays.PROXY.screenUpdateAutoVolumeInGui(new Vector3i(getBlockPos()), side, av);
        else {
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(level, getBlockPos())), CMessageScreenUpdate.autoVolume(this, side, av));
            setChanged();
        }
    }


//    @Override
//    public boolean shouldRefresh(Level world, BlockPos pos, @Nonnull BlockState oldState, @Nonnull BlockState newState) {
//        if(oldState.getBlock() != WebDisplays.INSTANCE.blockScreen || newState.getBlock() != WebDisplays.INSTANCE.blockScreen)
//            return true;
//
//        return oldState.getValue(BlockScreen.hasTE) != newState.getValue(BlockScreen.hasTE);
//    }

}
