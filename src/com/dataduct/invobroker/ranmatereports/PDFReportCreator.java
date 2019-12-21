/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dataduct.invobroker.ranmatereports;

import com.dataduct.invobroker.imsManager.ImsManager;
import com.dataduct.invobroker.utils.KeyValuePair;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.PieDataset;
        
/**
 *
 * @author Mike
 */
public class PDFReportCreator {

    // if in TEST_MODE, then we're not executing from within Concert and all the lovely DB library access that brings
    // plus we're writing to Client PC - side files
    private final static boolean TEST_MODE = true;
    private final static String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
        //private final static String DB_URL = "jdbc:mysql://185.171.220.1/metrics";
    private final static String DB_URL = "jdbc:mysql://185.171.220.1/OpenCellCM";
    private final static String USER = "dataduct";
    private final static String PASS = "Brearly16";
    public final static String REPORT_DIR = "/var/Concert/reports/";
    public final static String SITE_REPORT_DIR = REPORT_DIR + "sites/";
    public final static String CUSTOM_REPORT_DIR = REPORT_DIR + "custom/";
    public final static String CUSTOMER_REPORT_DIR = REPORT_DIR + "customers/";
    public final static String MNO_REPORT_DIR = REPORT_DIR + "operators/";
    public final static String REPORT_TEMPLATE_DIR = REPORT_DIR + "template/";
    // private final static Paint[] opcoColours = new Paint[] { new Color(255,36,36), new Color(42,137,192), new Color(182,95,194), new Color(43,172,177) };
    public final static Color[] opcoColours = { new Color(255,36,36), new Color(42,137,192), new Color(182,95,194), new Color(43,172,177) };
    private static XYLineAndShapeRenderer renderer = null;
    private static DefaultPieDataset data = null;
    private FileWriter logWriter = null; 
    private static int additionalMonthlyOffset = 0;
    private static float scalingFactor = 1;
    private static boolean strictCIS = true; // can be set via a command line parameter (was false prior to AD fix)
        
    String agentName = "PDFReportAgent";
    // String sql = "";
    String startTime = "";
    String endTime = "";
    String fileNameDate = null; // null for custom reports that aren't saved to file
    String reportMonth = "";
    String reportYear = "";
    String reportRange = "";
    String[] sites = new String[] {"Ladbroke Grove"};
    String responseText = "[{\"time\":\"2019-02-22\",\"metric_name\":\"Total Num Calls \",\"VF\":\"0\",\"O2\":\"30\",\"THREE\":\"372\",\"EE\":\"90\"}]";
    
//    private static JFreeChart barChart;
//    private static JFreeChart pieChart;
    private Connection conn = null;
    private Statement stmt = null;
    private HashMap<String,ArrayList> customerSites = new HashMap(100);
    private HashMap<String,ArrayList> customerSiteIds = new HashMap(100);
    private HashMap<String,String> siteCustomers = new HashMap(200);
    private HashMap<String, float[]> customerCisValues = new HashMap(200);
    private HashMap<String, int[]> customerCellCounts = new HashMap(200);
    private HashMap<String, float[]> siteCisValues = new HashMap(500);
    
    // These collections store values that will be used when generating the customer reports
    public HashMap<String, Long> siteVoiceCallNums = new HashMap(500);
    public HashMap<String, Integer> siteDownloadVols = new HashMap(500);
    public HashMap<String, Integer> siteUploadVols = new HashMap(500);

    public PDFReportCreator() {
        try {
            if (TEST_MODE) { // create the JDBC connection
                //STEP 2: Register JDBC driver
                Class.forName(JDBC_DRIVER);

                //STEP 3: Open a connection
                System.out.println("Connecting to database...");
                conn = DriverManager.getConnection(DB_URL,USER,PASS);

                //STEP 4: Execute a query
                System.out.println("Creating statement...");
                stmt = conn.createStatement();            
    
                renderer = new XYLineAndShapeRenderer();
                renderer.setSeriesLinesVisible(2, false);
                renderer.setSeriesShapesVisible(1, false);                
//                renderer.setSeriesPaint(0, new Color(255,36,36));
//                renderer.setSeriesPaint(1, new Color(42,137,192));
//                renderer.setSeriesPaint(2, new Color(182,95,194));
//                renderer.setSeriesPaint(3, new Color(43,172,177));
                renderer.setSeriesPaint(0, opcoColours[0]);
                renderer.setSeriesPaint(1, opcoColours[1]);
                renderer.setSeriesPaint(2, opcoColours[2]);
                renderer.setSeriesPaint(3, opcoColours[3]);
            }
            logWriter = openLog();
        } catch (Exception e) {
            e.printStackTrace();
        }     
    }

