package com.dataduct.invobroker.ranmatereports.servicereports;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;
import be.quodlibet.boxable.line.LineStyle;
import com.dataduct.invobroker.ranmatereports.PDFReportCreator4Mnos;
import java.awt.Color;
import java.util.ArrayList;
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
    private PDFReportCreator4Mnos creator = null;
    
    public PdfServiceReportIncidentLog(PDFReportCreator4Mnos theCreator) {
        creator = theCreator;
    }

    public int createIncidentLogSlide(PDDocument openCellDocument, PDFont fontCG, PDFont fontCGBold, int globalPageCounter){
        int noPages;
        int rowsPerPage;
        String title2 = "";
        String title3 = "Sites with recorded incidents";
        
        rowsPerPage = 14; 
        
        ArrayList<String[]> siteIncidents = creator.incidents;
        if (siteIncidents.size() == 0) {
            siteIncidents.add(new String[]{"", "(There are 0 site incidents in the log)"});
        }
        
        if ( siteIncidents.size() % rowsPerPage == 0 ) {
            noPages = siteIncidents.size()/rowsPerPage;
        } else {
            noPages = siteIncidents.size()/rowsPerPage + 1;
        }
        
        for (int pageCounter = 0; pageCounter < noPages;  pageCounter++){
            try {
                //Slide custom size to match current OpenCell Report - landscape, widescreen 16:9
                PDPage reportIncidentLogSlide = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
                openCellDocument.addPage(reportIncidentLogSlide);
        
                PDPageContentStream contentStream;
                contentStream = new PDPageContentStream(openCellDocument, reportIncidentLogSlide);
                PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 30, PdfServiceReportConfig.openCellFontBlue, 31, 467, "Incident Log");                         
                PdfServiceReportCommon.addTextToPage(contentStream, fontCGBold, 14, PdfServiceReportConfig.openCellFontGrey, 23, 430, title2);                         
                        
                //Grey header above the main table
                
                //Table
                float margin = 15;
                // starting y position is whole page height subtracted by top and bottom margin
                float yStartNewPage = 170;
                float tableWidth = reportIncidentLogSlide.getMediaBox().getWidth() - 30;
                boolean drawContent = true;
                float bottomMargin = 20;
                // y position is your coordinate of top left corner of the table
                float yPosition = 425;
                BaseTable table = new BaseTable(yPosition, yStartNewPage,
                bottomMargin, tableWidth, margin, openCellDocument, reportIncidentLogSlide, true, drawContent);
                
                PdfServiceReportCommon.addDarkGreyHeader(table, fontCG, title3);
                
                Row<PDPage> headerRow;
                Cell<PDPage> cell;
                headerRow = table.createRow(1);
                cell = headerRow.createCell(100, "");
                // border style
                cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 1));                    
                
                Row<PDPage> headerRow2 = table.createRow(12);
                addHeader2ToTable(headerRow2, cell, fontCG);
                
                int rowsinThisPage;
                if ((pageCounter + 1)*rowsPerPage <= siteIncidents.size()) {
                    rowsinThisPage = rowsPerPage + pageCounter*rowsPerPage;
                } else {
                    rowsinThisPage = siteIncidents.size();
                }
                            
                for (int i = pageCounter*rowsPerPage; i < rowsinThisPage; i++) {      
                    Row<PDPage> row = table.createRow(11);
                    Color rowColor;
                    if (i % 2 == 0 ){
                        rowColor = PdfServiceReportConfig.darkGrey;
                    } else {
                        rowColor = PdfServiceReportConfig.lightGrey;    
                    }
                    addRowToTable(row, cell, siteIncidents.get(i), fontCG, rowColor);
                }
                table.draw();
           
                PdfServiceReportCommon.addDatetoSlideFooter(contentStream, fontCG);
                PdfServiceReportCommon.addPageNoToSlideFooter(contentStream, fontCG, globalPageCounter + pageCounter + 1);
           
                // Make sure that the content stream is closed:
                contentStream.close();
                System.out.println("PDF Creation Incident Log Page done");            
            } catch (Exception e){
                System.out.println("Error creating PDF Creation Incident Log Page: " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }
        return noPages + globalPageCounter;
    }  
    
    private boolean addHeader2ToTable(Row<PDPage> row, Cell<PDPage> cell, PDFont fontCG) {
        int nonTtoOffset;
        nonTtoOffset = 0;
        try {
            cell = row.createCell(25, "Site Name");
            cell.setFontBold(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(Color.WHITE);
            cell.setFillColor(PdfServiceReportConfig.navyHeader);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 3));
            
            cell = row.createCell(75, "Incident");
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
    
    private boolean addRowToTable(Row<PDPage> row, Cell<PDPage> cell, String[] rowContent, PDFont fontCG, Color rowColor) {
        int nonTtoOffset;
        nonTtoOffset = 0;
        try {

            cell = row.createCell(25, rowContent[0]);
            cell.setFont(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setFillColor(rowColor);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.LEFT);
            cell.setBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setTopPadding(2);
            cell.setBottomPadding(1);
             
            cell = row.createCell(75, rowContent[1]);
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
                           
            return true;
        } catch (Exception e){
           return false; 
        } 

    }   
}
