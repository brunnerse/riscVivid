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

import java.util.Properties;

import riscVivid.datatypes.CacheReplacementPolicy;
import riscVivid.datatypes.CacheType;
import riscVivid.datatypes.DCacheWritePolicy;
import riscVivid.datatypes.RequestType;
import riscVivid.datatypes.uint32;
import riscVivid.exception.CacheException;
import riscVivid.exception.MemoryException;
import riscVivid.exception.PipelineDataTypeException;
import riscVivid.util.Statistics;

/**
 * Encapsulates instruction memory hierarchy.
 */
public class InstructionMemory
{
	private MemoryInterface mem;	
	
	private Statistics stat = Statistics.getInstance();

	
	public InstructionMemory(MainMemory mem, Properties config) throws MemoryException, PipelineDataTypeException
	{
		boolean useIcache;
		
		if(Integer.decode(config.getProperty("icache_use"))==0)
		{
			useIcache = false;
		}
		else
		{
			useIcache = true;
		}

		if(useIcache == false)
		{
			this.mem = mem;
		}
		else
		{

			int lineSize = 8;
			if(config.getProperty("icache_line_size")!=null)
			{
				lineSize = Integer.decode(config.getProperty("icache_line_size"));
			}

			int lineNo = 32;
			if(config.getProperty("icache_line_number")!=null)
			{
				lineNo = Integer.decode(config.getProperty("icache_line_number"));
			}

			int associativity = 1;
			if(config.getProperty("icache_associativity")!=null)
			{
				associativity = Integer.decode(config.getProperty("icache_associativity"));
			}

			CacheReplacementPolicy rpol = CacheReplacementPolicy.UNKNOWN;
			if(config.getProperty("icache_replacement_policy")!=null)
			{
				rpol = Cache.getCacheReplacementPolicyFromString(config.getProperty("icache_replacement_policy"));
			}

			if(associativity == 1)
			{
				this.mem = new CacheDirectMapped(CacheType.ICACHE, lineSize, lineNo, associativity, mem);
				if((rpol != CacheReplacementPolicy.DIRECT_MAPPED) && (rpol != CacheReplacementPolicy.UNKNOWN))
				{
					throw new CacheException("Wrong replacement policy for cache with associativity of 1. Replacement policy: " + rpol);
				}
				rpol = CacheReplacementPolicy.DIRECT_MAPPED;
				
			}
			else
			{
				switch(rpol)
				{
				case FIFO:
					this.mem = new CacheFIFO(CacheType.ICACHE, lineSize, lineNo, associativity, mem);
					break;
				case LRU:
					this.mem = new CacheLRU(CacheType.ICACHE, lineSize, lineNo, associativity, mem);
					break;
				default:
					throw new CacheException("Unknown cache replacement policy: " + rpol);
				}
			}
			stat.setCacheParameters(CacheType.ICACHE, rpol, lineSize, lineNo, associativity, DCacheWritePolicy.UNKNOWN);
		}
	}

	public int getRequestDelay(uint32 addr) throws MemoryException
	{
		return mem.getRequestDelay(RequestType.INSTR_RD, addr);
	}
	
 	public uint32 read_u32(uint32 addr) throws MemoryException
	{
		return mem.read_u32(addr, false);
	}
}
