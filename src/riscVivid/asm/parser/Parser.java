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
package riscVivid.asm.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import riscVivid.asm.AssemblerException;
import riscVivid.asm.MemoryBuffer;
import riscVivid.asm.instruction.Instruction;
import riscVivid.asm.instruction.InstructionException;
import riscVivid.asm.instruction.Instructions;
import riscVivid.asm.instruction.Registers;
import riscVivid.asm.tokenizer.Token;
import riscVivid.asm.tokenizer.TokenType;
import riscVivid.asm.tokenizer.Tokenizer;
import riscVivid.asm.tokenizer.TokenizerException;
import riscVivid.datatypes.uint32;
import riscVivid.gui.internalframes.util.ValueInput;

public class Parser {
	private static final String INCOMPLETE_DIRECTIVE = "incomplete directive";
	private static final String INCOMPLETE_INSTRUCTION = "incomplete instruction";
	private static final String INSTRUCTION_EXCEPTION = "instruction: ";
	private static final String LABEL_ALREADY_EXISTS = "label already exists";
	private static final String LABEL_DOES_NOT_EXISTS = "label does not exist";
	private static final String LABEL_NOT_ALLOWED_HERE = "Label not allowed here";
	private static final String MAIN_NOT_IN_TEXT = "main label is not in a text segment";
	private static final String MISSING_SEPARATOR = "missing separator before";
	private static final String MISSING_INITIAL_DIRECTIVE = "missing initial directive .text or .data";
	private static final String NOT_A_NUMBER = "expected number or label but got";
	private static final String NOT_A_CHAR_OR_CHAR_ARRAY = "expected a character or string but got";
	private static final String NOT_A_REGISTER = "expected register specifier but got";
	private static final String NUMBER_NEGATIVE = "negative value not allowed here";
	private static final String NUMBER_TOO_BIG = "number is too big or too small";
	private static final String REG_OR_IMM = "expected register or number but got";
	private static final String UNKNOWN_MNEMONIC = "unknown mnemonic";
	private static final String UNEXPECTED_LITERAL_END = "unexpected end of string literal";
	private static final String UNEXPECTED_TOKEN = "unexpected token";
	private static final String UNEXPECTED_TRASH = "unexpected trash at end of line";
	private static final String UNKNOWN_DIRECTIVE = "unknown directive";

	/*
	 * This parser runs up to two times over the code.
	 * The first run tries to resolve all labels.
	 * Instructions with labels that are unknown on the first run
	 * are saved in unresolvedInstructions for the second run. 
	 */
	private MemoryBuffer memory_; //where binary output is saved
	//however, there is no linker and hence no distinction between local and global labels
	private Hashtable<String, Integer> globalLabels_;
	private Hashtable<uint32, Integer> mnemonicAddressToLine;
	private Tokenizer tokenizer_;
	private SegmentPointer dataPointer_; //data segment pointer
	private SegmentPointer textPointer_; //text segment pointer
	private SegmentPointer segmentPointer_; //active segment pointer
	private boolean stopOnUnresolvedLabel; //turned to true on second run
	private List<UnresolvedInstruction> unresolvedInstructions_; //
	private boolean hasGlobalMain; //workaround unnecessary when distinction between global and local labels  

	public boolean hasGlobalMain() {
		return hasGlobalMain;
	}
	
	public final Hashtable<uint32, Integer> getAddressToLineTable() {
	    return mnemonicAddressToLine;
	}

	public Parser(int dataSegment, int textSegment) {
		tokenizer_ = new Tokenizer();
		dataPointer_ = new SegmentPointer(dataSegment);
		textPointer_ = new SegmentPointer(textSegment);
		segmentPointer_ = textPointer_;
	}

	/*
	 * ================================* Parse *================================
	 */
	/**
	 * another parse pass for jet unresolved labels, stop on any unresolved
	 * label
	 * 
	 * @param unresolvedInstructions
	 * @param globalLabels
	 * @param memory
	 * @throws ParserException
	 */
	public void resolve(List<UnresolvedInstruction> unresolvedInstructions,
			Hashtable<String, Integer> globalLabels, MemoryBuffer memory) throws AssemblerException {
		stopOnUnresolvedLabel = true;
		unresolvedInstructions_ = unresolvedInstructions;
		globalLabels_ = globalLabels;
		memory_ = memory;
		for (UnresolvedInstruction instr : unresolvedInstructions_) {
			if (instr.inTextSegment)
				segmentPointer_ = textPointer_;
			else
				segmentPointer_ = dataPointer_;
			segmentPointer_.set(instr.position);
			parseLine(instr.tokens);
		}
	}

