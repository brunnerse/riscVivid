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

import java.util.Hashtable;

import static riscVivid.asm.instruction.Instruction.*;

public class Instructions {
	private static Instructions instance_;
	private Hashtable<String, Instruction> str2instr_;
	private Hashtable<InstructionWrapper, String> instr2str_;
	private Hashtable<InstructionWrapper, ParseType> instr2type_;
	private Hashtable<String, String> str2desc_;
	private Hashtable<String, ParseType> str2type_;

	public static Instructions instance() {
		if (instance_ == null)
			instance_ = new Instructions();
		return instance_;
	}

	/**
	 * e.g. getMnemonic(new Instruction(0, 0, 0, 0, 0, 0x20)) == add
	 * 
	 * @param instr
	 * @return
	 */
	public String getMnemonic(Instruction instr) 
	{
		if (instr == null)
			return null;
		String mne = instr2str_.get(new InstructionWrapper(instr));
		if (mne == null) {
			Instruction i = instr.clone();
			i.setRegNotImm(false);
			mne = instr2str_.get(new InstructionWrapper(i));
		}
		return mne;
	}

	/**
	 * e.g. getType("add") == ParseType.ARITH
	 * 
	 * @param mnemonic
	 * @return
	 */
 	public ParseType getParseType(String mnemonic) 
 	{
		if (mnemonic==null) return null;
		return str2type_.get(mnemonic.toLowerCase());
	}
	
	/**
	 * e.g. getInstruction("add") == new Instruction(0, 0, 0, 0, 0, 0x20)
	 * 
	 * @param mnemonic
	 * @return
	 */
	public Instruction getInstruction(String mnemonic) {
		if (mnemonic==null) return null;
		return str2instr_.get(mnemonic.toLowerCase());
	}

	/**
	 * e.g. getDescription("addi") =
	 * "addi rt,rs,immediate: add immediate and rs and save result in rt"
	 * 
	 * @param mnem
	 * @return
	 */
	public String getDescription(String mnem) {
		if (mnem == null)
			return null;
		if (mnem.equalsIgnoreCase("nop"))
			return "";
		return str2desc_.get(mnem);
	}

