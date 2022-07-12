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
package riscVivid;

import java.util.Queue;


import org.apache.log4j.Logger;

import riscVivid.asm.instruction.Registers;
import riscVivid.datatypes.*;
import riscVivid.exception.PipelineException;
import riscVivid.exception.UninitializedRegisterException;
import riscVivid.util.RISCVSyscallHandler;
import riscVivid.util.Statistics;

public class WriteBack
{
	private static Logger logger = Logger.getLogger("WRITEBACK");
	private Statistics stat = Statistics.getInstance();
	private RegisterSet reg_set;
	private Queue<MemoryWritebackData> memory_writeback_latch;

	public WriteBack(RegisterSet reg_set)
	{
		this.reg_set = reg_set;
	}

	public void setInputLatch(Queue<MemoryWritebackData> memoryWritebackLatch)
	{
		memory_writeback_latch = memoryWritebackLatch;
	}

	public WritebackOutputData doCycle()
	{
		MemoryWritebackData mwd = memory_writeback_latch.element();
		uint32[] alu_out = mwd.getAluOut();
		uint32 alu_outLO = alu_out[0];
		uint32 alu_outHI = alu_out[1];
		uint32 ld_result = mwd.getLdResult();
		Instruction inst = mwd.getInst();
		uint32 pc = mwd.getPc();
		boolean jump = mwd.getJump();
		boolean caught_break = false;
		boolean interrupt_occured = false;

/*
		if((ArchCfg.getISAType() == ISAType.MIPS) && (inst.getOpNormal() == OpcodeNORMAL.SPECIAL) && (inst.getOpSpecial() == OpcodeSPECIAL.BREAK))
		{
			logger.info("Caught BREAK instruction - finishing simulation.");
			caught_break = true;
		}
		if((ArchCfg.getISAType() == ISAType.DLX) && (inst.getOpNormal() == OpcodeNORMAL.SPECIAL) && (inst.getOpSpecial() == OpcodeSPECIAL.TRAP) && (alu_outLO.getValue() == PipelineConstants.DLX_TRAP_STOP))
		{
			logger.info("Caught TRAP 0 - finishing simulation.");
			caught_break = true;
		}
*/

		
		boolean regWrite = false;
		uint8 regWriteSelect = new uint8(0);
		uint32 regWriteValue = new uint32(0);

//		logger.info("PC: " + pc.getValueAsHexString());
		if (inst.getLoad())
		{
			// write load result into the register
			regWriteValue = ld_result;
		}
		else if(inst.getBranchAndLink())
		{
			// write return address into the return address register (RA, reg 31)
			// for MIPS/DLX +8 is correct, for RISCV +4
			// Completely replace the ALU results, otherwise forwarding won't
			// work for the first instruction after the two branch delay slots 
			regWriteValue = alu_outLO = alu_out[0] = new uint32(pc.getValue() + 4);
		}
		else
		{
			// normal register write
			regWriteValue = alu_outLO;
		}

		if (inst.getWriteRd())
		{
			// write the register RD
			// if for a non-BranchAndLink instruction or if the BranchAndLink instruction jumps
			// Is this condition necessary? (in other words: if BAL but no jump then don't write)
			if((!inst.getBranchAndLink()) || (inst.getBranchAndLink() && jump))
			{
				regWrite = true;
				regWriteSelect = inst.getRd();
			}
		}
		else if (inst.getWriteRt())
		{
			// write the register RD (only for non branches)
			regWrite = true;
			regWriteSelect = inst.getRt();
		}

		if (regWrite)
		{
			if(regWriteSelect.getValue() != 0)
			{
				logger.debug("writing: " + regWriteValue.getValueAsHexString() + " to register " + regWriteSelect.getValue() + "/" + ArchCfg.getRegisterDescription(regWriteSelect.getValue()));
				reg_set.write(regWriteSelect, regWriteValue);
			}
			else
			{
				logger.debug("suppressing writing of register 0/" + ArchCfg.getRegisterDescription(0) + " with value: " + regWriteValue.getValueAsHexString());
			}
		}

		if (inst.getWriteLO() || inst.getWriteHI())
		{
			regWrite = true;
			if (inst.getWriteLO())
			{
				logger.debug("writing: " + alu_outLO.getValueAsHexString() + " to register " + SpecialRegisters.LO);
				reg_set.write_SP(SpecialRegisters.LO, alu_outLO);
			}

			if (inst.getWriteHI())
			{
				logger.debug("writing: " + alu_outHI.getValueAsHexString() + " to register " + SpecialRegisters.HI);
				reg_set.write_SP(SpecialRegisters.HI, alu_outHI);
			}
		}

		PipelineException wbException = null;
		
		if((inst.getOpNormal() == OpcodeNORMAL.SPECIAL) && (inst.getOpSpecial() == OpcodeSPECIAL.BREAK)) {
			logger.info("Caught SBREAK instruction - finishing simulation.");
			caught_break = true;
		} else if(isSyscall(inst)) {
			interrupt_occured = true;
			caught_break = RISCVSyscallHandler.getInstance().doSyscall(reg_set);
			uint8 regNotInitialized = reg_set.popLastRegisterReadUninitialized();
			if (regNotInitialized != null)
				wbException = new UninitializedRegisterException(regNotInitialized, pc);
		}
		
		if (regWrite)
		{
			reg_set.printContent();
		}
		
		// count all instructions that are no NOP
		// TODO: This only works, because the write back currently cannot be stalled
		if((inst.getOpNormal() != OpcodeNORMAL.NOP) && (inst.getInstr() != PipelineConstants.PIPELINE_BUBBLE_INSTR))
		{
			stat.countInstruction();
		}
		
		WriteBackData wbd = new WriteBackData(inst, pc, alu_out, ld_result);
		
		return new WritebackOutputData(wbd, caught_break, interrupt_occured, wbException);

	}
	
	public boolean isSyscall(Instruction inst) {
		return inst.getOpNormal() == OpcodeNORMAL.SPECIAL && inst.getOpSpecial() == OpcodeSPECIAL.SYSCALL;
	}

}
