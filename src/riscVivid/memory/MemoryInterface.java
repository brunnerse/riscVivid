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

import riscVivid.datatypes.RequestType;
import riscVivid.datatypes.uint16;
import riscVivid.datatypes.uint32;
import riscVivid.datatypes.uint64;
import riscVivid.datatypes.uint8;
import riscVivid.exception.MemoryException;

public interface MemoryInterface {

	short getRequestDelay(RequestType instrRd, uint32 addr) throws MemoryException;

	uint8 read_u8(uint32 addr, boolean log_output) throws MemoryException;
	uint16 read_u16(uint32 addr, boolean log_output) throws MemoryException;
	uint32 read_u32(uint32 addr, boolean log_output) throws MemoryException;
	uint64 read_u64(uint32 addr, boolean log_output) throws MemoryException;

//	uint32 read_u32(uint32 addr) throws MemoryException;

	void write_u8(uint32 addr, uint8 value) throws MemoryException;
//	void write_u8(uint32 addr, uint32 value) throws MemoryException;
	void write_u16(uint32 addr, uint16 value) throws MemoryException;
	void write_u32(uint32 addr, uint32 value) throws MemoryException;
	void write_u64(uint32 addr, uint64 value) throws MemoryException;


}
