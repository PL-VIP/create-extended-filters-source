package com.sure.createextendedfilters.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public interface ScreenPosAccessor {
	@Accessor("leftPos")
	int createExtendedFilters$getLeftPos();

	@Accessor("topPos")
	int createExtendedFilters$getTopPos();
}
