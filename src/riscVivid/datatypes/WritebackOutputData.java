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
package riscVivid.datatypes;

public class WritebackOutputData
{

	private WriteBackData wbd;
	private boolean caught_break;
	private boolean interrupt_occured;
	
	public WritebackOutputData(WriteBackData wbd, boolean caught_break, boolean interrupt_occured)
	{
		this.wbd = wbd; 
		this.caught_break = caught_break;
		this.interrupt_occured = interrupt_occured;
	}

	public WriteBackData getWbd()
	{
		return wbd;
	}
	
	public boolean getCaughtBreak()
	{
		return caught_break;
	}

	public boolean getInterruptOccured()
	{
		return interrupt_occured;
	}

}