	/**
	 * parse stream into memory, unresolved instructions are returned
	 * 
	 * @param reader
	 * @param globalLabels
	 * @param memory
	 * @return
	 * @throws TokenizerException
	 * @throws IOException
	 * @throws ParserException
	 */
	public List<UnresolvedInstruction> parse(BufferedReader reader,
			Hashtable<String, Integer> globalLabels, MemoryBuffer memory) throws IOException,
			AssemblerException {
		stopOnUnresolvedLabel = false;
		globalLabels_ = globalLabels;
		tokenizer_.setReader(reader);
		int currentLine = 1;
		mnemonicAddressToLine = new Hashtable<uint32, Integer>();
		memory_ = memory;
		unresolvedInstructions_ = new ArrayList<UnresolvedInstruction>();

		Token[] tokens = tokenizer_.readLine();
		// skip empty lines at the start of the file
		while (tokens != null && tokens.length == 0)
			tokens = tokenizer_.readLine();
		// check the first token: it has to be a directive declaring either a text or a data area
		if (tokens == null || tokens[0].getTokenType() != TokenType.Directive ||
				!(tokens[0].getString().equalsIgnoreCase(".text") || tokens[0].getString().equalsIgnoreCase(".data"))
		)
			throw new ParserException(MISSING_INITIAL_DIRECTIVE, null);

		while (tokens != null) {
		    // if line contains a mnemonic, add the line to the mnemonicLineAddress hashtable
		    for (Token t : tokens) {
		        if (t.getTokenType() == TokenType.Mnemonic) {
		            mnemonicAddressToLine.putIfAbsent(new uint32(segmentPointer_.get()), currentLine);
		            break;
		        }
		    }
			parseLine(tokens);
			tokens = tokenizer_.readLine();
			currentLine++;
		}
		String overlap = memory_.segmentsOverlap();
		if (overlap.length() > 0)
			throw new AssemblerException(overlap);
		return unresolvedInstructions_;
	}

	/**
	 * called from parse(...) and resolve(...)
	 * 
	 * @param tokens
	 * @throws ParserException
	 */
	private void parseLine(Token[] tokens) throws AssemblerException {
		if (tokens.length == 0)
			return;
		Token[] tmpTokens;
		try {
			switch (tokens[0].getTokenType()) {
			case Label:
				String label = tokens[0].getString();
				addLabel(label.substring(0, label.length() - 1));
				tmpTokens = new Token[tokens.length - 1];
				System.arraycopy(tokens, 1, tmpTokens, 0, tokens.length - 1);
				parseLine(tmpTokens);
				break;
			case Mnemonic:
				parseMnemonic(tokens);
				break;
			case Directive:
				parseDirectives(tokens);
				break;
			default:
				throw new ParserException(UNEXPECTED_TOKEN, tokens[0]);
			}
		} catch (ParserException ex) {
			if (ex.getToken() == null)
				ex.setToken(tokens[0]);
			throw ex;
		}
	}

	/*
	 * ===============================* Labels *===============================
	 */
	/**
	 * add label to labels if not exists
	 * 
	 * @param label
	 * @throws ParserException
	 */
	private void addLabel(String label) throws ParserException {
		if (globalLabels_.containsKey(label))
			throw new ParserException(LABEL_ALREADY_EXISTS, null);
		if (label.equals("main") && segmentPointer_ != textPointer_)
			throw new ParserException(MAIN_NOT_IN_TEXT, null);
		globalLabels_.put(label, segmentPointer_.get());
	}

	/**
	 * replaces labels with their corresponding integer constant. If not
	 * possible returns Label Token
	 * 
	 * @param tokens
	 * @return null if successful, otherwise the Identifier Token with the unresolved label
	 */
	private Token resolveLabels(Token[] tokens) {
		for (int i = 0; i < tokens.length; ++i) {
			Token t = tokens[i];
			if (t.getTokenType() == TokenType.Identifier) {
				Integer position = globalLabels_.get(t.getString());
				if (position == null)
					return t;
				tokens[i] = new Token(t.getPosition(), TokenType.IntegerConstant,
						position.toString());
			}
		}
		return null;
	}

