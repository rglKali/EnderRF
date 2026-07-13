package com.benbenlaw.enderrf.item;

import codechicken.enderstorage.item.ItemEnderStorage;
import com.benbenlaw.enderrf.EnderRF;
import com.benbenlaw.enderrf.block.EnderRFBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static codechicken.enderstorage.init.EnderStorageModContent.ENDER_TANK_BLOCK;

public class EnderRFItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EnderRF.MOD_ID);

    public static final DeferredItem<Item> ENDER_BATTERY_ITEM =
            ITEMS.register("ender_battery", () -> new ItemEnderStorage(EnderRFBlocks.ENDER_BATTERY.get()));


}





