/*******************************************************************************
 * riscVivid - A RISC-V processor simulator.
 * (C)opyright 2013-2016 The riscVivid project, University of Augsburg, Germany
 * https://github.com/unia-sik/riscVivid
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, see <LICENSE>. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package riscVivid.gui.dialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;

import riscVivid.gui.GUI_CONST.OpenDLXSimState;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;

@SuppressWarnings("serial")
public class Player extends JDialog implements ActionListener, KeyEventDispatcher
{

    private static final int RUN_SPEED_DEFAULT = 16;
    private int runSpeed = RUN_SPEED_DEFAULT;
    private boolean isPaused = false;

    private JButton play, pause, stop, times1, times2, times4, times8, times16;

    public Player(JFrame f)
    {
        super(f, false);
        setLayout(new FlowLayout());
        setTitle("OpenDLXSimulator run");

        play = new JButton("Run");
        play.addActionListener(this);
        add(play);
        play.setEnabled(false);
        pause = new JButton("Pause");
        pause.addActionListener(this);
        add(pause);
        stop = new JButton("Stop");
        stop.addActionListener(this);
        add(stop);
        times1 = new JButton("1x");
        times1.addActionListener(this);
        add(times1);
        times2 = new JButton("2x");
        times2.addActionListener(this);
        add(times2);
        times4 = new JButton("4x");
        times4.addActionListener(this);
        add(times4);
        times8 = new JButton("8x");
        times8.addActionListener(this);
        add(times8);
        times16 = new JButton("16x");
        times16.addActionListener(this);
        add(times16);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
        this.enableAllTimesButtonsBut(times1);  // times1 is default pressed

        for (JComponent c : new JComponent[] {
                play, pause, stop, times1, times2, times4, times8, times16
        })
           c.setFont(c.getFont().deriveFont((float)Preference.getFontSize()));

        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(f);

        pack();
        setVisible(true);
    }

    @Override
    public void dispose() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
        MainFrame.getInstance().setOpenDLXSimState(OpenDLXSimState.EXECUTING);
        isPaused = false;
        setVisible(false);
        this.runSpeed = RUN_SPEED_DEFAULT;
        super.dispose();
    }

    public int getRunSpeed() {
        return this.runSpeed;
    }
   public boolean isPaused() {
        return this.isPaused;
   }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == play)
        {
            pause.setEnabled(true);
            play.setEnabled(false);
            isPaused = false;
        }
        else if (e.getSource() == pause)
        {
            play.setEnabled(true);
            pause.setEnabled(true);
            isPaused = true;
            pause.setEnabled(false);
        }
        else if (e.getSource() == stop)
        {
            dispose();
        }
        else if (e.getSource() == times1)
        {
            this.runSpeed = RUN_SPEED_DEFAULT;
            this.enableAllTimesButtonsBut((JButton)e.getSource());
        }
        else if (e.getSource() == times2)
        {
            this.runSpeed = RUN_SPEED_DEFAULT / 2;
            this.enableAllTimesButtonsBut((JButton)e.getSource());
        }
        else if (e.getSource() == times4)
        {
            this.runSpeed = RUN_SPEED_DEFAULT / 4;
            this.enableAllTimesButtonsBut((JButton)e.getSource());
        }
        else if (e.getSource() == times8)
        {
            this.runSpeed = RUN_SPEED_DEFAULT / 8;
            this.enableAllTimesButtonsBut((JButton)e.getSource());
        }
        else if (e.getSource() == times16)
        {
            this.runSpeed = RUN_SPEED_DEFAULT / 16;
            this.enableAllTimesButtonsBut((JButton)e.getSource());
        }
    }

    private void enableAllTimesButtonsBut(JButton times) {
        for (JButton b : new JButton[] {times1, times2, times4, times8, times16}) {
            if (b != times)
                b.setEnabled(true);
            else
                b.setEnabled(false);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
           if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
               dispose();
               return true;
           }
        }
        return false;
    }
}
