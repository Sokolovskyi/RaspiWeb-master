/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.raspi;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jörg
 */
public class BMP180Test {

    static BMP180 bmp180;


    

    public static void main(String[] args) {
            bmp180=BMP180.getInstance(1,0x77);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(BMP180Test.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println ("Temperatur:"+bmp180.getTemperature()+" C");
            System.out.println ("Luftdruck:"+bmp180.getPressure()+" Pa");
            
    }

}
