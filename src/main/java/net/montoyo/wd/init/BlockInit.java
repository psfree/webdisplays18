package net.montoyo.wd.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.block.*;
import net.montoyo.wd.core.DefaultPeripheral;

public class BlockInit {

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }

    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "webdisplays");

    public static final RegistryObject<Block> blockScreen = BLOCKS.register("screen", () -> new BlockScreen(BlockBehaviour.Properties.of(Material.STONE)));

    public static final RegistryObject<Block> blockKeyBoard = BlockInit.BLOCKS.register("kb_left", BlockKeyboardLeft::new);
    public static final RegistryObject<Block> blockKbRight = BLOCKS.register("kb_right", BlockKeyboardRight::new);

    public static final RegistryObject<Block> blockRedControl = BlockInit.BLOCKS.register("redctrl", BlockRedCTRL::new);

    public static final RegistryObject<Block> blockRControl = BlockInit.BLOCKS.register("rctrl", BlockRCTRL::new);

    public static final RegistryObject<Block> blockServer = BlockInit.BLOCKS.register("server", BlockServer::new);
}
