package net.montoyo.wd.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.item.*;

import java.util.function.Supplier;

public class ItemInit {

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "webdisplays");

    public static final RegistryObject<Item> itemScreenCfg = ITEMS.register("screen_config", () -> new ItemScreenConfigurator(new Item.Properties()));
    public static final RegistryObject<Item> itemOwnerThief = ITEMS.register("owner_thief", () -> new ItemOwnershipThief(new Item.Properties()));
    public static final RegistryObject<Item> itemLinker = ITEMS.register("linker", () -> new ItemLinker(new Item.Properties()));
    public static final RegistryObject<Item> itemMinePad = ITEMS.register("mine_pad", () -> new ItemMinePad2(new Item.Properties()));
    public static final RegistryObject<Item> itemUpgrade = ITEMS.register("upgrade", ItemUpgrade::new);
    public static final RegistryObject<Item> itemCraftComp = ITEMS.register("craftcomp", () -> new ItemCraftComponent(new Item.Properties()));
    public static final RegistryObject<Item> itemLaserPointer = ITEMS.register("laserpointer", () -> new ItemLaserPointer(new Item.Properties()));

}
