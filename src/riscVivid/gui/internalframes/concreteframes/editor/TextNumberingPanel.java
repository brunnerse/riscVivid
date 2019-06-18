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
package riscVivid.gui.internalframes.concreteframes.editor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.*;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import riscVivid.datatypes.uint32;
import riscVivid.gui.MainFrame;
import riscVivid.gui.internalframes.Updateable;
import riscVivid.util.BreakpointManager;

//TODO: add popup menu
@SuppressWarnings("serial")
public class TextNumberingPanel extends JPanel
        implements CaretListener, DocumentListener, PropertyChangeListener, MouseListener, ItemListener, Updateable
{

    public final static float WEST = 0.0f;
    public final static float CENTER = 0.5f;
    public final static float EAST = 1.0f;
    private final static Border BORDER = new MatteBorder(0, 0, 0, 2, Color.GRAY);
    private final static int HEIGHT = Integer.MAX_VALUE - 1000000;
    private JTextComponent component;
    private boolean updateFont;
    private int borderGap;
    private Color currentLineForeground;
    private float digitAlignment;
    private int minimumDisplayDigits;
    private int lastDigits;
    private int lastHeight;
    private int lastLine;
    private Font lastFont;
    private HashMap<String, FontMetrics> fonts;
    private BreakpointManager bm;

    public TextNumberingPanel(JTextComponent component)
    {
        this(component, 3);
    }

    public TextNumberingPanel(JTextComponent component, int minimumDisplayDigits)
    {
        this.component = component;

        setFont(component.getFont());

        setBorderGap(5);
        setCurrentLineForeground(Color.GREEN);
        setDigitAlignment(EAST);
        setMinimumDisplayDigits(minimumDisplayDigits);

        component.getDocument().addDocumentListener(this);
        component.addCaretListener(this);
        component.addPropertyChangeListener("font", this);
        this.addMouseListener(this);
        
        bm = BreakpointManager.getInstance();
        
        JPopupMenu popupMenu = new JPopupMenu();
        this.setComponentPopupMenu(popupMenu);
        //JMenuItem itemAddRemoveBP = new JMenuItem("Add/Remove breakpoint");
        JMenuItem itemClearAllBP = new JMenuItem("Clear all breakpoints");
        //popupMenu.add(itemAddRemoveBP);
        popupMenu.add(itemClearAllBP);
        //itemAddRemoveBP.addActionListener( (e) -> {mouseClicked(new MouseEvent())});
        itemClearAllBP.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bm.clearBreakpoints();
                repaint();
            }
        });
        
        // TODO
    }

    public boolean getUpdateFont()
    {
        return updateFont;
    }

    public void setUpdateFont(boolean updateFont)
    {
        this.updateFont = updateFont;
    }

    public int getBorderGap()
    {
        return borderGap;
    }

    public void setBorderGap(int borderGap)
    {
        this.borderGap = borderGap;
        Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
        setBorder(new CompoundBorder(BORDER, inner));
        lastDigits = 0;
        setPreferredWidth();
    }

    public Color getBreakpointColor()
    {
        return Color.cyan.darker();
    }
    
    public Color getCurrentLineForeground()
    {
        return currentLineForeground == null ? getForeground() : currentLineForeground;
    }

    public void setCurrentLineForeground(Color currentLineForeground)
    {
        this.currentLineForeground = currentLineForeground;
    }

    public float getDigitAlignment()
    {
        return digitAlignment;
    }

    public void setDigitAlignment(float digitAlignment)
    {
        this.digitAlignment =
                digitAlignment > 1.0f ? 1.0f : digitAlignment < 0.0f ? -1.0f : digitAlignment;
    }

    public int getMinimumDisplayDigits()
    {
        return minimumDisplayDigits;
    }

    public void setMinimumDisplayDigits(int minimumDisplayDigits)
    {
        this.minimumDisplayDigits = minimumDisplayDigits;
        setPreferredWidth();
    }

    private void setPreferredWidth()
    {
        Element root = component.getDocument().getDefaultRootElement();
        int lines = root.getElementCount();
        int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);


        if (lastDigits != digits || getFont() != lastFont)
        {
            lastDigits = digits;
            lastFont = getFont();
            FontMetrics fontMetrics = getFontMetrics(getFont());
            int width = fontMetrics.charWidth('0') * digits;
            Insets insets = getInsets();
            int preferredWidth = insets.left + insets.right + width;

            Dimension d = getPreferredSize();
            d.setSize(preferredWidth, HEIGHT);
            setPreferredSize(d);
            setSize(d);
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        Insets insets = getInsets();
        int availableWidth = getSize().width - insets.left - insets.right;

        Rectangle clip = g.getClipBounds();
        int rowStartOffset = component.viewToModel(new Point(0, clip.y));
        int endOffset = component.viewToModel(new Point(0, clip.y + clip.height));
        
        MainFrame mf = MainFrame.getInstance();
        boolean drawStoppedOnLine = (mf.isExecuting() || mf.isRunning()) && mf.getOpenDLXSim() != null && !mf.getOpenDLXSim().isFinished();
        int lineStoppedOn = -1;
        if (drawStoppedOnLine) {
            uint32 addrStoppedOn = MainFrame.getInstance().getOpenDLXSim().getPipeline().getMemoryWriteBackLatch().element().getPc();
            if (addrStoppedOn.getValue() == 0)
                drawStoppedOnLine = false;
            else
                lineStoppedOn = BreakpointManager.getInstance().getCorrespondingLine(addrStoppedOn);
        }
        while (rowStartOffset <= endOffset)
        {
            try
            {
                int lineNumber = getLineNumber(rowStartOffset);
                if (bm.isBreakpoint(lineNumber)) {
                    int diameter = fontMetrics.getMaxAscent();
                    g.setColor(getBreakpointColor());
                    g.fillOval(getSize().width - insets.right - diameter, 
                            getOffsetY(rowStartOffset, fontMetrics) - diameter, diameter, diameter);
                }
                
                if (isCurrentLine(rowStartOffset))
                {
                    g.setColor(getCurrentLineForeground());
                }
                else
                {
                    g.setColor(getForeground());
                }

                String lineNumberText = String.valueOf(lineNumber);
                int stringWidth = fontMetrics.stringWidth(lineNumberText);
                int x = getOffsetX(availableWidth, stringWidth) + insets.left;
                int y = getOffsetY(rowStartOffset, fontMetrics);
                g.drawString(lineNumberText, x, y);
                
                if (lineNumber == lineStoppedOn && drawStoppedOnLine) {
                    g.setColor(Color.red);
                    g.setFont(g.getFont().deriveFont(Font.BOLD));
                    g.drawString("WB", insets.left, y);
                    int height = (int)(fontMetrics.getAscent() * 0.8);
                    int rightCoord = getSize().width - insets.right;
                    int leftCoord = Math.max(rightCoord - 2*height, 2*insets.left + fontMetrics.stringWidth("WB"));
                    g.fillPolygon(new int[] {leftCoord, rightCoord, leftCoord}, new int[]{y,y - height/2, y - height}, 3);
                    g.setFont(g.getFont().deriveFont(Font.PLAIN));
                    component.scrollRectToVisible(new Rectangle(0, rowStartOffset, 0, Utilities.getRowEnd(component, rowStartOffset)));
					//TODO: scroll Editor to that line
                }
                
                rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
            }
            catch (Exception e)
            {
            }
        }
    }

    private boolean isCurrentLine(int rowStartOffset)
    {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        if (root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPosition))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    protected String getTextLineNumber(int textIndex)
    {
        int lineNumber = getLineNumber(textIndex);
        return lineNumber >= 0 ? String.valueOf(lineNumber) : "";
    }
    
    private int getLineNumber(int textIndex)
    {      
        Element root = component.getDocument().getDefaultRootElement();
        int index = root.getElementIndex(textIndex);
        Element line = root.getElement(index);
    
        if (textIndex <= line.getEndOffset())
        {
            return index + 1;
        } else
        {
            return -1;
        }
    }
    
    private int getLineNumberAt(Point p) throws BadLocationException {
        Element root = component.getDocument().getDefaultRootElement();
        int modelLineNum = component.viewToModel(p);
        if (p.y > component.modelToView(modelLineNum).getMaxY())
                throw new BadLocationException("No Line here: Point.y is out of range", p.y);
        
        return root.getElementIndex(modelLineNum) + 1;
    }

    private int getOffsetX(int availableWidth, int stringWidth)
    {
        return (int) ((availableWidth - stringWidth) * digitAlignment);
    }

    private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics)
            throws BadLocationException
    {
        Rectangle r = component.modelToView(rowStartOffset);
        int lineHeight = fontMetrics.getHeight();
        int y = r.y + r.height;
        int descent = 0;


        if (r.height == lineHeight)
        {
            descent = fontMetrics.getDescent();
        }
        else
        {
            if (fonts == null)
            {
                fonts = new HashMap<String, FontMetrics>();
            }

            Element root = component.getDocument().getDefaultRootElement();
            int index = root.getElementIndex(rowStartOffset);
            Element line = root.getElement(index);

            for (int i = 0; i < line.getElementCount(); i++)
            {
                Element child = line.getElement(i);
                AttributeSet as = child.getAttributes();
                String fontFamily = (String) as.getAttribute(StyleConstants.FontFamily);
                Integer fontSize = (Integer) as.getAttribute(StyleConstants.FontSize);
                String key = fontFamily + fontSize;

                FontMetrics fm = fonts.get(key);

                if (fm == null)
                {
                    Font font = new Font(fontFamily, Font.PLAIN, fontSize);
                    fm = component.getFontMetrics(font);
                    fonts.put(key, fm);
                }

                descent = Math.max(descent, fm.getDescent());
            }
        }

        return y - descent;
    }

    @Override
    public void caretUpdate(CaretEvent e)
    {

        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        int currentLine = root.getElementIndex(caretPosition);

        if (lastLine != currentLine)
        {
            repaint();
            lastLine = currentLine;
        }
    }

    // TODO: find out when update is called and call BreakPointManager.linesChanged()
    @Override
    public void changedUpdate(DocumentEvent e)
    {
        documentChanged(e);
    }

    @Override
    public void insertUpdate(final DocumentEvent e)
    {
        documentChanged(e);
    }

    @Override
    public void removeUpdate(final DocumentEvent e)
    {
        documentChanged(e);
    }

    private void documentChanged(final DocumentEvent e)
    {

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                int preferredHeight = component.getPreferredSize().height;

                if (lastHeight != preferredHeight) // line was inserted/removed
                {
                    setPreferredWidth();
                    lastHeight = preferredHeight;
                    
                    BreakpointManager bm = BreakpointManager.getInstance();
                    
                    // handle line insertion/removal for breakpoints
                    Element rootDoc = e.getDocument().getDefaultRootElement();
                    DocumentEvent.ElementChange rootChange = e.getChange(rootDoc);
                    if (rootChange != null) {
                        if (e.getType().equals(DocumentEvent.EventType.INSERT)) {
                            Element[] addedLines = rootChange.getChildrenAdded();
                            int numLinesInserted = addedLines.length - 1; // the start line doesnt count
                            int firstLineStartOffset = addedLines[0].getStartOffset();
                            int firstLineInserted = rootDoc.getElementIndex(firstLineStartOffset) + 1;
                            // if actual insertion position is closer to the end than to the start, insert in next line, not in the current line
                            int replacedLineLen = rootChange.getChildrenRemoved()[0].getEndOffset() - rootChange.getChildrenRemoved()[0].getStartOffset();
                            System.out.println("old line length: " + replacedLineLen + "\t offset to change: " + (e.getOffset() - firstLineStartOffset));
                            if (e.getOffset() - firstLineStartOffset >= replacedLineLen / 2) {
                                firstLineInserted++;
                            }
                            System.out.println(numLinesInserted + " LINES inserted at " + firstLineInserted);
                            BreakpointManager.getInstance().linesChanged(firstLineInserted, firstLineInserted + numLinesInserted - 1, true);
                        }
                        else if (e.getType().equals(DocumentEvent.EventType.REMOVE)) {
                            Element[] removedLines = rootChange.getChildrenRemoved();
                            int numLinesRemoved = removedLines.length - 1; // the start line doesnt count
                            int firstLineStartOffset = removedLines[0].getStartOffset();
                            int firstLineRemoved = rootDoc.getElementIndex(firstLineStartOffset) + 1;
                            // if actual remove position is closer to the end than to the start, remove the next line instead of the current line
                            int replacedLineLen = rootChange.getChildrenRemoved()[0].getEndOffset() - rootChange.getChildrenRemoved()[0].getStartOffset();
                            if (e.getOffset() - firstLineStartOffset >= replacedLineLen / 2) {
                                firstLineRemoved++;
                            }
                            System.out.println(numLinesRemoved + " LINES removed at " + firstLineRemoved);

                            if (bm.isBreakpoint(firstLineRemoved)) {
                                int startRemovedText = rootChange.getChildrenRemoved()[0].getEndOffset();
                                int lengthRemovedText = rootChange.getChildrenAdded()[0].getEndOffset() - startRemovedText;
                                try {
                                    String firstLineRemovedText = e.getDocument().getText(startRemovedText, lengthRemovedText);
                                    System.out.println("shifted text: " + firstLineRemovedText + "<end>");
                                    bm.addBreakpoint(firstLineRemoved - 1, firstLineRemovedText); // adds the breakpoint if the removed text is a valid breakpoint
                                } catch (BadLocationException e1) {
                                }
                                
                            }
                            bm.linesChanged(firstLineRemoved, firstLineRemoved + numLinesRemoved- 1, false);
                        }
                    }
                    
                    repaint();
                }
            }

        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getNewValue() instanceof Font)
        {
            if (updateFont)
            {
                Font newFont = (Font) evt.getNewValue();
                setFont(newFont);
                lastDigits = 0;
                setPreferredWidth();
            }
            else
            {
                repaint();
            }
        }
    }

    @Override
    public void setFont(Font f) {
    	super.setFont(f);
    	if (this.component != null) //otherwise throws exception while initializing
    		setPreferredWidth();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            int lineNumber = getLineNumberAt(e.getPoint());
            Element lineElement = component.getDocument().getDefaultRootElement().getElement(lineNumber-1);
            if (bm.isBreakpoint(lineNumber)) {
                bm.removeBreakpoint(lineNumber);
            } else {
                int lineLength = lineElement.getEndOffset() - lineElement.getStartOffset();
                bm.addBreakpoint(lineNumber, 
                        component.getDocument().getText(lineElement.getStartOffset(), lineLength));
            }
        } catch (BadLocationException blEx) {}
        this.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    @Override
    public void itemStateChanged(ItemEvent arg0) {
        this.repaint();
    }

    @Override
    public void update() {
        this.repaint();
    }

}
