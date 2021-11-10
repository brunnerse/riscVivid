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
import riscVivid.datatypes.uint32;
import riscVivid.exception.DecodeStageException;
import riscVivid.exception.PipelineException;
import riscVivid.exception.UnknownInstructionException;
import riscVivid.gui.GUI_CONST.OpenDLXSimState;
import riscVivid.gui.MainFrame;
import riscVivid.gui.command.systemLevel.CommandUpdateFrames;
import riscVivid.gui.internalframes.concreteframes.ClockCycleFrame;
import riscVivid.gui.internalframes.concreteframes.CodeFrame;
import riscVivid.gui.internalframes.concreteframes.editor.EditorFrame;
import riscVivid.util.BreakpointManager;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

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
			DialogWrapper.showErrorDialog(mf, e.getMessage(), "Unsupported Instruction Error");
		} else if (type == DecodeStageException.class) {
			e.printStackTrace();
			DialogWrapper.showErrorDialog(mf, e.getMessage(), "Decode Stage Error");
		} else {
			int line = -1;
			if (e.getInstructionAddress() != null)
				line = BreakpointManager.getInstance().getCorrespondingLine(e.getInstructionAddress());
			if (line >= 0)
				EditorFrame.getInstance(mf).colorLine(line-1);
			if (e.isFatal()) {
			    e.printStackTrace();
		         DialogWrapper.showErrorDialog(mf, e.getMessage(), "General Pipeline Error");
			} else {
			    DialogWrapper.showWarningDialog(e.getMessage(), "Simulator paused");
			}
			EditorFrame.getInstance(mf).removeColorHighlights();
			EditorFrame.getInstance(mf).selectLine(line-1);
		}
		if (e.isFatal()) {
		    sim.stopSimulation(true);
			// set mf state to executing (if a file has been assembled before, so the state was anything but IDLE)
			// TODO: better set to an error state?
			if (mf.getOpenDLXSimState() != OpenDLXSimState.IDLE)
				mf.setOpenDLXSimState(OpenDLXSimState.EXECUTING);
		}
		if (e.getInstructionAddress() != null) {
			final uint32 addr = e.getInstructionAddress();
			for (JInternalFrame frame : mf.getinternalFrames()) {
				if (frame instanceof ClockCycleFrame) {
				    final ClockCycleFrame ccf = (ClockCycleFrame) frame;
					try {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								ccf.update();
								ccf.selectLine(addr);
							}
						});
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else if (frame instanceof CodeFrame) {
					((CodeFrame)frame).selectLine(addr);
				}
			}
		}
	}



	public void setSimulator(RiscVividSimulator sim) 
	{
		this.sim = sim;
	}
}
