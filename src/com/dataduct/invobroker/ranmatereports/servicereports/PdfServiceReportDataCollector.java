package com.dataduct.invobroker.ranmatereports.servicereports;

import com.dataduct.invobroker.ranmatereports.PDFReportCreator4Mnos;
import java.awt.Color;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author zorana
 */
public class PdfServiceReportDataCollector implements PdfServiceReportDataCollectorInterface {

    private PDFReportCreator4Mnos creator = null;
    
    public PdfServiceReportDataCollector(PDFReportCreator4Mnos theCreator) {
        creator = theCreator;
    }
    
    //select DATE(measurement_time) AS 'DATE',
    //ROUND(SUM(CIS_tmp_reports.num * CIS_tmp_reports.cells) / SUM(CIS_tmp_reports.cells), 2) AS 'CIS', 
    //ROUND(ifnull(((SUM(STANDALONESRBSUCCESS)/ SUM(STANDALONESRBATTEMPTS)) * 100),100),2) AS 'RRCCONSUCCESSRATE',
    //ROUND(ifnull(((SUM(CSSPEECHRABSUCCESS)/ SUM(CSSPEECHRABATTEMPTS)) * 100),100),2) AS 'CSSETUPSUCCESSRATE',
    //ROUND(ifnull((((SUM(SPEECHDROPAPINITATED) + SUM(CSVIDEODROPAPINITIATED) + SUM(CSVIDEODROPCNINITIATED)) / (SUM(CSSPEECHRABSUCCESS) + SUM(CSVIDEORABSUCCESS))) * 100),0),2) AS `CSDROPRATE`,
    //ROUND(ifnull((((SUM(PSR99RABSUCCESS) + SUM(PSHSDPARABSUCCESS)) / (SUM(PSR99RABATTEMPTS) + SUM(PSHSDPARABATTEMPTS))) * 100),100),2) AS `PSSETUPSUCCESSRATE`,
    //ROUND(ifnull(((SUM(PSDROPAPINITIATED) / (SUM(PSHSDPARABSUCCESS) + SUM(PSR99RABSUCCESS))) * 100),0),2) AS `PSDROPRATE`,
    //ROUND(ifnull(((SUM(INTERFAPCSHANDINSUCCESS) + SUM(INTERFAPCSHANDINCANCELS)) / (SUM(INTERFAPCSHANDINATTEMPTS)) * 100),100),2) AS `CSHANDINSUCCESSRATE`,
    //ROUND(ifnull(((SUM(INTERFAPPSHANDINSUCCESS) + SUM(INTERFAPPSHANDINCANCELS)) / (SUM(INTERFAPPSHANDINATTEMPTS)) * 100),100),2) AS `PSHANDINSUCCESSRATE`
    //from metrics.mno_counters, Concert.Site, Concert.Femto, metrics.CIS_tmp_reports 
    //where measurement_time BETWEEN '2020-02-01 00:00' and '2020-02-05 23:59' 
    //and (Site.tto = 'N' || Site.tto = 'P' || Site.tto = 'E') 
    //#and Site.id = 'MIC001'
    //and mno_counters.imei = Femto.imei 
    //and Femto.site_id = Site.id 
    //and CIS_tmp_reports.site_id = Site.id
    //#group by DATE(measurement_time)    
    @Override
    public String[] getNwPerformance( boolean isTto) {
        String[] nwPerformance = new String[9];
        String sql = null;
        String countSql = null;
        // for some reason I think this has to be 1 larger than the number of SQL array elements below
        String[] cisSql = new String[6]; // String[6] if we ever revert to RANmate CIS
        cisSql[0] = "delete from metrics.CIS_tmp_reports;";

        // CIS values taken from RANmate
        /* cisSql[1] = "INSERT INTO metrics.CIS_tmp_reports (site_id, num) SELECT node_id, ROUND(AVG(avg_cis_kpi_all),2) AS CIS " +
                       "FROM `ranmate-femto`.v_dashboard_hourly_site_operator_performance_history, `ranmate-femto`.sites " +
                        "WHERE v_dashboard_hourly_site_operator_performance_history.group_id = 'OpenCell' AND measurement_time BETWEEN '" + creator.startTime + "' and '" + creator.endTime +
                        //creator.getNetworkExclusionSql() + creator.getSiteExclusionSql(site) + creator.getBizHoursSql(site) +
                        "' and sector_no = 2 and sites.site_id = v_dashboard_hourly_site_operator_performance_history.site_id  and sites.group_id = 'OpenCell' " +
                        "and (avg_cis_kpi_all <> 0 or exists (select * from `ranmate-femto`.sites_sectors_live " +
                        "where v_dashboard_hourly_site_operator_performance_history.site_id = sites_sectors_live.site_id and v_dashboard_hourly_site_operator_performance_history.sector_no = sites_sectors_live.sector_no)) " +
                        "group by node_id;"; */
        // CIS values taken from PM Files
// Using Andy's UPTIME proposal - 12s to transfer 5 days worth so far
//DELETE FROM UPTIME_reports_tmp;
//insert INTO UPTIME_reports_tmp (measurement_time, measurement_period, imei, up_seconds) 
//select measurement_time - INTERVAL EXTRACT(SECOND from measurement_time) SECOND, measurement_period, imei, UPTIME
//from mno_counters
//where measurement_time BETWEEN SUBTIME('2020-02-05 00:00', '0:15:0') and '2020-02-05 23:59' 
//and measurement_period = 900;
        
//  0.6s for 10,000 rows representing 3 hours of data when indexed
//update UPTIME_reports_tmp up1 
//             left outer JOIN UPTIME_reports_tmp up2 
//             on up1.imei = up2.imei 
//             and TIMESTAMPDIFF(SECOND,up1.measurement_time,up2.measurement_time) = 900 
//set up2.delta_seconds = 
//(CASE
//    WHEN up1.up_seconds IS NULL THEN 900
//    WHEN up2.up_seconds IS NULL THEN 900
//    WHEN up2.up_seconds <= 900 THEN up2.up_seconds
//    ELSE LEAST(up2.up_seconds - up1.up_seconds, 900)
//END);

// NOW being done as part of pre processing        
//        cisSql[1] = "delete from metrics.UPTIME_reports_tmp;";
//        cisSql[2] = "insert INTO metrics.UPTIME_reports_tmp (measurement_time, measurement_period, imei, up_seconds) " + 
//                "select measurement_time - INTERVAL EXTRACT(SECOND from measurement_time) SECOND, measurement_period, imei, UPTIME " +
//                "from metrics.mno_counters where measurement_time BETWEEN SUBTIME('" + creator.startTime + "', '0:15:0') and '" + creator.endTime + "' " +  // need to include the meas interval before the period in question so that uptime can be calculated
//                "and measurement_period = 900;";
//        cisSql[3] = "update metrics.UPTIME_reports_tmp up1 left outer JOIN metrics.UPTIME_reports_tmp up2 on up1.imei = up2.imei " +
//                    "and TIMESTAMPDIFF(SECOND,up1.measurement_time,up2.measurement_time) = 900 " +
//                    "set up2.delta_seconds = (CASE WHEN up1.up_seconds IS NULL THEN 900 WHEN up2.up_seconds IS NULL THEN 900 WHEN up2.up_seconds <= 900 THEN up2.up_seconds " +
//                    //"set up2.delta_seconds = (CASE WHEN up1.up_seconds IS NULL THEN 900 WHEN up2.up_seconds IS NULL THEN 900 WHEN (up2.up_seconds > 0 and up2.up_seconds <= 900) THEN 900 " + // gaming the system using 900 instead of up2.up_seconds - this covers the restarts
//                    "ELSE LEAST(up2.up_seconds - up1.up_seconds, 900) END);";
//        cisSql[1] = "INSERT INTO metrics.CIS_tmp_reports (site_id, num) select site_id, ROUND(AVG(delta_seconds)/9,2) AS CIS " +
//                    "from metrics.UPTIME_reports_tmp, Concert.Femto where UPTIME_reports_tmp.imei = Femto.imei and measurement_time >= '" + creator.startTime + "' group by site_id;";
        cisSql[1] = "INSERT INTO metrics.CIS_tmp_reports (site_id, num) select site_id, ROUND(AVG(delta_seconds)/9,2) AS CIS " +
                    "from metrics.mno_counters_uptime_delta, Concert.Femto where mno_counters_uptime_delta.imei = Femto.imei and measurement_time >= '" + creator.startTime + "' group by site_id;";

        // Common SQL        
        cisSql[2] = "delete from metrics.CIS_CellCount_tmp_reports;";
        cisSql[3] = "insert into metrics.CIS_CellCount_tmp_reports select Site.id, count(DISTINCT Femto.imei) from Concert.Site, Concert.Femto " +
                    "where Femto.site_id = Site.id and Femto.exists and Femto.id % 4 = 3 group by site_id;";
        cisSql[4] = "update metrics.CIS_tmp_reports INNER JOIN metrics.CIS_CellCount_tmp_reports ON metrics.CIS_tmp_reports.site_id = CIS_CellCount_tmp_reports.site_id set cells = CIS_CellCount_tmp_reports.num;";

        
        // Duration for 1 query: 7.666 sec. */
        String ttoSql = "select ROUND(SUM(CIS_tmp_reports.num * CIS_tmp_reports.cells) / SUM(CIS_tmp_reports.cells), 2), " +
                        "ROUND(ifnull(((SUM(STANDALONESRBSUCCESS)/ SUM(STANDALONESRBATTEMPTS)) * 100),100),2) AS 'RRCCONSUCCESSRATE', " +
                        "ROUND(ifnull(((SUM(CSSPEECHRABSUCCESS)/ SUM(CSSPEECHRABATTEMPTS)) * 100),100),2) AS 'CSSETUPSUCCESSRATE', " +
                        "ROUND(ifnull((((SUM(SPEECHDROPAPINITATED) + SUM(CSVIDEODROPAPINITIATED) + SUM(CSVIDEODROPCNINITIATED)) / (SUM(CSSPEECHRABSUCCESS) + SUM(CSVIDEORABSUCCESS))) * 100),0),2) AS `CSDROPRATE`, " +
                        "ROUND(ifnull((((SUM(PSR99RABSUCCESS) + SUM(PSHSDPARABSUCCESS)) / (SUM(PSR99RABATTEMPTS) + SUM(PSHSDPARABATTEMPTS))) * 100),100),2) AS `PSSETUPSUCCESSRATE`, " +
                        "ROUND(ifnull(((SUM(PSDROPAPINITIATED) / (SUM(PSHSDPARABSUCCESS) + SUM(PSR99RABSUCCESS))) * 100),0),2) AS `PSDROPRATE`, " +
                        "ROUND(ifnull(((SUM(INTERFAPCSHANDINSUCCESS) + SUM(INTERFAPCSHANDINCANCELS)) / (SUM(INTERFAPCSHANDINATTEMPTS)) * 100),100),2) AS `CSHANDINSUCCESSRATE`, " +
                        "ROUND(ifnull(((SUM(INTERFAPPSHANDINSUCCESS) + SUM(INTERFAPPSHANDINCANCELS)) / (SUM(INTERFAPPSHANDINATTEMPTS)) * 100),100),2) AS `PSHANDINSUCCESSRATE`  " +
//                            "from metrics.mno_kpis, Concert.Site, Concert.Femto, metrics.CIS_tmp_reports " +
                        "from metrics.mno_counters, Concert.Site, Concert.Femto, metrics.CIS_tmp_reports " +
                        "where measurement_time BETWEEN '" + creator.startTime + "' and '" + creator.endTime +
                        "' and (Site.tto = 'T' or Site.tto = 'C' or Site.tto = 'E') and mno_counters.imei = Femto.imei and Femto.site_id = Site.id and CIS_tmp_reports.site_id = Site.id";
        // String ttoCountSql = "select count(*) from Site where tto = 'T'";         // original count
        String ttoCountSql = "select count(DISTINCT Site.id) from Femto, Site " +
                       "where (Femto.id % 4 = 3 and switch_id NOT LIKE '%_VF' and switch_id NOT LIKE '%_O2' and switch_id NOT LIKE '%_EE') " +
                       "and (tto = 'T' or tto = 'C' or tto = 'E') and Femto.site_id = Site.id and Femto.site_name NOT LIKE 'Test %'"; //  and Femto.live";
        
        String nonTtoSql = "select ROUND(SUM(CIS_tmp_reports.num * CIS_tmp_reports.cells) / SUM(CIS_tmp_reports.cells), 2), " +
                            "ROUND(ifnull(((SUM(STANDALONESRBSUCCESS)/ SUM(STANDALONESRBATTEMPTS)) * 100),100),2) AS 'RRCCONSUCCESSRATE', " +
                            "ROUND(ifnull(((SUM(CSSPEECHRABSUCCESS)/ SUM(CSSPEECHRABATTEMPTS)) * 100),100),2) AS 'CSSETUPSUCCESSRATE', " +
                            "ROUND(ifnull((((SUM(SPEECHDROPAPINITATED) + SUM(CSVIDEODROPAPINITIATED) + SUM(CSVIDEODROPCNINITIATED)) / (SUM(CSSPEECHRABSUCCESS) + SUM(CSVIDEORABSUCCESS))) * 100),0),2) AS `CSDROPRATE`, " +
                            "ROUND(ifnull((((SUM(PSR99RABSUCCESS) + SUM(PSHSDPARABSUCCESS)) / (SUM(PSR99RABATTEMPTS) + SUM(PSHSDPARABATTEMPTS))) * 100),100),2) AS `PSSETUPSUCCESSRATE`, " +
                            "ROUND(ifnull(((SUM(PSDROPAPINITIATED) / (SUM(PSHSDPARABSUCCESS) + SUM(PSR99RABSUCCESS))) * 100),0),2) AS `PSDROPRATE`, " +
                            "ROUND(ifnull(((SUM(INTERFAPCSHANDINSUCCESS) + SUM(INTERFAPCSHANDINCANCELS)) / (SUM(INTERFAPCSHANDINATTEMPTS)) * 100),100),2) AS `CSHANDINSUCCESSRATE`, " +
                            "ROUND(ifnull(((SUM(INTERFAPPSHANDINSUCCESS) + SUM(INTERFAPPSHANDINCANCELS)) / (SUM(INTERFAPPSHANDINATTEMPTS)) * 100),100),2) AS `PSHANDINSUCCESSRATE`  " +
//                            "from metrics.mno_kpis, Concert.Site, Concert.Femto, metrics.CIS_tmp_reports " +
                            "from metrics.mno_counters, Concert.Site, Concert.Femto, metrics.CIS_tmp_reports " +
                           "where measurement_time BETWEEN '" + creator.startTime + "' and '" + creator.endTime +
                           // "' and (Site.tto = 'N' || Site.tto = 'P' || Site.tto = 'E' || Site.tto = 'C') and mno_kpis.imei = Femto.imei and Femto.site_id = Site.id";
                           "' and Site.tto = 'N' and mno_counters.imei = Femto.imei and Femto.site_id = Site.id and CIS_tmp_reports.site_id = Site.id";
        //String nonTtoCountSql = "select count(*) from Site where tto = 'N' || tto = 'P' || tto = 'E'"; // || tto = 'C'";
        // String nonTtoCountSql = "select count(*) from Site where tto = 'N'"; // Updated 17/4/2020 following concall // original 
        String nonTtoCountSql = "select count(DISTINCT Site.id) from Femto, Site " +
                       "where (Femto.id % 4 = 3 and switch_id NOT LIKE '%_VF' and switch_id NOT LIKE '%_O2' and switch_id NOT LIKE '%_EE') " +
                       "and tto = 'N' and Femto.site_id = Site.id and Femto.site_name NOT LIKE 'Test %'"; //  and Femto.live";
        
        if (isTto) {
            sql = ttoSql;
            countSql = ttoCountSql;
//            nwPerformance[0] = "56";        //number of live TTO sites
//            nwPerformance[1] = "99.99";     //availability for TTO sites
//            nwPerformance[2] = "99.49";     //RRC SR rate for TTO sites
//            nwPerformance[3] = "99.50";     //call setup success rate
//            nwPerformance[4] = "3.65";      //call drop rate
//            nwPerformance[5] = "99.79";     //packet setup success rate
//            nwPerformance[6] = "4.24";      //packet drop rate
//            nwPerformance[7] = "96.17";     //inter FAP CS
//            nwPerformance[8] = "95.56";     //inter FAP PS
        } else {                            //as above for Non-TTO sites
            sql = nonTtoSql;
            countSql = nonTtoCountSql;
//            nwPerformance[0] = "12";
//            nwPerformance[1] = "99.95";
//            nwPerformance[2] = "99.01";
//            nwPerformance[3] = "99.64";
//            nwPerformance[4] = "5.93";
//            nwPerformance[5] = "99.42";
//            nwPerformance[6] = "9.63";
//            nwPerformance[7] = "93.05";
//            nwPerformance[8] = "94.74";
        }
        try {
            long start = new Date().getTime();
            ResultSet result = creator.executeQuerySQL(countSql);            
            System.out.println((new Date().getTime() - start)/1000 + " seconds to execute getNwPerformance() countSql");
            
            //while (result.next()) { // only has 1 entry
            result.next();
            nwPerformance[0] = result.getString(1);      
        } catch (Exception e) {
            System.out.println("Unable to get the count: " + e.getMessage());
        }

        try {
            // System.out.println("CIS Sql is " + cisSql[1]);
            long start = new Date().getTime();
            creator.executeUpdateSQL(cisSql);                
            System.out.println((new Date().getTime() - start)/1000 + " seconds to execute getNwPerformance() cisSql");
            //System.out.println("CIS Sql execution finished");
        } catch (Exception e) {
            System.out.println("Unable to create the CIS Temp Table: " + e.getMessage());
            System.out.println("Offending SQL is " + sql);
        }
        
        
        //System.out.println("Executing NW Performance SQL: " + sql);
        try {
            long start = new Date().getTime();
            ResultSet result = creator.executeQuerySQL(sql);            
            System.out.println((new Date().getTime() - start)/1000 + " seconds to execute getNwPerformance() main sql");
            while (result.next()) { // only has 1 entry
                for (int i = 1; i < 9; i++) {
                    nwPerformance[i] = result.getString(i);      
                }
            }            
        } catch (Exception e) {
            System.out.println("Unable to get the NW Performance KPIs: " + e.getMessage());
            System.out.println("Offending SQL is " + sql);
        }        
        if (!isTto) creator.nonTtoOverallNwPerformance = nwPerformance;
        return nwPerformance;
    }

