package com.stebars.moreobserversmod.utils;

import net.minecraft.block.BlockState;
import net.minecraft.state.IntegerProperty;

public class IntegerPropertyDetails {
	public IntegerProperty property;
	public int max;
	public int min = 0;

	public IntegerPropertyDetails(IntegerProperty property, int max, int min) {
		this.property = property;
		this.max = max;
		this.min = min;
	}

	public IntegerPropertyDetails(IntegerProperty property, int max) {
		this.property = property;
		this.max = max;
	}

	public int toBracket(BlockState state) {
		// Returns -1 if property not present, 0 if at min, 11 if at max, 1-10 if intermediate
		if (!state.hasProperty(property))
			return -1;

		int val = state.getValue(property);
		if (val == min) return 0;
		if (val == max) return 11;
		return 1 + ((val - min - 1) * 10) / (max - min - 1);
	}
}
