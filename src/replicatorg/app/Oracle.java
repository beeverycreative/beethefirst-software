package replicatorg.app;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.j3d.loaders.stl.STLFileReader;
import replicatorg.util.Point5d;

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
public class Oracle {

    private static double acceleration = 1000;
    private static double feedRate = 1000;
    private static Point5d start,finish;
    private static double estimatedGCodeTime = 0;
    private static double startTime = 0;
    
    // REDSOFT: This must be able to be performed using Scene or Model as argument
    //       and then accessing n_Facets per model to be printed
    
    /**
     * Get GCode generation Time based on a gross estimation on Facets number.
     * @param url URL to STL file
     * @return Process Time
     * 
     */
    public static double getGCodeGerationTime(File stl)
    {
        int n_facets = 0;
        int n_objects = 0;
        double layer_height = 0.1;
        STLFileReader fileReader = null;
        
        // STL Reader 
        try {
                fileReader = new STLFileReader(stl);
        } catch (IOException ex) {
            Logger.getLogger(Oracle.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Number of objects 
        n_objects = fileReader.getNumOfFacets().length;
        
        // Number of facets
        for(int i = 0; i < n_objects; i++)
        {
            n_facets += fileReader.getNumOfFacets()[i];
        }
        
        // Determines layer height
//        if(CuraGenerator.getSelectedProfile().toLowerCase().contains("low"))
//            layer_height = 0.3;
        
        double keyValue = Double.parseDouble(ProperDefault.get("keyValue"));

        estimatedGCodeTime = (n_facets/layer_height) *keyValue;
        ProperDefault.put("keyValue",String.valueOf(keyValue));
        return estimatedGCodeTime;
    }
    
    private static void resetVariables()
    {
        startTime = 0;
        estimatedGCodeTime = 0;
    }
    
    public static void setTic()
    {
        startTime = System.currentTimeMillis();
    }
    
    public static void setToc()
    {
        // Calculates real generation time
        double timeSplit = System.currentTimeMillis() - startTime;
//        EditingModel model2 = Base.getMainWindow().canvas.getModel();
        
//        System.out.println("Time for GCode generation---->" + timeSplit);

//        if(model2.getScale() > 0)
//        ProperDefault.put("keyValue", String.valueOf((estimatedGCodeTime/timeSplit)*model2.getScale()));
//        else
//            ProperDefault.put("keyValue", String.valueOf((estimatedGCodeTime/timeSplit)));
        
//       Approves new ratio
        if(timeSplit >= estimatedGCodeTime )
        {
//           System.out.println("keyvalue= "+(1+((timeSplit-estimatedGCodeTime)/timeSplit)));
            ProperDefault.put("keyValue", String.valueOf((1+ ((timeSplit-estimatedGCodeTime)/timeSplit))));
        }            
        else
        {
//            System.out.println("keyvalue= "+(1/(1+((estimatedGCodeTime-timeSplit)/estimatedGCodeTime))));
            ProperDefault.put("keyValue", String.valueOf(1/(1+((estimatedGCodeTime-timeSplit)/estimatedGCodeTime))));
        }
//            
//        
        resetVariables();
    }
    
    
}
