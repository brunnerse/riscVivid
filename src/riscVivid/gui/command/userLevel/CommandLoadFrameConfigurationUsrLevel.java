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
import riscVivid.gui.command.systemLevel.CommandLoadFrameConfigurationSysLevel;
import riscVivid.gui.util.DialogWrapper;

public class CommandLoadFrameConfigurationUsrLevel implements Command
{

    private MainFrame mf;//in

    public CommandLoadFrameConfigurationUsrLevel(MainFrame mf)
    {
        this.mf = mf;
    }

    @Override
    public void execute()
    {
        if (!mf.isRunning() && DialogWrapper.showConfirmDialog(mf,
                "Current window configuration will be lost. Proceed?", JOptionPane.YES_NO_OPTION) ==
                JOptionPane.YES_OPTION)
        {
            try
            {
                new CommandLoadFrameConfigurationSysLevel(mf).execute();
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
                e.printStackTrace();
                DialogWrapper.showMessageDialog(mf, "loading frame preferences failed");
            }
        }

    }

}
