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
package riscVivid.config;

public interface GlobalConfig {
	static final String PREFERENCES_DIR = "/riscVivid";
	static final String VERSION = "1.4";
	static final String ABOUT = 
			"riscVivid v"+VERSION+" - A RISC-V processor simulator.\n"
					+"(C)opyright 2013-2020 The riscVivid project, University of Augsburg\n\n"
					+"openDLX contributors: Stefan Metzlaff, Alexander Vogelgsang, Nico Krezic-Luger\n"
					+"                      Arne Garbade, Patrick Lehner\n"
					+"RISC-V support:       Jörg Mische\n"
					+"Further contributors: Severin Brunner\n"
					+"\n"
					+"https://github.com/unia-sik/riscVivid\n"
					+"\n"
					+"This program is free software: you can redistribute it and/or modify\n"
					+"it under the terms of the GNU General Public License as published by\n"
					+"the Free Software Foundation, either version 3 of the License, or\n"
					+"any later version.\n"
					+"\n"
					+"This program is distributed in the hope that it will be useful,\n"
					+"but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
					+"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
					+"GNU General Public License for more details.\n"
					+"\n"
					+"You should have received a copy of the GNU General Public License\n"
					+"along with this program, see <LICENSE>. If not, see\n"
					+"<http://www.gnu.org/licenses/>.";
}