    @Override
    public SitePerformance[] getSitePerformance( boolean isTto) {
        // Site numbers from Concert if we ever want to use this sql to populate a temp table and take from there
        // select postcode, Site.name, count(Femto.id) from Site, Femto where Femto.site_id = Site.id and Femto.id % 4 = 3 and Femto.live and Site.tto = 'C' group by postcode order by Site.name        
        String[] csRabSql = new String[3]; // the creator.executeUpdateSql() method requires an array 1 longer than the nunmber of SQL statements for reasons beyond everyone
        csRabSql[0] = "delete from metrics.CSSPEECHRABATTEMPTS_tmp_reports;";
        csRabSql[1] = "INSERT INTO metrics.CSSPEECHRABATTEMPTS_tmp_reports (site_id, num) select Femto.site_id, SUM(CSSPEECHRABATTEMPTS) from metrics.mno_counters, Concert.Femto " +
                        "where mno_counters.imei = Femto.imei and mno_counters.measurement_time BETWEEN '" + creator.startTime + "' and '" + creator.endTime +
                        "' group by site_id;";
                
        String sql = null;
        String sqlCommon1 = "select postcode, CIS_tmp_reports.num, " +
             // Ye Olde Lodgik
//                "ROUND(AVG(RRCCONSUCCESSRATE),2) AS 'RRC', "
//                "ROUND(AVG(CSSETUPSUCCESSRATE),2) AS 'CSSR', "
//                "ROUND(AVG(CSDROPRATE),2) AS 'CSDR', " +
//                "ROUND(AVG(PSSETUPSUCCESSRATE),2) AS 'PSSR', "
//        + "ROUND(AVG(PSDROPRATE),2) AS 'PSDR', "
//        + "ROUND(AVG(CSHANDINSUCCESSRATE),2) AS 'CSHSR', "
//        + "ROUND(AVG(PSHANDINSUCCESSRATE),2) AS 'PSHSR', " + 
//                "ROUND(AVG(INTERHANDOUTSUCCESSRATE),2)  AS 'Inter Handout', "
//        + "ROUND(AVG(INTRAHANDOUTSUCCESSRATE),2)  AS 'Inter Handover', "
//        + "CSSPEECHRABATTEMPTS_tmp_reports.num, " +
                "ROUND(ifnull(((SUM(STANDALONESRBSUCCESS)/ SUM(STANDALONESRBATTEMPTS)) * 100),100),2) AS 'RRC', " +
                "ROUND(ifnull(((SUM(CSSPEECHRABSUCCESS)/ SUM(CSSPEECHRABATTEMPTS)) * 100),100),2) AS 'CSSR', " +
                "ROUND(ifnull((((SUM(SPEECHDROPAPINITATED) + SUM(CSVIDEODROPAPINITIATED) + SUM(CSVIDEODROPCNINITIATED)) / (SUM(CSSPEECHRABSUCCESS) + SUM(CSVIDEORABSUCCESS))) * 100),0),2) AS `CSDR`, " +
                "ROUND(ifnull((((SUM(PSR99RABSUCCESS) + SUM(PSHSDPARABSUCCESS)) / (SUM(PSR99RABATTEMPTS) + SUM(PSHSDPARABATTEMPTS))) * 100),100),2) AS `PSSR`, " +
                "ROUND(ifnull(((SUM(PSDROPAPINITIATED) / (SUM(PSHSDPARABSUCCESS) + SUM(PSR99RABSUCCESS))) * 100),0),2) AS `PSDR`, " +
                "ROUND(ifnull(((SUM(INTERFAPCSHANDINSUCCESS) + SUM(INTERFAPCSHANDINCANCELS)) / (SUM(INTERFAPCSHANDINATTEMPTS)) * 100),100),2) AS `CSHSR`, " +
                "ROUND(ifnull(((SUM(INTERFAPPSHANDINSUCCESS) + SUM(INTERFAPPSHANDINCANCELS)) / (SUM(INTERFAPPSHANDINATTEMPTS)) * 100),100),2) AS `PSHSR`,  " +
                "ROUND(ifnull(((SUM(INTERFAPSPEECHINTERFREQHANDOUTSUCCESS) + SUM(INTERFAPSPEECHINTERFREQHANDOUTCANCELS)) / (SUM(INTERFAPSPEECHINTERFREQHANDOUTATTEMPTS)) * 100),100),2) AS `Inter Handout`, " +
                "LEAST(ROUND(ifnull(((SUM(INTERFAPSPEECHINTRAFREQHANDOUTSUCCESS) + SUM(INTERFAPSPEECHINTRAFREQHANDOUTCANCELS)) / (SUM(INTERFAPSPEECHINTRAFREQHANDOUTATTEMPTS)) * 100),100),2),100) AS `Intra Handout`, " +
                "CSSPEECHRABATTEMPTS_tmp_reports.num, " +                                
                "Site.name, count(DISTINCT Femto.imei), Site.reason " +
//                "from metrics.mno_kpis, Concert.Site, Concert.Femto, metrics.CSSPEECHRABATTEMPTS_tmp_reports, metrics.CIS_tmp_reports " + // , metrics.mno_counters " +
                "from metrics.mno_counters, Concert.Site, Concert.Femto, metrics.CSSPEECHRABATTEMPTS_tmp_reports, metrics.CIS_tmp_reports " + // , metrics.mno_counters " +
                "where mno_counters.measurement_time BETWEEN '" + creator.startTime + "' and '" + creator.endTime;
                //"' and mno_counters.measurement_time BETWEEN '" + creator.startTime + "' and '" + creator.endTime;        
        String concessionSqlFragment =  "' and Site.tto = 'C' ";
        String nonTtoSqlFragment =  "' and Site.tto = 'N' ";
        //String sqlCommon2 = "and mno_kpis.imei = Femto.imei and mno_counters.imei = Femto.imei and Femto.site_id = Site.id " +
        String sqlCommon2 = "and mno_counters.imei = Femto.imei and Femto.site_id = Site.id and CSSPEECHRABATTEMPTS_tmp_reports.site_id = Site.id and CIS_tmp_reports.site_id = Site.id " +
                        "group by postcode order by Site.name";
        String concessionTtoSql = sqlCommon1 + concessionSqlFragment + sqlCommon2;
        String nonTtoSql = sqlCommon1 + nonTtoSqlFragment + sqlCommon2;
        
        int length;
        if (isTto) {
            length = 24; // testing
            sql = concessionTtoSql;
            //System.out.println("Executing Concession SitePerformance SQL: " + sql);                
        } else {
            length = 15;
            sql = nonTtoSql;
        }

        try {
            // System.out.println("CSRAB Sql is " + csRabSql[1]);
            long start = new Date().getTime();
            creator.executeUpdateSQL(csRabSql);                
            System.out.println((new Date().getTime() - start)/1000 + " seconds to execute getSitePerformance() csRabSql");
            //System.out.println("CSRAB Sql execution finished");
        } catch (Exception e) {
            System.out.println("Unable to create the CSRAB Attempts Temp Table: " + e.getMessage());
            System.out.println("Offending SQL is " + sql);
        }
        
        //System.out.println("Executing SitePerformance SQL: " + sql);                
        ArrayList<SitePerformance> sitePerformanceArrayList = new ArrayList(10);
        try {
            long start = new Date().getTime();
            ResultSet result = creator.executeQuerySQL(sql);                
            System.out.println((new Date().getTime() - start)/1000 + " seconds to execute getSitePerformance() main sql");
            int numNonTtoSites = 0;
            double interFreqHandouts = 0;
            double interFreqHandovers = 0;
            int csAttempts = 0;
            int cells = 0;
            while (result.next()) {
//    public SitePerformance( String siteReference, double callAvailability, double rrcSR, double csSR, double csDCR, double psSR, double psDSR, double interFapCS,
//                double interFapPS, String interFreqHandouts, String interFreqHandover, String csAttempts, String site, int cells, String concessionReason) {
                sitePerformanceArrayList.add(new SitePerformance(result.getString(1),   // siteReference
                                                                 result.getDouble(2),   // callAvailability
                                                                 result.getDouble(3),   // rrcSR
                                                                 result.getDouble(4),   // csSR
                                                                 result.getDouble(5),   // csDCR
                                                                 result.getDouble(6),   // psSR
                                                                 result.getDouble(7),   // psDSR
                                                                 result.getDouble(8),   // interFapCS
                                                                 result.getDouble(9),   // interFapPS
                                                                 result.getString(10),   // interFreqHandouts
                                                                 result.getString(11),   // interFreqHandover
                                                                 result.getString(12),   // csAttempts
                                                                 result.getString(13),   // site
                                                                 result.getInt(14),      // cells
                                                                 result.getString(15)    // concessionReason
                ));
                if (!isTto) {
                   numNonTtoSites++;
                   interFreqHandouts += Double.valueOf(result.getString(10));
                   interFreqHandovers += Double.valueOf(result.getString(11));
                   csAttempts += Integer.valueOf(result.getString(12));
                   cells += result.getInt(14);
                }                
            }
            /// store them in the creator to avoid having to recalculate them again
            creator.nonTtoHands[0] = interFreqHandouts / numNonTtoSites;
            creator.nonTtoHands[1] = interFreqHandouts / numNonTtoSites;
            creator.nonTtoCounts[0] = csAttempts;
            creator.nonTtoCounts[1] = numNonTtoSites;
            creator.nonTtoCounts[2] = cells;
            
        } catch (Exception e) {
            System.out.println("Unable to get the SitePerformance KPIs: " + e.getMessage());
            System.out.println("Offending SQL is " + sql);
        }
                
        SitePerformance[] sitesPerformance = new SitePerformance[sitePerformanceArrayList.size()];
        for (int i = 0; i < sitesPerformance.length; i++) {
            sitesPerformance[i] = sitePerformanceArrayList.get(i);
        } 
        return sitesPerformance;
    }

