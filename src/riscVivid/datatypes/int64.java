/*******************************************************************************
 * riscVivid - A RISC-V processor simulator.
 * Copyright (C) 2013-2016 The riscVivid project, University of Augsburg, Germany
 * <https://github.com/unia-sik/riscVivid>
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
package riscVivid.datatypes;

public class int64 extends BasicNumber 
{
    private long value;

    public int64(long value)
    {
        this.value = value;
    }

    public int64(int value)
    {
        this.value = value;
    }

    public int64(short value)
    {
        this.value = (int) value;
    }

    public int64(byte value)
    {
        this.value = (int) value;
    }

    public int64(int64 value)
    {
        this.value = (int) value.getValue();
    }

    public long getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    public String getValueAsHexString()
    {
        return String.format("0x%016x", value);
    }

    public int hashCode()
    {
        return (int)((value>>32) ^ value);
    }

    public boolean equals(Object o)
    {
        if (o.getClass() == this.getClass())
        {
            return equals((int64) o);
        }
        return false;
    }

    public boolean equals(int64 other)
    {
        return (getValue() == other.getValue());
    }

    public String getValueAsDecimalString()
    {
        return Long.toString(getValue());
    }
}
