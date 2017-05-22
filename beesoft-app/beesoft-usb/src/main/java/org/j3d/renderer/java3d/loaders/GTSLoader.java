package org.j3d.renderer.java3d.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.net.URL;
import java.util.logging.Level;

import replicatorg.app.Base;

import org.scijava.java3d.loaders.IncorrectFormatException;
import org.scijava.java3d.loaders.LoaderBase;
import org.scijava.java3d.loaders.ParsingErrorException;
import org.scijava.java3d.loaders.Scene;

public class GTSLoader extends LoaderBase {

	public Scene load(String filename) throws FileNotFoundException,
			IncorrectFormatException, ParsingErrorException {
		File file = new File(filename);
		return load(new BufferedReader(new FileReader(file)));
	}

	public Scene load(URL url) throws FileNotFoundException,
			IncorrectFormatException, ParsingErrorException {
		assert(url != null);
        try
        {
			InputStream is = url.openStream();
			return load(new BufferedReader(new InputStreamReader(is)));
        }
        catch( InterruptedIOException ie )
        {
            // user cancelled loading
            return null;
        }
        catch( IOException e )
        {
        	//Base.logger.log(Level.SEVERE,"Could not open URL "+url.toString(),e);
        	return null;
        }
    }

	public Scene load(Reader reader) throws FileNotFoundException,
			IncorrectFormatException, ParsingErrorException {
		// TODO
		return null;
	}

}
