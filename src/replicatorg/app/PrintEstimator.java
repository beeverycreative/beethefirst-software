package replicatorg.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class PrintEstimator {

    private static String GCODER_PATH = Base.getApplicationDirectory().getAbsolutePath().concat("/estimator/gcoder.py");
    private static String ERROR_MESSAGE = "NA";
    private static String estimatedTime = ERROR_MESSAGE;
    private static String estimatedCost = ERROR_MESSAGE;
    private static String BIN_PATH_UNIX = "python";
    private static String BIN_PATH_WINDOWS = "C:\\Python27\\python.exe";
    private static final double patch = 1.50;
    private static final String dayKeyWord = "day";

    public static String getEstimatedTime() {
        return estimatedTime;
    }

    public static String getEstimatedCost() {
        return estimatedCost;
    }

    public static String estimateTime(File gcode) {

        //Validates if estimator dir exists
        if (new File(GCODER_PATH).exists()) {
            String[] command = new String[3];

            if (Base.isMacOS() || Base.isLinux()) {
                command[0] = BIN_PATH_UNIX;
                command[1] = GCODER_PATH;
                command[2] = gcode.getAbsolutePath();
            } else {
                command[0] = BIN_PATH_WINDOWS;
                command[1] = GCODER_PATH;
                command[2] = gcode.getAbsolutePath();
            }

            ProcessBuilder probuilder = new ProcessBuilder(command);

            //You can set up your work directory
            //        probuilder.directory(new File("c:\\xyzwsdemo"));

            Process process = null;
            try {
                process = probuilder.start();
            } catch (IOException ex) {
                Base.writeLog("Error starting process to estimate print duration");
                estimatedTime = ERROR_MESSAGE;
                return "Error";
            }

            //Read out dir output
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            String output = "";
            try {
                while ((line = br.readLine()) != null) {
                    output = output.concat(line);
                }
            } catch (IOException ex) {
                Base.writeLog("Error reading gcode estimater output");
            }

            //Wait to get exit value
            try {
                int exitValue = process.waitFor();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            estimatedCost = parseCostOutput(output);
            estimatedTime = parseTimeOutput(output);

        } else {
            Base.writeLog("Error starting process to estimate print duration");
            estimatedTime = ERROR_MESSAGE;
            return "Error";
        }

//        System.out.println("Estimated time: "+estimatedTime);

        return estimatedTime;
    }

    private static String noDays(String duration) {
        String re1 = ".*?";	// Non-greedy match on filler
        String re2 = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";	// HourMinuteSec 1
        String time1 = "ND";

        Pattern p = Pattern.compile(re1 + re2, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(duration);
        if (m.find()) {
            time1 = m.group(1);
//                System.out.print("("+time1.toString()+")"+"\n");
        }

        return time1.toString();
    }

    private static String withDays(String duration) {

        String re1 = ".*?";	// Non-greedy match on filler
        String re2 = "\\s+";	// Uninteresting: ws
        String re3 = ".*?";	// Non-greedy match on filler
        String re4 = "(\\s+)";	// White Space 1
        String re5 = "(\\d+)";	// Integer Number 1
        String re6 = "(\\s+)";	// White Space 2
        String re7 = "((?:[a-z][a-z]+))";	// Word 1
        String re8 = "(,)";	// Any Single Character 1
        String re9 = "(\\s+)";	// White Space 3
        String re10 = "(\\d+)";	// Integer Number 2
        String re11 = "(:)";	// Any Single Character 2
        String re12 = "(\\d+)";	// Integer Number 3
        String re13 = "(:)";	// Any Single Character 3
        String re14 = "(\\d+)";	// Integer Number 4
        int nDays = 0;
        int nHours = 0;
        String time1 = "ND";

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10 + re11 + re12 + re13 + re14, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(duration);
        if (m.find()) {
            String ws1 = m.group(1);

            // Number of days
            String int1 = m.group(2);
            nDays = Integer.valueOf(int1);

            String ws2 = m.group(3);
            String word1 = m.group(4);
            String c1 = m.group(5);
            String ws3 = m.group(6);

            String hours = m.group(7);
            nHours = Integer.valueOf(hours);

            String c2 = m.group(8);
            String minutes = m.group(9);
            String c3 = m.group(10);
            String seconds = m.group(11);

            int nTotalHours = nDays * 24;
            hours = String.valueOf(nTotalHours + nHours);

            time1 = hours + c2 + minutes + c2 + seconds;
        }

        return time1;
    }

    private static String parseTimeOutput(String out) {
        if (!out.isEmpty()) {
            String timeParsed = "ND";

            if (out.toLowerCase().contains(dayKeyWord)) {
                timeParsed = withDays(out);
            } else {
                timeParsed = noDays(out);
            }


            return filterTime(timeParsed);
        }
        return "ND";
    }

    private static String filterTime(String baseEstimation) {
        String estimation = "ND";
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        String re1 = "(\\d+)";	// Integer Number 1
        String re2 = ".*?";	// Non-greedy match on filler
        String re3 = "(\\d+)";	// Integer Number 2
        String re4 = ".*?";	// Non-greedy match on filler
        String re5 = "(\\d+)";	// Ibasenteger Number 3

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(baseEstimation);
        if (m.find()) {
            //20% patch to correct the deviation of the estimation
            hours = (int) Math.ceil(Integer.valueOf(m.group(1)) * patch);
            minutes = (int) Math.ceil(Integer.valueOf(m.group(2)) * patch);
            seconds = (int) Math.ceil(Integer.valueOf(m.group(3)) * patch);
        }

        /**
         * Tune estimation after aplying the patch
         */
        int[] timeTuned = tuneEstimation(hours, minutes);
        hours = timeTuned[0];
        minutes = timeTuned[1];


        if (hours > 0) {
            return String.valueOf(hours).concat(":").concat(String.valueOf(minutes));
        }
        if (minutes == 0) {
            //Very small prints are estimated in 0 minutes. Lets assume 2 min for them
            return String.valueOf(minutes + 2);
        }
        return String.valueOf(minutes);

    }

    private static int[] tuneEstimation(int hr, int mnt) {

        int[] time = new int[2];

        int hours = hr;
        int minutes = mnt;

        if (minutes > 60) {
            hours += (minutes / 60);
            minutes = (minutes % 60);
        }

        time[0] = hours;
        time[1] = minutes;

        return time;

    }

    private static String parseCostOutput(String out) {

        String costParsed = ERROR_MESSAGE;

        if (!out.isEmpty()) {
            String tag = "Filament used: ";
            try{
                costParsed = out.substring(out.indexOf(tag), out.indexOf("mm")).split(tag)[1];
            }
            catch(StringIndexOutOfBoundsException e)
            {
                ;//Do nothing - just avoid throw
            }
            
        }
        return costParsed;
    }
}
