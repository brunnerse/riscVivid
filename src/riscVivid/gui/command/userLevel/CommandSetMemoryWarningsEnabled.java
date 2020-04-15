package riscVivid.gui.command.userLevel;

import javax.swing.JCheckBoxMenuItem;

import riscVivid.gui.Preference;
import riscVivid.gui.command.Command;

public class CommandSetMemoryWarningsEnabled implements Command {
    
    JCheckBoxMenuItem menuItem;
    
    public CommandSetMemoryWarningsEnabled(JCheckBoxMenuItem menuItem) {
    	this.menuItem = menuItem;
    }
    @Override
    public void execute() {
    	Preference.pref.putBoolean(Preference.enableMemoryWarningsPreferenceKey, menuItem.isSelected());
    }
}
