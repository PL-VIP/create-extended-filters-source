package com.sure.createextendedfilters.client.gui;

import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;

public class ExclusionSelectionScrollInput extends SelectionScrollInput {
	private Runnable centerClickCallback;

	public ExclusionSelectionScrollInput(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public ExclusionSelectionScrollInput onCenterClick(Runnable callback) {
		centerClickCallback = callback;
		return this;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		int relX = (int) (mouseX - getX());
		if (relX > 9 && relX < getWidth() - 9) {
			if (centerClickCallback != null)
				centerClickCallback.run();
			return;
		}
		super.onClick(mouseX, mouseY);
	}
}
