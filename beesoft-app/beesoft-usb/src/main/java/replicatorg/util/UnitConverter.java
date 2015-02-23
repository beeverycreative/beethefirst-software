package replicatorg.util;

/**
* Copyright (c) 2013 BEEVC - Electronic Systems
* This file is part of BEESOFT software: you can redistribute it and/or modify 
* it under the terms of the GNU General Public License as published by the 
* Free Software Foundation, either version 3 of the License, or (at your option)
* any later version. BEESOFT is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
* or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
* for more details. You should have received a copy of the GNU General
* Public License along with BEESOFT. If not, see <http://www.gnu.org/licenses/>.
*/
public class UnitConverter {
 
    public static double millimetersToInches(double mm) {
        
        return (mm * 0.0393701);
    }
    
    public static double inchesToMillimeters(double inches) {
        
        return (inches * 25.4);
    }    
}
