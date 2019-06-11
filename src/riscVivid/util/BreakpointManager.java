package riscVivid.util;

import java.util.ArrayList;
import java.util.Observable;

import riscVivid.datatypes.uint32;
import riscVivid.gui.GUI_CONST.OpenDLXSimState;
import riscVivid.gui.MainFrame;

public class BreakpointManager extends Observable {
    private static BreakpointManager instance;
    
    private ArrayList<Breakpoint> breakpoints = new ArrayList<Breakpoint>();
    private Breakpoint BpStoppedAt = null;
    
    private BreakpointManager() {
        
    }
    
    public static BreakpointManager getInstance() {
        return instance == null ? new BreakpointManager() : instance;
    }
    
    public boolean isBreakpoint(uint32 address) {
        // simple linear search
        return getBreakpoint(address) != null;
    }
    
    public boolean isBreakpoint(int lineInEditor) {
        return breakpoints.contains(new Breakpoint(lineInEditor));
        /*for(int idx = 0; idx < breakpoints.size(); ++idx) {
            if (breakpoints.get(idx).lineInEditor == lineInEditor)
                return true;
        }
        return false;*/
    }
    
    /*public void setStoppedOnBreakpoint(uint32 address, boolean stopped) {
        Breakpoint BpToModify = getBreakpoint(address);
        if (BpToModify == null)
            throw new IllegalArgumentException("There's no breakpoint at " + address.getValueAsHexString());
        if (stopped)
            BpStoppedAt = BpToModify;
        else {
            if (BpStoppedAt.equals(BpToModify))
                BpStoppedAt = null;
            else
                throw new IllegalArgumentException("Execution isn't stopped at " + address.getValueAsHexString());
        }
        this.setChanged();
        this.notifyObservers();
    }*/
    
    /*
     * @return if execution isn't stopped, -1, otherwise the line of the breakpoint at the stop
     */
    public int getLineStoppedAt() {
        // if Simulator isn't stopped and there's no breakpoint there
        if (BpStoppedAt == null || !MainFrame.getInstance().isExecuting())
            return -1;
        return BpStoppedAt.lineInEditor;
    }
    /*
     * @return false if line is already a breakpoint; true if successfully added
     * adds breakpoint; sorted by line number
     */
    public boolean addBreakpoint(int lineInEditor) {
        System.out.println("adding breakpoint at " + lineInEditor);
        Breakpoint breakToInsert = new Breakpoint(lineInEditor);
        if (breakpoints.size() == 0) {
            breakpoints.add(breakToInsert);
            return true;
        }
        else 
        {
            for (int idx = 0; idx < breakpoints.size(); ++idx) {
                if (breakpoints.get(idx).lineInEditor >= lineInEditor) {
                    if (breakpoints.get(idx).equals(breakToInsert)) {
                        return false;
                    }
                    breakpoints.add(idx, breakToInsert);
                    return true;
                }
            }
            // at this point, the new breakpoint wasnt added to the list yet -> add to the end
            breakpoints.add(breakToInsert);
            return true;
        }
    }
    
    /**
     * @return false if line isnt a breakpoint, true if successfully removed
     */
    public boolean removeBreakpoint(int lineInEditor) {
        System.out.println("removing breakpoint at " + lineInEditor);
        int idx = breakpoints.indexOf(new Breakpoint(lineInEditor));
        if (idx >= 0) {
            breakpoints.remove(idx);
            return true;
        } else {
            return false;
        }
    }
    
    public void shiftBreakpointsBelowLine(int lineInEditor, int linesToShift) {
        for (Breakpoint bp : breakpoints) {
            if (bp.lineInEditor >= lineInEditor) {
                bp.lineInEditor += linesToShift;
            }
        }
    }
    
    private Breakpoint getBreakpoint(uint32 address) {
        for(int idx = 0; idx < breakpoints.size(); ++idx) {
            if (breakpoints.get(idx).address.equals(address))
                return breakpoints.get(idx);
        }
        return null;
    }

    
    public class Breakpoint {
        private uint32 address = new uint32(0);
        private int lineInEditor;
        
        public Breakpoint(int lineInEditor) {
            this.lineInEditor = lineInEditor;
        }
        
        public void setAddress(uint32 address) {
            if (address.getValue() % 4 != 0)
                throw new IllegalArgumentException("address of text must be aligned");
            this.address = address;
        }
        
        @Override
        public boolean equals(Object p) {
            if (p.getClass() != this.getClass())
                return false;
            return ((Breakpoint)p).lineInEditor == this.lineInEditor;
        }
    }
}