    @Override
    public SitePerformance getTotalSitePerformance(boolean isTto) {
        //return new SitePerformance("", 99.99, 99.01, 99.64, 5.93, 99.42, 9.63, 93.05, 94.74, "", "", "", "18", 197, "");
        return new SitePerformance("", Double.valueOf(creator.nonTtoOverallNwPerformance[1]), 
                                       Double.valueOf(creator.nonTtoOverallNwPerformance[2]),
                                       Double.valueOf(creator.nonTtoOverallNwPerformance[3]),
                                       Double.valueOf(creator.nonTtoOverallNwPerformance[4]),
                                       Double.valueOf(creator.nonTtoOverallNwPerformance[5]),
                                       Double.valueOf(creator.nonTtoOverallNwPerformance[6]),
                                       Double.valueOf(creator.nonTtoOverallNwPerformance[7]),
                                       Double.valueOf(creator.nonTtoOverallNwPerformance[8]),
                                       String.valueOf(creator.nonTtoHands[0]),
                                       String.valueOf(creator.nonTtoHands[1]),
                                       String.valueOf(creator.nonTtoCounts[0]),
                                       String.valueOf(creator.nonTtoCounts[1]),
                                       creator.nonTtoCounts[2], 
                                       "");
    }

    @Override
    public SiteStatus[] getSiteStatus(boolean isTto) {
        String sql = null;
        
        // Updated on 20/4/2020 according to Andy and John's email
        // String ttoSql = "select name, reason, action from Site where TTO = 'T' and reason != '' and reason IS NOT NULL and Site.name NOT LIKE 'Test %' order by name;";
        String ttoSql = "select name, reason, action from Site where TTO = 'T' and reason != '' and reason IS NOT NULL and Site.name NOT LIKE 'Test %' order by name;";
        String nonTtoSql = "select name, reason, action from Site where TTO = 'N' and reason != '' and reason IS NOT NULL and Site.name NOT LIKE 'Test %' order by name;";
        
        //int length; testing
        if (isTto) {
            sql = ttoSql;
            //length = 24;
        } else {
            sql = nonTtoSql;
            //length = 12;
        }

        // System.out.println("Executing SiteStatus SQL: " + sql);                
        ArrayList<SiteStatus> sitesStatusArrayList = new ArrayList(10);
        try {
            ResultSet result = creator.executeQuerySQL(sql);                
            while (result.next()) {
                Color overallColour = creator.overallSiteColour.get(result.getString(1));
                if (overallColour == null) {
                    overallColour = PdfServiceReportConfig.statusRed;
                }
                String comment = result.getString(2);
                if (!isTto || overallColour.equals(PdfServiceReportConfig.statusRed)) {
                    sitesStatusArrayList.add(new SiteStatus(result.getString(1), 
                                                            overallColour, // previously hardcoded to Green new Color(125, 184, 55), 
                                                            comment, 
                                                            result.getString(3)));
                }
                if (comment.matches("(.*)INC\\d{12}(.*)")) {
                    //System.out.println(result.getString(1) + " has a recorded incident " + comment);
                    creator.incidents.add(new String[]{result.getString(1), comment});
                }
            }            
        } catch (Exception e) {
            System.out.println("Unable to get the SiteStatus KPIs: " + e.getMessage());
            System.out.println("Offending SQL is " + sql);
        }
                        
        SiteStatus[] sitesStatus = new SiteStatus[sitesStatusArrayList.size()];
        for (int i = 0; i < sitesStatus.length; i++) {
            sitesStatus[i] = sitesStatusArrayList.get(i);
        }
        return sitesStatus;
    }

