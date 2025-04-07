package riscVivid.exception;

import riscVivid.datatypes.uint32;

@SuppressWarnings("serial")
public class UnreservedMemoryAccessException extends MemoryException {
   public enum Stage {
       FETCH,
       MEMORY
   }
   public enum  Area {
       INSTRUCTION,
       DATA,
       NONE
   }
   private final uint32 address;
   private final int nBytes;
   private Area area = Area.NONE;
   private Stage stage;

    public UnreservedMemoryAccessException(uint32 address, int nBytes, Stage stage, uint32 instrAddress) {
        super("", instrAddress, false); // nonfatal exception
        this.address = address;
        this.nBytes = nBytes;
        this.stage = stage;
    }

    public void setArea(Area area)  throws IllegalArgumentException {
        if ((this.stage == Stage.MEMORY && area == Area.DATA) ||
                (this.stage == Stage.FETCH && area == Area.INSTRUCTION)) {
            throw new IllegalArgumentException("the access couldn't have been unreserved");
        }
        this.area = area;
    }

    public uint32 getAddress() {
        return this.address;
    }
    public int getNBytes() {
        return this.nBytes;
    }

    @Override
    public String getMessage() {
        String prefix = "In " + (this.stage == Stage.FETCH ? "IF" : "MEM") + "-stage: ";
        switch(this.area) {
            case INSTRUCTION:
                return prefix + "Accessing instruction memory at " + address.getValueAsHexString();
            case DATA:
                return prefix + "Reading instruction from data memory at " + address.getValueAsHexString();
            case NONE:
            default:
                return prefix + "Accessing " + String.valueOf(nBytes) +
                        " bytes of unreserved memory at " + address.getValueAsHexString()
                    + "\n This warning can be disabled via Simulator->Options (not recommended)";
        }
    }
}
