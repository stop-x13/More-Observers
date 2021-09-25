package com.stebars.moreobserversmod.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

// Most code here taken from ObserverBlock, with POWERED changed to TRIGGERED except in some places

public class ToggleObserverBlock extends ObserverBlock {
	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
	// "Triggered" holds the value the vanilla observer would have, i.e. it pulses for 2 ticks when observed state changes
	// Then we flip the value of "powered" whenever "triggered" goes high.

	public static final int IMMUNITY_TICKS = 25;
	// After changing powered state, waits this number of ticks before being able to change again

	public ToggleObserverBlock() {
		super(Block.Properties.copy(Blocks.OBSERVER));
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> p_206840_1_) {
		p_206840_1_.add(FACING, POWERED, TRIGGERED);
	}

	@Override
	public void tick(BlockState state, ServerWorld p_225534_2_, BlockPos p_225534_3_, Random p_225534_4_) {
		if (state.getValue(TRIGGERED)) {
			p_225534_2_.setBlock(p_225534_3_, state.setValue(TRIGGERED, Boolean.valueOf(false)), 2);
		} else {
			p_225534_2_.setBlock(p_225534_3_, state.setValue(TRIGGERED, Boolean.valueOf(true))
					.setValue(POWERED, !Boolean.valueOf(state.getValue(POWERED))), 2);
			p_225534_2_.getBlockTicks().scheduleTick(p_225534_3_, this, IMMUNITY_TICKS);
		}

		this.updateNeighborsInFront(p_225534_2_, p_225534_3_, state);
	}

	@Override
	public BlockState updateShape(BlockState p_196271_1_, Direction p_196271_2_, BlockState p_196271_3_, IWorld p_196271_4_, BlockPos p_196271_5_, BlockPos p_196271_6_) {
		if (p_196271_1_.getValue(FACING) == p_196271_2_ && !p_196271_1_.getValue(TRIGGERED)) {
			this.startSignal(p_196271_4_, p_196271_5_);
		}

		return super.updateShape(p_196271_1_, p_196271_2_, p_196271_3_, p_196271_4_, p_196271_5_, p_196271_6_);
	}

	private void startSignal(IWorld p_203420_1_, BlockPos p_203420_2_) {
		if (!p_203420_1_.isClientSide() && !p_203420_1_.getBlockTicks().hasScheduledTick(p_203420_2_, this)) {
			p_203420_1_.getBlockTicks().scheduleTick(p_203420_2_, this, 2);
		}
	}

	@Override
	public void onPlace(BlockState p_220082_1_, World p_220082_2_, BlockPos p_220082_3_, BlockState p_220082_4_, boolean p_220082_5_) {
		if (!p_220082_1_.is(p_220082_4_.getBlock())) {
			if (!p_220082_2_.isClientSide() && p_220082_1_.getValue(TRIGGERED) && !p_220082_2_.getBlockTicks().hasScheduledTick(p_220082_3_, this)) {
				BlockState blockstate = p_220082_1_.setValue(TRIGGERED, Boolean.valueOf(false));
				p_220082_2_.setBlock(p_220082_3_, blockstate, 18);
				this.updateNeighborsInFront(p_220082_2_, p_220082_3_, blockstate);
			}
		}
	}

	@Override
	public void onRemove(BlockState p_196243_1_, World p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_, boolean p_196243_5_) {
		if (!p_196243_1_.is(p_196243_4_.getBlock())) {
			if (!p_196243_2_.isClientSide && p_196243_1_.getValue(TRIGGERED) && p_196243_2_.getBlockTicks().hasScheduledTick(p_196243_3_, this)) {
				this.updateNeighborsInFront(p_196243_2_, p_196243_3_, p_196243_1_.setValue(TRIGGERED, Boolean.valueOf(false)));
			}
		}
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return state.getValue(POWERED) ? 10 : super.getLightValue(state, world, pos);
	}
	
	// Don't want to send signal in other directions, e.g. forward (because the nose), because that would make it harder to detect
	// redstone signal changes from e.g. buttons.
}
