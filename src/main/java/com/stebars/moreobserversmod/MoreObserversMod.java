package com.stebars.moreobserversmod;

import com.stebars.moreobserversmod.blocks.DiscernerBlock;
import com.stebars.moreobserversmod.blocks.MobserverBlock;
import com.stebars.moreobserversmod.blocks.ToggleObserverBlock;

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

	public static final Block TOGGLE_OBSERVER_BLOCK = new ToggleObserverBlock()
			.setRegistryName(new ResourceLocation(MOD_ID, "toggle_observer"));
	public static final Item TOGGLE_OBSERVER_ITEM = new BlockItem(TOGGLE_OBSERVER_BLOCK,
			new Item.Properties().tab(ItemGroup.TAB_REDSTONE)).setRegistryName(new ResourceLocation(MOD_ID, "toggle_observer"));

	public static final Block MOBSERVER_BLOCK = new MobserverBlock()
			.setRegistryName(new ResourceLocation(MOD_ID, "mobserver"));
	public static final Item MOBSERVER_ITEM = new BlockItem(MOBSERVER_BLOCK,
			new Item.Properties().tab(ItemGroup.TAB_REDSTONE)).setRegistryName(new ResourceLocation(MOD_ID, "mobserver"));


	public MoreObserversMod() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(DISCERNER_BLOCK);
		event.getRegistry().registerAll(TOGGLE_OBSERVER_BLOCK);
		event.getRegistry().registerAll(MOBSERVER_BLOCK);
	}

	@SubscribeEvent
	public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(DISCERNER_ITEM);
		event.getRegistry().registerAll(TOGGLE_OBSERVER_ITEM);
		event.getRegistry().registerAll(MOBSERVER_ITEM);
	}
	
	// TODO: observer variant that detects distance to first solid block
}
