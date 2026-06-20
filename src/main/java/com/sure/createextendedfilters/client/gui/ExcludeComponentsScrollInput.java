package com.sure.createextendedfilters.client.gui;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ExcludeComponentsScrollInput extends SelectionScrollInput {
	public static final String BACK_PATH = "\0back";

	private Runnable centerClickCallback;
	private Component triggerLabel = Component.translatable("createextendedfilters.gui.filter.exclude_components");
	private int excludedCount;

	public ExcludeComponentsScrollInput(int x, int y, int width, int height) {
		super(x, y, width, height);
		lockedTooltipX = -1;
		lockedTooltipY = -1;
	}

	public ExcludeComponentsScrollInput onCenterClick(Runnable callback) {
		centerClickCallback = callback;
		return this;
	}

	public ExcludeComponentsScrollInput withTriggerLabel(Component label) {
		triggerLabel = label;
		return this;
	}

	public void setExcludedCount(int count) {
		excludedCount = count;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if (centerClickCallback != null) {
			centerClickCallback.run();
			return;
		}
		super.onClick(mouseX, mouseY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!active || !isMouseOver(mouseX, mouseY))
			return false;
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	protected void updateTooltip() {
		toolTip.clear();
		if (!active) {
			toolTip.add(Component.translatable("createextendedfilters.gui.filter.respect_data_hint")
				.withStyle(ChatFormatting.GRAY));
			return;
		}
		if (title == null)
			return;

		toolTip.add(title.plainCopy().withStyle(s -> s.withColor(HEADER_RGB.getRGB())));

		if (options == null || options.isEmpty())
			return;

		int min = Math.min(this.max - 16, state - 7);
		int max = Math.max(this.min + 16, state + 8);
		min = Math.max(min, this.min);
		max = Math.min(max, this.max);
		if (this.min + 1 == min)
			min--;
		if (min > this.min) {
			toolTip.add(Component.literal("> ...").withStyle(ChatFormatting.GRAY));
		}
		if (this.max - 1 == max)
			max++;
		for (int i = min; i < max; i++) {
			if (i == state)
				toolTip.add(Component.empty().append("-> ").append(options.get(i)).withStyle(ChatFormatting.WHITE));
			else
				toolTip.add(Component.empty().append("> ").append(options.get(i)).withStyle(ChatFormatting.GRAY));
		}
		if (max < this.max) {
			toolTip.add(Component.literal("> ...").withStyle(ChatFormatting.GRAY));
		}

		toolTip.add(Component.empty());
		if (hint != null)
			toolTip.add(hint.plainCopy().withStyle(s -> s.withColor(HINT_RGB.getRGB())));
		toolTip.add(CreateLang.translateDirect("gui.scrollInput.scrollToSelect")
			.plainCopy().withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}

	@Override
	protected void doRender(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		var font = Minecraft.getInstance().font;
		MutableComponent text = triggerLabel.copy();
		if (excludedCount > 0)
			text.append(Component.literal(" (" + excludedCount + ")").withStyle(ChatFormatting.GRAY));

		int color = !active ? 0x666666 : isMouseOver(mouseX, mouseY) ? 0xFFFFA0 : 0x404040;
		int textY = getY() + (height - 8) / 2;
		graphics.drawString(font, text, getX(), textY, color, false);

		if (active && isMouseOver(mouseX, mouseY)) {
			int underlineY = textY + 9;
			graphics.fill(getX(), underlineY, getX() + font.width(text), underlineY + 1, color);
		}
	}
}
