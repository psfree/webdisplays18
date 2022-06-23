/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class WDBlockContainer extends BaseContainerBlockEntity {

    protected BlockItem itemBlock;

    public WDBlockContainer(BlockEntityType<?> type, BlockBehaviour.Properties material, BlockState state) {
        super(type, material, state);
    }

    protected void setName(String name) {
        setUnlocalizedName("webdisplays." + name);
        setRegistryName(name);
    }

    protected abstract BlockItem createItemBlock();

    public void makeItemBlock() {
        if(itemBlock != null)
            throw new RuntimeException("WDBlockContainer.makeItemBlock() called twice!");

        itemBlock = createItemBlock();
        itemBlock.setUnlocalizedName(getUnlocalizedName());
        itemBlock.setRegistryName(getRegistryName());
    }

    public BlockItem getItem() {
        return itemBlock;
    }

}
