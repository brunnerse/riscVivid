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

import java.util.Properties;
import java.util.Queue;

import org.apache.log4j.Logger;

import riscVivid.branchPrediction.BranchTargetBuffer;
import riscVivid.datatypes.ArchCfg;
import riscVivid.datatypes.BranchPredictionModuleExecuteData;
import riscVivid.datatypes.BranchPredictionModuleFetchData;
import riscVivid.datatypes.BranchPredictionModuleOutputData;
import riscVivid.datatypes.BranchPredictorState;
import riscVivid.datatypes.BranchPredictorType;
import riscVivid.datatypes.BranchTargetBufferLookupResult;
import riscVivid.datatypes.ExecuteBranchPredictionData;
import riscVivid.datatypes.FetchDecodeData;
import riscVivid.datatypes.Instruction;
import riscVivid.datatypes.uint32;
import riscVivid.exception.BranchPredictionException;
import riscVivid.exception.PipelineException;
import riscVivid.util.Statistics;

import static riscVivid.datatypes.BranchPredictorType.UNKNOWN;

/**
 * @brief Module to encapsulate the branch predictor in the pipeline
 */
public class BranchPredictionModule
{
	/// Logging facility
	private static Logger logger = Logger.getLogger("BP_MODULE");
	/// The actual branch target buffer with branch predictors 
	private BranchTargetBuffer btb;
	/// Central module for simulation statistics 
	private Statistics stat;
	/// Input latch for the branch predictor module (table update part)
	private Queue<ExecuteBranchPredictionData> execute_branchprediction_latch;
	/// Input latch for the branch predictor module (table lookup part)
	private Queue<FetchDecodeData> fetch_branchprediction_latch;

	/**
	 * @brief Constructor
	 * @param config Configuration object, containing the branch predictor configuration.
	 * Currently the configuration entries:\n
	 * - btb_size - determine the size of the branch target buffer
	 * - btb_predictor - set the used predictor type (BranchPredictorType)
	 * - btb_predictor_initial_state - defines the initial state of the predictors (BranchPredictorState)
	 * - btb_predictor_reset_on_overwrite - defines the behavior on overwriting a branch target buffer entry by another branch (if true, which is not recommended, the predictor is reset to the initial state on branch target buffer miss)\n
	 * are supported.
	 * \sa BranchPredictorType, BranchPredictorState
	 * @throws PipelineException 
	 */
	public BranchPredictionModule(Properties config) throws PipelineException
	{
		// obtain settings for the BTB
		int btb_size = ArchCfg.getBranchPredictorTableSize();

		// get the predictor type, default value is S_ALWAYS_NOT_TAKEN
		BranchPredictorType btb_predictor = BranchPredictorType.S_ALWAYS_NOT_TAKEN;
		if (ArchCfg.getBranchPredictorType() != UNKNOWN)
			btb_predictor = ArchCfg.getBranchPredictorType();

		// get the predictor initial state, default value is PREDICT_NOT_TAKEN
		// Notice each predictor may have a different set of supported predictor states. 
		BranchPredictorState btb_predictor_initial_state = BranchPredictorState.PREDICT_NOT_TAKEN;
		if (ArchCfg.getBranchPredictorInitialState() != BranchPredictorState.UNKNOWN)
			btb_predictor_initial_state = ArchCfg.getBranchPredictorInitialState();

		// get the behavior if a btb entry is overwritten, the recommended default is false 
		boolean btb_predictor_reset_on_overwrite = false;
		if(config.getProperty("btb_predictor_reset_on_overwrite")!=null)
		{
			if(Integer.decode(config.getProperty("btb_predictor_reset_on_overwrite"))==0)
			{
				btb_predictor_reset_on_overwrite = false;
			}
			else
			{
				btb_predictor_reset_on_overwrite = true;
			}
		}
		
		btb = new BranchTargetBuffer(btb_size, btb_predictor, btb_predictor_initial_state, btb_predictor_reset_on_overwrite);
		
		// get statistics object and set btb config
		stat = Statistics.getInstance();
		stat.setBTBConfig(btb_size, btb_predictor);
	}

