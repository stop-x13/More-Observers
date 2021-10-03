package com.stebars.moreobserversmod.blocks;

import java.util.List;
import java.util.Random;

import com.mojang.datafixers.util.Pair;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;


public class MobserverBlock extends ObserverBlock {

	public static final int FREQUENCY_TICKS = 20;
	// How frequently to re-check for mobs

	public static final int FORWARD_RANGE = 5;
	// How far forward the detection range extends

	public static final int SIDE_RANGE = 2;
	// How far out the detection zone extends in each direction from forward direction
	// Actual bounding box has side length SIDE_RANGE * 2 + 1


	public MobserverBlock() {
		super(Block.Properties.copy(Blocks.OBSERVER));
	}

	public MobserverBlock(AbstractBlock.Properties p_i48358_1_) {
		super(p_i48358_1_);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(POWERED, Boolean.valueOf(false)));
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		List<Entity> mobs = world.getEntitiesOfClass(LivingEntity.class, getZone(state, pos));

		boolean poweredOld = state.getValue(POWERED);
		boolean poweredNew = !mobs.isEmpty();

		if (poweredOld != poweredNew) {
			BlockState updatedState = state.setValue(POWERED, Boolean.valueOf(poweredNew));
			Blocks.REDSTONE_WIRE.asItem();
			world.setBlock(pos, updatedState, 2);
			this.updateNeighborsInFront(world, pos, state);	
		}
		world.getBlockTicks().scheduleTick(pos, this, FREQUENCY_TICKS); // Schedule next check
	}

	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		if (!state.is(p_220082_4_.getBlock())) {
			if (!world.isClientSide() && state.getValue(POWERED) && !world.getBlockTicks().hasScheduledTick(pos, this)) {
				BlockState blockstate = state.setValue(POWERED, Boolean.valueOf(false));
				world.setBlock(pos, blockstate, 18);
				this.updateNeighborsInFront(world, pos, blockstate);
			}
		}

		// Schedule update tick to get initial value
		world.getBlockTicks().scheduleTick(pos, this, 2);
	}

	public AxisAlignedBB getZone(BlockState state, BlockPos pos) {
		// We recompute the zone on every update. Could do it faster by caching on block placement, but would
		// need to make tile entity (or they'd all share the same zone), I think
		Direction facing = state.getValue(FACING);
		BlockPos farPos = pos.relative(facing, FORWARD_RANGE);
		Pair<Direction, Direction> otherDirs = getPerpendicularDirections(facing);
		BlockPos bound1 = farPos
				.relative(otherDirs.getFirst(), SIDE_RANGE)
				.relative(otherDirs.getSecond(), SIDE_RANGE);
		BlockPos bound2 = pos //.relative(facing, 1) -- make it a half-block too big, not half-block gap from front
				.relative(otherDirs.getFirst(), -SIDE_RANGE)
				.relative(otherDirs.getSecond(), -SIDE_RANGE);

		return new AxisAlignedBB(
				Vector3d.atCenterOf(bound1),
				Vector3d.atCenterOf(bound2)
				);
	}

	public Pair<Direction, Direction> getPerpendicularDirections(Direction dir) {
		if (dir == Direction.DOWN || dir == Direction.UP)
			return new Pair<Direction, Direction>(Direction.EAST, Direction.NORTH);
		else if (dir == Direction.WEST || dir == Direction.EAST)
			return new Pair<Direction, Direction>(Direction.NORTH, Direction.UP);
		else //if (dir == Direction.NORTH || dir == Direction.SOUTH)
			return new Pair<Direction, Direction>(Direction.UP, Direction.WEST);
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return state.getValue(POWERED) ? 10 : super.getLightValue(state, world, pos);
	}

	@Override
	public int getSignal(BlockState p_180656_1_, IBlockReader p_180656_2_, BlockPos p_180656_3_, Direction dir) {
		// Send both forward and backward
		Direction facing = p_180656_1_.getValue(FACING);
		return p_180656_1_.getValue(POWERED) && (facing == dir || facing.getOpposite() == dir) ? 15 : 0;
	}
}