	private Instructions() {
		str2instr_ = new Hashtable<String, Instruction>();
		instr2str_ = new Hashtable<InstructionWrapper, String>();
		instr2type_ = new Hashtable<InstructionWrapper, ParseType>();
		str2desc_ = new Hashtable<String, String>();
		str2type_ = new Hashtable<String, ParseType>();
		try {
			add("lui", new Instruction(OPCODE_LUI), ParseType.UTYPE, 
					"lui[Load Upper Immediate] rd,imm20: set bits 31..20 of rd");
			add("auipc", new Instruction(OPCODE_AUIPC), ParseType.UTYPE, 
					"auipc[Add Upper Immediate to PC] rd,imm20: add to upper bits of pc");
			add("jal", new Instruction(OPCODE_JAL), ParseType.JTYPE, 
					"jal[Jump And Link] rd,disp: pc-relative jump, safe old pc in rd");
			add("j", new Instruction(OPCODE_JAL), ParseType.JUMP, 
					"j[Jump] disp: pc-relative jump");
			add("jalr", new Instruction(OPCODE_JALR, 0x0), ParseType.ITYPE, 
					"jal[Jump And Link] rd,disp: pc-relative jump, safe old pc in rd");
			add("jr", new Instruction(OPCODE_JALR, 0x0, 0x000), ParseType.ONEREG, 
					"retl[RETurn]: return from subroutine by jumping to ra");
			add("ret", new Instruction(0x00008067), ParseType.NOARGS, 
					"retl[RETurn]: return from subroutine by jumping to ra");
			add("beq", new Instruction(OPCODE_branch, 0x0), ParseType.BTYPE, 
					"beq[Branch if EQual] rs,rt,disp: branch if rs==rt");
			add("bne", new Instruction(OPCODE_branch, 0x1), ParseType.BTYPE, 
					"beq[Branch if Not Equal] rs,rt,disp: branch if rs!=rt");
			add("blt", new Instruction(OPCODE_branch, 0x4), ParseType.BTYPE, 
					"beq[Branch if Lower Than] rs,rt,disp: branch if rs<rt");
			add("bge", new Instruction(OPCODE_branch, 0x5), ParseType.BTYPE, 
					"beq[Branch if Greater or Equal] rs,rt,disp: branch if rs>=rt");
			add("bltu", new Instruction(OPCODE_branch, 0x6), ParseType.BTYPE, 
					"beq[Branch if Lower Than Unsigned] rs,rt,disp: branch if rs<rt (unsigned comparison))");
			add("bgeu", new Instruction(OPCODE_branch, 0x7), ParseType.BTYPE, 
					"beq[Branch if Greater or Equal Unsigned] rs,rt,disp: branch if rs>=rt (unsigned comparison)");
			add("lb", new Instruction(OPCODE_load, 0x0), ParseType.LOAD, 
					"lb[Load Byte] rd,imm(rs): read 8 bits from address rs+imm with sign extension");
			add("lh", new Instruction(OPCODE_load, 0x1), ParseType.LOAD, 
					"lh[Load Halfword] rd,imm(rs): read 16 bits from address rs+imm with sign extension");
			add("lw", new Instruction(OPCODE_load, 0x2), ParseType.LOAD, 
					"lw[Load Word] rd,imm(rs): read 32 bits from address rs+imm with zero extension");
			add("lbu", new Instruction(OPCODE_load, 0x4), ParseType.LOAD, 
					"lbu[Load Byte Unsigned] rd,imm(rs): read 8 bits from address rs+imm");
			add("lhu", new Instruction(OPCODE_load, 0x5), ParseType.LOAD, 
					"lhu[Load Halfword Unsigned] rd,imm(rs): read 16 bits from address rs+imm");
			add("sb", new Instruction(OPCODE_store, 0x0), ParseType.STYPE, 
					"sb[Store Byte] rt,imm(rs): write 8 bits to address rs+imm");
			add("sh", new Instruction(OPCODE_store, 0x1), ParseType.STYPE, 
					"sh[Store Halfword] rt,imm(rs): write 16 bits to address rs+imm");
			add("sw", new Instruction(OPCODE_store, 0x2), ParseType.STYPE, 
					"sw[Store Word] rt,imm(rs): write 32 bits to address rs+imm");
			add("nop", new Instruction(OPCODE_imm, 0x0), ParseType.NOARGS, 
					"nop[No OPeration]: do nothing");
			add("add", new Instruction(OPCODE_imm, 0x0), ParseType.ARITH,
					"add[ADD] rd,rs,rt/imm: add rs and rt/imm and save result in rd");
			add("sub", new Instruction(OPCODE_reg, 0x0, 0x400), ParseType.RTYPE,
					"sub[SUBtract word] rd,rs,rt: subtract rt from rs and save result in rd");
			add("sll", new Instruction(OPCODE_imm, 0x1), ParseType.SHIFT,
					"sll[Shift Left Logical] rd,rt,rs: do a logical left shift on rt by rs bits and save result in rd");
			add("slt", new Instruction(OPCODE_imm, 0x2), ParseType.ARITH,
					"slt[Set on Less Than] rd,rs,rt/imm: set rd to 1 if rs is less than rd");
			add("sltu", new Instruction(OPCODE_imm, 0x3), ParseType.ARITH,
					"sltu[Set on Less Than Unsigned] rd,rs,rt/imm: set rd to 1 if the unsigned number in rs is less than rd");
			add("xor", new Instruction(OPCODE_imm, 0x4), ParseType.ARITH,
					"xor[eXclusive OR] rd,rs,rt: do a bitwise XOR of rt and rs and save result in rd");
			add("srl", new Instruction(OPCODE_imm, 0x5, 0x000), ParseType.SHIFT,
					"sll[Shift Right Logical] rd,rt,rs: do a logical right shift on rt by rs bits and save result in rd");
			add("sra", new Instruction(OPCODE_imm, 0x5, 0x400), ParseType.SHIFT,
					"sll[Shift Right Arithmetical] rd,rt,rs: do an arithmetical right shift on rt by rs bits and save result in rd");
			add("or", new Instruction(OPCODE_imm, 0x6), ParseType.ARITH,
					"or[OR] rd,rs,rt: do a bitwise OR of rt and rs and save result in rd");
			add("and", new Instruction(OPCODE_imm, 0x7), ParseType.ARITH,
					"and[AND] rd,rs,rt: do a bitwise AND of rt and rs and save result in rd");
			add("fence", new Instruction(OPCODE_FENCE, 0x0), ParseType.NOARGS,
					"");
			add("fence.i", new Instruction(OPCODE_FENCE, 0x1), ParseType.NOARGS,
					"");
			add("scall", new Instruction(OPCODE_system, 0x0, 0x000), ParseType.NOARGS,
					"");
			add("sbreak", new Instruction(OPCODE_system, 0x0, 0x001), ParseType.NOARGS,
					"");
			add("rdcycle", new Instruction(OPCODE_system, 0x2, 0xc00), ParseType.ONEREG,
					"");
			add("rdcycleh", new Instruction(OPCODE_system, 0x2, 0xc10), ParseType.ONEREG,
					"");
			add("rdtime", new Instruction(OPCODE_system, 0x2, 0xc00), ParseType.ONEREG,
					"");
			add("rdtimeh", new Instruction(OPCODE_system, 0x2, 0xc10), ParseType.ONEREG,
					"");
			add("rdinstret", new Instruction(OPCODE_system, 0x2, 0xc00), ParseType.ONEREG,
					"");
			add("rdinstreth", new Instruction(OPCODE_system, 0x2, 0xc10), ParseType.ONEREG,
					"");
// RV64I
/*
			add("lwu", new Instruction(OPCODE_load, 0x6), ParseType.LOAD, 
					"lwu[Load Word Unsigned] rd,imm(rs): read 32 bits from address rs+imm");
			add("ld", new Instruction(OPCODE_load, 0x3), ParseType.LOAD, 
					"lwu[Load Doubleword] rd,imm(rs): read 64 bits from address rs+imm");
			add("sd", new Instruction(OPCODE_store, 0x3), ParseType.STYPE, 
					"sd[Store Doubleword] rt,imm(rs): write 64 bits to address rs+imm");
			add("addw", new Instruction(OPCODE_immW, 0x0), ParseType.ARITH,
					"");
			add("subw", new Instruction(OPCODE_regW, 0x0, 0x400), ParseType.RTYPE,
					"");
			add("sllw", new Instruction(OPCODE_immW, 0x1), ParseType.SHIFT,
					"");
			add("srlw", new Instruction(OPCODE_immW, 0x5, 0x000), ParseType.SHIFT,
					"");
			add("sraw", new Instruction(OPCODE_immW, 0x5, 0x400), ParseType.SHIFT,
					"");
*/
			
// RV32M
			add("mul", new Instruction(OPCODE_reg, 0x0, 0x020), ParseType.RTYPE,
					"mul[MULtiply] rd,rs,rt: rd=rs*rt");
			add("mulh", new Instruction(OPCODE_reg, 0x1, 0x020), ParseType.RTYPE,
					"mulh[MULtiply High word] rd,rs,rt: store the upper word of rs*rt in rd");
			add("mulhsu", new Instruction(OPCODE_reg, 0x2, 0x020), ParseType.RTYPE,
					"");
			add("mulhu", new Instruction(OPCODE_reg, 0x3, 0x020), ParseType.RTYPE,
					"");
			add("div", new Instruction(OPCODE_reg, 0x4, 0x020), ParseType.RTYPE,
					"");
			add("divu", new Instruction(OPCODE_reg, 0x5, 0x020), ParseType.RTYPE,
					"");
			add("rem", new Instruction(OPCODE_reg, 0x6, 0x020), ParseType.RTYPE,
					"");
			add("remu", new Instruction(OPCODE_reg, 0x7, 0x020), ParseType.RTYPE,
					"");
// RV32M
/*
			add("mulw", new Instruction(OPCODE_regW, 0x0, 0x020), ParseType.RTYPE,
					"");
			add("divw", new Instruction(OPCODE_regW, 0x4, 0x020), ParseType.RTYPE,
					"");
			add("divuw", new Instruction(OPCODE_regW, 0x5, 0x020), ParseType.RTYPE,
					"");
			add("remw", new Instruction(OPCODE_regW, 0x6, 0x020), ParseType.RTYPE,
					"");
			add("remuw", new Instruction(OPCODE_regW, 0x7, 0x020), ParseType.RTYPE,
					"");
*/

		} catch (InstructionException e) {
			e.printStackTrace();
		}
	}

