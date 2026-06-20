package com.sure.createextendedfilters.filter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.sure.createextendedfilters.registry.ModDataComponents;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import net.neoforged.neoforge.items.ItemStackHandler;

public final class ComponentExclusionHelper {
	public static final int FOOTER_HEIGHT = 14;
	public static final char PATH_SEPARATOR = '|';

	private ComponentExclusionHelper() {}

	public record ComponentEntry(String exclusionPath, Component displayName, boolean expandable) {}

	public static List<ComponentEntry> collectRootEntries(ItemStackHandler inventory) {
		Set<String> componentPaths = new LinkedHashSet<>();
		Set<String> withFields = new LinkedHashSet<>();

		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;
			for (TypedDataComponent<?> component : stack.getComponents()) {
				ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type());
				if (id == null)
					continue;
				String componentPath = id.toString();
				componentPaths.add(componentPath);
				if (component.value() instanceof CustomData customData && !customData.isEmpty())
					withFields.add(componentPath);
			}
		}

		List<ComponentEntry> entries = new ArrayList<>();
		for (String componentPath : componentPaths)
			entries.add(new ComponentEntry(componentPath, displayNameFor(componentPath, ""), withFields.contains(componentPath)));

		entries.sort(Comparator.comparing(ComponentEntry::exclusionPath));
		return entries;
	}

	public static List<ComponentEntry> collectFieldEntries(ItemStackHandler inventory, String componentPath) {
		Set<String> seen = new LinkedHashSet<>();
		List<ComponentEntry> entries = new ArrayList<>();

		MutableComponent entireLabel = displayNameFor(componentPath, "").copy()
			.append(" (")
			.append(Component.translatable("createextendedfilters.gui.filter.entire_component"))
			.append(")");
		entries.add(new ComponentEntry(componentPath, entireLabel, false));

		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;
			for (TypedDataComponent<?> component : stack.getComponents()) {
				ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type());
				if (id == null || !componentPath.equals(id.toString()))
					continue;
				if (component.value() instanceof CustomData customData && !customData.isEmpty())
					collectTagFields(componentPath, customData.copyTag(), "", seen, entries);
			}
		}

		entries.subList(1, entries.size()).sort(Comparator.comparing(ComponentEntry::exclusionPath));
		return entries;
	}

	private static void collectTagFields(String componentPath, CompoundTag tag, String prefix, Set<String> seen, List<ComponentEntry> entries) {
		for (String key : tag.getAllKeys()) {
			String fieldPath = prefix.isEmpty() ? key : prefix + "/" + key;
			String exclusionPath = componentPath + PATH_SEPARATOR + fieldPath;
			if (seen.add(exclusionPath))
				entries.add(new ComponentEntry(exclusionPath, displayNameFor(componentPath, fieldPath), false));

			Tag child = tag.get(key);
			if (child instanceof CompoundTag compound)
				collectTagFields(componentPath, compound, fieldPath, seen, entries);
		}
	}

	public static Component displayNameFor(String componentPath, String fieldPath) {
		ResourceLocation id = ResourceLocation.tryParse(componentPath);
		Component componentName = id != null ? displayNameForComponent(id) : Component.literal(componentPath);
		if (fieldPath.isEmpty())
			return componentName;

		int depth = fieldPath.split("/").length;
		StringBuilder indent = new StringBuilder();
		for (int i = 1; i < depth; i++)
			indent.append("  ");

		String leaf = fieldPath.contains("/") ? fieldPath.substring(fieldPath.lastIndexOf('/') + 1) : fieldPath;
		return Component.literal(indent.toString()).append(formatFieldName(leaf));
	}

	public static Component displayNameForComponent(ResourceLocation id) {
		String key = "component." + id.getNamespace() + "." + id.getPath().replace('/', '.');
		Component translated = Component.translatable(key);
		if (!translated.getString().equals(key))
			return translated;
		return Component.literal(formatPath(id.getPath()));
	}

	private static String formatPath(String path) {
		String[] parts = path.split("[/_]");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty())
				continue;
			if (!builder.isEmpty())
				builder.append(' ');
			builder.append(Character.toUpperCase(part.charAt(0)));
			if (part.length() > 1)
				builder.append(part.substring(1));
		}
		return builder.toString();
	}

	private static String formatFieldName(String field) {
		if (field.isEmpty())
			return field;
		return Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}

	public static boolean matchesWithExclusions(ItemStack reference, ItemStack stack, Set<String> excluded) {
		if (!ItemStack.isSameItem(reference, stack))
			return false;
		if (excluded.isEmpty())
			return ItemStack.isSameItemSameComponents(reference, stack);

		Set<DataComponentType<?>> types = new LinkedHashSet<>();
		reference.getComponents().forEach(component -> types.add(component.type()));
		stack.getComponents().forEach(component -> types.add(component.type()));

		for (DataComponentType<?> type : types) {
			ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
			if (id == null)
				continue;

			String componentPath = id.toString();
			if (excluded.contains(componentPath))
				continue;

			Set<String> nested = nestedExclusionsFor(excluded, componentPath);
			Object referenceValue = reference.get(type);
			Object stackValue = stack.get(type);

			if (nested.isEmpty()) {
				if (!Objects.equals(referenceValue, stackValue))
					return false;
			} else if (!compareWithNestedExclusions(referenceValue, stackValue, nested)) {
				return false;
			}
		}
		return true;
	}

	private static Set<String> nestedExclusionsFor(Set<String> excluded, String componentPath) {
		String prefix = componentPath + PATH_SEPARATOR;
		Set<String> nested = new LinkedHashSet<>();
		for (String path : excluded) {
			if (path.startsWith(prefix))
				nested.add(path.substring(prefix.length()));
		}
		return nested;
	}

	private static boolean compareWithNestedExclusions(Object referenceValue, Object stackValue, Set<String> nestedFieldPaths) {
		if (!(referenceValue instanceof CustomData referenceData) || !(stackValue instanceof CustomData stackData))
			return Objects.equals(referenceValue, stackValue);

		CompoundTag referenceTag = referenceData.copyTag();
		CompoundTag stackTag = stackData.copyTag();
		for (String fieldPath : nestedFieldPaths) {
			removeTagPath(referenceTag, fieldPath);
			removeTagPath(stackTag, fieldPath);
		}
		return referenceTag.equals(stackTag);
	}

	private static void removeTagPath(CompoundTag tag, String path) {
		int slash = path.indexOf('/');
		if (slash < 0) {
			tag.remove(path);
			return;
		}
		String head = path.substring(0, slash);
		String tail = path.substring(slash + 1);
		Tag child = tag.get(head);
		if (child instanceof CompoundTag compound) {
			removeTagPath(compound, tail);
			if (compound.isEmpty())
				tag.remove(head);
		}
	}

	public static Set<String> readExcluded(ItemStack filterItem) {
		return ModDataComponents.getExcluded(filterItem);
	}

	public static void writeExcluded(ItemStack filterItem, Set<String> excluded) {
		ModDataComponents.setExcluded(filterItem, excluded);
	}
}
