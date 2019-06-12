package riscVivid.exception;

import riscVivid.datatypes.uint32;

@SuppressWarnings("serial")
public class UnreservedMemoryAccessException extends MemoryException {
    public UnreservedMemoryAccessException(uint32 address, int nBytes ) {
        super("Accessing " + String.valueOf(nBytes) + " bytes of unreserved memory at " + address.getValueAsHexString());
        this.isFatal = false;
    }
}