    @Override
    public String[] getUpcomingSites() {
// ZTest
//        int length = 28;
//        String[] upcomingSites = new String[length];
//        for (int i = 0; i < length; i++) {
//            upcomingSites[i] = "We Work 1 - Poultry (22) - live in testing few more chars. We Work 1 - Poultry (22) - live in testing. " + i;
//        }

        String sql = "select concat(customer, ' - ', Site.name, ' (', count(Femto.objectId), ') - ', Site.reason) AS 'fullname' from Concert.Site, Concert.Femto " +
                    "where (TTO = 'D') and (Femto.id % 4 = 3) and Femto.site_id = Site.id and Site.name NOT LIKE 'Test %' group by Site.id order by fullname";
        // System.out.println("Executing Upcoming Site SQL: " + sql);                
        ArrayList<String> upcomingSitesArrayList = new ArrayList(10);
        try {
            ResultSet result = creator.executeQuerySQL(sql);                
            while (result.next()) {
                upcomingSitesArrayList.add(result.getString(1));
            }            
        } catch (Exception e) {
            System.out.println("Unable to get the Upcoming Sites: " + e.getMessage());
            System.out.println("Offending SQL is " + sql);
        }
                        
        String[] upcomingSites = new String[upcomingSitesArrayList.size()];
        for (int i = 0; i < upcomingSites.length; i++) {
            upcomingSites[i] = upcomingSitesArrayList.get(i);
        }
        return upcomingSites;
    }

