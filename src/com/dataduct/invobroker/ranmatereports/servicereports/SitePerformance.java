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
public class SitePerformance {
    public String siteReference; //8 characters
    public double cellAvailability;
    public double rrcSR;
    public double csSR;
    public double csDCR;
    public double psSR;
    public double psDSR;
    public double interFapCS;
    public double interFapPS;
    public String interFreqHandouts;
    public String interFreqHandover;
    public String csAttempts;
    public String siteName;
    public int cells;
    public String concessionReason;

    public SitePerformance() {

        this.siteReference = "1ES2D3ST";
        this.cellAvailability = 99.98;
        this.rrcSR = 99.39;
        this.csSR = 100;
        this.csDCR = 6.24;
        this.psSR =  99.82;
        this.psDSR = 22.82;
        this.interFapCS = 94.42;
        this.interFapPS = 98.21;
        this.interFreqHandouts = "100";
        this.interFreqHandover = "81.81";
        this.csAttempts = "1916";
        this.siteName = "Derby Conference Centre55";
        this.cells = 18;
        this.concessionReason = "Interfap CS/PS, east side of floor 7 is not owned by AeCOm - no solution to handover to in lift area.Interfap CS/PS, east side of floor 7 is not owned by AeCOm - no solution to handover to in lift area and a bit";

    }

    public SitePerformance( String siteReference, double callAvailability, double rrcSR, double csSR, double csDCR, double psSR, double psDSR, double interFapCS,
                double interFapPS, String interFreqHandouts, String interFreqHandover, String csAttempts, String site, int cells, String concessionReason) {

        this.siteReference = siteReference;
        this.cellAvailability = callAvailability;
        this.rrcSR = rrcSR;
        this.csSR = csSR;
        this.csDCR = csDCR;
        this.psSR =  psSR;
        this.psDSR = psDSR;
        this.interFapCS = interFapCS;
        this.interFapPS = interFapPS;
        this.interFreqHandouts = interFreqHandouts;
        this.interFreqHandover = interFreqHandover;
        this.csAttempts = csAttempts;
        this.siteName = site;
        this.cells = cells;
        this.concessionReason = concessionReason;

    }

}


