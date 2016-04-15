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
package riscVivid.asm.tokenizer;

import riscVivid.asm.AssemblerException;

@SuppressWarnings("serial")
public class TokenizerException extends AssemblerException {
	private Position position_;

	public TokenizerException(String message, Position position) {
		super(message);
		this.position_ = position;
	}

	public String getMessage() {
		return super.getMessage() + " at" + position_;
	}

	public Position getPosition() {
		return position_;
	}
}
