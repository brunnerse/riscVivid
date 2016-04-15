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
package riscVivid.memory;

import riscVivid.datatypes.CacheType;
import riscVivid.datatypes.DCacheWritePolicy;
import riscVivid.datatypes.uint32;
import riscVivid.exception.CacheException;
import riscVivid.exception.PipelineDataTypeException;

public class CacheDirectMapped extends Cache 
{

	
	public CacheDirectMapped(CacheType type, int line_size, int line_no, int associativity, DCacheWritePolicy write_policy, MainMemory mem) throws CacheException, PipelineDataTypeException 
	{
		super(type, line_size, line_no, associativity, write_policy, mem);
	}
	
	public CacheDirectMapped(CacheType type, int line_size, int line_no, int associativity, MainMemory mem) throws CacheException, PipelineDataTypeException 
	{
		super(type, line_size, line_no, associativity, mem);
	}

	protected int getCacheWayForReplacement(uint32 addr) 
	{
		return 0;
	}

	protected void updateReplacementCountersOnAccess(int way, int index) 
	{
		// nothing to do
	}

	protected void updateReplacementCountersOnMiss(int way, int index) 
	{
		// nothing to do
	}

}
