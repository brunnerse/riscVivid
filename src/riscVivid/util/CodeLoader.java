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
package riscVivid.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class CodeLoader
{

    public static String loadCode(String filePath) throws FileNotFoundException
    {
        File codeFile = new File(filePath);
        String ret="";
        if (codeFile.exists() && codeFile.isFile())
        {
            try
            {
                FileInputStream fstream = new FileInputStream(codeFile);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                String strLine;
                while ((strLine = br.readLine()) != null)
                {
                    ret+=strLine+"\n";
                }
                br.close();
                return ret;
            }
            catch (Exception e)
            {
                System.err.println(e.toString());
                e.printStackTrace();
                return null;
            }
        } else {
            throw new FileNotFoundException();
        }
    }

}
