package com.dataduct.invobroker.ranmatereports.servicereports;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;
import be.quodlibet.boxable.line.LineStyle;
import com.dataduct.invobroker.ranmatereports.PDFReportCreator4Mnos;
import java.awt.Color;
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
public class PdfServiceReportSitesPerformance {
    private boolean isTto;
    private PDFReportCreator4Mnos creator = null;
    
    public PdfServiceReportSitesPerformance(boolean isTto, PDFReportCreator4Mnos theCreator) {
        creator = theCreator;
        this.isTto = isTto;
    }

    public int createSitesPerformanceSlide(PDDocument openCellDocument, PDFont fontCG, String title, int totalPageCounter){
        int noPages;
        int rowsPerPage;
        if (isTto) {
            rowsPerPage = 9;
        } else {
            rowsPerPage = 17;
        }

        PdfServiceReportDataCollector pdfReportDataCollector = new PdfServiceReportDataCollector(creator);
        SitePerformance[] sitesPerformance = pdfReportDataCollector.getSitePerformance( isTto);

        if ( sitesPerformance.length % rowsPerPage == 0 ) {
            noPages = sitesPerformance.length/rowsPerPage;
        } else {
            noPages = sitesPerformance.length/rowsPerPage + 1;
        }
        System.out.println("Site Performance for " + title + " in total: " + noPages);

        for (int pageCounter = 0; pageCounter < noPages;  pageCounter++){
            try {
                //Page custom size to match current OpenCell Report - landscape, widescreen 16:9
                PDPage reportSitePerformancePage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
                openCellDocument.addPage(reportSitePerformancePage);

                PDPageContentStream contentStream;
                contentStream = new PDPageContentStream(openCellDocument, reportSitePerformancePage);
                PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 30, PdfServiceReportConfig.openCellFontBlue, 31, 467, title + " Network Performance");

                //Table
                //max number of characters (siteReference - 8, siteName - 24, concessionReason - 200)
                float margin = 15;
                // starting y position is whole page height subtracted by top and bottom margin
                float yStartNewPage = 170;
                float tableWidth = reportSitePerformancePage.getMediaBox().getWidth() - 30;
                boolean drawContent = true;
                float bottomMargin = 20;
                // y position is your coordinate of top left corner of the table
                float yPosition = 420;
                BaseTable table = new BaseTable(yPosition, yStartNewPage,
                bottomMargin, tableWidth, margin, openCellDocument, reportSitePerformancePage, true, drawContent);

                Row<PDPage> headerRow = table.createRow(12);
                //first cell in the header is empty
                Cell<PDPage> cell = headerRow.createCell(6, "");
                cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 1));
                addTableHeader(headerRow, cell, fontCG, isTto);

                int rowsinThisPage;
                if ((pageCounter + 1)*rowsPerPage <= sitesPerformance.length) {
                    rowsinThisPage = rowsPerPage + pageCounter*rowsPerPage;
                } else {
                    rowsinThisPage = sitesPerformance.length;
                }

                int nonTtoOffset;
                if (isTto) {
                    nonTtoOffset = 0;
                } else {
                    nonTtoOffset = 2;
                }

