package com.dataduct.invobroker.ranmatereports.servicereports;

import com.dataduct.invobroker.ranmatereports.PDFReportCreator4Mnos;
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
 * @author zoran
 */
public class PdfServiceReportSitesGraphs {
    String coveringPeriod;
    private PDFReportCreator4Mnos creator = null;
    
    public PdfServiceReportSitesGraphs(String theCoveringPeriod, PDFReportCreator4Mnos theCreator) {
        creator = theCreator;
        coveringPeriod = theCoveringPeriod;
    }

    public boolean createSitesGraphsSlide(PDDocument openCellDocument, PDFont fontCG, String title, int pageCounter, boolean isTto) {
        try {
            //Page custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage reportSitesGraphs = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(reportSitesGraphs);

            PDPageContentStream contentStream;
            contentStream = new PDPageContentStream(openCellDocument, reportSitesGraphs);
            PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 30, PdfServiceReportConfig.openCellFontBlue, 31, 467, title + " Graphs " + coveringPeriod +" Monthly");
            //PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 18, PdfServiceReportConfig.openCellFontGrey, 45, 410, "Cell Availability");
            //PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 18, PdfServiceReportConfig.openCellFontGrey, 505, 410, "CS/DCR");
            //PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 18, PdfServiceReportConfig.openCellFontGrey, 45, 210, "PS/DCR");
            //PdfServiceReportCommon.addTextToPage(contentStream, fontCG, 18, PdfServiceReportConfig.openCellFontGrey, 505, 210, "Interfap CS Handover");

            // Generate the Data Inbound Volumes Pie Chart
            //JFreeChart dataInPieChart = creator.createTtoDoughnut(Integer.parseInt(activitySites[3]), Integer.parseInt(activitySites[2]) - Integer.parseInt(activitySites[3]));
            //ChartUtilities.saveChartAsPNG(new File(PdfServiceReportConfig.GRAPHS_DIR + "TtoVsNon-Tto.png"), dataInPieChart, 350, 350);
            
            //drawImage 1
            JFreeChart cisChart = creator.createCisGraph(isTto);
            ChartUtilities.saveChartAsPNG(new File(PdfServiceReportConfig.GRAPHS_DIR + title + "CellAvailability.png"), cisChart, 450, 250);
            
            PDImageXObject image1 = PDImageXObject.createFromFile(PdfServiceReportConfig.GRAPHS_DIR + title + "CellAvailability.png", openCellDocument);
            float scaleRatio = 0.75f;
            contentStream.drawImage(image1, 57, 248, image1.getWidth()*scaleRatio, image1.getHeight()*scaleRatio);
            
            //drawImage 2
            JFreeChart csDcrChart = creator.createCsDcrGraph(isTto);
            ChartUtilities.saveChartAsPNG(new File(PdfServiceReportConfig.GRAPHS_DIR + title + "CSDCR.png"), csDcrChart, 450, 250);

            PDImageXObject image2 = PDImageXObject.createFromFile(PdfServiceReportConfig.GRAPHS_DIR + title + "CSDCR.png", openCellDocument);
            contentStream.drawImage(image2, 510, 248, image2.getWidth()*scaleRatio, image2.getHeight()*scaleRatio);

            //drawImage 3
            JFreeChart psDcrChart = creator.createPsDcrGraph(isTto);
            ChartUtilities.saveChartAsPNG(new File(PdfServiceReportConfig.GRAPHS_DIR + title + "PSDCR.png"), psDcrChart, 450, 250);

            PDImageXObject image3 = PDImageXObject.createFromFile(PdfServiceReportConfig.GRAPHS_DIR + title + "PSDCR.png", openCellDocument);
            contentStream.drawImage(image3, 57, 48, image3.getWidth()*scaleRatio, image3.getHeight()*scaleRatio);

            //drawImage 4
            JFreeChart interfapCSHOChart = creator.createInterfapCSHOGraph(isTto);
            ChartUtilities.saveChartAsPNG(new File(PdfServiceReportConfig.GRAPHS_DIR + title + "InterfapCSHandover.png"), interfapCSHOChart, 450, 250);

            PDImageXObject image4 = PDImageXObject.createFromFile(PdfServiceReportConfig.GRAPHS_DIR + title + "InterfapCSHandover.png", openCellDocument);
            contentStream.drawImage(image4, 510, 48, image4.getWidth()*scaleRatio, image4.getHeight()*scaleRatio);

            PdfServiceReportCommon.addDatetoSlideFooter(contentStream, fontCG);
            pageCounter++;
            PdfServiceReportCommon.addPageNoToSlideFooter(contentStream, fontCG, pageCounter);

          // Make sure that the content stream is closed:
            contentStream.close();
            System.out.println("PDF Creation Slide 5 done");
            return true;
        } catch (Exception e){
            System.out.println("Error creating PDF Creation Slide 5: " + e.getMessage());
            e.printStackTrace();
           return false;
        }

    }
}
