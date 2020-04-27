package com.dataduct.invobroker.ranmatereports.servicereports;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.line.LineStyle;
import com.dataduct.invobroker.ranmatereports.PDFReportCreator4Mnos;
import java.awt.Color;
import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zorana
 */
public class PdfServiceReportOverallStats {

    private PDFReportCreator4Mnos creator = null;
    
    public PdfServiceReportOverallStats(PDFReportCreator4Mnos theCreator) {
        creator = theCreator;
    }
    
    public int createOverallStatsSlide(PDDocument openCellDocument, PDFont fontCG, PDFont fontCGBold, int globalPageCounter, String coveringPeriod){

        PdfServiceReportDataCollector pdfReportDataCollector = new PdfServiceReportDataCollector(creator);
        // Problematic method call
        String[] activitySites = pdfReportDataCollector.getActivitySites();
        String[] latestTtoSites = pdfReportDataCollector.getLatestTtoSites(false);
        String[] newInstalls = pdfReportDataCollector.getNewInstalls();


        try {
            System.out.println("First page OverallStats");
            //Page custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage overallStatsPage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(overallStatsPage);
            PDPageContentStream contentStream;
            contentStream = new PDPageContentStream(openCellDocument, overallStatsPage);

            PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 30, PdfServiceReportConfig.openCellFontBlue, 31, 467, "Overall Deployment Statistics");
            PdfServiceReportCommon.addTextToPage(contentStream, fontCGBold, 14, PdfServiceReportConfig.openCellFontGrey, 31, 430, "Statistics as of end " + coveringPeriod);
            // Andy suggested removing this on 20/4/2020 - replaced with a single space to have minimal effect on slide layout
            //PdfServiceReportCommon.addTextToPage(contentStream, fontCGBold, 14, PdfServiceReportConfig.openCellFontGrey, 31, 410, "Total number of live sites: " + pdfReportDataCollector.getNoLiveSites() + " (More to follow once handed over from delivery)");
            PdfServiceReportCommon.addTextToPage(contentStream, fontCGBold, 14, PdfServiceReportConfig.openCellFontGrey, 31, 410, " ");

            //Table
            float margin = 15;
            // starting y position is whole page height subtracted by top and bottom margin
            float yStartNewPage = 170;
            float tableWidth = overallStatsPage.getMediaBox().getWidth()/2 + 150;
            boolean drawContent = true;
            float bottomMargin = 20;
            // y position is your coordinate of top left corner of the table
            float yPosition = 395;
            BaseTable tableOverallStats = new BaseTable(yPosition, yStartNewPage,
            bottomMargin, tableWidth, margin, openCellDocument, overallStatsPage, true, drawContent);
            PdfServiceReportCommon.addDarkGreyHeader(tableOverallStats, fontCG, "Deployment Statistics/Planned site forecast");

            // Generate the Data Inbound Volumes Pie Chart
            JFreeChart dataInPieChart = creator.createTtoDoughnut(Integer.parseInt(activitySites[3]), Integer.parseInt(activitySites[2]) - Integer.parseInt(activitySites[3]));
            ChartUtilities.saveChartAsPNG(new File(PdfServiceReportConfig.GRAPHS_DIR + "TtoVsNon-Tto.png"), dataInPieChart, 350, 350);

            //drawImage 1
            // Title is now part of the image
            //PdfServiceReportCommon.addTextToPage(contentStream, fontCGBold, 14, PdfServiceReportConfig.openCellFontGrey, 715, 380, "TTO vs NON-TTO");
            PDImageXObject image1 = PDImageXObject.createFromFile(PdfServiceReportConfig.GRAPHS_DIR + "TtoVsNon-Tto.png", openCellDocument);
            float scaleRatio = 0.75f;
            contentStream.drawImage(image1, tableWidth + 50, 150, image1.getWidth()*scaleRatio, image1.getHeight()*scaleRatio); // 3rd param was 200 oringally

            Row<PDPage> row = tableOverallStats.createRow(12);
            Cell<PDPage> cell = row.createCell(35, "Ongoing Deployment:");
            cell.setFont(fontCGBold);
            cell.setFontSize(12);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
            cell.setBottomBorderStyle(new LineStyle(PdfServiceReportConfig.openCellFontGrey, 2));
            cell.setBottomPadding(0);

            cell = row.createCell(15, "");
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));

            cell = row.createCell(50, "");
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));

            row = tableOverallStats.createRow(12);
            cell = row.createCell(10, "Activity:");
            cell.setFont(fontCGBold);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
            cell.setBottomBorderStyle(new LineStyle(PdfServiceReportConfig.openCellFontGrey, 1));
            cell.setBottomPadding(0);

            cell = row.createCell(25, "");
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));

            cell = row.createCell(7, "Sites");
            cell.setFont(fontCGBold);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
            cell.setBottomBorderStyle(new LineStyle(PdfServiceReportConfig.openCellFontGrey, 1));
            cell.setBottomPadding(0);

            cell = row.createCell(8, "");
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));

            cell = row.createCell(25, "New Installs past month:");
            cell.setFont(fontCGBold);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
            cell.setBottomBorderStyle(new LineStyle(PdfServiceReportConfig.openCellFontGrey, 1));
            cell.setBottomPadding(0);

            cell = row.createCell(25, "");
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));

            addRowToTable(tableOverallStats, "Acquired Sites", activitySites[0], getCellText(newInstalls, 0), fontCG);
            addRowToTable(tableOverallStats, "Approved number of sites", activitySites[1], getCellText(newInstalls, 1), fontCG);
            addRowToTable(tableOverallStats, "Live number of sites (Fully handed over)", activitySites[2],  getCellText(newInstalls, 2), fontCG);
            addRowToTable(tableOverallStats, "Live number of sites passed TTO", activitySites[3],  getCellText(newInstalls, 3), fontCG);
            addRowToTable(tableOverallStats, "Number of deployed femto cells", activitySites[4],  getCellText(newInstalls, 4), fontCG);
            addRowToTable(tableOverallStats, "", "",  getCellText(newInstalls, 5), fontCG);

            row = tableOverallStats.createRow(12);
            cell = row.createCell(20, "Latest TTO sites:");
            cell.setFont(fontCGBold);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
            cell.setBottomBorderStyle(new LineStyle(PdfServiceReportConfig.openCellFontGrey, 1));
            cell.setBottomPadding(0);

            cell = row.createCell(30, "");
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));

            cell = row.createCell(50, getCellText(newInstalls, 6));
            cell.setFont(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
            cell.setBottomPadding(0);

            for (int i = 0; i < 10; i++){
                addRowToTable(tableOverallStats, getCellText(latestTtoSites, i), "", getCellText(newInstalls, i+7), fontCG);
            }
            tableOverallStats.draw();

            PdfServiceReportCommon.addDatetoSlideFooter(contentStream, fontCG);
            globalPageCounter++;
            PdfServiceReportCommon.addPageNoToSlideFooter(contentStream, fontCG, globalPageCounter);

            // Make sure that the content stream is closed:
            contentStream.close();

            System.out.println("Overall Stats Page 1 done");

        } catch (Exception e){
            System.out.println("Error creating Overall Stats Page 1: " + e.getMessage());
            e.printStackTrace();
           return 0;
        }

        //do we need more pages, so far 10 ttiSites and 17 latestSites would be printed
        //int maxRemainingRows = Math.max(latestTtoSites.length - 10, newInstalls.length - 17); // replaced 16/4/2020 by MW following ZAW email
        int maxRemainingRows = Math.max(Math.max(latestTtoSites.length - 10,0), Math.max(newInstalls.length - 17, 0));        
        
        int rowsPerPage = 20;
        int noRemainingPages = 0;

        if ( maxRemainingRows % rowsPerPage == 0 ) {
            noRemainingPages = maxRemainingRows/rowsPerPage;
        } else {
            noRemainingPages = maxRemainingRows/rowsPerPage + 1;
        }

        boolean allLatestTtoPrinted = false;
        boolean allNewInstallsPrinted = false;

        if (latestTtoSites.length <= 10) {
            allLatestTtoPrinted = true;
        }

        if (newInstalls.length <= 17) {
            allNewInstallsPrinted = true;
        }

        for (int pageCounter = 0; pageCounter < noRemainingPages;  pageCounter++){
            try {
                PDPage overallStatsPage2 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
                openCellDocument.addPage(overallStatsPage2);
                PDPageContentStream contentStream2;
                contentStream2 = new PDPageContentStream(openCellDocument, overallStatsPage2);

                PdfServiceReportCommon.addTextToPage(contentStream2, fontCG, 30, PdfServiceReportConfig.openCellFontBlue, 31, 467, "Overall Deployment Statistics");
                //Table
                float margin = 15;
                // starting y position is whole page height subtracted by top and bottom margin
                float yStartNewPage = 170;
                float tableWidth = overallStatsPage2.getMediaBox().getWidth()/2 + 150;
                boolean drawContent = true;
                float bottomMargin = 20;
                // y position is your coordinate of top left corner of the table
                float yPosition = 435;
                BaseTable tableOverallStats = new BaseTable(yPosition, yStartNewPage,
                bottomMargin, tableWidth, margin, openCellDocument, overallStatsPage2, true, drawContent);
                PdfServiceReportCommon.addDarkGreyHeader(tableOverallStats, fontCG, "Deployment Statistics/Planned site forecast");

                String cell1Text;
                String cell3Text;
                Color cell1BorderColor;
                Color cell3BorderColor;
                if (allLatestTtoPrinted){
                    cell1Text = "";
                    cell1BorderColor = new Color(239, 242, 250);
                } else {
                    cell1Text = "Latest TTO sites:";
                    cell1BorderColor = PdfServiceReportConfig.openCellFontGrey;
                }
                if (allNewInstallsPrinted){
                    cell3Text = "";
                    cell3BorderColor = new Color(239, 242, 250);
                } else {
                    cell3Text = "New Installs past month:";
                    cell3BorderColor = PdfServiceReportConfig.openCellFontGrey;
                }

                Row<PDPage> row = tableOverallStats.createRow(12);
                Cell<PDPage> cell = row.createCell(25, cell1Text);
                cell.setFont(fontCGBold);
                cell.setFontSize(10);
                cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
                cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
                cell.setBottomBorderStyle(new LineStyle(cell1BorderColor, 1));
                cell.setBottomPadding(0);

                cell = row.createCell(15, "");
                cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));

                cell = row.createCell(25, cell3Text);
                cell.setFont(fontCGBold);
                cell.setFontSize(10);
                cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
                cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
                cell.setBottomBorderStyle(new LineStyle(cell3BorderColor, 1));
                cell.setBottomPadding(0);

                cell = row.createCell(35, "");
                cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));

                for (int i = 0; i < rowsPerPage; i++){
                    addRowToTable(tableOverallStats, getCellText(latestTtoSites, (pageCounter*rowsPerPage + i + 10)), "", getCellText(newInstalls, (pageCounter*rowsPerPage + i + 17)), fontCG);
                }

                if (latestTtoSites.length <= ((pageCounter + 1)*rowsPerPage + 10)) {
                    allLatestTtoPrinted = true;
                }

                if (newInstalls.length <= ((pageCounter + 1)*rowsPerPage + 17)) {
                    allNewInstallsPrinted = true;
                }

                tableOverallStats.draw();

                PdfServiceReportCommon.addDatetoSlideFooter(contentStream2, fontCG);
                globalPageCounter++;
                PdfServiceReportCommon.addPageNoToSlideFooter(contentStream2, fontCG, globalPageCounter);

                // Make sure that the content stream is closed:
                contentStream2.close();

                System.out.println("Overall Stats Page ... done");

            } catch (Exception e){
                System.out.println("Error creating Overall Stats Page: " + e.getMessage());
                e.printStackTrace();
               return 0;
            }
        }
        return globalPageCounter;
    }

    private boolean addRowToTable(BaseTable table, Object text1, Object text2, Object text3, PDFont fontCG) {
        try {
            Row<PDPage> row = table.createRow(12);
            Cell<PDPage> cell = row.createCell(35, text1.toString());
            cell.setFont(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
            cell.setBottomPadding(0);

            cell = row.createCell(5, text2.toString());
            cell.setFont(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
            cell.setAlign(HorizontalAlignment.RIGHT);
            cell.setBottomPadding(0);

            cell = row.createCell(10, "");
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));

            cell = row.createCell(50, text3.toString());
            cell.setFont(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setBorderStyle(new LineStyle(new Color(239, 242, 250), 0));
            cell.setBottomPadding(0);

            return true;
        } catch (Exception e){
            return false;
        }
    }

    private String getCellText(String[] sitesArray, int i){
        if (i < sitesArray.length){
            return "- " + sitesArray[i];
        } else {
            return "";
        }
    }
}
