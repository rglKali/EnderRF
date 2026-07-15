package com.benbenlaw.enderrf.util;

import java.util.List;

import codechicken.enderstorage.api.EnderStoragePlugin;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.enderstorage.manager.EnderStorageManager;
import net.minecraft.server.level.ServerPlayer;

public class EnderChemicalStoragePlugin implements EnderStoragePlugin<EnderChemicalStorage> {

    @Override
    public EnderChemicalStorage createEnderStorage(EnderStorageManager manager, Frequency freq) {
        return new EnderChemicalStorage(manager, freq);
    }

    @Override
    public StorageType<EnderChemicalStorage> identifier() {
        return EnderChemicalStorage.TYPE;
    }

    @Override
    public void sendClientInfo(ServerPlayer player, List<EnderChemicalStorage> list) {

    }
}