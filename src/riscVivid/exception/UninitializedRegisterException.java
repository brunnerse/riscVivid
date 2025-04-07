package riscVivid.exception;

import riscVivid.asm.instruction.Registers;
import riscVivid.datatypes.uint32;
import riscVivid.datatypes.uint8;

@SuppressWarnings("serial")
public class UninitializedRegisterException extends PipelineException {
    
    /** 
     * for reading uninitialized registers
     * @param register
     */
    public UninitializedRegisterException(uint8 register, uint32 instrAddress) {
        super("Reading from uninitialized register " + Registers.instance().getString(((Byte)register.getValue()).intValue())
                + "\n This warning can be disabled via Simulator->Options (not recommended)",
                instrAddress, false);
        // exception is nonfatal
    }
}
