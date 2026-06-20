package com.sure.createextendedfilters.mixin;

import java.util.LinkedHashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.logistics.filter.FilterMenu;
import com.sure.createextendedfilters.filter.ComponentExclusionHelper;
import com.sure.createextendedfilters.filter.FilterMenuExtension;

import net.minecraft.world.item.ItemStack;

@Mixin(value = FilterMenu.class, remap = false)
public abstract class FilterMenuMixin implements FilterMenuExtension {
	@Unique
	private Set<String> createExtendedFilters$excludedPaths;

	@Unique
	private Set<String> createExtendedFilters$excludedSet() {
		if (createExtendedFilters$excludedPaths == null)
			createExtendedFilters$excludedPaths = new LinkedHashSet<>();
		return createExtendedFilters$excludedPaths;
	}

	@Override
	public Set<String> createExtendedFilters$getExcludedPaths() {
		return createExtendedFilters$excludedSet();
	}

	@Override
	public void createExtendedFilters$toggleExcludedPath(String exclusionPath) {
		Set<String> excluded = createExtendedFilters$excludedSet();
		if (!excluded.add(exclusionPath))
			excluded.remove(exclusionPath);
	}

	@Inject(method = "initAndReadInventory", at = @At("TAIL"))
	private void createExtendedFilters$readExcluded(ItemStack filterItem, CallbackInfo ci) {
		Set<String> excluded = createExtendedFilters$excludedSet();
		excluded.clear();
		excluded.addAll(ComponentExclusionHelper.readExcluded(filterItem));
	}

	@Inject(method = "saveData", at = @At(value = "RETURN"))
	private void createExtendedFilters$writeExcluded(ItemStack filterItem, CallbackInfo ci) {
		ComponentExclusionHelper.writeExcluded(filterItem, createExtendedFilters$excludedSet());
	}
}
