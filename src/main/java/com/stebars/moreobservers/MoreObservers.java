package com.stebars.moreobservers;

import com.stebars.moreobservers.init.ModBlocks;
import com.stebars.moreobservers.init.ModItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MoreObservers.MOD_ID)
public class MoreObservers {
	public static final String MOD_ID = "moreobservers";

	public MoreObservers() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
    }

	public static CreativeModeTab CREATIVE_MODE_TAB = new CreativeModeTab(MOD_ID) {
		@Override
		public ItemStack makeIcon() {
			return ModItems.DISCERNER_BLOCK.get().getDefaultInstance();
		}
	};

}
