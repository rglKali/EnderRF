package com.benbenlaw.enderrf.block;

import com.benbenlaw.enderrf.EnderRF;
import com.benbenlaw.enderrf.item.EnderRFItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EnderRFBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(EnderRF.MOD_ID);

    public static final DeferredBlock<Block> ENDER_BATTERY = registerBlockWithoutBlockItem("ender_battery",
            () -> new BlockEnderBattery(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.STONE)
                    .noOcclusion()));

    public static final DeferredBlock<Block> ENDER_FLASK = registerBlockWithoutBlockItem("ender_flask",
            () -> new BlockEnderFlask(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.STONE)
                    .noOcclusion()));

    private static <T extends Block> DeferredBlock<T> registerBlockWithoutBlockItem(String name, Supplier<T> block) {
        return (DeferredBlock<T>) BLOCKS.register(name, block);
    }

}
