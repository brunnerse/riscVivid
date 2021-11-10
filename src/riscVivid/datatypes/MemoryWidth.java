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

public enum MemoryWidth
{
	BYTE(1, false),
	UBYTE(1, true),
	HWORD(2, false),
	UHWORD(2, true),
	WORD(4, false),
	UWORD(4, true),
	DWORD(8, false),
	UDWORD(8, true),
	WORD_RIGHT_PART(2, false),
	WORD_LEFT_PART(2, false);
	
	private int bytewidth;
	private boolean isUnsigned;
	
	MemoryWidth(int numBytes, boolean isUnsigned) {
	    this.bytewidth = numBytes;
	    this.isUnsigned = isUnsigned;
	}
	
	public int getByteWidth() {
	    return bytewidth;
	}
	
	public boolean isUnsigned() {
	    return isUnsigned;
	}
}
