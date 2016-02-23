/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tuttas.raspi;

import de.raspi.BMP180;
import de.raspi.SensorValue;
import de.raspi.Config;
import de.raspi.DBlogger;
import de.raspi.DS18B20;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Jörg
 */
@WebServlet(name = "SensorServlet", urlPatterns = {"/SensorServlet"})
public class SensorServlet extends HttpServlet {

    private String errorMsg = null;
    private String outFormat = "plain";
    private DBlogger dblogger;
    private ArrayList<SensorValue> data;
    private DS18B20 sensor;

    @Override
    public void init() throws ServletException {
            super.init(); //To change body of generated methods, choose Tools | Templates.
            sensor = new DS18B20(Config.SensorAdr);
            Statement stmt = null;
            try {
                JSONParser parser = new JSONParser();
                ServletContext cntxt = this.getServletContext();
                InputStream ins = cntxt.getResourceAsStream("/config.json");
                String inString = "";
                if (ins != null) {
                    InputStreamReader isr = new InputStreamReader(ins);
                    BufferedReader reader = new BufferedReader(isr);
                    int n = 0;
                    String word = "";
                    while ((word = reader.readLine()) != null) {
                        inString += word;
                    }
                    Object obj = parser.parse(inString);
                    JSONObject jsonObject = (JSONObject) obj;
                    dblogger = new DBlogger(jsonObject.get("dbserver").toString(), jsonObject.get("dbname").toString(), jsonObject.get("dbuser").toString(), jsonObject.get("dbpassword").toString());
                    System.out.println("Servlet connected to DB Server@" + jsonObject.get("dbserver").toString());
                    errorMsg = null;
                } else {
                    errorMsg = "Failed to load config.json!";
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SensorServlet.class.getName()).log(Level.SEVERE, null, ex);
                errorMsg = "Failed to load config.json";
            } catch (IOException ex) {
                Logger.getLogger(SensorServlet.class.getName()).log(Level.SEVERE, null, ex);
                errorMsg = "Failed to load config.json";
            } catch (ParseException ex) {
                Logger.getLogger(SensorServlet.class.getName()).log(Level.SEVERE, null, ex);
                errorMsg = "config.json not a correct json File";
            }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try (PrintWriter out = response.getWriter()) {
            if (errorMsg != null) {
                response.setContentType("text/html;charset=UTF-8");
                out.println("<h1>" + errorMsg + "</h1>");
            } else {
                if (outFormat.compareTo("plain") == 0) {
                    response.setContentType("text/html;charset=UTF-8");
                } else if (outFormat.compareTo("csv") == 0) {
                    response.setContentType("text/csv;charset=UTF-8");
                    response.addHeader("Content-Disposition", "attachment;filename=sensordata.csv");
                    out.println("\"TimeStamp\";\"Datetime\";\"Temperature\";\"Pressure\"");
                } else if (outFormat.compareTo("xml") == 0) {
                    response.setContentType("text/xml;charset=UTF-8");
                    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    out.println("<sensordata>");
                } else if (outFormat.compareTo("json") == 0) {
                    response.setContentType("application/json;charset=UTF-8");
                    out.print("{\"sensordata\" : [");
                } else {
                    response.setContentType("text/html;charset=UTF-8");
                    out.println("<html><head><title>Sensordata</title>");
                    out.println("<link rel=\"stylesheet\" href=\"css/sensordata.css\" />");
                    out.println("</head><body>");
                }
                for (SensorValue v : data) {
                    if (outFormat.compareTo("plain") == 0) {
                        out.print(v.toString());
                    } else if (outFormat.compareTo("csv") == 0) {
                        out.print(v.toCsv());
                    } else if (outFormat.compareTo("xml") == 0) {
                        out.print(v.toXml());
                    } else if (outFormat.compareTo("json") == 0) {
                        out.println(v.toJson(data.indexOf(v) == data.size() - 1));
                    } else {
                        out.print(v.toHtml());
                    }
                }
                if (outFormat.compareTo("plain") == 0) {
                } else if (outFormat.compareTo("csv") == 0) {
                } else if (outFormat.compareTo("xml") == 0) {
                    out.println("</sensordata>");
                } else if (outFormat.compareTo("json") == 0) {
                    out.print("]}");
                } else {
                    out.println("</body></html>");
                }

            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        data = new ArrayList<SensorValue>();
        String sqlFrom = "'" + new Timestamp(0).toString() + "'";
        String sqlTo = "'" + new Timestamp(GregorianCalendar.getInstance().getTimeInMillis()).toString() + "'";
        int last = -1;
        if (request.getParameter("from") != null) {
            sqlFrom = request.getParameterValues("from")[0];
        }
        if (request.getParameter("to") != null) {
            sqlTo = request.getParameterValues("to")[0];
        }
        String sql = "select * from " + DBlogger.TABLENAME + " where timestamp BETWEEN " + sqlFrom + " AND " + sqlTo + " ORDER BY id DESC";
        if (request.getParameter("out") != null) {
            outFormat = request.getParameterValues("out")[0];
        } else {
            outFormat = "html";
        }
        if (request.getParameter("last") != null) {
            last = Integer.parseInt(request.getParameterValues("last")[0]);
            sql = sql + " LIMIT " + (last);
        }
        if (request.getParameter("from") == null
                && request.getParameter("to") == null
                && request.getParameter("last") == null) {
            data.add(sensor.getValue());
        } else {
            //errorMsg = sql;
            Statement stmt = null;
            try {
                stmt = dblogger.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    data.add(new SensorValue(rs.getTimestamp("timestamp"), rs.getFloat("temp"), rs.getInt("pressure")));

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

        if (request.getParameter("log") != null) {
            if (request.getParameterValues("log")[0].compareTo("true") == 0) {
                dblogger.log(sensor);

            }
        }
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
