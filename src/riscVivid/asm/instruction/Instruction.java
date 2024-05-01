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

/**
 * <p>
 * This class is a wrapper around an instruction word Its operations access the
 * bit fields of such an instruction word.
 * </p>
 */
public class Instruction {
	private int instrWord_;
	
	public static final int OPCODE_load		= 0x03;
	public static final int OPCODE_imm		= 0x13;
	public static final int OPCODE_store	= 0x23;
	public static final int OPCODE_reg		= 0x33;
	public static final int OPCODE_branch	= 0x63;
	public static final int OPCODE_system	= 0x73;
	public static final int OPCODE_AUIPC	= 0x17;
	public static final int OPCODE_LUI		= 0x37;
	public static final int OPCODE_JALR		= 0x67;
	public static final int OPCODE_immW		= 0x1b;
	public static final int OPCODE_regW		= 0x3b;
	public static final int OPCODE_FENCE	= 0x0f;
	public static final int OPCODE_atomic	= 0x2f;
	public static final int OPCODE_JAL		= 0x6f;


	/*
	 * ============================* Constructors *============================
	 */

/*	
	public static Instruction fromMnemonic(String mnemonic) {
		Instruction i = Instructions.instance().getInstruction(mnemonic);
		if (i != null)
			return i.clone();
		else
			return null;
	}
*/
	
	/**
	 * Standard constructor
	 */
	public Instruction() {
		setInstrWord(0);
	}

	/**
	 * Constructor for instruction word
	 * 
	 * @param instrWord
	 */
	public Instruction(int instrWord) {
		setInstrWord(instrWord);
	}

	/**
	 * Constructor for instruction word
	 * 
	 * @param opcode
	 * @param funct3
	 */
	public Instruction(int opcode, int funct3) throws InstructionException {
		if (opcode>0x7f || opcode<0 || funct3<0 || funct3>7)
			throw new InstructionException("opcode ot of range");
		setInstrWord(opcode| (funct3<<12));
	}

	/**
	 * Constructor for instruction word
	 * 
	 * @param opcode
	 * @param funct3
	 * @param funct12
	 */
	public Instruction(int opcode, int funct3, int funct12) throws InstructionException {
		if (opcode>0x7f || opcode<0 ||  funct3<0 || funct3>7)
			throw new InstructionException("opcode ot of range");
		setInstrWord(opcode| (funct3<<12) | (funct12<<20));
	}


	// ================* skeleton *================
	public int skeleton() {
		int iw = instrWord();
		switch (iw & 0x7f) {
			case OPCODE_LUI:
			case OPCODE_AUIPC:
			case OPCODE_JAL:
				return (iw & 0x0000007f); // only opcode
			case OPCODE_immW:
			case OPCODE_imm:
				if ((iw&0x3000)==0x1000) // shift => opcode, funct2 and funct6
					return (iw & 0xfc00707f);
				// else fallthrough
			case OPCODE_JALR:
			case OPCODE_branch:
			case OPCODE_load:
			case OPCODE_store:
				return (iw & 0x0000707f); // opcode and funct3
			case OPCODE_reg:
			case OPCODE_regW:
			case OPCODE_atomic:
				return (iw & 0xfe00707f); // opcode, funct3 and funct7
		}
		return (iw & 0xfff0707f); // opcode, funct3 and funct12
	}

	// ================* instrWord *================
	public int instrWord() {
		return instrWord_;
	}

	public void setInstrWord(int instrWord) {
		this.instrWord_ = instrWord;
	}

	// ================* opcode *================
	public int opcode() {
		return instrWord_ & 0x7f;
	}

	public void setOpcode(int opcode) throws InstructionException {
		if (opcode>0x7f || opcode<0x0)
			throw new InstructionException("opcode ot of range (" + opcode + ")");
		instrWord_ = (instrWord_ & 0xffffff80) | (opcode);
	}

	public void setRegNotImm(boolean r) {
		if (r) instrWord_ |= 0x20; // true => register
		else instrWord_ &= ~0x20; // false => immediate
	}

	public int funct3() {
		return (instrWord_>>12) & 0x7;
	}

	// ================* rs *================
	public int rs() {
		return (instrWord_ >> 15) & 0x1f;
	}

	public String rsStr() {
		return Registers.RegNames[rs()];
	}

