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
package riscVivid.gui.command.systemLevel;

import riscVivid.RiscVividSimulator;
import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.util.DialogWrapper;

public class CommandSimulatorFinishedInfo implements Command
{

    
    @Override
    public void execute()
    {
        MainFrame mf =MainFrame.getInstance();
        RiscVividSimulator sim = mf.getOpenDLXSim();

        if (sim == null)
            return;
        if (sim.getCurrentCycle() >= sim.getSimCycles()) {
        	DialogWrapper.showWarningDialog(mf, "The simulator has reached its maximum cycle count.\n" +
        		"This could indicate that the program is stuck in an infinite loop\n" +
        		"or that it has not been terminated correctly.",
        		"Simulator finished: Maximum cycle count exceeded");
        	
        			
        } else {
            if (sim.getExitCode() == null) { // simulator stopped because of an error
               DialogWrapper.showErrorDialog(mf, "Simulator finished because a runtime error occured");
            } else {
                DialogWrapper.showMessageDialog(mf, "Simulator finished with code " + sim.getExitCode());
            }
        }
    }

}
