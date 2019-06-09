package riscVivid.gui.command.userLevel;
import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.internalframes.concreteframes.editor.EditorFrame;
import riscVivid.gui.internalframes.concreteframes.editor.FindReplaceDialog;

public class CommandFindReplace implements Command {
    private MainFrame mf;
    private EditorFrame editor;
    
    public CommandFindReplace(MainFrame mf, EditorFrame editor) {
        this.mf = mf;
        this.editor = editor;
    }
    
    @Override
    public void execute() {
        new FindReplaceDialog(mf, editor);
    }
}
