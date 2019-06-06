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

import java.awt.Cursor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.util.DialogWrapper;

public class CommandSave implements Command
{

	@Override
	public void execute() {
			MainFrame mf = MainFrame.getInstance();
	        if (!mf.isRunning())
	        {
	            File saveFile = new File(mf.getLoadedCodeFilePath());
	            if (saveFile.exists()) {
	            	save(saveFile);
	            	mf.setEditorSavedState();
	            } else {
	            	new CommandSaveAs().execute();	        	 
	            }
	        }
	}
	
	public static void save(File saveFile) {
		MainFrame mf = MainFrame.getInstance();
    	if (saveFile != null)
        {
            mf.getContentPane().setCursor(
                    Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			BufferedWriter out = null;
            try
            {
            	out = new BufferedWriter(new OutputStreamWriter(
            		new FileOutputStream(saveFile.getAbsolutePath()), "UTF-8"));
            			// always write UTF-8"
                out.write(mf.getEditorText());
                out.close();
            }
            catch (IOException e)
            {
                System.out.println("Exception ");
                e.printStackTrace();
                DialogWrapper.showErrorDialog(mf, "Saving file failed: " + e.toString());
				try {
					if (out != null)
						out.close();
				} catch (IOException ex) {}
            }
            finally {
                mf.getContentPane().setCursor(
                        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
	}

}
