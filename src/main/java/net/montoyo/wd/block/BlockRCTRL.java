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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import net.montoyo.mcef.utilities.Log;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.entity.*;
import net.montoyo.wd.init.BlockInit;
import net.montoyo.wd.item.ItemLinker;
import net.montoyo.wd.net.Messages;
import net.montoyo.wd.net.client.CMessageCloseGui;
import org.jetbrains.annotations.Nullable;

public class BlockRCTRL extends WDBlockContainer {

    public static final EnumProperty<DefaultPeripheral> type = BlockPeripheral.type;
    public static final DirectionProperty facing = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private static final Property<?>[] properties = new Property<?>[] {type, facing};

    public BlockRCTRL() {
        super(BlockBehaviour.Properties.of(Material.STONE).strength(1.5f, 10.f));
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


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityRCtrl(pos, state);
    }

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
        System.out.println(te);
        if(te instanceof TileEntityRCtrl)
            return ((TileEntityRCtrl) te).onRightClick(player, hand);
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
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if(world.isClientSide)
            return;
        if(placer instanceof Player) {
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

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborType, BlockPos neighbor, boolean isMoving) {
        BlockEntity te = world.getBlockEntity(pos);
        if(te != null && te instanceof TileEntityPeripheralBase)
            ((TileEntityPeripheralBase) te).onNeighborChange(neighborType, neighbor);

        if(world.isClientSide)
            return;

        if(neighbor.getX() == pos.getX() && neighbor.getY() == pos.getY() - 1 && neighbor.getZ() == pos.getZ() && world.isEmptyBlock(neighbor)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
//            dropBlockAsItem(world, pos, state, 0); //TODO Loottable
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(world, pos)), new CMessageCloseGui(pos));
        }
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(!world.isClientSide) {
            Messages.INSTANCE.send(PacketDistributor.NEAR.with(() -> point(world, pos)), new CMessageCloseGui(pos));
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        playerDestroy(level, null, pos, level.getBlockState(pos), null, null);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if(!world.isClientSide) {
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
