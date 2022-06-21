/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.core.IPeripheral;
import net.montoyo.wd.entity.TileEntityKeyboard;
import net.montoyo.wd.item.ItemLinker;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Vector3i;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockKeyboardRight extends Block implements IPeripheral {

    public static final IntegerProperty facing = IntegerProperty.create("facing", 0, 3);
    public static final AABB KEYBOARD_AABB = new AABB(0.0, 0.0, 0.0, 1.0, 1.0 / 16.0, 1.0);

    public BlockKeyboardRight() {
        super(Properties.of(Material.STONE)
                .strength(1.5f, 10.f));
                //("keyboard")
        //fullBlock = false;
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, properties);
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(BlockState state, BlockGetter world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    @Nonnull
    public AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        return KEYBOARD_AABB;
    }

    @Override
    @Nonnull
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(facing, meta);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(facing);
    }

    @Override
    @Nonnull
    public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, @Nonnull Level world, @Nonnull BlockPos pos, EntityPlayer player) {
        return new ItemStack(WebDisplays.INSTANCE.blockPeripheral, 1, 0);
    }

    private TileEntityKeyboard getTileEntity(Level world, BlockPos pos) {
        for(EnumFacing nf: EnumFacing.HORIZONTALS) {
            BlockPos np = pos.add(nf.getDirectionVec());
            IBlockState ns = world.getBlockState(np);

            if(ns.getBlock() instanceof BlockPeripheral && ns.getValue(BlockPeripheral.type) == DefaultPeripheral.KEYBOARD) {
                TileEntity te = world.getTileEntity(np);
                if(te != null && te instanceof TileEntityKeyboard)
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

    @Override
    @Nonnull
    public EnumPushReaction getMobilityFlag(BlockState state) {
        return EnumPushReaction.IGNORE;
    }

    public static boolean checkNeighborhood(IBlockAccess world, BlockPos bp, BlockPos ignore) {
        for(EnumFacing neighbor: EnumFacing.HORIZONTALS) {
            BlockPos np = bp.add(neighbor.getDirectionVec());

            if(ignore == null || !np.equals(ignore)) {
                IBlockState state = world.getBlockState(np);

                if(state.getBlock() instanceof BlockPeripheral) {
                    if(state.getValue(BlockPeripheral.type) == DefaultPeripheral.KEYBOARD)
                        return false;
                } else if(state.getBlock() instanceof BlockKeyboardRight)
                    return false;
            }
        }

        return true;
    }

    public void removeLeftPiece(Level world, BlockPos pos, boolean dropItem) {
        for(EnumFacing nf: EnumFacing.HORIZONTALS) {
            BlockPos np = pos.add(nf.getDirectionVec());
            BlockState ns = world.getBlockState(np);

            if(ns.getBlock() instanceof BlockPeripheral && ns.getValue(BlockPeripheral.type) == DefaultPeripheral.KEYBOARD) {
                if(dropItem)
                    ns.getBlock().dropBlockAsItem(world, np, ns, 0);

                world.setBlockToAir(np);
                break;
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborType, BlockPos neighbor) {
        if(world.isClientSide)
            return;

        if(neighbor.getX() == pos.getX() && neighbor.getY() == pos.getY() - 1 && neighbor.getZ() == pos.getZ() && world.isAirBlock(neighbor)) {
            removeLeftPiece(world, pos, true);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public boolean removedByPlayer(@Nonnull BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player ply, boolean willHarvest) {
        if(!world.isClientSide)
            removeLeftPiece(world, pos, !ply.isCreative());

        return super.removedByPlayer(state, world, pos, ply, willHarvest);
    }

    @Override
    public void onBlockDestroyedByExplosion(Level world, BlockPos pos, Explosion explosionIn) {
        if(!world.isClientSide)
            removeLeftPiece(world, pos, true);
    }

    @Override
    public void onEntityCollidedWithBlock(Level world, BlockPos pos, BlockState state, Entity entity) {
        double rpos = (entity.getY() - ((double) pos.getY())) * 16.0;
        if(!world.isClientSide && rpos >= 1.0 && rpos <= 2.0 && Math.random() < 0.25) {
            TileEntityKeyboard tek = getTileEntity(world, pos);

            if(tek != null)
                tek.simulateCat(entity);
        }
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(player.isSneaking())
            return false;

        if(player.getHeldItem(hand).getItem() instanceof ItemLinker)
            return false;

        TileEntityKeyboard tek = getTileEntity(world, pos);
        if(tek != null)
            return tek.onRightClick(player, hand, BlockSide.values()[facing.ordinal()]);

        return false;
    }

}
