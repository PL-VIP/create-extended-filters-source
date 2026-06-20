package com.sure.createextendedfilters.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public interface ScreenSizeAccessor {
	@Accessor("imageHeight")
	int createExtendedFilters$getImageHeight();

	@Accessor("imageHeight")
	void createExtendedFilters$setImageHeight(int height);
}
