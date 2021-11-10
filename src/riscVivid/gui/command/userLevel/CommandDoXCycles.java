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
import riscVivid.exception.PipelineException;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.Command;
import riscVivid.gui.command.systemLevel.CommandSimulatorFinishedInfo;
import riscVivid.gui.command.systemLevel.CommandUpdateFrames;
import riscVivid.gui.internalframes.util.ValueInput;
import riscVivid.gui.util.DialogWrapper;

public class CommandDoXCycles implements Command
{

    private MainFrame mf;
    private String preferenceKey = "doxcyclesnumber";
    //default cycle number 
    private int cycles = 5;

    public CommandDoXCycles(MainFrame mf)
    {
        this.mf = mf;
    }

    @Override
    public void execute()
    {
        if (mf.isExecuting())
        {
            if (mf.getOpenDLXSim().isFinished())
                new CommandResetCurrentProgram(mf).execute();

            try
            {
                RiscVividSimulator openDLXSim = mf.getOpenDLXSim();
                // if there is a saved preference for x cycles, load it
                cycles = Preference.pref.getInt(preferenceKey, cycles);
                //show inputDialog and get input value
                Integer value = ValueInput.getValue("cycles: ", cycles);
                // value is null if there was an error during input
                if (value != null)
                {
                    // if value is valid, save it as new preference
                    Preference.pref.putInt(preferenceKey, value);
                    for (int i = 1; i <= value; ++i)
                    {
                        if (!openDLXSim.isFinished())
                        {
                            try
                            {
                                openDLXSim.step();
                            }
                            catch (PipelineException e)
                            {
                                mf.getPipelineExceptionHandler().handlePipelineExceptions(e);
                                break;
                            }
                        }
                        else
                        {
                            break;
                        }

                    }
                    new CommandUpdateFrames(mf).execute();

                    if (openDLXSim.isFinished())
                        new CommandSimulatorFinishedInfo().execute();
                }
            }
            catch (NumberFormatException e)
            {
                DialogWrapper.showWarningDialog(mf, "for input only Integer - decimal or hex (0x...) allowed", "Invalid input");
                //if an error during input occured, restart input dialog to get new input
                execute();
            }
            catch (Exception e)
            {
                //something else went wrong
                System.err.println(e.toString());
                e.printStackTrace();
                DialogWrapper.showErrorDialog(mf, "Executing commands failed");
            }
        }


    }

}
