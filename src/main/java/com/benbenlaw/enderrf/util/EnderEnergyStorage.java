package com.benbenlaw.enderrf.util;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.enderstorage.manager.EnderStorageManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnderEnergyStorage extends AbstractEnderStorage implements IEnergyStorage {

    public static final StorageType<EnderEnergyStorage> TYPE =
            new StorageType<>("energy");

    public static final int CAPACITY = 100000;

    private int energy;

    public EnderEnergyStorage(EnderStorageManager manager, Frequency freq) {
        super(manager, freq);
    }

    @Override
    public CompoundTag saveToTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("energy", energy);
        return tag;
    }

    @Override
    public void loadFromTag(CompoundTag tag, HolderLookup.Provider registries) {
        this.energy = tag.getInt("energy");
    }

    @Override
    public void clearStorage() {
        energy = 0;
        setDirty();
    }

    @Override
    public String type() {
        return "energy";
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = Math.min(CAPACITY - energy, maxReceive);

        if (!simulate && received > 0) {
            energy += received;
            setDirty();
        }

        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(energy, maxExtract);

        if (!simulate && extracted > 0) {
            energy -= extracted;
            setDirty();
        }

        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return CAPACITY;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }


}