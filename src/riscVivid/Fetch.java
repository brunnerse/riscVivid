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

import java.util.Arrays;
import java.util.Queue;

import org.apache.log4j.Logger;

import riscVivid.datatypes.*;
import riscVivid.exception.MemoryException;
import riscVivid.exception.PipelineException;
import riscVivid.exception.UnreservedMemoryAccessException;
import riscVivid.gui.Preference;
import riscVivid.memory.InstructionMemory;
import riscVivid.util.Statistics;

public class Fetch {


	private static Logger logger = Logger.getLogger("FETCH");
	private Statistics stat = Statistics.getInstance();
	
	private uint32 program_counter;
	private InstructionMemory imem;
	private Queue<ExecuteFetchData> execute_fetch_latch;
	private Queue<BranchPredictionModuleFetchData> branchprediction_fetch_latch;
	private final int numBranchDelaySlots;
	
	public Fetch(uint32 init_pc, InstructionMemory imem, int numBranchDelaySlots)
	{
		program_counter = new uint32(init_pc);
		this.imem = imem;
		this.numBranchDelaySlots = numBranchDelaySlots;
	}
	
	public void setPc(uint32 pc)
	{
		program_counter.setValue(pc.getValue());
		logger.debug("Set Pc to: " + program_counter.getValueAsHexString());
	}
	
	public uint32 getPc()
	{
		return new uint32(program_counter);
	}
	
	private uint32 doFetch() throws MemoryException
	{
		uint32 instr = null;
		if(imem.getRequestDelay(program_counter)==0)
		{
			instr = imem.read_u32(program_counter);
			stat.countFetch();
		}
		return new uint32(instr);
	}
	
	public void increasePC()
	{
		program_counter.setValue(program_counter.getValue()+4);
		logger.debug("Pc is now at: " + program_counter.getValueAsHexString());
	}

	public void setInputLatches(Queue<ExecuteFetchData> executeFetchLatch, Queue<BranchPredictionModuleFetchData> branchpredictionFetchLatch)
	{
		execute_fetch_latch = executeFetchLatch;
		branchprediction_fetch_latch = branchpredictionFetchLatch;
	}

	public FetchOutputData doCycle() throws MemoryException
	{
		ExecuteFetchData efd = execute_fetch_latch.element();
		BranchPredictionModuleFetchData bpmfd = branchprediction_fetch_latch.element();
		
		boolean[] flush = new boolean[PipelineConstants.STAGES];
		Arrays.fill(flush, false);

		if(bpmfd.getDoSpeculativeJump())
		{
			logger.debug("speculatively jumping from " + bpmfd.getPc().getValueAsHexString() + " to " + bpmfd.getBranchTgt().getValueAsHexString());
			// the branch predictor predicted a branch, set the pc to the predicted target
			setPc(bpmfd.getBranchTgt());
		}

		if (efd.getMispredictedBranch()) {
			logger.debug("mispredicted branch at pc "
					+ efd.getPc().getValueAsHexString()
					+ " the branch was actually " + ((efd.getJump())
					? ("taken to " + efd.getNewPc().getValueAsHexString())
					: ("not taken next instr is " + new uint32(efd.getPc().getValue()+8).getValueAsHexString())));
			if (ArchCfg.getISAType() == ISAType.MIPS)
			{
				// according to the MIPS specification one instruction is executed in the branch delay
				// slot, this instruction will be already in the execute stage (i.e. in the latch before the execute stage).
				// Consequently the instruction in the decode stage has to be kicked out.
				flush[PipelineConstants.DECODE_STAGE] = true;
			}
			if (efd.getJump()) {
				setPc(efd.getNewPc());
			} else {
				setPc(new uint32(efd.getPc().getValue() + 4 * numBranchDelaySlots));
			}
		}

		if((efd.getInst().getBranch()) && (efd.getJump() == false) && (efd.getInst().getBranchLikely()))
		{
			logger.debug("likely branch was not taken, flushing branch delay slot");
			// for likely branches if they are not taken the instruction in the branch delay slot hat to be nullified, e.g. by flushing it.
			// notice: this is independent of the branch prediction
			flush[PipelineConstants.EXECUTE_STAGE] = true;
			if (numBranchDelaySlots > 2)
				flush[PipelineConstants.MEMORY_STAGE] = true;
		}

		// flush branch delay slot on taken branch
		if(efd.getInst().getBranch() && efd.getJump() && ArchCfg.ignoreBranchDelaySlots())
		{
			logger.debug("branch was taken, flushing branch delay slots");
			flush[PipelineConstants.DECODE_STAGE] = true;
			flush[PipelineConstants.EXECUTE_STAGE] = true;
			if (numBranchDelaySlots > 2)
				flush[PipelineConstants.MEMORY_STAGE] = true;
		}
		
		uint32 instr = doFetch();
		if(instr != null)
		{
			logger.debug("PC: " + getPc().getValueAsHexString() + " fetched instruction " + instr.getValueAsHexString());
		}
		else
		{
			// stalling
		}

		PipelineException fetchEx = null;
		if (Preference.isMemoryWarningsEnabled() && !imem.isReserved(program_counter, 4))
			fetchEx = new UnreservedMemoryAccessException(new uint32(getPc()), 4, UnreservedMemoryAccessException.Stage.FETCH, getPc());
		
		FetchDecodeData fdd = new FetchDecodeData(instr, getPc());
		
		return new FetchOutputData(fdd, flush, fetchEx);
	}
        
        

}
