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
public class PdfServiceReportTitleSlide {
    private final String coveringPeriod;
    private final String mno;

    PdfServiceReportTitleSlide(String mno, String coveringPeriod){
        this.coveringPeriod = coveringPeriod;
        this.mno = mno;
    }

    public int createTitleSlide(PDDocument openCellDocument, PDFont fontCG, int pageCounter){
        try {
            //Page 1, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage titlePage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(titlePage);

            PDPageContentStream contentStream = new PDPageContentStream(openCellDocument, titlePage);
            PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 48, PdfServiceReportConfig.openCellFontGrey, 100, 380, "StrattoOpencell");
            PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 48, PdfServiceReportConfig.openCellFontGrey, 100, 320, mno + " Service Report");
            PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 36, PdfServiceReportConfig.openCellFontGrey, 100, 180, "Covering period: " + coveringPeriod);
            PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 20, PdfServiceReportConfig.openCellFontGrey, 100, 70, "Report by: RANmate");

            PdfServiceReportCommon.addDatetoSlideFooter(contentStream, fontCG);
            PdfServiceReportCommon.addPageNoToSlideFooter(contentStream, fontCG, 1);

            // Make sure that the content stream is closed:
            contentStream.close();
            pageCounter++;
            System.out.println("PDF Creation Page " + pageCounter + " done");
            return pageCounter;
        } catch (Exception e){
            System.out.println("Error creating PDF Creation Page " + pageCounter + ": " + e.getMessage());
            e.printStackTrace();
           return 0;
        }

    }
}
