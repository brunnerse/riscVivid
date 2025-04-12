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

public enum OpcodeSPECIAL {
	SLL,
	MOVCI,
	SRL,
	SRA,
	SLLV,
	SRLV,
	SRAV,
	JR,
	JALR,
	MOVZ,
	MOVN,
	SYSCALL,
	BREAK,
	SYNC,
	MFHI,
	MTHI,
	MFLO,
	MTLO,
	DSLLV,
	DSRLV,
	DSRAV,
	MULT,
	MULTU,
	DIV,
	DIVU,
	DMULT,
	DMULTU,
	DDIV,
	DDIVU,
	ADD,
	ADDU,
	SUB,
	SUBU,
	AND,
	OR,
	XOR,
	NOR,
	SEQ, // for DLX ISA only
	SEQU, // for DLX ISA only
	SNE, // for DLX ISA only
	SNEU, // for DLX ISA only
	SLE, // for DLX ISA only
	SLEU, // for DLX ISA only
	SGE, // for DLX ISA only
	SGEU, // for DLX ISA only
	SGT, // for DLX ISA only
	SGTU, // for DLX ISA only
	TRAP, // for DLX ISA only
	SLT,
	SLTU,
	DADD,
	DADDU,
	DSUB,
	DSUBU,
	TGE,
	TGEU,
	TLT,
	TLTU,
	TEQ,
	TNE,
	DSLL,
	DSRL,
	DSRA,
	DSLL32,
	DSRL32,
	DSRA32,
	AMOLR,
	AMOSC,
	AMOSWAP,
	AMOADD,
	AMOXOR,
	AMOAND,
	AMOOR,
	AMOMIN,
	AMOMAX,
	AMOMINU,
	AMOMAXU,
	UNKNOWN
}
