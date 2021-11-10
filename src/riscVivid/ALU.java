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

import java.math.BigInteger;
import org.apache.log4j.Logger;

import riscVivid.datatypes.*;
import riscVivid.exception.ExecuteStageException;
import riscVivid.exception.PipelineException;
import riscVivid.util.DLXTrapHandler;

public class ALU
{
	private static Logger logger = Logger.getLogger("EXECUTE/ALU");
	private DLXTrapHandler trap_handler=null;
	
	public ALU()
	{
		trap_handler = DLXTrapHandler.getInstance();
	}
	
	
	/* calculates result of ALU operation. If a 32bit result is calculated, it is available to both 32bit outputs. 
	 * For a 64bit result, the result is split into a lower and upper 32bit results.
	 */
	public uint32[] doOperation(ALUFunction operation, uint32 A, uint32 B) throws PipelineException
	{
		uint32 resultLO = new uint32(0);
		uint32 resultHI = new uint32(0);
		
		switch(operation)
		{
		case ADD:
			// TODO missing trap on overflow
		case ADDU:
			resultLO.setValue(A.getValue() + B.getValue());
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " + " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case AND:
			resultLO.setValue(A.getValue() & B.getValue());
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " & " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case BA:
			resultLO.setValue((A.getValue()&0xF0000000)|((int)B.getValue()<<2));
			logger.debug("(" + B.getValue() + "(" + B.getValueAsHexString() + ")" + " << 2) + " + (A.getValue()&0xF0000000) + "(" + Integer.toHexString(A.getValue()&0xF0000000) + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case BR:
			short tgt_offset = (short)B.getValue();
			resultLO.setValue(A.getValue()+(int)(tgt_offset << 2));
			logger.debug("(" + B.getValue() + "(" + B.getValueAsHexString() + ")" + " << 2) + " + A.getValue() + "(" + A.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case DIV:
		{	
			if(B.getValue() == 0)
			{
				throw new ExecuteStageException("Division by zero.");
			}
			// takes usually multiple cycles (3 according to the isa)
			int q = A.getValue() / B.getValue();
			int r = A.getValue() % B.getValue();
			resultLO.setValue(q);
			resultHI.setValue(r);
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " / " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " =  LO (q): " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")" + " HI (r): " + resultHI.getValue() + "(" + resultHI.getValueAsHexString() + ")");
			break;
		}
		case DIVU:
		{	
			if(B.getValue() == 0)
			{
				throw new ExecuteStageException("Division by zero.");
			}
			// takes usually multiple cycles (3 according to the isa)
			long q = ((long)A.getValue()&0xffffffffL) / ((long)B.getValue()&0xffffffffL);
			long r = ((long)A.getValue()&0xffffffffL) % ((long)B.getValue()&0xffffffffL);
			resultLO.setValue((int)q);
			resultHI.setValue((int)r);
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " / " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " =  LO (q): " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")" + " HI (r): " + resultHI.getValue() + "(" + resultHI.getValueAsHexString() + ")");
			break;
		}
		case REM:
		{	
			if(B.getValue() == 0)
			{
				throw new ExecuteStageException("Division by zero.");
			}
			// takes usually multiple cycles (3 according to the isa)
			int r = A.getValue() % B.getValue();
			resultLO.setValue(r);
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " / " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " =  LO (q): " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")" + " HI (r): " + resultHI.getValue() + "(" + resultHI.getValueAsHexString() + ")");
			break;
		}
		case REMU:
		{	
			if(B.getValue() == 0)
			{
				throw new ExecuteStageException("Division by zero.");
			}
			long r = ((long)A.getValue()&0xffffffffL) % ((long)B.getValue()&0xffffffffL);
			resultLO.setValue((int)r);
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " / " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " =  LO (q): " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")" + " HI (r): " + resultHI.getValue() + "(" + resultHI.getValueAsHexString() + ")");
			break;
		}
		case LUI:
			resultLO.setValue(((int)B.getValue() << 16));
			logger.debug(B.getValue() + "(" + B.getValueAsHexString() + ")" + " << " + 16 + "(0x10)" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case MULT:
		{
			// takes usually multiple cycles (3 according to the isa)
			long mult = (long)A.getValue() * (long)B.getValue();
			resultLO.setValue((int)mult);
			resultHI.setValue((int)(mult >>> 32));
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " * " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + mult + " HI: " + resultHI.getValue() + "(" + resultHI.getValueAsHexString() + ")" + " LO: " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			break;
		}
		case MULTU:
		{
			// takes usually multiple cycles (3 according to the isa)
			long mult = ((long)A.getValue()&0xFFFFFFFFL) * ((long)B.getValue()&0xFFFFFFFFL);
			resultLO.setValue((int)(mult & 0xFFFFFFFFL));
			resultHI.setValue((int)((mult >>> 32) & 0xFFFFFFFFL));
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " * " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + mult + " HI: " + resultHI.getValue() + "(" + resultHI.getValueAsHexString() + ")" + " LO: " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			break;
		}
		case MULH:
		{
			long mult = (long)A.getValue() * (long)B.getValue();
			resultLO.setValue((int)((mult >>> 32) & 0xFFFFFFFF));
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() 
				+ ") * " + B.getValue() + "(" + B.getValueAsHexString() 
				+ ")>>32= " + resultLO.getValue() + '(' + resultLO.getValueAsHexString() + ")");
			break;
		}
		case MULHU:
		{
			BigInteger biga = BigInteger.valueOf(((long)A.getValue()) & 0xffffffffL);
			BigInteger bigb = BigInteger.valueOf(((long)B.getValue()) & 0xffffffffL);
			resultLO.setValue((int)biga.multiply(bigb).shiftRight(32).longValue());
			logger.debug(biga.longValue() + "(" + String.format("0x%08x", biga.longValue())  
				+ ") * " + bigb.longValue() + "(" + String.format("0x%08x", bigb.longValue())  
				+ ")>>32= " + resultLO.getValue() + '(' + resultLO.getValueAsHexString() + ")");
			break;
		}
		case MULHSU:
		{
			BigInteger biga = BigInteger.valueOf((long)A.getValue());
			BigInteger bigb = BigInteger.valueOf((long)B.getValue() & 0xffffffffL);
			resultLO.setValue((int)biga.multiply(bigb).shiftRight(32).longValue());
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() 
				+ ") * " + B.getValue() + "(" + B.getValueAsHexString() 
				+ ")>>32= " + resultLO.getValue() + '(' + resultLO.getValueAsHexString() + ")");
			break;
		}

		
		
		case OR:
			resultLO.setValue(A.getValue() | B.getValue());
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " | " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case NOR:
			resultLO.setValue(~(A.getValue() | B.getValue()));
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " NOR " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SLL:
		{
			short s = (short) (B.getValue() & 0x1F);
			resultLO.setValue(A.getValue() << s);
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " << " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		}
		case SLLV:
		{
			short s = (short) (A.getValue() & 0x1F);
			resultLO.setValue(B.getValue() << s);
			logger.debug(B.getValue() + "(" + B.getValueAsHexString() + ")" + " << " + A.getValue() + "(" + A.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		}
		case SEQ:
			// NOTICE: this ALU function is only needed for the DLX ISA
			if(A.getValue() == B.getValue())
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SEQ " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SEQU:
			// NOTICE: this ALU function is only needed for the DLX ISA
			// chop off sign bit
			if((A.getValue()&0x7FFFFFFF) == (B.getValue()&0x7FFFFFFF))
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SEQU " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SNE:
			// NOTICE: this ALU function is only needed for the DLX ISA
			if(A.getValue() != B.getValue())
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SNE " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SNEU:
			// NOTICE: this ALU function is only needed for the DLX ISA
			// chop off sign bit
			if((A.getValue()&0x7FFFFFFF) != (B.getValue()&0x7FFFFFFF))
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SNEU " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SGE:
			// NOTICE: this ALU function is only needed for the DLX ISA
			if(A.getValue() >= B.getValue())
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SGE " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SGEU:
			// NOTICE: this ALU function is only needed for the DLX ISA
			if(((long)A.getValue()&0xFFFFFFFFL) >= ((long)B.getValue()&0xFFFFFFFFL))
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SGEU " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SGT:
			// NOTICE: this ALU function is only needed for the DLX ISA
			if(A.getValue() > B.getValue())
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SGT " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SGTU:
			// NOTICE: this ALU function is only needed for the DLX ISA
			if(((long)A.getValue()&0xFFFFFFFFL) > ((long)B.getValue()&0xFFFFFFFFL))
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SGT " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SLE:
			// NOTICE: this ALU function is only needed for the DLX ISA
			if(A.getValue() <= B.getValue())
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SLE " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SLEU:
			// NOTICE: this ALU function is only needed for the DLX ISA
			if(((long)A.getValue()&0xFFFFFFFFL) <= ((long)B.getValue()&0xFFFFFFFFL))
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SLEU " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SLT:
			if(A.getValue() < B.getValue())
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SLT " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SLTU:
			if( ((long)A.getValue()&0xFFFFFFFFL) < ((long)B.getValue()&0xFFFFFFFFL) )
			{
				resultLO.setValue(1);
			}
			else
			{
				resultLO.setValue(0);
			}
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " SLT " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case SRL:
		{
			short s = (short) (B.getValue() & 0x1F);
			resultLO.setValue(A.getValue() >>> s);
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " >>> " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		}
		case SRA:
		{
			short s = (short) (B.getValue() & 0x1F);
			resultLO.setValue(A.getValue() >> s);
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " >> " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		}
		case SRLV:
		{
			short s = (short) (A.getValue() & 0x1F);
			resultLO.setValue(B.getValue() >>> s);
			logger.debug(B.getValue() + "(" + B.getValueAsHexString() + ")" + " >>> " + A.getValue() + "(" + A.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		}
		case SRAV:
		{
			short s = (short) (A.getValue() & 0x1F);
			resultLO.setValue(B.getValue() >> s);
			logger.debug(B.getValue() + "(" + B.getValueAsHexString() + ")" + " >> " + A.getValue() + "(" + A.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		}
		case SUB:
			// TODO missing trap on overflow
		case SUBU:
			resultLO.setValue(A.getValue() - B.getValue());
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " - " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case XOR:
			resultLO.setValue(A.getValue() ^ B.getValue());
			logger.debug(A.getValue() + "(" + A.getValueAsHexString() + ")" + " XOR " + B.getValue() + "(" + B.getValueAsHexString() + ")" + " = " + resultLO.getValue() + "(" + resultLO.getValueAsHexString() + ")");
			// duplicate results
			resultHI = resultLO;
			break;
		case NOP:
			resultLO.setValue(0);
			// duplicate results
			resultHI = resultLO;
			break;
		case SYSCALL:
/*
			// NOTICE: this ALU function is only needed for the MIPS ISA
			doSyscall(A.getValue(),B.getValue());
			riscvSyscall();
			
			resultLO.setValue(0);
			// duplicate results
			resultHI = resultLO;
*/
			logger.debug("syscall: handled in write back stage");
			break;
		case TRAP:
			// NOTICE: this ALU function is only needed for the DLX ISA
			uint32 trapResult = doDLXTrap(B.getValue(),A.getValue());
			
			resultLO.setValue(trapResult);
			// duplicate results
			resultHI = resultLO;
			break;
		case TEQ:
			if(A.getValue() == B.getValue())
			{
				doTrap();
			}
			
			resultLO.setValue(0);
			// duplicate results
			resultHI = resultLO;
			break;
		case TGE:
			if(A.getValue() >= B.getValue())
			{
				doTrap();
			}
			
			resultLO.setValue(0);
			// duplicate results
			resultHI = resultLO;
			break;
		case TGEU:
			if(((long)A.getValue()&0xFFFFFFFFL) >= ((long)B.getValue()&0xFFFFFFFFL))
			{
				doTrap();
			}
			
			resultLO.setValue(0);
			// duplicate results
			resultHI = resultLO;
			break;
		case TLT:
			if(A.getValue() < B.getValue())
			{
				doTrap();
			}
			
			resultLO.setValue(0);
			// duplicate results
			resultHI = resultLO;
			break;
		case TLTU:
			if(((long)A.getValue()&0xFFFFFFFFL) < ((long)B.getValue()&0xFFFFFFFFL))
			{
				doTrap();
			}
			
			resultLO.setValue(0);
			// duplicate results
			resultHI = resultLO;
			break;
		case TNE:
			if(A.getValue() != B.getValue())
			{
				doTrap();
			}
			
			resultLO.setValue(0);
			// duplicate results
			resultHI = resultLO;
			break;
		default:
			throw new ExecuteStageException("Unknown ALU operation");
		}
		
		uint32[] results = new uint32[2];
		results[0] = resultLO;
		results[1] = resultHI;
		return results;
	}
	
	private uint32 doDLXTrap(int trap_id, int parameter) throws PipelineException 
	{
		uint32 return_result = new uint32(trap_id);
		
		switch(trap_id)
		{
		case PipelineConstants.DLX_TRAP_STOP:
		{
			logger.info("Catched trap 0: stopping pipeline in write back");
			break;
		}
		case PipelineConstants.DLX_TRAP_OPEN:
		{
			logger.debug("TRAP " + trap_id + ": parameter: " + parameter);
			trap_handler.open(parameter);
			break;
		}
		case PipelineConstants.DLX_TRAP_CLOSE:
		{
			logger.debug("TRAP " + trap_id + ": parameter: " + parameter);
			trap_handler.close(parameter);
			break;
		}
		case PipelineConstants.DLX_TRAP_READ:
		{
			uint32 trap_result = trap_handler.read(parameter);
			return_result.setValue(trap_result);
			break;
		}
		case PipelineConstants.DLX_TRAP_WRITE:
		{
			logger.debug("TRAP " + trap_id + ": parameter: " + parameter);
			trap_handler.write(parameter);
			break;
		}
		case PipelineConstants.DLX_TRAP_PRINTF:
		{
			trap_handler.printf(parameter);
			break;
		}
		default:
			logger.warn("Unknown Trap: " + trap_id + " parameter: " + parameter);
		}
		return return_result;
	}

	private void doTrap()
	{
		logger.warn("Catched Trap  ... don't know what to do maybe divition by zero occured. Stopping simulation.");
		System.exit(1);
	}

}
