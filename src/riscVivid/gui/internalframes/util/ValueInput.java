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
package riscVivid.gui.internalframes.util;

import riscVivid.gui.util.DialogWrapper;

import javax.swing.JOptionPane;

public class ValueInput
{

    public static Integer getValue(String message, Object defaultValue) throws NumberFormatException
    {
        String valueString = DialogWrapper.showInputDialog(message, defaultValue);
        return strToInt(valueString);
    }

    public static Integer strToInt(String valueString)
    {
        if (valueString == null)
            return null;
        else if (valueString.contains("0x"))
            return Integer.parseUnsignedInt(valueString.substring(2), 16);
        else
            return Integer.parseInt(valueString);
    }
}