    @Override
    public String[] getPendingDesignSites() {
// ZTest
//        int length = 6;
//        String[] pendingDS = new String[length];
//        for (int i = 0; i < length; i++) {
//            pendingDS[i] = "We Work 1 - Poultry (22) - live in testing few more " + i;
//        }
        String sql = "select concat(customer, ' - ', Site.name) AS 'fullname' from Concert.Site, Concert.Femto " +
                    "where (TTO = 'P') and (Femto.id % 4 = 3) and Femto.site_id = Site.id and Site.name NOT LIKE 'Test %' group by Site.id order by fullname";
        //System.out.println("Executing Pending Design SQL: " + sql);                
        ArrayList<String> pendingDSArrayList = new ArrayList(10);
        try {
            ResultSet result = creator.executeQuerySQL(sql);                
            while (result.next()) {
                pendingDSArrayList.add(result.getString(1));
            }            
        } catch (Exception e) {
            System.out.println("Unable to get the Pending Design Sites: " + e.getMessage());
            System.out.println("Offending SQL is " + sql);
        }
                        
        String[] pendingDS = new String[pendingDSArrayList.size()];
        for (int i = 0; i < pendingDS.length; i++) {
            pendingDS[i] = pendingDSArrayList.get(i);
        }
        return pendingDS;
    }

