package net.montoyo.wd.init;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.entity.*;

import java.util.Locale;

public class TileInit {

    public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITIES, "webdisplays");

    //Register tile entities
    public static final RegistryObject<BlockEntityType<TileEntityScreen>> SCREEN_BLOCK_ENTITY = TILE_TYPES
            .register("screen", () -> BlockEntityType.Builder
                    .of(TileEntityScreen::new, BlockInit.blockScreen.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> KEYBOARD = TILE_TYPES.register("kb_left", () -> BlockEntityType.Builder
                    .of(TileEntityKeyboard::new, BlockInit.blockKeyBoard.get()).build(null));

   public static final RegistryObject<BlockEntityType<?>> REMOTE_CONTROLLER = TILE_TYPES.register("rctrl",
           () -> BlockEntityType.Builder.of(TileEntityRCtrl::new, BlockInit.blockRControl.get()).build(null));         //WITHOUT FACING (>= 3)

    public static final RegistryObject<BlockEntityType<?>> REDSTONE_CONTROLLER = TILE_TYPES.register("redctrl",
            () -> BlockEntityType.Builder.of(TileEntityRedCtrl::new, BlockInit.blockRedControl.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> SERVER = TILE_TYPES.register("server" ,
            () -> BlockEntityType.Builder.of(TileEntityServer::new, BlockInit.blockServer.get()).build(null));

    public static void init(IEventBus bus) {
        TILE_TYPES.register(bus);
    }
}
