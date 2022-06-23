package net.montoyo.wd.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.entity.TileEntityScreen;

public class TileInit {

    public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITIES, "webdisplays");

    //Register tile entities
    public static final BlockEntityType<TileEntityScreen> SCREEN_BLOCK_ENTITY = TILE_TYPES.register("screen", () -> BlockEntityType.Builder.of(TileEntityScreen::new, BlockInit.blockScreen).build(null));

    public static void registerPeripherals() {
        for (DefaultPeripheral dp : DefaultPeripheral.values()) {
            if (dp.getTEClass() != null)
                TILE_TYPES.register(dp.name(), () -> BlockEntityType.Builder.of(dp.getTEClass(), BlockInit.blockPeripheral.get()).build(null));
        }
    }
}
