package com.dataduct.invobroker.ranmatereports.servicereports;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;
import be.quodlibet.boxable.line.LineStyle;
import com.dataduct.invobroker.ranmatereports.PDFReportCreator4Mnos;
import com.dataduct.invobroker.ranmatereports.servicereports.PdfServiceReportCommon;
import com.dataduct.invobroker.ranmatereports.servicereports.PdfServiceReportConfig;
import com.dataduct.invobroker.ranmatereports.servicereports.PdfServiceReportDataCollector;
import com.dataduct.invobroker.ranmatereports.servicereports.SiteStatus;
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
public class PdfServiceReportSitesStatus {
    boolean isTto;
    private PDFReportCreator4Mnos creator = null;
    
    public PdfServiceReportSitesStatus(boolean isTto, PDFReportCreator4Mnos theCreator) {
        creator = theCreator;
        this.isTto = isTto;
    }
    
    public int createSitesStatusSlide(PDDocument openCellDocument, PDFont fontCG, PDFont fontCGBold, int globalPageCounter){
        int noPages;
        int rowsPerPage;
        String title1;
        String title2;
        String title3;
        
        if (isTto) {
            //rowsPerPage = 7;
            title1 = "TTO";
            title2 = "Failing TTO/consistent offending sites this month";
            title3 = "Failing sites last month with actions/past 4 weeks";
        } else {
            //rowsPerPage = 11;
            title1 = "Non-TTO";
            title2 = "Actions for Non-TTO sites";
            title3 = "Failing Non-TTO sites";
        }
        rowsPerPage = 14;       // was previously 10
        
        PdfServiceReportDataCollector pdfReportDataCollector = new PdfServiceReportDataCollector(creator);
        SiteStatus[] sitesStatus = pdfReportDataCollector.getSiteStatus( isTto);
        
        if ( sitesStatus.length % rowsPerPage == 0 ) {
            noPages = sitesStatus.length/rowsPerPage;
        } else {
            noPages = sitesStatus.length/rowsPerPage + 1;
        }
        
        for (int pageCounter = 0; pageCounter < noPages;  pageCounter++){
            System.out.println("Loop for pages, counter pageCounter for slide 4 = " + pageCounter);
            try {
                //Slide custom size to match current OpenCell Report - landscape, widescreen 16:9
                PDPage reportSitesStatusSlide = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
                openCellDocument.addPage(reportSitesStatusSlide);
        
                PDPageContentStream contentStream;
                contentStream = new PDPageContentStream(openCellDocument, reportSitesStatusSlide);
                PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 30, PdfServiceReportConfig.openCellFontBlue, 31, 467, title1 + " Site Performance");                         
                PdfServiceReportCommon.addTextToPage(contentStream, fontCGBold, 14, PdfServiceReportConfig.openCellFontGrey, 23, 430, title2);                         
                        
                //Grey header above the main table
                
                //Table
                float margin = 15;
                // starting y position is whole page height subtracted by top and bottom margin
                float yStartNewPage = 170;
                float tableWidth = reportSitesStatusSlide.getMediaBox().getWidth() - 30;
                boolean drawContent = true;
                float bottomMargin = 20;
                // y position is your coordinate of top left corner of the table
                float yPosition = 425;
                BaseTable table = new BaseTable(yPosition, yStartNewPage,
                bottomMargin, tableWidth, margin, openCellDocument, reportSitesStatusSlide, true, drawContent);
                
                PdfServiceReportCommon.addDarkGreyHeader(table, fontCG, title3);
                
                Row<PDPage> headerRow;
                Cell<PDPage> cell;
                if (isTto) {
                    // subtitle
                    headerRow = table.createRow(15);
                    cell = headerRow.createCell(100, "Main offending sites last month");
                    cell.setFont(fontCG);
                    cell.setFontSize(14);
                    cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
                    cell.setValign(VerticalAlignment.MIDDLE);
                    cell.setAlign(HorizontalAlignment.LEFT);
                    // border style
                    cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 1));
                } else {
                    headerRow = table.createRow(1);
                    cell = headerRow.createCell(100, "");
                    // border style
                    cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 1));                    
                }
                
                Row<PDPage> headerRow2 = table.createRow(12);
                addHeader2ToTable(headerRow2, cell, fontCG);
                
                int rowsinThisPage;
                if ((pageCounter + 1)*rowsPerPage <= sitesStatus.length) {
                    rowsinThisPage = rowsPerPage + pageCounter*rowsPerPage;
                } else {
                    rowsinThisPage = sitesStatus.length;
                }
                            
                for (int i = pageCounter*rowsPerPage; i < rowsinThisPage; i++) {      
                    Row<PDPage> row = table.createRow(11);
                    Color rowColor;
                    if (i % 2 == 0 ){
                        rowColor = PdfServiceReportConfig.darkGrey;
                    } else {
                        rowColor = PdfServiceReportConfig.lightGrey;    
                    }
                    addRowToTable(row, cell, sitesStatus[i], fontCG, rowColor);
                }
                table.draw();
           
                PdfServiceReportCommon.addDatetoSlideFooter(contentStream, fontCG);
                PdfServiceReportCommon.addPageNoToSlideFooter(contentStream, fontCG, globalPageCounter + pageCounter + 1);
           
                // Make sure that the content stream is closed:
                contentStream.close();
                System.out.println("PDF Creation Page 3 done");            
            } catch (Exception e){
                System.out.println("Error creating PDF Creation Page 3: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }
        return noPages + globalPageCounter;
    }  
    
    private boolean addHeader2ToTable(Row<PDPage> row, Cell<PDPage> cell, PDFont fontCG) {
        int nonTtoOffset;
        /*if (isTto) {
            //nonTtoOffset = 0; //remove Next Actions from TTO slide
            nonTtoOffset = 25;
        } else {
            nonTtoOffset = 25;
        }*/
        nonTtoOffset = 0;
        try {
            /*if (isTto) {
                //cell = row.createCell(10, "Site Name"); //remove Next Actions from TTO slide
                cell = row.createCell(20, "Site Name");
            } else {
                cell = row.createCell(20, "Site Name");
            }*/
            cell = row.createCell(17, "Site Name");
            cell.setFontBold(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(Color.WHITE);
            cell.setFillColor(PdfServiceReportConfig.navyHeader);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 3));
            
            cell = row.createCell(5, "Overall");
            cell.setFontBold(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(Color.WHITE);
            cell.setFillColor(PdfServiceReportConfig.navyHeader);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 3));

            cell = row.createCell(32 + nonTtoOffset, "Comments"); // was 51 (%) previously 
            cell.setFontBold(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(Color.WHITE);
            cell.setFillColor(PdfServiceReportConfig.navyHeader);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 3));
            
            //remove Next Actions from TTO slide
            /*if (isTto){
                cell = row.createCell(35, "Next Action");
                cell.setFontBold(fontCG);
                cell.setFontSize(10);
                cell.setTextColor(Color.WHITE);
                cell.setFillColor(PdfServiceReportConfig.navyHeader);
                cell.setValign(VerticalAlignment.MIDDLE);
                cell.setAlign(HorizontalAlignment.CENTER);
                cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));
                cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 3));
            }*/
            cell = row.createCell(46, "Next Action");
            cell.setFontBold(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(Color.WHITE);
            cell.setFillColor(PdfServiceReportConfig.navyHeader);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 3));
      
        return true;
        } catch (Exception e){
           return false; 
        } 

    }
    
    private boolean addRowToTable(Row<PDPage> row, Cell<PDPage> cell, SiteStatus rowContent, PDFont fontCG, Color rowColor) {
        int nonTtoOffset;
        /*if (isTto) {
            //nonTtoOffset = 0; //remove Next Actions from TTO slide
            nonTtoOffset = 25;
        } else {
            nonTtoOffset = 25;
        }*/
        nonTtoOffset = 0;
        try {

            /*if (isTto) {
                //cell = row.createCell(10, rowContent.siteName.replaceFirst(" ", "<br>")); //remove Next Actions from TTO slide
                cell = row.createCell(20, rowContent.siteName);
            } else {
                cell = row.createCell(20, rowContent.siteName);
            }*/
            cell = row.createCell(17, rowContent.siteName);
            cell.setFont(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setFillColor(rowColor);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.LEFT);
            cell.setBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setTopPadding(2);
            cell.setBottomPadding(1);
            
            cell = row.createCell(5, "");
            cell.setFont(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setFillColor(rowContent.status);
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setTopPadding(2);
            cell.setBottomPadding(1);
 
            cell = row.createCell(32 + nonTtoOffset, rowContent.comments);
            cell.setFont(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setFillColor(rowColor);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.LEFT);
            cell.setTopPadding(2);
            cell.setBottomPadding(1);
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));
            
            //remove Next Actions from TTO slide
            /*if (isTto) {
                cell = row.createCell(35, rowContent.nextStep);
                cell.setFont(fontCG);
                cell.setFontSize(10);
                cell.setFillColor(rowColor);
                cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
                cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 1));
                cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));
                cell.setTopPadding(2);
                cell.setBottomPadding(1);
            }*/
            
            cell = row.createCell(46, rowContent.nextStep);
            cell.setFont(fontCG);
            cell.setFontSize(10);
            cell.setFillColor(rowColor);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setTopPadding(2);
            cell.setBottomPadding(1);

   
        return true;
        } catch (Exception e){
           return false; 
        } 

    }   
}
