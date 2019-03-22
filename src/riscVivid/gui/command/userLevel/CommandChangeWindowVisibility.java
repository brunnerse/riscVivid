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
package riscVivid.gui.command.userLevel;

import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;


public class CommandChangeWindowVisibility implements Command
{

    private Class<? extends JInternalFrame> internalFrameClass;
    private MainFrame mf;

    public CommandChangeWindowVisibility(Class<? extends JInternalFrame> internalFrameClass, MainFrame mf)
    {
        this.internalFrameClass = internalFrameClass;
        this.mf = mf;
    }

    @Override
    public void execute()
    {
        for (JInternalFrame internalFrame : mf.getinternalFrames())
        {
            if (internalFrame.getClass().equals(internalFrameClass))
            {
                if (internalFrame.isIcon())
                {
                    try {
                        internalFrame.setIcon(false);
                    } catch (PropertyVetoException e) {
                        e.printStackTrace();
                    }
                }
                if (internalFrame.isClosed() || !internalFrame.isVisible() ||
                        !internalFrame.isEnabled())
                {
                    internalFrame.setVisible(true);
                }
                internalFrame.moveToFront();

                try
                {
                    internalFrame.setSelected(true);
                }
                catch (PropertyVetoException e)
                {
                    e.printStackTrace();
                }

                /* // if users closes or opens frame - should it be a preference automatically ?
                 new FrameConfiguration(internalFrame).saveFrameConfiguration();*/
                break;
            }
        }
    }

}