	/**
	 * Sets the input latch for the synchronous operation of the branch prediction module
	 * @param executeBranchpredictionLatch The input latch containing all necessary information for the update part of the branch prediction (altering the predictors) 
	 * @param fetchBranchPredictionLatch The input latch containing the necessary information for the lookup part of the branch prediction (predicting jumps)
	 */
	public void setInputLatches(Queue<ExecuteBranchPredictionData> executeBranchpredictionLatch, Queue<FetchDecodeData> fetchBranchPredictionLatch)
	{
		execute_branchprediction_latch = executeBranchpredictionLatch;
		fetch_branchprediction_latch = fetchBranchPredictionLatch;
	}

	public BranchPredictionModuleOutputData doCycle() throws BranchPredictionException
	{

		// lookup for jump target
		BranchPredictionModuleOutputData bpmod = lookupTables();
		
		// update prediction tables
		updateTables();
		
		return bpmod;
	}
	
	/**
	 * The synchronous operation of the branch prediction module
	 * @throws BranchPredictionException 
	 */
	public void updateTables() throws BranchPredictionException
	{
		ExecuteBranchPredictionData ebd = execute_branchprediction_latch.element();
		Instruction inst = ebd.getInst();
		uint32 branch_pc = ebd.getBranchPc();
		uint32 branch_tgt = ebd.getBranchTgt();
		boolean jump = ebd.getJumpTaken();
		
		if(inst.getBranch())
		{
			logger.info("Jump from " + branch_pc.getValueAsHexString() + " to " + branch_tgt.getValueAsHexString() + " that is |" + ((jump)?("taken"):("not taken")) + "| was predicted: |" + ((btb.checkPrediction(branch_pc, branch_tgt, jump)?("correctly"):("not correctly"))) + "| BTB said: |" + btb.lookupBranch(branch_pc) + "| BTB entry: |" + btb.getIndexForBranchPc(branch_pc) + "| predictor state: |" + btb.getPredictorState(branch_pc) + "|");
			stat.countBranchInformation(branch_pc, btb.getIndexForBranchPc(branch_pc), branch_tgt, jump, btb.lookupBranch(branch_pc), btb.checkPrediction(branch_pc, branch_tgt, jump));
			stat.countPredictions(btb.checkPrediction(branch_pc, branch_tgt, jump));
			stat.countBTBAccesses(btb.lookupBranch(branch_pc));
			btb.updateOnBranch(branch_pc, branch_tgt, jump);
		}
	}
	
	public BranchPredictionModuleOutputData lookupTables()
	{
		FetchDecodeData fdd = fetch_branchprediction_latch.element();
		
		uint32 pc = fdd.getPc();
		boolean do_speculative_jump = false;
		uint32 branch_tgt = new uint32(0);
		
		BranchTargetBufferLookupResult result = btb.lookupBranch(pc);
		
		if(result == BranchTargetBufferLookupResult.HIT_PREDICT_TAKEN)
		{
			do_speculative_jump = true;
			branch_tgt = btb.getBranchTarget(pc);
		}
		
		if((result == BranchTargetBufferLookupResult.HIT_PREDICT_NOT_TAKEN) || (result == BranchTargetBufferLookupResult.HIT_PREDICT_TAKEN))
		{
			logger.debug("instruction at: " + pc.getValueAsHexString() + " found in BTB and is predicted as " + ((do_speculative_jump)?("taken to addr: " + branch_tgt.getValueAsHexString()):("not taken")));
		}
		else if(result == BranchTargetBufferLookupResult.MISS)
		{
			logger.debug("instruction at: " + pc.getValueAsHexString() + " was not found in BTB");
		}
		
		BranchPredictionModuleFetchData bpmfd = new BranchPredictionModuleFetchData(do_speculative_jump, pc, branch_tgt);
		BranchPredictionModuleExecuteData bpmed = new BranchPredictionModuleExecuteData(do_speculative_jump, pc, branch_tgt);
		return new BranchPredictionModuleOutputData(bpmfd, bpmed);
	}

}
