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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
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
import org.jetbrains.annotations.Nullable;

public class BlockKeyboardLeft extends BlockPeripheral {

    public static final EnumProperty<DefaultPeripheral> type = EnumProperty.create("type", DefaultPeripheral.class);
    public static final DirectionProperty facing = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private static final Property<?>[] properties = new Property<?>[] { type, facing };

    public BlockKeyboardLeft() {
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(properties);
    }

    //    @Nullable TODO: Fix
//    @Override
//    public BlockState getStateForPlacement(BlockPlaceContext context) {
//        Direction rot = Direction.fromYRot(placer.getYHeadRot());
//        return defaultBlockState().setValue(type, DefaultPeripheral.fromMetadata(meta)).setValue(facing, rot);
//
//
//        return getStateForPlacement(context);
//    }
//
//    public BlockState getStateForPlacement(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Direction nocare, float hitX,
//                                            float hitY, float hitZ, int meta, @Nonnull LivingEntity placer, InteractionHand hand) {
//    }

//    @Override
//    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
//        for(DefaultPeripheral dp : DefaultPeripheral.values())
//            list.add(new ItemStack(getItem(), 1, dp.toMetadata(0)));
//    }

//    @Override
//    @Nonnull
//    public IBlockState getStateFromMeta(int meta) {
//        DefaultPeripheral dp = DefaultPeripheral.fromMetadata(meta);
//        IBlockState state = getDefaultState().withProperty(type, dp);
//
//        if(dp.hasFacing())
//            state = state.withProperty(facing, (meta >> 2) & 3);
//
//        return state;
//    }
//
//    @Override
//    public int getMetaFromState(IBlockState state) {
//        return state.getValue(type).toMetadata(state.getValue(facing));
//    }


    /*@Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        BlockEntityType.BlockEntitySupplier<? extends BlockEntity> cls = state.getValue(type).getTEClass();
        if(cls == null)
            return null;

        try {
            return cls.create(pos, state);
        } catch(Throwable t) {
            Log.errorEx("Couldn't instantiate peripheral TileEntity:", t);
        }

        return null;
    } */

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

//    @Override
//    public int damageDropped(IBlockState state) {
//        return state.getValue(type).toMetadata(0);
//    }


    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(player.isShiftKeyDown())
            return InteractionResult.FAIL;

        if(player.getItemInHand(hand).getItem() instanceof ItemLinker)
            return InteractionResult.FAIL;

        BlockEntity te = world.getBlockEntity(pos);

        if(te instanceof TileEntityKeyboard)
            return ((TileEntityKeyboard) te).onRightClick(player, hand);
        else if(te instanceof TileEntityServer) {
            ((TileEntityServer) te).onPlayerRightClick(player);
            return InteractionResult.PASS;
        } else
            return InteractionResult.FAIL;
    }

//    @Override
//    public boolean isFullCube(IBlockState state) { TODO: FIx.
//        return state.getValue(type) != DefaultPeripheral.KEYBOARD;
//    }
//
//    @Override
//    public boolean isFullBlock(IBlockState state) {
//        return state.getValue(type) != DefaultPeripheral.KEYBOARD;
//    }
//
//    @Override
//    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
//        return state.getValue(type) != DefaultPeripheral.KEYBOARD;
//    }
//
//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
//        return state.getValue(type) != DefaultPeripheral.KEYBOARD;
//    }
//
//    @Override
//    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
//        return state.getValue(type) != DefaultPeripheral.KEYBOARD;
//    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(type) == DefaultPeripheral.KEYBOARD ? BlockKeyboardRight.KEYBOARD_AABB : Shapes.block();
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if(world.isClientSide)
            return;

        if(state.getValue(type) == DefaultPeripheral.KEYBOARD) {
            //Keyboard special treatment
            Direction f = state.getValue(facing);
            Vec3i dir = f.getClockWise().getNormal();
            BlockPos left = pos.offset(dir);
            BlockPos right = pos.subtract(dir);

            if(!world.isEmptyBlock(pos.below()) && BlockKeyboardRight.checkNeighborhood(world, pos, null)) {
                if(world.isEmptyBlock(right) && !world.isEmptyBlock(right.below()) && BlockKeyboardRight.checkNeighborhood(world, right, pos)) {
                    world.setBlock(right, BlockInit.blockKbRight.get().defaultBlockState().setValue(BlockKeyboardRight.facing, f), 3);
                    return;
                } else if(world.isEmptyBlock(left) && !world.isEmptyBlock(left.below()) && BlockKeyboardRight.checkNeighborhood(world, left, pos)) {
                    world.setBlock(left, state, 3);
                    world.setBlock(pos, BlockInit.blockKbRight.get().defaultBlockState().setValue(BlockKeyboardRight.facing, f), 3);
                    return;
                }
            }

            //Not good; remove this shit...
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            if(!(placer instanceof Player) || !((Player) placer).isCreative()) {
//                dropBlockAsItem(world, pos, state, 0); TODO: Loottable?
            }
        } else if(placer instanceof Player) {
            BlockEntity te = world.getBlockEntity(pos);

            if(te instanceof TileEntityServer)
                ((TileEntityServer) te).setOwner((Player) placer);
            else if(te instanceof TileEntityInterfaceBase)
                ((TileEntityInterfaceBase) te).setOwner((Player) placer);
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }

    private void removeRightPiece(Level world, BlockPos pos) {
        for(Direction nf: Direction.Plane.HORIZONTAL) {
            BlockPos np = pos.offset(nf.getNormal());

            if(world.getBlockState(np).getBlock() instanceof BlockKeyboardRight) {
                world.setBlock(np, Blocks.AIR.defaultBlockState(), 3);
                break;
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborType, BlockPos neighbor, boolean isMoving) {
        BlockEntity te = world.getBlockEntity(pos);
        if(te != null && te instanceof TileEntityPeripheralBase)
            ((TileEntityPeripheralBase) te).onNeighborChange(neighborType, neighbor);

        if(world.isClientSide || state.getValue(type) != DefaultPeripheral.KEYBOARD)
            return;

        if(neighbor.getX() == pos.getX() && neighbor.getY() == pos.getY() - 1 && neighbor.getZ() == pos.getZ() && world.isEmptyBlock(neighbor)) {
            removeRightPiece(world, pos);
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
//            dropBlockAsItem(world, pos, state, 0); //TODO Loottable
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(world, pos)), new CMessageCloseGui(pos));
        }
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(!world.isClientSide) {
            if(state.getBlock() == this && state.getValue(type) == DefaultPeripheral.KEYBOARD)
                removeRightPiece(world, pos);

            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(world, pos)), new CMessageCloseGui(pos));
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        playerDestroy(level, null, pos, level.getBlockState(pos), null, null);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if(!world.isClientSide && world.getBlockState(pos).getValue(type) == DefaultPeripheral.KEYBOARD) {
            double rpos = (entity.getY() - ((double) pos.getY())) * 16.0;

            if(rpos >= 1.0 && rpos <= 2.0 && Math.random() < 0.25) {
                BlockEntity te = world.getBlockEntity(pos);

                if(te != null && te instanceof TileEntityKeyboard)
                    ((TileEntityKeyboard) te).simulateCat(entity);
            }
        }
    }

    public static PacketDistributor.TargetPoint point(Level world, BlockPos bp) {
        return new PacketDistributor.TargetPoint(bp.getX(), bp.getY(), bp.getZ(), 64.0, world.dimension());
    }

}
