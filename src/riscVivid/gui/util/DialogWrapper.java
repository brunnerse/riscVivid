package riscVivid.gui.util;

import java.io.File;

import javax.swing.JOptionPane;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.userLevel.CommandSave;

public final class DialogWrapper {
	/**
	 * Asks user in a dialog if current content should be saved,
	 * and does so in that case
	 * To be called whenever the program modifies the text in the editor,
	 * e.g. when loading a new file
	 * @return: true if program can proceed with its action(closing / loading another file),
	 * 			false if program should abort
	 * @param clearEditor:  clear Editor except if user chooses 'Cancel'
	 *
	 */
	public static boolean askAndSave(boolean clearEditor) {
		MainFrame mf = MainFrame.getInstance();
		File loadedFile = new File(mf.getLoadedCodeFilePath());
		String dialogText = loadedFile.exists() ?
				"Save changes to file \"" + loadedFile.getName() + "\"?" :
					"Save editor changes?";

        switch(JOptionPane.showConfirmDialog(mf,dialogText)) {
        case JOptionPane.OK_OPTION:
			new CommandSave().execute();
			if (!mf.isEditorTextSaved()) // if user aborted saving in the SaveAs-dialog
				return false;
        case JOptionPane.NO_OPTION:
			if (clearEditor) {
                 mf.setEditorText("");
                 mf.setEditorSavedState();
                 mf.setLoadedCodeFilePath("");
			}
			return true;
        case JOptionPane.CANCEL_OPTION:
        default:
			return false;         
         }
	}
}