	/*
	 * ==============================* Mnemonics *==============================
	 */
	/**
	 * @param tokens
	 * @throws ParserException
	 */
	private void parseMnemonic(Token[] tokens) throws ParserException {
		int iw;
		
		//unresolved labels
		Token t = resolveLabels(tokens);
		if (t != null) {
			if (stopOnUnresolvedLabel == true)
				throw new ParserException(LABEL_DOES_NOT_EXISTS, t);
			unresolvedInstructions_.add(new UnresolvedInstruction(tokens, segmentPointer_.get(),
					segmentPointer_ == textPointer_ ? true : false));
		} else {
			switch (Instructions.instance().getParseType(tokens[0].getString())) {
			case RTYPE:
				iw = rType(tokens);
				break;
			case ITYPE:
				iw = iType(tokens, false);
				break;
			case ISHIFT:
				iw = iType(tokens, true);
				break;
			case STYPE:
				iw = memory(tokens, true);
				break;
			case UTYPE:
				iw = uType(tokens);
				break;
			case BTYPE:
				iw = bType(tokens);
				break;
			case JTYPE:
				iw = jType(tokens);
				break;
			case JUMP:
				iw = jump(tokens);
				break;
			case ARITH:
				iw = arith(tokens, false);
				break;
			case SHIFT:
				iw = arith(tokens, true);
				break;
			case LOAD:
				iw = memory(tokens, false);
				break;
			case ONEREG:
				iw = oneReg(tokens);
				break;
			case SRCREG:
				iw = srcReg(tokens);
				break;
			case MVLI:
				iw = mvli(tokens);
				break;
			case ATOMIC:
				iw = atomic(tokens);
				break;
			case NOARGS:
				iw = noArgs(tokens);
				break;
			default:
				throw new ParserException(UNKNOWN_MNEMONIC, tokens[0]);
			}
			memory_.writeWord(segmentPointer_.get(), iw);
		}
		segmentPointer_.add(4);
		updateMemorySegmentEnd();
	}

	private void expect(Token t, String s) throws ParserException
	{
		if (!t.getString().equals(s))
			throw new ParserException(MISSING_SEPARATOR, t);
	}

	private int expect_reg(Token t) throws ParserException
	{
		Integer	reg = Registers.instance().getInteger(t.getString());
		if (reg==null) throw new ParserException(NOT_A_REGISTER, t);
		return reg;
	}


