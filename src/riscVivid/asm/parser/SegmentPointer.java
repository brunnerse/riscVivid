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
package riscVivid.asm.parser;

/**
 * This class is used to wrap integer values to make them mutable. Fuck java!
 */
public class SegmentPointer {
	private int value_;

	public SegmentPointer(int value) {
		set(value);
	}

	public int get() {
		return value_;
	}

	public void set(int value) {
		this.value_ = value;
	}

	public void add(int value) {
		this.value_ += value;
	}

	public void sub(int value) {
		this.value_ -= value;
	}

	public String toString() {
		return Integer.toString(value_);
	}
}
