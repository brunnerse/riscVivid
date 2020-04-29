package riscVivid.util;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import riscVivid.asm.tokenizer.TokenType;
import riscVivid.asm.tokenizer.Token;
import riscVivid.asm.tokenizer.Tokenizer;
import riscVivid.asm.tokenizer.TokenizerException;
import riscVivid.datatypes.uint32;

public class BreakpointManager implements ItemSelectable {
    
    private static BreakpointManager instance = null;
    
    private TreeSet<Integer> breakpointLines = new TreeSet<Integer>();
    private Hashtable<uint32, Integer> addressToLineTable = new Hashtable<uint32, Integer>();
    
    private ArrayList<ItemListener> listeners = new ArrayList<ItemListener>();
    
    private BreakpointManager() {
    }
    
    public static BreakpointManager getInstance() {
        if (instance == null)
           instance = new BreakpointManager();
        return instance;
    }

    public int getNumBreakpoints() {
        return breakpointLines.size();
    }
    public boolean isBreakpoint(uint32 address) {
        return isBreakpoint(addressToLineTable.getOrDefault(address, -1));
    }
    
    public boolean isBreakpoint(int lineInEditor) {
        return breakpointLines.contains(lineInEditor);
    }
    /**
     * checks if line starts with a mnemonic (ignoring labels)
     * @param line
     * @return
     */
    public static boolean isValidBreakpoint(String line) {
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
            // End of tokens reached and no mnemonic
            return false;
        } catch (IOException e) {
            return true;
        } catch (TokenizerException e) {
            return false;
        }
    }
    
    /**
     *  Sets the new table and resolves all breakpoints according to it
     */
    public void setAddressToLineTable(Hashtable<uint32, Integer> addressToLineTable) {
        this.addressToLineTable = addressToLineTable;
        if (breakpointLines.isEmpty())
            return;
        TreeSet<Integer> unresolvedLines = new TreeSet<Integer>(breakpointLines);
        System.out.print("breakpoint addresses: ");
        // check if breakpoints are contained; otherwise remove them
        for(Enumeration<uint32> enumKeys = addressToLineTable.keys();
                enumKeys.hasMoreElements();) {
            uint32 addr = enumKeys.nextElement();
            for (Integer line : unresolvedLines) {
                if (addressToLineTable.get(addr).equals(line)) {
                    System.out.print("line " + line + " at " + addr.getValueAsHexString() + ", ");
                    unresolvedLines.remove(line);
                    break;
                }
            }
        }
        // remove last comma with carriage return
        System.out.println("\r\r");
        breakpointLines.removeAll(unresolvedLines);
        notifyListeners(-1, true);
    }
    
    public int getCorrespondingLine(uint32 address) {
        return addressToLineTable.getOrDefault(address, -1);
    }

    /*
     * @return false if line isn't a valid breakpoint (isn't a instruction),
     * true if successfully added or is already a breakpoint
     */
    public boolean addBreakpoint(int lineInEditor, String lineText) {
        if (isValidBreakpoint(lineText)) {
            if (breakpointLines.add(lineInEditor)) // notify listeners if line wasnt already contained
                notifyListeners(lineInEditor, true);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * @return false if line isnt a breakpoint, true if successfully removed
     */
    public boolean removeBreakpoint(int lineInEditor) {
        if (!breakpointLines.remove((Integer)lineInEditor))
            return false;
        notifyListeners(lineInEditor, false);
        return true;
    }
    
    public void clearBreakpoints() {
        breakpointLines.clear();
    }
    
    /**
     * to call when some lines were either removed or added
     * synchronized to avoid possible concurrency issues, as function might not be reentrant
     * @param inserted:   false if lines were removed
     */
    public synchronized void linesChanged(int firstLine, int lastLine, boolean inserted) {
        //TODO: not entirely bugfree!
        int numLines = lastLine - firstLine + 1;
        if (inserted) {
            for(Enumeration<uint32> enumKeys = addressToLineTable.keys();
                    enumKeys.hasMoreElements();) {
                uint32 addr = enumKeys.nextElement();
                int line = addressToLineTable.get(addr);
                if (line >= firstLine)
                    addressToLineTable.replace(addr,  line + numLines);
            }
            SortedSet<Integer> tailSet = breakpointLines.tailSet(firstLine);
            // needs to copy it because tailSet points on the same storage as breakpointLines
            Integer[] linesBelow = new Integer[tailSet.size()];
            tailSet.toArray(linesBelow);
            System.out.println("shifting following breakpoints: " + tailSet);
            // remove all Lines in tailSet from breakpointLines; works because tailSet points to same storage as breakpointLines!
            tailSet.clear();
            for (Integer line : linesBelow) {
                breakpointLines.add(line + numLines);
            }
        } else { // lines were deleted
            for(Enumeration<uint32> enumKeys = addressToLineTable.keys();
                    enumKeys.hasMoreElements();) {
                uint32 addr = enumKeys.nextElement();
                int line = addressToLineTable.get(addr);
                if (line >= firstLine) {
                    if (line <= lastLine) {
                        addressToLineTable.remove(addr);
                    } else {
                        addressToLineTable.replace(addr,  line - numLines);
                    }
                }
            }
            SortedSet<Integer> tailSet = breakpointLines.tailSet(firstLine);
            // needs to copy it because tailSet points on the same storage as breakpointLines
            Integer[] linesBelow = new Integer[tailSet.size()];
            tailSet.toArray(linesBelow);
            System.out.println("shifting following breakpoints: " + tailSet);
            // remove all Lines in tailSet from breakpointLines; works because tailSet points to same storage as breakpointLines!
            tailSet.clear();
            for (Integer line : linesBelow) {
                if (line > lastLine)
                    breakpointLines.add(line - numLines);
                // do not insert again if line <= lastLine (as it is in the deleted part then)
            }
        }
        System.out.println("new breakpoints: " + breakpointLines);
        notifyListeners(-1, false);
    }

    @Override
    public void addItemListener(ItemListener il) {
        listeners.add(il);
        
    }

    @Override
    public Object[] getSelectedObjects() {
        return breakpointLines.toArray();
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
