package com.benbenlaw.enderrf.plugin;

import java.util.List;

import com.benbenlaw.enderrf.storage.EnderEnergyStorage;

import codechicken.enderstorage.api.EnderStoragePlugin;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.enderstorage.manager.EnderStorageManager;
import net.minecraft.server.level.ServerPlayer;

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