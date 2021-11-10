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
package riscVivid.datatypes;

/**
 * Enumeration of all possible branch predictor types
 * - S for static predictors 
 * - D for dynamic predictors
 */
public enum BranchPredictorType
{
	S_ALWAYS_NOT_TAKEN,
	S_ALWAYS_TAKEN,
	S_BACKWARD_TAKEN,
	D_1BIT,
	D_2BIT_SATURATION,
	D_2BIT_HYSTERESIS,
	UNKNOWN;
	
	public String toGuiString()
	{
		switch(this)
		{
		case S_ALWAYS_NOT_TAKEN:
			return "Static, Always Not Taken";
		case S_ALWAYS_TAKEN:
			return "Static, Always Taken";
		case S_BACKWARD_TAKEN:
			return "Static, Backward Always Taken";
		case D_1BIT:
			return "Dynamic 1-Bit Predictor";
		case D_2BIT_SATURATION:
			return "Dynamic 2-Bit Saturation Predictor";
		case D_2BIT_HYSTERESIS:
			return "Dynamic 2-Bit Hysteresis Predictor";
		case UNKNOWN:
			return "None";
		default:
			return "Unknown Predictor";
		}
	}

	public static String[] getValuesGuiStrings() {
		String[] array = new String[values().length];
		for (int i = 0; i < values().length; ++i)
			array[i] = values()[i].toGuiString();
		return array;
	}
}
