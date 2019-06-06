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

import java.util.prefs.BackingStoreException;

import javax.swing.JOptionPane;

import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.Command;
import riscVivid.gui.command.systemLevel.CommandResetSimulator;
import riscVivid.gui.internalframes.concreteframes.editor.EditorFrame;
import riscVivid.gui.util.DialogWrapper;

public class CommandClearAllPreferences implements Command
{

    @Override
    public void execute()
    {
        if (DialogWrapper.showConfirmDialog(MainFrame.getInstance(),
                "All preferences will be deleted - confirm ?",
                JOptionPane.OK_CANCEL_OPTION) ==
                JOptionPane.OK_OPTION)
        {
            try
            {
                Preference.pref.clear();
                MainFrame mf = MainFrame.getInstance();
                new CommandResetSimulator(mf).execute();
                EditorFrame.getInstance(mf).resetLocationAndSize();
                // reset font size
                CommandChangeFontSize.setFontSize(Preference.getFontSize());
            }
            catch (BackingStoreException ex)
            {
                DialogWrapper.showErrorDialog("Clearing all preferences failed", "Error");
                System.err.println(ex);
                ex.printStackTrace();
            }
        }
    }

}
