package riscVivid.gui.internalframes.concreteframes.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.internalframes.FrameConfiguration;

@SuppressWarnings("serial")
public class FindReplaceDialog extends JDialog 
    implements ActionListener, FocusListener, KeyListener {
    
    private final EditorFrame editor;
    
    private JButton find, replace, replaceAll;
    private JTextField findField, replaceField;
    private JLabel findLabel, replaceLabel;
    
    public FindReplaceDialog(MainFrame mf, EditorFrame editor) {
        super(mf, "Find/Replace");
        this.editor = editor;
        initialize(mf);
        final FrameConfiguration fc = new FrameConfiguration(this);
        fc.loadFrameConfiguration();
        pack();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                EditorFrame.getInstance(MainFrame.getInstance()).removeColorHighlights();
                fc.saveFrameConfiguration();
            }
        });
    }
    

    private void initialize(MainFrame mf) {
        JPanel textFieldPanel = new JPanel(new BorderLayout());
        findField = new JTextField();
        replaceField = new JTextField();
        findLabel = new JLabel("Find: ");
        replaceLabel = new JLabel("Replace with: ");
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(findLabel, BorderLayout.NORTH);
        labelPanel.add(replaceLabel, BorderLayout.SOUTH);
        JPanel textInputPanel = new JPanel(new BorderLayout());
        textInputPanel.add(findField, BorderLayout.NORTH);
        textInputPanel.add(replaceField, BorderLayout.SOUTH);
        
        textFieldPanel.add(labelPanel, BorderLayout.WEST);
        textFieldPanel.add(textInputPanel, BorderLayout.CENTER);
        
        for (JTextField tf : new JTextField[] {findField, replaceField}) {
            tf.addFocusListener(this);
            tf.addKeyListener(this);
        }
        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        find = new JButton("Find");
        replace = new JButton("Replace");
        replaceAll = new JButton("Replace all");
        
        for (JButton b : new JButton[]{find, replace, replaceAll}) {
            b.addActionListener(this);
            b.addKeyListener(this);
        }
        JPanel buttonReplacePanel = new JPanel(new GridLayout(1,2));
        buttonReplacePanel.add(replace);
        buttonReplacePanel.add(replaceAll);
        
        buttonPanel.add(find, BorderLayout.NORTH);
        buttonPanel.add(buttonReplacePanel, BorderLayout.SOUTH);

        
        this.add(textFieldPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        setFont(findField.getFont().deriveFont((float)Preference.getFontSize()));
        resetLocationAndSize();
        setVisible(true);
    }

    public void onFind() {
        String findStr = findField.getText();
        if (findStr.length() == 0)
            return;
        editor.removeColorHighlights();
        int startIdx = Math.max(editor.getSelectionStart() + 1, 0);
        String text = editor.getText();
        int findIdx = text.indexOf(findStr, startIdx);
        if (findIdx < 0) // end of file reached; start at the beginning again
            findIdx = text.indexOf(findStr);
        if (findIdx >= 0) {
            
            editor.selectSection(findIdx, findIdx + findStr.length());
        }
        highlightTermInEditor(findStr);
    }
    
    public void onReplace() {
        onFind();
        String findStr = findField.getText();
        String replaceStr = replaceField.getText();
        if (findStr.length() == 0 || replaceStr.length() == 0)
            return;
        String text = editor.getText();
        int startIdx = Math.max(editor.getSelectionStart() + 1, 0);
        int findIdx = text.indexOf(findStr, startIdx);
        if (findIdx < 0) // end of file reached; start at the beginning again
            findIdx = text.indexOf(findStr);
        if (findIdx >= 0) {
            text = text.substring(0, findIdx) + text.substring(findIdx).replaceFirst(findStr, replaceStr);
            editor.setText(text);
            editor.selectSection(findIdx, findIdx + replaceStr.length());
        }
    }
    
    public void onReplaceAll() {
        String findStr = findField.getText();
        String replaceStr = replaceField.getText();
        if (findStr.length() == 0 || replaceStr.length() == 0)
            return;
        String text = editor.getText();
        text = text.replace(findStr, replaceStr);
        editor.setText(text);
        highlightTermInEditor(replaceStr);
    }
    
    private void highlightTermInEditor(String term) {
        String text = editor.getText();
        int findIdx = text.indexOf(term);
        while(findIdx >= 0) {
            try {
                editor.colorSection(findIdx, findIdx + term.length(), Color.LIGHT_GRAY);
            } catch (BadLocationException e) {
                return;
            }
            findIdx = text.indexOf(term, findIdx+1);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == find) {
            onFind();
        } else if (e.getSource() == replace) {
            onReplace();
        } else if (e.getSource() == replaceAll) {
            onReplaceAll();
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (e.getSource() == findField) {
                find.doClick();
            } else if (e.getSource() == replaceField) {
                replace.doClick();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            this.dispose();
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void focusGained(FocusEvent e)
    {
        JTextField f = (JTextField) e.getSource();
        f.selectAll();
    }

    @Override
    public void focusLost(FocusEvent e)
    {
    }
    
    public void resetLocationAndSize() {
        MainFrame mf = MainFrame.getInstance();
        this.setLocation(mf.getWidth()/2 - this.getWidth()/2,
                mf.getHeight()/2 - this.getHeight()/2);
        this.setLocationRelativeTo(mf);
        pack();
    }
    
    @Override
    public void setFont(Font f) {
        super.setFont(f);
        for (Component c : new Component[]{find, replace, replaceAll,
                findField, replaceField, findLabel, replaceLabel})
            if (c != null)
                c.setFont(f);
        pack();
    }
}
