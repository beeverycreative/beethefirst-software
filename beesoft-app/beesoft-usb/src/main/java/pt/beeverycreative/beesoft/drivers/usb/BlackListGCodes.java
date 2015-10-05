package pt.beeverycreative.beesoft.drivers.usb;

/**
 * Copyright (c) 2013 BEEVC - Electronic Systems This file is part of BEESOFT
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. BEESOFT is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with
 * BEESOFT. If not, see <http://www.gnu.org/licenses/>.
 */
public enum BlackListGCodes {

    /**
     * List of all bad GCodes for BTF's driver.
     */
    M29, M82, M84, M101, M103, M108, M113, M117, M140, G21;

    /**
     * Checks if gcode line contains any of denied gcodes.
     *
     * @param test gcode line
     * @return <li> true, if contains
     * <li> false, if not
     */
    public static boolean contains(String test) {

        for (BlackListGCodes c : BlackListGCodes.values()) {
            if (c.name().equals(test)) {
                return true;
            }
        }

        return false;
    }
}
