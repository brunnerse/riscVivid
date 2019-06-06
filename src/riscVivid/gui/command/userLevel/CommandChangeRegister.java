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
package riscVivid.gui.command.userLevel;

import riscVivid.RiscVividSimulator;
import riscVivid.datatypes.uint32;
import riscVivid.datatypes.uint8;
import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.command.systemLevel.CommandUpdateFrames;
import riscVivid.gui.internalframes.util.ValueInput;
import riscVivid.gui.util.DialogWrapper;

public class CommandChangeRegister implements Command
{

    private int row; //in
    private MainFrame mf;
    private RiscVividSimulator openDLXSim;

    public CommandChangeRegister(int row)
    {
        this.row = row;
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
                Integer value = ValueInput.getValue("change register value: ",0);
                if (value != null)
                {
                    openDLXSim.getPipeline().getRegisterSet().write(new uint8(row), new uint32(value));
                    new CommandUpdateFrames(mf).execute();
                }
            }
            catch (NumberFormatException e)
            {
                DialogWrapper.showErrorDialog(mf, "for input only Integer - decimal or hex (0x...) allowed",
                        "Wrong input");
                execute();
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
                e.printStackTrace();
                DialogWrapper.showErrorDialog(mf, "Changing register failed");
            }
        }

    }

}
