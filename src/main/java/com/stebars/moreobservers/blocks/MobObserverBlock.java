package com.stebars.moreobservers.blocks;

import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;




public class MobObserverBlock extends ObserverBlock {
	
	// How frequently to re-check for mobs
	public static final int FREQUENCY_TICKS = 2;
	
	// How far forward the detection range extends
	public static final int FORWARD_RANGE = 5;

	// How far out the detection zone extends in each direction from forward direction
	// Actual bounding box has side length SIDE_RANGE * 2 + 1
	public static final int SIDE_RANGE = 2;

	public MobObserverBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.SOUTH)
			.setValue(POWERED, Boolean.valueOf(false)));
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
		AABB boundingBox = getZone(state, pos);
		List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class, boundingBox);

		boolean poweredOld = state.getValue(POWERED);
		boolean poweredNew = !mobs.isEmpty();

		if (poweredOld != poweredNew) {
			BlockState updatedState = state.setValue(POWERED, Boolean.valueOf(poweredNew));
			Blocks.REDSTONE_WIRE.asItem();
			level.setBlock(pos, updatedState, 2);
			this.updateNeighborsInFront(level, pos, state);
			
		}
		
		
		level.scheduleTick(pos, this, FREQUENCY_TICKS); // Schedule next check
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!state.is(oldState.getBlock())) {
			if (!level.isClientSide() && state.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
				BlockState blockstate = state.setValue(POWERED, Boolean.valueOf(false));
				level.setBlock(pos, blockstate, 18);
				this.updateNeighborsInFront(level, pos, blockstate);
			}
		}

		// Schedule update tick to get initial value
		level.scheduleTick(pos, this, 2);
	}

	public AABB getZone(BlockState state, BlockPos pos) {
		// We recompute the zone on every update. Could do it faster by caching on block placement, but would
		// need to make tile entity (or they'd all share the same zone), I think
		Direction facing = state.getValue(FACING);
		Pair<Direction, Direction> normalDirs = getPerpendicularDirections(facing);
		
		// Calculate the grid bounding box
		if(isPosDir(facing)) {
			BlockPos farPos = pos.relative(facing, FORWARD_RANGE + 1);
			BlockPos nearPos = pos.relative(facing, 1);
			BlockPos bound1 = farPos.relative(normalDirs.getFirst(), SIDE_RANGE + 1).relative(normalDirs.getSecond(), SIDE_RANGE + 1);
			BlockPos bound2 = nearPos.relative(normalDirs.getFirst(), -SIDE_RANGE).relative(normalDirs.getSecond(), -SIDE_RANGE);
			return new AABB(bound1, bound2);
		}
		else {
			BlockPos farPos = pos.relative(facing, FORWARD_RANGE);
			BlockPos nearPos = pos;
			BlockPos bound1 = farPos.relative(normalDirs.getFirst(), SIDE_RANGE + 1).relative(normalDirs.getSecond(), SIDE_RANGE + 1);
			BlockPos bound2 = nearPos.relative(normalDirs.getFirst(), -SIDE_RANGE).relative(normalDirs.getSecond(), -SIDE_RANGE);
			return new AABB(bound1, bound2);
		}
	}
	
	// helper
	private static boolean isPosDir(Direction dir) {
		return dir == Direction.EAST || dir == Direction.SOUTH || dir == Direction.UP;
	}
	
	
	// First is positive and second is negative
	private static Pair<Direction, Direction> getPerpendicularDirections(Direction dir) {
		if (dir == Direction.DOWN || dir == Direction.UP)
			return new Pair<Direction, Direction>(Direction.EAST, Direction.SOUTH);
		else if (dir == Direction.WEST || dir == Direction.EAST)
			return new Pair<Direction, Direction>(Direction.UP, Direction.SOUTH);
		else // NORTH or SOUTH
			return new Pair<Direction, Direction>(Direction.UP, Direction.EAST);
	}

	@Override
	public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction direction) {
		// Send both forward and backward
		Direction facing = state.getValue(FACING);
		return state.getValue(POWERED) && facing == direction ? 15 : 0;
	}

	public static ToIntFunction<BlockState> getLightValue() {
		return (state) -> state.getValue(POWERED) ? 10 : 0;
	}
}
