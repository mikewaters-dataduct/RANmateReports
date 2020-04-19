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

/**
 *
 * @author zorana
 */
public class PdfServiceReportNetworkPerformance {

    private PDFReportCreator4Mnos creator = null;
    
    public PdfServiceReportNetworkPerformance(PDFReportCreator4Mnos theCreator) {
        creator = theCreator;
    }
    
    public int createNwPerformanceSlide(PDDocument openCellDocument, PDFont fontCG, PDFont fontCGBold, int pageCounter){
        try {
            //Page custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage nwPerformancePage = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(nwPerformancePage);
            PDPageContentStream contentStream;
            contentStream = new PDPageContentStream(openCellDocument, nwPerformancePage);

            PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 30, PdfServiceReportConfig.openCellFontBlue, 31, 467, "TTO/Non-TTO Network Monthly Performance");

            PdfServiceReportDataCollector pdfReportDataCollector = new PdfServiceReportDataCollector(creator);

            //TTO Table
            String[] ttoNwPerformance = pdfReportDataCollector.getNwPerformance(true);
            PdfServiceReportCommon.addTextToPage(contentStream, fontCGBold, 18, PdfServiceReportConfig.openCellFontGrey, 45, 410, "Number of live TTO sites: " + ttoNwPerformance[0]);

            float margin = 40;
            // starting y position is whole page height subtracted by top and bottom margin
            float yStartNewPage = 170;
            float tableWidth = nwPerformancePage.getMediaBox().getWidth() - 650;
            boolean drawContent = true;
            float bottomMargin = 20;
            // y position is your coordinate of top left corner of the table
            float yPosition = 395;
            BaseTable tableLeft = new BaseTable(yPosition, yStartNewPage,
                bottomMargin, tableWidth, margin, openCellDocument, nwPerformancePage, true, drawContent);

            PdfServiceReportCommon.addDarkGreyHeader(tableLeft, fontCG, "TTO Network Performance");

            addHeader2ToTable(tableLeft, fontCG);
            addRowToTable(tableLeft, "Availability", PdfServiceReportConfig.cellAvailabilityTarget, ttoNwPerformance[1], fontCG, PdfServiceReportConfig.darkGrey);
            addRowToTable(tableLeft, "RRC SR Rate", PdfServiceReportConfig.rrcSRTarget, ttoNwPerformance[2], fontCG, PdfServiceReportConfig.lightGrey);
            addRowToTable(tableLeft, "Call Setup Success Rate", PdfServiceReportConfig.csSRTarget, ttoNwPerformance[3], fontCG, PdfServiceReportConfig.darkGrey);
            addRowToTable(tableLeft, "Call Drop Rate", PdfServiceReportConfig.csDCRTarget, ttoNwPerformance[4], fontCG, PdfServiceReportConfig.lightGrey);
            addRowToTable(tableLeft, "Packet Setup Success Rate", PdfServiceReportConfig.psSRTarget, ttoNwPerformance[5], fontCG, PdfServiceReportConfig.darkGrey);
            addRowToTable(tableLeft, "Packet Drop Rate", PdfServiceReportConfig.psDSRTarget, ttoNwPerformance[6], fontCG, PdfServiceReportConfig.lightGrey);
            addRowToTable(tableLeft, "Inter FAP CS", PdfServiceReportConfig.interFapCSTarget, ttoNwPerformance[7], fontCG, PdfServiceReportConfig.darkGrey);
            addRowToTable(tableLeft, "Inter FAP PS", PdfServiceReportConfig.interFapPSTarget, ttoNwPerformance[8], fontCG, PdfServiceReportConfig.lightGrey);

            tableLeft.draw();

            //Non-TTO table
            String[] nonTtoNwPerformance = pdfReportDataCollector.getNwPerformance(false);
            PdfServiceReportCommon.addTextToPage(contentStream, fontCGBold, 18, PdfServiceReportConfig.openCellFontGrey, 505, 410, "Number of live Non-TTO sites: " + nonTtoNwPerformance[0]);

            float marginR = 500;
            BaseTable tableRight = new BaseTable(yPosition, yStartNewPage,
                bottomMargin, tableWidth, marginR, openCellDocument, nwPerformancePage, true, drawContent);

            PdfServiceReportCommon.addDarkGreyHeader(tableRight, fontCG, "Non-TTO Network Performance");

            addHeader2ToTable(tableRight, fontCG);

            addRowToTable(tableRight, "Availability", PdfServiceReportConfig.cellAvailabilityTarget, nonTtoNwPerformance[1], fontCG, PdfServiceReportConfig.darkGrey);
            addRowToTable(tableRight, "RRC SR Rate", PdfServiceReportConfig.rrcSRTarget, nonTtoNwPerformance[2], fontCG, PdfServiceReportConfig.lightGrey);
            addRowToTable(tableRight, "Call Setup Success Rate", PdfServiceReportConfig.csSRTarget, nonTtoNwPerformance[3], fontCG, PdfServiceReportConfig.darkGrey);
            addRowToTable(tableRight, "Call Drop Rate", PdfServiceReportConfig.csDCRTarget, nonTtoNwPerformance[4], fontCG, PdfServiceReportConfig.lightGrey);
            addRowToTable(tableRight, "Packet Setup<br>Success Rate", PdfServiceReportConfig.psSRTarget, nonTtoNwPerformance[5], fontCG, PdfServiceReportConfig.darkGrey);
            addRowToTable(tableRight, "Packet Drop Rate", PdfServiceReportConfig.psDSRTarget, nonTtoNwPerformance[6], fontCG, PdfServiceReportConfig.lightGrey);
            addRowToTable(tableRight, "Inter FAP CS", PdfServiceReportConfig.interFapCSTarget, nonTtoNwPerformance[7], fontCG, PdfServiceReportConfig.darkGrey);
            addRowToTable(tableRight, "Inter FAP PS", PdfServiceReportConfig.interFapPSTarget, nonTtoNwPerformance[8], fontCG, PdfServiceReportConfig.lightGrey);

            tableRight.draw();

            PdfServiceReportCommon.addDatetoSlideFooter(contentStream, fontCG);
            pageCounter++;
            PdfServiceReportCommon.addPageNoToSlideFooter(contentStream, fontCG, pageCounter);

          // Make sure that the content stream is closed:
            contentStream.close();
            System.out.println("PDF Creation Page 2 done");
            return pageCounter;
        } catch (Exception e){
            System.out.println("Error creating PDF Creation Page 2: " + e.getMessage());
            e.printStackTrace();
           return 0;
        }

    }

    private boolean addHeader2ToTable(BaseTable table, PDFont fontCG) {
        try {
            Row<PDPage> row = table.createRow(16);
            Cell<PDPage> cell = row.createCell(34, "");
            cell.setFont(fontCG);
            cell.setFontSize(16);
            cell.setTextColor(Color.WHITE);
            cell.setFillColor(PdfServiceReportConfig.navyHeader);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 3));

            cell = row.createCell(33, "Target");
            cell.setFont(fontCG);
            cell.setFontSize(16);
            cell.setTextColor(Color.WHITE);
            cell.setFillColor(PdfServiceReportConfig.navyHeader);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 3));

            cell = row.createCell(33, "Actual");
            cell.setFont(fontCG);
            cell.setFontSize(16);
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
    private boolean addRowToTable(BaseTable table, String text1, String text2, String text3, PDFont fontCG, Color color) {
        try {
            Row<PDPage> row = table.createRow(16);

            Cell<PDPage> cell = row.createCell(34, text1);
            cell.setFont(fontCG);
            cell.setFontSize(10);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setFillColor(color);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setBorderStyle(new LineStyle(Color.WHITE, 1));

            cell = row.createCell(33, text2);
            cell.setFont(fontCG);
            cell.setFontSize(16);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setFillColor(new Color(125, 184, 55));
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));

            cell = row.createCell(33, text3);
            cell.setFont(fontCG);
            cell.setFontSize(16);
            cell.setTextColor(PdfServiceReportConfig.openCellFontGrey);
            cell.setFillColor(PdfServiceReportCommon.getRedOrGreen(text2, text3));
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell.setBottomBorderStyle(new LineStyle(Color.WHITE, 1));
            cell.setRightBorderStyle(new LineStyle(Color.WHITE, 1));

        return true;
        } catch (Exception e){
           return false;
        }

    }
}
