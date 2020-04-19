package com.dataduct.invobroker.ranmatereports.servicereports;

import java.awt.Color;
import java.io.File;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zorana
 */
public class PdfServiceReportConfig {
    static final Color openCellFontBlue = new Color (51, 71, 109);
    static final Color openCellFontGrey = new Color (89,89, 89);
    static final Color darkGreyHeader = new Color (127,134, 141);
    static final Color navyHeader = new Color (51, 71, 109);
    static final Color lightGrey = new Color (232, 233, 235);
    static final Color darkGrey = new Color (205, 207, 212);
    static final Color statusRed = new Color(255, 0, 0);
    static final Color statusGreen = new Color (125, 184, 55);
    static final Color statusOrange = new Color (255, 192, 0);
    static final String REPORT_DIR = "/data1/reports/"; // to be replaced
    static final String GRAPHS_DIR = "/data1/reports/tmp/graphs/"; // to be replaced
    // static final String mno = "Three"; / /now passed to the constructor of PdfServiceReportGeneration
    // static final String reportNameFixed = "StrattoOpenCell Service Report " + mno + " "; version when above was set here
    static final String reportNameFixed = "StrattoOpenCell Service Report ";
    static final String templateName = "OpenCellPageTemplate.pdf";
    static final String cellAvailabilityTarget = ">= 99%";
    static final String rrcSRTarget = ">=99.2%";
    static final String csSRTarget = ">=99%";
    static final String csDCRTarget = "<=5%";
    static final String psSRTarget = ">=98%";
    static final String psDSRTarget = "<=6.5%";
    static final String interFapCSTarget = ">=93%";
    static final String interFapPSTarget = ">=93%";
}
