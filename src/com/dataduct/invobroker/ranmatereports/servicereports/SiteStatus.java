package com.dataduct.invobroker.ranmatereports.servicereports;

import java.awt.Color;

/**
 *
 * @author zorana
 */
public class SiteStatus {
    public String siteName;
    public Color status;
    public String comments;
    public String nextStep;

    public SiteStatus() {
        this.siteName = "Regus Lamabrd Street 12345";
        this.status = new Color(125, 184, 55);
        this.nextStep = "Deep dive on weekly performance. Adding more characters to get to the max 100 characters. Eleven more chars";
        this.comments = "PS/DCR and CS/DCR fluctuating weekly/bi weekly, another cell may need to be added near entrance. Backhaul scheduled to be upgraded by client. Adding more characters to get to the max 200 characters. Eleven more chars";
    }

    public SiteStatus(String siteName, Color status, String comments, String nextStep) {
        this.siteName = siteName;
        this.status = status;
        this.nextStep = nextStep;
        this.comments = comments;
    }
}