package replicatorg.app.util;


import replicatorg.app.ProperDefault;
import replicatorg.app.Base;

/**
 * Utilities for detecting python versions and running python apps or scripts from
 * SimpleG.
 * @author phooky
 * Copyright (c) 2013 BEEVC - Electronic Systems
 *
 */
public class PythonUtils {
	/**
	 * Preference name for preferred Python path.
	 */
	final static String PYTON_PATH_PREF = Base.getApplicationDirectory().getPath();
        
    //"/home/bitbox/git/bitbucket-bitbox/r2c2_software/ReplicatorG/resources/bin/pypy";
	
	/**
	 * Callback for Python selector method.
	 */
	public interface Selector {
		/**
		 * Select the path to the desired Python implementation.
		 * @param candidates a list of paths to candidate implementations 
		 * @return the selected path (which need not be a member of the candidate list)
		 */
		//String selectPythonPath(Vector<String> candidates);
	}
	
	/**
	 * Class representing a Python version.  The members are directly accessible. 
	 * @author phooky
	 */
	public static class Version implements Comparable<Version> {
		public int major;
		public int minor;
		public int revision;
		public Version(int major, int minor, int revision) {
			this.major = major;
			this.minor = minor;
			this.revision = revision;
		}
		/// returns 0 on match, 1 if this version is newer than other, -1 if this version is older than other
		public int compareTo(Version other) {
			if (major < other.major) return -1;
			if (major > other.major) return 1;
			if (minor < other.minor) return -1;
			if (minor > other.minor) return 1;
			if (revision < other.revision) return -1;
			if (revision > other.revision) return 1;
			return 0;
		}
		
		public String toString() {
			return Integer.toString(major)+"."+Integer.toString(minor)+"."+Integer.toString(revision);
		}
	}
	
	static String pythonPath = null;
	static Version pythonVersion = null;
	
	/**
	 * Calculate the expected path to the Python installation.  The result is cached.
	 * @return the path as a string
	 */
	public static String getPythonPath(){
	
		String os_name = System.getProperty("os.name").toLowerCase();
		
		try {
			if (os_name.contains("mac")){
                                ProperDefault.put(PYTON_PATH_PREF, PYTON_PATH_PREF.concat("/pypy/bin/pypy"));
				return PYTON_PATH_PREF.concat("/pypy/bin/pypy");
			}
			if (os_name.contains("win")){
                                ProperDefault.put(PYTON_PATH_PREF, PYTON_PATH_PREF.concat("/pypy/pypy.exe"));
				return PYTON_PATH_PREF.concat("/pypy/pypy.exe");
			}
			if (os_name.contains("nux")|os_name.contains("nix")|os_name.contains("aix")){
                                if(Base.isx86())
                                {
                                    ProperDefault.put(PYTON_PATH_PREF, PYTON_PATH_PREF.concat("/pypy/bin/pypy"));
                                    return PYTON_PATH_PREF.concat("/pypy/bin/pypy");
                                }
                                else
                                {
                                    ProperDefault.put(PYTON_PATH_PREF, PYTON_PATH_PREF.concat("/pypy/bin/unix64/pypy"));
                                    return PYTON_PATH_PREF.concat("/pypy/bin/unix64/pypy");
                                }
                        }
		} catch (Exception e) {
//			Base.showWarning("Pypy", "Fatal Error! Could not find OS! Reinstall Aplication.", e);
		}

		return null;

	}
	
}
