/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.montoyo.mcef.api.*;
import net.montoyo.wd.SharedProxy;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.block.BlockScreen;
import net.montoyo.wd.client.gui.*;
import net.montoyo.wd.client.gui.loading.GuiLoader;
import net.montoyo.wd.client.renderers.IItemRenderer;
import net.montoyo.wd.client.renderers.LaserPointerRenderer;
import net.montoyo.wd.client.renderers.MinePadRenderer;
import net.montoyo.wd.core.DefaultUpgrade;
import net.montoyo.wd.core.HasAdvancement;
import net.montoyo.wd.core.JSServerRequest;
import net.montoyo.wd.data.GuiData;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.init.BlockInit;
import net.montoyo.wd.init.ItemInit;
import net.montoyo.wd.item.WDItem;
import net.montoyo.wd.miniserv.client.Client;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.net.server.SMessagePadCtrl;
import net.montoyo.wd.net.server.SMessageScreenCtrl;
import net.montoyo.wd.utilities.*;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

public class ClientProxy extends SharedProxy implements IDisplayHandler, IJSQueryHandler, ResourceManagerReloadListener {

    public class PadData {

        public IBrowser view;
        private boolean isInHotbar;
        private final int id;
        private long lastURLSent;

        private PadData(String url, int id) {
            view = mcef.createBrowser(WebDisplays.applyBlacklist(url));
            view.resize((int) WebDisplays.INSTANCE.padResX, (int) WebDisplays.INSTANCE.padResY);
            isInHotbar = true;
            this.id = id;
        }

    }

    private Minecraft mc;
    private final ArrayList<ResourceModelPair> modelBakers = new ArrayList<>();
    private net.montoyo.mcef.api.API mcef;
    private MinePadRenderer minePadRenderer;
    private JSQueryDispatcher jsDispatcher;
    private LaserPointerRenderer laserPointerRenderer;
    private Screen nextScreen;
    private boolean isF1Down;

    //Miniserv handling
    private int miniservPort;
    private boolean msClientStarted;

    //Client-side advancement hack
    private final Field advancementToProgressField = findAdvancementToProgressField();
    private ClientAdvancements lastAdvMgr;
    private Map advancementToProgress;

    //Laser pointer
    private TileEntityScreen pointedScreen;
    private BlockSide pointedScreenSide;
    private long lastPointPacket;

    //Tracking
    private final ArrayList<TileEntityScreen> screenTracking = new ArrayList<>();
    private int lastTracked = 0;

    //MinePads Management
    private final HashMap<Integer, PadData> padMap = new HashMap<>();
    private final ArrayList<PadData> padList = new ArrayList<>();
    private int minePadTickCounter = 0;

    /**************************************** INHERITED METHODS ****************************************/

    @Override
    public void preInit() {
        mc = Minecraft.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
//        registerCustomBlockBaker(new ScreenBaker(), WebDisplays.INSTANCE.blockScreen);

        mcef = MCEFApi.getAPI();
        if(mcef != null)
            mcef.registerScheme("wd", WDScheme.class, true, false, false, true, true, false, false);
    }

    @Override
    public void init() {
        //ClientRegistry.bindTileEntitySpecialRenderer(TileEntityScreen.class, new ScreenRenderer());
        jsDispatcher = new JSQueryDispatcher(this);
        minePadRenderer = new MinePadRenderer();
        laserPointerRenderer = new LaserPointerRenderer();
    }

    @Override
    public void postInit() {
        ((ReloadableResourceManager) mc.getResourceManager()).registerReloadListener(this);

        if(mcef == null)
            throw new RuntimeException("MCEF is missing");

        mcef.registerDisplayHandler(this);
      //  mcef.registerJSQueryHandler(this); //TODO why crashing on this method!
        findAdvancementToProgressField();
    }

    @Override
    public Level getWorld(ResourceKey<Level> dim) {
        Level ret = mc.level;
//        if(dim == CURRENT_DIMENSION)
//            return ret;
        if(ret != null) {
            if (!ret.dimension().equals(dim))
                throw new RuntimeException("Can't get non-current dimension " + dim + " from client.");
            return ret;
        } else {
            throw new RuntimeException("Level on client is null");
        }
    }

    @Override
    public void enqueue(Runnable r) {
        mc.submit(r);
    }

