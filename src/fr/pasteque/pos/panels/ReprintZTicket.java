//    POS-Tech
//    Based upon Openbravo POS
//
//    Copyright (C) 2007-2009 Openbravo, S.L.
//                       2012 SARL SCOP Scil (http://scil.coop)
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

package fr.pasteque.pos.panels;

import fr.pasteque.basic.BasicException;
import fr.pasteque.beans.JCalendarDialog;
import fr.pasteque.data.loader.Datas;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.data.loader.StaticSentence;
import fr.pasteque.data.loader.SerializerWriteBasic;
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppView;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.BeanFactoryApp;
import fr.pasteque.pos.forms.BeanFactoryException;
import fr.pasteque.pos.forms.DataLogicSales;
import fr.pasteque.pos.forms.DataLogicSystem;
import fr.pasteque.pos.forms.JPanelView;
import fr.pasteque.pos.forms.JPrincipalApp;
import fr.pasteque.pos.printer.DeviceTicket;
import fr.pasteque.pos.printer.TicketParser;
import fr.pasteque.pos.printer.TicketPrinterException;
import fr.pasteque.pos.scripting.ScriptEngine;
import fr.pasteque.pos.scripting.ScriptException;
import fr.pasteque.pos.scripting.ScriptFactory;
import fr.pasteque.pos.ticket.CashSession;
import fr.pasteque.pos.ticket.ZTicket;
import fr.pasteque.pos.util.ThumbNailBuilder;
import fr.pasteque.pos.widgets.CoinCountButton;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.WidgetsBuilder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class ReprintZTicket extends JPanel implements JPanelView, BeanFactoryApp {

    private JPanel coinCountBtnsContainer;
    private List<CoinCountButton> coinButtons;
    private JLabel totalAmount;
    private double total;
    private CashSession selectedCash;

    private DeviceTicket ticketPrinter;
    private TicketParser screenPrinter;
    private TicketParser paperPrinter;

    public ReprintZTicket() {
        initComponents();
        this.ticketList.setCellRenderer(new ZTicketRenderer());
        this.ticketPrinter = new DeviceTicket();   
        this.screenPrinter = new TicketParser(this.ticketPrinter,
                new DataLogicSystem());
        this.ticketPanel.add(this.ticketPrinter.getDevicePrinter("1").getPrinterComponent(), BorderLayout.CENTER);
        this.printBtn.setEnabled(false);
    }

    public void init(AppView app) throws BeanFactoryException {
        DataLogicSystem dlSys = new DataLogicSystem();
        this.paperPrinter = new TicketParser(app.getDeviceTicket(), dlSys);
    }
    public Object getBean() {
        return this;
    }
    
    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        return AppLocal.getIntString("Menu.ReprintZTicket");
    }
    
    public boolean requiresOpenedCash() {
        return false;
    }
    
    public void activate() throws BasicException {
    }

    public boolean deactivate() {
        return true;
    }

    public void executeSearch() {
        try {
            this.ticketList.clearSelection();
            DataLogicSystem dlSys = new DataLogicSystem();
            Date start = (Date) Formats.TIMESTAMP.parseValue(this.startDate.getText());
            Date stop = (Date) Formats.TIMESTAMP.parseValue(this.endDate.getText());
            List<CashSession> tkts = null;
            tkts = dlSys.searchCashSession(null, start, stop);
            this.ticketList.setModel(new MyListData(tkts));
        } catch (BasicException e) {
            e.printStackTrace();
        }

    }

    /** Show selected ticket in preview */
    private void printTicket(TicketParser device) {
        DataLogicSystem dlSys = new DataLogicSystem();
        DataLogicSales dlSales = new DataLogicSales();
        String sresource = dlSys.getResourceAsXML("Printer.CloseCash");
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
            return;
        }
        this.ticketPrinter.getDevicePrinter("1").reset();
        try {
            PaymentsModel payments = PaymentsModel.loadInstance(this.selectedCash);
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
            script.put("payments", payments);
            device.printTicket(script.eval(sresource).toString());
        } catch (ScriptException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
            msg.show(this);
        } catch (TicketPrinterException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
            msg.show(this);
        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
            msg.show(this);
        }
    }

    private class ZTicketRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, null, index, isSelected,
                    cellHasFocus);
            CashSession session = (CashSession) value;
            String sHtml = "<tr><td>"
                    + session.getHost() + "<br /> [" + session.getSequence() + "]"
                    + "</td>"
                    + "<td>"
                    + Formats.TIMESTAMP.formatValue(session.getOpenDate())
                    + "</td>"
                    + "<td>"
                    + Formats.TIMESTAMP.formatValue(session.getCloseDate())
                    + "</td>";
            setText("<html><table>" + sHtml +"</table></html>");
            return this;
        }   
    }
    private static class MyListData extends javax.swing.AbstractListModel {
        private java.util.List m_data;
        public MyListData(java.util.List data) {
            m_data = data;
        }
        @Override
        public Object getElementAt(int index) {
            return m_data.get(index);
        }
        @Override
        public int getSize() {
            return m_data.size();
        }
    }

    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = null;

        JLabel startLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.StartDate"));
        this.startDate = WidgetsBuilder.createTextField();
        this.startBtn = WidgetsBuilder.createButton(ImageLoader.readImageIcon("calendar.png"));
        this.startBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDateStartActionPerformed(evt);
            }
        });
        JLabel endLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.EndDate"));
        this.endDate = WidgetsBuilder.createTextField();
        this.endBtn = WidgetsBuilder.createButton(ImageLoader.readImageIcon("calendar.png"));
        this.endBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDateEndActionPerformed(evt);
            }
        });
        JButton run = WidgetsBuilder.createButton(ImageLoader.readImageIcon("execute.png"),
                AppLocal.getIntString("button.executefilter"),
                WidgetsBuilder.SIZE_MEDIUM);
        run.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });
        this.printBtn = WidgetsBuilder.createButton(ImageLoader.readImageIcon("tkt_print.png"),
                AppLocal.getIntString("button.print"),
                WidgetsBuilder.SIZE_MEDIUM);
        this.printBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printActionPerformed(evt);
            }
        });


        this.ticketPanel = new JPanel();
        this.ticketPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.ticketPanel.setLayout(new java.awt.BorderLayout());
        JScrollPane ticketListScroll = new JScrollPane();
        ticketListScroll.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        this.ticketList = new javax.swing.JList();
        this.ticketList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListTicketsValueChanged(evt);
            }
        });
        ticketListScroll.setViewportView(this.ticketList);

        // Input zone
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        // Start date
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(btnSpacing, btnSpacing, btnSpacing, btnSpacing);
        inputPanel.add(startLbl, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.5;
        c.insets = new Insets(btnSpacing, 0, btnSpacing, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(startDate, c);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(btnSpacing, btnSpacing, btnSpacing, btnSpacing);
        inputPanel.add(startBtn, c);
        // End date
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0, btnSpacing, btnSpacing, btnSpacing);
        inputPanel.add(endLbl, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.5;
        c.insets = new Insets(0, 0, btnSpacing, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(endDate, c);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.insets = new Insets(0, btnSpacing, btnSpacing, btnSpacing);
        inputPanel.add(endBtn, c);
        // Search
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        c.insets = new Insets(0, btnSpacing, btnSpacing, btnSpacing);
        inputPanel.add(run, c);
        // Add all
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        this.add(inputPanel, c);

        // Ticket list zone
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        this.add(ticketListScroll, c);

        // Third column: ticket preview
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        this.add(this.ticketPanel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        this.add(this.printBtn, c);
    }

    private void btnDateStartActionPerformed(java.awt.event.ActionEvent evt) {
        Date date;
        try {
            date = (Date) Formats.TIMESTAMP.parseValue(this.startDate.getText());
        } catch (BasicException e) {
            date = null;
        }        
        date = JCalendarDialog.showCalendarTimeHours(this, date);
        if (date != null) {
            this.startDate.setText(Formats.TIMESTAMP.formatValue(date));
        }
    }
    private void btnDateEndActionPerformed(java.awt.event.ActionEvent evt) {
        Date date;
        try {
            date = (Date) Formats.TIMESTAMP.parseValue(this.endDate.getText());
        } catch (BasicException e) {
            date = null;
        }        
        date = JCalendarDialog.showCalendarTimeHours(this, date);
        if (date != null) {
            this.endDate.setText(Formats.TIMESTAMP.formatValue(date));
        }
    }
    private void jListTicketsValueChanged(javax.swing.event.ListSelectionEvent evt) {
        this.selectedCash = (CashSession) this.ticketList.getSelectedValue();
        if (this.selectedCash != null) {
            this.printTicket(this.screenPrinter);
            this.printBtn.setEnabled(true);
        }
    }
    private void searchActionPerformed(java.awt.event.ActionEvent evt) {
        this.executeSearch();
    }
    private void printActionPerformed(java.awt.event.ActionEvent evt) {
        this.printTicket(this.paperPrinter);
    }

    private JList ticketList;
    private JPanel ticketPanel;
    private JTextField startDate;
    private JTextField endDate;
    private JButton startBtn;
    private JButton endBtn;
    private JButton printBtn;
}
