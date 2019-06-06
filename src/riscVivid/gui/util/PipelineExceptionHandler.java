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
package riscVivid.gui.util;

import riscVivid.RiscVividSimulator;
import riscVivid.exception.DecodeStageException;
import riscVivid.exception.PipelineException;
import riscVivid.exception.UnknownInstructionException;
import riscVivid.gui.GUI_CONST.OpenDLXSimState;
import riscVivid.gui.MainFrame;

public class PipelineExceptionHandler {
	
	private MainFrame mf;
	private RiscVividSimulator sim = null;



	public PipelineExceptionHandler(MainFrame main)
	{
		mf = main;
	}
	
	 
	public void handlePipelineExceptions(PipelineException e) 
	{
		Class<? extends PipelineException> type = e.getClass();
		if (type == UnknownInstructionException.class) {
			e.printStackTrace();
			DialogWrapper.showErrorDialog(mf, e.getMessage(), "Unspported Instruction Error");
		} else if (type == DecodeStageException.class) {
			e.printStackTrace();
			DialogWrapper.showErrorDialog(mf, e.getMessage(), "Decode Stage Error");
		} else {
			e.printStackTrace();
			DialogWrapper.showErrorDialog(mf, e.getMessage(), "General Pipeline Error");
		}
		sim.stopSimulation(true);
		// TODO: better set to an error state?
		mf.setOpenDLXSimState(OpenDLXSimState.IDLE);
	}



	public void setSimulator(RiscVividSimulator sim) 
	{
		this.sim = sim;
	}
}
