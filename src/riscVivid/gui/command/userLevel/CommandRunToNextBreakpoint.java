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

import java.util.Queue;

import riscVivid.RiscVividSimulator;
import riscVivid.datatypes.MemoryWritebackData;
import riscVivid.datatypes.uint32;
import riscVivid.exception.PipelineException;
import riscVivid.gui.MainFrame;
import riscVivid.gui.Preference;
import riscVivid.gui.command.Command;
import riscVivid.gui.command.systemLevel.CommandSimulatorFinishedInfo;
import riscVivid.gui.command.systemLevel.CommandUpdateFrames;
import riscVivid.gui.internalframes.concreteframes.editor.EditorFrame;
import riscVivid.gui.util.DialogWrapper;
import riscVivid.util.BreakpointManager;

public class CommandRunToNextBreakpoint implements Command
{

    private MainFrame mf;
    private BreakpointManager bm;

    public CommandRunToNextBreakpoint(MainFrame mf)
    {
        this.mf = mf;
        this.bm = BreakpointManager.getInstance();
    }

    @Override
    public void execute()
    {
        if (mf.isExecuting() && mf.isUpdateAllowed())
        {
            try
            {
                RiscVividSimulator openDLXSim = mf.getOpenDLXSim();
                Queue<MemoryWritebackData> memory_writeback_latch = openDLXSim.getPipeline().getMemoryWriteBackLatch();
                
                try
                {
                    while (!openDLXSim.isFinished())
                    {
                        openDLXSim.step();

                        uint32 writebackAddr = memory_writeback_latch.element().getPc();
                        if (bm.isBreakpoint(writebackAddr)) {
                            int line = bm.getCorrespondingLine(writebackAddr);
                            EditorFrame.getInstance(mf).scrollLineToVisible(line);
                            //EditorFrame.getInstance(mf).selectLine(line);
                            break;
                        }
                    }
                }
                catch (PipelineException e)
                {
                    mf.getPipelineExceptionHandler().handlePipelineExceptions(e);
                }

                new CommandUpdateFrames(mf).execute();

                if (openDLXSim.isFinished())
                { // if the current riscVivid has finished, dont allow any gui updates any more
                    mf.setUpdateAllowed(false);
                    new CommandSimulatorFinishedInfo().execute();
                }
                
            }
            catch (Exception e)
            {
                //something else went wrong
                System.err.println(e.toString());
                e.printStackTrace();
                DialogWrapper.showErrorDialog("Executing commands failed", "Error while running to next breakpoint");
            }
        }


    }

}
