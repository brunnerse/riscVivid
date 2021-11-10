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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import riscVivid.asm.instruction.Instruction;
import riscVivid.datatypes.ArchCfg;
import riscVivid.datatypes.BranchPredictorType;

public class AsmFileLoader
{

    private String path = null;
    private static final String BINARY = "_riscVivivFile.bin";
    private static final String CONFIG = "_riscVividFile.cfg";
    private static final boolean printDebugOutput = false;

    public AsmFileLoader(String path)
    {
        this.path = path;
    }

    public File createConfigFile() throws Exception
    {
        MemoryBuffer memory = assembleFromFile();
        if(printDebugOutput)
        {
        	printToStdOut(memory);
        }
        return saveToFile(memory);
    }

    private MemoryBuffer assembleFromFile() throws Exception
    {
        DLXAssembler asm = new DLXAssembler();
        MemoryBuffer mb = asm.assemble(new BufferedReader(new FileReader(new File(path))));
        Labels.labels = asm.getLabels();
        return mb;
    }

    private void printToStdOut(MemoryBuffer memory)
    {
        System.out.println(memory);
        int WRAPPING = 0x10;
        for (int i = memory.getEntryPoint(); i < memory.size(); i += 4)
        {
            if (i % WRAPPING == 0)
            {
                System.out.println();
            }
            Instruction instr = new Instruction(memory.readWord(i));
            //System.out.print(" (" + instr.toHexString() + ")");
            //System.out.print(instr + "\t\t");
            System.out.printf("%1$-16s", instr.toString());
        }
        System.out.println();
    }

    private File saveToFile(MemoryBuffer memory) throws Exception
    {
        //binary        
        String parts = path.substring(0, path.lastIndexOf(46));
        System.out.println(parts);
        FileOutputStream binOut = new FileOutputStream(new File(parts + BINARY));
        binOut.write(memory.toByteArray());
        binOut.close();
        //cfg
        File configFile = new File(parts + CONFIG);
        PrintWriter textOut = new PrintWriter(new FileWriter(configFile));
        textOut.println("file=" + parts + BINARY);
        textOut.println("code_start_addr=0x0");
        textOut.println("entry_point=0x" + Integer.toHexString(memory.getEntryPoint()));
        for (int i = 0; i < memory.getNumTextSegments(); ++i) {
            textOut.println("text_begin_" + i + "=0x" + Integer.toHexString(memory.getTextBegin(i)));
            textOut.println("text_end_" + i + "=0x" + Integer.toHexString(memory.getTextEnd(i)));
        }
        for (int i = 0; i < memory.getNumDataSegments(); ++i) {
            textOut.println("data_begin_" + i + "=0x" + Integer.toHexString(memory.getDataBegin(i)));
            textOut.println("data_end_" + i + "=0x" + Integer.toHexString(memory.getDataEnd(i)));
        }
        textOut.println("print_file=" + parts + ".out");
        textOut.println("log_file=" + parts + ".log");
        textOut.println("log4j=log4j.properties");
        textOut.println("isa_type=" + ArchCfg.getISAType());
        textOut.println("use_forwarding=" + ArchCfg.useForwarding());
        textOut.println("use_load_stall_bubble=" + ArchCfg.useLoadStallBubble());
        textOut.println("no_branch_delay_slot=" + ArchCfg.ignoreBranchDelaySlots());
        textOut.println("num_branch_delay_slots=" + ArchCfg.getNumBranchDelaySlots());
        if(ArchCfg.getBranchPredictorType() != BranchPredictorType.UNKNOWN)
        {
        	textOut.println("btb_predictor=" + ArchCfg.getBranchPredictorType());
        	textOut.println("btb_predictor_initial_state=" + ArchCfg.getBranchPredictorInitialState());
        	textOut.println("btb_size=" + ArchCfg.getBranchPredictorTableSize());
        }
        textOut.println("cycles=" + ArchCfg.getMaxCycles());
        textOut.close();
        return configFile;
    }
}
