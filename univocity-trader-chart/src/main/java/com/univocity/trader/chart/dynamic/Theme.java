package com.univocity.trader.chart.dynamic;

import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.painter.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */

@UIBound
public abstract class Theme implements Repaintable {

	@CheckBoxBound("Logarithmic scale")
	private boolean displayingLogarithmicScale = true;

	@Label("Background color")
	@ColorBound()
	private Color backgroundColor = new Color(255, 255, 255);

	private Color inverseBackgroundColor;

	private Color transparentBackgroundColor;

	private JPanel controlPanel;

	@CheckBoxBound("Anti-aliasing")
	private boolean isAntialiased = true;


	public boolean isAntialiased() {
		return isAntialiased;
	}

	public void setIsAntialiased(boolean isAntialiased) {
		this.isAntialiased = isAntialiased;
	}

	public boolean isDisplayingLogarithmicScale() {
		return displayingLogarithmicScale;
	}

	public void setDisplayingLogarithmicScale(boolean displayingLogarithmicScale) {
		if (this.displayingLogarithmicScale != displayingLogarithmicScale) {
			this.displayingLogarithmicScale = displayingLogarithmicScale;
		}
	}


	protected JMenuItem getLinearScaleMenuItem() {
		return createScaleMenuItem("Linear", false);
	}

	protected JMenuItem getLogarithmicScaleMenuItem() {
		return createScaleMenuItem("Logarithmic", true);
	}

	private JMenuItem createScaleMenuItem(String text, final boolean isItemForLogarithmic) {
		JMenuItem menu = new JMenuItem(text);
		menu.addActionListener(e -> setDisplayingLogarithmicScale(isItemForLogarithmic));
		menu.setEnabled(isDisplayingLogarithmicScale() != isItemForLogarithmic);
		return menu;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Color getInverseBackgroundColor() {
		if (inverseBackgroundColor == null) {
			inverseBackgroundColor = new Color(255 - backgroundColor.getRed(), 255 - backgroundColor.getGreen(), 255 - backgroundColor.getBlue(), 64);
		}
		return inverseBackgroundColor;
	}

	public Color getTransparentBackgroundColor() {
		if (transparentBackgroundColor == null) {
			this.transparentBackgroundColor = new Color(backgroundColor.getRed(), backgroundColor.getBlue(), backgroundColor.getGreen(), 196);
		}
		return transparentBackgroundColor;
	}

	public List<JMenu> getMenuOptions() {
		ArrayList<JMenu> options = new ArrayList<JMenu>();
		return options;
	}

	protected JMenu getScaleMenu() {
		JMenu appearanceMenu = new JMenu("Scale");
		appearanceMenu.add(getLinearScaleMenuItem());
		appearanceMenu.add(getLogarithmicScaleMenuItem());
		return appearanceMenu;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		this.transparentBackgroundColor = null;
		this.inverseBackgroundColor = null;
	}

	public JPanel getThemeSettingsPanel() {
		if (controlPanel == null) {
			controlPanel = PanelBuilder.createPanel(this);
		}
		return controlPanel;
	}
}
