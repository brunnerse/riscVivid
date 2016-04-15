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
package riscVivid.gui.internalframes.factories.tableFactories;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import riscVivid.RiscVividSimulator;
import riscVivid.datatypes.uint32;
import riscVivid.exception.MemoryException;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.userLevel.CommandChangeMemory;
import riscVivid.gui.internalframes.renderer.ChangeableFrameTableCellRenderer;
import riscVivid.gui.internalframes.util.NotSelectableTableModel;

public class MemoryTableFactory extends TableFactory
{

    private int rows;
    private int startAddr;
    private RiscVividSimulator openDLXSim;

    public MemoryTableFactory(int rows, int startAddr)
    {
        this.rows = rows;
        this.startAddr = startAddr;
        openDLXSim = MainFrame.getInstance().getOpenDLXSim();
    }

    @Override
    public JTable createTable()
    {
        model = new NotSelectableTableModel();
        table = new JTable(model);
        table.setFocusable(false);
        model.addColumn("address");
        model.addColumn("value");

        try
        {
            for (int i = 0; i < rows; ++i)
            {
                final Object secondItem;
                if (Preference.displayMemoryAsHex())
                    secondItem = openDLXSim.getPipeline().getMainMemory().read_u32(new uint32(startAddr + i * 4), false);
                else
                    secondItem = openDLXSim.getPipeline().getMainMemory().read_u32(new uint32(startAddr + i * 4), false).getValue();

                model.addRow(new Object[]
                        { new uint32(startAddr + i * 4), secondItem });
            }
        }
        catch (MemoryException e)
        {
            MainFrame.getInstance().getPipelineExceptionHandler().handlePipelineExceptions(e);
        }

        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setMaxWidth(150);
        tcm.getColumn(1).setMaxWidth(150);
        table.setDefaultRenderer(Object.class, new ChangeableFrameTableCellRenderer());

        table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);

                if (e.getClickCount() == 2)
                    new CommandChangeMemory(new uint32(Integer.parseInt(
                            model.getValueAt(row, 0).toString().substring(2),
                            16))).execute();
            }

        });

        table.setFillsViewportHeight(true);
        return table;
    }

}
