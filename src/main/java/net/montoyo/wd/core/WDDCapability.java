/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.core;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

public class WDDCapability implements IWDDCapability {

    public static class Factory implements Callable<IWDDCapability> {

        @Override
        public IWDDCapability call() {
            return new WDDCapability();
        }

    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        public static Capability<IWDDCapability> cap = CapabilityManager.get(new CapabilityToken<>(){});
        private final LazyOptional<IWDDCapability> INSTANCE = LazyOptional.of(this::createWDDCapability);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction arg) {
            return cap == capability ?  INSTANCE.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (INSTANCE.isPresent()) {
               INSTANCE.ifPresent(cap -> tag.put("Tag", tag));
            }
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (INSTANCE.isPresent()) {
                INSTANCE.ifPresent(cap -> {
                    tag.get("Tag");
                });
            }
        }

        @Nonnull
        private IWDDCapability createWDDCapability() {
            return cap == null ? new WDDCapability() : (IWDDCapability) cap;
        }

    }

    private boolean firstRun = true;

    private WDDCapability() {
    }

    @Override
    public boolean isFirstRun() {
        return firstRun;
    }

    @Override
    public void clearFirstRun() {
        firstRun = false;
    }

    @Override
    public void cloneTo(IWDDCapability dst) {
        if(!isFirstRun())
            dst.clearFirstRun();
    }

}
