package com.dataduct.invobroker.ranmatereports.servicereports;

import java.io.IOException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author zorana
 */
public class PdfServiceReportTestMain {

    public static void main(String[] args) throws IOException {
        String coveringPeriod = "January 2020";//to be removed
        //PdfServiceReportGenerator reportGenerator = new PdfServiceReportGenerator(coveringPeriod);
        //reportGenerator.createReport();
        System.out.println("PDF report creation for " + coveringPeriod + " is done.");
   }
}

