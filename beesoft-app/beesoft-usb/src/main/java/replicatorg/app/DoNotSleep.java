package replicatorg.app;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.*;

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
public class DoNotSleep{
    
    public static Timer timer=null;
    public void DisableSleep() throws Exception{
        int mseg = (1000*30);
        
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                try {
                    
                    Robot hal = new Robot();
                    Point pObj;
                    PointerInfo pi = MouseInfo.getPointerInfo();
                    if (pi != null){
                        pObj = pi.getLocation();
                        //WriteToConsole(pObj.toString() + "x>>" + pObj.x + "  y>>" +   pObj.y);
                        hal.mouseMove(pObj.x - 1, pObj.y);
                        pObj = pi.getLocation();
                        hal.mouseMove(pObj.x + 1, pObj.y);
                       
                    }
                    else{
                        Toolkit tk = Toolkit.getDefaultToolkit();
                        tk.beep();
                        hal.mouseMove(1, 1);
                        hal.mouseMove(2, 2);
                        
                    }
                } catch (Exception ex){
                }
            }
        }, mseg, mseg);
    }
        
    public void EnabledSleep() throws Exception{
        try{
            if (timer != null){
                timer.cancel();
            }
		} catch (Exception ex){
		}
    }
    
}