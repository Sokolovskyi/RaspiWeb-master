package de.raspi;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  RPIServoBlasterExample.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2015 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
import com.pi4j.component.servo.ServoDriver;
import com.pi4j.component.servo.ServoProvider;
import com.pi4j.component.servo.impl.RPIServoBlasterProvider;
import java.io.IOException;
import java.util.ArrayList;


public class RPIServoBlasterExample implements Runnable {
    
    private long onTime = 0;
    private Thread dimmer;
    private boolean running = false;
    private BlasterDimValue dimValue;
    private ArrayList<BlasterValueChanged> listeners = new ArrayList<>();
    private static RPIServoBlasterExample[] instances = new RPIServoBlasterExample[28];
    private int pin;
  
    private RPIServoBlasterExample(int pin) {
    this.pin=pin;
    dimValue=new BlasterDimValue();
    //init(pin);        
    dimmer = new Thread(this);
    running = true;
    dimmer.start();
    }
    
    public static RPIServoBlasterExample getInstance(int pin) {
        if (instances[pin] == null) {
            instances[pin] = new RPIServoBlasterExample(pin);
        }
        return instances[pin];
    }
    
    public void turnOn(boolean b) {
    if (b) {
        this.dim(100);
    } else {
        this.dim(0);

    }
}
    
    public BlasterDimValue getDimValue() {
        return dimValue;
    }
    
    public void dim(int i) {
        dimValue.setDim(i);
        onTime = 20 * i / 100;
        System.out.println("Dimmer set to " + i + "% onTime is " + onTime + " ms");
        for (BlasterValueChanged l : listeners) {
            l.valueChanged(i);
        }
    }

    public void addListener(BlasterValueChanged aThis) {
        listeners.add(aThis);
    }

    public void removeListener(BlasterValueChanged aThis) {
        listeners.remove(aThis);
    }

    private void init(int pin) throws IOException {        
        ServoProvider servoProvider = new RPIServoBlasterProvider();
        ServoDriver servo = servoProvider.getServoDriver(servoProvider.getDefinedServoPins().get(pin));
    }
    
    
    public static void main(String[] args) throws Exception {
        ServoProvider servoProvider = new RPIServoBlasterProvider();
        
        ServoDriver servo7 = servoProvider.getServoDriver(servoProvider.getDefinedServoPins().get(7));
        
        long start = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - start < 120000) { // 2 minutes
            
            for (int i = 50; i < 150; i++) {
                servo7.setServoPulseWidth(i); // Set raw value for this servo driver - 50 to 195
                Thread.sleep(10);
            }
            
            for (int i = 150; i > 50; i--) {
                servo7.setServoPulseWidth(i); // Set raw value for this servo driver - 50 to 195
                Thread.sleep(10);
            }
            
        }
        
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

