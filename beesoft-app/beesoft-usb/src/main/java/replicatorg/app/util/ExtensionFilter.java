package replicatorg.app.util;

import java.io.File;
import java.util.LinkedList;
import javax.swing.filechooser.FileFilter;

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
public class ExtensionFilter extends FileFilter {

        private LinkedList<String> extensions = new LinkedList<String>();
        private String description;

        public ExtensionFilter(String extension, String description) {
            this.extensions.add(extension);
            this.description = description;
        }

        public ExtensionFilter(String[] extensions, String description) {
            for (String e : extensions) {
                this.extensions.add(e);
            }
            this.description = description;
        }

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return !f.isHidden();
            }
            for (String extension : extensions) {
                if (f.getPath().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return description;
        }

        public String getFirstExtension() {
            return extensions.getFirst();
        }
    };
