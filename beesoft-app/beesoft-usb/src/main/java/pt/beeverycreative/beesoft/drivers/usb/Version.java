/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.beeverycreative.beesoft.drivers.usb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import replicatorg.app.Base;

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
public class Version implements Comparable<Version> {

    public enum Flavour {

        BEEVC, MSFT, UNKNOWN;
    }

    private int bug = 0;
    private int minor = 0;
    private int major = 0;
    private Flavour flavour = Flavour.UNKNOWN;
    private PrinterInfo printer = PrinterInfo.UNKNOWN;
    private String versionString = "";

    public void setVersionFromString(String versionString) {

        if (versionString == null) {
            return;
        }

        String re1 = "(\\d+)";	// Integer Number 1
        String re2 = "(.)";	// Any Single Character 1
        String re3 = "(\\d+)";	// Integer Number 2
        String re4 = "(.)";	// Any Single Character 2
        String re5 = "(\\d+)";	// Integer Number 3

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(versionString);
        if (m.find()) {
            major = Integer.valueOf(m.group(1));
            minor = Integer.valueOf(m.group(3));
            bug = Integer.valueOf(m.group(5));
        }

        this.versionString = versionString;

    }

    public int getBug() {
        return bug;
    }

    public Flavour getFlavour() {
        return flavour;
    }

    public String getVersionString() {
        return flavour + "-" + printer + "-" + major + "." + minor + "." + bug;
    }

    public String getRawVersionString() {
        return versionString;
    }

    public Version() {
    }

    public static Version fromMachineOld(String machineString) {

        Version version = new Version();
        version.versionString = machineString;

        String re1 = "(ok)";	// US State 1
        String re2 = ".*?";	// Non-greedy match on filler
        String re3 = "(N)";	// Variable Name 1
        String re4 = "(:)";	// Any Single Character 1
        String re5 = ".*?";	// Non-greedy match on filler
        String re6 = "\\d+";	// Uninteresting: int
        String re7 = ".*?";	// Non-greedy match on filler
        String re8 = "(\\d+)";	// Integer Number 1
        String re9 = ".*?";	// Non-greedy match on filler
        String re10 = "(\\d+)";	// Integer Number 2
        String re11 = ".*?";	// Non-greedy match on filler
        String re12 = "(\\d+)";	// Integer Number 3

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10 + re11 + re12, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(machineString);
        if (m.find()) {

            version.major = Integer.parseInt(m.group(4));
            version.minor = Integer.parseInt(m.group(5));
            version.bug = Integer.parseInt(m.group(6));
            Base.writeLog("Version is: " + version.versionString);
        } else {
            version.major = 0;
            version.minor = 0;
            version.bug = 0;
            Base.writeLog("Version format invalid: " + machineString);
        }
        return version;
    }

    public static Version fromMachine(String machineString) {

        Version version = new Version();
        version.versionString = machineString;

        String re1 = "(ok)";	// Word 1
        String re2 = "(\\s+)";	// White space 1
        String re3 = "((?:[a-z][a-z]+))";	// Variable Name 1
        String re4 = "(-)";	// Any Single Character 1
        String re5 = "((?:[a-z][a-z0-9_]*))";
        String re6 = "(-)";	// Any Single Character 1
        String re7 = "(\\d+)";	// Integer Number 1
        String re8 = "(\\.)";	// Any Single Character 2
        String re9 = "(\\d+)";	// Integer Number 2
        String re10 = "(\\.)";	// Any Single Character 3
        String re11 = "(\\d+)";	// Integer Number 3

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10 + re11, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(machineString);
        if (m.find()) {
            version.major = Integer.parseInt(m.group(7));
            version.minor = Integer.parseInt(m.group(9));
            version.bug = Integer.parseInt(m.group(11));

            try {
                version.flavour = Flavour.valueOf(m.group(3));
            } catch (IllegalArgumentException e) {
                version.flavour = Flavour.UNKNOWN;
            }

            try {
                version.printer = PrinterInfo.valueOf(m.group(5));
            } catch (IllegalArgumentException e) {
                version.printer = PrinterInfo.UNKNOWN;
            }

        } else {
            version.flavour = Flavour.UNKNOWN;
            version.major = 0;
            version.minor = 0;
            version.bug = 0;
            Base.writeLog("Version format invalid: " + version.versionString);
        }

        return version;
    }

