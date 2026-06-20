package com.sure.createextendedfilters.mixin;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sure.createextendedfilters.filter.ComponentExclusionHelper;
import com.simibubi.create.content.logistics.filter.FilterItemStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(value = FilterItemStack.ListFilterItemStack.class, remap = false)
public abstract class ListFilterItemStackMixin {
	@Shadow
	@Final
	public List<FilterItemStack> containedItems;

	@Shadow
	public boolean shouldRespectNBT;

	@Shadow
	public boolean isBlacklist;

	@Inject(
		method = "test(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Z)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void createExtendedFilters$testWithExclusions(Level world, ItemStack stack, boolean matchNBT, CallbackInfoReturnable<Boolean> cir) {
		// ListFilterItemStack ignores the matchNBT argument and always uses shouldRespectNBT
		// for contained items; Create's funnels/deployers call test(level, stack) with matchNBT=false.
		if (!shouldRespectNBT)
			return;

		ItemStack filterItem = ((FilterItemStack) (Object) this).item();
		Set<String> excluded = ComponentExclusionHelper.readExcluded(filterItem);
		if (excluded.isEmpty())
			return;

		for (FilterItemStack contained : containedItems) {
			ItemStack reference = contained.item();
			if (ComponentExclusionHelper.matchesWithExclusions(reference, stack, excluded)) {
				cir.setReturnValue(!isBlacklist);
				return;
			}
		}
		cir.setReturnValue(isBlacklist);
	}
}
