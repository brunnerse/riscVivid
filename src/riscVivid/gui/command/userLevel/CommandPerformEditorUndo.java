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
package riscVivid.gui.command.userLevel;

import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import riscVivid.gui.command.Command;

public class CommandPerformEditorUndo implements Command
{
	private UndoManager manager;

	/**
	 * @param mgr Takes the UndoManager object
	 */
	public CommandPerformEditorUndo( UndoManager mgr )
	{
		this.manager = mgr;
	}
	
    @Override
    public void execute()
    {
    	try {
    	    String undoAction = manager.getUndoPresentationName();
			manager.undo();
            // if a deletion follows immediately to an addition, do both
			if (undoAction.contains("addition") && manager.getUndoPresentationName().contains("deletion"))
			    manager.undo();
		} catch (CannotUndoException e) {
			/* This exception is thrown, when there is no more undo available.
			 * Nothing to be done here
			 */
		}
    }

}
