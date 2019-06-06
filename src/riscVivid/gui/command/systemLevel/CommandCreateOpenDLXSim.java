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

import java.io.File;

import riscVivid.RiscVividSimulator;
import riscVivid.gui.GUI_CONST.OpenDLXSimState;
import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.util.DialogWrapper;

public class CommandCreateOpenDLXSim implements Command
{

    private MainFrame mf;//in
    private File configFile; //in
    private RiscVividSimulator sim; // out

    public CommandCreateOpenDLXSim(MainFrame mf, File f)
    {
        this.mf = mf;
        configFile = f;
    }

    @Override
    public void execute()
    {
        try
        {
            //create new riscVivid simulator
            sim = new RiscVividSimulator(configFile);
            //state executing means riscVivid is loaded
            mf.setOpenDLXSimState(OpenDLXSimState.EXECUTING);
            // assign new riscVivid to MainFrame
            mf.setOpenDLXSim(sim);
            //assign new configFile to OpenDLXSim
            mf.setConfigFile(configFile);
        }
        catch (Exception e)
        {
            sim = null;
            mf.setOpenDLXSim(sim);
            System.err.println(e.toString());
            e.printStackTrace();
            DialogWrapper.showErrorDialog(mf, "Using configFile to create riscVivid failed");
        }

    }

}
