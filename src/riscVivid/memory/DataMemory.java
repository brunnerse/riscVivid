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
package riscVivid.memory;

import java.util.Properties;

import riscVivid.datatypes.CacheReplacementPolicy;
import riscVivid.datatypes.CacheType;
import riscVivid.datatypes.DCacheWritePolicy;
import riscVivid.datatypes.RequestType;
import riscVivid.datatypes.uint16;
import riscVivid.datatypes.uint32;
import riscVivid.datatypes.uint64;
import riscVivid.datatypes.uint8;
import riscVivid.exception.CacheException;
import riscVivid.exception.MemoryException;
import riscVivid.exception.PipelineDataTypeException;
import riscVivid.gui.internalframes.util.ValueInput;
import riscVivid.util.Statistics;

/**
 * Encapsulates data memory hierarchy.
 */
public class DataMemory
{
	private MemoryInterface mem;
	
	private Statistics stat = Statistics.getInstance();
	private MemoryLogger reservedMemLogger = null;
	
	public DataMemory(MainMemory mem, Properties config) throws MemoryException, PipelineDataTypeException
	{

		boolean useDcache;

		if(Integer.decode(config.getProperty("dcache_use"))==0)
		{
			useDcache = false;
		}
		else
		{
			useDcache = true;
		}

		if(useDcache == false)
		{
			this.mem = mem;
		}
		else
		{

			int lineSize = 8;
			if(config.getProperty("dcache_line_size")!=null)
			{
				lineSize = Integer.decode(config.getProperty("dcache_line_size"));
			}

			int lineNo = 32;
			if(config.getProperty("dcache_line_number")!=null)
			{
				lineNo = Integer.decode(config.getProperty("dcache_line_number"));
			}

			int associativity = 1;
			if(config.getProperty("dcache_associativity")!=null)
			{
				associativity = Integer.decode(config.getProperty("dcache_associativity"));
			}

			CacheReplacementPolicy rpol = CacheReplacementPolicy.UNKNOWN;
			if(config.getProperty("dcache_replacement_policy")!=null)
			{
				rpol = Cache.getCacheReplacementPolicyFromString(config.getProperty("dcache_replacement_policy"));
			}

			DCacheWritePolicy wpol = DCacheWritePolicy.WRITE_THROUGH; // write through is the default
			if(config.getProperty("dcache_write_policy")!=null)
			{
				wpol = Cache.getCacheWritePolicyFromString(config.getProperty("dcache_write_policy"));
			}

			if(associativity == 1)
			{
				this.mem = new CacheDirectMapped(CacheType.DCACHE, lineSize, lineNo, associativity, wpol, mem);
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
					this.mem = new CacheFIFO(CacheType.DCACHE, lineSize, lineNo, associativity, wpol, mem);
					break;
				case LRU:
					this.mem = new CacheLRU(CacheType.DCACHE, lineSize, lineNo, associativity, wpol, mem);
					break;
				default:
					throw new CacheException("Unknown cache replacement policy: " + rpol);
				}
			}
			stat.setCacheParameters(CacheType.DCACHE, rpol, lineSize, lineNo, associativity, wpol);
		}
        
		// init memory log
		reservedMemLogger = new MemoryLogger();
		// read all data segments
		for (int i = 0; config.containsKey("data_begin_" + i); i++) {
		    reservedMemLogger.add(ValueInput.strToInt(config.getProperty("data_begin_"+i)),
		        ValueInput.strToInt(config.getProperty("data_end_"+i)));
		}
		System.out.println("Data segments: " + reservedMemLogger);
	}
	
	public boolean isReserved(uint32 addr, int bytes) {
	    return reservedMemLogger.checkBytes(addr, bytes);
	}
	public MemoryLogger getReservedMemLogger() {
	    return reservedMemLogger;
	}

	public int getRequestDelay(RequestType type, uint32 addr) throws MemoryException
	{
		return mem.getRequestDelay(type, addr);
	}

	public uint8 read_u8(uint32 addr, boolean log_output) throws MemoryException
	{
		return mem.read_u8(addr, log_output);
	}

	public uint16 read_u16(uint32 addr, boolean log_output) throws MemoryException
	{
		return mem.read_u16(addr, log_output);
	}

	public uint32 read_u32(uint32 addr, boolean log_output) throws MemoryException
	{
		return mem.read_u32(addr, log_output);
	}

	public uint64 read_u64(uint32 addr, boolean log_output) throws MemoryException
	{
		return mem.read_u64(addr, log_output);
	}
	
	public void write_u8(uint32 addr, uint8 value) throws MemoryException
	{
		mem.write_u8(addr, value);
	}
	
	public void write_u16(uint32 addr, uint16 value) throws MemoryException
	{
		mem.write_u16(addr, value);
	}
	
	public void write_u32(uint32 addr, uint32 value) throws MemoryException
	{
		mem.write_u32(addr, value);
	}

	public void write_u64(uint32 addr, uint64 value) throws MemoryException
	{
		mem.write_u64(addr, value);
	}
	
}
