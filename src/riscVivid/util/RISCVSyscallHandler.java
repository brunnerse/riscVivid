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
package riscVivid.util;



import org.apache.log4j.Logger;

import riscVivid.RegisterSet;
import riscVivid.asm.instruction.Registers;
import riscVivid.datatypes.*;
import riscVivid.exception.MemoryException;
import riscVivid.gui.dialog.Input;
import riscVivid.gui.util.DialogWrapper;
import riscVivid.memory.DataMemory;
import riscVivid.memory.MainMemory;

/* Very dirty implementation of syscalls: 
 * In the Decode Stage setSyscall() is set to the arguments and these are read
 * when doSyscall() is called from ALU
 */ 	

public class RISCVSyscallHandler {
	
    private final uint8 A0 = new uint8(Registers.instance().getInteger("a0"));
    private final uint8 A1 = new uint8(Registers.instance().getInteger("a1"));
    private final uint8 A2 = new uint8(Registers.instance().getInteger("a2"));
    private final uint8 A7 = new uint8(Registers.instance().getInteger("a7"));
    
	private static Logger logger = Logger.getLogger("SCALL");
	private static final RISCVSyscallHandler instance = new RISCVSyscallHandler();
	private TrapObservable oOutput = null;
	private TrapObservable oInput = null;
	private Input input = null;
	private DataMemory mem=null;

	private int lastExitCode = 0;
	
	
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

	public void setMemory(DataMemory dataMem)
	{
		mem = dataMem;
	}

	public int getLastExitCode() {
		return this.lastExitCode;
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
	
	/**
	 * @param reg_set fromwhere the Syscall reads and writes needed registers
	 * @return true if exit syscall was done, otherwise false
	 */
	public boolean doSyscall(RegisterSet reg_set)
	{
		int addr;
		int len;
		int i;
		
		int a0, a1, a2, a3;
		int a7 = reg_set.read(A7, true).getValue();
		
		try {
		switch(a7)
		{
		case 63: // read (fd, buf, len)
		    a0 = reg_set.read(A0, true).getValue();
		    a1 = reg_set.read(A1, true).getValue();
		    a2 = reg_set.read(A2, true).getValue();
		    
			if (a0 != 0) {
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
				reg_set.write(A0, new uint32(len));
			}
			if (!mem.isReserved(new uint32(addr), len)) {
				// find first unreserved byte
				int unreservedAddr = addr;
				while (mem.isReserved(new uint32(unreservedAddr), 1))
					unreservedAddr++;
				DialogWrapper.showWarningDialog("The user input is " + (unreservedAddr > addr ? "partially " : "") +
						"written into unreserved memory at " + new uint32(unreservedAddr).getValueAsHexString(),
						"Input into unreserved memory region");
			}
			break;
		
		case 64: // write(fd, buf, len)
			a0 = reg_set.read(A0, true).getValue();
            a1 = reg_set.read(A1, true).getValue();
            a2 = reg_set.read(A2, true).getValue();

			if (a0 != 1) {
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
		    a0 = reg_set.read(A0, true).getValue();
		    this.lastExitCode = a0;
			logger.info("Exit program with exit code " + a0);
			return true;
		default:
			logger.warn("Unknown Syscall: " + a7);
		}
		} catch (MemoryException e) {
		}
		return false;
	}

	
}
