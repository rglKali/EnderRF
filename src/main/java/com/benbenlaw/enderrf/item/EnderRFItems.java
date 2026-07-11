package com.benbenlaw.enderrf.item;

import com.benbenlaw.enderrf.EnderRF;
import com.benbenlaw.enderrf.block.EnderRFBlocks;

import codechicken.enderstorage.item.ItemEnderStorage;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EnderRFItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EnderRF.MOD_ID);

    public static final DeferredItem<Item> ENDER_BATTERY_ITEM = ITEMS.register("ender_battery",
            () -> new ItemEnderStorage(EnderRFBlocks.ENDER_BATTERY.get()));

    public static final DeferredItem<Item> ENDER_FLASK_ITEM = ITEMS.register("ender_flask",
            () -> new ItemEnderStorage(EnderRFBlocks.ENDER_FLASK.get()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