    @Override
    public void displayGui(GuiData data) {
        Screen gui = data.createGui(mc.screen, mc.level);
        if(gui != null)
            mc.setScreen(gui);
    }

    @Override
    public void trackScreen(TileEntityScreen tes, boolean track) {
        int idx = -1;
        for(int i = 0; i < screenTracking.size(); i++) {
            if(screenTracking.get(i) == tes) {
                idx = i;
                break;
            }
        }

        if(track) {
            if(idx < 0)
                screenTracking.add(tes);
        } else if(idx >= 0)
            screenTracking.remove(idx);
    }

    @Override
    public void onAutocompleteResult(NameUUIDPair[] pairs) {
        if(mc.screen != null && mc.screen instanceof WDScreen screen) {
            if(pairs.length == 0)
                (screen).onAutocompleteFailure();
            else
                (screen).onAutocompleteResult(pairs);
        }
    }

    @Override
    public GameProfile[] getOnlineGameProfiles() {
        return new GameProfile[] { mc.player.getGameProfile() };
    }

    @Override
    public void screenUpdateResolutionInGui(Vector3i pos, BlockSide side, Vector2i res) {
        if(mc.screen != null && mc.screen instanceof GuiScreenConfig gsc) {
            if(gsc.isForBlock(pos.toBlock(), side))
                gsc.updateResolution(res);
        }
    }

    @Override
    public void screenUpdateRotationInGui(Vector3i pos, BlockSide side, Rotation rot) {
        if(mc.screen != null && mc.screen instanceof GuiScreenConfig gsc) {
            if(gsc.isForBlock(pos.toBlock(), side))
                gsc.updateRotation(rot);
        }
    }

    @Override
    public void screenUpdateAutoVolumeInGui(Vector3i pos, BlockSide side, boolean av) {
        if(mc.screen != null && mc.screen instanceof GuiScreenConfig gsc) {
            if(gsc.isForBlock(pos.toBlock(), side))
                gsc.updateAutoVolume(av);
        }
    }

    @Override
    public void displaySetPadURLGui(String padURL) {
        mc.setScreen(new GuiSetURL2(padURL));
    }

    @Override
    public void openMinePadGui(int padId) {
        PadData pd = padMap.get(padId);

        if(pd != null && pd.view != null)
            mc.setScreen(new GuiMinePad(pd));
    }

    @Override
    @Nonnull
    public HasAdvancement hasClientPlayerAdvancement(@Nonnull ResourceLocation rl) {
        if(advancementToProgressField != null && mc.player != null && mc.player.connection != null) {
            ClientAdvancements cam = mc.player.connection.getAdvancements();
            Advancement adv = cam.getAdvancements().get(rl);

            if(adv == null)
                return HasAdvancement.DONT_KNOW;

            if(lastAdvMgr != cam) {
                lastAdvMgr = cam;

                try {
                    advancementToProgress = (Map) advancementToProgressField.get(cam);
                } catch(Throwable t) {
                    Log.warningEx("Could not get ClientAdvancementManager.advancementToProgress field", t);
                    advancementToProgress = null;
                    return HasAdvancement.DONT_KNOW;
                }
            }

            if(advancementToProgress == null)
                return HasAdvancement.DONT_KNOW;

            Object progress = advancementToProgress.get(adv);
            if(progress == null)
                return HasAdvancement.NO;

            if(!(progress instanceof AdvancementProgress)) {
                Log.warning("The ClientAdvancementManager.advancementToProgress map does not contain AdvancementProgress instances");
                advancementToProgress = null; //Invalidate this: it's wrong
                return HasAdvancement.DONT_KNOW;
            }

            return ((AdvancementProgress) progress).isDone() ? HasAdvancement.YES : HasAdvancement.NO;
        }

        return HasAdvancement.DONT_KNOW;
    }

    @Override
    public MinecraftServer getServer() {
        return mc.getSingleplayerServer();
    }

    @Override
    public void handleJSResponseSuccess(int reqId, JSServerRequest type, byte[] data) {
        JSQueryDispatcher.ServerQuery q = jsDispatcher.fulfillQuery(reqId);

        if(q == null)
            Log.warning("Received success response for invalid query ID %d of type %s", reqId, type.toString());
        else {
            if(type == JSServerRequest.CLEAR_REDSTONE || type == JSServerRequest.SET_REDSTONE_AT)
                q.success("{\"status\":\"success\"}");
            else
                Log.warning("Received success response for query ID %d, but type is invalid", reqId);
        }
    }