    public static Version bootloaderVersion(String machineString) {
        Version version = new Version();
        version.versionString = machineString;

        String re1 = "(ok)";	// Word 1
        String re2 = "(\\s+)";	// White Space 1
        String re3 = "(\\d+)";	// Integer Number 1
        String re4 = "(\\.)";	// Any Single Character 1
        String re5 = "(\\d+)";	// Integer Number 2
        String re6 = "(\\.)";	// Any Single Character 2
        String re7 = "(\\d+)";	// Integer Number 3

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(machineString);
        if (m.find()) {
            version.major = Integer.parseInt(m.group(3));
            version.minor = Integer.parseInt(m.group(5));
            version.bug = Integer.parseInt(m.group(7));
        } else {
            // if bootloader detection failed, try with the legacy method, it's
            // probably an old bootloader
            return bootloaderVersionLegacy(machineString);
        }

        return version;
    }

    private static Version bootloaderVersionLegacy(String machineString) {
        Version version = new Version();
        version.versionString = machineString;

        String re1 = "(ok)";	// Word 1
        String re2 = "(\\s+)";	// White Space 1
        String re3 = "([a-z])";	// Any Single Word Character (Not Whitespace) 1
        String re4 = "(:)";	// Any Single Character 1
        String re5 = "(\\d+)";	// Integer Number 1
        String re6 = "(\\s+)";	// White Space 2
        String re7 = "(\\d+)";	// Integer Number 2
        String re8 = "(\\.)";	// Any Single Character 2
        String re9 = "(\\d+)";	// Integer Number 3
        String re10 = "(\\.)";	// Any Single Character 3
        String re11 = "(\\d+)";	// Integer Number 4

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10 + re11, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(machineString);
        if (m.find()) {
            version.major = Integer.parseInt(m.group(7));
            version.minor = Integer.parseInt(m.group(9));
            version.bug = Integer.parseInt(m.group(11));
        } else {
            version.major = 0;
            version.minor = 0;
            version.bug = 0;
            Base.writeLog("Couldn't determine bootloader version");
            Base.writeLog("Bootloader string: " + machineString, Version.class);
        }
        
        return version;
    }

    public static Version fromMachineAtFirmware(String machineString) {

        Version version = new Version();
        version.versionString = machineString;

        String re1 = "((?:[a-z][a-z]+))";	// Variable Name 1
        String re2 = "(-)";	// Any Single Character 1
        String re3 = "((?:[a-z][a-z0-9_]*))";
        String re4 = "(-)";	// Any Single Character 1
        String re5 = "(\\d+)";	// Integer Number 1
        String re6 = "(\\.)";	// Any Single Character 2
        String re7 = "(\\d+)";	// Integer Number 2
        String re8 = "(\\.)";	// Any Single Character 3
        String re9 = "(\\d+)";	// Integer Number 3

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(machineString);
        if (m.find()) {
            version.major = Integer.parseInt(m.group(5));
            version.minor = Integer.parseInt(m.group(7));
            version.bug = Integer.parseInt(m.group(9));

            try {
                version.flavour = Flavour.valueOf(m.group(1));
            } catch (IllegalArgumentException e) {
                version.flavour = Flavour.UNKNOWN;
            }

            try {
                version.printer = PrinterInfo.valueOf(m.group(3));
            } catch (IllegalArgumentException e) {
                version.printer = PrinterInfo.UNKNOWN;
            }

        } else {
            version.flavour = Flavour.UNKNOWN;
            version.major = 0;
            version.minor = 0;
            version.bug = 0;
            Base.writeLog("Version format invalid: " + version.versionString);
        }
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Version) {
            Version v = (Version) o;
            return (major == v.major)
                    && (minor == v.minor)
                    && (bug == v.bug);
        }
        return false;
    }

    public boolean atLeast(Version v) {
        return compareTo(v) >= 0;
    }

    @Override
    public int compareTo(Version v) {
        int version_val = major * 10000
                + minor * 100
                + bug * 1;

        int v_val = v.major * 10000
                + v.minor * 100
                + v.bug * 1;

        return version_val - v_val;
    }

    @Override
    public String toString() {
        return "" + major + "." + minor + "." + bug;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public void setBug(int bug) {
        this.bug = bug;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public void setMajor(int major) {
        this.major = major;
    }
}
