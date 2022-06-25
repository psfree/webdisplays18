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

    public static final RegistryObject<Item> itemScreenCfg = register("item_screen_config", () -> new ItemScreenConfigurator(new Item.Properties()));
    public static final RegistryObject<Item> itemOwnerThief = register("item_owner_thief", () -> new ItemOwnershipThief(new Item.Properties()));
    public static final RegistryObject<Item> itemLinker = register("item_linker", () -> new ItemLinker(new Item.Properties()));
    public static final RegistryObject<Item> itemMinePad = register("item_mine_pad", () -> new ItemMinePad2(new Item.Properties()));
    public static final RegistryObject<Item> itemUpgrade = register("item_upgrade", ItemUpgrade::new);
    public static final RegistryObject<Item> itemCraftComp = register("item_craftcomp", () -> new ItemCraftComponent(new Item.Properties()));
    public static final RegistryObject<Item> itemLaserPointer = register("item_laserpointer", () -> new ItemLaserPointer(new Item.Properties()));

}
