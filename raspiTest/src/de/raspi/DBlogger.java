/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.raspi;

import com.mysql.jdbc.Connection;
import static de.raspi.BMP180Test.bmp180;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.System.exit;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Jörg
 */
public class DBlogger {

    Connection connection = null;
    public static final String TABLENAME="sensordata";


    public DBlogger(String serverIP, String dbName, String user, String password) {
        Statement stmt = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = (Connection) DriverManager.getConnection("jdbc:mysql://" + serverIP + ":3306/" + dbName, user, password);
            createTable();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBlogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DBlogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConnection() {
        return connection;
    }
    
    public void log(DS18B20 sensor) {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            if (sensor!=null) try {
                stmt.execute("insert into "+TABLENAME+" (timestamp,temp,pressure) Values (now(),"+sensor.getTemperature().getTemperature()+","+0+")");
            } catch (IOException ex) {
                Logger.getLogger(DBlogger.class.getName()).log(Level.SEVERE, null, ex);
            }
            else stmt.execute("insert into "+TABLENAME+" (timestamp,temp,pressure) Values (now(),-1,0)");
        } catch (SQLException ex) {
            Logger.getLogger(DBlogger.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        limit(10000);

    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBlogger.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        if (args[0]==null) {
            System.out.println ("No path to config.json added!");
        }
        else {
            System.out.println ("read config.json: "+args[0]);
            try {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(new FileReader(args[0]));
                JSONObject jsonObject = (JSONObject) obj;
                System.out.println ("Conntect to MYSQL Server:"+jsonObject.get("dbserver"));
                DBlogger dbl = new DBlogger(jsonObject.get("dbserver").toString(), jsonObject.get("dbname").toString(), jsonObject.get("dbuser").toString(), jsonObject.get("dbpassword").toString());
                bmp180=BMP180.getInstance(1,0x77);
                Thread.sleep(2000);
                System.out.println ("Temperatur:"+bmp180.getTemperature()+" C");
                System.out.println ("Luftdruck:"+bmp180.getPressure()+" Pa");
                //dbl.log(bmp180.getValue());
                dbl.close();
            } catch (IOException ex) {
                Logger.getLogger(DBlogger.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println ("Kann Konfigurationsdatei nicht finden!");
                exit(1);
            } catch (ParseException ex) {
                Logger.getLogger(DBlogger.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println ("Kann Konfigurationsdatei hat ein flasches Format!");
                exit(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(DBlogger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }  
    }

    private void limit(int max) {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select count(*) as num from "+TABLENAME);
            rs.next();
            System.out.println ("Found "+rs.getString("num")+" rows!");
            int num = Integer.parseInt(rs.getString("num"));
            if (num>max) {
                stmt = connection.createStatement();
                stmt.execute("DELETE FROM "+TABLENAME+" ORDER BY id LIMIT "+(num-max));	
            }
                  
        } catch (SQLException ex) {
            Logger.getLogger(DBlogger.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createTable() {
        String sqlString="CREATE TABLE IF NOT EXISTS `"+TABLENAME+"` ( `id` int(11) NOT NULL AUTO_INCREMENT,"+
  "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"+
  "`temp` float DEFAULT NULL,"+
  "`pressure` int(11) DEFAULT NULL,"+
  "PRIMARY KEY (`id`)"+
") ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=latin1;";

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute(sqlString);
        } catch (SQLException ex) {
            Logger.getLogger(DBlogger.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
