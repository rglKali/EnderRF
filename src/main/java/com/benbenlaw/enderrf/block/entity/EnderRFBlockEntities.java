package com.benbenlaw.enderrf.block.entity;

import com.benbenlaw.enderrf.EnderRF;
import com.benbenlaw.enderrf.block.EnderRFBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class EnderRFBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, EnderRF.MOD_ID);


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEnderBattery>> TILE_ENDER_BATTERY =
            register("tile_ender_battery", () ->
                    BlockEntityType.Builder.of(TileEnderBattery::new, EnderRFBlocks.ENDER_BATTERY.get()));


    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                EnderRFBlockEntities.TILE_ENDER_BATTERY.get(), TileEnderBattery::getEnergyHandler);


    }

    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(@Nonnull String name, @Nonnull Supplier<BlockEntityType.Builder<T>> initializer) {
        return BLOCK_ENTITIES.register(name, () -> initializer.get().build(null));
    }

}