	private void add(String mnemonic, Instruction instruction, ParseType encoding, String desc) {
		str2instr_.put(mnemonic, instruction);
		str2desc_.put(mnemonic, desc);
		str2type_.put(mnemonic, encoding);

		/*
		if (encoding==ParseType.ARITH || encoding==ParseType.SHIFT) {
			Instruction i = instruction;
			i.setRegNotImm(false);
			InstructionWrapper w1 = new InstructionWrapper(i);
			instr2str_.put(w1, mnemonic);
			instr2type_.put(w1, encoding);
			
  			//i.setRegNotImm(true);
			InstructionWrapper w2 = new InstructionWrapper(i);
			instr2str_.put(w2, mnemonic);
			instr2type_.put(w2, encoding);

		} else {
*/

			InstructionWrapper wrapper = new InstructionWrapper(instruction);
			instr2str_.put(wrapper, mnemonic);
			instr2type_.put(wrapper, encoding);
//		}
	}

	/**
	 * This wrapper is used so that the test for equality is only applied on the
	 * skeleton.
	 * 
	 */
	private class InstructionWrapper {
		public Instruction instr_;

		public InstructionWrapper(Instruction instr) {
			this.instr_ = instr;
		}

		public boolean equals(Object o) {
			if (o instanceof InstructionWrapper) {
				return ((InstructionWrapper) o).instr_.equalsFamily((instr_));
			}
			return false;
		}

		public int hashCode() {
			return instr_.skeleton();
		}
	}
}
