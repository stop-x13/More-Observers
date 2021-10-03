package com.stebars.moreobserversmod.blocks;

import java.util.Random;

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
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;


public class SurveyorBlock extends ObserverBlock {

	public static final int FREQUENCY_TICKS = 20;
	// How frequently to re-check

	public static final int FORWARD_RANGE = 15;
	// How far forward the detection range extends

	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	
	public static final int REDSTONE_MAX = 15;

	
	public SurveyorBlock() {
		super(Block.Properties.copy(Blocks.OBSERVER));
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> p_206840_1_) {
		p_206840_1_.add(FACING, POWERED, POWER);
	}


	public SurveyorBlock(AbstractBlock.Properties p_i48358_1_) {
		super(p_i48358_1_);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(POWERED, Boolean.valueOf(false))
				.setValue(POWER, Integer.valueOf(0)));
	}

	@SuppressWarnings("deprecation") // .isAir() is deprecated, says to use .isAir(world, pos) which is also deprecated?? 
	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int distanceToFirstBlock = -1;
		boolean seen = false;
		Direction facing = state.getValue(FACING);
		for (int distance = 1; distance <= FORWARD_RANGE; distance++) {
			if (!world.getBlockState(pos.relative(facing, distance)).isAir()) {
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
			world.setBlock(pos, updatedState, 2);
			this.updateNeighborsInFront(world, pos, state);	
		}
		world.getBlockTicks().scheduleTick(pos, this, FREQUENCY_TICKS); // Schedule next check
	}

	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		if (!state.is(p_220082_4_.getBlock())) {
			if (!world.isClientSide() && state.getValue(POWERED) && !world.getBlockTicks().hasScheduledTick(pos, this)) {
				BlockState blockstate = state.setValue(POWERED, Boolean.valueOf(false)).setValue(POWER, Integer.valueOf(0));
				world.setBlock(pos, blockstate, 18);
				this.updateNeighborsInFront(world, pos, blockstate);
			}
		}
		
		// Schedule update tick to get initial value
		world.getBlockTicks().scheduleTick(pos, this, 2);
	}


	@Override
	public int getSignal(BlockState state, IBlockReader reader, BlockPos pos, Direction direction) {
		return state.getValue(FACING) == direction ? state.getValue(POWER) : 0;
	}
}
