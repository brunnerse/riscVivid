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

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import riscVivid.RiscVividSimulator;
import riscVivid.asm.DLXAssembler;
import riscVivid.datatypes.uint32;
import riscVivid.exception.MemoryException;
import riscVivid.gui.MainFrame;
import riscVivid.gui.internalframes.renderer.CodeFrameTableCellRenderer;
import riscVivid.gui.internalframes.util.NotSelectableTableModel;

public class CodeTableFactory extends TableFactory
{

    private RiscVividSimulator openDLXSim;

    public CodeTableFactory(RiscVividSimulator openDLXSim)
    {
        this.openDLXSim = openDLXSim;
    }

    @Override
    public JTable createTable()
    {

        model = new NotSelectableTableModel();
        table = new JTable(model);
        table.setFocusable(false);

        model.addColumn("address");
        model.addColumn("code hex");
        model.addColumn("code RISCV");

        //default max width values change here
        TableColumnModel tcm = table.getColumnModel();
        final int defaultWidth = 120;
        tcm.getColumn(0).setMaxWidth(defaultWidth);
        tcm.getColumn(1).setMaxWidth(defaultWidth);
        tcm.getColumn(2).setMaxWidth(2*defaultWidth);
        table.setDefaultRenderer(Object.class, new CodeFrameTableCellRenderer());

        //insert code
        int start;
        if (!openDLXSim.getConfig().containsKey("text_begin"))
            start = openDLXSim.getPipeline().getFetchStage().getPc().getValue();
        else
            start = stringToInt(openDLXSim.getConfig().getProperty("text_begin"));

        int end = openDLXSim.getSimCycles();
        if (!openDLXSim.getConfig().containsKey("text_end"))
            end = start + 4 * openDLXSim.getSimCycles();
        else
            end = stringToInt(openDLXSim.getConfig().getProperty("text_end"));

        DLXAssembler asm = new DLXAssembler();
        try
        {
            for (int i = start; i < end; i += 4)
            {
                final uint32 addr = new uint32(i);
                final uint32 inst = openDLXSim.getPipeline().getInstructionMemory().read_u32(addr);

                model.addRow(new Object[]
                        {
                            addr,
                            inst,
                            asm.Instr2Str(addr.getValue(), inst.getValue())
                        });
            }
        }
        catch (MemoryException e)
        {
            MainFrame.getInstance().getPipelineExceptionHandler().handlePipelineExceptions(e);
        }
        return table;
    }

    private int stringToInt(String s)
    {
        return Long.decode(s).intValue();
    }

}
