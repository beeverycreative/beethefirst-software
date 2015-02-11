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

    private int bug = 0;
    private int minor = 0;
    private int major = 0;
    private String verionString = "";

    public void setVerionFromString(String verionString) {

        if (verionString == null) {
            return;
        }

        String re1 = "(\\d+)";	// Integer Number 1
        String re2 = "(.)";	// Any Single Character 1
        String re3 = "(\\d+)";	// Integer Number 2
        String re4 = "(.)";	// Any Single Character 2
        String re5 = "(\\d+)";	// Integer Number 3

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(verionString);
        if (m.find()) {
            major = Integer.valueOf(m.group(1));
            minor = Integer.valueOf(m.group(3));
            bug = Integer.valueOf(m.group(5));
        }

        this.verionString = verionString;

    }

    public int getBug() {
        return bug;
    }

    public String getVerionString() {
        return major + "." + minor + "." + bug;
    }

    public String getVerionStringComplete() {
        return verionString;
    }

    public Version() {
    }

    public Version fromFile(String verionString) {

        Version version = new Version();
        version.verionString = verionString;
        String re1 = "(BEETHEFIRST)";	// Word 1
        String re2 = ".*?";	// Non-greedy match on filler
        String re3 = "(\\d+)";	// Integer Number 1
        String re4 = ".*?";	// Non-greedy match on filler
        String re5 = "(\\d+)";	// Integer Number 2
        String re6 = ".*?";	// Non-greedy match on filler
        String re7 = "(\\d+)";	// Integer Number 3
        String re8 = ".*?";	// Non-greedy match on filler
        String re9 = "(bin)";	// Word 2

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(version.verionString);
        if (m.find()) {
            version.major = Integer.parseInt(m.group(2));
            version.minor = Integer.parseInt(m.group(3));
            version.bug = Integer.parseInt(m.group(4));
        } else {
            version.major = 0;
            version.minor = 0;
            version.bug = 0;
            Base.writeLog("Version format invalid: " + verionString);
        }
        return version;
    }

    Version fromMachine3(String machineString) {

        Version version = new Version();
        version.verionString = machineString;

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


        } else {
            version.major = 0;
            version.minor = 0;
            version.bug = 0;
            Base.writeLog("Version format invalid: " + verionString);
        }
        return version;
    }

    Version fromMachine4(String machineString) {

        Version version = new Version();
        version.verionString = machineString;

        String re1 = "(ok)";	// Variable Name 1
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
            version.major = 0;
            version.minor = 0;
            version.bug = 0;
            Base.writeLog("Version format invalid: " + verionString);
        }
        return version;
    }

    Version fromMachineAtFirmware(String machineString) {

        Version version = new Version();
        version.verionString = machineString;

        String re1 = "(\\d+)";	// Integer Number 1
        String re2 = "(.)";	// Any Single Character 1
        String re3 = "(\\d+)";	// Integer Number 2
        String re4 = "(.)";	// Any Single Character 2
        String re5 = "(\\d+)";	// Integer Number 3
        String re6 = "(\\s+)";	// White Space 1
        String re7 = "(ok)";	// Word 1
        String re8 = "(\\s+)";	// White Space 2
        String re9 = "(Q)";	// Any Single Word Character (Not Whitespace) 1
        String re10 = "(:)";	// Any Single Character 3
        String re11 = "(\\d+)";	// Integer Number 4

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10 + re11, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(machineString);
        if (m.find()) {

            version.major = Integer.parseInt(m.group(1));
            version.minor = Integer.parseInt(m.group(3));
            version.bug = Integer.parseInt(m.group(5));


        } else {
            version.major = 0;
            version.minor = 0;
            version.bug = 0;
            Base.writeLog("Version format invalid: " + verionString);
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