package com.benbenlaw.enderrf.tile;

import static mekanism.common.capabilities.Capabilities.CHEMICAL;

import com.benbenlaw.enderrf.EnderRF;
import com.benbenlaw.enderrf.block.EnderRFBlocks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EnderRFTiles {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister
            .create(BuiltInRegistries.BLOCK_ENTITY_TYPE, EnderRF.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEnderBattery>> TILE_ENDER_BATTERY = TILES
            .register("tile_ender_battery", () -> BlockEntityType.Builder
                    .of(TileEnderBattery::new, EnderRFBlocks.ENDER_BATTERY.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEnderFlask>> TILE_ENDER_FLASK = TILES
            .register("tile_ender_flask",
                    () -> BlockEntityType.Builder.of(TileEnderFlask::new, EnderRFBlocks.ENDER_FLASK.get()).build(null));

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, EnderRFTiles.TILE_ENDER_BATTERY.get(), TileEnderBattery::getEnergyHandler);
        event.registerBlockEntity(CHEMICAL.block(), EnderRFTiles.TILE_ENDER_FLASK.get(), TileEnderFlask::getChemicalHandler);
    }

    public static void register(IEventBus bus) {
        TILES.register(bus);
    }
}
