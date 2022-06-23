package net.montoyo.wd.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.item.*;

import java.util.function.Supplier;

public class ItemInit {

    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(Item.class, "webdisplays");

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static <T extends Item> RegistryObject<T> register(String name, Supplier<T> item) {
        return ITEMS.register(name, item);
    }

    static <T extends Item> RegistryObject<T> register(Supplier<T> c, String id) {
        return register(id, c);
    }

    public static final RegistryObject<Item> itemScreenCfg = register("itemScreenConfig", () -> new ItemScreenConfigurator(new Item.Properties()));
    public static final RegistryObject<Item> itemOwnerThief = register("itemOwnerThief", () -> new ItemOwnershipThief(new Item.Properties()));
    public static final RegistryObject<Item> itemLinker = register("itemLinker", () -> new ItemLinker(new Item.Properties()));
    public static final RegistryObject<Item> itemMinePad = register("itemMinePad", () -> new ItemMinePad2(new Item.Properties()));
    public static final RegistryObject<Item> itemUpgrade = register("itemUpgrade", ItemUpgrade::new);
    public static final RegistryObject<Item> itemCraftComp = register("itemCraftComp", () -> new ItemCraftComponent(new Item.Properties()));
    public static final RegistryObject<Item> itemLaserPointer = register("itemLaserPointer", () -> new ItemLaserPointer(new Item.Properties()));

}
