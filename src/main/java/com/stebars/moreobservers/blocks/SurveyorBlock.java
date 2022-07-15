package com.stebars.moreobservers.blocks;

import java.util.Random;
import java.util.function.ToIntFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;




public class SurveyorBlock extends ObserverBlock {

	public static final int FREQUENCY_TICKS = 20;
	// How frequently to re-check

	public static final int FORWARD_RANGE = 15;
	// How far forward the detection range extends

	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	
	public static final int REDSTONE_MAX = 15;

	public SurveyorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(POWERED, Boolean.valueOf(false))
				.setValue(POWER, Integer.valueOf(0)));
	}
	

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED, POWER);
	}


	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
		int distanceToFirstBlock = -1;
		boolean seen = false;
		Direction facing = state.getValue(FACING);
		for (int distance = 1; distance <= FORWARD_RANGE; distance++) {
			if (!level.getBlockState(pos.relative(facing, distance)).isAir()) {
				seen = true;
				distanceToFirstBlock = distance;
				break;
			}
		}
		
		int powerOld = state.getValue(POWER);
		int powerNew = seen ? REDSTONE_MAX - distanceToFirstBlock + 1 : 0;

		if (powerOld != powerNew) {
			BlockState updatedState = state
					.setValue(POWER, Integer.valueOf(powerNew))
					.setValue(POWERED, Boolean.valueOf(powerNew > 0));
			level.setBlock(pos, updatedState, 2);
			this.updateNeighborsInFront(level, pos, state);	
		}
		level.scheduleTick(pos, this, FREQUENCY_TICKS); // Schedule next check
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMovinig) {
		if (!state.is(oldState.getBlock())) {
			if (!level.isClientSide() && state.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
				BlockState blockstate = state.setValue(POWERED, Boolean.valueOf(false)).setValue(POWER, Integer.valueOf(0));
				level.setBlock(pos, blockstate, 18);
				this.updateNeighborsInFront(level, pos, blockstate);
			}
		}
		
		// Schedule update tick to get initial value
		level.scheduleTick(pos, this, 2);
	}


	@Override
	public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction direction) {
		return state.getValue(FACING) == direction ? state.getValue(POWER) : 0;
	}

	public static ToIntFunction<BlockState> getLightValue() {
		return (state) -> state.getValue(POWERED) ? 10 : 0;
	}
}
