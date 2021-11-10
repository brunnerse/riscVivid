package riscVivid.gui.command.userLevel;
import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.internalframes.concreteframes.editor.EditorFrame;
import riscVivid.gui.internalframes.concreteframes.editor.FindReplaceDialog;

public class CommandFindReplace implements Command {
    private MainFrame mf;

    public CommandFindReplace(MainFrame mf) {
        this.mf = mf;
    }
    
    @Override
    public void execute() {
        new FindReplaceDialog(mf, EditorFrame.getInstance(mf));
    }
}
