/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.entity.TileEntityInterfaceBase;
import net.montoyo.wd.entity.TileEntityKeyboard;
import net.montoyo.wd.entity.TileEntityPeripheralBase;
import net.montoyo.wd.entity.TileEntityServer;
import net.montoyo.wd.init.BlockInit;
import net.montoyo.wd.item.ItemLinker;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.net.client.CMessageCloseGui;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockKeyboardLeft extends BlockPeripheral {

    public static final EnumProperty<DefaultPeripheral> TYPE = EnumProperty.create("type", DefaultPeripheral.class);
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
//    public static final DirectionProperty HALF = DirectionProperty.create("facing", Direction.EAST, Direction.WEST);
    
    private static final Property<?>[] properties = new Property<?>[] {TYPE, FACING};

    public BlockKeyboardLeft() {
    }
    
    // TODO: make non static (for extensibility purposes)
    public static TileEntityKeyboard getTileEntity(BlockState state, Level world, BlockPos pos) {
        if (state.getBlock() instanceof BlockKeyboardLeft) {
            BlockEntity te = world.getBlockEntity(pos); // TODO: check?
            if (te instanceof TileEntityKeyboard)
                return (TileEntityKeyboard) te;
        }
    
        BlockPos relative = pos.relative(BlockKeyboardLeft.mapDirection(state.getValue(FACING).getOpposite()));
        BlockState ns = world.getBlockState(relative);
        
        if(ns.getBlock() instanceof BlockPeripheral && ns.getValue(BlockPeripheral.type) == DefaultPeripheral.KEYBOARD) {
            BlockEntity te = world.getBlockEntity(relative); // TODO: check?
            if (te instanceof TileEntityKeyboard)
                return (TileEntityKeyboard) te;
        }
        
        return null;
    }
    
    public static Direction mapDirection(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> facing;
        };
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(properties);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        double rpos = (entity.getY() - ((double) pos.getY())) * 16.0;
        if (!world.isClientSide && rpos >= 1.0 && rpos <= 2.0 && Math.random() < 0.25) {
            TileEntityKeyboard tek = BlockKeyboardLeft.getTileEntity(state, world, pos);
            
            if (tek != null)
                tek.simulateCat(entity);
        }
    }
    
    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(player.getItemInHand(hand).getItem() instanceof ItemLinker)
            return InteractionResult.PASS;
        
        TileEntityKeyboard tek = BlockKeyboardLeft.getTileEntity(state, level, pos);
        if(tek != null)
            return tek.onRightClick(player, hand);
        
        return InteractionResult.PASS;
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TYPE) == DefaultPeripheral.KEYBOARD ? BlockKeyboardRight.KEYBOARD_AABB : Shapes.block();
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }
    
    private static void removeRightPiece(BlockState state, Level world, BlockPos pos) {
        BlockPos relative = pos.relative(BlockKeyboardLeft.mapDirection(state.getValue(FACING)));
        
        BlockState ns = world.getBlockState(relative);
        if(ns.getBlock() instanceof BlockKeyboardRight) {
            world.setBlock(relative, Blocks.AIR.defaultBlockState(), 3);
        }
    }
    
    public static void remove(BlockState state, Level world, BlockPos pos, boolean setState, boolean drop) {
        removeRightPiece(state, world, pos);
        if (setState) {
            if (drop) {
                // TODO: force drop item
            }
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(world, pos)), new CMessageCloseGui(pos));
    }
    
//    @Override
//    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
//        if(!world.isClientSide) {
//            remove(state, world, pos, false, false);
//        }
//    }
    
    @Override
    public void onRemove(BlockState arg, Level arg2, BlockPos arg3, BlockState arg4, boolean bl) {
        if(!arg2.isClientSide) {
            remove(arg, arg2, arg3, false, false);
        }
        super.onRemove(arg, arg2, arg3, arg4, bl);
    }
    
    public static PacketDistributor.TargetPoint point(Level world, BlockPos bp) {
        return new PacketDistributor.TargetPoint(bp.getX(), bp.getY(), bp.getZ(), 64.0, world.dimension());
    }
    
    @Override
    public VoxelShape getOcclusionShape(BlockState arg, BlockGetter arg2, BlockPos arg3) {
        return Shapes.empty();
    }
}
