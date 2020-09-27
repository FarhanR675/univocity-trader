package com.univocity.trader.chart.indicators;

import com.univocity.trader.chart.dynamic.code.*;
import com.univocity.trader.chart.gui.*;
import org.apache.commons.lang3.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

class IndicatorOptionsPanel extends JPanel {

	private IndicatorDefinition indicatorDefinition;
	private GridBagConstraints c;
	private final IndicatorSelector indicatorSelector;

	public IndicatorOptionsPanel(IndicatorSelector indicatorSelector) {
		super(new GridBagLayout());
		this.indicatorSelector = indicatorSelector;
	}

	public boolean updateIndicator(IndicatorDefinition indicatorDefinition) {
		if (indicatorDefinition == null && this.indicatorDefinition != null) {
			this.indicatorDefinition = null;
			ContainerUtils.clearPanel(this);
			return false;
		}
		this.indicatorDefinition = indicatorDefinition;

		ContainerUtils.clearPanel(this);
		if (indicatorDefinition != null) {
			setBorder(new TitledBorder("Indicator parameters"));
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weighty = 0.0;
			c.insets = new Insets(2, 2, 2, 2);
			c.anchor = GridBagConstraints.WEST;
			c.gridy = 0;
			indicatorDefinition.arguments.forEach(this::addComponent);
		} else {
			setBorder(null);
		}

		revalidate();
		repaint();
		return true;
	}

	private void addComponent(Argument argument) {
		JLabel lbl = new JLabel(StringUtils.capitalize(argument.name));
		JComponent input = argument.getComponent(indicatorSelector);

		if (input != null) {
			c.gridx = 0;
			c.ipadx = 100;
			if (!(input instanceof JCheckBox)) {
				add(lbl, c);
				c.gridx = 1;
			}
			c.weightx = 1.0;
			if (input instanceof UserCode) {
				c.fill = GridBagConstraints.BOTH;
				c.weighty = 1.0;
			}

			add(input, c);
			c.gridy++;
		}
		c.gridx = 0;
		c.gridwidth = 2;
	}
}