    @Override
    public void handleJSResponseError(int reqId, JSServerRequest type, int errCode, String err) {
        JSQueryDispatcher.ServerQuery q = jsDispatcher.fulfillQuery(reqId);

        if(q == null)
            Log.warning("Received error response for invalid query ID %d of type %s", reqId, type.toString());
        else
            q.error(errCode, err);
    }

    @Override
    public void setMiniservClientPort(int port) {
        miniservPort = port;
    }

    @Override
    public void startMiniservClient() {
        if(miniservPort <= 0) {
            Log.warning("Can't start miniserv client: miniserv is disabled");
            return;
        }

        if(mc.player == null) {
            Log.warning("Can't start miniserv client: player is null");
            return;
        }

        SocketAddress saddr = mc.player.connection.getConnection().channel().remoteAddress();
        if(saddr == null || !(saddr instanceof InetSocketAddress)) {
            Log.warning("Miniserv client: remote address is not inet, assuming local address");
            saddr = new InetSocketAddress("127.0.0.1", 1234);
        }

        InetSocketAddress msAddr = new InetSocketAddress(((InetSocketAddress) saddr).getAddress(), miniservPort);
        Client.getInstance().start(msAddr);
        msClientStarted = true;
    }

    @Override
    public boolean isMiniservDisabled() {
        return miniservPort <= 0;
    }

    @Override
    public void closeGui(BlockPos bp, BlockSide bs) {
        if(mc.screen instanceof WDScreen) {
            WDScreen scr = (WDScreen) mc.screen;

            if(scr.isForBlock(bp, bs))
                mc.setScreen(null);
        }
    }

    @Override
    public void renderRecipes() {
        nextScreen = new RenderRecipe();
    }

    @Override
    public boolean isShiftDown() {
        return Screen.hasShiftDown();
    }


    /**************************************** RESOURCE MANAGER METHODS ****************************************/

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Log.info("Resource manager reload: clearing GUI cache...");
        GuiLoader.clearCache();
    }

    /**************************************** DISPLAY HANDLER METHODS ****************************************/

    @Override
    public void onAddressChange(IBrowser browser, String url) {
        if(browser != null) {
            long t = System.currentTimeMillis();

            for(PadData pd: padList) {
                if(pd.view == browser && t - pd.lastURLSent >= 1000) {
                    if(WebDisplays.isSiteBlacklisted(url))
                        pd.view.loadURL(WebDisplays.BLACKLIST_URL);
                    else {
                        pd.lastURLSent = t; //Avoid spamming the server with porn URLs
                        Messages.INSTANCE.sendToServer(new SMessagePadCtrl(pd.id, url));
                    }

                    break;
                }
            }

            for(TileEntityScreen tes: screenTracking)
                tes.updateClientSideURL(browser, url);
        }
    }

    @Override
    public void onTitleChange(IBrowser browser, String title) {
    }

    @Override
    public void onTooltip(IBrowser browser, String text) {
    }

    @Override
    public void onStatusMessage(IBrowser browser, String value) {
    }

    /**************************************** JS HANDLER METHODS ****************************************/

    @Override
    public boolean handleQuery(IBrowser browser, long queryId, String query, boolean persistent, IJSQueryCallback cb) {
        if(browser != null && persistent && query != null && cb != null) {
            query = query.toLowerCase();

            if(query.startsWith("webdisplays_")) {
                query = query.substring(12);

                String args;
                int parenthesis = query.indexOf('(');
                if(parenthesis < 0)
                    args = null;
                else {
                    if(query.indexOf(')') != query.length() - 1) {
                        cb.failure(400, "Malformed request");
                        return true;
                    }

                    args = query.substring(parenthesis + 1, query.length() - 1);
                    query = query.substring(0, parenthesis);
                }

                if(jsDispatcher.canHandleQuery(query))
                    jsDispatcher.enqueueQuery(browser, query, args, cb);
                else
                    cb.failure(404, "Unknown WebDisplays query");

                return true;
            }
        }

        return false;
    }

    @Override
    public void cancelQuery(IBrowser browser, long queryId) {
    }

    /**************************************** EVENT METHODS ****************************************/

