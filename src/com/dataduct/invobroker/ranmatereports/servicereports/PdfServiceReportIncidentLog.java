package com.dataduct.invobroker.ranmatereports.servicereports;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zorana
 */
public class PdfServiceReportIncidentLog {

    public int createIncidentLogSlide(PDDocument openCellDocument, PDFont fontCG, int pageCounter){
        try {
            //Page custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage nwPerformancePage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(nwPerformancePage);
            PDPageContentStream contentStream;
            contentStream = new PDPageContentStream(openCellDocument, nwPerformancePage);

            PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 30, PdfServiceReportConfig.openCellFontBlue, 31, 467, "Incident Log");
            PdfServiceReportCommon.addDatetoSlideFooter(contentStream, fontCG);
            pageCounter++;
            PdfServiceReportCommon.addPageNoToSlideFooter(contentStream, fontCG, pageCounter);

          // Make sure that the content stream is closed:
            contentStream.close();
            System.out.println("PDF Creation Page Incident Log done");
            return pageCounter;
        } catch (Exception e){
            System.out.println("Error creating PDF Creation Page Incident Log: " + e.getMessage());
            e.printStackTrace();
           return 0;
        } 

    }
}
