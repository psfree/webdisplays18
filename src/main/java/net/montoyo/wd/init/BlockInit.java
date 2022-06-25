package net.montoyo.wd.init;

import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.block.BlockKeyboardRight;
import net.montoyo.wd.block.BlockPeripheral;
import net.montoyo.wd.block.BlockScreen;
import net.montoyo.wd.core.WDCreativeTab;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockInit {

    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registry.BLOCK_REGISTRY, "webdisplays");

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }

    private static RegistryObject<Block> registerBlock(String name, Supplier<Block> blockSupplier) {
        return registerBlock(name, blockSupplier, b -> () -> new BlockItem(blockSupplier.get(), new Item.Properties().tab(WDCreativeTab.TAB_REDSTONE)));
    }

    private static RegistryObject<Block> registerBlock(String name, Supplier<Block> block, Function<RegistryObject<Block>, Supplier<? extends BlockItem>> item) {
        var reg = BLOCKS.register(name, block);
        ItemInit.ITEMS.register(name, () -> item.apply(reg).get());
        return reg;
    }

    public static final RegistryObject<Block> blockScreen = registerBlock("screen_block", BlockScreen::new);

    public static final RegistryObject<Block> blockPeripheral = registerBlock("peripheral_block", BlockPeripheral::new);

    public static final RegistryObject<Block> blockKbRight = registerBlock("kb_right_block", BlockKeyboardRight::new);
}
