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
package riscVivid.exception;

import riscVivid.datatypes.uint32;

@SuppressWarnings("serial")
public class PipelineException extends Exception {
    // if simulation needs to be aborted because of the exception
    private final boolean isFatal;
    private uint32 address;

    public PipelineException(String message) {
        this(message, null, true);
    }
    public PipelineException(String message, uint32 instrAddress) {
		this(message, instrAddress, true);
	}
    
    public PipelineException(String message, uint32 instrAddress, boolean isFatal) {
        super(message);
        this.address = instrAddress;
        this.isFatal = isFatal;
    }
    
    public boolean isFatal() {
        return this.isFatal;
    }

    public uint32 getInstructionAddress() {
        return this.address;
    }

    public void setInstructionAddress(uint32 instrAddress) {
        this.address = instrAddress;
    }
}