	public void setRs(int rs) throws InstructionException {
		if (rs > 0x1f || rs < 0)
			throw new InstructionException("register out of range");
		instrWord_ = (instrWord_ & 0xfff07fff) | (rs << 15);
	}

	// ================* rt *================
	public int rt() {
		return (instrWord_ >> 20) & 0x1f;
	}

	public String rtStr() {
		return Registers.RegNames[rt()];
	}

	public void setRt(int rt) throws InstructionException {
		if (rt > 0x1f || rt < 0)
			throw new InstructionException("register out of range");
		instrWord_ = (instrWord_ & 0xfe0fffff) | (rt << 20);
	}

	// ================* rd *================
	public int rd() {
		return (instrWord_ >> 7) & 0x1f;
	}

	public String rdStr() {
		return Registers.RegNames[rd()];
	}

	public void setRd(int rd) throws InstructionException {
		if (rd > 0x1f || rd < 0)
			throw new InstructionException("register out of range");
		instrWord_ = (instrWord_ & 0xfffff07f) | (rd << 7);
	}
	
	// ================* immediates *================

	public int immI() {
		return instrWord_>>20;
	}

	public void setImmI(int imm) throws InstructionException {
		if (imm > 0x7ff || imm < -0x800)
			throw new InstructionException("immI out of range");
		instrWord_ = (instrWord_ & 0x000fffff) | (imm<<20);
	}

	public void setShamt(int imm) throws InstructionException {
		if (imm<0 || imm > 0x1f)
			throw new InstructionException("immI out of range");
		instrWord_ = (instrWord_ & 0xfe0fffff) | (imm<<20);
	}
	
	public int immS() {
		return ((instrWord_>>20)& ~0x1f) | ((instrWord_>>7)&0x1f);
	}

	public void setImmS(int imm) throws InstructionException {
		if (imm > 0x7ff || imm < -0x800)
			throw new InstructionException("immS out of range");
		instrWord_ = (instrWord_ & 0x01fff07f) | ((imm&0x00000fe0)<<20) | ((imm&0x00001f)<<7);
	}

	public int immU() {
		return (instrWord_>>12) & 0x000fffff;
	}

	public void setImmU(int imm) throws InstructionException {
		if (imm > 0xfffff || imm < 0)
			throw new InstructionException("immU out of range");
		instrWord_ = (instrWord_ & 0x00000fff) | (imm<<12);
	}

	public int immB() {
		return ((instrWord_>>19)&0xfffff000) |
			((instrWord_<<4)    &0x00000800) |
			((instrWord_>>20)   &0x000007e0) |
			((instrWord_>>7)    &0x000001e);
	}

	public void setImmB(int imm) throws InstructionException {
		if (imm > 0xfff || imm < -0x1000)
			throw new InstructionException("immB out of range");
		if ((imm&1)!=0)
			throw new InstructionException("immB not aligned");
		instrWord_ = (instrWord_ & 0x01fff07f) 
			| ((imm<<19) & 0x80000000)
			| ((imm<<20) & 0x7e000000)
			| ((imm<<7)  & 0x00000f00)
			| ((imm>>4)  & 0x00000080);
	}

	public int immJ() {
		return ((instrWord_>>11)&0xfff00000) |
			((instrWord_)       &0x000ff000) |
			((instrWord_>>9)    &0x00000800) |
			((instrWord_>>20)   &0x000007fe);
	}

	public void setImmJ(int imm) throws InstructionException {
		if (imm > 0xfffff || imm < -0x100000)
			throw new InstructionException("immJ out of range");
		if ((imm&1)!=0)
			throw new InstructionException("immJ not aligned");
		instrWord_ = (instrWord_ & 0x00000fff) 
			| ((imm<<11) & 0x80000000)
			| ((imm<<20) & 0x7fe00000)
			| ((imm<<9)  & 0x00100000)
			| ((imm)     & 0x000ff000);
	}












