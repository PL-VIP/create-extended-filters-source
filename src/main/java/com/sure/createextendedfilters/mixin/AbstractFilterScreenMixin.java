package com.sure.createextendedfilters.mixin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.logistics.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.filter.FilterMenu;
import com.simibubi.create.content.logistics.filter.FilterScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import com.sure.createextendedfilters.client.gui.ExcludeComponentsScrollInput;
import com.sure.createextendedfilters.filter.ComponentExclusionHelper;
import com.sure.createextendedfilters.filter.ComponentExclusionHelper.ComponentEntry;
import com.sure.createextendedfilters.filter.FilterMenuExtension;
import com.sure.createextendedfilters.mixin.accessor.ContainerScreenAccessor;
import com.sure.createextendedfilters.mixin.accessor.FilterMenuAccessor;
import com.sure.createextendedfilters.mixin.accessor.ScreenPosAccessor;
import com.sure.createextendedfilters.mixin.accessor.ScreenSizeAccessor;
import com.sure.createextendedfilters.mixin.accessor.ScreenWidgetInvoker;
import com.sure.createextendedfilters.mixin.accessor.SlotYAccessor;
import com.sure.createextendedfilters.network.ModNetwork;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

@Mixin(value = AbstractFilterScreen.class, remap = false)
public abstract class AbstractFilterScreenMixin {
	private static final int FILTER_SLOT_START = 36;

	@Unique
	private ExcludeComponentsScrollInput createExtendedFilters$excludeScroll;

	@Unique
	private List<ComponentEntry> createExtendedFilters$componentEntries = List.of();

	@Unique
	private Set<String> createExtendedFilters$lastExcluded = Set.of();

	@Unique
	private String createExtendedFilters$openComponentPath;

	@Unique
	private boolean createExtendedFilters$lastRespectData;

	@Unique
	private int createExtendedFilters$footerApplied;

	@Unique
	private static final MutableComponent createExtendedFilters$PANEL_TITLE =
		Component.translatable("createextendedfilters.gui.filter.components");

	@Redirect(
		method = "renderBg",
		at = @At(
			value = "INVOKE",
			target = "Lcom/simibubi/create/content/logistics/filter/AbstractFilterScreen;renderPlayerInventory(Lnet/minecraft/client/gui/GuiGraphics;II)V"
		)
	)
	private void createExtendedFilters$renderPlayerInventory(AbstractFilterScreen screen, GuiGraphics graphics, int x, int y) {
		if ((Object) this instanceof FilterScreen)
			y += createExtendedFilters$footerApplied;
		screen.renderPlayerInventory(graphics, x, y);
	}

