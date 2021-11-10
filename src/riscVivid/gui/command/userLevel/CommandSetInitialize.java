package riscVivid.gui.command.userLevel;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import riscVivid.config.GlobalConfig;
import riscVivid.gui.GUI_CONST;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.Command;
import riscVivid.gui.internalframes.util.ValueInput;
import riscVivid.gui.util.DialogWrapper;

public class CommandSetInitialize implements Command {
	public static enum Choice {
	    ZERO("0x00", 0), FF("0xFF", 0xff), C0("0xC0", 0xC0), 
	    _100("100", 100), RAND("RAND", Integer.MAX_VALUE);
	    
	    private String numberStr;
	    private int number;
	    Choice(String numberAsStr, int number) {
	        this.numberStr = numberAsStr;
	        this.number = number;
	    }
    };
    
    public static enum Component {
        MEMORY, REGISTERS;
    }

	private Choice c;
	private Component comp;
	
	
    public static int getChoiceInt(Choice c) {
        return c.number;
    }
    public static int getChoiceInt(String c_str) {
    	Choice c =  getChoiceFromString(c_str);
    	if (c == null)
    		throw new IllegalArgumentException("c_str isn't a valid choice");
    	return getChoiceInt(c);
	}

    public static String getChoiceString(Choice c) {
        return c.numberStr;
    }
    public static Choice getChoiceFromString(String c_str) {
    	for (Choice c : Choice.values()) {
			if (getChoiceString(c).equals(c_str)) {
				return c;
			}
		}
    	return null;
	}
	
	public CommandSetInitialize(Choice c, Component comp) {
		this.c = c;
		this.comp = comp;
	}
	public CommandSetInitialize(String c_str, Component comp) {
		this.comp = comp;
		c = getChoiceFromString(c_str);
		if (c == null)
    		throw new IllegalArgumentException("c_str doesn't match any Choice");
	}

	@Override
	public void execute() {
    	MainFrame mf = MainFrame.getInstance();

	    String prefKey;
	    switch(comp) {
	        case MEMORY:
	            prefKey = Preference.initializeMemoryPreferenceKey;
	            break;
	        case REGISTERS:
	            prefKey = Preference.initializeRegistersPreferenceKey;
	            break;
	        default:
	            return;
	    }
	    int oldChoice = Preference.pref.getInt(prefKey, getChoiceInt(Choice.ZERO));

	    if (getChoiceInt(c) != oldChoice) {
			Preference.pref.putInt(prefKey, getChoiceInt(c));
			// if simulator was started, display message that simulator needs to be restarted in order to apply the new settings
			if (mf.getOpenDLXSimState() != GUI_CONST.OpenDLXSimState.IDLE &&
					Preference.pref.getBoolean(Preference.showInitializeOptionMessage, true))
			{
				final String message = "In order to apply the new settings, the program must be recompiled.";
				final JCheckBox checkbox = new JCheckBox("Do not show this message again.");
				final Object content[] = {message, checkbox};
				DialogWrapper.showWarningDialog(mf, content, "");

				Preference.pref.putBoolean(Preference.showInitializeOptionMessage, !checkbox.isSelected());
			}

		}

	}
}