    /**
     * 
     * @param date in the format "YYYY-MM-DD"
     */
    public void setDates(boolean custom, String theStartTime, String theEndTime) {
        // " 201902 February.pdf";
        // "1/2/19 - 28/2/19", "February", "2019"        
        // "2019-03-01 00:00";
        // "2019-03-31 23:59";
        DateTimeFormatter dtfFileName = null;
        YearMonth thisMonth = YearMonth.now();
        if (custom) {
            startTime = theStartTime;
            endTime = theEndTime;
            DateTimeFormatter dtfCustom = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);
            DateTimeFormatter dtfDayMonthYear = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH);
            dtfFileName = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH);
            fileNameDate = dtfFileName.format(LocalDate.parse(theStartTime, dtfCustom)) + "-" + dtfFileName.format(LocalDate.parse(theEndTime, dtfCustom));
            reportRange = dtfDayMonthYear.format(LocalDate.parse(theStartTime, dtfCustom)) + " - " + dtfDayMonthYear.format(LocalDate.parse(theEndTime, dtfCustom));
            reportMonth = reportRange;
            reportYear = "";
        } else {
            // it's a scheduled export, output for the previous month. If this month is January, also output for an entire year
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd"); 
            String now = sdfDate.format(new Date());            
            
            YearMonth lastMonth = thisMonth.minusMonths(1 + additionalMonthlyOffset);
            int lastDay = lastMonth.lengthOfMonth();
            DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            dtfFileName = DateTimeFormatter.ofPattern("yyyyMM MMMM", Locale.ENGLISH);
            DateTimeFormatter dtfMonth = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH);
            DateTimeFormatter dtfYear = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH);
            DateTimeFormatter dtfYearMonth = DateTimeFormatter.ofPattern("yyyy-MM", Locale.ENGLISH);
            DateTimeFormatter dtfMonthYear = DateTimeFormatter.ofPattern("MM/yy", Locale.ENGLISH);
            LocalDate ld = LocalDate.parse(now, dtf1);
            fileNameDate = lastMonth.format(dtfFileName);
            reportMonth = lastMonth.format(dtfMonth);
            reportYear = lastMonth.format(dtfYear);
            startTime = lastMonth.format(dtfYearMonth) + "-01 00:00";
            endTime = lastMonth.format(dtfYearMonth) + "-" + lastDay + " 23:59";
            reportRange = "1/" + lastMonth.format(dtfMonthYear) + " - " + lastDay + "/" + lastMonth.format(dtfMonthYear);
        }            
    }
    /**
     * @begin a date 2019-02-01 or longer
    */
    private float[] getCisValues(String begin, String end, String site) {
        // initing to -1 causes calculation errors when averaging. Lets try zeros
        //float[] cisValues = new float[]{-1f, -1f, -1f, -1f, -1f, 100f}; // V,O,T,E, avg, min 
        float[] cisValues = new float[]{0f, 0f, 0f, 0f, 0f, 100f}; // V,O,T,E, avg, min 
        int numMnos = 0; // used for calculating the average where not all operators are present at a site
        
        String cisSql = "";
        if (strictCIS) {
            // Exactly matches the SLR screen in the GUI, not the extra filters for zero values - requested by Andy to be changed for https://dataduct.atlassian.net/browse/OC-134
            // so that potentially out of service cells are not missed        
            log("Using strict CIS calculation");
            // version with fractional bug for unused sectors
            //cisSql = "SELECT sector_no, AVG(avg_cis_kpi_all) AS CIS FROM `ranmate-femto`.v_dashboard_hourly_site_operator_performance_history " +
            //            "WHERE group_id = 'OpenCell' AND measurement_time BETWEEN '" + begin + "' AND '" + end + "' AND site_id LIKE '" + site + "-%' " +
            //            "group by sector_no";
            // version with fractional bug for unused sectors fixed
            cisSql = "SELECT sector_no, AVG(avg_cis_kpi_all) AS CIS FROM `ranmate-femto`.v_dashboard_hourly_site_operator_performance_history " +
                        "WHERE group_id = 'OpenCell' AND measurement_time BETWEEN '" + begin + "' AND '" + end + "' AND site_id LIKE '" + site + "-%' " +
                        "and (avg_cis_kpi_all <> 0 or exists (select * from `ranmate-femto`.sites_sectors_live where v_dashboard_hourly_site_operator_performance_history.site_id = sites_sectors_live.site_id and v_dashboard_hourly_site_operator_performance_history.sector_no = sites_sectors_live.sector_no)) " +
                        "group by v_dashboard_hourly_site_operator_performance_history.sector_no";
            //System.out.println("CIS sql is " + cisSql);            
        } else {
            log("Using tolerant CIS calculation");
            cisSql = "SELECT sector_no, AVG(NULLIF(avg_cis_kpi_all ,0)) AS CIS FROM `ranmate-femto`.v_dashboard_hourly_site_operator_performance_history " +
                        "WHERE group_id = 'OpenCell' AND measurement_time BETWEEN '" + begin + "' AND '" + end + "' AND site_id LIKE '" + site + "-%' " +
                        "AND avg_cis_kpi_all != 0 AND min_cis_kpi_all != 0 AND max_cis_kpi_all != 0 group by sector_no";
        }
        //System.out.println("CIS SQL is " + cisSql);
        try {
            ResultSet result = executeQuerySQL(cisSql);
            boolean noCisValues = true;
            while (result.next()) {
                noCisValues = false;
                int mno = result.getInt("sector_no");      
                cisValues[mno] = result.getFloat("CIS");
                if (cisValues[mno] > 0 ) {
                   numMnos++;
                    if ((cisValues[mno] < cisValues[5]))
                        cisValues[5] = cisValues[mno];
                }
                mno++;
            }
            // fixes the bug where 'Perfect' was reported for sites with no CIS values because cisValues[5] is initialised to 100.
            if (noCisValues)
                cisValues[5] = 0f;
            cisValues[4] = (cisValues[0] + cisValues[1] + cisValues[2] + cisValues[3]) / numMnos; // average CIS KPI for this site            
            //if (site.startsWith("Candy")) {
            //    System.out.println("    " + site + " original cis values = " + Arrays.toString(cisValues) + " and numMnos=" + numMnos); 
            //}
            
        } catch (Exception e) {
            System.out.println("Unable to get the CIS values");
        }
        //System.out.println("CIS values for " + site + " are " + Arrays.toString(cisValues));
        return cisValues;        
    }       
            
    /**
     * 
     * @param site - the longish name of the site
     * @param siteId - the 3 char 3 digit id of the site, eg, WEW012
     * @param customer
     * @param baseDir
     * @param subDir
     * @param dataSql
     * @param callSql
     * @return 
     */
    private boolean createReport(boolean custom, String site, String siteId, String customer, String baseDir, String subDir, String[] dataSql, String[] callSql) {
        //System.out.println("\nProcessing " + subDir);
        createDirIfNotExist(baseDir + subDir);
        long voiceCallsNum = 0;             // number of calls
        int downloadNumGb = 0;              // number of Gigabytes. Anything less than 1 will be displayed as < 1                         
        int uploadNumGb = 0;
        boolean allOk = false;
             
        // Data Volumes SQL
        //String[] sql = getSiteDataSQL(site);
        Date before = new Date();
        int[] cellCounts = null;
        try {
            // Get the cell counts
            if (site != null) {
                cellCounts = getCellCounts(site);
                updateCustomerCellCounts(customer, cellCounts);
            } else {
                cellCounts = customerCellCounts.get(customer);
            }
            
            // added for debuggin - not needed. problem was missing config in RANmate
            //for (int w = 0; w < dataSql.length; w++) {
            //        System.out.println("    " + dataSql[w]);
            //        log("    " + dataSql[w]);                                    
            //}
            executeUpdateSQL(dataSql);
            ResultSet dataData = executeQuerySQL(dataSql[dataSql.length - 1]);
            if (dataData.next()) { // there should be 2 rows returned, the first is Inbound, the second Outbound
                int vfVal = dataData.getInt("VF");
                int o2Val = dataData.getInt("O2");
                int threeVal = dataData.getInt("THREE");
                int eeVal = dataData.getInt("EE");
                if (reportMonth.equals("April") && reportYear.equals("2019") && !baseDir.equals(CUSTOM_REPORT_DIR)) {
                    //System.out.println("Corrective scaling factor set to 2 for April 2019 report");
                    scalingFactor = 2;
                }
                vfVal = (int)(vfVal * scalingFactor);
                o2Val = (int)(o2Val * scalingFactor);
                threeVal = (int)(threeVal * scalingFactor);
                eeVal = (int)(eeVal * scalingFactor);
                //System.out.println("Scaling Factor of " + scalingFactor + " applied");
                downloadNumGb = (vfVal + o2Val + threeVal + eeVal)/1024;
                //System.out.println("Download VF=" + vfVal + ", O2=" + o2Val + ", 3=" + threeVal + ", EE=" + eeVal + ", Total GB=" + uploadNumGb);

                // Generate the Data Inbound Volumes Pie Chart
                JFreeChart dataInPieChart = createPieChart(vfVal, o2Val, threeVal, eeVal);
                ChartUtilities.saveChartAsPNG(new File(baseDir + subDir + "/DataInboundPieChart.png"), dataInPieChart, 450, 450);
                if (site != null) {
                    createDirIfNotExist(CUSTOMER_REPORT_DIR + customer);
                    ChartUtilities.saveChartAsPNG(new File(CUSTOMER_REPORT_DIR + customer + "/DataInboundPieChart_" + site + ".png"), dataInPieChart, 450, 450);
                    siteDownloadVols.put(site, downloadNumGb);
                    //cellCounts = getCellCounts(site);
                    //updateCustomerCellCounts(customer, cellCounts);
                } else {
                    //cellCounts = customerCellCounts.get(customer);
                }

                // Generate the Data Inbound Volumes Bar Chart
                JFreeChart dataInBarChart = createBarChart(vfVal, o2Val, threeVal, eeVal, "MBytes", cellCounts);
                ChartUtilities.saveChartAsPNG(new File(baseDir + subDir + "/DataInboundBarChart.png"), dataInBarChart, 450, 450);
                if (site != null)
                    ChartUtilities.saveChartAsPNG(new File(CUSTOMER_REPORT_DIR + customer + "/DataInboundBarChart_" + site + ".png"), dataInBarChart, 450, 450);

                if (dataData.next()) { // there should be 2 rows returned, the first is Inbound, the second Outbound
                    vfVal = dataData.getInt("VF");
                    o2Val = dataData.getInt("O2");
                    threeVal = dataData.getInt("THREE");
                    eeVal = dataData.getInt("EE");
                    uploadNumGb = (vfVal + o2Val + threeVal + eeVal)/1024;
                    if (reportMonth.equals("April") && reportYear.equals("2019") && !baseDir.equals(CUSTOM_REPORT_DIR)) {
                        System.out.println("Corrective scaling factor set to 2 for April 2019 report");
                        scalingFactor = 2;
                    }
                    vfVal = (int)(vfVal * scalingFactor);
                    o2Val = (int)(o2Val * scalingFactor);
                    threeVal = (int)(threeVal * scalingFactor);
                    eeVal = (int)(eeVal * scalingFactor);
                    //System.out.println("Upload VF=" + vfVal + ", O2=" + o2Val + ", 3=" + threeVal + ", EE=" + eeVal + ", Total GB=" + uploadNumGb);

                    // Generate the Data Outbound Volumes Pie Chart
                    JFreeChart dataOutPieChart = createPieChart(vfVal, o2Val, threeVal, eeVal);
                    ChartUtilities.saveChartAsPNG(new File(baseDir + subDir + "/DataOutboundPieChart.png"), dataOutPieChart, 450, 450);
                    if (site != null) {
                        ChartUtilities.saveChartAsPNG(new File(CUSTOMER_REPORT_DIR + customer + "/DataOutboundPieChart_" + site + ".png"), dataOutPieChart, 450, 450);
                        siteUploadVols.put(site, uploadNumGb);
                    }

                    // Generate the Data Outbound Volumes Bar Chart
                    JFreeChart dataOutBarChart = createBarChart(vfVal, o2Val, threeVal, eeVal, "MBytes", cellCounts);
                    ChartUtilities.saveChartAsPNG(new File(baseDir + subDir + "/DataOutboundBarChart.png"), dataOutBarChart, 450, 450);
                    if (site != null)
                        ChartUtilities.saveChartAsPNG(new File(CUSTOMER_REPORT_DIR + customer + "/DataOutboundBarChart_" + site + ".png"), dataOutBarChart, 450, 450);

                    // Only include sites with traffic data in each customer's report
                    if (site != null) {
                        if (!customerSites.containsKey(customer)) {
                            customerSites.put(customer, new ArrayList(10));
                            customerSiteIds.put(customer, new ArrayList(10));
                        } 
                        customerSites.get(customer).add(site);
                        siteCustomers.put(site, customer);
                        customerSiteIds.get(customer).add(siteId);
                        createDirIfNotExist(CUSTOMER_REPORT_DIR + customer);
                    }
                    
                    allOk = true;
                } else {
                    System.out.println("Data Upload values not available for " + subDir + ", will not create report.");
                    log("Data Upload values not available for " + subDir + ", will not create report.");
                }                 
            } else {
                System.out.println("Data Download values not available for " + subDir + ", will not create report.");
                log("Data Download values not available for " + subDir + ", will not create report.");
            }                 
        } catch (Exception e) {
            System.out.println("Error creating Data Charts for " + subDir + ", " + e.getMessage());                
            log("Error creating Data Charts for " + subDir + ", " + e.getMessage());
            e.printStackTrace();
        }

        // Call Numbers SQL
        if (allOk) {
            try {
                executeUpdateSQL(callSql);
                ResultSet callData = executeQuerySQL(callSql[callSql.length - 1]);
                if (callData.next()) { // there should only be 1 row returned
                    // correction added here to cater for - https://dataduct.atlassian.net/browse/OC-85
                    // divide by 2 since the samples
                    int vfVal = (callData.getInt("VF") / 2);
                    int o2Val = (callData.getInt("O2") / 2);
                    int threeVal = (callData.getInt("THREE") / 2);
                    int eeVal = (callData.getInt("EE") / 2);
                    if (reportMonth.equals("April") && reportYear.equals("2019") && !baseDir.equals(CUSTOM_REPORT_DIR)) {
                        System.out.println("Corrective scaling factor set to 2 for April 2019 report");
                        scalingFactor = 2;
                    }
                    vfVal = (int)(vfVal * scalingFactor);
                    o2Val = (int)(o2Val * scalingFactor);
                    threeVal = (int)(threeVal * scalingFactor);
                    eeVal = (int)(eeVal * scalingFactor);
                    voiceCallsNum = vfVal + o2Val + threeVal + eeVal;
                    System.out.println("Voice Calls# Customer=" + customer + ", Site=" + site + ", VF=" + vfVal + ", O2=" + o2Val + ", 3=" + threeVal + ", EE=" + eeVal + ", Total=" + voiceCallsNum);

                    // Generate the Call Volumes Pie Chart
                    JFreeChart callsPieChart = createPieChart(vfVal, o2Val, threeVal, eeVal);
                    ChartUtilities.saveChartAsPNG(new File(baseDir + subDir + "/TotalNumCallsPieChart.png"), callsPieChart, 450, 450);
                    ChartUtilities.saveChartAsPNG(new File(CUSTOMER_REPORT_DIR + customer + "/TotalNumCallsPieChart_" + site + ".png"), callsPieChart, 450, 450);

                    // Generate the Call Volumes Bar Chart
                    JFreeChart callsBarChart = createBarChart(vfVal, o2Val, threeVal, eeVal, "# Calls", cellCounts);
                    ChartUtilities.saveChartAsPNG(new File(baseDir + subDir + "/TotalNumCallsBarChart.png"), callsBarChart, 450, 450);                    
                    ChartUtilities.saveChartAsPNG(new File(CUSTOMER_REPORT_DIR + customer +"/TotalNumCallsBarChart_" + site + ".png"), callsBarChart, 450, 450);                    

                    // PDF Report Generation
                    PdfReportWriter pdfWriter = null;
                    String reportName = "";
                    if (site != null) {
                        //float[] mnoCis = { 99.23f, 98.45f, -1.0f, 96.0f, 99.4f}; 
                        float[] mnoCis = getCisValues(startTime, endTime, site);
                        siteVoiceCallNums.put(site, voiceCallsNum);
                        
                        // additional logging requested by Andy in OC-134 on 5/11/19
                        String cisStr = "";
                        for (float aCis: mnoCis) { cisStr += ", " + aCis; }
                        log("CIS, " + reportMonth + ", " + site + cisStr);

                        //reportName = "StrattoOpenCell Service Report - " + customer + " " + site + " 201902 February.pdf";
                        if ((customer == null) || customer.equals("")) {
                            reportName = "StrattoOpenCell Service Report - " + site + " " + fileNameDate + ".pdf";
                        } else {
                            reportName = "StrattoOpenCell Service Report - " + customer + " " + site + " " + fileNameDate + ".pdf";                            
                        }
                        //pdfWriter = new PdfReportWriter(this, false, 1, 1, reportRange, reportMonth, reportYear, subDir,
                        //             voiceCallsNum, downloadNumGb, uploadNumGb, "", mnoCis, reportName, null, null, siteId, null); // custom = false
                        pdfWriter = new PdfReportWriter(this, custom, 1, 1, reportRange, reportMonth, reportYear, subDir,
                                     voiceCallsNum, downloadNumGb, uploadNumGb, "", mnoCis, reportName, null, null, siteId, null);
                        updateCustomerCisValues(customer, site, mnoCis);
                    } else {
                        float[] customerCis = calculateAverageCis(customerCisValues.get(customer));
                        //reportName = "StrattoOpenCell Service Report - " + customer + " " + fileNameDate + "201902 February.pdf";
                        reportName = "StrattoOpenCell Service Report - " + customer + " " + fileNameDate + ".pdf";
                        // pdfWriter = new PdfReportWriter(false, 2, 1, "1/2/19 - 28/2/19", "February", "2019", subDir,
                        //             voiceCallsNum, downloadNumGb, uploadNumGb, "", customerCis, reportName, customerSites.get(customer), siteCisValues);
                        pdfWriter = new PdfReportWriter(this, false, 2, 1, reportRange, reportMonth, reportYear, subDir,
                                     voiceCallsNum, downloadNumGb, uploadNumGb, "", customerCis, reportName, customerSites.get(customer), siteCisValues, "", customerSiteIds.get(customer));
                    }
                                        
                    if (pdfWriter.createReport()) {             // if the PDF file is created ok   
                        deleteCharts(baseDir + subDir);         // Delete the component charts
                    }
                    Date after = new Date();
                    long duration = after.getTime() - before.getTime();
                    System.out.println("Report " + reportName + " completed in " + duration/1000 + "s");
                    log("Report " + reportName + " completed in " + duration/1000 + "s");                    
                } else {
                    allOk = false;
                    System.out.println("Call Volumes not available for " + subDir + ", will not create report.");
                    log("Call Volumes not available for " + subDir + ", will not create report.");
                }
            } catch (Exception e) {
                allOk = false;
                System.out.println("Unable to create Call Charts for " + subDir + ", will not create report. " + e.getMessage());                
                log("Unable to create Call Charts for " + subDir + ", will not create report. " + e.getMessage());                
                e.printStackTrace();
            }
        }
        return allOk;
    }

    private void updateCustomerCellCounts(String customer, int[] siteCounts) {
        int[] custCellCounts = customerCellCounts.get(customer);
        if (custCellCounts == null) {
            custCellCounts = new int[4]; 
        }
        for (int i = 0; i < 4; i++) {
            custCellCounts[i] += siteCounts[i];
        }
        customerCellCounts.put(customer, custCellCounts);
    }

    private FileWriter openLog() {
        FileWriter fw = null;
        try {
            //fw = new FileWriter("/var/opt/Concert/logs/PdfReportCreator.log", true);
            fw = new FileWriter("/opt/RANmateReports/logs/PdfReportCreator.log", true);
        } catch (Exception e) { /* gotta swallow it */ }
        return fw;
    }

    private void closeLog() {
        try {
            if (logWriter != null)
                logWriter.close();
        } catch (Exception e) { /* gotta swallow it */ }
    }
    
    
    private void log(String msg) {
        try {
            if (logWriter != null) {
                String date = new SimpleDateFormat("yyyy-MM-dd h:mm").format(Calendar.getInstance().getTime());
                logWriter.write(date + " " + msg + "\n");
            }
        } catch (Exception e) { /* gotta swallow it */ }
    }
    
    private void updateCustomerCisValues(String customer, String site, float[] mnoCis) {
        // [0..3] per mno running average
        // [4] customer running average
        // [5] customer min
        // [6..9] the number of sites to divide by for each MNO
        // [10] the number of sites to divide the average for this customer by
        float[] averagedCisValues = customerCisValues.get(customer);
        if (averagedCisValues == null) {
            averagedCisValues = new float[11]; // 11 elements in the array. The extra 5 elements at the end stores the number of sites to divide by for each MNO plus AVG (may not be the same)
        }
        //if (customer.equals("Candy & Candy")) {
        //    System.out.println("    " + customer + " cis values = " + Arrays.toString(averagedCisValues)); 
        //    System.out.println("    " + customer + " " + site + " cis values = " + Arrays.toString(mnoCis)); 
        //}
            
        for (int i = 0; i < 5; i++) {
            if (mnoCis[i] > 0) {
                averagedCisValues[i] += mnoCis[i];
                averagedCisValues[i + 6]++;
            }
        }
        // [5] is the minimum so shouldn't be averaged
        if ((averagedCisValues[5] == 0) || ((mnoCis[5] > 0) && (mnoCis[5] < averagedCisValues[5])))
            averagedCisValues[5] = mnoCis[5];
        customerCisValues.put(customer, averagedCisValues);
        siteCisValues.put(site, mnoCis);
        //if (customer.equals("Candy & Candy")) {
        //    System.out.println("    " + customer + " updated cis values = " + Arrays.toString(averagedCisValues)); 
        //}
    }

    /**
     * Divides the accumulated totals for the first 5 elements (4 MNOs and average) by the number of occurrences for each (recorded in the last 5 elements of the array)
     */ 
    private float[] calculateAverageCis(float[] cisTotals) {
        float[] averagedCisValues = new float[6];
        for (int i = 0; i < 5; i++) {
            averagedCisValues[i] = cisTotals[i]/cisTotals[i + 6];
        }
        averagedCisValues[5] = cisTotals[5]; // the min value doesn't get averaged
        return averagedCisValues;
    }
    
    /**
     * @param args the command line arguments
     * Usage: PDFReportGenerator type offset scalingFactor
     * eg. PDFReportGenerator full 0
     * type =  test/full/custom
     * offset = int. the number of additional months to offset the generated period by for fixed monthly periods, normally the previous month is generated (offset = 0)
     * scalingFactor = float if some days recordings in the month are missed for all sites, the volume charts can be adjusted by the scaling factor provided
     * 
     * One example from Project Properties
     * custom "Moor Place" "2019-04-22 07:33" "2019-05-03 13:33" 1
     */
    public static void main(String[] args) {
        boolean BRUNTWOOD_AND_CANDY_ONLY = false; // used for testing, small number of sites
        boolean custom = false; // set here for now
        
        PDFReportCreator reportCreator = new PDFReportCreator();

        if (args.length == 4) { // fixed
            if (args[0].equals("test") ) {
                //System.err.println("Fixed period Test Report requested");
                BRUNTWOOD_AND_CANDY_ONLY = true;                    
            } else if (args[0].equals("custom")) {
                System.err.println("Too few arguments (4) for Custom Report request, should be 5");
                System.exit(1);
            } else {
                //System.err.println("Fixed period Full Report requested");                
            }
            additionalMonthlyOffset = Integer.parseInt(args[1]);
            //System.err.println("additionalMonthlyOffset is " + additionalMonthlyOffset);
            scalingFactor = Float.parseFloat(args[2]);
            if ("strict".equals(args[3])) {
                strictCIS = true;
            } // else remains false
            reportCreator.setDates(custom, null, null);
            //System.err.println("scalingFactor is " + scalingFactor);
        } else if (args.length == 5) { // custom
            if (args[0].equals("custom")) {
                System.err.println("Custom Report requested");
                custom = true;                    
            } else {
                System.err.println("Too many arguments (5) for Fixed Period Report request, should be 4");
                System.exit(1);
            }
            reportCreator.setDates(custom, args[2], args[3]);
            additionalMonthlyOffset = 0; // (to be shure to be shure)
            //System.err.println("additionalMonthlyOffset is " + additionalMonthlyOffset);
            scalingFactor = Float.parseFloat(args[4]);
        } else {
            System.err.println("Incorrect number of arguments (" + args.length + "), should be 4 or 5");
            System.err.println("Usage: PDFReportGenerator full/test offset scalingFactor tolerant/strict");
            System.err.println("Usage: PDFReportGenerator custom site_name start_date_time end_date_time scalingFactor");
            System.exit(1);
        }
            
        // reportCreator.setDates(true, "2019-03-16 00:00", "2019-03-24 23:59");
        
        ArrayList<String> customers = null;
        ArrayList<String>[] sites = null;
        if (custom) {
            String customSiteName = args[1];
            reportCreator.createReport(custom, customSiteName, "", "", CUSTOM_REPORT_DIR, customSiteName, reportCreator.getSiteDataSQL(customSiteName), reportCreator.getSiteCallSQL(customSiteName));
        } else {
            customers = reportCreator.getCustomerList(BRUNTWOOD_AND_CANDY_ONLY);
            sites = reportCreator.getSiteList(BRUNTWOOD_AND_CANDY_ONLY);
        
            //System.out.println("Customers are " + customers.toString());
            //System.out.println("Sites are " + sites.toString());

            for (int i = 0; i < sites[0].size(); i++) {
                String siteName = sites[0].get(i);
                String customer = sites[1].get(i); 
                String siteId = sites[2].get(i); 
                //String site = "Here East"; // testing only
                //String customer = "Innovation City"; // testing only
                if (reportCreator.createReport(custom, siteName, siteId, customer, SITE_REPORT_DIR, siteName, reportCreator.getSiteDataSQL(siteName), reportCreator.getSiteCallSQL(siteName))) {
                    try {
                        String[] sql = new String[2]; // don't ask why 2...
                        sql[0] = "INSERT IGNORE INTO metrics.generated_reports VALUES ('" + customer + "','" + siteName + "'," + PdfReportWriter.SITE + "," + PdfReportWriter.MONTH + ", '" + reportCreator.getDirName(reportCreator.endTime) + "');";
                        reportCreator.executeUpdateSQL(sql);
                    } catch (Exception e) {
                        System.out.println("Error recording report generated for site  " + siteName + ", " + e.getMessage());
                        reportCreator.log("Error recording report generated for site  " + siteName + ", " + e.getMessage());
                    }
                }

                // only enable for debugging, otherwise not needed
                // try { Thread.sleep(10000);} catch (Exception e) {} // sleep for 10 seconds between creating reports
            }

            for (String customer: customers) {
                if (reportCreator.createReport(custom, null, "", customer, CUSTOMER_REPORT_DIR, customer, reportCreator.getCustomerDataSQL(customer), reportCreator.getCustomerCallSQL(customer))) {
                    try {
                        String[] sql = new String[2];
                        sql[0] = "INSERT IGNORE INTO metrics.generated_reports VALUES ('" + customer + "','" + customer + "'," + PdfReportWriter.CUSTOMER + "," + PdfReportWriter.MONTH + ", '" + reportCreator.getDirName(reportCreator.endTime) + "');";
                        reportCreator.executeUpdateSQL(sql);
                    } catch (Exception e) {
                        System.out.println("Error recording report generated for customer " + customer + ", " + e.getMessage());
                        reportCreator.log("Error recording report generated for customer " + customer + ", " + e.getMessage());
                    }
                }
            }        
        }
        reportCreator.closeConn();
        reportCreator.closeLog();
    }

    private String getDirName(String timestamp) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyyMM (MMM)", Locale.ENGLISH);
        LocalDate ld = LocalDate.parse(timestamp, dtf);
        return dtf2.format(ld);
    }
    
    private void deleteCharts(String dirName) {
        try {
            new File(dirName + "/TotalNumCallsPieChart.png").delete();
            new File(dirName + "/TotalNumCallsBarChart.png").delete();
            new File(dirName + "/DataInboundPieChart.png").delete();
            new File(dirName + "/DataInboundBarChart.png").delete();
            new File(dirName + "/DataOutboundPieChart.png").delete();
            new File(dirName + "/DataOutboundBarChart.png").delete();
            
            File dir = new File(dirName);
            // delete files created by meeting Andy Requirement
            for (File f : dir.listFiles()) {
                if (f.getName().startsWith("TotalNumCallsPieChart_")) { f.delete(); }
                if (f.getName().startsWith("TotalNumCallsBarChart_")) { f.delete(); }
                if (f.getName().startsWith("DataInboundPieChart_")) { f.delete(); }
                if (f.getName().startsWith("DataInboundBarChart_")) { f.delete(); }
                if (f.getName().startsWith("DataOutboundPieChart_")) { f.delete(); }
                if (f.getName().startsWith("DataOutboundBarChart_")) { f.delete(); }
            }            
        } catch (Exception e) {
            System.out.println("Error deleting graph files in directory " + dirName);
        }
    }
        
    private void createDirIfNotExist(String path) {
        File directory = new File(path);
        if(!directory.exists()) {
            directory.mkdir();
        }        
    }
    
    private void closeConn() {
        //finally block used to close resources
        try {
            if(stmt!=null) 
                  stmt.close();
              } catch(SQLException se2) {
            }// nothing we can do
            try{
               if(conn!=null)
                  conn.close();
            }catch(SQLException se) {
               se.printStackTrace();
        }//end finally try
    }

    /**
     * @param siteOrCustName either a site name or a customer name 
     * @return an array with 4 VOTE elements containing the number of cells present at that site for that operator
     */
    private int[] getCellCounts(String siteName) {
        int[] cellNum = {0,0,0,0};
        ResultSet countData = null;         // normal switches
        ResultSet virtualCountData = null;         // normal switches
        try {
            // this logic tries to calculate only those cells that were active during the period in question
            //countData = executeQuerySQL("select cell_no % 4, count(*) from `ranmate-femto`.cells where exist and NOT exclude and group_id = 'Opencell' and site_id LIKE '" + siteName + "%' and effective_to > '" + startTime + "' and effective_from < '" + endTime + "' group by (cell_no % 4)");
            // this query matches Andy's expectations from Concert
            countData = executeQuerySQL("select (id - 1) % 4, count(*) from OpenCellCM.Femto where `exists` and live and site_name LIKE '" + siteName + "%' and switch_id not like '%_VF' and switch_id not like '%_O2' and switch_id not like '%_3' and switch_id not like '%_EE' group by (id - 1) % 4");
            while (countData.next()) { // there should only be 1 row returned
                cellNum[countData.getInt(1)] = countData.getInt(2);
            }
            virtualCountData = executeQuerySQL("select 0, count(*) from OpenCellCM.Femto where `exists` and live and site_name LIKE '" + siteName + "%' and switch_id like '%_VF' " +
                                  "union select 1, count(*) from OpenCellCM.Femto where `exists` and live and site_name LIKE '" + siteName + "%' and switch_id like '%_O2' " +
                                  "union select 2, count(*) from OpenCellCM.Femto where `exists` and live and site_name LIKE '" + siteName + "%' and switch_id like '%_3' " +
                                  "union select 3, count(*) from OpenCellCM.Femto where `exists` and live and site_name LIKE '" + siteName + "%' and switch_id like '%_EE'");
            while (virtualCountData.next()) { // there should only be 1 row returned
                cellNum[virtualCountData.getInt(1)] += virtualCountData.getInt(2); // add any virtual switch counts to the normal switch counts
            }
        } catch (Exception e) {
            System.out.println("Error getting the cell counts for " + siteName + ", " + e.getMessage());
        } finally {
            try {countData.close();virtualCountData.close();} catch (Exception e) {}
        }
        return cellNum;
    }

    /**
     * @param site
     * @return the SQL that will output the Total Calls graph data
     */    
    private String[] getSiteCallSQL(String siteName) {
        String[] sql = new String[11]; // update and query type commands separate
        sql[0] = "SET @VF_LIVE=(select count(*) from `ranmate-femto`.cells where exist and NOT exclude and effective_to > '" + startTime + "' and effective_from < '" + endTime + "' and site_id LIKE '" + siteName + "%' and cell_no % 4 = 0);";
        sql[1] = "SET @O2_LIVE=(select count(*) from `ranmate-femto`.cells where exist and NOT exclude and effective_to > '" + startTime + "' and effective_from < '" + endTime + "' and site_id LIKE '" + siteName + "%' and cell_no % 4 = 1);";
        sql[2] = "SET @THREE_LIVE=(select count(*) from `ranmate-femto`.cells where exist and NOT exclude and effective_to > '" + startTime + "' and effective_from < '" + endTime + "' and site_id LIKE '" + siteName + "%' and cell_no % 4 = 2);";
        sql[3] = "SET @EE_LIVE=(select count(*) from `ranmate-femto`.cells where exist and NOT exclude and effective_to > '" + startTime + "' and effective_from < '" + endTime + "' and site_id LIKE '" + siteName + "%' and cell_no % 4 = 3);";
        sql[4] = "DELETE FROM metrics.jflow_viewer_output_aggregated;";
        //sql[1] = "INSERT INTO metrics.jflow_viewer_output_aggregated (SELECT DATE(measurement_time), site_name, operator_id, SUM(cs_inbound) AS 'Calls Inbound', SUM(ps_inbound) AS 'Data Inbound', SUM(signalling_inbound) AS 'Remainder Inbound',  SUM(total_inbound) AS 'Total Inbound',SUM(cs_outbound) AS 'Calls Outbound', SUM(ps_outbound) AS 'Data Outbound', SUM(signalling_outbound) AS 'Remainder Outbound',  SUM(total_outbound) AS 'Total Outbound'FROM metrics.jflow WHERE measurement_time BETWEEN '" + startTime + "' AND '" + endTime + "'  AND jflow.site_name REGEXP '^" + siteName + "' GROUP BY DATE(measurement_time), site_name, operator_id order by measurement_time, site_name, operator_id);";
        sql[5] = "INSERT INTO metrics.jflow_viewer_output_aggregated (SELECT DATE(measurement_time), site_name, operator_id, SUM(cs_inbound) AS 'Calls Inbound', SUM(ps_inbound) AS 'Data Inbound', SUM(signalling_inbound) AS 'Remainder Inbound',  SUM(total_inbound) AS 'Total Inbound',SUM(cs_outbound) AS 'Calls Outbound', SUM(ps_outbound) AS 'Data Outbound', SUM(signalling_outbound) AS 'Remainder Outbound',  SUM(total_outbound) AS 'Total Outbound'FROM metrics.jflow WHERE measurement_time BETWEEN '" + startTime + "' AND '" + endTime + "'  AND jflow.site_name LIKE '" + siteName + "%' GROUP BY DATE(measurement_time), site_name, operator_id order by measurement_time, site_name, operator_id);";
        sql[6] = "DELETE FROM metrics.jflow_viewer_output_pivoted;";
        sql[7] = "INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Num Calls Inbound' AS Metric, site_name, case when (operator_id = 1 AND @VF_LIVE) then cs_inbound end, case when (operator_id = 2 AND @O2_LIVE) then cs_inbound end,case when (operator_id = 3 AND @THREE_LIVE) then cs_inbound end, case when (operator_id = 4 AND @EE_LIVE) then cs_inbound end FROM metrics.jflow_viewer_output_aggregated);";
        sql[8] = "INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Num Calls Outbound' AS Metric, site_name, case when (operator_id = 1 AND @VF_LIVE) then cs_outbound end, case when (operator_id = 2 AND @O2_LIVE) then cs_outbound end,case when (operator_id = 3 AND @THREE_LIVE) then cs_outbound end, case when (operator_id = 4 AND @EE_LIVE) then cs_outbound end FROM metrics.jflow_viewer_output_aggregated);";
        sql[9] = "INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT measurement_time, 'Total Num Calls ' AS Metric, site_name, sum(operator1), sum(operator2), sum(operator3), sum(operator4) FROM metrics.jflow_viewer_output_pivoted);";
        sql[10] = "SELECT (DATE(measurement_time)) AS measurement_time,  metric_name, COALESCE(ROUND(SUM(operator1)/(1048576 * 0.3)),0) AS VF, COALESCE(ROUND(SUM(operator2)/(1048576 * 0.3)),0) AS O2, COALESCE(ROUND(SUM(operator3)/(1048576 * 0.3)),0) AS THREE, COALESCE(ROUND(SUM(operator4)/(1048576 * 0.3)),0) AS EE FROM metrics.jflow_viewer_output_pivoted WHERE metric_name = 'Total Num Calls';";
        //System.out.println(siteName + " Call SQL is " + Arrays.toString(sql));
        return sql; 
    }

    /**
     * @param site
     * Customer oriented getSQL methods use 'IN' instead of REGEXP that's used for site-oriented
     * site-oriented methods use the same SQL as used by the Javascript on the website
     * @return the SQL that will output the Total Calls graph data
     */    
    private String[] getCustomerCallSQL(String customerName) {
        String[] sql = new String[9]; // update and query type commands separate
        sql[0] = "DELETE FROM metrics.jflow_viewer_output_aggregated;";
        sql[1] = "INSERT INTO metrics.jflow_viewer_output_aggregated (SELECT DATE(measurement_time), site_name, operator_id, SUM(cs_inbound) AS 'Calls Inbound', SUM(ps_inbound) AS 'Data Inbound', SUM(signalling_inbound) AS 'Remainder Inbound',  SUM(total_inbound) AS 'Total Inbound',SUM(cs_outbound) AS 'Calls Outbound', SUM(ps_outbound) AS 'Data Outbound', SUM(signalling_outbound) AS 'Remainder Outbound',  SUM(total_outbound) AS 'Total Outbound'FROM metrics.jflow WHERE measurement_time BETWEEN '" + startTime + "' AND '" + endTime + "' AND jflow.site_name IN (SELECT name from OpenCellCM.Site where Customer = '" + customerName + "') GROUP BY DATE(measurement_time), site_name, operator_id order by measurement_time, site_name, operator_id);";
        sql[2] = "DELETE FROM metrics.jflow_viewer_output_aggregated_live;";
        sql[3] = "INSERT INTO metrics.jflow_viewer_output_aggregated_live (select DISTINCT jflow_viewer_output_aggregated.* from metrics.jflow_viewer_output_aggregated, `ranmate-femto`.cells where cells.site_id LIKE CONCAT(jflow_viewer_output_aggregated.site_name, '%') and jflow_viewer_output_aggregated.operator_id = (cells.cell_no % 4) + 1 and cells.effective_to > '" + startTime + "' and cells.effective_from < '" + endTime + "' and cells.exist and NOT exclude order by jflow_viewer_output_aggregated.measurement_time);";
        sql[4] = "DELETE FROM metrics.jflow_viewer_output_pivoted;";
        sql[5] = "INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Num Calls Inbound' AS Metric, site_name, case when operator_id = 1 then cs_inbound end, case when operator_id = 2 then cs_inbound end,case when operator_id = 3 then cs_inbound end, case when operator_id = 4 then cs_inbound end FROM metrics.jflow_viewer_output_aggregated_live);";
        sql[6] = "INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Num Calls Outbound' AS Metric, site_name, case when operator_id = 1 then cs_outbound end, case when operator_id = 2 then cs_outbound end,case when operator_id = 3 then cs_outbound end, case when operator_id = 4 then cs_outbound end FROM metrics.jflow_viewer_output_aggregated_live);";
        sql[7] = "INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT measurement_time, 'Total Num Calls ' AS Metric, site_name, sum(operator1), sum(operator2), sum(operator3), sum(operator4) FROM metrics.jflow_viewer_output_pivoted);";
        sql[8] = "SELECT (DATE(measurement_time)) AS measurement_time,  metric_name, COALESCE(ROUND(SUM(operator1)/(1048576 * 0.3)),0) AS VF, COALESCE(ROUND(SUM(operator2)/(1048576 * 0.3)),0) AS O2, COALESCE(ROUND(SUM(operator3)/(1048576 * 0.3)),0) AS THREE, COALESCE(ROUND(SUM(operator4)/(1048576 * 0.3)),0) AS EE FROM metrics.jflow_viewer_output_pivoted WHERE metric_name = 'Total Num Calls';";
        //System.out.println(customerName + " Call SQL is " + Arrays.toString(sql));
        return sql; 
    }
    
    private String[] getSiteDataSQL(String siteName) {
        String[] sql = new String[10]; // update and query type commands separate
        sql[0] = "SET @VF_LIVE=(select count(*) from `ranmate-femto`.cells where exist and NOT exclude and effective_to > '" + startTime + "' and effective_from < '" + endTime + "' and site_id LIKE '" + siteName + "%' and cell_no % 4 = 0);";
        sql[1] = "SET @O2_LIVE=(select count(*) from `ranmate-femto`.cells where exist and NOT exclude and effective_to > '" + startTime + "' and effective_from < '" + endTime + "' and site_id LIKE '" + siteName + "%' and cell_no % 4 = 1);";
        sql[2] = "SET @THREE_LIVE=(select count(*) from `ranmate-femto`.cells where exist and NOT exclude and effective_to > '" + startTime + "' and effective_from < '" + endTime + "' and site_id LIKE '" + siteName + "%' and cell_no % 4 = 2);";
        sql[3] = "SET @EE_LIVE=(select count(*) from `ranmate-femto`.cells where exist and NOT exclude and effective_to > '" + startTime + "' and effective_from < '" + endTime + "' and site_id LIKE '" + siteName + "%' and cell_no % 4 = 3);";
        
        sql[4] = "DELETE FROM metrics.jflow_viewer_output_aggregated;";
        //sql[1] = "INSERT INTO metrics.jflow_viewer_output_aggregated (SELECT DATE(measurement_time), site_name, operator_id, SUM(cs_inbound) AS 'Calls Inbound', SUM(ps_inbound) AS 'Data Inbound', SUM(signalling_inbound) AS 'Remainder Inbound',  SUM(total_inbound) AS 'Total Inbound',SUM(cs_outbound) AS 'Calls Outbound', SUM(ps_outbound) AS 'Data Outbound', SUM(signalling_outbound) AS 'Remainder Outbound',  SUM(total_outbound) AS 'Total Outbound'FROM metrics.jflow WHERE measurement_time BETWEEN '" + startTime + "' AND '" + endTime + "'  AND jflow.site_name REGEXP '^" + siteName + "' GROUP BY DATE(measurement_time), site_name, operator_id order by measurement_time, site_name, operator_id);";
        sql[5] = "INSERT INTO metrics.jflow_viewer_output_aggregated (SELECT DATE(measurement_time), site_name, operator_id, SUM(cs_inbound) AS 'Calls Inbound', SUM(ps_inbound) AS 'Data Inbound', SUM(signalling_inbound) AS 'Remainder Inbound',  SUM(total_inbound) AS 'Total Inbound',SUM(cs_outbound) AS 'Calls Outbound', SUM(ps_outbound) AS 'Data Outbound', SUM(signalling_outbound) AS 'Remainder Outbound',  SUM(total_outbound) AS 'Total Outbound'FROM metrics.jflow WHERE measurement_time BETWEEN '" + startTime + "' AND '" + endTime + "'  AND jflow.site_name LIKE '" + siteName + "%' GROUP BY DATE(measurement_time), site_name, operator_id order by measurement_time, site_name, operator_id);";
        sql[6] = "DELETE FROM metrics.jflow_viewer_output_pivoted;";
        sql[7] = "INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Data Inbound' AS Metric, site_name, case when (operator_id = 1 AND @VF_LIVE) then ps_inbound end, case when (operator_id = 2 AND @O2_LIVE) then ps_inbound end,case when (operator_id = 3 AND @THREE_LIVE) then ps_inbound end, case when (operator_id = 4 AND @EE_LIVE) then ps_inbound end FROM metrics.jflow_viewer_output_aggregated);";
        sql[8] = "INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Data Outbound' AS Metric, site_name, case when (operator_id = 1 AND @VF_LIVE) then ps_outbound end, case when (operator_id = 2 AND @O2_LIVE) then ps_outbound end,case when (operator_id = 3 AND @THREE_LIVE) then ps_outbound end, case when (operator_id = 4 AND @EE_LIVE) then ps_outbound end FROM metrics.jflow_viewer_output_aggregated);";
        sql[9] = "SELECT (DATE(measurement_time)) AS measurement_time, metric_name, COALESCE(ROUND(SUM(operator1)/1048576),0) AS VF, COALESCE(ROUND(SUM(operator2)/1048576),0) AS O2, COALESCE(ROUND(SUM(operator3)/1048576),0) AS THREE, COALESCE(ROUND(SUM(operator4)/1048576),0) AS EE FROM metrics.jflow_viewer_output_pivoted GROUP BY metric_name ORDER BY NULL;";
        // System.out.println(siteName + " Data SQL is " + Arrays.toString(sql));
        return sql; 
    }

    private String[] getCustomerDataSQL(String customerName) {
        String[] sql = new String[8]; // update and query type commands separate
        sql[0] = "DELETE FROM metrics.jflow_viewer_output_aggregated;";
        sql[1] = "INSERT INTO metrics.jflow_viewer_output_aggregated (SELECT DATE(measurement_time), site_name, operator_id, SUM(cs_inbound) AS 'Calls Inbound', SUM(ps_inbound) AS 'Data Inbound', SUM(signalling_inbound) AS 'Remainder Inbound',  SUM(total_inbound) AS 'Total Inbound',SUM(cs_outbound) AS 'Calls Outbound', SUM(ps_outbound) AS 'Data Outbound', SUM(signalling_outbound) AS 'Remainder Outbound',  SUM(total_outbound) AS 'Total Outbound'FROM metrics.jflow WHERE measurement_time BETWEEN '" + startTime + "' AND '" + endTime + "'  AND jflow.site_name IN (SELECT name from OpenCellCM.Site where Customer = '" + customerName + "') GROUP BY DATE(measurement_time), site_name, operator_id order by measurement_time, site_name, operator_id);";
        sql[2] = "DELETE FROM metrics.jflow_viewer_output_aggregated_live;";
        sql[3] = "INSERT INTO metrics.jflow_viewer_output_aggregated_live (select DISTINCT jflow_viewer_output_aggregated.* from metrics.jflow_viewer_output_aggregated, `ranmate-femto`.cells where cells.site_id LIKE CONCAT(jflow_viewer_output_aggregated.site_name, '%') and jflow_viewer_output_aggregated.operator_id = (cells.cell_no % 4) + 1 and cells.effective_to > '" + startTime + "' and cells.effective_from < '" + endTime + "' and cells.exist and NOT exclude order by jflow_viewer_output_aggregated.measurement_time);";      
        sql[4] = "DELETE FROM metrics.jflow_viewer_output_pivoted;";
        sql[5] = "INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Data Inbound' AS Metric, site_name, case when operator_id = 1 then ps_inbound end, case when operator_id = 2 then ps_inbound end,case when operator_id = 3 then ps_inbound end, case when operator_id = 4 then ps_inbound end FROM metrics.jflow_viewer_output_aggregated_live);";
        sql[6] = "INSERT INTO metrics.jflow_viewer_output_pivoted (SELECT DATE(measurement_time), 'Data Outbound' AS Metric, site_name, case when operator_id = 1 then ps_outbound end, case when operator_id = 2 then ps_outbound end,case when operator_id = 3 then ps_outbound end, case when operator_id = 4 then ps_outbound end FROM metrics.jflow_viewer_output_aggregated_live);";
        sql[7] = "SELECT (DATE(measurement_time)) AS measurement_time,  metric_name, COALESCE(ROUND(SUM(operator1)/1048576),0) AS VF, COALESCE(ROUND(SUM(operator2)/1048576),0) AS O2, COALESCE(ROUND(SUM(operator3)/1048576),0) AS THREE, COALESCE(ROUND(SUM(operator4)/1048576),0) AS EE FROM metrics.jflow_viewer_output_pivoted GROUP BY metric_name ORDER BY NULL;";
        //System.out.println("Customer SQL is " + Arrays.toString(sql));
        return sql; 
    }
    
    /**
     * Very initial POC test
     * @return 
     */
    private static JFreeChart oldCreatePieChart(String site, int vf, int o2, int three, int ee) {
        int total = vf + o2 + three + ee;
        data = new DefaultPieDataset();
//        data.setValue("VF", new Double(vf/total));
//        data.setValue("O2", new Double(o2/total));
//        data.setValue("THREE", new Double(three/total));
//        data.setValue("EE", new Double(ee/total));
        data.setValue("VF", new Double(vf));
        data.setValue("O2", new Double(o2));
        data.setValue("THREE", new Double(three));
        data.setValue("EE", new Double(ee));
        return ChartFactory.createPieChart(site, data);
    }

    private JFreeChart createPieChart(int vfValue, int o2Value, int threeValue, int eeValue) {
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("VF", vfValue);
        dataset.setValue("O2", o2Value);
        dataset.setValue("THREE", threeValue);
        dataset.setValue("EE", eeValue);

        JFreeChart pieChart = ChartFactory.createPieChart("", dataset, true, false, false);
        PiePlot plot = (PiePlot) pieChart.getPlot();
 
        plot.setBackgroundPaint(null);
        plot.setInteriorGap(0.04);
        plot.setSectionPaint("VF", opcoColours[0]);
        plot.setSectionPaint("O2", opcoColours[1]);
        plot.setSectionPaint("THREE", opcoColours[2]);
        plot.setSectionPaint("EE", opcoColours[3]);
        plot.setOutlineVisible(false);

        plot.setShadowPaint(Color.WHITE);
        plot.setBaseSectionOutlinePaint(Color.white);
        plot.setSectionOutlinesVisible(true);
        plot.setBaseSectionOutlineStroke(new BasicStroke(2.0f));
        plot.setSimpleLabels(true);
        plot.setLegendItemShape(new Rectangle(10,10));
        
        plot.setLabelFont(new Font("Arial", Font.BOLD, 14));
        plot.setLabelOutlineStroke(null);
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelShadowPaint(null);
        pieChart.getLegend().setFrame(BlockBorder.NONE);

        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator("{2}", new DecimalFormat("0"), new DecimalFormat("0%")) {
            @Override
            public String generateSectionLabel(PieDataset dataset, Comparable key) {
                if (dataset.getValue(key) == null || dataset.getValue(key).intValue() == 0) {
                    return null;
                }
                return super.generateSectionLabel(dataset, key);
            }
        };
        plot.setLabelGenerator(gen);
        return pieChart;        
    }
    
    private JFreeChart createBarChart(int vfValue, int o2Value, int threeValue, int eeValue, String units, int[] cellCounts) {
//        double vfValue = 7500;
//        double o2Value = 3800;
//        double threeValue = 0;
//        double eeValue = 150;
        
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();  
//        dataset.addValue(vfValue, "", "VF (4)");
//        dataset.addValue(o2Value, "", "O2 (4)");
//        dataset.addValue(threeValue, "", "THREE (5)");
//        dataset.addValue(eeValue, "", "EE (2)");
        dataset.addValue(vfValue, "", "VF (" + cellCounts[0] + ")");
        dataset.addValue(o2Value, "", "O2 (" + cellCounts[1] + ")");
        if (cellCounts[2] > 99 ) {
            dataset.addValue(threeValue, "", "3 (" + cellCounts[2] + ")");
        } else {
            dataset.addValue(threeValue, "", "THREE (" + cellCounts[2] + ")");            
        }
        dataset.addValue(eeValue, "", "EE (" + cellCounts[3] + ")");

        JFreeChart barChart = ChartFactory.createBarChart("", "", units, dataset, PlotOrientation.VERTICAL, true, false, false);
           
        ChartPanel chartPanel = new ChartPanel(barChart);        
        //chartPanel.setPreferredSize(new java.awt.Dimension(560 , 367));  
        chartPanel.setBackground(Color.white);
        barChart.getLegend().setFrame(BlockBorder.NONE);
        
        CategoryPlot plot = (CategoryPlot)barChart.getPlot();
        plot.setBackgroundPaint(Color.white);

        // MW change test
        barChart.getCategoryPlot().setRenderer(new BarRenderer() {
            public Paint getItemPaint(final int row, final int column) {
                return opcoColours[column];
//                if (column==0)
//                    return new Color(255,36,36);
//                else if(column==1)
//                    return new Color(42,137,192);
//                else if(column==2)
//                    return new Color(182,95,194);
//                else 
//                    return new Color(43,172,177);
           }
        });

        //set  bar chart color
        ((BarRenderer)plot.getRenderer()).setBarPainter(new StandardBarPainter());
        BarRenderer renderer = (BarRenderer)barChart.getCategoryPlot().getRenderer();
        plot.setBackgroundPaint(Color.white);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(Color.gray);
        plot.getDomainAxis().setTickMarksVisible(false);
        renderer.setShadowVisible(false);
             
        // Z's original way of colouring
        //renderer.setSeriesPaint(0, new Color(255,36,36));
        //renderer.setSeriesPaint(1, new Color(42,137,192));
        //renderer.setSeriesPaint(2, new Color(182,95,194));
        //renderer.setSeriesPaint(3, new Color(43,172,177));
        // barChart.getLegend().setFrame(BlockBorder.NONE);
        
        barChart.getLegend().setVisible(false); // 
        return barChart;         
    }
        
    private ArrayList getCustomerList(boolean bruntwoodOnly) {
        ArrayList customers = new ArrayList(100);
        if (!bruntwoodOnly) { // used for testing, a custoemr with a small number of sites
            // requested to be changed from this to below by Andy Gillions email 1/5/2019 15:12
            // this causes reports to be generated for all customers, including those with only 1 site and the data within will be duplicated
            //String customerListSql = "SELECT customer FROM OpenCellCM.Site GROUP BY customer HAVING count(*) > 1";
            String customerListSql = "SELECT customer FROM OpenCellCM.Site GROUP BY customer";
            try {
                ResultSet result = executeQuerySQL(customerListSql);
                while (result.next()) {
                    customers.add(result.getString("customer"));
                }
            } catch (Exception e) {
                System.out.println("Unable to get list of customers");
            }
        } else {
            //customers.add("Bruntwood");
            //customers.add("Candy & Candy");
            //customers.add("Village Hotel");
            customers.add("Workspace");
        }
        return customers;
    }
    
    private ArrayList[] getSiteList(boolean bruntwoodOnly) {
        ArrayList<String>[] sites = new ArrayList[3];
        sites[0] = new ArrayList(100);
        sites[1] = new ArrayList(100); // stores the corresponding customer name as required by Andy Gillions 4/3/19
        sites[2] = new ArrayList(100); // stores the corresponding site id as re-required by Andy Gillions email 1/5/19 15:12
        String siteListSql = "";
        //String siteListSql = "Select DISTINCT site_name from metrics.routers ORDER BY site_name";            
        if (!bruntwoodOnly) { // used for testing, a customer with a small number of sites
            // siteListSql = "Select DISTINCT site_name, customer, id from metrics.routers, OpenCellCM.Site where routers.site_name = Site.name ORDER BY site_name;";            
            // only sites that have a live Femto
            siteListSql = "Select DISTINCT routers.site_name, Site.customer, Site.id from metrics.routers, OpenCellCM.Site, OpenCellCM.Femto where routers.site_name = Site.name and Femto.site_name = Site.name and Femto.live ORDER BY site_name;";            
            
        } else {
            siteListSql = "Select DISTINCT site_name, customer, id from metrics.routers, OpenCellCM.Site where routers.site_name = Site.name "
                    // + "AND (customer = 'Bruntwood' OR customer = 'Candy & Candy') ORDER BY site_name;";            
                    // + "AND (customer = 'Village Hotel') ORDER BY site_name;";            
                    + "AND name = 'The Frames' ORDER BY site_name;";            
        }
        try {
            ResultSet result = executeQuerySQL(siteListSql);
            while (result.next()) {
                sites[0].add(result.getString("site_name"));
                sites[1].add(result.getString("customer"));
                String siteId = result.getString("id");
                if (siteId == null) { // no need to check for this (sites[2].equals(""))) {
                    sites[2].add("");
                } else {
                    sites[2].add("  (" + siteId + ")");
                }
            }
        } catch (Exception e) {
            System.out.println("Unable to get list of sites");
        }
        return sites;
    }

    /**
     * Look, not ideal, but to avoid having to process the sql array before it's sent to this method, it's sent here in its entirety 
     * The last sql statement in the array is a query, which is not to be executed here
     * @param sql
     * @throws Exception 
     */
    private void executeUpdateSQL(String[] sql) throws Exception {
        int ret = -1;            
        try {
            //if (sql.length > 1) { // How did this work before
            if (sql.length == 2) { 
                ret = stmt.executeUpdate(sql[0]);
            } else {
                for (int i = 0; i < sql.length - 1; i++ ) {
                    stmt.addBatch(sql[i]);
                }
                stmt.executeBatch();
            }
        } catch(SQLException se){
           //Handle errors for JDBC
           System.out.println(se.getMessage());
           se.printStackTrace();
        } catch(Exception e){
           //Handle errors for Class.forName
           System.out.println(e.getMessage());
           e.printStackTrace();
        }
    }
    
    private ResultSet executeQuerySQL(String sql) throws Exception {
        ResultSet rs = null;      
        try {
           rs = stmt.executeQuery(sql);            
        } catch(SQLException se){
           //Handle errors for JDBC
           se.printStackTrace();
        } catch(Exception e){
           //Handle errors for Class.forName
           e.printStackTrace();
        }
        return rs;
    }

    private KeyValuePair<String[], String[][]> oldExecuteSQL(String sql) throws Exception {
                    
        KeyValuePair<String[], String[][]> result = null;
        if (!TEST_MODE) {
            result = ImsManager.igm.executeMySQLQuery(agentName, sql, true, null, null, "Concert");
            int numParams = result.getValue().length;
            if (numParams == 1) {
                System.out.println("Only 1 result row returned");
                if (result.getValue()[0][0].startsWith("Error during")) {
                    System.out.println("Error executing SQL: " + result.getValue()[0][0]);
                    throw new Exception("Error executing SQL");
                }
            }
        } else {
            try {
               ResultSet rs = stmt.executeQuery(sql);            
            } catch(SQLException se){
               //Handle errors for JDBC
               se.printStackTrace();
            } catch(Exception e){
               //Handle errors for Class.forName
               e.printStackTrace();
            }
        }
        return result;
    }

    /* 
     * A simple renderer for setting custom colors 
     * for a pie chart. 
     */     
    public static class PieRenderer 
    { 
        private Color[] color; 
        
        public PieRenderer(Color[] color) { 
            this.color = color; 
        }        
        
        public void setColor(PiePlot plot, DefaultPieDataset dataset) { 
            List <Comparable> keys = dataset.getKeys(); 
            int aInt; 
            
            for (int i = 0; i < keys.size(); i++) 
            { 
                aInt = i % this.color.length; 
                plot.setSectionPaint(keys.get(i), this.color[aInt]); 
            } 
        } 
    } 
        
}
