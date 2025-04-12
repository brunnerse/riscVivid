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
package riscVivid;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import riscVivid.datatypes.*;
import riscVivid.exception.*;

public class Decode
{
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


	private static Logger logger = Logger.getLogger("DECODE");
	private Instruction current_inst;
	private RegisterSet reg_set;
	private Queue<FetchDecodeData> fetch_decode_latch;

	private LinkedList<uint8> forwardedRegs = new LinkedList<uint8>();
	private int cyclesSinceLastBranch = ArchCfg.getNumBranchDelaySlots();
	
	public Decode(RegisterSet reg_set)
	{
		this.reg_set = reg_set;
	}

	Instruction decodeInstr(uint32 instr) throws UnknownInstructionException, CacheException, PipelineDataTypeException
	{
		OpcodeNORMAL opN = OpcodeNORMAL.UNKNOWN;
		OpcodeREGIMM opR = OpcodeREGIMM.UNKNOWN;
		OpcodeSPECIAL opS = OpcodeSPECIAL.UNKNOWN;
		current_inst = new Instruction(instr);
		
		if (instr.getValue()==0) {
			// special treatment of empty memory
			current_inst.setOpNormal(OpcodeNORMAL.NOP);
			current_inst.setALUFunction(ALUFunction.NOP);
			current_inst.setALUPortA(ALUPort.ZERO);
			current_inst.setALUPortB(ALUPort.ZERO);
			logger.debug("Empty instruction");
			return current_inst;
		}
		
		switch (current_inst.getOpcode().getValue())
		{
			
		case OPCODE_LUI:
			current_inst.setUseImmediate(true);
			current_inst.setImmType(ImmType.UTYPE);
			current_inst.setWriteRd(true);
			current_inst.setALUPortA(ALUPort.ZERO);
			current_inst.setALUPortB(ALUPort.IMM);
			current_inst.setALUFunction(ALUFunction.ADD);
			break;

		case OPCODE_AUIPC:
			current_inst.setUseImmediate(true);
			current_inst.setImmType(ImmType.UTYPE);
			current_inst.setWriteRd(true);
			current_inst.setALUPortA(ALUPort.PC);
			current_inst.setALUPortB(ALUPort.IMM);
			current_inst.setALUFunction(ALUFunction.ADD);
			break;

		case OPCODE_JAL:
			opN = (OpcodeNORMAL.JAL); // ?
			current_inst.setType(InstructionType.JTYPE);
			
//			current_inst.setUseInstrIndex(true);
			current_inst.setWriteRd(true);
			current_inst.setUseImmediate(true);
			current_inst.setImmType(ImmType.JTYPE);
			current_inst.setALUFunction(ALUFunction.ADD);
			current_inst.setALUPortA(ALUPort.PC);
			current_inst.setALUPortB(ALUPort.IMM);
			current_inst.setBranch(true);
			current_inst.setBranchAndLink(true);
			current_inst.setBranchCondition(BranchCondition.UNCOND);
			current_inst.setBranchPortA(BranchCtrlPort.ZERO);
			current_inst.setBranchPortB(BranchCtrlPort.ZERO);
			break;
			
		case OPCODE_JALR:
			opS = (OpcodeSPECIAL.JR);
			current_inst.setType(InstructionType.ITYPE);
			
			current_inst.setWriteRd(true);
			current_inst.setReadRs(true);
			current_inst.setUseImmediate(true);
			current_inst.setImmType(ImmType.ITYPE);
			current_inst.setALUFunction(ALUFunction.ADD);
			current_inst.setALUPortA(ALUPort.RS);
			current_inst.setALUPortB(ALUPort.IMM);
			current_inst.setBranch(true);
			current_inst.setBranchAndLink(true);
			current_inst.setBranchCondition(BranchCondition.UNCOND);
			current_inst.setBranchPortA(BranchCtrlPort.ZERO);
			current_inst.setBranchPortB(BranchCtrlPort.ZERO);
			break;
			
		case OPCODE_branch:
			opN = (OpcodeNORMAL.BEQ); // req?
			current_inst.setType(InstructionType.ITYPE); // req?
			
			current_inst.setReadRs(true);
			current_inst.setReadRt(true);
			current_inst.setUseImmediate(true);
			current_inst.setImmType(ImmType.BTYPE);
			current_inst.setALUFunction(ALUFunction.ADD); 
				// ADD insted of BR which adds 4*imm
			current_inst.setALUPortA(ALUPort.PC);
			current_inst.setALUPortB(ALUPort.IMM);
			current_inst.setBranch(true);
			current_inst.setBranchPortA(BranchCtrlPort.RS);
			current_inst.setBranchPortB(BranchCtrlPort.RT);

			switch (current_inst.getFunct3()) {
			case 0x0:
				current_inst.setBranchCondition(BranchCondition.BEQ);
				break;
			case 0x1:
				current_inst.setBranchCondition(BranchCondition.BNE);
				break;
			case 0x4:
				current_inst.setBranchCondition(BranchCondition.BLT);
				break;
			case 0x5:
				current_inst.setBranchCondition(BranchCondition.BGE);
				break;
			case 0x6:
				current_inst.setBranchCondition(BranchCondition.BLTU);
				break;
			case 0x7:
				current_inst.setBranchCondition(BranchCondition.BGEU);
				break;
			default:
				throw new UnknownInstructionException("Unknown funct3 in branch instruction: " 
						+ current_inst.getFunct3());
			}
			break;

		case OPCODE_load:
			opN = (OpcodeNORMAL.LB);
			current_inst.setType(InstructionType.ITYPE);
			current_inst.setReadRs(true);
			current_inst.setWriteRd(true);
			current_inst.setUseImmediate(true);
			current_inst.setImmType(ImmType.ITYPE);
			current_inst.setLoad(true);
			current_inst.setALUFunction(ALUFunction.ADD);
			current_inst.setALUPortA(ALUPort.RS);
			current_inst.setALUPortB(ALUPort.IMM);

			switch (current_inst.getFunct3()) {
			case 0x0:
				current_inst.setMemoryWidth(MemoryWidth.BYTE);
				break;
			case 0x1:
				current_inst.setMemoryWidth(MemoryWidth.HWORD);
				break;
			case 0x2:
				current_inst.setMemoryWidth(MemoryWidth.WORD);
				break;
			case 0x3:
				current_inst.setMemoryWidth(MemoryWidth.DWORD);
				break;
			case 0x4:
				current_inst.setMemoryWidth(MemoryWidth.UBYTE);
				break;
			case 0x5:
				current_inst.setMemoryWidth(MemoryWidth.UHWORD);
				break;
			case 0x6:
				current_inst.setMemoryWidth(MemoryWidth.UWORD);
				break;
			default:
				throw new UnknownInstructionException("Unknown funct3 in load instruction: " 
					+ current_inst.getFunct3());
			}
			break;

		case OPCODE_store:
			opN = (OpcodeNORMAL.SB);
			current_inst.setType(InstructionType.ITYPE); // required?
			current_inst.setReadRs(true);
			current_inst.setReadRt(true);
			current_inst.setUseImmediate(true);
			current_inst.setImmType(ImmType.STYPE);
			current_inst.setStore(true);
			current_inst.setALUFunction(ALUFunction.ADD);
			current_inst.setALUPortA(ALUPort.RS);
			current_inst.setALUPortB(ALUPort.IMM);

			switch (current_inst.getFunct3()) {
			case 0x0:
				current_inst.setMemoryWidth(MemoryWidth.BYTE);
				break;
			case 0x1:
				current_inst.setMemoryWidth(MemoryWidth.HWORD);
				break;
			case 0x2:
				current_inst.setMemoryWidth(MemoryWidth.WORD);
				break;
			case 0x3:
				current_inst.setMemoryWidth(MemoryWidth.DWORD);
				break;
			default:
				throw new UnknownInstructionException("Unknown funct3 in store instruction: " 
					+ current_inst.getFunct3());
			}
			break;
			
		case OPCODE_imm:
			current_inst.setType(InstructionType.ITYPE);
			current_inst.setReadRs(true);
			current_inst.setUseImmediate(true);
			current_inst.setImmType(ImmType.ITYPE);
			current_inst.setWriteRd(true);
			current_inst.setALUPortA(ALUPort.RS);
			current_inst.setALUPortB(ALUPort.IMM);

			switch (current_inst.getFunct3()) {
				case 0x0:
					opN = (OpcodeNORMAL.ADDI);
					current_inst.setALUFunction(ALUFunction.ADD);
					break;
				case 0x1:
					opN = OpcodeNORMAL.SPECIAL;
					opS = (OpcodeSPECIAL.SLL);
					current_inst.setALUFunction(ALUFunction.SLL);
					break;
				case 0x2:
					opN = (OpcodeNORMAL.SLTI);
					current_inst.setALUFunction(ALUFunction.SLT);
					break;
				case 0x3:
					opN = (OpcodeNORMAL.SLTIU);
					current_inst.setALUFunction(ALUFunction.SLTU);
					break;
				case 0x4:
					opN = (OpcodeNORMAL.XORI);
					current_inst.setALUFunction(ALUFunction.XOR);
					break;
				case 0x5:
					opN = OpcodeNORMAL.SPECIAL;
					if ((current_inst.getFunct12()&0x400)==0) {
						opS = (OpcodeSPECIAL.SRL);
						current_inst.setALUFunction(ALUFunction.SRL);
					} else {
						opS = (OpcodeSPECIAL.SRA);
						current_inst.setALUFunction(ALUFunction.SRA);
					}
					break;
				case 0x6:
					opN = (OpcodeNORMAL.ORI);
					current_inst.setALUFunction(ALUFunction.OR);
					break;
				case 0x7:
					opN = (OpcodeNORMAL.ANDI);
					current_inst.setALUFunction(ALUFunction.AND);
					break;

			}
			break;

		case OPCODE_reg:
			current_inst.setType(InstructionType.RTYPE);
			current_inst.setReadRs(true);
			current_inst.setReadRt(true);
			current_inst.setWriteRd(true);
			current_inst.setALUPortA(ALUPort.RS);
			current_inst.setALUPortB(ALUPort.RT);
			
			opN = OpcodeNORMAL.SPECIAL;
			switch (current_inst.getFunct12()>>5) {
			case 0:
				switch (current_inst.getFunct3()) {
				case 0x0:
					opS = (OpcodeSPECIAL.ADD);
					current_inst.setALUFunction(ALUFunction.ADD);
					break;
				case 0x1:
					opS = (OpcodeSPECIAL.SLLV);
					current_inst.setALUFunction(ALUFunction.SLL);
					break;
				case 0x2:
					opS = (OpcodeSPECIAL.SLT);
					current_inst.setALUFunction(ALUFunction.SLT);
					break;
				case 0x3:
					opS = (OpcodeSPECIAL.SLTU);
					current_inst.setALUFunction(ALUFunction.SLTU);
					break;
				case 0x4:
					opS = (OpcodeSPECIAL.XOR);
					current_inst.setALUFunction(ALUFunction.XOR);
					break;
				case 0x5:
					opS = (OpcodeSPECIAL.SRLV);
					current_inst.setALUFunction(ALUFunction.SRL);
					break;
				case 0x6:
					opS = (OpcodeSPECIAL.OR);
					current_inst.setALUFunction(ALUFunction.OR);
					break;
				case 0x7:
					opS = (OpcodeSPECIAL.AND);
					current_inst.setALUFunction(ALUFunction.AND);
					break;
				}
				break;

			case 1:
				switch (current_inst.getFunct3()) {
				case 0x0:
					opS = (OpcodeSPECIAL.MULT);
					current_inst.setALUFunction(ALUFunction.MULT);
					break;
				case 0x1:
					opS = (OpcodeSPECIAL.MULT);
					current_inst.setALUFunction(ALUFunction.MULH);
					break;
				case 0x2:
					opS = (OpcodeSPECIAL.MULT);
					current_inst.setALUFunction(ALUFunction.MULHSU);
					break;
				case 0x3:
					opS = (OpcodeSPECIAL.MULT);
					current_inst.setALUFunction(ALUFunction.MULHU);
					break;
				case 0x4:
					opS = (OpcodeSPECIAL.DIV);
					current_inst.setALUFunction(ALUFunction.DIV);
					break;
				case 0x5:
					opS = (OpcodeSPECIAL.DIVU);
					current_inst.setALUFunction(ALUFunction.DIVU);
					break;
				case 0x6:
					opS = (OpcodeSPECIAL.DIV);
					current_inst.setALUFunction(ALUFunction.REM);
					break;
				case 0x7:
					opS = (OpcodeSPECIAL.DIVU);
					current_inst.setALUFunction(ALUFunction.REMU);
					break;
				}
				break;
				
			case 0x20:
				switch (current_inst.getFunct3()) {
				case 0x0:
					opS = (OpcodeSPECIAL.SUB);
					current_inst.setALUFunction(ALUFunction.SUB);
					break;
				case 0x5:
					opS = (OpcodeSPECIAL.SRAV);
					current_inst.setALUFunction(ALUFunction.SRA);
					break;
				default:
					logger.error("Instruction unknown " + String.format("(0x%08x).", current_inst.getInstr().getValue()));
					throw new UnknownInstructionException("Instruction unknown " + String.format("(0x%08x).", current_inst.getInstr().getValue()));
				}
				break;
			
			default:
				logger.error("Instruction unknown " + String.format("(0x%08x).", current_inst.getInstr().getValue()));
				throw new UnknownInstructionException("Instruction unknown " + String.format("(0x%08x).", current_inst.getInstr().getValue()));
				
			}
		break;

		case OPCODE_FENCE:
			current_inst.setOpNormal(OpcodeNORMAL.NOP);
			current_inst.setALUFunction(ALUFunction.NOP);
			current_inst.setALUPortA(ALUPort.ZERO);
			current_inst.setALUPortB(ALUPort.ZERO);
			break;
		case OPCODE_atomic:
			opN = (OpcodeNORMAL.SPECIAL);
			current_inst.setType(InstructionType.RTYPE);
			current_inst.setReadRs(true);
			current_inst.setReadRt(true);
			current_inst.setWriteRd(true);
			current_inst.setUseImmediate(true);
			current_inst.setImmType(ImmType.ITYPE);
			current_inst.setLoad(true);
			current_inst.setStore(true);
			current_inst.setALUFunction(ALUFunction.ADD);
			current_inst.setALUPortA(ALUPort.RS);
			current_inst.setALUPortB(ALUPort.ZERO);

			switch (current_inst.getFunct3()) {
				case 0x2:
					current_inst.setMemoryWidth(MemoryWidth.WORD);
					break;
				case 0x3:
					current_inst.setMemoryWidth(MemoryWidth.DWORD);
					break;
				default:
					throw new UnknownInstructionException("Unknown funct3 in atomic instruction: "
							+ current_inst.getFunct12());
			}

			switch (current_inst.getFunct12() >> 7) { // Only the 5 upper bits matter
				case 0x2:
					opS = OpcodeSPECIAL.AMOLR;
					break;
				case 0x3:
					opS = OpcodeSPECIAL.AMOSC;
					break;
				case 0x1:
					opS = OpcodeSPECIAL.AMOSWAP;
					break;
				case 0x0:
					opS = OpcodeSPECIAL.AMOADD;
					break;
				case 0x4:
					opS = OpcodeSPECIAL.AMOXOR;
					break;
				case 0xc:
					opS = OpcodeSPECIAL.AMOAND;
					break;
				case 0x8:
					opS = OpcodeSPECIAL.AMOOR;
					break;
				case 0x10:
					opS = OpcodeSPECIAL.AMOMIN;
					break;
				case 0x14:
					opS = OpcodeSPECIAL.AMOMAX;
					break;
				case 0x18:
					opS = OpcodeSPECIAL.AMOMINU;
					break;
				case 0x1c:
					opS = OpcodeSPECIAL.AMOMAXU;
					break;
				default:
					throw new UnknownInstructionException("Unknown funct12 in atomic instruction: "
						+ current_inst.getFunct3());
			}
			break;

		case OPCODE_system:
			if (current_inst.getFunct3()==0 && current_inst.getFunct12()==0) {
				// scall
				opN = OpcodeNORMAL.SPECIAL;
				opS = (OpcodeSPECIAL.SYSCALL);
				current_inst.setALUFunction(ALUFunction.SYSCALL);
				break;
			} else if (current_inst.getFunct3()==0 && current_inst.getFunct12()==1) {
				// sbreak
				opN = OpcodeNORMAL.SPECIAL;
				opS = (OpcodeSPECIAL.BREAK);
				current_inst.setALUFunction(ALUFunction.SYSCALL);
				break;
			} if (current_inst.getFunct3()==2) {
				break;
			}
			// fallthrough
			
		default:
			logger.error("Instruction unknown " + String.format("(0x%08x).", current_inst.getInstr().getValue()));
			throw new UnknownInstructionException("Instruction unknown " + String.format("(0x%08x).", current_inst.getInstr().getValue()));
		}
		
		current_inst.setOpNormal(opN);
		current_inst.setOpRegimm(opR);
		current_inst.setOpSpecial(opS);
		return current_inst;
	}

	
	public void setInputLatch(Queue<FetchDecodeData> fetchDecodeLatch)
	{
		fetch_decode_latch = fetchDecodeLatch;
	}

