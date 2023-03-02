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

    public void makeItemBlock() {
        if(itemBlock != null)
            throw new RuntimeException("WDBlock.makeItemBlock() called twice!");

        itemBlock = new BlockItem(this, new Item.Properties());
    }

    public BlockItem getItem() {
        return itemBlock;
    }

}