    @Override
    public String[] getLatestTtoSites(boolean includeDetails) {
//        int length = 9;
//        String[] pendingDS = new String[length];
//        for (int i = 0; i < length; i++) {
//            pendingDS[i] = "We Work 1 - Poultry (22) few more " + i;
//        }
//        return pendingDS;

        String sql  = null;
        if (includeDetails) {
            sql = "select concat(customer, ' - ', Site.name, ' (', count(Femto.objectId), ') ', LEFT(ttoDate,10)) AS 'fullname', LEFT(ttoDate,10) " +
                //"from Concert.Site, Concert.Femto where TTO = 'T' and ttoDate > NOW() - INTERVAL 12 MONTH and (Femto.id % 4 = 3) and " + // generate some entries for test
                //"Femto.site_id = Site.id and Site.name NOT LIKE 'Test %' group by Site.id order by ttoDate DESC LIMIT 5";
                "from Concert.Site, Concert.Femto where TTO = 'T' and ttoDate > NOW() - INTERVAL 3 MONTH and (Femto.id % 4 = 3) and " +
                "Femto.site_id = Site.id and Site.name NOT LIKE 'Test %' group by Site.id order by ttoDate";
        } else {
            sql = "select concat(customer, ' - ', Site.name) AS 'fullname', LEFT(ttoDate,10) " +
                // "from Concert.Site, Concert.Femto where TTO = 'T' and ttoDate > NOW() - INTERVAL 12 MONTH and (Femto.id % 4 = 3) and " + // generate some entries for test
                // "Femto.site_id = Site.id and Site.name NOT LIKE 'Test %' group by Site.id order by ttoDate DESC LIMIT 5";            
                "from Concert.Site, Concert.Femto where TTO = 'T' and ttoDate > NOW() - INTERVAL 3 MONTH and (Femto.id % 4 = 3) and " +
                "Femto.site_id = Site.id and Site.name NOT LIKE 'Test %' group by Site.id order by ttoDate";            
        }
                                
        // System.out.println("Executing Latest TTO SQL: " + sql);                
        ArrayList<String> latestTtoArrayList = new ArrayList(10);
        try {
            ResultSet result = creator.executeQuerySQL(sql);                
            while (result.next()) {
                latestTtoArrayList.add(result.getString(1));
            }            
        } catch (Exception e) {
            System.out.println("Unable to get the Latest TTO Sites: " + e.getMessage());
            System.out.println("Offending SQL is " + sql);
        }
                        
        String[] pendingDS = new String[latestTtoArrayList.size()];
        for (int i = 0; i < pendingDS.length; i++) {
            pendingDS[i] = latestTtoArrayList.get(i);
        }
        return pendingDS;
    
    }

