package riscVivid.gui.internalframes.concreteframes.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;

@SuppressWarnings("serial")
public class FindReplaceDialog extends JDialog 
    implements ActionListener, FocusListener, KeyListener {
    
    private EditorFrame editor;
    
    private JButton find, replace, replaceAll;
    private JTextField findField, replaceField;
    private JLabel findLabel, replaceLabel;
    
    public FindReplaceDialog(MainFrame mf, EditorFrame editor) {
        super(mf, "Find/Replace");
        this.editor = editor;
        //initialize();
        pack();
        setVisible(true);
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
        System.out.println(findField.getText());
    }
    
    public void onReplace() {
        System.out.println(replaceField.getText());
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("clicked: " + e.getSource());
        if (e.getSource() == find) {
            onFind();
        } else if (e.getSource() == replace) {
            onReplace();
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
}
