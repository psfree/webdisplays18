/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.core;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.montoyo.wd.entity.TileEntityKeyboard;
import net.montoyo.wd.entity.TileEntityRCtrl;
import net.montoyo.wd.entity.TileEntityRedCtrl;
import net.montoyo.wd.entity.TileEntityServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum DefaultPeripheral implements StringRepresentable {

    KEYBOARD("keyboard", "Keyboard", TileEntityKeyboard::new, TileEntityKeyboard::getBlockFromTE),                          //WITH FACING (< 3)
//    CC_INTERFACE("ccinterface", "ComputerCraft_Interface", TileEntityCCInterface.class),
//    OC_INTERFACE("cointerface", "OpenComputers_Interface", TileEntityOCInterface.class),
    REMOTE_CONTROLLER("remotectrl", "Remote_Controller", TileEntityRCtrl::new ,TileEntityRCtrl::getBlockFromTE),         //WITHOUT FACING (>= 3)
    REDSTONE_CONTROLLER("redstonectrl", "Redstone_Controller", TileEntityRedCtrl::new ,TileEntityRedCtrl::getBlockFromTE),
    SERVER("server", "Server", TileEntityServer::new, TileEntityServer::getBlockFromTE);

    private final String name;
    private final String wikiName;
    private final BlockEntityType.BlockEntitySupplier<? extends BlockEntity> teClass;
    private final Supplier<? extends Block> bClass;

    DefaultPeripheral(String name, String wname, BlockEntityType.BlockEntitySupplier<? extends BlockEntity> factory, Supplier<? extends Block> supplier) {
        this.name = name;
        wikiName = wname;
        teClass = factory;
        bClass = supplier;
    }

    public Supplier<? extends Block> getBlockClass() {
        return bClass;
    }

    public static DefaultPeripheral fromMetadata(int meta) {
        if((meta & 3) == 3)
            return values()[(((meta >> 2) & 3) | 4) - 1]; //Without facing
        else
            return values()[meta & 3]; //With facing
    }

    public BlockEntityType.BlockEntitySupplier<? extends BlockEntity> getTEClass() {
        return teClass;
    }

    public boolean hasFacing() {
        return ordinal() < 3;
    }

    public int toMetadata(int facing) {
        int ret = ordinal();
        if(ret < 3) //With facing
            ret |= facing << 2;
        else //Without facing
            ret = (((ret + 1) & 3) << 2) | 3;

        return ret;
    }

    @Override
    public @NotNull String getSerializedName() {
        return "default_peripheral_" + name;
    }
}