//    @SubscribeEvent TODO: CHeck if we need this at all
//    public void onStitchTextures(TextureStitchEvent.Pre ev) {
//        TextureAtlas texMap = ev.getAtlas();
//
//        if(texMap == mc.getTextureManager()..getTextureAtlas()) {
//            for(ResourceModelPair pair : modelBakers)
//                pair.getModel().loadTextures(texMap);
//        }
//    }
//
//    @SubscribeEvent
//    public void onBakeModel(ModelBakeEvent ev) {
//        for(ResourceModelPair pair : modelBakers)
//            ev.getModelRegistry().put(pair.getResourceLocation(), pair.getModel());
//    }

    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent ev) {
        final WebDisplays wd = WebDisplays.INSTANCE;

        //I hope I'm doing this right because it doesn't seem like it...
//        registerItemModel(wd.blockScreen.getItem(), 0, "inventory");
//        ModelLoaderRegistry.setCustomModelResourceLocation(wd.blockPeripheral.getItem(), 0, new ModelResourceLocation("webdisplays:kb_inv", "normal"));
//        registerItemModel(wd.blockPeripheral.getItem(), 1, "facing=2,type=ccinterface");
//        registerItemModel(wd.blockPeripheral.getItem(), 2, "facing=2,type=cointerface");
//        registerItemModel(wd.blockPeripheral.getItem(), 3, "facing=0,type=remotectrl");
//        registerItemModel(wd.blockPeripheral.getItem(), 7, "facing=0,type=redstonectrl");
//        registerItemModel(wd.blockPeripheral.getItem(), 11, "facing=0,type=server");
//        registerItemModel(wd.itemScreenCfg, 0, "normal");
//        registerItemModel(wd.itemOwnerThief, 0, "normal");
//        registerItemModel(wd.itemLinker, 0, "normal");
//        registerItemModel(wd.itemMinePad, 0, "normal");
//        registerItemModel(wd.itemMinePad, 1, "normal");
//        registerItemModel(wd.itemLaserPointer, 0, "normal");
//        registerItemMultiModels(wd.itemUpgrade);
//        registerItemMultiModels(wd.itemCraftComp);
//        registerItemMultiModels(wd.itemAdvIcon);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent ev) {
        if(ev.phase == TickEvent.Phase.END) {
            //Help
            if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F1)) {
                if(!isF1Down) {
                    isF1Down = true;

                    String wikiName = null;
                    if(mc.screen instanceof WDScreen)
                        wikiName = ((WDScreen) mc.screen).getWikiPageName();
                    else if(mc.screen instanceof ContainerScreen) {
                        Slot slot = ((ContainerScreen) mc.screen).getSlotUnderMouse();

                        if(slot != null && slot.hasItem() && slot.getItem().getItem() instanceof WDItem)
                            wikiName = ((WDItem) slot.getItem().getItem()).getWikiName(slot.getItem());
                    }

                    if(wikiName != null)
                        mcef.openExampleBrowser("https://montoyo.net/wdwiki/index.php/" + wikiName);
                }
            } else if(isF1Down)
                isF1Down = false;

            //Workaround cuz chat sux
            if(nextScreen != null && mc.screen == null) {
                mc.setScreen(nextScreen);
                nextScreen = null;
            }

            //Unload/load screens depending on client player distance
            if(mc.player != null && !screenTracking.isEmpty()) {
                int id = lastTracked % screenTracking.size();
                lastTracked++;

                TileEntityScreen tes = screenTracking.get(id);
                double dist2 = mc.player.distanceToSqr(tes.getBlockPos().getX(), tes.getBlockPos().getY(), tes.getBlockPos().getZ());

                if(tes.isLoaded()) {
                    if(dist2 > WebDisplays.INSTANCE.unloadDistance2)
                        tes.unload();
                    //else if(WebDisplays.INSTANCE.enableSoundDistance)
                       // tes.updateTrackDistance(dist2, SoundSystemConfig.getMasterGain());
                } else if(dist2 <= WebDisplays.INSTANCE.loadDistance2)
                    tes.load();
            }

            //Load/unload minePads depending on which item is in the player's hand
            if(++minePadTickCounter >= 10) {
                minePadTickCounter = 0;
                Player ep = mc.player;

                for(PadData pd: padList)
                    pd.isInHotbar = false;

                if(ep != null) {
                    updateInventory(ep.getInventory().items, ep.getItemInHand(InteractionHand.MAIN_HAND), 9);
                    updateInventory(ep.getInventory().offhand, ep.getItemInHand(InteractionHand.OFF_HAND), 1); //Is this okay?
                }

                //TODO: Check for GuiContainer.draggedStack

                for(int i = padList.size() - 1; i >= 0; i--) {
                    PadData pd = padList.get(i);

                    if(!pd.isInHotbar) {
                        pd.view.close();
                        pd.view = null; //This is for GuiMinePad, in case the player dies with the GUI open
                        padList.remove(i);
                        padMap.remove(pd.id);
                    }
                }
            }

            //Laser pointer raycast
            boolean raycastHit = false;

            if(mc.player != null && mc.level != null && ItemInit.itemLaserPointer.isPresent() && mc.player.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(ItemInit.itemLaserPointer.get())
                                                     && mc.options.keyUse.isDown()
                                                     && (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK)) {
                laserPointerRenderer.isOn = true;
                BlockHitResult result = raycast(64.0); //TODO: Make that distance configurable

                BlockPos bpos = result.getBlockPos();

                if(result.getType() == HitResult.Type.BLOCK && mc.level.getBlockState(bpos).getBlock() == BlockInit.blockScreen.get()) {
                    Vector3i pos = new Vector3i(result.getBlockPos());
                    BlockSide side = BlockSide.values()[result.getDirection().ordinal()];

                    Multiblock.findOrigin(mc.level, pos, side, null);
                    TileEntityScreen te = (TileEntityScreen) mc.level.getBlockEntity(pos.toBlock());

                    if(te != null && te.hasUpgrade(side, DefaultUpgrade.LASER_MOUSE)) { //hasUpgrade returns false is there's no screen on side 'side'
                        //Since rights aren't synchronized, let the server check them for us...
                        TileEntityScreen.Screen scr = te.getScreen(side);

                        if(scr.browser != null) {
                            float hitX = ((float) result.getLocation().x) - (float) bpos.getX();
                            float hitY = ((float) result.getLocation().y) - (float) bpos.getY();
                            float hitZ = ((float) result.getLocation().z) - (float) bpos.getZ();
                            Vector2i tmp = new Vector2i();

                            if(BlockScreen.hit2pixels(side, bpos, pos, scr, hitX, hitY, hitZ, tmp)) {
                                laserClick(te, side, scr, tmp);
                                raycastHit = true;
                            }
                        }
                    }
                }
            } else
                laserPointerRenderer.isOn = false;

            if(!raycastHit)
                deselectScreen();

            //Handle JS queries
            jsDispatcher.handleQueries();

            //Miniserv
            if(msClientStarted && mc.player == null) {
                msClientStarted = false;
                Client.getInstance().stop();
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayerHand(RenderHandEvent ev) {
        Item item = ev.getItemStack().getItem();
        IItemRenderer renderer;

        if(ItemInit.itemMinePad.isPresent() && ItemInit.itemLaserPointer.isPresent()) {
            if (item == ItemInit.itemMinePad.get())
                renderer = minePadRenderer;
            else if (item == ItemInit.itemLaserPointer.get())
                renderer = laserPointerRenderer;
            else
                return;
            HumanoidArm handSide = mc.player.getMainArm();
            if (ev.getHand() == InteractionHand.OFF_HAND)
                handSide = handSide.getOpposite();

            renderer.render(ev.getPoseStack(), ev.getItemStack(), (handSide == HumanoidArm.RIGHT) ? 1.0f : -1.0f, ev.getSwingProgress(), ev.getEquipProgress(), ev.getMultiBufferSource(), ev.getPackedLight());
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload ev) {
        Log.info("World unloaded; killing screens...");
        if(ev.getWorld() instanceof Level level) {
            ResourceLocation dim = level.dimension().location();
            for(int i = screenTracking.size() - 1; i >= 0; i--) {
                if(screenTracking.get(i).getLevel().dimension().location().equals(dim)) //Could be world == ev.getWorld()
                    screenTracking.remove(i).unload();
            }
        }
    }

    /**************************************** OTHER METHODS ****************************************/

    private void laserClick(TileEntityScreen tes, BlockSide side, TileEntityScreen.Screen scr, Vector2i hit) {
        if(pointedScreen == tes && pointedScreenSide == side) {
            long t = System.currentTimeMillis();

            if(t - lastPointPacket >= 100) {
                lastPointPacket = t;
                Messages.INSTANCE.sendToServer(SMessageScreenCtrl.vec2(tes, side, SMessageScreenCtrl.CTRL_LASER_MOVE, hit));
            }
        } else {
            deselectScreen();
            pointedScreen = tes;
            pointedScreenSide = side;
            Messages.INSTANCE.sendToServer(SMessageScreenCtrl.vec2(tes, side, SMessageScreenCtrl.CTRL_LASER_DOWN, hit));
        }
    }

    private void deselectScreen() {
        if(pointedScreen != null && pointedScreenSide != null) {
            Messages.INSTANCE.sendToServer(SMessageScreenCtrl.laserUp(pointedScreen, pointedScreenSide));
            pointedScreen = null;
            pointedScreenSide = null;
        }
    }

    private BlockHitResult raycast(double dist) {
        Vec3 start = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = mc.player.getLookAngle();
        Vec3 end = start.add(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist);

        return mc.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null));
    }

    private void updateInventory(NonNullList<ItemStack> inv, ItemStack heldStack, int cnt) {
        for(int i = 0; i < cnt; i++) {
            ItemStack item = inv.get(i);

            if(ItemInit.itemMinePad.isPresent()) {
                if (item.getItem() == ItemInit.itemMinePad.get()) {
                    CompoundTag tag = item.getTag();

                    if (tag != null && tag.contains("PadID"))
                        updatePad(tag.getInt("PadID"), tag, item == heldStack);
                }
            }
        }
    }

