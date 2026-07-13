package com.benbenlaw.enderrf.util;

import codechicken.enderstorage.api.EnderStoragePlugin;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.network.EnderStorageSPH;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class EnderEnergyStoragePlugin implements EnderStoragePlugin<EnderEnergyStorage> {

    @Override
    public EnderEnergyStorage createEnderStorage(EnderStorageManager manager, Frequency freq) {
        return new EnderEnergyStorage(manager, freq);
    }

    @Override
    public StorageType<EnderEnergyStorage> identifier() {
        return EnderEnergyStorage.TYPE;
    }

    @Override
    public void sendClientInfo(ServerPlayer player, List<EnderEnergyStorage> list) {

    }
}