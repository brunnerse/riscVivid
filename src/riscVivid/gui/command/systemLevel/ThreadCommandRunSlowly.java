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
package riscVivid.gui.command.systemLevel;

import java.awt.EventQueue;

import riscVivid.RiscVividSimulator;
import riscVivid.exception.PipelineException;
import riscVivid.gui.GUI_CONST;
import riscVivid.gui.MainFrame;
import riscVivid.gui.dialog.Player;

public class ThreadCommandRunSlowly implements Runnable
{

    private MainFrame mf;
    Player player;

    public ThreadCommandRunSlowly(MainFrame mf, Player player)
    {
        this.mf = mf;
        this.player = player;
    }

    @Override
    public void run()
    {
        RiscVividSimulator sim = mf.getOpenDLXSim();
        //start running
        mf.setOpenDLXSimState(GUI_CONST.OpenDLXSimState.RUNNING);
        //check if running was paused/quit or if openDLXSim has finished
        while (!sim.isFinished() && mf.isRunning())
        {
            if (player.isPaused())
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {}

                continue; // check again if simulator is finished and mf is running instead of doing a step
            }

            // do a cycle within riscVivid
            try
            {
                sim.step();
            }
            catch (PipelineException e)
            {
                mf.getPipelineExceptionHandler().handlePipelineExceptions(e);
            }

            try
            {
                // queue CommandUpdateFrames/execute() to event dispatch thread
                EventQueue.invokeAndWait(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //update frames
                        new CommandUpdateFrames(mf).execute();
                    }

                });
                // wait a certain amount of time, which is defined within MainFrame
                Thread.sleep(player.getRunSpeed() * 100);
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
                e.printStackTrace();
            }

        }
        // when running stops or riscVivid has finished, set state back to executing, as executing means a program is loaded but not running
        mf.setOpenDLXSimState(GUI_CONST.OpenDLXSimState.EXECUTING);
        if (sim.isFinished()) {
            player.dispose();
            new CommandSimulatorFinishedInfo().execute();
        }
    }

}