    @Override
    public String[] getActivitySites() {
        String[] activitySites = new String[5];
        
        // need to have a dummy first field so that identical values for separate queries are kept seperate
        String siteCountsSql = "select '1', count(DISTINCT Site.id) from Femto, Site " +
                       "where (Femto.id % 4 = 3 and switch_id NOT LIKE '%_VF' and switch_id NOT LIKE '%_O2' and switch_id NOT LIKE '%_EE') " +
                       //"and (tto != '') " +
                       "and (tto = 'T' or tto = 'C' or tto = 'E' or tto = 'N' or tto = 'P' or tto = 'D') " + // everything except blank
                       "and Femto.site_id = Site.id and Femto.site_name NOT LIKE 'Test %' " +  // Does Andy want live Femtos? // and Femto.live";
                       "UNION " +
                       "select '2', count(DISTINCT Site.id) from Femto, Site " +
                       "where (Femto.id % 4 = 3 and switch_id NOT LIKE '%_VF' and switch_id NOT LIKE '%_O2' and switch_id NOT LIKE '%_EE') " +
                       //"and (tto != '') and (tto != 'D') " + // P + N + E + C + T                
                       "and (tto = 'T' or tto = 'C' or tto = 'E' or tto = 'N' or tto = 'P') " +
                       "and Femto.site_id = Site.id and Femto.site_name NOT LIKE 'Test %' " +  // Does Andy want live Femtos? // and Femto.live";
                       "UNION " +                
                       "select '3', count(DISTINCT Site.id) from Femto, Site  " +
                       "where (Femto.id % 4 = 3 and switch_id NOT LIKE '%_VF' and switch_id NOT LIKE '%_O2' and switch_id NOT LIKE '%_EE') " +
                       // "and (tto != '') and (tto != 'D') and (tto != 'P')" + // N + E + C + T                
                       "and (tto = 'T' or tto = 'C' or tto = 'E' or tto = 'N') " +
                       "and Femto.site_id = Site.id and Femto.site_name NOT LIKE 'Test %' " +  // Does Andy want live Femtos? // and Femto.live";
                       "UNION " +
                       "select '4', count(DISTINCT Site.id) from Femto, Site  " +
                       "where (Femto.id % 4 = 3 and switch_id NOT LIKE '%_VF' and switch_id NOT LIKE '%_O2' and switch_id NOT LIKE '%_EE') " +
                       "and (tto = 'T' or tto = 'C' or tto = 'E') " +
                       "and Femto.site_id = Site.id and Femto.site_name NOT LIKE 'Test %' ";  // Does Andy want live Femtos? // and Femto.live";
        String cellCountSql = "select count(*) from Concert.Femto where Femto.live and Femto.site_name NOT LIKE 'Test %' and Femto.id % 4 = 3";

        //System.out.println("Executing Site Counts SQL: " + siteCountsSql);                
        try {
// ZTest
//        activitySites[0] = "85";
//        activitySites[1] = "81";
//        activitySites[2] = "75";
//        activitySites[3] = "52";
//        activitySites[4] = "586";
        // commented out for testing
            ResultSet result = creator.executeQuerySQL(siteCountsSql);                
            result.next();
            activitySites[0] = result.getString(2); // was previously String(1)
            result.next();
            activitySites[1] = result.getString(2);
            result.next();
            activitySites[2] = result.getString(2);
            result.next();
            activitySites[3] = result.getString(2);
        } catch (Exception e) {
            System.out.println("Unable to get the Site Counts: " + e.getMessage());
            System.out.println("Offending SQL is " + siteCountsSql);
        }

        //System.out.println("Executing Cell Count SQL: " + cellCountSql);                
        try {
            ResultSet result = creator.executeQuerySQL(cellCountSql);                
            result.next();
            activitySites[4] = result.getString(1);
        } catch (Exception e) {
            System.out.println("Unable to get the Cell Count: " + e.getMessage());
            System.out.println("Offending SQL is " + siteCountsSql);
        }
        
        return activitySites;
    }