	/**
	 * e.g. sub r1,r2,r3
	 *
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int atomic(Token[] tokens) throws ParserException
	{
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		int i=1;
		try {
			instr.setRd(expect_reg(tokens[i++]));
			expect(tokens[i++], ",");
			instr.setRs(expect_reg(tokens[i++]));
			expect(tokens[i++], ",");
			if (!tokens[i].getString().equals("(")) {
			        Integer imm = Integer.decode(tokens[i].getString());
			        if (imm == null)
					throw new ParserException(NOT_A_NUMBER, tokens[i]);
			        if (imm != 0)
			                throw new InstructionException("Address offset for atomic instructions must be 0");
			        i++;
			}
			expect(tokens[i++], "(");
			instr.setRt(expect_reg(tokens[i++]));
			expect(tokens[i++], ")");
			if (i < tokens.length)
				throw new ParserException(UNEXPECTED_TRASH, tokens[i]);

            if (instr.toMnemonic().matches("amoor|amoand"))
				throw new ParserException("RiscVivid does not support this atomic instruction", tokens[0]); 
            if (instr.rd() == instr.rs() || instr.rd() == instr.rt() || instr.rs() == instr.rt())
				throw new ParserException("RiscVivid cannot handle atomic instructions where the same register occurs twice", tokens[0]); 
            if (instr.rd() == 0 || instr.rs() == 0)
				throw new ParserException("RiscVivid cannot handle atomic instructions with the register x0", tokens[0]); 

			// Replace the atomic instruction with equivalent instructions
			List<Instruction> instructions = new ArrayList<Instruction>();
			// first: lw instruction to store current val in rd
            Instruction instr_new = Instructions.instance().getInstruction("lw").clone();
            //System.out.println("Found amo instruction: " + instr.toString());
            //System.out.printf("rd: %d, rt: %d, rs: %d\n", instr.rd(), instr.rt(), instr.rs());
			instr_new.setRd(instr.rd());
			instr_new.setImmI(0);
			instr_new.setRs(instr.rt());
			instructions.add(instr_new);

			// second: perform computation (not necessary for amoswap)
            if (instr.toMnemonic().matches("amoxor|amoadd")) {
            	if (instr.toMnemonic().equals("amoxor"))
					instr_new = Instructions.instance().getInstruction("xor").clone();
				else
					instr_new = Instructions.instance().getInstruction("add").clone();
				instr_new.setRd(instr.rs());
				instr_new.setRs(instr.rs());
				instr_new.setRt(instr.rd());
				instr_new.setRegNotImm(true);
				instructions.add(instr_new);
			}
			// third: sw instruction to store the computed value
			instr_new = Instructions.instance().getInstruction("sw").clone();
			instr_new.setRt(instr.rs());
			instr_new.setImmS(0);
			instr_new.setRs(instr.rt());
			instructions.add(instr_new);
			// fourth: restore old value in register by performing inverse instruction
			if (instr.toMnemonic().matches("amoxor|amoadd")) {
				if (instr.toMnemonic().equals("amoxor"))
					instr_new = Instructions.instance().getInstruction("xor").clone();
				else
					instr_new = Instructions.instance().getInstruction("sub").clone();
				instr_new.setRd(instr.rs());
				instr_new.setRs(instr.rs());
				instr_new.setRt(instr.rd());
				instr_new.setRegNotImm(true);
				instructions.add(instr_new);
			}
			// Write the partial instructions into memory (apart from the last one, which is written in parse())
			// Remove last partial instruction from list and set instr to it
			instr = instructions.remove(instructions.size()-1);
			for (Instruction part_instr : instructions){
				memory_.writeWord(segmentPointer_.get(), part_instr.instrWord());
				segmentPointer_.add(4);
				updateMemorySegmentEnd();
			}
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[i]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[i]);
		}
	}

	/**
	 * e.g. sub r1,r2,r3
	 * 
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int rType(Token[] tokens) throws ParserException 
	{
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		int i=1;
		try {
			instr.setRd(expect_reg(tokens[i++]));
			expect(tokens[i++], ",");
			instr.setRs(expect_reg(tokens[i++]));
			expect(tokens[i++], ",");
			instr.setRt(expect_reg(tokens[i++]));
			if (i < tokens.length)
				throw new ParserException(UNEXPECTED_TRASH, tokens[i]);
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[i]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[i]);
		}
	}


	/**
	 * mv rd, rs
	 * li rd, 100
	 *
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int mvli(Token[] tokens) throws ParserException {
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		int i=1;
		try {
		    instr.setRd(expect_reg(tokens[i++]));
		    expect(tokens[i++], ",");
			switch (tokens[0].getString()) {
				case "mv":
					Integer value = Registers.instance().getInteger(tokens[i].getString());
					if (value == null)
						throw new ParserException(NOT_A_REGISTER, tokens[i]);
					instr.setRt(value);
					break;
				case "li":
					value = Integer.decode(tokens[i].getString());
					if (value == null)
						throw new ParserException(NOT_A_NUMBER, tokens[i]);
					instr.setImmI(value);
					break;
				default:
					throw new ParserException(UNKNOWN_MNEMONIC, tokens[0]);
			}
			if (i < tokens.length - 1)
				throw new ParserException(UNEXPECTED_TRASH, tokens[i]);
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[i]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[i]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i]);
		}
	}
	/**
	 * jalr gp
	 * jalr ra, gp
	 * jalr ra, 0x10(gp)
	 * jalr 0x10(gp)
	 *
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int iType(Token[] tokens, boolean shift ) throws ParserException {
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		int i=1;

		boolean isJalr = tokens[0].getString().toLowerCase().equals("jalr");
		try {
			Integer firstReg = null;
			int imm = 0;
			// First try the regular syntax, i.e.  mnemonic rd, rs, imm
			try {
				firstReg = expect_reg(tokens[i++]);
				expect(tokens[i++], ",");
				int srcReg = expect_reg(tokens[i++]);
				expect(tokens[i++], ",");
				imm = Integer.decode(tokens[i].getString());
				instr.setRd(firstReg.intValue());
				instr.setRs(srcReg);
				if (shift)
					instr.setShamt(imm);
				else
					instr.setImmI(imm);
				i++;  // Increment separately in case an exception is thrown by decode or setImm/setShamt
			} catch (Exception e) {
				if (!isJalr)
					throw e;
				// Try other syntaxes for jalr
				boolean expectImmFormat = true;
				if (firstReg == null) {
					// jalr imm(reg)
					instr.setRd(1);
					i = 1;
					expectImmFormat = true;
				} else {
					// jalr reg
					if (tokens.length < 3) {
						i = 2;
						instr.setRd(1);
						instr.setRs(firstReg.intValue());
						instr.setImmI(0);
						expectImmFormat = false;
					} else {
						i = 2;
						expect(tokens[i++], ",");

						// Check if token is an imm or a reg
						Integer reg = Registers.instance().getInteger(tokens[i].getString());
						if (reg != null) {
							// jalr rd, rs
							instr.setRd(firstReg.intValue());
							instr.setRs(reg.intValue());
							i++; // Increment for reading the register
							expectImmFormat = false;
						} else {
							//  jalr rd, imm(rs)
							if (tokens[i].getString().equals("(") ||
							   (tokens.length > i+1 && tokens[i+1].getString().equals("("))) {
								//  jalr rd, imm(rs)
								instr.setRd(firstReg.intValue());
								expectImmFormat = true;
							} else {
								// jalr rs, imm
								imm = Integer.decode(tokens[i++].getString());
								instr.setRd(1);
								instr.setRs(firstReg.intValue());
								instr.setImmI(imm);
								expectImmFormat = false;
							}
						}
					}
				}
				if (expectImmFormat) {
					// offset was given
					if (tokens[i].getString().equals("("))
						imm = 0;
					else
						imm = Integer.decode(tokens[i++].getString());
					instr.setImmI(imm);
					expect(tokens[i++], "(");
					instr.setRs(expect_reg(tokens[i++]));
					expect(tokens[i++], ")");
				}
			}

			if (i < tokens.length)
				throw new ParserException(UNEXPECTED_TRASH, tokens[i]);
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[i]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[i]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i]);
		}
	}

	/**
	 * e.g. lui r1, 123
	 *
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int uType(Token[] tokens) throws ParserException {
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		int i=1;
		try {
			instr.setRd(expect_reg(tokens[i++]));
			expect(tokens[i++], ",");
			instr.setImmU(Integer.decode(tokens[i++].getString()));
			if (i < tokens.length)
				throw new ParserException(UNEXPECTED_TRASH, tokens[i]);
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[i]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[i]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i]);
		}
	}

	/**
	 * e.g. sw ra, -16(sp)
	 * 
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int memory(Token[] tokens, boolean store) throws ParserException {
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		int i=1;
		try {
			int reg = expect_reg(tokens[i++]);
			expect(tokens[i++], ",");
			int imm;
			if (tokens[i].getString().equals("(")) {
				imm = 0;
			} else {
				imm = Integer.decode(tokens[i++].getString());
			}
			expect(tokens[i++], "(");
			instr.setRs(expect_reg(tokens[i++]));
			expect(tokens[i], ")");
			if (i + 1 < tokens.length)
				throw new ParserException(UNEXPECTED_TRASH, tokens[i]);
			if (store) {
				instr.setRt(reg);
				instr.setImmS(imm);
			} else {
				instr.setRd(reg);
				instr.setImmI(imm);
			}
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[i]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[i]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i]);
		}
	}

	/**
	 * e.g. beq a1, a0, relative
	 * 
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int bType(Token[] tokens) throws ParserException {
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		int i=1;
		try {
			instr.setRs(expect_reg(tokens[i++]));
			expect(tokens[i++], ",");
			instr.setRt(expect_reg(tokens[i++]));
			expect(tokens[i++], ",");
			instr.setImmB(Integer.decode(tokens[i].getString()) - segmentPointer_.get());
			if (i < tokens.length-1) {
				throw new ParserException(UNEXPECTED_TRASH, tokens[i+1]);
			}
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[i]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[i]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i]);
		}
	}

	/**
	 * jal relative
	 * jal r7, relative
	 * 
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int jType(Token[] tokens) throws ParserException {
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		int i=1;
		try {
			// reg or no reg
			Integer	value = Registers.instance().getInteger(tokens[i].getString());
			if (value==null) {
				// no reg given: instruction defaults to writing into ra (x1)
				instr.setRd(1);
			} else {
				instr.setRd(value);
				i++;
				expect(tokens[i], ",");
				i++;
			}
			instr.setImmJ(Integer.decode(tokens[i].getString()) - segmentPointer_.get());
			if (i < tokens.length - 1) {
				throw new ParserException(UNEXPECTED_TRASH, tokens[i+1]);
			}
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[i]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[i]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i]);
		}
	}

	/**
	 * j relative
	 * 
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int jump(Token[] tokens) throws ParserException {
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		int i=1;
		try {
			instr.setImmJ(Integer.decode(tokens[i].getString()) - segmentPointer_.get());
			if (i < tokens.length - 1) {
				throw new ParserException(UNEXPECTED_TRASH, tokens[i+1]);
			}
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[i]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i]);
		}
	}
	
	/**
	 * e.g. add r1,r2,r3
	 * or   add r1,r2,7
	 * 
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int arith(Token[] tokens, boolean shift) throws ParserException {
		int i=1;
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		try {
			instr.setRd(expect_reg(tokens[i++]));
			expect(tokens[i++], ",");
			instr.setRs(expect_reg(tokens[i++]));
			expect(tokens[i++], ",");
			// rt or imm
			Integer value = Registers.instance().getInteger(tokens[i].getString());
			if (value!=null) {
				instr.setRt(value);
				instr.setRegNotImm(true); // reg
			} else {
				value = Integer.decode(tokens[i].getString());
				if (value==null)
					throw new ParserException(REG_OR_IMM, tokens[i]);
				if (shift) instr.setShamt(value);
				else instr.setImmI(value);
				instr.setRegNotImm(false); // imm
			}
			if (i < tokens.length - 1) {
				throw new ParserException(UNEXPECTED_TRASH, tokens[++i]);
			}
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[i]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[i]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i]);
		}
	}
	
	/**
	 * rdcycle, rdtime, rdinstret
	 * 
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int oneReg(Token[] tokens) throws ParserException 
	{
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		try {
			instr.setRd(expect_reg(tokens[1]));
			if (tokens.length > 2)
				throw new ParserException(UNEXPECTED_TRASH, tokens[1]);
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[1]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[1]);
		}
	}

	/**
	 * jr ra
	 * 
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int srcReg(Token[] tokens) throws ParserException 
	{
		Instruction instr = Instructions.instance().getInstruction(tokens[0].getString()).clone();
		try {
			instr.setRs(expect_reg(tokens[1]));
			if (tokens.length > 2)
				throw new ParserException(UNEXPECTED_TRASH, tokens[1]);
			return instr.instrWord();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		} catch (NullPointerException ex) {
			throw new ParserException(NOT_A_REGISTER, tokens[1]);
		} catch (InstructionException ex) {
			throw new ParserException(INSTRUCTION_EXCEPTION + ex.getMessage(), tokens[1]);
		}
	}
	
	/**
	 * fence, fence.i, scall, sbreak
	 * 
	 * @param tokens
	 * @return
	 * @throws ParserException
	 */
	private int noArgs(Token[] tokens) throws ParserException {
		if (tokens.length > 1)
			throw new ParserException(UNEXPECTED_TRASH, tokens[1]);
		return Instructions.instance().getInstruction(tokens[0].getString()).instrWord();
	}
	
	
	/*
	 * =============================* Directives *=============================
	 */
	/**
	 * .align .ascii .asciiz .byte .data .global .half .space .text .word
	 * 
	 * @param tokens
	 * @throws ParserException
	 */
	private void parseDirectives(Token[] tokens) throws ParserException {

		String name = tokens[0].getString();
		//unresolved labels
		Token t = resolveLabels(tokens);
		if (t != null) {
			if (stopOnUnresolvedLabel == true)
				throw new ParserException(LABEL_DOES_NOT_EXISTS, t);
			unresolvedInstructions_.add(new UnresolvedInstruction(tokens, segmentPointer_.get(),
					segmentPointer_ == textPointer_ ? true : false));
			if (name.equalsIgnoreCase(".word")) {
				segmentPointer_.add(4);
			} else if (name.equalsIgnoreCase(".global") || name.equalsIgnoreCase(".globl")) {
				//do nothing
			} else {
				throw new ParserException(LABEL_NOT_ALLOWED_HERE, t);
			}
			return;
		}

		if (name.equalsIgnoreCase(".align")) {
			align(tokens);
		} else if (name.equalsIgnoreCase(".ascii")) {
			ascii(tokens);
		} else if (name.equalsIgnoreCase(".asciiz")) {
			ascii(tokens);
			memory_.writeByte(segmentPointer_.get(), (byte) 0x0);//terminating null
			segmentPointer_.add(1);
			updateMemorySegmentEnd();
		} else if (name.equalsIgnoreCase(".byte")) {
			byteDir(tokens);
		} else if (name.equalsIgnoreCase(".data")) {
			data(tokens);
		} else if (name.equalsIgnoreCase(".global") || name.equalsIgnoreCase(".globl")) {
			global(tokens);
		} else if (name.equalsIgnoreCase(".half")) {
			half(tokens);
		} else if (name.equalsIgnoreCase(".space")) {
			space(tokens);
		} else if (name.equalsIgnoreCase(".text")) {
			text(tokens);
		} else if (name.equalsIgnoreCase(".word")) {
			word(tokens);
		} else {
			throw new ParserException(UNKNOWN_DIRECTIVE, null);
		}
	}

