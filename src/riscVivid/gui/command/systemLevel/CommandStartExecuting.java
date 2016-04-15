/*******************************************************************************
 * riscVivid - A RISC-V processor simulator.
 * Copyright (C) 2013-2016 The riscVivid project, University of Augsburg, Germany
 * <https://github.com/unia-sik/riscVivid>
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
package riscVivid.gui.command.systemLevel;

import java.io.File;
import javax.swing.JOptionPane;

import riscVivid.gui.MainFrame;
import riscVivid.gui.command.Command;

public class CommandStartExecuting implements Command
{

    private File configFile; //in
    private MainFrame mf;
    private String[] intFrameOrder;

    public CommandStartExecuting(MainFrame mf, File f, String[] intFrameOrder)
    {
        this.mf = mf;
        this.configFile = f;
        this.intFrameOrder = intFrameOrder;
    }
    
    public CommandStartExecuting(MainFrame mf, File f)
    {
        this(mf, f, new String[]{});
    }

    @Override
    public void execute()
    {
        // call the riscVivid constructor and assign a configFile to the new riscVivid
        new CommandCreateOpenDLXSim(mf, configFile).execute();
        if (mf.getOpenDLXSim() != null)
        {   //create all executing-frames   
            new CommandCreateFrames(mf, intFrameOrder).execute();
        }
        else
        {
        	System.out.println("Could not initiate riscVivid simulator.");
            JOptionPane.showMessageDialog(mf, "Could not initiate riscVivid simulator.");
        }
    }

}
