package com.dataduct.invobroker.ranmatereports.servicereports;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zorana
 */
interface PdfServiceReportDataCollectorInterface {
    public String[] getNwPerformance( boolean isTto);
    public SitePerformance[] getSitePerformance( boolean isTto);
    public SitePerformance getTotalSitePerformance( boolean isTto);
    public SiteStatus[] getSiteStatus(boolean isTto);
    public String[] getUpcomingSites();
    public String[] getPendingDesignSites();
    public String[] getLatestTtoSites(boolean includeDetails);
    public String[] getActivitySites();
    public String[] getNewInstalls();
    public int getNoLiveSites();
}
