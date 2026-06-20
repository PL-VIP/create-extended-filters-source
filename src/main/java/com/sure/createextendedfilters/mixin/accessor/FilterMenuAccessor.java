package com.sure.createextendedfilters.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.simibubi.create.content.logistics.filter.FilterMenu;

@Mixin(value = FilterMenu.class, remap = false)
public interface FilterMenuAccessor {
	@Accessor("respectNBT")
	boolean getRespectNBT();
}
