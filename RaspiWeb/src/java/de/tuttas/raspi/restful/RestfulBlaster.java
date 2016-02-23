/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tuttas.raspi.restful;

import de.raspi.BlasterDimValue;
import de.raspi.RPIServoBlasterExample;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Jörg
 */
@Path("blaster")
public class RestfulBlaster {
    
     RPIServoBlasterExample BlasterControl = RPIServoBlasterExample.getInstance(7);
        
     @GET
     @Consumes(MediaType.APPLICATION_JSON)
     public BlasterDimValue getDimValue() {
         return BlasterControl.getDimValue();
     }
     
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     public BlasterDimValue setDimValue(BlasterDimValue d) {
         BlasterControl.dim(d.getDim());
         return BlasterControl.getDimValue();
     }
}
