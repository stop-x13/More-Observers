package com.stebars.moreobservers.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.ToIntFunction;

import com.stebars.moreobservers.utils.IntegerPropertyDetails;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;


public class DiscernerBlock extends ObserverBlock {

	public static final IntegerProperty POWER = BlockStateProperties.POWER;

	public DiscernerBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(POWERED, Boolean.valueOf(false)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED, POWER);
	}

	// List of properties it'll check, in order, stopping at first one that's present

	public static ArrayList<IntegerPropertyDetails> propertyList = new ArrayList<>(Arrays.asList(
		new IntegerPropertyDetails(BlockStateProperties.AGE_1, 1), 
		new IntegerPropertyDetails(BlockStateProperties.AGE_2, 2), 
		new IntegerPropertyDetails(BlockStateProperties.AGE_3, 3), 
		new IntegerPropertyDetails(BlockStateProperties.AGE_5, 5), 
		new IntegerPropertyDetails(BlockStateProperties.AGE_7, 7), 
		new IntegerPropertyDetails(BlockStateProperties.AGE_15, 15), 
		new IntegerPropertyDetails(BlockStateProperties.AGE_25, 25), 
		// redstone power
		new IntegerPropertyDetails(BlockStateProperties.POWER, 15), 
		new IntegerPropertyDetails(BlockStateProperties.LEVEL_HONEY, 5), 
		// bamboo and sapling stage
		new IntegerPropertyDetails(BlockStateProperties.STAGE, 1), 

		new IntegerPropertyDetails(BlockStateProperties.LEVEL_CAULDRON, 3), 
		new IntegerPropertyDetails(BlockStateProperties.LEVEL_COMPOSTER, 8), 
		// level of flowing fluid; actual max is 15, but only goes up to 7 for flowing blocks
		new IntegerPropertyDetails(BlockStateProperties.LEVEL, 8), 
		new IntegerPropertyDetails(BlockStateProperties.MOISTURE, 7), 
		// cake bites
		new IntegerPropertyDetails(BlockStateProperties.BITES, 6), 
		new IntegerPropertyDetails(BlockStateProperties.DELAY, 1, 4), 
		new IntegerPropertyDetails(BlockStateProperties.HATCH, 2), 
		// turtle eggs
		new IntegerPropertyDetails(BlockStateProperties.EGGS, 1, 4), 
		// sea pickles
		new IntegerPropertyDetails(BlockStateProperties.PICKLES, 1, 4)
	));


	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {		
		BlockState observedState = level.getBlockState(pos.relative(state.getValue(FACING)));

		int seen0to11 = -1;
		for (IntegerPropertyDetails details : propertyList) {
			int propertyVal = details.toBracket(observedState);
			if (propertyVal != -1) {
				seen0to11 = propertyVal;
				break;
			}
		}
		// Some boolean properties:
		// Piston blocks
		if (seen0to11 == -1 && state.hasProperty(BlockStateProperties.EXTENDED))
			seen0to11 = state.getValue(BlockStateProperties.EXTENDED) ? 11 : 0;
		// Whether objects are powered (anything, eg doors, bells, note blocks)
		// Do not enable this - for some reason it thinks everything is powered, even empty air
		//else if (seen0to11 == -1 && state.hasProperty(BlockStateProperties.POWERED))
		//	seen0to11 = state.getValue(BlockStateProperties.POWERED) ? 11 : 0;

		// Given seen value, we want to output 0 if no property, 5-10 for intermediate values, 15 if at max
		int outputSignal;
		if (seen0to11 == -1) // no properties detected
			outputSignal = 0;
		else if (seen0to11 == 11) // property is at max
			outputSignal = 15;
		else
			outputSignal = 5 + (seen0to11 / 2); // intermediate values: output in range 5-10

		BlockState updatedState = state
				.setValue(POWER, Integer.valueOf(outputSignal))
				.setValue(POWERED, Boolean.valueOf(outputSignal > 0));
		level.setBlock(pos, updatedState, 2);

		this.updateNeighborsInFront(level, pos, state);
	}


	

	@Override
	public int getDirectSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
		return state.getSignal(getter, pos, direction);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currPos, BlockPos facingPos) {
		if (state.getValue(FACING) == direction ) {
			this.startSignal(level, currPos);
		}
		return super.updateShape(state, direction, facingState, level, currPos, facingPos);
	}
	private void startSignal(LevelAccessor level, BlockPos pos) {
		if (!level.isClientSide() && !level.getBlockTicks().hasScheduledTick(pos, this)) {
			level.scheduleTick(pos, this, 2);
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!state.is(oldState.getBlock())) {
			if (!level.isClientSide() && state.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
				BlockState blockstate = state.setValue(POWERED, Boolean.valueOf(false)).setValue(POWER, Integer.valueOf(0));
				level.setBlock(pos, blockstate, 18);
				this.updateNeighborsInFront(level, pos, blockstate);
			}
		}
		// schedule update tick to get initial value
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

