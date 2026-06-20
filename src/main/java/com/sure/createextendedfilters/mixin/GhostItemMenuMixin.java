package com.sure.createextendedfilters.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.logistics.filter.FilterMenu;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import com.sure.createextendedfilters.filter.FilterMenuExtension;

@Mixin(value = GhostItemMenu.class, remap = false)
public class GhostItemMenuMixin {
	@Inject(method = "clearContents", at = @At("TAIL"))
	private void createExtendedFilters$clearExclusions(CallbackInfo ci) {
		if ((Object) this instanceof FilterMenu menu)
			((FilterMenuExtension) menu).createExtendedFilters$getExcludedPaths().clear();
	}
}