//    private void registerCustomBlockBaker(IModelBaker baker, Block block0) {
//        ModelResourceLocation normalLoc = new ModelResourceLocation(block0.getRegistryName(), "normal");
//        ResourceModelPair pair = new ResourceModelPair(normalLoc, baker);
//        modelBakers.add(pair);
//        ModelLoader.setCustomStateMapper(block0, new StaticStateMapper(normalLoc));
//    }
//
//    private void registerItemModel(Item item, int meta, String variant) {
//        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), variant));
//    }
//
//    private void registerItemMultiModels(ItemMulti item) {
//        Enum[] values = item.getEnumValues();
//
//        for(int i = 0; i < values.length; i++)
//            ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName().toString() + '_' + values[i], "normal"));
//    }

    private void updatePad(int id, CompoundTag tag, boolean isSelected) {
        PadData pd = padMap.get(id);

        if(pd != null)
            pd.isInHotbar = true;
        else if(isSelected && tag.contains("PadURL")) {
            pd = new PadData(tag.getString("PadURL"), id);
            padMap.put(id, pd);
            padList.add(pd);
        }
    }

    public MinePadRenderer getMinePadRenderer() {
        return minePadRenderer;
    }

    public PadData getPadByID(int id) {
        return padMap.get(id);
    }

    public net.montoyo.mcef.api.API getMCEF() {
        return mcef;
    }

    public static final class ScreenSidePair {

        public TileEntityScreen tes;
        public BlockSide side;

    }

    public boolean findScreenFromBrowser(IBrowser browser, ScreenSidePair pair) {
        for(TileEntityScreen tes: screenTracking) {
            for(int i = 0; i < tes.screenCount(); i++) {
                TileEntityScreen.Screen scr = tes.getScreen(i);

                if(scr.browser == browser) {
                    pair.tes = tes;
                    pair.side = scr.side;
                    return true;
                }
            }
        }

        return false;
    }

    private static Field findAdvancementToProgressField() {
        Field[] fields = ClientAdvancements.class.getDeclaredFields();
        Optional<Field> result = Arrays.stream(fields).filter(f -> f.getType() == Map.class).findAny();

        if(result.isPresent()) {
            try {
                Field ret = result.get();
                ret.setAccessible(true);
                return ret;
            } catch(Throwable t) {
                t.printStackTrace();
            }
        }

        Log.warning("ClientAdvancementManager.advancementToProgress field could not be found");
        return null;
    }

}
