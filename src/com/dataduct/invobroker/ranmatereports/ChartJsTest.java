/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dataduct.invobroker.ranmatereports;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer.Form;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 *
 * @author Mike
 */
public class ChartJsTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        String metric = "Calls"; // Traffic or Calls
        //String metric = "Traffic"; // Traffic or Calls
        
        // read script file
        try {
            engine.eval(Files.newBufferedReader(Paths.get("C:/Dataduct/Source/RANmateWeb/public_html/js/RANMateMetrics_ReportCharts.js"), StandardCharsets.UTF_8));
            engine.eval(Files.newBufferedReader(Paths.get("C:/Dataduct/Source/RANmateWeb/public_html/js/Chart_2_7_3.min.js"), StandardCharsets.UTF_8));
            
            Invocable inv = (Invocable) engine;
            
            /* Here's the SQL that's run to get the Traffic graph data
            DELETE FROM metrics.jflow_viewer_output_aggregated;
            INSERT INTO metrics.jflow_viewer_output_aggregated (SELECT DATE(measurement_time), site_name, operator_id, SUM(cs_inbound) AS 'Calls Inbound', SUM(ps_inbound) AS 'Data Inbound', SUM(signalling_inbound) AS 'Remainder Inbound',  SUM(total_inbound) AS 'Total Inbound',SUM(cs_outbound) AS 'Calls Outbound', SUM(ps_outbound) AS 'Data Outbound', SUM(signalling_outbound) AS 'Remainder Outbound',  SUM(total_outbound) AS 'Total Outbound'FROM metrics.jflow WHERE measurement_time BETWEEN '2019-02-22 06:30' AND '2019-02-23 12:30'  AND jflow.site_name REGEXP '^15 Bishopsgate' GROUP BY DATE(measurement_time), site_name, operator_id order by measurement_time, site_name, operator_id);
            DELETE FROM metrics.jflow_viewer_output_pivoted;
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Calls Inbound' AS Metric, site_name, case when operator_id = 1 then cs_inbound end, case when operator_id = 2 then cs_inbound end,case when operator_id = 3 then cs_inbound end, case when operator_id = 4 then cs_inbound end FROM metrics.jflow_viewer_output_aggregated);
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Data Inbound' AS Metric, site_name, case when operator_id = 1 then ps_inbound end, case when operator_id = 2 then ps_inbound end,case when operator_id = 3 then ps_inbound end, case when operator_id = 4 then ps_inbound end FROM metrics.jflow_viewer_output_aggregated);
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Remainder Inbound' AS Metric, site_name, case when operator_id = 1 then signalling_inbound end, case when operator_id = 2 then signalling_inbound end,case when operator_id = 3 then signalling_inbound end, case when operator_id = 4 then signalling_inbound end FROM metrics.jflow_viewer_output_aggregated);
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Total Inbound' AS Metric, site_name, case when operator_id = 1 then total_inbound end, case when operator_id = 2 then total_inbound end,case when operator_id = 3 then total_inbound end, case when operator_id = 4 then total_inbound end FROM metrics.jflow_viewer_output_aggregated);
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Calls Outbound' AS Metric, site_name, case when operator_id = 1 then cs_outbound end, case when operator_id = 2 then cs_outbound end,case when operator_id = 3 then cs_outbound end, case when operator_id = 4 then cs_outbound end FROM metrics.jflow_viewer_output_aggregated);
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Data Outbound' AS Metric, site_name, case when operator_id = 1 then ps_outbound end, case when operator_id = 2 then ps_outbound end,case when operator_id = 3 then ps_outbound end, case when operator_id = 4 then ps_outbound end FROM metrics.jflow_viewer_output_aggregated);
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Remainder Outbound' AS Metric, site_name, case when operator_id = 1 then signalling_outbound end, case when operator_id = 2 then signalling_outbound end,case when operator_id = 3 then signalling_outbound end, case when operator_id = 4 then signalling_outbound end FROM metrics.jflow_viewer_output_aggregated);
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Total Outbound' AS Metric, site_name, case when operator_id = 1 then total_outbound end, case when operator_id = 2 then total_outbound end,case when operator_id = 3 then total_outbound end, case when operator_id = 4 then total_outbound end FROM metrics.jflow_viewer_output_aggregated);
            SELECT (DATE(measurement_time)) AS measurement_time,  metric_name, COALESCE(ROUND(SUM(operator1)/1048576),0) AS VF, COALESCE(ROUND(SUM(operator2)/1048576),0) AS O2, COALESCE(ROUND(SUM(operator3)/1048576),0) AS THREE, COALESCE(ROUND(SUM(operator4)/1048576),0) AS EE FROM jflow_viewer_output_pivoted GROUP BY metric_name ORDER BY NULL;
            */            

            /* Here's the SQL that's run to get the Calls Graph Data
            DELETE FROM metrics.jflow_viewer_output_aggregated;
            INSERT INTO metrics.jflow_viewer_output_aggregated (SELECT DATE(measurement_time), site_name, operator_id, SUM(cs_inbound) AS 'Calls Inbound', SUM(ps_inbound) AS 'Data Inbound', SUM(signalling_inbound) AS 'Remainder Inbound',  SUM(total_inbound) AS 'Total Inbound',SUM(cs_outbound) AS 'Calls Outbound', SUM(ps_outbound) AS 'Data Outbound', SUM(signalling_outbound) AS 'Remainder Outbound',  SUM(total_outbound) AS 'Total Outbound'FROM metrics.jflow WHERE measurement_time BETWEEN '2019-02-22 06:30' AND '2019-02-23 12:30'  AND jflow.site_name REGEXP '^15 Bishopsgate' GROUP BY DATE(measurement_time), site_name, operator_id order by measurement_time, site_name, operator_id);
            DELETE FROM metrics.jflow_viewer_output_pivoted;
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Num Calls Inbound' AS Metric, site_name, case when operator_id = 1 then cs_inbound end, case when operator_id = 2 then cs_inbound end,case when operator_id = 3 then cs_inbound end, case when operator_id = 4 then cs_inbound end FROM metrics.jflow_viewer_output_aggregated);
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Num Calls Outbound' AS Metric, site_name, case when operator_id = 1 then cs_outbound end, case when operator_id = 2 then cs_outbound end,case when operator_id = 3 then cs_outbound end, case when operator_id = 4 then cs_outbound end FROM metrics.jflow_viewer_output_aggregated);
            INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT measurement_time, 'Total Num Calls ' AS Metric, site_name, sum(operator1), sum(operator2), sum(operator3), sum(operator4) FROM metrics.jflow_viewer_output_pivoted);
            SELECT (DATE(measurement_time)) AS measurement_time,  metric_name, COALESCE(ROUND(SUM(operator1)/(1048576 * 0.3)),0) AS VF, COALESCE(ROUND(SUM(operator2)/(1048576 * 0.3)),0) AS O2, COALESCE(ROUND(SUM(operator3)/(1048576 * 0.3)),0) AS THREE, COALESCE(ROUND(SUM(operator4)/(1048576 * 0.3)),0) AS EE FROM jflow_viewer_output_pivoted GROUP BY metric_name ORDER BY NULL;            
            */
                        
            String sql = "";
            String startTime = "2019-01-01 00:00";
            String endTime = "2019-01-31 23:59";
            String[] sites = new String[] {"Ladbroke Grove"};            
            
            // getTrafficSQL(metric, startDateTime, endDateTime, sites, false, false)
            // getCallsSQL(metric, startDateTime, endDateTime, sites, false, false);
            if (metric.equals("Traffic")) {
                sql = (String)inv.invokeFunction("getTrafficSQL", metric, startTime, endTime, sites, false, false);
            } else if (metric.equals("Calls")) {
                sql = (String)inv.invokeFunction("getCallsSQL", metric, startTime, endTime, sites, false, false);                
            } else {
                System.out.println("Unexpected Metric for PDF Reports");
            }            
            //System.out.println("SQL in Java = " + sql);
            
            // This is the current response text for Traffic metrics
            // response text is [{"time":"2019-02-22","metric_name":"Calls Inbound","VF":"0.0000","O2":"5.3872","THREE":"76.1319","EE":"18.4809"},{"time":"2019-02-22","metric_name":"Data Inbound","VF":"0.0000","O2":"5.0284","THREE":"71.3752","EE":"23.5964"},{"time":"2019-02-22","metric_name":"Remainder Inbound","VF":"0.0000","O2":"10.8652","THREE":"53.0303","EE":"36.1045"},{"time":"2019-02-22","metric_name":"Total Inbound","VF":"0.0000","O2":"5.1957","THREE":"71.0653","EE":"23.7390"},{"time":"2019-02-22","metric_name":"Calls Outbound","VF":"0.0000","O2":"6.8360","THREE":"74.9721","EE":"18.1918"},{"time":"2019-02-22","metric_name":"Data Outbound","VF":"0.0000","O2":"5.9440","THREE":"68.9900","EE":"25.0660"},{"time":"2019-02-22","metric_name":"Remainder Outbound","VF":"0.0000","O2":"10.8260","THREE":"53.1997","EE":"35.9743"},{"time":"2019-02-22","metric_name":"Total Outbound","VF":"0.0000","O2":"6.4876","THREE":"68.3366","EE":"25.1757"}]
            // This is the current response text for Calls metrics
            // response text is [{"time":"2019-02-22","metric_name":"Num Calls Inbound","VF":"0","O2":"13","THREE":"186","EE":"45"},{"time":"2019-02-22","metric_name":"Num Calls Outbound","VF":"0","O2":"17","THREE":"186","EE":"45"},{"time":"2019-02-22","metric_name":"Total Num Calls ","VF":"0","O2":"30","THREE":"372","EE":"90"}]
            // same as above but nicely formatted
            /* response text is [{"time":"2019-02-22","metric_name":"Calls Inbound","VF":"0.0000","O2":"5.3872","THREE":"76.1319","EE":"18.4809"},
                                 {"time":"2019-02-22","metric_name":"Data Inbound","VF":"0.0000","O2":"5.0284","THREE":"71.3752","EE":"23.5964"},
                                 {"time":"2019-02-22","metric_name":"Remainder Inbound","VF":"0.0000","O2":"10.8652","THREE":"53.0303","EE":"36.1045"},
                                 {"time":"2019-02-22","metric_name":"Total Inbound","VF":"0.0000","O2":"5.1957","THREE":"71.0653","EE":"23.7390"},
                                 {"time":"2019-02-22","metric_name":"Calls Outbound","VF":"0.0000","O2":"6.8360","THREE":"74.9721","EE":"18.1918"},
                                 {"time":"2019-02-22","metric_name":"Data Outbound","VF":"0.0000","O2":"5.9440","THREE":"68.9900","EE":"25.0660"},
                                 {"time":"2019-02-22","metric_name":"Remainder Outbound","VF":"0.0000","O2":"10.8260","THREE":"53.1997","EE":"35.9743"},
                                 {"time":"2019-02-22","metric_name":"Total Outbound","VF":"0.0000","O2":"6.4876","THREE":"68.3366","EE":"25.1757"}] */
            /* response text is [{"time":"2019-02-22","metric_name":"Num Calls Inbound","VF":"0","O2":"13","THREE":"186","EE":"45"},
                                 {"time":"2019-02-22","metric_name":"Num Calls Outbound","VF":"0","O2":"17","THREE":"186","EE":"45"},
                                 {"time":"2019-02-22","metric_name":"Total Num Calls ","VF":"0","O2":"30","THREE":"372","EE":"90"}] */
            
            // of the above we want to be able to generate individually
            //                     [{"time":"2019-02-22","metric_name":"Data Download","VF":"0.0000","O2":"5.0284","THREE":"71.3752","EE":"23.5964"}]
            //                     [{"time":"2019-02-22","metric_name":"Data Upload","VF":"0.0000","O2":"5.9440","THREE":"68.9900","EE":"25.0660"}]
            //                     [{"time":"2019-02-22","metric_name":"Total Num Calls ","VF":"0","O2":"30","THREE":"372","EE":"90"}]

            // commented out for now so as to continue with Concert independent testing
            //KeyValuePair<String[], String[][]> configNames = getIms().executeMySQLQuery("InterSystemAgent", "select DISTINCT system, configuration from ConfigTemplate ORDER by system", true, null, null, Form.getUserBank().getUsername());            
            String responseText = "[{\"time\":\"2019-02-22\",\"metric_name\":\"Total Num Calls \",\"VF\":\"0\",\"O2\":\"30\",\"THREE\":\"372\",\"EE\":\"90\"}]";
            // call function from script file
            inv.invokeFunction("showGraph", 
                    "graph",
                    true,
                    new String[] {"Ladbroke Grove"},   // site
                    "Traffic",
                    startTime,      // start time
                    endTime,        // end time
                    responseText, 
                    false           // Bar Chartf format, if false, then it's a pie chart
                    // replaces the httpRequest response text
                    
                    );            
        } catch (Exception e) {
            System.out.println("Error executing Javascript: " + e.getMessage());
        }
    }
    
}
