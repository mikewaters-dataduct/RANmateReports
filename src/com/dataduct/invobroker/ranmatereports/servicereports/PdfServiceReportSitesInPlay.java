package com.dataduct.invobroker.ranmatereports.servicereports;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
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
public class PdfServiceReportSitesInPlay {

    private PDFReportCreator4Mnos creator = null;
    
    public PdfServiceReportSitesInPlay(PDFReportCreator4Mnos theCreator) {
        creator = theCreator;
    }
    
    public int createSitesInPlaySlide(PDDocument openCellDocument, PDFont fontCG, PDFont fontCGBold, int globalPageCounter){
        int noPages;
        int rowsPerPage = 18;
        int ltPageCounter = 0;

        PdfServiceReportDataCollector pdfReportDataCollector = new PdfServiceReportDataCollector(creator);
        String[] upcomingSites = pdfReportDataCollector.getUpcomingSites();
        String[] pendingDASites = pdfReportDataCollector.getPendingDesignSites();
        String[] latestTtoSites = pdfReportDataCollector.getLatestTtoSites(true);

        int maxRows = Math.max(upcomingSites.length, (pendingDASites.length + latestTtoSites.length + 1)); // updated 17/4/2020 by ZAW after SOC concall with Andy and John
        //int maxRows = Math.max(upcomingSites.length, pendingDASites.length);
        
        if ( maxRows % rowsPerPage == 0 ) {
            noPages = maxRows/rowsPerPage;
        } else {
            noPages = maxRows/rowsPerPage + 1;
        }

        boolean allUCSitesPrinted = false;
        boolean allPDSitesPrinted = false;
        boolean allLTSitesPrinted = false; // previously false; // updated 17/4/2020 by ZAW after SOC concall with Andy and John
        int rawsTaken = 0;


        for (int pageCounter = 0; pageCounter < noPages;  pageCounter++){
            System.out.println("Loop for pages, counter pageCounter for upcoming Sites = " + pageCounter);
            PDPageContentStream contentStream = null;
            try {
                //Page custom size to match current OpenCell Report - landscape, widescreen 16:9
                PDPage sitesInPlayPage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
                openCellDocument.addPage(sitesInPlayPage);
                contentStream = new PDPageContentStream(openCellDocument, sitesInPlayPage);

                PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 30, PdfServiceReportConfig.openCellFontBlue, 31, 467, "Sites in Play");
                PdfServiceReportCommon.addTextToPage(contentStream, fontCGBold, 14, PdfServiceReportConfig.openCellFontGrey, 31, 430, "Upcoming sites to be design approved and commissioned");

                //Table - Upcoming Sites
                float margin1 = 15;
                // starting y position is whole page height subtracted by top and bottom margin
                float yStartNewPage1 = 170;
                float tableWidth1 = sitesInPlayPage.getMediaBox().getWidth()/2 + 70;
                boolean drawContent1 = true;
                float bottomMargin1 = 20;
                // y position is your coordinate of top left corner of the table
                float yPosition1 = 415;
                BaseTable tableUpcomingSites = new BaseTable(yPosition1, yStartNewPage1,
                bottomMargin1, tableWidth1, margin1, openCellDocument, sitesInPlayPage, true, drawContent1);

                int ucSitesRowsinThisPage;
                if ((pageCounter + 1)*rowsPerPage <= upcomingSites.length) {
                    ucSitesRowsinThisPage = rowsPerPage + pageCounter*rowsPerPage;
                } else {
                    ucSitesRowsinThisPage = upcomingSites.length;
                }

                if (!allUCSitesPrinted) {
                    //PdfServiceReportCommon.addDarkGreyHeader(tableUpcomingSites, fontCG, "Upcoming sites to be deployed");
                    PdfServiceReportCommon.addDarkGreyHeader(tableUpcomingSites, fontCG, "Approved Sites in Delivery"); // Changed following concall 17/4/2020
                    for (int i = pageCounter*rowsPerPage; i < ucSitesRowsinThisPage; i++) {
                        Row<PDPage> row = tableUpcomingSites.createRow(14);
                        Cell<PDPage> cell = row.createCell(100, "- " + upcomingSites[i]);
                        cell.setFont(fontCG);
                        cell.setFontSize(11);
                        cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
                        cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
                        cell.setTopPadding(2);
                        cell.setTopPadding(1);
                        if ( i == upcomingSites.length -1 ) {
                            allUCSitesPrinted = true;
                        }
                    }
                }
                tableUpcomingSites.draw();

                //Table - Pending Design Sites
                float margin2 = tableWidth1 + 50;
                // starting y position is whole page height subtracted by top and bottom margin
                float yStartNewPage2 = 170;
                float tableWidth2 = sitesInPlayPage.getMediaBox().getWidth()/2 - 150;
                boolean drawContent2 = true;
                float bottomMargin2 = 20;
                // y position is your coordinate of top left corner of the table
                float yPosition2 = 415;
                BaseTable tablePendingDA = new BaseTable(yPosition2, yStartNewPage2,
                bottomMargin2, tableWidth2, margin2, openCellDocument, sitesInPlayPage, true, drawContent2);

                int pdSitesRowsinThisPage;
                if ((pageCounter + 1)*rowsPerPage <= pendingDASites.length) {
                    pdSitesRowsinThisPage = rowsPerPage + pageCounter*rowsPerPage;
                } else {
                    pdSitesRowsinThisPage = pendingDASites.length;
                }
                
                // New lines inserted 26/3/2020 by Zed
                if ( pendingDASites.length == 0) {
                    allPDSitesPrinted = true;
                }

                if (!allPDSitesPrinted) {
                    PdfServiceReportCommon.addDarkGreyHeader(tablePendingDA, fontCG, "Pending design approval");
                    for (int i = pageCounter*rowsPerPage; i < pdSitesRowsinThisPage; i++) {
                        Row<PDPage> row = tablePendingDA.createRow(14);
                        Cell<PDPage> cell = row.createCell(100, "- " + pendingDASites[i]);
                        cell.setFont(fontCG);
                        cell.setFontSize(11);
                        cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
                        cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
                        cell.setTopPadding(2);
                        cell.setTopPadding(1);
                        if ( i == pendingDASites.length -1 ) {
                            allPDSitesPrinted = true;
                            ltPageCounter = 0;
                        }
                    }
                }

                if (allPDSitesPrinted && !allLTSitesPrinted) {
                    PdfServiceReportCommon.addDarkGreyHeader(tablePendingDA, fontCG, "Latest TTO Sites");
                    //System.out.println("OVDE OVDE OVDE -1 p");

                    int ltSitesRowsinThisPage;
                    //System.out.println("OVDE OVDE OVDE 0 pdSitesRowsinThisPage: " + pdSitesRowsinThisPage);
                    //System.out.println("OVDE OVDE OVDE 0 rowsPerPage: " + rowsPerPage);
                    //System.out.println("OVDE OVDE OVDE 0 pageCounter: " + pageCounter);

                    if ( ltPageCounter == 0 ) {
                        rawsTaken = pdSitesRowsinThisPage - rowsPerPage * pageCounter + 1;
                        //System.out.println("OVDE OVDE OVDE 0 rawsTaken: " + rawsTaken);
                        if ((rowsPerPage - rawsTaken) <= latestTtoSites.length) {
                            ltSitesRowsinThisPage = rowsPerPage - rawsTaken;
                            //System.out.println("OVDE OVDE OVDE 1 ltSitesRowsinThisPage: " + ltSitesRowsinThisPage);
                        } else {
                            ltSitesRowsinThisPage = latestTtoSites.length;
                            //System.out.println("OVDE OVDE OVDE 2 ltSitesRowsinThisPage: " + ltSitesRowsinThisPage);
                        }

                        //System.out.println("OVDE OVDE OVDE 51 ltPageCounter: " + ltPageCounter);
                        for (int i = ltPageCounter*rowsPerPage; i < ltSitesRowsinThisPage; i++) {
                            Row<PDPage> row = tablePendingDA.createRow(14);
                            Cell<PDPage> cell = row.createCell(100, "- " + latestTtoSites[i]);
                            //System.out.println("OVDE OVDE OVDE 6 latestTtoSites[i]: " + latestTtoSites[i] + " for i=" + i);
                            cell.setFont(fontCG);
                            cell.setFontSize(11);
                            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
                            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
                            cell.setTopPadding(2);
                            cell.setTopPadding(1);
                            if ( i == latestTtoSites.length -1 ) {
                                allLTSitesPrinted = true;
                            }
                        }
                        ltPageCounter++;

                    } else {
                         if ((ltPageCounter + 1)*rowsPerPage - rawsTaken <= latestTtoSites.length) {
                            ltSitesRowsinThisPage = rowsPerPage + ltPageCounter*rowsPerPage - rawsTaken;
                            //System.out.println("OVDE OVDE OVDE 3 ltSitesRowsinThisPage: " + ltSitesRowsinThisPage);
                        } else {
                            ltSitesRowsinThisPage = latestTtoSites.length;
                            //System.out.println("OVDE OVDE OVDE 4 ltSitesRowsinThisPage: " + ltSitesRowsinThisPage);
                        }
                        //System.out.println("OVDE OVDE OVDE 52 ltPageCounter: " + ltPageCounter);
                        for (int i = ltPageCounter*rowsPerPage - rawsTaken; i < ltSitesRowsinThisPage; i++) {
                            Row<PDPage> row = tablePendingDA.createRow(14);
                            Cell<PDPage> cell = row.createCell(100, "- " + latestTtoSites[i]);
                            //System.out.println("OVDE OVDE OVDE 6 latestTtoSites[i]: " + latestTtoSites[i] + " for i=" + i);
                            cell.setFont(fontCG);
                            cell.setFontSize(11);
                            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
                            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
                            cell.setTopPadding(2);
                            cell.setTopPadding(1);
                            if ( i == latestTtoSites.length -1 ) {
                                allLTSitesPrinted = true;
                            }
                        }
                        ltPageCounter++;
                    }
                }
                tablePendingDA.draw();

                PdfServiceReportCommon.addDatetoSlideFooter(contentStream, fontCG);
                globalPageCounter++;
                PdfServiceReportCommon.addPageNoToSlideFooter(contentStream, fontCG, globalPageCounter);

              // Make sure that the content stream is closed:
              // contentStream.close(); // moved to finally
                System.out.println("PDF Creation Page Sites in Play done");
            } catch (Exception e) {
                System.out.println("Error creating PDF Creation Page Sites in Play: " + e.getMessage());
                e.printStackTrace();
               return 0;
            } finally {
                try { 
                    contentStream.close();
                } catch (Exception ie) {
                    System.out.println("Unable to close contentStream: " + ie.getMessage());
                    ie.printStackTrace();
                }
            }
        }
        return globalPageCounter;
    }
}
