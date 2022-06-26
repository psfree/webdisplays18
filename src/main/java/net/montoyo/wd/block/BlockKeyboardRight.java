/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.core.IPeripheral;
import net.montoyo.wd.entity.TileEntityKeyboard;
import net.montoyo.wd.init.BlockInit;
import net.montoyo.wd.item.ItemLinker;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Vector3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.CallbackI;

import java.util.List;
import java.util.Objects;

public class BlockKeyboardRight extends Block implements IPeripheral {

    public static final DirectionProperty facing = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape KEYBOARD_AABB = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0 / 16.0, 1.0);

    public BlockKeyboardRight() {
        super(Properties.of(Material.STONE)
                .strength(1.5f, 10.f));

        //("keyboard")
        //fullBlock = false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(facing);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return KEYBOARD_AABB;
    }

    private TileEntityKeyboard getTileEntity(Level world, BlockPos pos) {
        for (Direction nf : Direction.Plane.HORIZONTAL) {
            BlockPos np = pos.above(nf.getNormal().getX()); //TODO is X correct?
            BlockState ns = world.getBlockState(np);

            if (ns.getBlock() instanceof BlockPeripheral && ns.getValue(BlockPeripheral.type) == DefaultPeripheral.KEYBOARD) {
                BlockEntity te = world.getBlockEntity(np);
                if (te != null && te instanceof TileEntityKeyboard)
                    return (TileEntityKeyboard) te;

                break;
            }
        }

        return null;
    }

    @Override
    public boolean connect(Level world, BlockPos pos, BlockState state, Vector3i scrPos, BlockSide scrSide) {
        TileEntityKeyboard keyboard = getTileEntity(world, pos);
        return keyboard != null && keyboard.connect(world, pos, state, scrPos, scrSide);
    }

    public static boolean checkNeighborhood(Level world, BlockPos bp, BlockPos ignore) {
        for (Direction neighbor : Direction.Plane.HORIZONTAL) {
            BlockPos np = bp.above(neighbor.getNormal().getX()); //TODO is X correct?

            if (ignore == null || !np.equals(ignore)) {
                BlockState state = world.getBlockState(np);

                if (state.getBlock() instanceof BlockPeripheral) {
                    if (state.getValue(BlockPeripheral.type) == DefaultPeripheral.KEYBOARD)
                        return false;
                } else if (state.getBlock() instanceof BlockKeyboardRight)
                    return false;
            }
        }

        return true;
    }

    public void removeLeftPiece(Level world, BlockPos pos, boolean dropItem) {
        for (Direction nf : Direction.Plane.HORIZONTAL) {
            BlockPos np = pos.above(nf.getNormal().getX()); //TODO is X correct?
            BlockState ns = world.getBlockState(np);

            if (ns.getBlock() instanceof BlockPeripheral && ns.getValue(BlockPeripheral.type) == DefaultPeripheral.KEYBOARD) {
               /* if(dropItem)
                    if(world instanceof ServerLevel serverWorld) {
                       // ns.getBlock().getDrops(ns, serverWorld, np,0);
                    } */
                world.setBlock(np, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                break;
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighbor, boolean isMoving) {
        if (world.isClientSide())
            return;

        if (neighbor.getX() == pos.getX() && neighbor.getY() == pos.getY() - 1 && neighbor.getZ() == pos.getZ()) {
            removeLeftPiece(world, pos, true);
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player ply, boolean willHarvest, FluidState fluid) {
        if (!world.isClientSide)
            removeLeftPiece(world, pos, !ply.isCreative());

        return super.onDestroyedByPlayer(state, world, pos, ply, willHarvest, fluid);
    }


    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        double rpos = (entity.getY() - ((double) pos.getY())) * 16.0;
        if (!world.isClientSide && rpos >= 1.0 && rpos <= 2.0 && Math.random() < 0.25) {
            TileEntityKeyboard tek = getTileEntity(world, pos);

            if (tek != null)
                tek.simulateCat(entity);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.getItemInHand().isEmpty()) //Keyboard
            return false;

        //Special checks for the keyboard
        BlockPos pos = context.getClickedPos().offset(context.getHorizontalDirection().getNormal());
        if (context.getLevel().getBlockState(pos.below()) == Blocks.AIR.defaultBlockState() || !BlockKeyboardRight.checkNeighborhood(context.getLevel(), pos, null))
            return true;

        int f = (int) Math.floor(((double) (context.getPlayer().getYRot() * 4.0f / 360.0f)) + 2.5) & 3;
        Vec3i dir = Direction.from2DDataValue(f).getNormal();
        BlockPos left = pos.offset(dir);
        BlockPos right = pos.subtract(dir);

        if (context.getLevel().getBlockState(right) == Blocks.AIR.defaultBlockState() && !(context.getLevel().getBlockState(right.below()) == Blocks.AIR.defaultBlockState()) && BlockKeyboardRight.checkNeighborhood(context.getLevel(), right, null))
            return false;
        else
            return !(context.getLevel().getBlockState(left) == Blocks.AIR.defaultBlockState() && !(context.getLevel().getBlockState(left.below()) == Blocks.AIR.defaultBlockState()) && BlockKeyboardRight.checkNeighborhood(context.getLevel(), left, null));
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (canBeReplaced(state, new BlockPlaceContext(player, hand, itemStack, hit))) {
                int f = (int) Math.floor(((double) (player.getYRot() * 4.0f / 360.0f)) + 2.5) & 3;
                Vec3i dir = Direction.from2DDataValue(f).getNormal();
                level.setBlock(pos.offset(dir), BlockInit.blockKeyBoard.get().defaultBlockState(), UPDATE_ALL_IMMEDIATE);
                return InteractionResult.SUCCESS;
            }
        }

        if(player.getItemInHand(hand).getItem() instanceof ItemLinker)
            return InteractionResult.PASS;

        TileEntityKeyboard tek = getTileEntity(level, pos);
        if(tek != null)
            return tek.onRightClick(player, hand);

        return InteractionResult.PASS;
    }

}
