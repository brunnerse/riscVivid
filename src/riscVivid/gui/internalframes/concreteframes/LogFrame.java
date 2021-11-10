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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import riscVivid.datatypes.uint8;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.userLevel.CommandChangeRegister;
import riscVivid.gui.internalframes.OpenDLXSimInternalFrame;
import riscVivid.gui.internalframes.renderer.LogFrameTableCellRenderer;
import riscVivid.gui.internalframes.util.LogReader;
import riscVivid.gui.internalframes.util.NotSelectableTableModel;
import riscVivid.gui.util.DialogWrapper;
import riscVivid.gui.util.MWheelFontSizeChanger;

@SuppressWarnings("serial")
public final class LogFrame extends OpenDLXSimInternalFrame
{

    //tables, scrollpane and table models
    private JTable infoTable;
    private NotSelectableTableModel model;
    private JScrollPane scrollpane;
    //data
    private LogReader logReader;
    private String logFileAddr;

    private String longestRow = ""; //needed in update() to determine scrollbar viewport width

    public LogFrame(String title)
    {
        super(title, true);
        MainFrame mf = MainFrame.getInstance();
        initialize();
        try
        {
            logFileAddr = mf.getOpenDLXSim().getConfig().getProperty("log_file");
            logReader = new LogReader(logFileAddr);
        }
        catch (Exception e)
        {
            String err = "Reading log file failed -  " + e.toString();
            DialogWrapper.showErrorDialog(mf, err);
        }

        // Change resize mode of the table appropriately 021if the size changes
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
            	setAppropriateScrollPaneResizeMode();
            }
        });
    }

    @Override
    public void update()
    {
        if (logFileAddr != null && logReader != null)
        {
            logReader.update();
            
            for(String s : logReader.getLog())
            {
                model.addRow(new String[]{s});
                if (s.length() > longestRow.length())
                    longestRow = s;
            }

            // Adjust size of table depending on the longest row string and number of rows
            int width = getFontMetrics(infoTable.getFont()).stringWidth(longestRow + "xxx");
            infoTable.setPreferredSize(new Dimension(width,
            		infoTable.getRowCount() * infoTable.getRowHeight()));
            setAppropriateScrollPaneResizeMode();

            // if new column was added, scroll to the bottom
            if (scrollpane != null && logReader.getLog().size() > 0)
                infoTable.scrollRectToVisible(infoTable.getCellRect(
                        infoTable.getRowCount() - 1, 0, true));
            logReader.getLog().clear();
        }
        else {
            infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }
    }

    @Override
    public void clean()
    {
        setVisible(false);
        dispose();
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        MainFrame mf = MainFrame.getInstance();
        setLayout(new BorderLayout());
        model = new NotSelectableTableModel();
        infoTable = new JTable(model);
        infoTable.setFocusable(false);
        model.addColumn("");
        infoTable.setShowGrid(false);
        infoTable.setDefaultRenderer(Object.class, new LogFrameTableCellRenderer());
        scrollpane = new JScrollPane(infoTable);
        scrollpane.setFocusable(false);
        add(scrollpane, BorderLayout.CENTER);

        MWheelFontSizeChanger.getInstance().add(scrollpane);

        infoTable.addMouseListener(new MouseAdapter()
        {
            int selectedRow = -1;

            @Override
            public void mouseClicked(MouseEvent e)
            {
                Point p = e.getPoint();
                int row = infoTable.rowAtPoint(p);

                // if one row is selected, delete the selection if the user clicks on the row
                if (infoTable.getSelectedRows().length == 1) {
                    if (selectedRow == row){
                        infoTable.clearSelection();
                        selectedRow = -1;
                    } else {
                        selectedRow = row;
                    }
                } else {
                    selectedRow = -1;
                }
            }
        });

        setFont(infoTable.getFont().deriveFont((float)(Preference.getFontSize())));
        Dimension desktopSize = mf.getContentPane().getSize();
        setPreferredSize(new Dimension(desktopSize.width/2, infoTable.getRowHeight() * 20));
        pack();
        this.setLocation(0, 30);
        setVisible(true);
    }

    @Override
    public void setFont(Font f) {
    	super.setFont(f);
    	if (infoTable != null) {
    		infoTable.setFont(f);
    		infoTable.setRowHeight(f.getSize() + 4);
    		update();
    	}
    }

    /**
     *  if the table is shorter than the width of the frame(so the horizontal scroll bar is hidden),
    	the table should automatically resize itself
    */
    private void setAppropriateScrollPaneResizeMode() {
    	if (infoTable != null && scrollpane != null) {
		    int width = getFontMetrics(infoTable.getFont()).stringWidth(longestRow + "xxx");
		    if (scrollpane.getVisibleRect().getWidth() > width)
		    	infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		    else
		    	infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    	}
    }
}
