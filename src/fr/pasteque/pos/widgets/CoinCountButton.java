//    POS-Tech
//
//    Copyright (C) 2012 Scil (http://scil.coop)
//
//    This file is part of POS-Tech.
//
//    POS-Tech is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    POS-Tech is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with POS-Tech.  If not, see <http://www.gnu.org/licenses/>.


package fr.pasteque.pos.widgets;

import fr.pasteque.basic.BasicException;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.widgets.JEditorNumber;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** A button to count coins and get total amount */
public class CoinCountButton {

    private JPanel component;
    private JEditorNumber input;
    private JEditorKeys keypad;
    private int count;
    private double amount;
    private Listener listener;

    public CoinCountButton(ImageIcon image, double amount,
            JEditorKeys keypad, Listener listener) {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
        this.amount = amount;
        this.listener = listener;
        this.component = new JPanel();
        this.component.setLayout(new GridBagLayout());
        JButton btn = WidgetsBuilder.createButton(image,
                Formats.CURRENCY.formatValue(amount));
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setRequestFocusEnabled(false);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.addActionListener(new AddListener());
        GridBagConstraints cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        this.component.add(btn, cstr);
        this.input = new JEditorIntegerPositive();
        this.keypad = keypad;
        this.input.addEditorKeys(keypad);
        input.addPropertyChangeListener("Edition", new RecalculateAmount());
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.insets = new Insets(btnSpacing, 0, 0, 0);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        this.component.add(this.input, cstr);
    }

    public void reset() {
        this.input.reset();
        this.count = 0;
    }

    /** Get unit value */
    public double getValue() {
        return this.amount;
    }

    public int getCount() {
        try {
            return this.input.getValueInteger();
        } catch (BasicException e) {
            return 0;
        }
    }

    /** Get total amount */
    public double getAmount() {
        return this.getCount() * this.amount;
    }

    public JComponent getComponent() {
        return this.component;
    }

    private void update() {
        this.input.setValueInteger(this.count);
    }

    private class RecalculateAmount implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            CoinCountButton btn = CoinCountButton.this;
            if (btn.input.getText().equals("")) {
                // Cleared: reset
                btn.count = 0;
            }
            if (btn.listener != null) {
                btn.listener.countUpdated();
            }
        }
    }

    private class AddListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            CoinCountButton btn = CoinCountButton.this;
            btn.count++;
            btn.update();
            if (btn.listener != null) {
                btn.listener.coinAdded(btn.amount, btn.count);
            }
            btn.input.activate();
        }
    }

    public interface Listener {
        public void coinAdded(double amount, int newCount);
        public void countUpdated();
    }
}
