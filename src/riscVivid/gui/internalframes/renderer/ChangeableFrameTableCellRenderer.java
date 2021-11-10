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
package riscVivid.gui.internalframes.renderer;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import riscVivid.gui.GUI_CONST;
import riscVivid.gui.command.userLevel.CommandDisplayTooltips;

public class ChangeableFrameTableCellRenderer implements TableCellRenderer, GUI_CONST
{

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
        //set defaults
        JLabel label = new JLabel(value.toString());
        label.setOpaque(true);
        Border b = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        label.setBorder(b);
        label.setFont(table.getFont());
        label.setForeground(table.getForeground());
        label.setBackground(table.getBackground());

        if (isSelected)
            label.setBackground(new Color(0xBAD2E4));

        if (CommandDisplayTooltips.isTooltipsEnabled())
            label.setToolTipText("double click to change");

        return label;
    }
}
