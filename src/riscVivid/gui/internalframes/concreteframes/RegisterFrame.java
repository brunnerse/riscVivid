/*******************************************************************************
 * riscVivid - A RISC-V processor simulator.
 * (C)opyright 2013-2016 The riscVivid project, University of Augsburg, Germany
 * https://github.com/unia-sik/riscVivid
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, see <LICENSE>. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package riscVivid.gui.internalframes.concreteframes;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import riscVivid.RegisterSet;
import riscVivid.datatypes.ArchCfg;
import riscVivid.datatypes.uint32;
import riscVivid.datatypes.uint8;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.internalframes.OpenDLXSimInternalFrame;
import riscVivid.gui.internalframes.factories.tableFactories.RegisterTableFactory;
import riscVivid.gui.internalframes.util.TableSizeCalculator;
import riscVivid.gui.util.MWheelFontSizeChanger;

@SuppressWarnings("serial")
public final class RegisterFrame extends OpenDLXSimInternalFrame implements ItemListener
{

    private RegisterSet rs;
    private JTable registerTable;
    private JCheckBox checkBoxHex;
    private JScrollPane scrollPane;

    static int registerOrder[] = {0, 1, 2, 3, 4, 8,  // zero - fp
                                5, 6, 7, 28, 29, 30, 31, // t0 - t6
                                10, 11, 12, 13, 14, 15, 16, 17, // a0 - a7
                                9, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27 // s1 - s11
                                };

    public RegisterFrame(String title)
    {
        super(title, false);
        this.rs = MainFrame.getInstance().getOpenDLXSim().getPipeline().getRegisterSet();
        initialize();
    }

    @Override
    public void update()
    {
        for (int i = 0; i < ArchCfg.getRegisterCount(); ++i)
        {
            int registerNum = registerOrder[i];
            final String value;
            final uint32 register_value = rs.read(new uint8(registerNum));
            if (Preference.displayRegistersAsHex())
                value = register_value.getValueAsHexString();
            else
                value = register_value.getValueAsDecimalString();
            
            registerTable.getModel().setValueAt(value, i, 1);
        }
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        //make the scrollpane
        registerTable = new RegisterTableFactory(rs, registerOrder).createTable();
        scrollPane = new JScrollPane(registerTable);
        scrollPane.setFocusable(false);

        registerTable.setFillsViewportHeight(true);
        TableSizeCalculator.setDefaultMaxTableSize(scrollPane, registerTable,
                TableSizeCalculator.SET_SIZE_WIDTH);

        MWheelFontSizeChanger.getInstance().add(scrollPane);

        //config internal frame
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        // new checkbox
        checkBoxHex = new JCheckBox("values as hex");
		checkBoxHex.setSelected(Preference.displayRegistersAsHex());
		checkBoxHex.addItemListener(this);
        JPanel controlPanel = new JPanel();
		controlPanel.add(checkBoxHex);
		add(controlPanel, BorderLayout.SOUTH);

        setFont(registerTable.getFont().deriveFont((float)Preference.getFontSize()));
        Dimension minSize = new Dimension();
        minSize.height = this.getHeight() - scrollPane.getViewport().getHeight() + registerTable.getHeight();
        Dimension desktopSize = MainFrame.getInstance().getContentPane().getSize();
        this.setPreferredSize(new Dimension(getPreferredSize().width, (int) Math.min(desktopSize.height,
            getPreferredSize().getHeight() - scrollPane.getPreferredSize().height
                    + registerTable.getRowCount() * registerTable.getRowHeight() + 50)));

        pack();
        setLocation(0,0);
        setVisible(true);
    }

    @Override
    public void setFont(Font f) {
    	super.setFont(f);
    	if (checkBoxHex != null)
    		checkBoxHex.setFont(f);
    	if (registerTable != null) {
	    	registerTable.setFont(f);
	    	registerTable.getTableHeader().setFont(f);
	    	registerTable.setRowHeight(f.getSize() + 4);
	    	TableColumn registerColumn = registerTable.getColumn("Register");
	    	int registerColWidth = registerTable.getFontMetrics(f).stringWidth("0x0: zero__");
	    	// if width is shortened, first set min width, then max width
	    	if (registerColWidth < registerColumn.getMaxWidth()) {
	    		registerColumn.setMinWidth(registerColWidth);
	        	registerColumn.setMaxWidth(registerColWidth);
	    	} else {
	        	registerColumn.setMaxWidth(registerColWidth);
	    		registerColumn.setMinWidth(registerColWidth);
	    	}
	    	TableColumn valuesColumn = registerTable.getColumn("Values");
	    	int valuesColWidth = registerTable.getFontMetrics(f).stringWidth("0x00000000_");
	    	// the last column may expand indefinitely
	    	valuesColumn.setMaxWidth(Integer.MAX_VALUE);
	    	valuesColumn.setMinWidth(valuesColWidth);

	    	int minWidth = valuesColWidth + registerColWidth;
    	}
    }

    @Override
    public void clean()
    {
        setVisible(false);
        dispose();
    }

	@Override
	public void itemStateChanged(ItemEvent e) {
		Preference.pref.putBoolean(Preference.displayRegistersAsHex, checkBoxHex.isSelected());
		update();
		
	}

}
