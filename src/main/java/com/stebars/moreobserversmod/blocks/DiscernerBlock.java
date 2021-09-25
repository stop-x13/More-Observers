package com.stebars.moreobserversmod.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.stebars.moreobserversmod.utils.IntegerPropertyDetails;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;


public class DiscernerBlock extends ObserverBlock {

	public static final IntegerProperty POWER = BlockStateProperties.POWER;

	public DiscernerBlock() {
		super(Block.Properties.copy(Blocks.OBSERVER));
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> p_206840_1_) {
		p_206840_1_.add(FACING, POWERED, POWER);
	}

	// List of properties it'll check, in order, stopping at first one that's present
	public static List<IntegerPropertyDetails> propertyList = new ArrayList<IntegerPropertyDetails>(Arrays.asList(
			new IntegerPropertyDetails(BlockStateProperties.AGE_1, 1),
			new IntegerPropertyDetails(BlockStateProperties.AGE_2, 2),
			new IntegerPropertyDetails(BlockStateProperties.AGE_3, 3),
			new IntegerPropertyDetails(BlockStateProperties.AGE_5, 5),
			new IntegerPropertyDetails(BlockStateProperties.AGE_7, 7),
			new IntegerPropertyDetails(BlockStateProperties.AGE_15, 15),
			new IntegerPropertyDetails(BlockStateProperties.AGE_25, 25),
			new IntegerPropertyDetails(BlockStateProperties.POWER, 15), // redstone power
			new IntegerPropertyDetails(BlockStateProperties.LEVEL_HONEY, 5),
			new IntegerPropertyDetails(BlockStateProperties.STAGE, 1), // bamboo and sapling stage
			new IntegerPropertyDetails(BlockStateProperties.LEVEL_CAULDRON, 3),
			new IntegerPropertyDetails(BlockStateProperties.LEVEL_COMPOSTER, 8),
			new IntegerPropertyDetails(BlockStateProperties.LEVEL, 15), // level of flowing fluid
			//new IntegerPropertyDetails(BlockStateProperties.LEVEL_FLOWING, 8, 1),
			new IntegerPropertyDetails(BlockStateProperties.MOISTURE, 7),
			new IntegerPropertyDetails(BlockStateProperties.BITES, 6), // cake bites
			new IntegerPropertyDetails(BlockStateProperties.DELAY, 4, 1), // repeater delay
			new IntegerPropertyDetails(BlockStateProperties.HATCH, 2),
			new IntegerPropertyDetails(BlockStateProperties.EGGS, 4, 1), // turtle eggs
			new IntegerPropertyDetails(BlockStateProperties.PICKLES, 4, 1) // sea pickles
			));

	public DiscernerBlock(AbstractBlock.Properties p_i48358_1_) {
		super(p_i48358_1_);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(POWERED, Boolean.valueOf(false))
				.setValue(POWER, Integer.valueOf(0)));
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {		
		BlockState observedState = world.getBlockState(pos.relative(state.getValue(FACING)));

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
		world.setBlock(pos, updatedState, 2);

		this.updateNeighborsInFront(world, pos, state);
	}


	@Override
	public int getSignal(BlockState state, IBlockReader reader, BlockPos pos, Direction direction) {
		return state.getValue(FACING) == direction ? state.getValue(POWER) : 0;
	}

	@Override
	public int getDirectSignal(BlockState p_176211_1_, IBlockReader p_176211_2_, BlockPos p_176211_3_, Direction p_176211_4_) {
		return p_176211_1_.getSignal(p_176211_2_, p_176211_3_, p_176211_4_);
	}

	@Override
	public BlockState updateShape(BlockState p_196271_1_, Direction p_196271_2_, BlockState p_196271_3_, IWorld p_196271_4_, BlockPos p_196271_5_, BlockPos p_196271_6_) {
		if (p_196271_1_.getValue(FACING) == p_196271_2_ ) {
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
			if (!p_220082_2_.isClientSide() && p_220082_1_.getValue(POWERED) && !p_220082_2_.getBlockTicks().hasScheduledTick(p_220082_3_, this)) {
				BlockState blockstate = p_220082_1_.setValue(POWERED, Boolean.valueOf(false)).setValue(POWER, Integer.valueOf(0));
				p_220082_2_.setBlock(p_220082_3_, blockstate, 18);
				this.updateNeighborsInFront(p_220082_2_, p_220082_3_, blockstate);
			}
		}
		// schedule update tick to get initial value
		p_220082_2_.getBlockTicks().scheduleTick(p_220082_3_, this, 2);
	}

}

