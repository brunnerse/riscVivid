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

import java.awt.Cursor;
import java.io.File;
import java.io.FileNotFoundException;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.util.DialogWrapper;
import riscVivid.util.CodeLoader;

/**
 * if the command fails to load the file to the editor,
 * the command does the error handling (including an error message to the user) itself.
 * For the caller to check whether it has failed, use function hasFailed() after call
 */
public class CommandLoadCodeFileToEditor implements Command
{

    private MainFrame mf;
    private File codeFile;
    private boolean clean;
    private boolean hasFailed = false;

    public CommandLoadCodeFileToEditor(MainFrame mf, File f, boolean clean)
    {
        this.mf = mf;
        this.codeFile = f;
        this.clean = clean;
    }

    @Override
    public void execute()
    {
        hasFailed = false;
        try
        {
            mf.getContentPane().setCursor(
                    Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            String help = codeFile.getAbsolutePath().replace("\\", "/");
            String text;
            if (clean == true)
                text = "";
            else
                text = mf.getEditorText() + "\n";
            text += CodeLoader.loadCode(help);
            mf.setEditorText(text);
            if (clean)
            	mf.setEditorSavedState();
        }
        catch (FileNotFoundException e) {
            System.err.println(e.toString());
            e.printStackTrace();
            DialogWrapper.showErrorDialog(mf, "File " + codeFile.getAbsolutePath() + " doesn't exist",
                    "Loading file failed");
            this.hasFailed = true;
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
            e.printStackTrace();
            DialogWrapper.showErrorDialog(mf, e.getMessage(),
                    "Loading file failed");
            this.hasFailed = true;
        }
        finally
        {
            mf.getContentPane().setCursor(
                    Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
      * @return whether the last execute() call has failed to load the file into the editor
     */
    public boolean hasFailed() {
       return this.hasFailed;
    }
}
