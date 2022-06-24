package net.montoyo.wd.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.entity.TileEntityScreen;

public class TileInit {

    public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITIES, "webdisplays");

    public static RegistryObject<BlockEntityType<?>> PERIPHERAL;

    //Register tile entities
    public static final RegistryObject<BlockEntityType<TileEntityScreen>> SCREEN_BLOCK_ENTITY = TILE_TYPES
            .register("screen", () -> BlockEntityType.Builder
                    .of(TileEntityScreen::new, BlockInit.blockScreen.get()).build(null));

    public static void registerPeripherals() {
        for (DefaultPeripheral dp : DefaultPeripheral.values()) {
            if (dp.getTEClass() != null)
                PERIPHERAL = TILE_TYPES.register(dp.name(), () -> BlockEntityType.Builder
                    .of(dp.getTEClass(), BlockInit.blockPeripheral.get()).build(null));

        }
    }
}
