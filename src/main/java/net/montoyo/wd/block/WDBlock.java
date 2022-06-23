/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class WDBlock extends Block {

    protected BlockItem itemBlock;

    public WDBlock(Properties properties) {
        super(properties);
    }

    protected void setName(String name) {
        setRegistryName(name);
    }

    public void makeItemBlock() {
        if(itemBlock != null)
            throw new RuntimeException("WDBlock.makeItemBlock() called twice!");

        itemBlock = new BlockItem(this, new Item.Properties());
        itemBlock.setRegistryName(getRegistryName());
    }

    public BlockItem getItem() {
        return itemBlock;
    }

}
