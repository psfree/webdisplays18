/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.montoyo.wd.WebDisplays;

public abstract class WDBlockContainer extends Block {

    protected static BlockItem itemBlock;

    public WDBlockContainer(Properties arg) {
        super(arg);
    }

    protected void setName(String name) {
       // setRegistryName(name);
    }

    protected static BlockItem createItemBlock(Block block) {
        return new BlockItem(block, new Item.Properties().tab(WebDisplays.CREATIVE_TAB));
    }

    public static void makeItemBlock(Block block) {
        if(itemBlock != null)
            throw new RuntimeException("WDBlockContainer.makeItemBlock() called twice!");

        itemBlock = createItemBlock(block);
       // itemBlock.setRegistryName(getName().getString());
    }

    public BlockItem getItem() {
        return itemBlock;
    }

}
