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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import riscVivid.asm.instruction.Instruction;
import riscVivid.asm.instruction.Instructions;
import riscVivid.asm.parser.Parser;
import riscVivid.asm.parser.UnresolvedInstruction;
import riscVivid.util.BreakpointManager;

public class DLXAssembler implements AssemblerInterface {
	private static int defaultDataStart = 0x100;
	private static int defaultTextStart = 0x4000;
	private static int defaultMemorySize = 0x5000;

	private Parser parser;
	private Hashtable<String, Integer> globalLabels;

	public DLXAssembler() {
	}

	public MemoryBuffer assemble(BufferedReader stream) throws AssemblerException, IOException {
		BufferedReader[] streams = { stream };
		return assemble(streams);
	}

	public MemoryBuffer assemble(BufferedReader[] streams) throws IOException, AssemblerException {
		MemoryBuffer memory = new MemoryBuffer(defaultMemorySize);
		memory.setDataBegin(defaultDataStart);
		memory.setTextBegin(defaultTextStart);
		memory.setLittleEndian(true);
		parser = new Parser(defaultDataStart, defaultTextStart);
		globalLabels = new Hashtable<String, Integer>();

		List<UnresolvedInstruction> unresolvedInstructions = new ArrayList<UnresolvedInstruction>();
		for (BufferedReader reader : streams) {
			unresolvedInstructions.addAll(parser.parse(reader, globalLabels, memory));
		}
		parser.resolve(unresolvedInstructions, globalLabels, memory);
		Integer entryPoint = globalLabels.get("main");
		if (entryPoint != null)
			memory.setEntryPoint(entryPoint);
		else
			throw new AssemblerException("no entry point 'main' found!\nPlease specify 'main:'.");

		BreakpointManager.getInstance().setAddressToLineTable(parser.getAddressToLineTable());

		return memory;
	}

	public String Instr2Str(int pc, int instr) {
		Instruction i = new Instruction(instr);
		return i.toString(pc);
	}

	public String InstrDescription(String mnem) {
		return Instructions.instance().getDescription(mnem);
	}

	public Hashtable<String, Integer> getLabels() {
		return globalLabels;
	}

}
