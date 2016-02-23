/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tuttas.raspi;

import de.raspi.BlasterValueChanged;
import de.raspi.RPIServoBlasterExample;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


/**
 *
 * @author Jörg
 */
@ServerEndpoint("/blasterpoint")
public class BlasterWSEndpoint  implements BlasterValueChanged{

   Session session;
    
   RPIServoBlasterExample BlasterControl = RPIServoBlasterExample.getInstance(7);
    
 
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Dim Websocket blaster befor onOpen");
        this.session=session;
        //BlasterControl.addListener(this);
        System.out.println(session.getId() + " has opened a connection blaster");
        this.sendDimValue();
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Dim Websocket blaster befor onClose");
        //BlasterControl.removeListener(this);
        System.out.println("Session blaster " + session.getId() + " has ended");
        session=null;
    }

    @OnMessage
    public String onMessage(String message) {
        System.out.println("Dim Websocket blaster receive:" + message);
        //BlasterControl.dim(Integer.parseInt(message));
        return null;
    }

    private void sendDimValue() {
         try {
            System.out.println("Dim Websocket blaster Value");
            session.getBasicRemote().sendText("kurwa");
        } catch (IOException ex) {
            Logger.getLogger(BlasterWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
        try {
            System.out.println("Dim Websocket blaster Value");
            session.getBasicRemote().sendText(BlasterControl.getDimValue().toString());
        } catch (IOException ex) {
            Logger.getLogger(BlasterWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
                    */
    }

    @Override
    public void valueChanged(int v) {
        System.out.println("Dim Websocket blaster Change");
        this.sendDimValue();
    }

   
}
