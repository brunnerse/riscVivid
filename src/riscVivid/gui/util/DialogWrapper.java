package riscVivid.gui.util;

import java.awt.*;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
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
	public static boolean askForSave(boolean clearEditor) {
		MainFrame mf = MainFrame.getInstance();
		File loadedFile = new File(mf.getLoadedCodeFilePath());
		String dialogText = loadedFile.exists() ?
				"Save changes to file \"" + loadedFile.getName() + "\"?" :
					"Save editor changes?";

        switch(showConfirmDialog(mf, dialogText, JOptionPane.YES_NO_CANCEL_OPTION)) {
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

	/*
	 * @return JOptionPane integer of the selected option
	 * @param optionType JOptionPane optionType
	 */
	public static int showConfirmDialog(Component parent, String message, String title, int optionType) {
	    return JOptionPane.showConfirmDialog(parent, generateLabel(message),
	            title, optionType);
	}
	public static int showConfirmDialog(Component parent, Object[] content, String title, int optionType) {
	    Object[] content_cpy = content.clone();
	    for (int i = 0; i < content_cpy.length; ++i) {
			if (content_cpy[i] instanceof String)
				content_cpy[i] = generateLabel((String) content_cpy[i]);
			else if (content_cpy[i] instanceof Component) {
				Component c = (Component)content_cpy[i];
				c.setFont(c.getFont().deriveFont((float)Preference.getFontSize()));
			}
		}
		return JOptionPane.showConfirmDialog(parent, content_cpy,
				title, optionType);
	}
	public static int showConfirmDialog(Component parent, String message, int optionType) {
        return showConfirmDialog(parent, message, null, optionType);
    }

	public static String showInputDialog(String message, String title, Object defaultVal) {
        return JOptionPane.showInputDialog(generateLabel(message), message, defaultVal);
    }
	public static String showInputDialog(String message, Object defaultVal) {
	    return JOptionPane.showInputDialog(MainFrame.getInstance(),
	            generateLabel(message), defaultVal);
	}
    public static String showInputDialog(Component parent, String message, Object defaultVal) {
        return JOptionPane.showInputDialog(parent, generateLabel(message), defaultVal);
    }
	
	public static void showMessageDialog(String message) {
	    JOptionPane.showMessageDialog(MainFrame.getInstance(), generateLabel(message));
	}
	public static void showMessageDialog(String message, String title) {
	    showMessageDialog(message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	public static void showMessageDialog(Component parent, String message) {
	    JOptionPane.showMessageDialog(parent, generateLabel(message));
	}
	
	public static void showWarningDialog(String message, String title) {
	    showMessageDialog(message, title, JOptionPane.WARNING_MESSAGE);
	}

    public static void showWarningDialog(Component parent, String message, String title) {
        showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

	public static void showWarningDialog(Component parent, Object[] content, String title) {
		showMessageDialog(parent, content, title, JOptionPane.WARNING_MESSAGE);
	}

	public static void showErrorDialog(String message) {
		showErrorDialog(message, null);
	}
	public static void showErrorDialog(Component parent, String message) {
	    showErrorDialog(parent, message, null);
	}
	public static void showErrorDialog(String message, String title) {
	    showMessageDialog(message, title, JOptionPane.ERROR_MESSAGE);
	}
	public static void showErrorDialog(Component parent, String message, String title) {
	    showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showMessageDialog(String message, String title, int JOptionPaneType) {
        showMessageDialog(MainFrame.getInstance(), message, title, JOptionPaneType);
	}
	public static void showMessageDialog(Component parent, String message, String title, int JOptionPaneType) {
        JOptionPane.showMessageDialog(parent, generateLabel(message),
                title, JOptionPaneType);
	}
	public static void showMessageDialog(Component parent, Object[] content, String title, int JOptionPaneType) {
		Object[] content_cpy = content.clone();
		for (int i = 0; i < content_cpy.length; ++i) {
			if (content_cpy[i] instanceof String)
				content_cpy[i] = generateLabel((String) content_cpy[i]);
			else if (content_cpy[i] instanceof Component) {
				Component c = (Component) content_cpy[i];
				c.setFont(c.getFont().deriveFont((float) Preference.getFontSize()));
			}
		}
		JOptionPane.showMessageDialog(parent, content_cpy,
				title, JOptionPaneType);
	}

	/*
	 * Generates a label with the preferred font size
	 */
	private static JLabel generateLabel(String message) {
	    if (message.contains("\n")) {
	        message = "<html>" + message.replace("\n", "<br>") + "</html>";
	    }
	    JLabel label = new JLabel(message);
        label.setFont(label.getFont().deriveFont((float)Preference.getFontSize()));
        return label;
    }
}
