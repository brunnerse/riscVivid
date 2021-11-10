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
package riscVivid.asm;

/**
 * This is the base exception class for all assembler exceptions. Its properties
 * are message, line number and row number
 * 
 */
@SuppressWarnings("serial")
public class AssemblerException extends Exception {

	/**
	 * simple forwarding to Exception constructor
	 * 
	 * @param message
	 */
	public AssemblerException(String message) {
		super(message);
	}

	/**
	 * 
	 * @return source line where exception was thrown or -1 if no line specified
	 */
	public int getLine() {
		//minimal implementation
		return -1;
	}

	/**
	 * 
	 * @return source row where exception was thrown or -1 if no row specified
	 */
	public int getRow() {
		//minimal implementation
		return -1;
	}

	/**
	 * 
	 * @return message + (line,row)
	 */
	public String toString() {
		if (getLine() != -1)
			return super.toString() + " at (" + (getLine()+1) + "," + (getRow()+1) + ")"; // +1 because line numbering starts at 1
		else
			return super.toString();
	}
}
