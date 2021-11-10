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

import java.util.Properties;

import riscVivid.BranchPredictionModule;
import riscVivid.asm.instruction.Registers;
import riscVivid.gui.Preference;

import static riscVivid.datatypes.BranchPredictorType.UNKNOWN;

public class ArchCfg
{

    private static ISAType isa_type = stringToISAType(Preference.pref.get(Preference.isaTypePreferenceKey, "DLX"));

    // forwarding implies the two boolean: use_forwarding and use_load_stall_bubble
    private static boolean use_forwarding = Preference.pref.getBoolean(Preference.forwardingPreferenceKey, true);

    // determines whether to stall the following instruction if it depends on a preceding load instruction;
    // is equivalent to mipsCompatibility
    private static boolean  use_load_stall_bubble = use_forwarding ?
            Preference.pref.getBoolean(Preference.mipsCompatibilityPreferenceKey, true) : false;

    private static boolean  no_branch_delay_slot = Preference.pref.getBoolean(Preference.noBranchDelaySlotPreferenceKey, true);

    // num_branch_delay_slots must bei either 2 or 3
    private static int num_branch_delay_slots = Preference.pref.getInt(Preference.numBranchDelaySlotsPreferenceKey, 3);
/*
    public static final String[] GP_NAMES_MIPS =
    {
        "ze", "at", "v0", "v1", "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "t4", "t5", "t6", "t7",
        "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "t8", "t9", "k0", "k1", "gp", "sp", "s8", "ra"
    };
    public static final String[] GP_NAMES_DLX =
    {
        "r0 ", "r1 ", "r2 ", "r3 ", "r4 ", "r5 ", "r6 ", "r7 ", "r8 ", "r9 ", "r10", "r11", "r12", "r13", "r14", "r15",
        "r16", "r17", "r18", "r19", "r20", "r21", "r22", "r23", "r24", "r25", "r26", "r27", "r28", "r29", "r30", "r31"
    };
*/

    private static BranchPredictorType branch_predictor_type =
            getBranchPredictorTypeFromString(Preference.pref.get(Preference.bpTypePreferenceKey, ""));
    private static BranchPredictorState branch_predictor_initial_state =
           getBranchPredictorInitialStateFromString(
                    Preference.pref.get(Preference.bpInitialStatePreferenceKey, ""));
    private static int branch_predictor_table_size = Preference.pref.getInt(
            Preference.btbSizePreferenceKey, 1);

    private static int max_cycles = Preference.pref.getInt(Preference.maxCyclesPreferenceKey, 1000);

    public static void registerArchitectureConfig(Properties config)
    {
        if (!getUseForwardingCfg(config) && getUseLoadStallBubble(config))
            throw new IllegalArgumentException("Error in config: forwarding must be enabled if use_load_stall_bubble is enabled");
        ArchCfg.isa_type = stringToISAType(config.getProperty("isa_type"));
        ArchCfg.use_forwarding = getUseForwardingCfg(config);
        ArchCfg.use_load_stall_bubble = getUseLoadStallBubble(config);
        ArchCfg.no_branch_delay_slot = getNoBranchDelaySlot(config);
        ArchCfg.num_branch_delay_slots = getNumBranchDelaySlots(config);
        ArchCfg.branch_predictor_initial_state = getBranchPredictorInitialState(config);
        ArchCfg.branch_predictor_table_size = getBranchPredictorTableSize(config);
        ArchCfg.branch_predictor_type = getBranchPredictorType(config);
        ArchCfg.max_cycles = getMaxCycles(config);
    }


    private static int getBranchPredictorTableSize(Properties config) {
        String str = config.getProperty("btb_size");
        try {
            if (str != null && str.length() > 0)
                return Integer.decode(str);
        } catch (Exception e) { }

        return ArchCfg.branch_predictor_table_size;  // return already set value
    }

    private static BranchPredictorType getBranchPredictorType(Properties config) {
        String str = config.getProperty("btb_predictor");
        if (str == null)
            str = "";
        return getBranchPredictorTypeFromString(str);
    }

    public static BranchPredictorType getBranchPredictorTypeFromString(String str) {
        BranchPredictorType type = UNKNOWN; // default: UNKNOWN
        for (BranchPredictorType bpt : BranchPredictorType.values()) {
            if (str.equalsIgnoreCase(bpt.toString())) {
                type = bpt;
                break;
            }
        }
        return type;
    }

    public static BranchPredictorType getBranchPredictorTypeFromGuiString(String str) {
        BranchPredictorType type = UNKNOWN;
        for (BranchPredictorType bpt : BranchPredictorType.values()) {
            if (str.equalsIgnoreCase(bpt.toGuiString())) {
                type = bpt;
                break;
            }
        }
        return type;
    }