	public DecodeOutputData doCycle() throws DecodeStageException, CacheException, PipelineDataTypeException 
	{
		uint32 alu_in_a;
		uint32 alu_in_b;
		FetchDecodeData fdd = fetch_decode_latch.element();
		uint32 decode_instr = fdd.getInstr();
		uint32 pc = fdd.getPc();
		Instruction inst;
		try {
			inst = decodeInstr(decode_instr);
		} catch (UnknownInstructionException e) {
			e.setInstructionAddress(pc);
			throw e;
		}
		logger.debug("PC: " + pc.getValueAsHexString()
				+ " instruction decoded as " + inst.getString());

		// determination of input for ALU port A
		switch (inst.getALUPortA())
		{
		case RS:
			alu_in_a = reg_set.read(inst.getRs());
			break;
		case RT:
			alu_in_a = reg_set.read(inst.getRt());
			break;
		case LO:
			alu_in_a = reg_set.read_SP(SpecialRegisters.LO);
			break;
		case HI:
			alu_in_a = reg_set.read_SP(SpecialRegisters.HI);
			break;
		case PC:
			// for MIPS/DLX: increment the pc by 4, because relative jumps assume the pc of the next instruction
			// for RISCV: no increment necessary
			alu_in_a = new uint32(pc.getValue());
			break;
		case ZERO:
			alu_in_a = new uint32(0);
			break;
		default:
			alu_in_a = new uint32(0);
			throw new DecodeStageException("Wrong ALU Port A", pc);
		}

		// determination of input for ALU port B
		switch (inst.getALUPortB())
		{
		case RT:
			alu_in_b = reg_set.read(inst.getRt());
			break;
		case IDX:
			alu_in_b = new uint32(inst.getInstrIndex().getValue());
			break;
		case IMM:
			alu_in_b = new uint32(inst.getImm());
/*
			if(inst.getImmExtend()==ImmExtend.ZERO)
			{
				alu_in_b = new uint32((inst.getOffset().getValue())&0xFFFF);
			}
			else if(inst.getImmExtend()==ImmExtend.SIGN)
			{
				alu_in_b = new uint32((inst.getOffset().getValue()));
			}
			else if((ArchCfg.isa_type == ISAType.DLX) && (inst.getOpSpecial() == OpcodeSPECIAL.TRAP))
			{
				alu_in_b = new uint32(inst.getRs().getValue());
			}
			else
			{
				alu_in_b = new uint32(0);
				throw new DecodeStageException("Wrong IMM at ALU Port B", pc);
			}
*/
			break;
		case SA:
			alu_in_b = new uint32(inst.getSa().getValue());
			break;
		case ZERO:
			alu_in_b = new uint32(0);
			break;
		default:
			alu_in_b = new uint32(0);
			throw new DecodeStageException("Wrong ALU Port B", pc);
		}

		uint32 branch_ctrl_in_a;
		uint32 branch_ctrl_in_b;
		// determination of input for BRANCH CONTROL port A
		switch(inst.getBrachControlPortA())
		{
		case RS:
			branch_ctrl_in_a = reg_set.read(inst.getRs());
			break;
		case ZERO:
			branch_ctrl_in_a = new uint32(0);
			break;
		default:
			branch_ctrl_in_a = new uint32(0);
			throw new DecodeStageException("Wrong Branch Port A", pc);
		}

		// determination of input for BRANCH CONTROL port B
		switch(inst.getBrachControlPortB())
		{
		case RT:
			branch_ctrl_in_b = reg_set.read(inst.getRt());
			break;
		case ZERO:
			branch_ctrl_in_b = new uint32(0);
			break;
		default:
			branch_ctrl_in_b = new uint32(0);
			throw new DecodeStageException("Wrong Branch Port B", pc);
		}

		// determination of the store value
		uint32 store_value = new uint32(0);
		if (inst.getStore())
		{
			store_value = reg_set.read(inst.getRt());
		}



		// Detection system for uninitialized registers
		PipelineException decodeException = null;
		if (ArchCfg.ignoreBranchDelaySlots()) {
			if (inst.getBranch())
				cyclesSinceLastBranch = 0;
			else
				cyclesSinceLastBranch++;
		}
		// if branch instructions were executed before, don't check for initialization,
		// as the instruction might be ignored as a branch delay slot
		if (!ArchCfg.ignoreBranchDelaySlots() || cyclesSinceLastBranch > ArchCfg.getNumBranchDelaySlots() - 1) {
			if (inst.getReadRs() && !reg_set.isRegisterInitialized(inst.getRs())) {
				if (!forwardedRegs.contains(inst.getRs())) {
					decodeException = new UninitializedRegisterException(inst.getRs(), pc);
				}
			} else if (inst.getReadRt() && !reg_set.isRegisterInitialized(inst.getRt())) {
				if (!forwardedRegs.contains(inst.getRt())) {
					decodeException = new UninitializedRegisterException(inst.getRt(), pc);
				}
			}
		}
		// add register that was written into the forwardedRegs AFTER checking the instruction for initialization
		if (ArchCfg.useForwarding()) {
			if (inst.getWriteRd())
				forwardedRegs.add(inst.getRd());
			if (inst.getWriteRt())
				forwardedRegs.add(inst.getRt());
			while (forwardedRegs.size() > 3) // use forwardedRegs as ring buffer of size 3
				forwardedRegs.pop();
		}

		DecodeExecuteData ded = new DecodeExecuteData(inst, pc, alu_in_a, alu_in_b, branch_ctrl_in_a, branch_ctrl_in_b, store_value);

		return new DecodeOutputData(ded, decodeException);
	}

}
