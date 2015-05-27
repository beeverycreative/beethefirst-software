package replicatorg.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class Units_and_Numbers {

    public static double millimetersToInches(double mm) {

        return (mm * 0.0393701);
    }

    public static double inchesToMillimeters(double inches) {

        return (inches * 25.4);
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static double sGetDecimalStringAnyLocaleAsDouble(String value) {

        try {
            Locale theLocale = Locale.getDefault();
            NumberFormat numberFormat = DecimalFormat.getInstance(theLocale);
            Number theNumber;
            String filteredValue = value;
            
            //We have to filter out any commas. Some locales just ignore the 
            //commas and transform a number like 102,92 into 10292...
            if (filteredValue.contains(",")) {
                filteredValue = value.replaceAll(",", ".");
            }

            //theNumber = numberFormat.parse(filteredValue);
            //return theNumber.doubleValue();
            return Double.valueOf(filteredValue);
                                    
        } catch (Exception ex) {
            Logger.getLogger(Units_and_Numbers.class.getName()).log(Level.SEVERE, null, ex);
            
            return Double.valueOf(value);
        }
        
    }
}
