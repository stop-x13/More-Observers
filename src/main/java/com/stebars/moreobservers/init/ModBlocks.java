package com.stebars.moreobservers.init;


import com.stebars.moreobservers.MoreObservers;
import com.stebars.moreobservers.blocks.DiscernerBlock;
import com.stebars.moreobservers.blocks.MobObserverBlock;
import com.stebars.moreobservers.blocks.SurveyorBlock;
import com.stebars.moreobservers.blocks.ToggleObserverBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MoreObservers.MOD_ID);

    public static final RegistryObject<Block> DISCERNER_BLOCK = BLOCKS.register("discerner", 
        () -> new DiscernerBlock(Properties.copy(Blocks.OBSERVER)
			.strength(3.0F)
			.requiresCorrectToolForDrops()
			.isRedstoneConductor(ModBlocks::never)
			.lightLevel(DiscernerBlock.getLightValue())));

	public static final RegistryObject<Block> MOB_OBSERVER_BLOCK = BLOCKS.register("mob_observer", 
		() -> new MobObserverBlock(Properties.copy(Blocks.OBSERVER)
			.strength(3.0F)
			.requiresCorrectToolForDrops()
			.isRedstoneConductor(ModBlocks::never)
			.lightLevel(MobObserverBlock.getLightValue())));


	public static final RegistryObject<Block> SURVEYOR_BLOCK = BLOCKS.register("surveyor", 
		() -> new SurveyorBlock(Properties.copy(Blocks.OBSERVER)
			.strength(3.0F)
			.requiresCorrectToolForDrops()
			.isRedstoneConductor(ModBlocks::never)
			.lightLevel(SurveyorBlock.getLightValue())));

	public static final RegistryObject<Block> TOGGLE_OBSERVER_BLOCK = BLOCKS.register("toggle_observer", 
		() -> new ToggleObserverBlock(Properties.copy(Blocks.OBSERVER)
			.strength(3.0F)
			.requiresCorrectToolForDrops()
			.isRedstoneConductor(ModBlocks::never)
			.lightLevel(ToggleObserverBlock.getLightValue())));


	private static Boolean never(BlockState state, BlockGetter getter, BlockPos pos) {
		return false;
	}
}