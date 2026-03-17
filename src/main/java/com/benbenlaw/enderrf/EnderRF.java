package com.benbenlaw.enderrf;

import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.enderstorage.item.ItemEnderStorage;
import codechicken.enderstorage.manager.EnderStorageManager;
import com.benbenlaw.enderrf.block.EnderRFBlocks;
import com.benbenlaw.enderrf.block.entity.EnderRFBlockEntities;
import com.benbenlaw.enderrf.client.render.RenderTileEnderBattery;
import com.benbenlaw.enderrf.item.EnderRFItems;
import com.benbenlaw.enderrf.util.EnderEnergyStorage;
import com.benbenlaw.enderrf.util.EnderEnergyStoragePlugin;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.world.item.Items.ENDER_CHEST;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(EnderRF.MOD_ID)
public class EnderRF{
    public static final String MOD_ID = "enderrf";
    public static final Logger LOGGER = LogManager.getLogger();


    public EnderRF(final IEventBus eventBus, final ModContainer modContainer) {

        EnderRFItems.ITEMS.register(eventBus);
        EnderRFBlocks.BLOCKS.register(eventBus);
        EnderRFBlockEntities.BLOCK_ENTITIES.register(eventBus);

        EnderStorageManager.registerPlugin(new EnderEnergyStoragePlugin());

        eventBus.addListener(this::registerCapabilities);

        eventBus.addListener(this::networkingSetup);
        eventBus.addListener(this::addCreativeTabContents);
        //modContainer.registerConfig(ModConfig.Type.STARTUP, EnableFixesConfig.SPEC, "bbl/fixes/fixes.toml");

    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        EnderRFBlockEntities.registerCapabilities(event);
    }

    public void networkingSetup(RegisterPayloadHandlersEvent event) {
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            BlockEntityRenderers.register(EnderRFBlockEntities.TILE_ENDER_BATTERY.get(), RenderTileEnderBattery::new);
        }
    }

    public void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            ItemStack craftingTable = event.getParentEntries().stream()
                    .filter(stack -> stack.getItem() == EnderStorageModContent.ENDER_TANK_BLOCK.get().asItem())
                    .findFirst()
                    .orElse(null);

            if (craftingTable != null) {
                ItemStack enderBattery = new ItemStack(EnderRFBlocks.ENDER_BATTERY.get());
                event.insertAfter(craftingTable, enderBattery, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            } else {
                event.accept(new ItemStack(EnderRFBlocks.ENDER_BATTERY.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }

}