	@Redirect(
		method = "renderBg",
		at = @At(
			value = "INVOKE",
			target = "Lnet/createmod/catnip/gui/element/GuiGameElement$GuiRenderBuilder;render(Lnet/minecraft/client/gui/GuiGraphics;)V"
		)
	)
	private void createExtendedFilters$skipFilterPreview(GuiGameElement.GuiRenderBuilder builder, GuiGraphics graphics) {
		if (!((Object) this instanceof FilterScreen))
			builder.render(graphics);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void createExtendedFilters$initExcludeComponents(CallbackInfo ci) {
		if (!((Object) this instanceof FilterScreen))
			return;

		int x = ((ScreenPosAccessor) this).createExtendedFilters$getLeftPos();
		int y = ((ScreenPosAccessor) this).createExtendedFilters$getTopPos();
		int footerTop = y + AllGuiTextures.FILTER.getHeight();

		createExtendedFilters$excludeScroll = new ExcludeComponentsScrollInput(
			x + 38, footerTop + 3, 140, ComponentExclusionHelper.FOOTER_HEIGHT - 4);
		createExtendedFilters$excludeScroll.forOptions(List.of(Component.empty()));
		createExtendedFilters$excludeScroll.onCenterClick(this::createExtendedFilters$onCenterClick);
		createExtendedFilters$excludeScroll.addHint(
			Component.translatable("createextendedfilters.gui.filter.center_click_hint").plainCopy());
		createExtendedFilters$excludeScroll.visible = false;

		((ScreenWidgetInvoker) this).createExtendedFilters$addRenderableWidget(createExtendedFilters$excludeScroll);

		createExtendedFilters$lastRespectData = ((FilterMenuAccessor) createExtendedFilters$menu()).getRespectNBT();
		createExtendedFilters$applyFooterLayout(createExtendedFilters$lastRespectData);
		createExtendedFilters$refreshComponents();
	}

	@Inject(method = "containerTick", at = @At("TAIL"))
	private void createExtendedFilters$tickExcludeComponents(CallbackInfo ci) {
		if (!((Object) this instanceof FilterScreen) || createExtendedFilters$excludeScroll == null)
			return;

		boolean respectData = ((FilterMenuAccessor) createExtendedFilters$menu()).getRespectNBT();
		if (respectData != createExtendedFilters$lastRespectData) {
			createExtendedFilters$lastRespectData = respectData;
			if (!respectData)
				createExtendedFilters$openComponentPath = null;
			createExtendedFilters$applyFooterLayout(respectData);
		}

		createExtendedFilters$refreshComponents();
	}

	@Inject(method = "renderBg", at = @At("TAIL"))
	private void createExtendedFilters$renderFooter(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
		if (!((Object) this instanceof FilterScreen) || createExtendedFilters$footerApplied == 0)
			return;

		int x = ((ScreenPosAccessor) this).createExtendedFilters$getLeftPos();
		int y = ((ScreenPosAccessor) this).createExtendedFilters$getTopPos() + AllGuiTextures.FILTER.getHeight();
		int width = AllGuiTextures.FILTER.getWidth();
		int height = ComponentExclusionHelper.FOOTER_HEIGHT;

		graphics.fill(x + 1, y, x + width - 1, y + height, 0xFF_C8BC98);
		graphics.fill(x + 1, y, x + width - 1, y + 1, 0xFF_5C4A3A);
		graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, 0xFF_5C4A3A);
	}

	@Unique
	private FilterMenu createExtendedFilters$menu() {
		return (FilterMenu) ((ContainerScreenAccessor) this).createExtendedFilters$getMenu();
	}

	@Unique
	private void createExtendedFilters$applyFooterLayout(boolean showFooter) {
		int target = showFooter ? ComponentExclusionHelper.FOOTER_HEIGHT : 0;
		int delta = target - createExtendedFilters$footerApplied;
		if (delta == 0)
			return;

		ScreenSizeAccessor size = (ScreenSizeAccessor) (Object) this;
		size.createExtendedFilters$setImageHeight(size.createExtendedFilters$getImageHeight() + delta);

		for (Slot slot : ((ContainerScreenAccessor) this).createExtendedFilters$getMenu().slots) {
			if (slot.index < FILTER_SLOT_START) {
				SlotYAccessor pos = (SlotYAccessor) slot;
				pos.createExtendedFilters$setY(pos.createExtendedFilters$getY() + delta);
			}
		}

		createExtendedFilters$footerApplied = target;
		if (createExtendedFilters$excludeScroll != null)
			createExtendedFilters$excludeScroll.visible = showFooter;
	}

	@Unique
	private void createExtendedFilters$goBack() {
		createExtendedFilters$openComponentPath = null;
		createExtendedFilters$excludeScroll.setState(0);
		createExtendedFilters$refreshComponents();
	}

