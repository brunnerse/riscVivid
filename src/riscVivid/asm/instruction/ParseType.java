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
package riscVivid.asm.instruction;

// How to parse after the mnemonic
public enum ParseType {
//	LOAD, SAVE, ALU_REGISTER, ALU_IMMEDIATE, BRANCH, LOAD_IMMEDIATE, JUMP, JUMP_REGISTER, SHIFT_REGISTER, SHIFT_IMMEDIATE, TRAP, NOP,
	RTYPE, ITYPE, STYPE, UTYPE, BTYPE, JTYPE, JUMP, JALR, ARITH, SHIFT, LOAD, ONEREG, SRCREG, NOARGS;
}
