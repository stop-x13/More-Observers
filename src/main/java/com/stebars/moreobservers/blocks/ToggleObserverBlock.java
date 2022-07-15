package com.stebars.moreobservers.blocks;

import java.util.Random;
import java.util.function.ToIntFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;


// Most code here taken from ObserverBlock, with POWERED changed to TRIGGERED except in some places

public class ToggleObserverBlock extends ObserverBlock {
	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
	// "Triggered" holds the value the vanilla observer would have, i.e. it pulses for 2 ticks when observed state changes
	// Then we flip the value of "powered" whenever "triggered" goes high.

	public static final int IMMUNITY_TICKS = 20;
	// After changing powered state, waits this number of ticks before being able to change again

	public ToggleObserverBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(POWERED, Boolean.valueOf(false)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED, TRIGGERED);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
		if (state.getValue(TRIGGERED)) {
			level.setBlock(pos, state.setValue(TRIGGERED, Boolean.valueOf(false)), 2);
		} else {
			level.setBlock(pos, state.setValue(TRIGGERED, Boolean.valueOf(true))
					.setValue(POWERED, !Boolean.valueOf(state.getValue(POWERED))), 2);
			level.scheduleTick(pos, this, IMMUNITY_TICKS);
		}

		this.updateNeighborsInFront(level, pos, state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(FACING) == direction && !state.getValue(TRIGGERED)) {
			this.startSignal(level, currentPos);
		}

		return super.updateShape(state, direction, facingState, level, currentPos, facingPos);
	}

	private void startSignal(LevelAccessor level, BlockPos pos) {
		if (!level.isClientSide() && !level.getBlockTicks().hasScheduledTick(pos, this)) {
			level.scheduleTick(pos, this, 2);
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!state.is(oldState.getBlock())) {
			if (!level.isClientSide() && state.getValue(TRIGGERED) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
				BlockState blockstate = state.setValue(TRIGGERED, Boolean.valueOf(false));
				level.setBlock(pos, blockstate, 18);
				this.updateNeighborsInFront(level, pos, blockstate);
			}
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!state.is(oldState.getBlock())) {
			if (!level.isClientSide && state.getValue(TRIGGERED) && level.getBlockTicks().hasScheduledTick(pos, this)) {
				this.updateNeighborsInFront(level, pos, state.setValue(TRIGGERED, Boolean.valueOf(false)));
			}
		}
	}

	public static ToIntFunction<BlockState> getLightValue() {
		return (state) -> state.getValue(POWERED) ? 10 : 0;
	}
	
}