	/*
	 * ===============================* String *===============================
	 */
	public String toString(int pc) {
		int i;
		StringBuffer strBuf = new StringBuffer();
		String mnemonic = toMnemonic();
		if (mnemonic==null) mnemonic = "unknown";
		
		switch (opcode()) {
			case OPCODE_load:
				strBuf.append(mnemonic);
				strBuf.append(' ');
				strBuf.append(rdStr());
				strBuf.append(',');
				strBuf.append(immI());
				strBuf.append('(');
				strBuf.append(rsStr());
				strBuf.append(')');
				break;
			case OPCODE_store:
				strBuf.append(mnemonic);
				strBuf.append(' ');
				strBuf.append(rtStr());
				strBuf.append(',');
				strBuf.append(immS());
				strBuf.append('(');
				strBuf.append(rsStr());
				strBuf.append(')');
				break;
			case OPCODE_reg:
			case OPCODE_regW:
				strBuf.append(mnemonic);
				strBuf.append(' ');
				strBuf.append(rdStr());
				strBuf.append(',');
				strBuf.append(rsStr());
				strBuf.append(',');
				strBuf.append(rtStr());
				break;
			case OPCODE_atomic:
				strBuf.append(mnemonic);
				strBuf.append(' ');
				strBuf.append(rdStr());
				strBuf.append(',');
				strBuf.append(rsStr());
				strBuf.append(", (");
				strBuf.append(rtStr());
				strBuf.append(",)");
				break;
			case OPCODE_imm:
			case OPCODE_immW:
				if (instrWord_==0x00000013) {
					strBuf.append("nop");
					break;
				}
				strBuf.append(mnemonic);
				strBuf.append(' ');
				strBuf.append(rdStr());
				strBuf.append(',');
				strBuf.append(rsStr());
				strBuf.append(',');
				i = funct3();
				if (i==1 || i==5) { // for shift operations
					strBuf.append(immI() & 0x3f);
				} else {
					strBuf.append(immI());
				}
				break;
			case OPCODE_branch:
				strBuf.append(mnemonic);
				strBuf.append(' ');
				strBuf.append(rsStr());
				strBuf.append(',');
				strBuf.append(rtStr());
				strBuf.append(",");
				// strBuf.append(",pc");
				strBuf.append(String.format("0x%08x", pc+immB()));
				break;
			case OPCODE_LUI:
			case OPCODE_AUIPC:
				strBuf.append(mnemonic);
				strBuf.append(' ');
				strBuf.append(rdStr());
				strBuf.append(",0x");
				strBuf.append(String.format("%05x", immU() & 0xfffff));
				break;
			case OPCODE_JAL:
				if (rd()==0) {
					strBuf.append("j ");
				} else {
					strBuf.append("jal ");
					if (rd()!=1) {
						strBuf.append(rdStr());
						strBuf.append(',');
					}
				}
				strBuf.append(String.format("0x%08x", pc+immJ()));
				break;
			case OPCODE_JALR:
				if (instrWord_==0x00008067) {
					strBuf.append("ret");
				} else {
					if (rd()==0) {
						strBuf.append("jr ");
					} else {
						strBuf.append("jalr ");
						if (rd()!=1) {
							strBuf.append(rdStr());
							strBuf.append(',');
						}
					}
					if (immI()!=0) {
						strBuf.append(immI());
						strBuf.append('(');
						strBuf.append(rsStr());
						strBuf.append(')');
					} else {
						strBuf.append(rsStr());
					}
				}
				break;
			case OPCODE_FENCE:
			case OPCODE_system:
				// can be improved
				strBuf.append(mnemonic);
				if (rd()!=0) {
					strBuf.append(' ');
					strBuf.append(rdStr());
				}
			default:
			}
		return strBuf.toString();
	}

	public String toMnemonic() {
		return Instructions.instance().getMnemonic(this);
	}

	public String toHexString() {
		String hex = Integer.toHexString(instrWord());
		//leading zeros
		for (int diff = 8 - hex.length(); diff > 0; diff--) {
			hex = '0' + hex;
		}
		hex = "0x" + hex;

		return hex;
	}

	/*
	 * ==============================* Equality *==============================
	 */
	public boolean equals(Object o) {
		if (o instanceof Instruction) {
			Instruction i = (Instruction) o;
			if (i.instrWord() == instrWord())
				return true;
		}
		return false;
	}

	/**
	 * true if i is the same mnemonic e.g. "<b>add r1,r0,r0</b>" and
	 * "<b>add r9,r8,r7</b>"
	 * 
	 * @param i
	 * @return
	 */

	public boolean equalsFamily(Instruction i) {
		if (i.skeleton() == skeleton())
			return true;
		return false;
	}

	public Instruction clone() {
		return new Instruction(instrWord());
	}
}
