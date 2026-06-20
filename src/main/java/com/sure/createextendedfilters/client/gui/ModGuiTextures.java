package com.sure.createextendedfilters.client.gui;

import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.minecraft.client.gui.GuiGraphics;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ModGuiTextures {
	private static final int FILTER_BOTTOM_V = 72;

	private ModGuiTextures() {}

	/** Continuation strip below the list filter panel — no attribute-filter slots or buttons. */
	public static void renderComponentPanel(GuiGraphics graphics, int x, int y, int width, int height) {
		var location = AllGuiTextures.FILTER.getLocation();
		graphics.blit(location, x, y, 0, FILTER_BOTTOM_V, width, height, 256, 256);
	}
}
