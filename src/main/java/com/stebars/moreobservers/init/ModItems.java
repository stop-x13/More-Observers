package com.stebars.moreobservers.init;

import com.stebars.moreobservers.MoreObservers;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MoreObservers.MOD_ID);

    public static final RegistryObject<BlockItem> DISCERNER_BLOCK = ITEMS.register("discerner", 
        () -> new BlockItem(ModBlocks.DISCERNER_BLOCK.get(), new Item.Properties().tab(MoreObservers.CREATIVE_MODE_TAB)));
    
    public static final RegistryObject<BlockItem> MOB_OBSERVER_BLOCK = ITEMS.register("mob_observer", 
        () -> new BlockItem(ModBlocks.MOB_OBSERVER_BLOCK.get(), new Item.Properties().tab(MoreObservers.CREATIVE_MODE_TAB)));

    public static final RegistryObject<BlockItem> SURVEYOR_BLOCK = ITEMS.register("surveyor", 
        () -> new BlockItem(ModBlocks.SURVEYOR_BLOCK.get(), new Item.Properties().tab(MoreObservers.CREATIVE_MODE_TAB)));

    public static final RegistryObject<BlockItem> TOGGLE_OBSERVER_BLOCK = ITEMS.register("toggle_observer", 
        () -> new BlockItem(ModBlocks.TOGGLE_OBSERVER_BLOCK.get(), new Item.Properties().tab(MoreObservers.CREATIVE_MODE_TAB)));

}
