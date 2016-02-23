
package de.tuttas.raspi;


import de.raspi.LED;
import de.raspi.RPIServoBlasterExample;
import javax.ejb.Stateful;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Jörg
 */
@ManagedBean
@Stateful
public class RaspiBean {

    
    int ledState;
    int ledGlow;
    int temp;
    LED ledControl= LED.getInstance(18);

    int BlasterState;
    int BlasterGlow;
    int tempBlaster;
    RPIServoBlasterExample BlasterControl= RPIServoBlasterExample.getInstance(7);
    
    public void setLedState(int ledState) {
        System.out.println ("Set LED State="+ledState);
        if (ledState==1) {
            if (ledControl!=null) ledControl.turnOn(true);
        }
        else {
            if (ledControl!=null) ledControl.turnOn(false);
        }
        this.ledState = ledState;
    }
    
    public int getLedState() {
        return ledState;
                
    }

    public void setLedGlow(int ledGlow) {
        System.out.println ("Set LED Glow to "+ledGlow+" %");
        this.ledGlow = ledGlow;
        if (ledControl!=null) ledControl.dim(ledGlow);
    }

    public int getLedGlow() {
        return ledGlow;
    }
    
    public void setBlasterState(int BlasterState) {
        System.out.println ("Set LED State="+ledState);
        if (BlasterState==1) {
            if (BlasterControl!=null) BlasterControl.turnOn(true);
        }
        else {
            if (BlasterControl!=null) BlasterControl.turnOn(false);
        }
        this.BlasterState = BlasterState;
    }
    
    public int getBlusterState() {
        return BlasterState;
                
    }

    public void setBlasterGlow(int ledGlow) {
        System.out.println ("Set LED Glow to "+BlasterGlow+" %");
        this.BlasterGlow = BlasterGlow;
        if (BlasterControl!=null) BlasterControl.dim(BlasterGlow);
    }

    public int getBlusterGlow() {
        return BlasterGlow;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getTemp() {
        return temp;
    }
    
   
    
    
}
