package replicatorg.app;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.prefs.Preferences;  

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
public class CategoriesList implements Iterable<String> {

	// Preference key name
	final static String CATEGORIES_LIST_KEY = "categories";
	
	// List of most recently opened files names.
	private LinkedList<String> categoriesNames;
	
	private static CategoriesList singleton = null;
	
	public static CategoriesList getMRUList() {
		if (singleton == null) {
			singleton = new CategoriesList();
		}
		return singleton;
	}
	
	private CategoriesList() {
		String mruString = ProperDefault.get(CATEGORIES_LIST_KEY);
		categoriesNames = new LinkedList<String>();
                
		// Deserialize preference
		if (mruString != null && mruString.length() != 0) {
			for (String entry : mruString.split(",")) {
                                categoriesNames.addLast(entry);
			}
                        if(categoriesNames.size() == 0)
                            categoriesNames.addLast("Untitled");
		}
	}
	
	private void writeToPreferences() {
		// Truncate the string at MAX_VALUE_LENGTH; long paths hurt the
		// prefs mechanism.  Someday we'll switch to some hacky KEY_1, KEY_2...
		// mechanism to work around this for most paths.
		StringBuffer sb = new StringBuffer();
		int remaining_chars = Preferences.MAX_VALUE_LENGTH;
		for (String s : categoriesNames) {
			final int len = sb.length();
			if ( (remaining_chars - (len+1)) < 0) {
				break;
			}
			if (len != 0) {
				sb.append(",");
				remaining_chars--;
			}
			sb.append(s);
			remaining_chars -= len;
		}
                ProperDefault.put(CATEGORIES_LIST_KEY,sb.toString());
	}

	public void update(String category) {
		categoriesNames.remove(category);
		categoriesNames.addFirst(category);
		writeToPreferences();
	}
        
	public void remove(String category) {
		categoriesNames.remove(category);
		writeToPreferences();
	}

	public Iterator<String> iterator() {
		return categoriesNames.iterator();
	}
        
        public String[] getCategories()
        {
            String[] list = new String[categoriesNames.size()];

            for(int i = 0; i < list.length; i++)
            {
                list[i] = categoriesNames.get(i);
            }
            
            if(list.length == 0)
            {
                categoriesNames.addLast("Untitled");
                return new String[]{"Untitled"};
            }
                
            return list; 
        }
}