	@Unique
	private void createExtendedFilters$refreshComponents() {
		if (createExtendedFilters$excludeScroll == null)
			return;

		boolean respectData = ((FilterMenuAccessor) createExtendedFilters$menu()).getRespectNBT();
		createExtendedFilters$excludeScroll.active = respectData;
		if (!respectData)
			return;

		var inventory = ((GhostItemMenu<?>) createExtendedFilters$menu()).ghostInventory;
		List<ComponentEntry> entries = new ArrayList<>(createExtendedFilters$openComponentPath == null
			? ComponentExclusionHelper.collectRootEntries(inventory)
			: ComponentExclusionHelper.collectFieldEntries(inventory, createExtendedFilters$openComponentPath));

		if (createExtendedFilters$openComponentPath != null) {
			entries.add(0, new ComponentEntry(
				ExcludeComponentsScrollInput.BACK_PATH,
				Component.translatable("createextendedfilters.gui.filter.back"),
				false));
		}

		Set<String> excluded = ((FilterMenuExtension) createExtendedFilters$menu()).createExtendedFilters$getExcludedPaths();
		if (entries.equals(createExtendedFilters$componentEntries) && excluded.equals(createExtendedFilters$lastExcluded))
			return;

		createExtendedFilters$componentEntries = entries;
		createExtendedFilters$lastExcluded = new LinkedHashSet<>(excluded);
		createExtendedFilters$excludeScroll.setExcludedCount(excluded.size());

		List<Component> options = new ArrayList<>();
		for (ComponentEntry entry : entries)
			options.add(createExtendedFilters$formatComponent(entry, excluded.contains(entry.exclusionPath())));

		if (options.isEmpty())
			options.add(Component.translatable("createextendedfilters.gui.filter.no_components")
				.withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

		MutableComponent scrollTitle = createExtendedFilters$openComponentPath == null
			? createExtendedFilters$PANEL_TITLE.plainCopy()
			: ComponentExclusionHelper.displayNameFor(createExtendedFilters$openComponentPath, "").copy();
		createExtendedFilters$excludeScroll.titled(
			CreateLang.text(scrollTitle.getString() + "...").color(ScrollInput.HEADER_RGB.getRGB()).component());

		int previous = Math.min(createExtendedFilters$excludeScroll.getState(), Math.max(options.size() - 1, 0));
		createExtendedFilters$excludeScroll.forOptions(options);
		createExtendedFilters$excludeScroll.setState(previous);
	}

	@Unique
	private Component createExtendedFilters$formatComponent(ComponentEntry entry, boolean excluded) {
		if (ExcludeComponentsScrollInput.BACK_PATH.equals(entry.exclusionPath()))
			return entry.displayName().copy().withStyle(ChatFormatting.YELLOW);

		MutableComponent name = entry.displayName().copy();
		if (entry.expandable() && createExtendedFilters$openComponentPath == null)
			name.append(Component.literal(" ›").withStyle(ChatFormatting.GRAY));
		return excluded
			? name.withStyle(ChatFormatting.STRIKETHROUGH, ChatFormatting.RED)
			: name.withStyle(ChatFormatting.WHITE);
	}

	@Unique
	private void createExtendedFilters$onCenterClick() {
		if (!((FilterMenuAccessor) createExtendedFilters$menu()).getRespectNBT())
			return;

		int index = createExtendedFilters$excludeScroll.getState();
		if (index < 0 || index >= createExtendedFilters$componentEntries.size())
			return;

		ComponentEntry entry = createExtendedFilters$componentEntries.get(index);
		if (ExcludeComponentsScrollInput.BACK_PATH.equals(entry.exclusionPath())) {
			createExtendedFilters$goBack();
			return;
		}

		if (createExtendedFilters$openComponentPath == null && entry.expandable()) {
			createExtendedFilters$openComponentPath = entry.exclusionPath();
			createExtendedFilters$excludeScroll.setState(0);
			createExtendedFilters$refreshComponents();
			return;
		}

		createExtendedFilters$toggleSelectedExclusion(entry.exclusionPath());
	}

	@Unique
	private void createExtendedFilters$toggleSelectedExclusion(String exclusionPath) {
		((FilterMenuExtension) createExtendedFilters$menu()).createExtendedFilters$toggleExcludedPath(exclusionPath);
		PacketDistributor.sendToServer(new ModNetwork.ToggleComponentExclusionPacket(exclusionPath));
		createExtendedFilters$refreshComponents();
	}
}