    /**
     * Logic as per John's email 17/4/2020 12:11
     * "Nee installs past month = any site ticked live and appropriate letter past 30 days stating obvious I guess."
     * @return 
     */
    @Override
    public String[] getNewInstalls(){
//        int length = 35;
//        String[] newInstalls = new String[length];
//        for (int i = 0; i < length; i++) {
//            newInstalls[i] = "We Work 1 - Poultry (22) - live in testing few more " + i;
//        }
        String sql = "select concat(customer, ' - ', Site.name, ' (', count(Femto.objectId), ') ', LEFT(activationDate,10)) AS 'fullname', LEFT(activationDate,10) " +
                //"from Concert.Site, Concert.Femto where TTO = 'N' and activationDate > NOW() - INTERVAL 35 DAY and (Femto.id % 4 = 3) and " +
                "from Concert.Site, Concert.Femto where TTO = 'N' and activationDate > NOW() - INTERVAL 2 MONTH and (Femto.id % 4 = 3) and " +
                "Femto.site_id = Site.id and Site.name NOT LIKE 'Test %' group by Site.id order by activationDate";
                
        // System.out.println("Executing New Installs SQL: " + sql);                
        ArrayList<String> newInstallsArrayList = new ArrayList(10);
        try {
            ResultSet result = creator.executeQuerySQL(sql);                
            while (result.next()) {
                newInstallsArrayList.add(result.getString(1));
            }            
        } catch (Exception e) {
            System.out.println("Unable to get the New Installs Sites: " + e.getMessage());
            System.out.println("Offending SQL is " + sql);
        }
                        
        String[] newInstalls = new String[newInstallsArrayList.size()];
        for (int i = 0; i < newInstalls.length; i++) {
            newInstalls[i] = newInstallsArrayList.get(i);
        }
        return newInstalls;
    }

    @Override
    public int getNoLiveSites() {
        return 81;
    }

}
