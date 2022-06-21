/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Vector3i;

public interface IPeripheral {

    boolean connect(Level world, BlockPos blockPos, BlockState blockState, Vector3i screenPos, BlockSide screenSide);

}
