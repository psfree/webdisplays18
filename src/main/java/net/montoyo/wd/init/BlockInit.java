package net.montoyo.wd.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
import net.montoyo.wd.core.WDCreativeTab;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockInit {

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
    }

    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "webdisplays");

    public static final RegistryObject<Block> blockScreen = BLOCKS.register("screen_block", () -> new BlockScreen(BlockBehaviour.Properties.of(Material.STONE)));

    public static final RegistryObject<Block> blockPeripheral = BLOCKS.register("peripheral_block", BlockPeripheral::new);

    public static final RegistryObject<Block> blockKbRight = BLOCKS.register("kb_right_block", BlockKeyboardRight::new);
}