	/**
	 * e.g. .align 2
	 * 
	 * @param tokens
	 */
	private void align(Token[] tokens) throws ParserException {
		try {
			int align = Integer.decode(tokens[1].getString());
			if (align >= 0 && align < 32)
				align = 1 << align;
			else
				throw new ParserException(NUMBER_TOO_BIG, tokens[1]);
			while (segmentPointer_.get() % align != 0)
				segmentPointer_.add(1);
			updateMemorySegmentEnd();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_DIRECTIVE, tokens[0]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[1]);
		}
	}

	/**
	 * e.g. .ascii "Foo\n"
	 * 
	 * @param tokens
	 * @throws ParserException
	 */
	private void ascii(Token[] tokens) throws ParserException {
		try {
			if (tokens[1].getTokenType() != TokenType.StringLiteral &&
					tokens[1].getTokenType() != TokenType.CharacterLiteral)
					throw new ParserException(NOT_A_CHAR_OR_CHAR_ARRAY, tokens[1]);
			byte[] str = replaceEscapeSequences(tokens[1].getString()).getBytes();
			for (int i = 0; i < str.length; ++i) {
				memory_.writeByte(segmentPointer_.get(), str[i]);
				segmentPointer_.add(1);
				updateMemorySegmentEnd();
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_DIRECTIVE, tokens[0]);
		} catch(ParserException ex) {
			ex.setToken(tokens[1]);
			throw ex;
		}
	}

	/**
	 * e.g. .byte 1,2,3
	 * 
	 * @param tokens
	 * @throws ParserException
	 */
	private void byteDir(Token[] tokens) throws ParserException {
		int i = 0;
		try {
			i = 1;
			do {
				int value = Integer.decode(tokens[i++].getString());
				memory_.writeByte(segmentPointer_.get(), (byte) value);
				segmentPointer_.add(1);
				updateMemorySegmentEnd();
			} while (i < tokens.length && tokens[i++].getString().equals(","));
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_DIRECTIVE, tokens[0]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i - 1]);
		}
	}

	/**
	 * e.g. .data 0x1000
	 * 
	 * @param tokens
	 * @throws ParserException
	 */
	private void data(Token[] tokens) throws ParserException {
		segmentPointer_ = dataPointer_;
		int startAddr;
		if (tokens.length == 1) {
		    while (segmentPointer_.get() % 4 != 0) // align segment
		    	segmentPointer_.add(1);
		    	startAddr = segmentPointer_.get();
		} else if (tokens.length == 2) {
			try {
				startAddr = ValueInput.strToInt(tokens[1].getString());
				if (startAddr < 0)
					throw new ParserException(NUMBER_NEGATIVE, tokens[1]);
				dataPointer_.set(startAddr);
			} catch (NumberFormatException ex) {
				throw new ParserException(NOT_A_NUMBER, tokens[1]);
			}
		} else {
			throw new ParserException(UNEXPECTED_TRASH, tokens[2]);
		}
		memory_.setDataBegin(startAddr);
	}

	/**
	 * e.g. .global Label
	 * 
	 * @param tokens
	 * @throws ParserException
	 */
	private void global(Token[] tokens) throws ParserException {
		//TODO or not to do that's the question
		try {
			if (Integer.decode(tokens[1].getString()).intValue() == globalLabels_.get("main")) {
				hasGlobalMain = true;
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_INSTRUCTION, tokens[0]);
		}
	}

	/**
	 * e.g. .half 1,2,3
	 * 
	 * @param tokens
	 * @throws ParserException
	 */
	private void half(Token[] tokens) throws ParserException {
		int i = 0;
		try {
			i = 1;
			do {
				int value = Integer.decode(tokens[i++].getString());
				memory_.writeHalf(segmentPointer_.get(), (short) value);
				segmentPointer_.add(2);
				updateMemorySegmentEnd();
			} while (i < tokens.length && tokens[i++].getString().equals(","));
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_DIRECTIVE, tokens[0]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i - 1]);
		}
	}

	/**
	 * e.g. .space 4,2,2
	 * 
	 * @param tokens
	 * @throws ParserException
	 */
	private void space(Token[] tokens) throws ParserException {
		int i = 0;
		try {
			i = 1;
			do {
				int value = ValueInput.strToInt(tokens[i++].getString());
				segmentPointer_.add(value);
			} while (i < tokens.length && tokens[i++].getString().equals(","));
			if (segmentPointer_ == dataPointer_)
			    updateMemorySegmentEnd();
			else if (segmentPointer_ == textPointer_)
			    updateMemorySegmentEnd();
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_DIRECTIVE, tokens[0]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i - 1]);
		}
	}

	/**
	 * e.g. .text 0x100
	 * 
	 * @param tokens
	 * @throws ParserException
	 */
	private void text(Token[] tokens) throws ParserException {
		segmentPointer_ = textPointer_;
		int startAddr;
		if (tokens.length == 1) {
			while (segmentPointer_.get() % 4 != 0) // align segment
				segmentPointer_.add(1);
			startAddr = segmentPointer_.get();
		} else if (tokens.length == 2) {
			try {
				startAddr = ValueInput.strToInt(tokens[1].getString());
				if (startAddr < 0)
					throw new ParserException(NUMBER_NEGATIVE, tokens[1]);
				textPointer_.set(startAddr);
			} catch (NumberFormatException ex) {
				throw new ParserException(NOT_A_NUMBER, tokens[1]);
			}
		} else {
			throw new ParserException(UNEXPECTED_TRASH, tokens[2]);
		}
		memory_.setTextBegin(startAddr);
	}

	/**
	 * e.g. .word 1,2,3
	 * 
	 * @param tokens
	 * @throws ParserException
	 */
	private void word(Token[] tokens) throws ParserException {
		int i = 0;
		try {
			i = 1;
			do {
				int value = ValueInput.strToInt(tokens[i++].getString());
				memory_.writeWord(segmentPointer_.get(), value);
				segmentPointer_.add(4);
				updateMemorySegmentEnd();
			} while (i < tokens.length && tokens[i++].getString().equals(","));
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new ParserException(INCOMPLETE_DIRECTIVE, tokens[0]);
		} catch (NumberFormatException ex) {
			throw new ParserException(NOT_A_NUMBER, tokens[i - 1]);
		}
	}


	private void updateMemorySegmentEnd() {
		if (segmentPointer_ == textPointer_)
			memory_.setTextEnd(segmentPointer_.get());
		else
			memory_.setDataEnd(segmentPointer_.get());
	}
	/*
	 * ========================================================================
	 */
	private String replaceEscapeSequences(String str) throws ParserException {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			if (c != '\\')
				buffer.append(c);
			else {
				try {
					++i;
					switch (str.charAt(i)) {
					case '0':
						buffer.append('\0');
						break;
					case 'n':
						buffer.append('\n');
						break;
					case 't':
						buffer.append('\t');
						break;
					case 'f':
						buffer.append('\f');
						break;
					case 'b':
						buffer.append('\b');
						break;
					case '\\':
						buffer.append('\\');
						break;
					case '\0':
						buffer.append('\0');
						break;
					case '\'':
						buffer.append('\'');
						break;
					case '\"':
						buffer.append('\"');
						break;
					case 'x':
						++i;
						char x1 = str.charAt(i);
						++i;
						char x2 = str.charAt(i);
						int hex = Integer.decode("0x" + x1 + x2);
						buffer.append((char) hex);
						break;
					default:
						buffer.append(c);
						buffer.append(str.charAt(i));
					}
				} catch (IndexOutOfBoundsException ex) {
					throw new ParserException(UNEXPECTED_LITERAL_END, null);
				} catch (NumberFormatException ex) {
					throw new ParserException(NOT_A_NUMBER, null);
				}
			}
		}
		return buffer.toString();
	}
}
