package com.dataduct.invobroker.ranmatereports.servicereports;

/**
 *
 * @author zorana
 */
import com.dataduct.invobroker.ranmatereports.PDFReportCreator4Mnos;
import java.io.*;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.multipdf.Overlay;
import java.util.HashMap;
import org.apache.pdfbox.pdmodel.font.PDFont;

import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;


public class PdfServiceReportGenerator {

    private PDDocument openCellDocument;
    private PDFont fontCG;
    private PDFont fontCGBold;
    private String mno;

    private final String coveringPeriod;
    private int pageCounter;
    private PDFReportCreator4Mnos creator = null;
    private String subdir = null;
    boolean custom = false;

    public PdfServiceReportGenerator (PDFReportCreator4Mnos theCreator, String theCoveringPeriod, String theMno, String theSubdir, boolean isCustom) {
        creator = theCreator;
        coveringPeriod = theCoveringPeriod;
        mno = theMno;
        pageCounter = 0;
        subdir = theSubdir;
        custom = isCustom;
    }

    public boolean createReport(){
        System.out.println("PDF with Tables Creation Started");

        try {
            //StrattoOpenCell Service Report pdf document
            openCellDocument = new PDDocument();
            //load OpenCell font - Century Gothic
            //fontCG = PDTrueTypeFont.loadTTF(openCellDocument, new File(PdfServiceReportConfig.REPORT_DIR + "century"));
            //fontCGBold = PDTrueTypeFont.loadTTF(openCellDocument, new File(PdfServiceReportConfig.REPORT_DIR + "century-gothic-bold.ttf"));
            fontCG = PDTrueTypeFont.loadTTF(openCellDocument, new File(creator.REPORT_TEMPLATE_DIR + "century"));
            fontCGBold = PDTrueTypeFont.loadTTF(openCellDocument, new File(creator.REPORT_TEMPLATE_DIR + "century-gothic-bold.ttf"));            
                    
            PdfServiceReportTitleSlide titleSlideCreator;
            //titleSlideCreator = new PdfServiceReportTitleSlide(PdfServiceReportConfig.mno, coveringPeriod);
            titleSlideCreator = new PdfServiceReportTitleSlide(mno, coveringPeriod);
            pageCounter = titleSlideCreator.createTitleSlide(openCellDocument, fontCG, pageCounter);

            PdfServiceReportNetworkPerformance nwPerformanceSlideCreator;
            nwPerformanceSlideCreator = new PdfServiceReportNetworkPerformance(creator);
            pageCounter = nwPerformanceSlideCreator.createNwPerformanceSlide(openCellDocument, fontCG, fontCGBold, pageCounter);

            PdfServiceReportSitesPerformance ttoSitesPerformanceSlide;
            ttoSitesPerformanceSlide = new PdfServiceReportSitesPerformance(true, creator);
            pageCounter = ttoSitesPerformanceSlide.createSitesPerformanceSlide(openCellDocument, fontCG, "Concession TTO", pageCounter);

            PdfServiceReportSitesStatus ttoSitesStatusSlide;
            ttoSitesStatusSlide = new PdfServiceReportSitesStatus(true, creator);
            pageCounter = ttoSitesStatusSlide.createSitesStatusSlide(openCellDocument, fontCG, fontCGBold, pageCounter);

            PdfServiceReportSitesGraphs ttoGraphs;
            ttoGraphs = new PdfServiceReportSitesGraphs(coveringPeriod, creator);
            ttoGraphs.createSitesGraphsSlide(openCellDocument, fontCG, "TTO", pageCounter, true);
            pageCounter ++;

            PdfServiceReportSitesGraphs nonTtoGraphs;
            nonTtoGraphs = new PdfServiceReportSitesGraphs(coveringPeriod, creator);
            nonTtoGraphs.createSitesGraphsSlide(openCellDocument, fontCG, "Non-TTO", pageCounter, false);
            pageCounter ++;

            PdfServiceReportSitesPerformance nonTtoSitesPerformanceSlide;
            nonTtoSitesPerformanceSlide = new PdfServiceReportSitesPerformance(false, creator);
            pageCounter = nonTtoSitesPerformanceSlide.createSitesPerformanceSlide(openCellDocument, fontCG, "Non-TTO", pageCounter);

            PdfServiceReportSitesStatus nonTtoSitesStatusSlide;
            nonTtoSitesStatusSlide = new PdfServiceReportSitesStatus(false, creator);
            pageCounter = nonTtoSitesStatusSlide.createSitesStatusSlide(openCellDocument, fontCG, fontCGBold, pageCounter);

            PdfServiceReportIncidentLog incidentLogSlide;
            incidentLogSlide = new PdfServiceReportIncidentLog(creator);
            incidentLogSlide.createIncidentLogSlide(openCellDocument, fontCG, fontCGBold, pageCounter);
            pageCounter ++;

            PdfServiceReportSitesInPlay sitesInPlaySlide;
            sitesInPlaySlide = new PdfServiceReportSitesInPlay(creator);
            pageCounter = sitesInPlaySlide.createSitesInPlaySlide(openCellDocument, fontCG, fontCGBold, pageCounter);

            PdfServiceReportOverallStats overallStatsSlide;
            overallStatsSlide = new PdfServiceReportOverallStats(creator);
            // FINAL slide - the dodgy one
            pageCounter = overallStatsSlide.createOverallStatsSlide(openCellDocument, fontCG, fontCGBold, pageCounter, coveringPeriod);

            //Content of all pages created, Add Background, Header and Footer as per the provided template
            HashMap<Integer, String> overlayGuide = new HashMap<Integer, String>();
            for(int i=0; i<openCellDocument.getNumberOfPages(); i++){
                //overlayGuide.put(i+1, PdfServiceReportConfig.REPORT_DIR + PdfServiceReportConfig.templateName); // old version
                overlayGuide.put(i+1, creator.REPORT_TEMPLATE_DIR + PdfServiceReportConfig.templateName);
            }

            Overlay overlay = new Overlay();
            overlay.setInputPDF(openCellDocument);
            overlay.setOverlayPosition(Overlay.Position.BACKGROUND);
            overlay.overlay(overlayGuide);

            //create file to store generated pdf - probably missing the dir
            File openCellFile = null;
            if (custom) {
                openCellFile = new File(creator.CUSTOM_REPORT_DIR + mno + "/" + PdfServiceReportConfig.reportNameFixed + "- " + mno + " " + coveringPeriod + ".pdf");                                
            } else {
                openCellFile = new File(creator.MNO_REPORT_DIR + PdfServiceReportConfig.reportNameFixed + "- " + mno + " " + coveringPeriod + ".pdf");                
            }
            System.out.println(openCellFile.getName() + " created");
            openCellDocument.save(openCellFile);
            System.out.println(openCellFile.getName() + " saved");
            openCellDocument.close();

            return true;
        } catch (Exception e){
            System.out.println("MNO Service Report creation failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}