                for (int i = pageCounter*rowsPerPage; i < rowsinThisPage; i++) {
                    Row<PDPage> row = table.createRow(16);
                    //first cell
                    cell = row.createCell(6, sitesPerformance[i].siteReference);
                    cell.setFontBold(fontCG);
                    cell.setFontSize(9);
                    cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
                    cell.setFillColor(new Color (255, 255, 153));
                    cell.setValign(VerticalAlignment.MIDDLE);
                    cell.setAlign(HorizontalAlignment.CENTER);
                    cell.setBorderStyle(new LineStyle(new Color(204, 204, 255), 1));
                    cell.setLeftPadding(0);
                    cell.setRightPadding(0);

                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(sitesPerformance[i].cellAvailability), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.cellAvailabilityTarget, Double.toString(sitesPerformance[i].cellAvailability)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(sitesPerformance[i].rrcSR), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.rrcSRTarget, Double.toString(sitesPerformance[i].rrcSR)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(sitesPerformance[i].csSR), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.csSRTarget, Double.toString(sitesPerformance[i].csSR)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(sitesPerformance[i].csDCR), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.csDCRTarget, Double.toString(sitesPerformance[i].csDCR)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(sitesPerformance[i].psSR), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.psSRTarget, Double.toString(sitesPerformance[i].psSR)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(sitesPerformance[i].psDSR), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.psDSRTarget, Double.toString(sitesPerformance[i].psDSR)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(sitesPerformance[i].interFapCS), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.interFapCSTarget, Double.toString(sitesPerformance[i].interFapCS)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(sitesPerformance[i].interFapPS), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.interFapPSTarget, Double.toString(sitesPerformance[i].interFapPS)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, sitesPerformance[i].interFreqHandouts, fontCG, Color.WHITE, false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, sitesPerformance[i].interFreqHandover, fontCG, Color.WHITE, false);
                    addCellToTableRow(row, cell, 4 + nonTtoOffset, sitesPerformance[i].csAttempts, fontCG, Color.WHITE, false);
                    addCellToTableRow(row, cell, 13, sitesPerformance[i].siteName, fontCG, PdfServiceReportConfig.lightGrey, true);
                    addCellToTableRow(row, cell, 3 + nonTtoOffset, Integer.toString(sitesPerformance[i].cells), fontCG, PdfServiceReportConfig.lightGrey, false);
                    if (isTto){
                        addCellToTableRow(row, cell, 24, sitesPerformance[i].concessionReason, fontCG, new Color(204, 233, 173), true);
                    }
                }

                if ((pageCounter == noPages -1) && !isTto) {
                    SitePerformance totalPerformance = pdfReportDataCollector.getTotalSitePerformance(isTto);
                    Row<PDPage> row = table.createRow(16);
                    //first cell
                    cell = row.createCell(100, "");
                    cell.setBorderStyle(new LineStyle(new Color(204, 204, 255), 1));

                    row = table.createRow(16);
                    //first cell
                    cell = row.createCell(6, "Totals");
                    cell.setFontBold(fontCG);
                    cell.setFontSize(9);
                    cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
                    cell.setFillColor(new Color (255, 255, 153));
                    cell.setValign(VerticalAlignment.MIDDLE);
                    cell.setAlign(HorizontalAlignment.CENTER);
                    cell.setBorderStyle(new LineStyle(new Color(204, 204, 255), 1));
                    cell.setLeftPadding(0);
                    cell.setRightPadding(0);

                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(totalPerformance.cellAvailability), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.cellAvailabilityTarget, Double.toString(totalPerformance.cellAvailability)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(totalPerformance.rrcSR), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.rrcSRTarget, Double.toString(totalPerformance.rrcSR)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(totalPerformance.csSR), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.csSRTarget, Double.toString(totalPerformance.csSR)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(totalPerformance.csDCR), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.csDCRTarget, Double.toString(totalPerformance.csDCR)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(totalPerformance.psSR), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.psSRTarget, Double.toString(totalPerformance.psSR)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(totalPerformance.psDSR), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.psDSRTarget, Double.toString(totalPerformance.psDSR)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(totalPerformance.interFapCS), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.interFapCSTarget, Double.toString(totalPerformance.interFapCS)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, Double.toString(totalPerformance.interFapPS), fontCG, PdfServiceReportCommon.getRedOrGreen(PdfServiceReportConfig.interFapPSTarget, Double.toString(totalPerformance.interFapPS)), false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, totalPerformance.interFreqHandouts, fontCG, Color.WHITE, false);
                    addCellToTableRow(row, cell, 5 + nonTtoOffset, totalPerformance.interFreqHandover, fontCG, Color.WHITE, false);
                    addCellToTableRow(row, cell, 4 + nonTtoOffset, totalPerformance.csAttempts, fontCG, Color.WHITE, false);
                    addCellToTableRow(row, cell, 13, "Sites - " + totalPerformance.siteName, fontCG, PdfServiceReportConfig.lightGrey, true);
                    addCellToTableRow(row, cell, 3 + nonTtoOffset, Integer.toString(totalPerformance.cells), fontCG, PdfServiceReportConfig.lightGrey, false);
                }
                table.draw();

                PdfServiceReportCommon.addDatetoSlideFooter(contentStream, fontCG);
                PdfServiceReportCommon.addPageNoToSlideFooter(contentStream, fontCG, totalPageCounter + pageCounter + 1);

                // Make sure that the content stream is closed:
                contentStream.close();
                System.out.println("PDF Creation Page " + totalPageCounter + pageCounter + 1 + " done");
            } catch (Exception e){
                System.out.println("Error creating PDF Creation Page " + totalPageCounter + pageCounter + 1 + ": " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }
        return (totalPageCounter + noPages);
    }
    private boolean addCellToHeaderRow(Row<PDPage> headerRow, Cell<PDPage> cell, int cellWidth, String text, PDFont fontCG){
        try {
            cell = headerRow.createCell(cellWidth, text);
            cell.setFontBold(fontCG);
            cell.setFontSize(9);
            cell.setTextColor(Color.WHITE);
            cell.setFillColor(PdfServiceReportConfig.navyHeader);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setBottomBorderStyle(new LineStyle(new Color(204, 204, 255), 1));
            cell.setTopBorderStyle(new LineStyle(new Color(204, 204, 255), 1));
            cell.setRightBorderStyle(new LineStyle(new Color(204, 204, 255), 1));
            cell.setLeftPadding(0);
            cell.setRightPadding(0);
            return true;
        } catch (Exception e){
           return false;
        }
    }

    private boolean addCellToTableRow(Row<PDPage> row, Cell<PDPage> cell, int cellWidth, String text, PDFont fontCG, Color cellColor, boolean isLeftJustified){
        try {
            cell = row.createCell(cellWidth, text);
            cell.setFontBold(fontCG);
            cell.setFontSize(9);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setFillColor(cellColor);
            cell.setValign(VerticalAlignment.MIDDLE);
            if ( isLeftJustified) {
                cell.setAlign(HorizontalAlignment.LEFT);
            } else {
                cell.setAlign(HorizontalAlignment.CENTER);
            }
            cell.setBottomBorderStyle(new LineStyle(new Color(204, 204, 255), 1));
            cell.setRightBorderStyle(new LineStyle(new Color(204, 204, 255), 1));
            cell.setTopBorderStyle(new LineStyle(new Color(204, 204, 255), 1));
            cell.setLeftPadding(0);
            cell.setRightPadding(0);
            cell.setBottomPadding(1);
            cell.setTopPadding(2);
            return true;
        } catch (Exception e){
           return false;
        }
    }

    private boolean addTableHeader(Row<PDPage> headerRow, Cell<PDPage> cell, PDFont fontCG, boolean isTto) {
        int nonTtoOffset;
        if (isTto) {
            nonTtoOffset = 0;
        } else {
            nonTtoOffset = 2;
        }

        addCellToHeaderRow(headerRow, cell, 5 + nonTtoOffset, "Cell<br>Availability<br>" + PdfServiceReportConfig.cellAvailabilityTarget, fontCG);
        addCellToHeaderRow(headerRow, cell, 5 + nonTtoOffset, "RRC SR% " + PdfServiceReportConfig.rrcSRTarget , fontCG);
        addCellToHeaderRow(headerRow, cell, 5 + nonTtoOffset, "CSSR% " + PdfServiceReportConfig.csSRTarget, fontCG);
        addCellToHeaderRow(headerRow, cell, 5 + nonTtoOffset, "CSDCR% " + PdfServiceReportConfig.csDCRTarget, fontCG);
        addCellToHeaderRow(headerRow, cell, 5 + nonTtoOffset, "PSSR% " + PdfServiceReportConfig.psSRTarget, fontCG);
        addCellToHeaderRow(headerRow, cell, 5 + nonTtoOffset, "PS DSR% " + PdfServiceReportConfig.psDSRTarget, fontCG);
        addCellToHeaderRow(headerRow, cell, 5 + nonTtoOffset, "Inter FAP-<br>CS%<br>" + PdfServiceReportConfig.interFapCSTarget, fontCG);
        addCellToHeaderRow(headerRow, cell, 5 + nonTtoOffset, "Inter FAP-<br>PS%<br>" + PdfServiceReportConfig.interFapPSTarget, fontCG);
        addCellToHeaderRow(headerRow, cell, 5 + nonTtoOffset, "Inter-Freq Handouts", fontCG);
        addCellToHeaderRow(headerRow, cell, 5 + nonTtoOffset, "Inter-Freq Handover", fontCG);
        addCellToHeaderRow(headerRow, cell, 4 + nonTtoOffset, "CS Attempts", fontCG);
        addCellToHeaderRow(headerRow, cell, 13, "Site", fontCG);
        addCellToHeaderRow(headerRow, cell, 3 + nonTtoOffset, "Cells", fontCG);
        if (isTto) {
            addCellToHeaderRow(headerRow, cell, 24, "Reason for Concession", fontCG);
        }
        return true;
    }
}
