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

import java.io.File;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.command.systemLevel.CommandOpenConfigFile;
import riscVivid.gui.command.systemLevel.CommandResetSimulator;
import riscVivid.gui.command.systemLevel.CommandSaveFrameConfigurationSysLevel;
import riscVivid.gui.command.systemLevel.CommandStartExecuting;

public class CommandRunFromConfigurationFile implements Command
{

    private MainFrame mf;

    public CommandRunFromConfigurationFile(MainFrame mf)
    {
        this.mf = mf;
    }

    @Override
    public void execute()
    {
        if (!mf.isRunning())
        {
            //save current window position
            new CommandSaveFrameConfigurationSysLevel(mf).execute();
            //show filechooser dialog and choose file
            CommandOpenConfigFile c10 = new CommandOpenConfigFile(mf);
            c10.execute();
            //get chosen file
            File configFile = c10.getConfigFile();

            //file is null if user canceled filechooser dialog
            if (configFile != null)
            {
                //reset simulator before loading new content
                new CommandResetSimulator(mf).execute();

                //initialize riscVivid and create internal frames, set status to executing
                new CommandStartExecuting(mf, configFile).execute();
            }
        }
    }

}
