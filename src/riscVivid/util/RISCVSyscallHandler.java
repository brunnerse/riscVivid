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
package riscVivid.util;



import org.apache.log4j.Logger;

import riscVivid.datatypes.*;
import riscVivid.exception.MemoryException;
import riscVivid.gui.dialog.Input;
import riscVivid.memory.MainMemory;

/* Very dirty implementation of syscalls: 
 * In the Decode Stage setSyscall() is set to the arguments and these are read
 * when doSyscall() is called from ALU
 */ 	

public class RISCVSyscallHandler {
	
	private static Logger logger = Logger.getLogger("SCALL");
	private static final RISCVSyscallHandler instance = new RISCVSyscallHandler();
	private TrapObservable oOutput = null;
	private TrapObservable oInput = null;
	private Input input = null;
	private MainMemory mem=null;
	
	
	
	private RISCVSyscallHandler()
	{
	}
	
	public static RISCVSyscallHandler getInstance()
	{
		return instance;
	}
	
	public void setTrapObserverOutput(TrapObservable to)
	{
		oOutput = to;
	}

	public void setTrapObserverInput(TrapObservable to)
	{
		oInput = to;
	}

	public void setInput(Input input) 
	{
		this.input = input;
	}

	public void setMemory(MainMemory mainMem) 
	{
		mem = mainMem;
	}

	private String stringFromMemory(int addr, int len)
	{
		byte s[] = new byte[len];
		int i;
		try {
			for (i=0; i<len; i++) {
				s[i] = mem.read_u8(new uint32(addr+i), false).getValue();
			}
		} catch (MemoryException e) {
		}
		return new String(s);
	}

	public boolean checkExit(int a7)
	{
		return (a7==93);
	}
	
	public int doSyscall(int a7, int a0, int a1, int a2, int a3)
	{
		int addr;
		int len;
		int i;
		
		try {
		switch(a7)
		{
		case 63: // read (fd, buf, len)
			if (a0!=0) {
				logger.warn("Syscall 63: Reading only supported from stdin (a0==0)");
				break;
			}

			String user_input = null;
			if((oInput != null) && (input != null)) {
				oInput.notifyObservers("Input string...");
				user_input = input.getInput();
			}
			
			addr = a1;
			len = a2;
			if(user_input != null) {
				System.out.println("Input: \"" + user_input + "\"");
				logger.info("Input: \"" + user_input + "\" @" + String.format("0x%08x", addr));
				byte[] raw = user_input.getBytes();
				i = raw.length;
				if (i<len) len=i;
				for(i=0; i<len; i++)
					mem.write_u8(new uint32(addr+i), new uint8(raw[i]));
				return len;
			}
			break;
		
		case 64: // write(fd, buf, len)
			if (a0!=1) {
				logger.warn("Syscall 64: Writing only supported to stdout (a0==1)");
				break;
			}
			
			String msg = stringFromMemory(a1, a2);
			System.out.println(msg);
			logger.info("Printf out: \"" + msg + "\" from address "+ String.format("0x%08x", a1));
			
			if (oOutput != null)
				oOutput.notifyObservers(msg);
			break;
			
		case 93: // exit
			logger.info("Exit programm with exit code " + a0);
			break;

		default:
			logger.warn("Unknown Syscall: " + a7);
		}
		} catch (MemoryException e) {
		}
		return 0;
	}

	
}
