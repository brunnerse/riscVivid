package riscVivid.gui.command.userLevel;

import riscVivid.asm.tokenizer.Token;
import riscVivid.asm.tokenizer.TokenType;
import riscVivid.asm.tokenizer.Tokenizer;
import riscVivid.asm.tokenizer.TokenizerException;
import riscVivid.gui.GUI_CONST.OpenDLXSimState;
import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;
import riscVivid.gui.command.systemLevel.CommandWriteToTmpFile;
import riscVivid.gui.internalframes.concreteframes.editor.EditorFrame;
import riscVivid.gui.util.DialogWrapper;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class CommandReformatCode implements Command {

    private static final List<String> specialDirectives = Arrays.asList(new String[]{".global", ".data", ".text"});

    @Override
    public void execute() {
       MainFrame mf = MainFrame.getInstance();
        try {
            LineNumberReader reader = new LineNumberReader(new StringReader(mf.getEditorText()));
            Tokenizer tokenizer = new Tokenizer(reader);
            StringBuilder output = new StringBuilder();
            final int tabSize = EditorFrame.getInstance(mf).getTabSize();

            Token[] lineTokens;
            reader.mark(1000);
            while ((lineTokens = tokenizer.readLine()) != null)
            {   reader.reset(); // reset to last mark
                String lineContent = reader.readLine();
                StringBuilder line = new StringBuilder();
                reader.mark(1000);
                Token lastToken = null;
                int lastTokenEndPosOld = 0;   // position after the last token in the original line
                for (Token t : lineTokens) {
                    switch(t.getTokenType()) {
                        case Label:
                            line.append(t.getString());
                            break;
                        case Directive:
                            if (specialDirectives.contains(t.getString())) {
                                if (lastToken != null) // there's something before the directive in the line
                                    line.append("\n");
                                line.append("\t");
                            }
                            else {
                                line.append("\t");
                                if (lastToken == null || lastToken.getString().length() < tabSize)
                                    line.append("\t");
                            }
                            line.append(t.getString());
                            break;
                        case Separator:
                            line.append(t.getString());
                           if (t.getString().equals(","))
                               line.append(' ');
                           break;
                        case IntegerConstant:
                        case Identifier:
                            if (lastToken != null && lastToken.getTokenType() == TokenType.Directive
                                    && specialDirectives.contains(lastToken.getString())) {
                                line.append(" ");
                                line.append(t.getString());
                                break;
                            } // intentionally no break here
                        case Operator:
                        case Register:
                            if (lastToken != null)
                                if (lastToken.getTokenType() == TokenType.Mnemonic ||
                                        lastToken.getTokenType() == TokenType.Directive)
                                    line.append("\t");
                            line.append(t.getString());
                            break;
                        case CharacterLiteral:
                        case StringLiteral:
                            char separator = t.getTokenType() == TokenType.CharacterLiteral ? '\'' : '\"';
                            line.append("\t");
                            line.append(separator);
                            line.append(t.getString());
                            // the ending separator is in the line of the line and gets added after the switch statement
                            break;
                        case Mnemonic:
                            if (lastToken != null && lastToken.getTokenType() == TokenType.Label)
                                line.append("\n");
                            line.append("\t");
                            line.append(t.getString());
                            break;
                        case Unknown:
                        default:
                    }
                    lastToken = t;
                    lastTokenEndPosOld = lineContent.indexOf(t.getString(), lastTokenEndPosOld) + t.getString().length();
                }
                int lastTokenEndPosNew = line.length();
                line.append(lineContent.substring(lastTokenEndPosOld));
                // line of line: format comments
                int commentStartIdx = line.indexOf("#", lastTokenEndPosNew);
                // remove leading whitespaces before the comment
                while (commentStartIdx > 0 && line.codePointAt(commentStartIdx-1) <= 0x20)
                    line.deleteCharAt(--commentStartIdx);
                // if line contains a comment and something before the comment, set tabs correctly
                if (commentStartIdx > 0) {
                    // between the # and the comma, there must be one space (0x20)
                    while (commentStartIdx+1 < line.length() && line.codePointAt(commentStartIdx+1) <= 0x20)
                        line.deleteCharAt(commentStartIdx+1);
                    line.insert(commentStartIdx+1, " ");
                    final int TARGET_CHARS = 5*8;
                    // count how many spaces to insert; tabs count more
                    int charsBeforeComment = 0;
                    for (int i = 0; i < commentStartIdx; ++i) {
                        if (line.charAt(i) == '\t')
                            charsBeforeComment += tabSize - (charsBeforeComment % tabSize); // jump to next tab step
                        else
                            charsBeforeComment += 1;
                    }
                    int toInsert = TARGET_CHARS - charsBeforeComment;
                    do {
                        line.insert(commentStartIdx, "\t");
                        toInsert -= tabSize;
                    } while (toInsert > 0);
                }
                // add line of the line to the output, e.g. comments, ...
                output.append(line.toString());
                output.append("\n");
            }
            mf.setEditorText(output.toString());
        } catch (IOException e) {
            DialogWrapper.showErrorDialog(mf, "IOException during reformatting: " + e.getMessage(),
                    "Reformatting failed");
        } catch (TokenizerException e) {
            if (e.getLine() != -1)
            {
                mf.colorEditorLine(e.getLine());
            }
            DialogWrapper.showErrorDialog(mf, e.toString(), "Reformatting failed");
            EditorFrame.getInstance(mf).removeColorHighlights();
            EditorFrame.getInstance(mf).selectLine(e.getLine());
        }
    }
}
