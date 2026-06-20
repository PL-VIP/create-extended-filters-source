package com.sure.createextendedfilters.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.inventory.Slot;

@Mixin(Slot.class)
public interface SlotYAccessor {
	@Accessor("y")
	int createExtendedFilters$getY();

	@Accessor("y")
	@Mutable
	void createExtendedFilters$setY(int y);
}
