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

import riscVivid.asm.*;
import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.util.DialogWrapper;
import riscVivid.gui.internalframes.concreteframes.editor.EditorFrame;

public class CommandCompileCode implements Command
{

    private File codeFile = null; // in 
    private File configFile = null; // out
    private MainFrame mf;

    public CommandCompileCode(MainFrame mf, File in)
    {
        codeFile = in;
        this.mf = mf;
    }

    @Override
    public void execute()
    {
        try
        {
            if (codeFile != null)
            {
                String codeFilePath = codeFile.getAbsolutePath().replace("\\", "/");
                AsmFileLoader afl = new AsmFileLoader(codeFilePath);
                configFile = afl.createConfigFile();
            }
        }
        catch (AssemblerException e)
        {
            if (e.getLine() != -1)
            {
                mf.colorEditorLine(e.getLine());
            }
            DialogWrapper.showErrorDialog(mf, e.toString(), "Error during compiling");
            EditorFrame.getInstance(mf).removeColorHighlights();
            EditorFrame.getInstance(mf).selectLine(e.getLine());
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
            e.printStackTrace();
            DialogWrapper.showErrorDialog(mf, "Compiling/Assembling Code Failed");
        }
    }

    public File getConfigFile()
    {
        return configFile;
    }

}