    private static BranchPredictorState getBranchPredictorInitialState(Properties config) {
        String str = config.getProperty("btb_predictor_initial_state");
        if (str == null)
            str = "";
        BranchPredictorState state = BranchPredictorState.UNKNOWN; // default: UNKNOWN
        for (BranchPredictorState bps : BranchPredictorState.values()) {
            if (str.equalsIgnoreCase(bps.toString())) {
                state = bps;
                break;
            }
        }
        return state;
    }

    public static BranchPredictorState getBranchPredictorInitialStateFromString(String str) {
        BranchPredictorState state = BranchPredictorState.UNKNOWN; // default: UNKNOWN
        for (BranchPredictorState bps : BranchPredictorState.values()) {
            if (str.equalsIgnoreCase(bps.toString())) {
                state = bps;
                break;
            }
        }
        return state;
    }

    public static BranchPredictorState getBranchPredictorInitialStateFromGuiString(String str) {
        BranchPredictorState state = BranchPredictorState.UNKNOWN; // default: UNKNOWN
        for (BranchPredictorState bps : BranchPredictorState.values()) {
            if (str.equalsIgnoreCase(bps.toGuiString())) {
                state = bps;
                break;
            }
        }
        return state;
    }

    private static int getMaxCycles(Properties config) {
        String str = config.getProperty("max_cycles");
        try {
           if (str != null && str.length() > 0)
               return Integer.decode(str);
        } catch (Exception e) { }

        return ArchCfg.max_cycles;  // return already set value
    }

    public static ISAType stringToISAType(String s)
    {
        if (s.compareTo("MIPS") == 0)
        {
            return ISAType.MIPS;
        }
        else if (s.compareTo("DLX") == 0)
        {
            return ISAType.DLX;
        }

        return ISAType.UNKNOWN_ISA;
    }

    public static ISAType getISAType() {
        return isa_type;
    }
    public static boolean useForwarding() {
        return use_forwarding;
    }
    public static boolean useLoadStallBubble() {
        return use_load_stall_bubble;
    }
    public static boolean ignoreBranchDelaySlots() {
        return no_branch_delay_slot;
    }
    public static int getNumBranchDelaySlots() {
        return num_branch_delay_slots;
    }
    public static BranchPredictorType getBranchPredictorType() {
        return branch_predictor_type;
    }
    public static BranchPredictorState getBranchPredictorInitialState() {
        return branch_predictor_initial_state;
    }
    public static int getBranchPredictorTableSize() {
        return branch_predictor_table_size;
    }
    public static int getMaxCycles() {
        return max_cycles;
    }



    private static boolean getUseForwardingCfg(Properties config)
    {
        if (ArchCfg.isa_type == ISAType.MIPS)
        {
            return true;
        }
        else if (ArchCfg.isa_type == ISAType.DLX)
        {
            if ((((config.getProperty("use_forwarding", "false")).toLowerCase()).compareTo("true") == 0)
                    || ((config.getProperty("use_forwarding", "false")).compareTo("1") == 0))
            {
                return true;
            }
            else
            {
                return false;
            }

        }
        return true;
    }

    private static boolean getUseLoadStallBubble(Properties config)
    {
        if (ArchCfg.isa_type == ISAType.MIPS)
        {
            return true;
        }
        else if (ArchCfg.isa_type == ISAType.DLX)
        {
            if ((((config.getProperty("use_load_stall_bubble", "false")).toLowerCase()).compareTo("true") == 0)
                    || ((config.getProperty("use_load_stall_bubble", "false")).compareTo("1") == 0))
            {
                return true;
            }
            else
            {
                return false;
            }

        }
        return true;
    }

    private static boolean getNoBranchDelaySlot(Properties config)
    {
    	if ((((config.getProperty("no_branch_delay_slot", "false")).toLowerCase()).compareTo("true") == 0)
    		|| ((config.getProperty("no_branch_delay_slot", "false")).compareTo("1") == 0))
    			return true;
        return false;
    }

    private static int getNumBranchDelaySlots(Properties config)
    {
        int n = Integer.decode(config.getProperty("num_branch_delay_slots", "3"));
        if (n != 2 && n != 3) {
            throw new IllegalArgumentException("Error in config: num_branch_delay_slots must be either 2 or 3");
        }
        return n;
    }
    
    public static String getRegisterDescription(int reg_id)
    {
/*
    	if (isa_type == ISAType.MIPS)
        {
            return GP_NAMES_MIPS[reg_id];
        }
        else if (isa_type == ISAType.DLX)
        {
            return GP_NAMES_DLX[reg_id];
        }

        return "-";
*/
    	return reg_id + ": " + Registers.RegNames[reg_id];
    }

    public static int getRegisterCount()
    {
/*
        if (isa_type == ISAType.MIPS)
        {
            return GP_NAMES_MIPS.length;
        }
        else if (isa_type == ISAType.DLX)
        {
            return GP_NAMES_DLX.length;
        }
        return 0;
*/
    	return 32;
    }

}
