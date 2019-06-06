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

import javax.swing.JOptionPane;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.command.systemLevel.CommandSaveFrameConfigurationSysLevel;
import riscVivid.gui.util.DialogWrapper;

public class CommandSaveFrameConfigurationUsrLevel implements Command
{

    private MainFrame mf;//in

    public CommandSaveFrameConfigurationUsrLevel(MainFrame mf)
    {
        this.mf = mf;
    }

    @Override
    public void execute()
    {
        if (!mf.isRunning() && (JOptionPane.showConfirmDialog(mf,
                "Saving current window configuration will overwrite old configuration. Proceed ?")) ==
                JOptionPane.YES_OPTION)
        {
            try
            {
                new CommandSaveFrameConfigurationSysLevel(mf).execute();
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
                e.printStackTrace();
                DialogWrapper.showErrorDialog(mf, "saving frame preferences failed");
            }
        }
    }

}
