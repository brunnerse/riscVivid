package riscVivid.gui.command.userLevel;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.dialog.Input;
import riscVivid.gui.internalframes.concreteframes.editor.TextNumberingPanel;
import riscVivid.gui.util.DialogWrapper;
import riscVivid.util.BreakpointManager;

import javax.swing.text.BadLocationException;

public class CommandToggleBreakpoint implements Command {
    private TextNumberingPanel tln;

    public CommandToggleBreakpoint(TextNumberingPanel tln) {
        this.tln = tln;
    }

    @Override
    public void execute() {
        try {
            int line = tln.getCurrentLine();

            BreakpointManager bm = BreakpointManager.getInstance();

            if (bm.isBreakpoint(line)) {
                bm.removeBreakpoint(line);
            } else {
                bm.addBreakpoint(line, tln.getLineText(line));
            }

            tln.updateUI();
        } catch (BadLocationException e) {
            DialogWrapper.showErrorDialog(MainFrame.getInstance(), e.getClass().getSimpleName() + " during toggling breakpoints: " + e.getMessage());
        }
    }
}
