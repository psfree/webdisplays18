package net.montoyo.wd.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.block.BlockKeyboardRight;
import net.montoyo.wd.block.BlockPeripheral;
import net.montoyo.wd.block.BlockScreen;

public class BlockInit {

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }

    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "webdisplays");

    public static final RegistryObject<Block> blockScreen = BLOCKS.register("screen", () -> new BlockScreen(BlockBehaviour.Properties.of(Material.STONE)));

    public static final RegistryObject<Block> blockKeyBoard = BlockInit.BLOCKS.register("kb_left", BlockPeripheral::new);

    public static final RegistryObject<Block> blockRedControl = BlockInit.BLOCKS.register("redctrl", BlockPeripheral::new);

    public static final RegistryObject<Block> blockRControl = BlockInit.BLOCKS.register("rctrl", BlockPeripheral::new);

    public static final RegistryObject<Block> blockServer = BlockInit.BLOCKS.register("server", BlockPeripheral::new);

    public static final RegistryObject<Block> blockKbRight = BLOCKS.register("kb_right", BlockKeyboardRight::new);
}
