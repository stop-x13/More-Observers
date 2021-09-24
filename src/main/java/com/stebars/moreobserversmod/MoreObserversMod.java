package com.stebars.moreobserversmod;

import com.stebars.moreobserversmod.blocks.DiscernerBlock;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod(MoreObserversMod.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MoreObserversMod {
	public final static String MOD_ID = "moreobservers";

	public static final Block DISCERNER_BLOCK = new DiscernerBlock()
			.setRegistryName(new ResourceLocation(MOD_ID, "discerner"));
	public static final Item DISCERNER_ITEM = new BlockItem(DISCERNER_BLOCK,
			new Item.Properties().tab(ItemGroup.TAB_REDSTONE)).setRegistryName(new ResourceLocation(MOD_ID, "discerner"));


	public MoreObserversMod() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(DISCERNER_BLOCK);
	}

	@SubscribeEvent
	public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(DISCERNER_ITEM);
	}
	
	// TODO: observer variant that functions as a T-flip-flop, ie detects pulses, outputs constant
	// TODO: observer variant that detects mobs in region (see Create Irrigation lava code). Will let you detect through walls
	// TODO: observer variant that detects distance to first solid block
	// TODO: observer that pulses for any changes in the 5-long line it's looking at (would need block entity to store it, I think?)
}
