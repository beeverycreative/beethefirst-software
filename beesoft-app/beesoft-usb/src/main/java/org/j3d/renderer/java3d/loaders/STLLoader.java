/*****************************************************************************
 * STLLoader.java
 * Java Source
 *
 * This source is licensed under the GNU LGPL v2.1.
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information.
 *
 * Copyright (c) 2001, 2002 Dipl. Ing. P. Szawlowski
 * University of Vienna, Dept. of Medical Computer Sciences
 ****************************************************************************/

package org.j3d.renderer.java3d.loaders;

// Local imports
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Vector3d;

import org.j3d.loaders.stl.STLFileReader;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.LoaderBase;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;
import java.io.Serializable;
import replicatorg.app.Base;

/**
 * Class to load objects from a STL (Stereolithography) file into Java3D.<p>
 * In case that the file uses the binary STL format, no check can be done to
 * assure that the file is in STL format. A wrong format will only be
 * recognized if an invalid amount of data is contained in the file.<p>
 * @author  Dipl. Ing. Paul Szawlowski -
 *          University of Vienna, Dept. of Medical Computer Sciences
 * @version $Revision: 1.2 $
 */
public class STLLoader extends LoaderBase implements Serializable
{
    private final Component itsParentComponent;
    private boolean         itsShowProgress = false;

    /**
     * Creates a STLLoader object.
     */
    public STLLoader( )
    {
        itsParentComponent = null;
    }

    /**
     * Creates a STLLoader object which shows the progress of reading. Effects
     * only {@link #load( URL )} and {@link #load( String )}.
     * @param parentComponent Parent <code>Component</code> of progress monitor.
     *      Use <code>null</code> if there is no parent.
     */
    public STLLoader( final Component parentComponent )
    {
        itsParentComponent = parentComponent;
        itsShowProgress = true;
    }

    /**
     * Loads a STL file from a file. The data may be in ASCII or binary
     * format.<p>
     * The <code>getNamedObjects</code> method of the <code>Scene</code> object
     * will return <code>Shape3D</code> objects with no <code>Appearance</code>
     * set.
     * @return <code>Scene object</code> of the content of <code>fileName</code>
     *      or <code>null</code> if user cancelled loading (only possible if
     *      progress monitoring is enabled).
     */
    public Scene load( String fileName ) throws FileNotFoundException,
    IncorrectFormatException, ParsingErrorException
    {
        try
        {
            return load( new File(fileName).toURI().toURL() );
        }
        catch( MalformedURLException e )
        {
            throw new FileNotFoundException( );
        }
    }

    /**
     * Loads a STL file from an URL. The data may be in ASCII or binary
     * format.<p>
     * The <code>getNamedObjects</code> method of the <code>Scene</code> object
     * will return <code>Shape3D</code> objects with no <code>Appearance</code>
     * set.
     * @return <code>Scene object</code> of the content of <code>url</code> or
     *      <code>null</code> if user cancelled loading (only possible if
     *      progress monitoring is enabled).
     */
    public Scene load( URL url ) throws FileNotFoundException,
    IncorrectFormatException, ParsingErrorException
    {
        STLFileReader reader = null;
        try
        {
            if( itsShowProgress )
            {
                reader = new STLFileReader( url, itsParentComponent );
            }
            else
            {
                reader = new STLFileReader( url );
            }
            return createScene( reader );
        }
        catch( InterruptedIOException ie )
        {
            // user cancelled loading
            return null;
        }
        catch( IOException e )
        {
            throw new IncorrectFormatException( e.toString( ) );
        }
    }

    /**
     * Loading from a <code>Reader</code> object not supported.
     * @return <code>null</code>
     */
    public Scene load( Reader reader ) throws FileNotFoundException,
    IncorrectFormatException, ParsingErrorException
    {
        /** @todo loading from Reader object */
        return null;
    }

    /**
     * Creates a <code>Scene</code> object with the contents of the STL file.
     * Closes the reader after finishing reading.
     * @param reader <code>STLFileReader</code> object for reading the STL file.
     */
    public static Scene createScene( final STLFileReader reader )
    throws IncorrectFormatException, ParsingErrorException
    {
        try
        {
            final SceneBase scene = new SceneBase( );
            final BranchGroup bg = new BranchGroup( );
            final int numOfObjects = reader.getNumOfObjects( );
            final int[ ] numOfFacets = reader.getNumOfFacets( );
            final String[ ] names = reader.getObjectNames( );

            final double[ ] normal = new double[ 3 ];
            final float[ ] fNormal = new float[ 3 ];
            final double[ ][ ] vertices = new double[ 3 ][ 3 ];
            
            if(numOfObjects == 0 || numOfFacets.length == 0)
            {
                Base.getMainWindow().showFeedBackMessage("modelMeshError");
                return null;
            }
            
            for( int i = 0; i < numOfObjects; i ++ )
            {
                final TriangleArray geometry = new TriangleArray
                (
                    3 * numOfFacets[ i ],
                    TriangleArray.NORMALS | TriangleArray.COORDINATES
                );
                int index = 0;
                for( int j = 0; j < numOfFacets[ i ]; j ++ )
                {
                    final boolean ok = reader.getNextFacet( normal, vertices );
                    if( ok )
                    {
                        fNormal[ 0 ] = ( float ) normal[ 0 ];
                        fNormal[ 1 ] = ( float ) normal[ 1 ];
                        fNormal[ 2 ] = ( float ) normal[ 2 ];
                        if (
                        		fNormal[0] == 0 &&
                        		fNormal[1] == 0 &&
                        		fNormal[2] == 0)
                        {
                        	// Calculate normal
                        	Vector3d v0 = new Vector3d(vertices[0]);
                        	v0.negate();
                        	Vector3d v1 = new Vector3d(vertices[1]);
                        	v1.add(v0);
                        	Vector3d v2 = new Vector3d(vertices[2]);
                        	v2.add(v0);
                        	Vector3d n = new Vector3d();
                        	n.cross(v1,v2);
                        	n.normalize();
                        	fNormal[0] = (float)n.x;
                        	fNormal[1] = (float)n.y;
                        	fNormal[2] = (float)n.z;
                        }
                        for( int k = 0; k < 3; k ++ )
                        {
                            geometry.setNormal( index, fNormal );
                            geometry.setCoordinate( index, vertices[ k ] );
                            index ++;
                        }
                    }
                    else
                    {
                        throw new ParsingErrorException( );
                    }
                }
                final Shape3D shape = new Shape3D( geometry );
                bg.addChild( shape );
                String name = names[ i ];
                if( name == null )
                {
                    name = new String( "Unknown_" + i );
                }
                scene.addNamedObject( name, shape );
            }
            scene.setSceneGroup( bg );
            return scene;
        }
        catch( InterruptedIOException ie )
        {
            Base.getMainWindow().showFeedBackMessage("modelMeshError");
            // user cancelled loading
            return null;
        }
        catch( IOException e )
        {
            Base.getMainWindow().showFeedBackMessage("modelMeshError");
            throw new ParsingErrorException( e.toString( ) );
        }
        finally
        {
            try
            {
                reader.close( );
            }
            catch( IOException e )
            {
                e.printStackTrace( );
            }
        }
    }
}