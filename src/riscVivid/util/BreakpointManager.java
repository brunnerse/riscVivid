package riscVivid.util;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;

import riscVivid.asm.tokenizer.TokenType;
import riscVivid.asm.tokenizer.Token;
import riscVivid.asm.tokenizer.Tokenizer;
import riscVivid.asm.tokenizer.TokenizerException;
import riscVivid.datatypes.uint32;

public class BreakpointManager implements ItemSelectable {
    
    private static BreakpointManager instance = null;
    
    private ArrayList<Breakpoint> breakpoints = new ArrayList<Breakpoint>();
    private Hashtable<Integer, Integer> lineToAddressTable = new Hashtable<Integer, Integer>();
    
    private ArrayList<ItemListener> listeners = new ArrayList<ItemListener>();
    
    private BreakpointManager() {
    }
    
    public static BreakpointManager getInstance() {
        if (instance == null)
           instance = new BreakpointManager();
        return instance;
    }
    
    public boolean isBreakpoint(uint32 address) {
        // simple linear search
        return getBreakpointIndex(address) >= 0;
    }
    

    public boolean isBreakpoint(int lineInEditor) {
        return getBreakpointIndex(lineInEditor) >= 0;
    }
    /**
     * checks if line starts with a mnemonic (ignoring labels)
     * @param line
     * @return
     */
    private static boolean isValidBreakpoint(String line) {
        Tokenizer t = new Tokenizer();
        try {
            t.setReader(new BufferedReader(new StringReader(line)));
            Token[] tokens = t.readLine();
            int idx = 0;
            while (idx < tokens.length) {
                if (tokens[idx].getTokenType() == TokenType.Mnemonic)
                    return true;
                else if (tokens[idx].getTokenType() == TokenType.Label)
                    idx++;
                else
                    return false;
            }
            // End of tokens reached and no menmonic
            return false;
        } catch (IOException e) {
            return true;
        } catch (TokenizerException e) {
            return false;
        }
    }
    
    public void setLineToAddressTable(Hashtable<Integer, Integer> lineToAddressTable) {
        this.lineToAddressTable = lineToAddressTable;
        System.out.print("resolving breakpoints: ");
        for (Breakpoint p : breakpoints) {
            p.resolveAddress();
            System.out.print("line " + p.lineInEditor + " to " + p.address.getValueAsHexString() + ", \t");
        }
        System.out.println();
    }

    /*
     * @return false if line isn't a valid breakpoint (isn't a instruction),
     * true if successfully added or is already a breakpoint
     */
    public boolean addBreakpoint(int lineInEditor, String lineText) {
        if (!isValidBreakpoint(lineText))
            return false;
        Breakpoint breakToInsert = new Breakpoint(lineInEditor);
        if (breakpoints.size() == 0) {
            breakpoints.add(breakToInsert);
        }
        else 
        {
            int idx = 0;
            for (; idx < breakpoints.size(); ++idx) {
                if (breakpoints.get(idx).lineInEditor >= lineInEditor) {
                    if (breakpoints.get(idx).lineInEditor != lineInEditor) {
                        breakpoints.add(idx, breakToInsert);
                    }
                    break;
                }
            }
            if (idx == breakpoints.size())// if new breakpoint wasnt added to the list yet -> add to the end
                breakpoints.add(breakToInsert);
        }
        breakToInsert.resolveAddress();
        return true;
    }
    
    /**
     * @return false if line isnt a breakpoint, true if successfully removed
     */
    public boolean removeBreakpoint(int lineInEditor) {
        System.out.println("removing breakpoint at " + lineInEditor);
        int idx = getBreakpointIndex(lineInEditor);
        if (idx >= 0) {
            breakpoints.remove(idx);
            return true;
        } else {
            return false;
        }
    }
    
    public void clear() {
        breakpoints.clear();
        lineToAddressTable.clear();
    }
    
    public void shiftBreakpointsBelowLine(int lineInEditor, int linesToShift) {
        for (Breakpoint bp : breakpoints) {
            if (bp.lineInEditor >= lineInEditor) {
                bp.lineInEditor += linesToShift;
            }
        }
    }
    
    
    private int getBreakpointIndex(int line) {
        for(int idx = 0; idx < breakpoints.size(); ++idx) {
            if (breakpoints.get(idx).lineInEditor == line)
                return idx;
            //else if (breakpoints.get(idx).lineInEditor > line) // breakpoints are sorted by line
            //    break;
        }
        return -1;
    }
    
    private int getBreakpointIndex(uint32 address) {
        for(int idx = 0; idx < breakpoints.size(); ++idx) {
            if (breakpoints.get(idx).address.equals(address))
                return idx;
        }
        return -1;
    }

    
    private class Breakpoint {
        private uint32 address;
        private int lineInEditor;
        
        public Breakpoint(int lineInEditor) {
            this.lineInEditor = lineInEditor;
            this.address = new uint32(0);
            resolveAddress();
        }
        
        public void resolveAddress() {
            Integer addr = BreakpointManager.getInstance().lineToAddressTable.get(lineInEditor);
            if (addr != null)
                address.setValue(addr);
            else // line not found in table
                address.setValue(0);
        }
        
        @Override
        public boolean equals(Object p) {
            if (p.getClass() != this.getClass())
                return false;
            return ((Breakpoint)p).lineInEditor == this.lineInEditor && this.address.equals(((Breakpoint)p).address);
        }
    }


    @Override
    public void addItemListener(ItemListener il) {
        listeners.add(il);
        
    }

    @Override
    public Object[] getSelectedObjects() {
        Integer[] lines = new Integer[breakpoints.size()];
        for (int i = 0; i < lines.length; ++i)
            lines[i] = breakpoints.get(i).lineInEditor;
        return lines;
    }

    @Override
    public void removeItemListener(ItemListener il) {
        listeners.remove(il);
    }
    
    public void notifyListeners(int line, boolean added) {
        ItemEvent e = new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, line, 
                added ? ItemEvent.SELECTED : ItemEvent.DESELECTED);
        for (ItemListener il : listeners) {
            il.itemStateChanged(e);
        }
    }
}
