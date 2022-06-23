/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.montoyo.wd.item.ItemPeripheral;

public abstract class WDBlockContainer extends BaseContainerBlockEntity {

    protected BlockItem itemBlock;

    public WDBlockContainer(BlockEntityType<?> type, BlockPos blockPos, BlockState state) {
        super(type, blockPos, state);
    }

    protected void setName(String name) {
       // setRegistryName(name);
    }

    protected abstract ItemPeripheral createItemBlock();

    public void makeItemBlock() {
        if(itemBlock != null)
            throw new RuntimeException("WDBlockContainer.makeItemBlock() called twice!");

        itemBlock = createItemBlock();
        itemBlock.setRegistryName(getName().getString());
    }

    public BlockItem getItem() {
        return itemBlock;
    }

}
