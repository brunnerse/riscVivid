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
package riscVivid.gui.dialog;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.util.DialogWrapper;

public class FileSaver
{
    /**
     * @return null if user cancelled selection
     */
    public File saveAs(MainFrame mf)
    {
    	String path = Preference.getSaveFileChooserPath();
        @SuppressWarnings("serial")
        final JFileChooser chooser = new JFileChooser(path)
        {
            @Override
            public void approveSelection()
            {
                File f = getSelectedFile();

                if (f.getName().indexOf('.') < 0)
                	f = new File(f.getAbsolutePath() + ".s");
                this.setSelectedFile(f);
                // Ask for overwrite if file exists and the chosen file isn't the loaded file
                if (f.exists() && !f.getAbsolutePath().equals(MainFrame.getInstance().getLoadedCodeFilePath()))
                {
                    int result = DialogWrapper.showConfirmDialog(this, "The file exists, overwrite?",
                            "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result)
                    {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CLOSED_OPTION:
                            return;
                    }
                }
                super.approveSelection();
            }

        };
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setVisible(true);
        chooser.setFileFilter(new FileFilter()
        {
            @Override
            public boolean accept(File f)
            {
                return f.getName().toLowerCase().endsWith(".s") || f.isDirectory();
            }

            @Override
            public String getDescription()
            {
                return "Assembler Files(*.s)";
            }
        });

        String filePath = mf.getLoadedCodeFilePath();
        if (!new File(filePath).exists())
        	filePath = "code.s"; //default
        chooser.setSelectedFile(new File(filePath));

        if (chooser.showSaveDialog(mf) == JFileChooser.APPROVE_OPTION)
        {
            path = chooser.getSelectedFile().getParent();
            Preference.pref.put(Preference.saveChooserPathPreferenceKey, path);
            return chooser.getSelectedFile();
        }
        else
        {
            return null;
        }
    }

}
