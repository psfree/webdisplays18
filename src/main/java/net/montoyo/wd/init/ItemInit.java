package net.montoyo.wd.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.block.WDBlockContainer;
import net.montoyo.wd.block.item.KeyboardItem;
import net.montoyo.wd.core.CraftComponent;
import net.montoyo.wd.core.DefaultUpgrade;
import net.montoyo.wd.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemInit{

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "webdisplays");
    public static RegistryObject<Item> itemCraftComp = null;

    public static RegistryObject<Item> laserMouse = null;
    public static RegistryObject<Item> redInput = null;
    public static RegistryObject<Item> redOutput = null;
    public static RegistryObject<Item> gps = null;

    public static final RegistryObject<Item> itemScreenCfg = ITEMS.register("screencfg", () -> new ItemScreenConfigurator(new Item.Properties()));
    public static final RegistryObject<Item> itemOwnerThief = ITEMS.register("ownerthief", () -> new ItemOwnershipThief(new Item.Properties()));
    public static final RegistryObject<Item> itemLinker = ITEMS.register("linker", () -> new ItemLinker(new Item.Properties()));
    public static final RegistryObject<Item> itemMinePad = ITEMS.register("minepad", () -> new ItemMinePad2(new Item.Properties()));
    public static final RegistryObject<Item> itemLaserPointer = ITEMS.register("laserpointer", () -> new ItemLaserPointer(new Item.Properties()));

    public static void registerUpgrade() {
        laserMouse = ITEMS.register("upgrade_" + DefaultUpgrade.LASERMOUSE.name().toLowerCase(Locale.ROOT), ItemUpgrade::new);
        redInput = ITEMS.register("upgrade_" + DefaultUpgrade.REDINPUT.name().toLowerCase(Locale.ROOT), ItemUpgrade::new);
        redOutput = ITEMS.register("upgrade_" + DefaultUpgrade.REDOUTPUT.name().toLowerCase(Locale.ROOT), ItemUpgrade::new);
        gps = ITEMS.register("upgrade_" + DefaultUpgrade.GPS.name().toLowerCase(Locale.ROOT), ItemUpgrade::new);
    }

    public static void registerComponents() {
        for (CraftComponent cc : CraftComponent.values()) {
            itemCraftComp = ITEMS.register("craftcomp_" + cc.name().toLowerCase(Locale.ROOT), () -> new ItemCraftComponent(new Item.Properties()));
        }
    }

    public static final RegistryObject<Item> screen = ITEMS.register("screen", () -> new BlockItem(BlockInit.blockScreen.get(), new Item.Properties().tab(WebDisplays.CREATIVE_TAB)));

    public static final RegistryObject<Item> keyboard = ITEMS.register("keyboard", () -> new KeyboardItem(BlockInit.blockKeyBoard.get(), new Item.Properties().tab(WebDisplays.CREATIVE_TAB)));
    public static final RegistryObject<Item> redctrl = ITEMS.register("redctrl", () -> new BlockItem(BlockInit.blockRedControl.get(), new Item.Properties().tab(WebDisplays.CREATIVE_TAB)));
    public static final RegistryObject<Item> rctrl = ITEMS.register("rctrl", () -> new BlockItem(BlockInit.blockRControl.get(), new Item.Properties().tab(WebDisplays.CREATIVE_TAB)));
    public static final RegistryObject<Item> server = ITEMS.register("server", () -> new BlockItem(BlockInit.blockServer.get(), new Item.Properties().tab(WebDisplays.CREATIVE_TAB)));

}
