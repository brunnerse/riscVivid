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
package riscVivid;

import org.apache.log4j.Logger;

import riscVivid.datatypes.*;

public class BranchControl
{

	private static Logger logger = Logger.getLogger("EXECUTE/BCTRL");
	
	public boolean checkBranch(Instruction inst, int A, int B)
	{
		boolean jump = false;
		switch(inst.getBranchCondition())
		{
		case BEQ:
			jump = (A==B);
			break;
		case BNE:
			jump = (A!=B);
			break;
		case BLT:
			jump = (A<B);
			break;
		case BGE:
			jump = (A>=B);
			break;
		case BLTU:
			long longA = 0xffffffffL & (long)A;
			long longB = 0xffffffffL & (long)B;
			jump = (longA < longB);
			break;
		case BGEU:
			longA = 0xffffffffL & (long)A;
			longB = 0xffffffffL & (long)B;
			jump = (longA >= longB);
			break;

/*
 		case BGEZ:
			if(A.getValue() >= 0)
			{
				jump = true;
			}
		case BGTZ:
			if(A.getValue() > 0)
			{
				jump = true;
			}
			break;
		case BLEZ:
			if(A.getValue() <= 0)
			{
				jump = true;
			}
			break;
		case BLTZ:
			if(A.getValue() < 0)
			{
				jump = true;
			}
			break;
*/
		case UNCOND:
			jump = true;
			break;
		default:
			jump = false;
		}
		if(inst.getBranch())
			logger.debug("A: " + A + " " + inst.getBranchCondition() + " B: " + B + " jump: " + jump);
		return jump;
	}
}
