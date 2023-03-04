package net.montoyo.wd.block.item;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.montoyo.wd.block.BlockKeyboardLeft;
import net.montoyo.wd.init.BlockInit;

public class KeyboardItem extends BlockItem {
	public KeyboardItem(Block arg, Properties arg2) {
		super(arg, arg2);
	}
	
	@Override
	protected boolean placeBlock(BlockPlaceContext arg, BlockState arg2) {
		Direction facing = arg.getHorizontalDirection();
		arg2 = arg2.setValue(BlockKeyboardLeft.FACING, facing);
		
		Direction d = BlockKeyboardLeft.mapDirection(facing);
		
		if (isValid(arg, arg2, d)) {
			Block kbRight = BlockInit.blockKbRight.get();
			BlockState rightState = kbRight.defaultBlockState();
			
			rightState = rightState.setValue(BlockKeyboardLeft.FACING, facing);
			if (!arg.getLevel().setBlock(
					arg.getClickedPos().relative(d),
					rightState,
					11
			)) return false;
			return arg.getLevel().setBlock(arg.getClickedPos(), arg2, 11);// 161
		}
		return false;
	}
	
	private boolean isValid(BlockPlaceContext context, BlockState state, Direction d) {
		return context.getLevel().getBlockState(context.getClickedPos().relative(d)).isAir();
	}
}
