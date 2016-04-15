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
package riscVivid.datatypes;

import riscVivid.PipelineConstants;

public class FetchDecodeData
{
	private uint32 instr;
	private uint32 pc;

	public FetchDecodeData()
	{
		flush();
	}

	public FetchDecodeData(uint32 instr, uint32 pc)
	{
		this.instr = instr;
		this.pc = pc;
	}

	public uint32 getInstr()
	{
		return instr;
	}

	public uint32 getPc()
	{
		return pc;
	}

	public void flush()
	{
		instr = PipelineConstants.PIPELINE_BUBBLE_INSTR;
		pc = PipelineConstants.PIPELINE_BUBBLE_ADDR;
	}
}
