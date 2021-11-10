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
package riscVivid.gui.internalframes;

import static riscVivid.gui.Preference.pref;

import java.awt.Component;

import javax.swing.JInternalFrame;

public class FrameConfiguration
{

    private Component c = null;
    private String posXPreferenceKey = "posx";
    private String posYPreferenceKey = "posy";
    private String sizeXPreferenceKey = "sizex";
    private String sizeYPreferenceKey = "sizey";
    private String zOrderPreferenceKey = "zorder";
    private String iconizedPreferenceKey = "iconized";

    public FrameConfiguration(JInternalFrame jif) {
        this.c = jif;
    }
    public FrameConfiguration(Component c) {
        this.c = c;
    }

    public void saveFrameConfiguration()
    {
    	String frameTitle = c.getClass().getSimpleName();
        pref.putInt(frameTitle + posXPreferenceKey, c.getX());
        pref.putInt(frameTitle + posYPreferenceKey, c.getY());
        pref.putInt(frameTitle + sizeXPreferenceKey, c.getSize().width);
        pref.putInt(frameTitle + sizeYPreferenceKey, c.getSize().height);
        if (c instanceof JInternalFrame) {
            JInternalFrame jif = (JInternalFrame) c;
            pref.putBoolean(frameTitle + iconizedPreferenceKey, jif.isIcon());
            if (jif.getParent() != null)
                pref.putInt(frameTitle + zOrderPreferenceKey,
                    c.getParent().getComponentZOrder(jif));
        }
    }

    public void loadFrameConfiguration()
    {
    	String frameTitle = c.getClass().getSimpleName();
        c.setBounds(pref.getInt(frameTitle + posXPreferenceKey, c.getX()),
                pref.getInt(frameTitle + posYPreferenceKey, c.getY()),
                pref.getInt(frameTitle + sizeXPreferenceKey, c.getWidth()),
                pref.getInt(frameTitle + sizeYPreferenceKey, c.getHeight()));
        if (c instanceof JInternalFrame) {
            JInternalFrame jif = (JInternalFrame) c;
            if (jif.getParent() != null)
                jif.getParent().setComponentZOrder(jif,
                        pref.getInt(frameTitle + zOrderPreferenceKey, 0));
            try
            {
                jif.setIcon(pref.getBoolean(frameTitle + iconizedPreferenceKey, false));
            }
            catch (Exception e) { }
        }
    }
    
}
