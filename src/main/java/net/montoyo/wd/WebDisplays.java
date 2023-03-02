/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd;

import com.google.gson.Gson;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.montoyo.wd.client.ClientProxy;
import net.montoyo.wd.config.ModConfig;
import net.montoyo.wd.core.*;
import net.montoyo.wd.init.BlockInit;
import net.montoyo.wd.init.ItemInit;
import net.montoyo.wd.init.TileInit;
import net.montoyo.wd.miniserv.server.Server;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.net.client.CMessageServerInfo;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.utilities.Util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mod("webdisplays")
public class WebDisplays {

    public static final String MOD_VERSION = "1.1";

    public static WebDisplays INSTANCE;

    public static SharedProxy PROXY = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> SharedProxy::new);

    public static WDCreativeTab CREATIVE_TAB;
    public static final ResourceLocation ADV_PAD_BREAK = new ResourceLocation("webdisplays", "webdisplays/pad_break");
    public static final String BLACKLIST_URL = "mod://webdisplays/blacklisted.html";
    public static final Gson GSON = new Gson();
    public static final ResourceLocation CAPABILITY = new ResourceLocation("webdisplays", "customdatacap");

    //Sounds
    public SoundEvent soundTyping;
    public SoundEvent soundUpgradeAdd;
    public SoundEvent soundUpgradeDel;
    public SoundEvent soundScreenCfg;
    public SoundEvent soundServer;
    public SoundEvent soundIronic;

    //Criterions
    public Criterion criterionPadBreak;
    public Criterion criterionUpgradeScreen;
    public Criterion criterionLinkPeripheral;
    public Criterion criterionKeyboardCat;

    //Config
    public static final double PAD_RATIO = 59.0 / 30.0;
    public String homePage;
    public double padResX;
    public double padResY;
    private int lastPadId = 0;
    public boolean doHardRecipe;
    private boolean hasOC;
    private boolean hasCC;
    private List<String> blacklist;
    public boolean disableOwnershipThief;
    public double unloadDistance2;
    public double loadDistance2;
    public int maxResX;
    public int maxResY;
    public int maxScreenX;
    public int maxScreenY;
    public int miniservPort;
    public long miniservQuota;
    public boolean enableSoundDistance;
    public float ytVolume;
    public float avDist100;
    public float avDist0;

    public WebDisplays() {
        INSTANCE = this;
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        ConfigHolder<ModConfig> configHolder = AutoConfig.getConfigHolder(ModConfig.class);
        ModConfig config = configHolder.getConfig();
        configHolder.save();

        this.blacklist = config.main.blacklist;
        doHardRecipe = config.main.hardRecipes;
        this.homePage = config.main.homepage;
        disableOwnershipThief = config.main.disableOwnershipThief;
        unloadDistance2 = config.client.unloadDistance * config.client.unloadDistance;
        loadDistance2 = config.client.loadDistance * config.client.loadDistance;
        this.maxResX = config.main.maxResolutionX;
        this.maxResY = config.main.maxResolutionY;
        this.miniservPort = config.main.miniservPort;
        this.miniservQuota = config.main.miniservQuota * 1024L;
        this.maxScreenX = config.main.maxScreenSizeX;
        this.maxScreenY = config.main.maxScreenSizeY;
        enableSoundDistance = config.client.autoVolumeControl.enableAutoVolume;
        this.ytVolume = (float) config.client.autoVolumeControl.ytVolume;
        avDist100 = (float) config.client.autoVolumeControl.dist100;
        avDist0 = (float) config.client.autoVolumeControl.dist0;

        CREATIVE_TAB = new WDCreativeTab();

        //Criterions
        criterionPadBreak = new Criterion("pad_break");
        criterionUpgradeScreen = new Criterion("upgrade_screen");
        criterionLinkPeripheral = new Criterion("link_peripheral");
        criterionKeyboardCat = new Criterion("keyboard_cat");
        registerTrigger(criterionPadBreak, criterionUpgradeScreen, criterionLinkPeripheral, criterionKeyboardCat);

        //Read configuration
        padResY = config.main.padHeight;
        padResX = padResY * PAD_RATIO;

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(Messages::registryNetworkPackets);
        SOUNDS.register(bus);
        onRegisterSounds();
        BlockInit.init(bus);
        ItemInit.init(bus);
        ItemInit.registerUpgrade();
        ItemInit.registerComponents();
        TileInit.init(bus);

        PROXY.preInit();

        MinecraftForge.EVENT_BUS.register(this);


        //Other things
        PROXY.init();

        PROXY.postInit();
        hasOC = ModList.get().isLoaded("opencomputers");
        hasCC = ModList.get().isLoaded("computercraft");

      /*  if(hasCC) {
            try {
                //We have to do this because the "register" method might be stripped out if CC isn't loaded
                CCPeripheralProvider.class.getMethod("register").invoke(null);
            } catch(Throwable t) {
                Log.error("ComputerCraft was found, but WebDisplays wasn't able to register its CC Interface Peripheral");
                t.printStackTrace();
            }
        } */
    }

    @SubscribeEvent
    public static void onAttachPlayerCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player && !event.getObject().getCapability(WDDCapability.Provider.cap).isPresent()) {
            event.addCapability(new ResourceLocation("webdisplays", "wddcapability"), new WDDCapability.Provider());
        }
    }

    public void onRegisterSounds() {
        registerSound("keyboard_type");
        registerSound( "upgrade_add");
        registerSound( "upgrade_del");
        registerSound("screencfg_open");
        registerSound("server");
        registerSound("ironic");
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load ev) {
        if (ev.getLevel() instanceof Level level) {
            if (ev.getLevel().isClientSide() || level.dimension() != Level.OVERWORLD)
                return;

            File worldDir = Objects.requireNonNull(ev.getLevel().getServer()).getServerDirectory();
            File f = new File(worldDir, "wd_next.txt");

            if (f.exists()) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    String idx = br.readLine();
                    Util.silentClose(br);

                    if (idx == null)
                        throw new RuntimeException("Seems like the file is empty (1)");

                    idx = idx.trim();
                    if (idx.isEmpty())
                        throw new RuntimeException("Seems like the file is empty (2)");

                    lastPadId = Integer.parseInt(idx); //This will throw NumberFormatException if it goes wrong
                } catch (Throwable t) {
                    Log.warningEx("Could not read last minePad ID from %s. I'm afraid this might break all minePads.", t, f.getAbsolutePath());
                }
            }

            if (miniservPort != 0) {
                Server sv = Server.getInstance();
                sv.setPort(miniservPort);
                sv.setDirectory(new File(worldDir, "wd_filehost"));
                sv.start();
            }
        }
    }

    @SubscribeEvent
    public void onWorldLeave(LevelEvent.Unload ev) throws IOException {
        if(ev.getLevel() instanceof Level level) {
            if (ev.getLevel().isClientSide() || level.dimension() != Level.OVERWORLD)
                return;
            Server sw = Server.getInstance();
            sw.stopServer();
        }
    }

    @SubscribeEvent
    public void onWorldSave(LevelEvent.Save ev) {
        if(ev.getLevel() instanceof Level level) {
            if (ev.getLevel().isClientSide() || level.dimension() != Level.OVERWORLD)
                return;
            File f = new File(Objects.requireNonNull(ev.getLevel().getServer()).getServerDirectory(), "wd_next.txt");

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.write("" + lastPadId + "\n");
                Util.silentClose(bw);
            } catch (Throwable t) {
                Log.warningEx("Could not save last minePad ID (%d) to %s. I'm afraid this might break all minePads.", t, lastPadId, f.getAbsolutePath());
            }
        }
    }

    @SubscribeEvent
    public void onToss(ItemTossEvent ev) {
        if(!ev.getEntity().getLevel().isClientSide) {
            ItemStack is = ev.getEntity().getItem();

            if(is.getItem() == ItemInit.itemMinePad.get()) {
                CompoundTag tag = is.getTag();

                if(tag == null) {
                    tag = new CompoundTag();
                    is.setTag(tag);
                }

                UUID thrower = ev.getPlayer().getGameProfile().getId();
                tag.putLong("ThrowerMSB", thrower.getMostSignificantBits());
                tag.putLong("ThrowerLSB", thrower.getLeastSignificantBits());
                tag.putDouble("ThrowHeight", ev.getPlayer().getY() + ev.getPlayer().getEyeHeight());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerCraft(PlayerEvent.ItemCraftedEvent ev) {
        if(doHardRecipe && ev.getCrafting().getItem() == ItemInit.itemCraftComp.get() && (CraftComponent.EXTENSION_CARD.makeItemStack().is(ev.getCrafting().getItem()))) {
            if((ev.getEntity() instanceof ServerPlayer && !hasPlayerAdvancement((ServerPlayer) ev.getEntity(), ADV_PAD_BREAK)) || PROXY.hasClientPlayerAdvancement(ADV_PAD_BREAK) != HasAdvancement.YES) {
                ev.getCrafting().setDamageValue(CraftComponent.BAD_EXTENSION_CARD.ordinal());

                if(!ev.getEntity().getLevel().isClientSide)
                    ev.getEntity().getLevel().playSound(null, ev.getEntity().getX(), ev.getEntity().getY(), ev.getEntity().getZ(), SoundEvents.ITEM_BREAK, SoundSource.MASTER, 1.0f, 1.0f);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent ev) throws IOException {
        Server.getInstance().stopServer();
    }

    @SubscribeEvent
    public void onLogIn(PlayerEvent.PlayerLoggedInEvent ev) {
        if(!ev.getEntity().getLevel().isClientSide && ev.getEntity() instanceof ServerPlayer) {
            IWDDCapability cap =
                    ev.getEntity().getCapability(WDDCapability.Provider.cap, null).orElseThrow(RuntimeException::new);

            if(cap.isFirstRun()) {
                Util.toast(ev.getEntity(), ChatFormatting.LIGHT_PURPLE, "welcome1");
                Util.toast(ev.getEntity(), ChatFormatting.LIGHT_PURPLE, "welcome2");
                Util.toast(ev.getEntity(), ChatFormatting.LIGHT_PURPLE, "welcome3");

                cap.clearFirstRun();
            }

            PacketDistributor.PacketTarget packetDistrutor = PacketDistributor.PLAYER
                    .with(
                    () ->
                                    (ServerPlayer) ev.getEntity());

            CMessageServerInfo message = new CMessageServerInfo(miniservPort);

            Messages.INSTANCE.send(packetDistrutor, message);
        }
    }

    @SubscribeEvent
    public void onLogOut(PlayerEvent.PlayerLoggedOutEvent ev) {
        if(!ev.getEntity().getLevel().isClientSide)
            Server.getInstance().getClientManager().revokeClientKey(ev.getEntity().getGameProfile().getId());
    }

    @SubscribeEvent
    public void attachEntityCaps(AttachCapabilitiesEvent<Entity> ev) {
        if(ev.getObject() instanceof Player)
            ev.addCapability(CAPABILITY, new WDDCapability.Provider());
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone ev) {
        IWDDCapability src =  ev.getOriginal().getCapability(WDDCapability.Provider.cap, null).orElse(new WDDCapability.Factory().call());
        IWDDCapability dst =  ev.getEntity().getCapability(WDDCapability.Provider.cap, null).orElse(new WDDCapability.Factory().call());

        if(src == null) {
            Log.error("src is null");
            return;
        }

        if(dst == null) {
            Log.error("dst is null");
            return;
        }

        src.cloneTo(dst);
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent ev) {
        String msg = ev.getMessage().getString().replaceAll("\\s+", " ").toLowerCase();
        StringBuilder sb = new StringBuilder(msg.length());
        for(int i = 0; i < msg.length(); i++) {
            char chr = msg.charAt(i);

            if(chr != '.' && chr != ',' && chr != ';' && chr != '!' && chr != '?' && chr != ':' && chr != '\'' && chr != '\"' && chr != '`')
                sb.append(chr);
        }

        if(sb.toString().equals("ironic he could save others from death but not himself")) {
            Player ply = ev.getPlayer();
            ply.getLevel().playSound(null, ply.getX(), ply.getY(), ply.getZ(), soundIronic, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    @SubscribeEvent
    public void onClientChat(ClientChatEvent ev) {
        if(ev.getMessage().equals("!WD render recipes"))
            PROXY.renderRecipes();
    }

    private boolean hasPlayerAdvancement(ServerPlayer ply, ResourceLocation rl) {
        MinecraftServer server = PROXY.getServer();
        if(server == null)
            return false;

        Advancement adv = server.getAdvancements().getAdvancement(rl);
        return adv != null && ply.getAdvancements().getOrStartProgress(adv).isDone();
    }

    public static int getNextAvailablePadID() {
        return new WebDisplays().lastPadId++;
    }

    public static DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "webdisplays");

    private static void registerSound(String resName) {
        ResourceLocation resLoc = new ResourceLocation("webdisplays", resName);
        SoundEvent ret = new SoundEvent(resLoc);

        SOUNDS.register(resName, () -> ret);
    }

    private static void registerTrigger(Criterion ... criteria) {
        for(Criterion c: criteria)
            CriteriaTriggers.register(c);
    }

   // public static boolean isOpenComputersAvailable() {
   //     return INSTANCE.hasOC;
  //  }

  //  public static boolean isComputerCraftAvailable() {
  //      return INSTANCE.hasCC;
  //  }

    public static boolean isSiteBlacklisted(String url) {
        try {
            URL url2 = new URL(Util.addProtocol(url));
            return new ModConfig.Main().blacklist.stream().anyMatch(str -> str.equalsIgnoreCase(url2.getHost()));
        } catch(MalformedURLException ex) {
            return false;
        }
    }

    public static String applyBlacklist(String url) {
        return isSiteBlacklisted(url) ? BLACKLIST_URL : url;
    }

}

