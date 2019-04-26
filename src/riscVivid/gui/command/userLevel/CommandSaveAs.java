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
import riscVivid.gui.dialog.FileSaver;

public class CommandSaveAs implements Command
{

    @Override
    public void execute()
    {
        MainFrame mf = MainFrame.getInstance();
        if (!mf.isRunning())
        {
            File saveFile = new FileSaver().saveAs(mf);
            // cancel saving if user cancelled selection
            if (saveFile == null)
            	return;
            CommandSave.save(saveFile);

            //if there's no valid file currently loaded, 
            // set the loaded file to the chosen one
            File loadedFile = new File(mf.getLoadedCodeFilePath());
            if (!loadedFile.exists()) {
            	mf.setLoadedCodeFilePath(saveFile.getAbsolutePath());
            	mf.setEditorSavedState();
            } else if (loadedFile.equals(saveFile)) {
            	mf.setEditorSavedState();
            }
        }
    }

}
