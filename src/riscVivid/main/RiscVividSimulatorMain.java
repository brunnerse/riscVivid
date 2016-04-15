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
package riscVivid.main;

import riscVivid.RiscVividSimulator;
import riscVivid.config.GlobalConfig;
import riscVivid.datatypes.ArchCfg;
import riscVivid.datatypes.ISAType;
import riscVivid.exception.PipelineException;
import riscVivid.gui.*;

public class RiscVividSimulatorMain
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {

    	if(args.length == 0)
    	{
    		main_gui();
    	}
    	else
    	{

    		System.out.println("Parameters: ");
    		for (int i = 0; i < args.length; i++)
    		{
    			System.out.println(i + " " + args[i]);
    		}

    		if (args[0].compareTo("-c") == 0)
    		{
    			String[] cmd_args = new String[args.length-1];
    			for(int i = 0; i < args.length-1; i++)
    			{
    				cmd_args[i] = args[i+1];
    			}
    			main_cmd(cmd_args);
    		}
    		else if (args[0].compareTo("-g") == 0)
    		{
    			main_gui();
    		}
    		else if (args[0].compareTo("-about") == 0)
    		{
    			about();
    		}
    		else
    		{
    			usage();
    		}
    	}
    }

    static void usage()
    {
    	String blanks = "";
    	for(int i = GlobalConfig.VERSION.length(); i < 35; i++)
    	{
    		blanks +=" ";
    	}
    	
    	System.out.println("+==============================================+");
    	System.out.println("|   riscVivid - a RISC-V processor simulator   |");
    	System.out.println("|   Version "+GlobalConfig.VERSION+blanks+"|");
    	System.out.println("| Copyright (C) 2013-2016-15 University of Augsburg |");
    	System.out.println("+==============================================+");
    	System.out.println("| Usage:                                       |");
    	System.out.println("| For GUI version:                             |");
    	System.out.println("|   java -jar riscVivid.jar [-g]               |");
    	System.out.println("| For non interactive version:                 |");
    	System.out.println("|   java -jar riscVivid.jar -c config_file.cfg |");
    	System.out.println("| This help message:                           |");
    	System.out.println("|   java -jar riscVivid.jar -h                 |");
      	System.out.println("| About & license information:                 |");
    	System.out.println("|   java -jar riscVivid.jar -about             |");
    	System.out.println("+==============================================+");
		
	}
    
    static void about()
    {
    	System.out.println("====================================================================");
    	System.out.println(GlobalConfig.ABOUT);
    	System.out.println("====================================================================");
    }
    
    static void main_gui()
    {
    	// DLX is the default for graphical mode.
    	ArchCfg.isa_type = ISAType.DLX;
    	RiscVividSimGui.riscVividGui_main();
    }
    
    static void main_cmd(String[] args)
    {
    	RiscVividSimulator cmdl;
		try {
			cmdl = new RiscVividSimulator(args);
		} catch (PipelineException e) {
			e.printStackTrace();
			cmdl = null;
			System.exit(1);
			
		}
		cmdl.riscVividCmdl_main();
    }

}
