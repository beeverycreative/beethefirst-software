package replicatorg.app;

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
public class ProperDefault {

    public ProperDefault() {
    
    
    }
    
    /**
     * Adds to properties file the new property
     * @param key property name
     * @param value property value
     */
    public static void put(String key, String value)
    {
        Base.storeProperty(key, value);
    }
    
    /**
     * Gets property value 
     * @return value stored in properties or the default one in DefaultProperties
     */
    public static String get(String key)
    {
        DefaultProperties.init();
        
        String value = Base.readConfig(key);
        
        if(value != null)
            return value;
        
        return DefaultProperties.getDefault(key);
    }
    
    /**
     * Remove property key and value
     * @param key key representing the pair key-value
     */
    public static void remove(String key)
    {
        Base.removeProperty(key);
    }
    
}
