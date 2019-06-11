package riscVivid.exception;

import riscVivid.datatypes.uint32;

@SuppressWarnings("serial")
public class UnreservedMemoryAccessException extends MemoryException {

   public enum  Area {
       INSTRUCTION,
       DATA,
       NONE
   }
   private final uint32 address;
   private final int nBytes;
   private Area area = Area.NONE;

    public UnreservedMemoryAccessException(uint32 address, int nBytes) {
        super("", false); // nonfatal exception
        this.address = address;
        this.nBytes = nBytes;
    }

    public void setArea(Area area)  throws IllegalArgumentException {
        if (area == Area.DATA) {
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
        if (this.area == Area.NONE) {
            return "Accessing " + String.valueOf(nBytes) + " bytes of unreserved memory at " + address.getValueAsHexString();
        } else {
            return "Accessing instruction memory at" + address.getValueAsHexString();
        }
    }
}