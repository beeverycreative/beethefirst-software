/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replicatorg.plugin.toolpath.cura;

import pt.beeverycreative.beesoft.drivers.usb.BlackListGCodes;

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
public enum CuraPrintProfiles {
    
    LowResolution,HighResolution;

    public static boolean contains(String test) {

        for (BlackListGCodes c : BlackListGCodes.values()) {
            if (c.name().equals(test)) {
                return true;
            }
        }

        return false;
    }
    
}
