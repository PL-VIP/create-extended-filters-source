package com.sure.createextendedfilters.registry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.sure.createextendedfilters.CreateExtendedFilters;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
	public static final DeferredRegister<DataComponentType<?>> REGISTER =
		DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CreateExtendedFilters.MOD_ID);

	private static final Codec<Set<String>> EXCLUDED_CODEC = Codec.STRING.listOf()
		.xmap(list -> (Set<String>) new LinkedHashSet<>(list), set -> new ArrayList<>(set));

	public static final Supplier<DataComponentType<Set<String>>> FILTER_EXCLUDED_COMPONENTS = REGISTER.register(
		"filter_excluded_components",
		() -> DataComponentType.<Set<String>>builder()
			.persistent(EXCLUDED_CODEC)
			.networkSynchronized(ByteBufCodecs.collection(LinkedHashSet::new, ByteBufCodecs.STRING_UTF8))
			.build()
	);

	public static void register(IEventBus modBus) {
		REGISTER.register(modBus);
	}

	public static Set<String> getExcluded(ItemStack stack) {
		return stack.getOrDefault(FILTER_EXCLUDED_COMPONENTS.get(), Set.of());
	}

	public static void setExcluded(ItemStack stack, Set<String> excluded) {
		if (excluded.isEmpty())
			stack.remove(FILTER_EXCLUDED_COMPONENTS.get());
		else
			stack.set(FILTER_EXCLUDED_COMPONENTS.get(), new LinkedHashSet<>(excluded));
	}
}
