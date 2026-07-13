package com.benbenlaw.enderrf.block;

import com.benbenlaw.enderrf.EnderRF;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EnderRFBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EnderRF.MOD_ID);

    // source:
    // https://github.com/TheCBProject/EnderStorage/blob/d801697cac7493095d8c98b8d4f31a9f6f76f757/src/main/java/codechicken/enderstorage/init/EnderStorageModContent.java#L47
    private static final BlockBehaviour.Properties blockProps = Block.Properties.of().mapColor(MapColor.STONE)
            .strength(20, 100);

    public static final DeferredBlock<Block> ENDER_BATTERY = BLOCKS.register("ender_battery",
            () -> new BlockEnderBattery(blockProps));

    public static final DeferredBlock<Block> ENDER_FLASK = BLOCKS.register("ender_flask",
            () -> new BlockEnderFlask(blockProps));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
