/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.data.ServerData;
import net.montoyo.wd.utilities.NameUUIDPair;
import net.montoyo.wd.utilities.Util;

import javax.annotation.Nonnull;

public class TileEntityServer extends BlockEntity {

    private NameUUIDPair owner;

    public TileEntityServer(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        owner = Util.readOwnerFromNBT(tag);
    }

    @Override
    @Nonnull
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        super.serializeNBT();
        return Util.writeOwnerToNBT(tag, owner);
    }

    public void setOwner(Player ep) {
        owner = new NameUUIDPair(ep.getGameProfile());
        setChanged();
    }

    public void onPlayerRightClick(Player ply) {
        if(level.isClientSide)
            return;

        if(WebDisplays.INSTANCE.miniservPort == 0)
            Util.toast(ply, "noMiniserv");
        else if(owner != null && ply instanceof ServerPlayer)
            (new ServerData(getBlockPos(), owner)).sendTo((ServerPlayer) ply);
    }

}
