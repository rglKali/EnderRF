package com.benbenlaw.enderrf.storage;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.lib.fluid.FluidUtils;
import mekanism.api.Action;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.functions.ConstantPredicates;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;

// Acts as a BasicChemicalTank wrapper for the Ender Storage API
public class EnderChemicalStorage extends AbstractEnderStorage implements IChemicalTank, IChemicalHandler {
    public static final StorageType<EnderChemicalStorage> TYPE = new StorageType<>("chemical");

    // 64 buckets -- x4 ratio with fluids
    public static final int CAPACITY = 64 * FluidUtils.B;

    class Tank extends BasicChemicalTank {
        public Tank(long capacity) {
            super(capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(),
                    ConstantPredicates.alwaysTrue(), null, null, null);
        }

        @Override
        public void onContentsChanged() {
            setDirty();
        }
    }

    private Tank tank;

    public EnderChemicalStorage(EnderStorageManager manager, Frequency freq) {
        super(manager, freq);
        this.tank = new Tank(CAPACITY);
    }

    @Override
    public void onContentsChanged() {
        this.setDirty();
    }

    // Ender Storage methods
    @Override
    public CompoundTag saveToTag(Provider provider) {
        return this.tank.serializeNBT(provider);
    }

    @Override
    public void loadFromTag(CompoundTag tag, Provider provider) {
        this.tank.deserializeNBT(provider, tag);
    }

    @Override
    public void clearStorage() {
        this.tank.setEmpty();
        this.setDirty();
    }

    @Override
    public String type() {
        return "chemical";
    }

    // Chemical Tank methods
    @Override
    public ChemicalStack getStack() {
        return this.tank.getStack();
    }

    @Override
    public void setStack(ChemicalStack stack) {
        this.tank.setStack(stack);
    }

    @Override
    public void setStackUnchecked(ChemicalStack stack) {
        this.tank.setStackUnchecked(stack);
    }

    @Override
    public long getCapacity() {
        return this.tank.getCapacity();
    }

    @Override
    public boolean isValid(ChemicalStack stack) {
        return this.tank.isValid(stack);
    }

    // Chemical Handler methods
    @Override
    public int getChemicalTanks() {
        return this.tank.getChemicalTanks();
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank) {
        return this.tank.getChemicalInTank(tank);
    }

    @Override
    public void setChemicalInTank(int tank, ChemicalStack stack) {
        this.tank.setChemicalInTank(tank, stack);
    }

    @Override
    public long getChemicalTankCapacity(int tank) {
        return this.tank.getChemicalTankCapacity(tank);
    }

    @Override
    public boolean isValid(int tank, ChemicalStack stack) {
        return this.tank.isValid(tank, stack);
    }

    @Override
    public ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        return this.tank.insertChemical(tank, stack, action);
    }

    @Override
    public ChemicalStack extractChemical(int tank, long amount, Action action) {
        return this.tank.extractChemical(tank, amount, action);
    }
}
