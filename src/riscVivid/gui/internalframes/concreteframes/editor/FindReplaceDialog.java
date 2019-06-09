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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;

@SuppressWarnings("serial")
public class FindReplaceDialog extends JDialog 
    implements ActionListener, FocusListener, KeyListener, 
    WindowListener {
    
    private final EditorFrame editor;
    
    private JButton find, replace, replaceAll;
    private JTextField findField, replaceField;
    private JLabel findLabel, replaceLabel;
    
    public FindReplaceDialog(MainFrame mf, EditorFrame editor) {
        super(mf, "Find/Replace");
        this.editor = editor;
        initialize();
        this.addWindowListener(this);
    }
    

    private void initialize() {
        JPanel textFieldPanel = new JPanel(new GridLayout(2,2));
        findField = new JTextField("find");
        replaceField = new JTextField("replace with");
        findLabel = new JLabel("Find: ");
        replaceLabel = new JLabel("Replace with: ");

        //textFieldPanel.add(labelPanel);
        textFieldPanel.add(findLabel);
        textFieldPanel.add(findField);
        textFieldPanel.add(replaceLabel);
        textFieldPanel.add(replaceField);
        
        for (JTextField tf : new JTextField[] {findField, replaceField}) {
            tf.addFocusListener(this);
            tf.addKeyListener(this);
        }
        
        JPanel buttonPanel = new JPanel(new GridLayout(2,2));
        find = new JButton("Find");
        replace = new JButton("Replace");
        replaceAll = new JButton("Replace all");
        
        for (JButton b : new JButton[]{find, replace, replaceAll}) {
            b.addActionListener(this);
        }
        buttonPanel.add(find);
        buttonPanel.add(new JPanel());
        buttonPanel.add(replace);
        buttonPanel.add(replaceAll);
        
        this.add(textFieldPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        setFont(findField.getFont().deriveFont((float)Preference.getFontSize()));
        pack();
        setVisible(true);
    }

    public void onFind() {
        String findStr = findField.getText();
        int startIdx = editor.getSelectionStart();
        if (startIdx < 0)
            startIdx = 0;
        highlightTermInEditor(findStr);
        String text = editor.getText();
        int findIdx = text.indexOf(findStr);
        if (findIdx >= 0) {
            editor.selectSection(findIdx, findIdx + findStr.length() - 1);
        }
    }
    
    public void onReplace() {
        String text = editor.getText();
        int startSection = text.indexOf(findField.getText());
        text = text.replaceFirst(findField.getText(), replaceField.getText());
        int endSection = text.indexOf(replaceField.getText(), startSection);
        editor.setText(text);
        highlightTermInEditor(findField.getText());
        editor.selectSection(startSection,  endSection);
    }
    
    public void onReplaceAll() {
        String text = editor.getText();
        text = text.replace(findField.getText(), replaceField.getText());
        highlightTermInEditor(replaceField.getText());
    }
    
    private void highlightTermInEditor(String term) {
        String text = editor.getText();
        int findIdx = text.indexOf(term);
        while(findIdx >= 0) {
            try {
                editor.colorSection(findIdx, findIdx + term.length() - 1, Color.LIGHT_GRAY);
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
        }
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
    
    @Override
    public void setFont(Font f) {
        super.setFont(f);
        for (Component c : new Component[]{find, replace, replaceAll,
                findField, replaceField, findLabel, replaceLabel})
            if (c != null)
                c.setFont(f);
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        editor.removeColorHighlights();
    }
    @Override
    public void windowActivated(WindowEvent arg0) {
    }
    @Override
    public void windowClosing(WindowEvent arg0) {
}
    @Override
    public void windowDeactivated(WindowEvent arg0) {
    }
    @Override
    public void windowDeiconified(WindowEvent arg0) {    }
    @Override
    public void windowIconified(WindowEvent arg0) {
    }
    @Override
    public void windowOpened(WindowEvent arg0) {
    }
}
