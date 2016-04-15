/*******************************************************************************
 * riscVivid - A RISC-V processor simulator.
 * Copyright (C) 2013-2016 The riscVivid project, University of Augsburg, Germany
 * <https://github.com/unia-sik/riscVivid>
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
package riscVivid.gui.command.userLevel;

import javax.swing.JOptionPane;

import riscVivid.RiscVividSimulator;
import riscVivid.datatypes.uint32;
import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.command.systemLevel.CommandUpdateFrames;
import riscVivid.gui.internalframes.util.ValueInput;

public class CommandChangeMemory implements Command
{

    private MainFrame mf;
    private RiscVividSimulator openDLXSim;
    private uint32 address;

    public CommandChangeMemory(uint32 address)
    {
        this.address = address;
        this.mf = MainFrame.getInstance();
        openDLXSim = mf.getOpenDLXSim();
    }

    @Override
    public void execute()
    {
        if (mf.isExecuting())
        {
            try
            {
                Integer value = ValueInput.getValue("change memory cell value", 0);
                if (value != null)
                {
                    openDLXSim.getPipeline().getMainMemory().write_u32(address, new uint32(value));
                    new CommandUpdateFrames(mf).execute();
                }
            }
            catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(mf, "for input only Integer - decimal or hex (0x...) allowed");
                execute();
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
                e.printStackTrace();
                JOptionPane.showMessageDialog(mf, "Changing memory failed");
            }
        }

    }

}
