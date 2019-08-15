/**
 *
 * @author zorana
 */
package com.dataduct.invobroker.ranmatereports;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.multipdf.Overlay;
import java.util.HashMap;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

public class PdfReportWriter {

    public static final int SITE = 1;
    public static final int CUSTOMER = 2;
    public static final int OPERATOR = 3;

    public static final int MONTH = 1;
    public static final int YEAR = 2;
    
    PDFReportCreator creator = null;
    private boolean custom = false;                 // currently ignored - indicates that this report is a custom report requested from the browser
    private int subject = SITE;                        // 1 = Site, 2 = Customer, 3 = Operator
    private int intervalType = 1;                   // currently ignored -1 = Month, 2 = Year
    private String interval = ""; // 1/1/19 - 30/1/19";   // from date, to date
    private String month = ""; /// "January";
    private String year = ""; // "2019";
    private String subjectName = ""; // "15 Bishopsgate";    // currently only one -a list of 1 or more sites, or customers, but not a mix and match    
    private String siteId = ""; // WEW001
    private long voiceCallsNum = 0L; // 24567L;             // number of calls
    private int downloadNumGb = 0; // 52;                  // number of Gigabytes. Anything less than 1 will be displayed as < 1                         
    private int uploadNumGb = 0; // 15;                    // number of Gigabytes. Anything less than 1 will be displayed as < 1                         
    private String availabilityString = ""; // "excellent";
    private float[] availability;                    // currently ignored - CIS values using Vladimir’s views, in V, O, T, E order. Any non-existing operator will be -1.0f
    private String reportName = "";  // "StrattoOpenCell Service Report - NEO Bruntwood 201901 January.pdf";
    private Color openCellFontBlue;
    private Color openCellFontGrey;
    private ArrayList<String> additionalSectionNames;
    private ArrayList<String> additionalSectionIds;
    private HashMap<String, float[]> siteCisValues;
    private PDFont fontCG = null;
    private String imagesDir = null;
    private int pageNumber = 1;
    
    /**
     * 
     * @param custom
     * @param subject
     * @param intervalType
     * @param interval
     * @param month
     * @param year
     * @param subjectName
     * @param voiceCallsNum
     * @param downloadNumGb
     * @param uploadNumGb
     * @param availabilityString
     * @param availability
     * @param reportName
     * @param additionalSectionNames at this stage the names of sites per customer for the new format report
     */
    public PdfReportWriter (PDFReportCreator theCreator, boolean custom, int subject, int intervalType, String interval, String month, String year, String subjectName,
                             long voiceCallsNum, int downloadNumGb, int uploadNumGb, String availabilityString, float[] availability, 
                             String reportName, ArrayList<String> additionalSectionNames, HashMap<String, float[]> siteCisValues, String theSiteId, ArrayList<String> theAdditionalSectionIds) {
        creator = theCreator;
        this.custom = custom;
        this.subject = subject;
        this.intervalType = intervalType;
        this.interval = interval;
        this.month = month;
        this.year = year;
        this.subjectName = subjectName;
        this.voiceCallsNum = voiceCallsNum;
        this.downloadNumGb = downloadNumGb;
        this.uploadNumGb = uploadNumGb;
        this.availabilityString = availabilityString;
        this.availability = availability;     
        this.reportName = reportName;
        this.openCellFontBlue = new Color (51, 71, 109);
        this.openCellFontGrey = new Color (89,89, 89);
        this.additionalSectionNames = additionalSectionNames;
        this.siteCisValues = siteCisValues;
        this.siteId = theSiteId;
        this.additionalSectionIds = theAdditionalSectionIds;
    }
    
    public PdfReportWriter() {
        this.openCellFontBlue = new Color (51, 71, 109);
        this.openCellFontGrey = new Color (89,89, 89);       
    }
    
    public boolean createReport(){
        boolean success = false;
        PDPageContentStream titlePage = null;
        PDPageContentStream callsPage = null;
        PDPageContentStream downloadPage = null;
        PDPageContentStream uploadPage = null;
        PDPageContentStream cisPage = null;
        PDPageContentStream finalPage = null;
        Overlay overlay = null;
        PDDocument openCellDocument = null;
        try {
            Date before = new Date();
            //System.out.println("PDF Creation Started");        
            //OpenCell Report
            openCellDocument = new PDDocument();
            //Century Gothic font
            fontCG = PDTrueTypeFont.loadTTF(openCellDocument, new File(PDFReportCreator.REPORT_TEMPLATE_DIR + "century"));
            imagesDir = "";
            if (custom) {
                imagesDir = PDFReportCreator.CUSTOM_REPORT_DIR + subjectName + "/";                
            } else {
                switch(subject) {
                    case SITE:
                        imagesDir = PDFReportCreator.SITE_REPORT_DIR + subjectName + "/";
                        break;
                    case CUSTOMER:
                        imagesDir = PDFReportCreator.CUSTOMER_REPORT_DIR + subjectName + "/";
                        break;
                    case OPERATOR:
                        imagesDir = PDFReportCreator.MNO_REPORT_DIR + subjectName + "/";
                        break;
                    default:
                        imagesDir = PDFReportCreator.REPORT_DIR + "sites/" + subjectName + "/";
                }
            }

            //Page 1, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage openCellPage1 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(openCellPage1);
       
            titlePage = new PDPageContentStream(openCellDocument, openCellPage1);

            titlePage.beginText();
            titlePage.setFont(fontCG, 48);
            titlePage.setNonStrokingColor(this.openCellFontGrey);
            titlePage.moveTextPositionByAmount(100, 380);
            titlePage.drawString("StrattoOpencell Service Report");
            titlePage.endText();

            titlePage.beginText();
            titlePage.setFont(fontCG, 48);
            titlePage.setNonStrokingColor(this.openCellFontGrey);
            titlePage.moveTextPositionByAmount(100, 320);
            titlePage.drawString(month + " " + year);
            titlePage.endText();

            titlePage.beginText();
            titlePage.setFont(fontCG, 36);
            titlePage.setNonStrokingColor(this.openCellFontGrey);
            titlePage.moveTextPositionByAmount(100, 180);
            titlePage.drawString(subjectName + siteId);
            titlePage.endText();

            //date
            Date today = Calendar.getInstance().getTime();
            // print out today's date
            String date = new SimpleDateFormat("dd/MM/yy").format(new Date());
            titlePage.beginText();
            titlePage.setFont(fontCG, 10);
            titlePage.setNonStrokingColor(Color.WHITE);
            titlePage.moveTextPositionByAmount(29, 20);
            titlePage.drawString(date);
            titlePage.endText();       
                
            //page number
            titlePage.beginText();
            titlePage.setFont(fontCG, 10);
            titlePage.setNonStrokingColor(Color.WHITE);
            titlePage.moveTextPositionByAmount(925, 20);
            titlePage.drawString(Integer.toString(pageNumber++));
            //titlePage.drawString("1");
            titlePage.endText();  
        
            // Make sure that the content stream is closed:
            titlePage.close();
            //System.out.println("PDF Creation Page 1 done");

            //Page 2, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage openCellPage2 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(openCellPage2);
        
            callsPage = new PDPageContentStream(openCellDocument, openCellPage2);    

            //Page 2 Title 
            callsPage.beginText();
            callsPage.setFont(fontCG, 36);
            callsPage.setNonStrokingColor(openCellFontBlue);
            callsPage.moveTextPositionByAmount(31, 467);
            callsPage.drawString("Traffic Statistics – Voice Calls");
            callsPage.endText();

        
            //Comment
            callsPage.beginText();
            callsPage.setFont(fontCG, 20);
            callsPage.setNonStrokingColor(openCellFontGrey);
            callsPage.moveTextPositionByAmount(45, 432);
            String strVoiceCalls = DecimalFormat.getNumberInstance().format(voiceCallsNum);
            callsPage.drawString("\u2022" + strVoiceCalls + " total number of calls (" + interval+ "" +")");
            callsPage.endText();                
                
               
            // print out today's date
            callsPage.beginText();
            callsPage.setFont(fontCG, 10);
            callsPage.setNonStrokingColor(Color.WHITE);
            callsPage.moveTextPositionByAmount(29, 20);
            callsPage.drawString(date);
            callsPage.endText();       
                
            //page number
            callsPage.beginText();
            callsPage.setFont(fontCG, 10);
            callsPage.setNonStrokingColor(Color.WHITE);
            callsPage.moveTextPositionByAmount(925, 20);
            callsPage.drawString(Integer.toString(pageNumber++));
            //callsPage.drawString("2");
            callsPage.endText();       

            //drawImage 1 - Bar Chart
            PDImageXObject imageBar = PDImageXObject.createFromFile(imagesDir+ "TotalNumCallsBarChart.png", openCellDocument);
            float scaleBar = 0.75f;
            callsPage.drawImage(imageBar, 57, 63, imageBar.getWidth()*scaleBar, imageBar.getHeight()*scaleBar);

            //drawImage 2 - Pie Chart
            PDImageXObject imagePie = PDImageXObject.createFromFile(imagesDir+ "TotalNumCallsPieChart.png", openCellDocument);                
            float scalePie = 0.75f;
            callsPage.drawImage(imagePie, 510, 64, imagePie.getWidth()*scalePie, imagePie.getHeight()*scalePie);
 
            // Make sure that the content stream is closed:
            callsPage.close();
            //System.out.println("PDF Creation Page 2 done");

            //Page 3, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage openCellPage3 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(openCellPage3);
        
            downloadPage = new PDPageContentStream(openCellDocument, openCellPage3);

            //Page 3 Title 
            downloadPage.beginText();
            downloadPage.setFont(fontCG, 36);
            downloadPage.setNonStrokingColor(openCellFontBlue);
            downloadPage.moveTextPositionByAmount(31, 467);
            downloadPage.drawString("Traffic Statistics – Data Download");
            downloadPage.endText();
        
            //Comment
            downloadPage.beginText();
            downloadPage.setFont(fontCG, 20);
            downloadPage.setNonStrokingColor(openCellFontGrey);
            downloadPage.moveTextPositionByAmount(45, 432);
            if (downloadNumGb < 1) {
                downloadPage.drawString("\u2022" + " < 1 GB data download (Web browsing, video streaming, file downloads " + interval + ")");
            } else {
                downloadPage.drawString("\u2022" + downloadNumGb + "GB data download (Web browsing, video streaming, file downloads " + interval + ")");                
            }
            downloadPage.endText();                
                               
            // print out today's date
            downloadPage.beginText();
            downloadPage.setFont(fontCG, 10);
            downloadPage.setNonStrokingColor(Color.WHITE);
            downloadPage.moveTextPositionByAmount(29, 20);
            downloadPage.drawString(date);
            downloadPage.endText();       
                
            //page number
            downloadPage.beginText();
            downloadPage.setFont(fontCG, 10);
            downloadPage.setNonStrokingColor(Color.WHITE);
            downloadPage.moveTextPositionByAmount(925, 20);
            downloadPage.drawString(Integer.toString(pageNumber++));
            //downloadPage.drawString("3");
            downloadPage.endText();  

            //drawImage 1 - Bar Chart
            PDImageXObject imageBar2 = PDImageXObject.createFromFile(imagesDir + "DataInboundBarChart.png", openCellDocument);
            downloadPage.drawImage(imageBar2, 57, 63, imageBar2.getWidth()*scaleBar, imageBar2.getHeight()*scaleBar);

            //drawImage 2 - Pie Chart
            PDImageXObject imagePie2 = PDImageXObject.createFromFile(imagesDir + "DataInboundPieChart.png", openCellDocument);                
            downloadPage.drawImage(imagePie2, 510, 64, imagePie2.getWidth()*scalePie, imagePie2.getHeight()*scalePie);
 
            // Make sure that the content stream is closed:
            downloadPage.close();
            //System.out.println("PDF Creation Page 3 done");

            //Page 4, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage openCellPage4 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(openCellPage4);
        
            uploadPage = new PDPageContentStream(openCellDocument, openCellPage4);

            //Page 4 Title 
            uploadPage.beginText();
            uploadPage.setFont(fontCG, 36);
            uploadPage.setNonStrokingColor(openCellFontBlue);
            uploadPage.moveTextPositionByAmount(31, 467);
            uploadPage.drawString("Traffic Statistics – Data Upload");
            uploadPage.endText();

        
            //Comment
            uploadPage.beginText();
            uploadPage.setFont(fontCG, 20);
            uploadPage.setNonStrokingColor(openCellFontGrey);
            uploadPage.moveTextPositionByAmount(45, 432);
            if (downloadNumGb < 1) {
                uploadPage.drawString("\u2022" + " < 1 GB data upload (Email sending, file uploads, WhatsApp etc " + interval + ")");
            } else {
                uploadPage.drawString("\u2022" + uploadNumGb + "GB data upload (Email sending, file uploads, WhatsApp etc " + interval + ")");
            }
            uploadPage.endText();                
                
               
            // print out today's date
            uploadPage.beginText();
            uploadPage.setFont(fontCG, 10);
            uploadPage.setNonStrokingColor(Color.WHITE);
            uploadPage.moveTextPositionByAmount(29, 20);
            uploadPage.drawString(date);
            uploadPage.endText();       
                
            //page number
            uploadPage.beginText();
            uploadPage.setFont(fontCG, 10);
            uploadPage.setNonStrokingColor(Color.WHITE);
            uploadPage.moveTextPositionByAmount(925, 20);
            uploadPage.drawString(Integer.toString(pageNumber++));
            //uploadPage.drawString("4");
            uploadPage.endText();       

            //drawImage 1 - Bar Chart
            PDImageXObject imageBar3 = PDImageXObject.createFromFile(imagesDir + "DataOutboundBarChart.png", openCellDocument);
            uploadPage.drawImage(imageBar3, 67, 63, imageBar3.getWidth()*scaleBar, imageBar3.getHeight()*scaleBar);

            //drawImage 2 - Pie Chart
            PDImageXObject imagePie3 = PDImageXObject.createFromFile(imagesDir + "DataOutboundPieChart.png", openCellDocument);                
            uploadPage.drawImage(imagePie3, 520, 63, imagePie3.getWidth()*scalePie, imagePie3.getHeight()*scalePie);
 
            // Make sure that the content stream is closed:
            uploadPage.close();
            //System.out.println("PDF Creation Page 4 done");
            
            //Page 5, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage openCellPage5 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(openCellPage5);
        
            cisPage = new PDPageContentStream(openCellDocument, openCellPage5); 

            //Page 5 Title 
            cisPage.beginText();
            cisPage.setFont(fontCG, 36);
            cisPage.setNonStrokingColor(openCellFontBlue);
            cisPage.moveTextPositionByAmount(31, 467);
            cisPage.drawString("Availability Statistics – " + month + " " + year);
            cisPage.endText();
        
            //Comment
            cisPage.beginText();
            cisPage.setFont(fontCG, 16);
            cisPage.setNonStrokingColor(openCellFontGrey);
            cisPage.moveTextPositionByAmount(45, 432);
            cisPage.drawString("\u2022" + "Statistics from " + interval);
            cisPage.endText();                
            //Comment
            cisPage.beginText();
            cisPage.setFont(fontCG, 16);
            cisPage.setNonStrokingColor(openCellFontGrey);
            cisPage.moveTextPositionByAmount(45, 407);
            //if (subjectName.equals("Candy & Candy")) {
            //    System.out.println("    " + subjectName + " ReportWriter cis values = " + Arrays.toString(availability)); 
            //}
            cisPage.drawString("\u2022" + "Availability " + getAvailabilityDesc(availability[5]));
            cisPage.endText();                
            //Comment
            cisPage.beginText();
            cisPage.setFont(fontCG, 24);
            cisPage.setNonStrokingColor(PDFReportCreator.opcoColours[0]);
            cisPage.moveTextPositionByAmount(45, 375);
            //cisPage.drawString("O2 99.92%");
            if (availability[0] > 0) {
                cisPage.drawString("VF " + String.format("%.2f", availability[0]) + "%");
            } else {
                cisPage.drawString("VF");                
            }
            cisPage.endText();                
            
            cisPage.beginText();
            cisPage.setFont(fontCG, 24);
            cisPage.setNonStrokingColor(PDFReportCreator.opcoColours[1]);
            cisPage.moveTextPositionByAmount(45, 340);
            if (availability[1] > 0) {
                cisPage.drawString("O2 " + String.format("%.2f", availability[1]) + "%");
            } else {
                cisPage.drawString("O2");
            }                
            cisPage.endText();

            cisPage.beginText();
            cisPage.setFont(fontCG, 24);
            cisPage.setNonStrokingColor(PDFReportCreator.opcoColours[2]);
            cisPage.moveTextPositionByAmount(45, 305);
            if (availability[2] > 0) {
                cisPage.drawString("THREE " + String.format("%.2f", availability[2]) + "%");
            } else {
                cisPage.drawString("THREE");
            }
            cisPage.endText();                

            cisPage.beginText();
            cisPage.setFont(fontCG, 24);
            cisPage.setNonStrokingColor(PDFReportCreator.opcoColours[3]);
            cisPage.moveTextPositionByAmount(45, 270);
            if (availability[3] > 0) {
                cisPage.drawString("EE " + String.format("%.2f", availability[3]) + "%");
            } else {
                cisPage.drawString("EE");
            }    
            cisPage.endText();                
            
                     
            // print out today's date
            cisPage.beginText();
            cisPage.setFont(fontCG, 10);
            cisPage.setNonStrokingColor(Color.WHITE);
            cisPage.moveTextPositionByAmount(29, 20);
            cisPage.drawString(date);
            cisPage.endText();       
                
            //page number
            cisPage.beginText();
            cisPage.setFont(fontCG, 10);
            cisPage.setNonStrokingColor(Color.WHITE);
            cisPage.moveTextPositionByAmount(925, 20);
            cisPage.drawString(Integer.toString(pageNumber++));
            //cisPage.drawString("5");
            cisPage.endText();       

            // Make sure that the content stream is closed:
            cisPage.close();
            //System.out.println("PDF Creation CIS Page done");

            // Extra site sections in customer report requested by Andy
            if (subject == CUSTOMER) {
                for (int i = 0; i < additionalSectionNames.size(); i++) {
                    String siteName = additionalSectionNames.get(i);
                    String siteId = additionalSectionIds.get(i);
                // for (String siteName: additionalSectionNames) { // before we needed an 'i' to index the siteIds for Andy
                    try {
                        addSiteToCustomerReport(openCellDocument, siteName, siteId);
                    } catch (Exception e) {
                        System.out.println("Unable to add " + siteName + " section to " + subjectName + " report: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // New page
            //Page 5, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage openCellPage6 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(openCellPage6);
        
            finalPage = new PDPageContentStream(openCellDocument, openCellPage6);

            //Page 5 Title 
            finalPage.beginText();
            finalPage.setFont(fontCG, 32);
            finalPage.setNonStrokingColor(openCellFontGrey);
            finalPage.moveTextPositionByAmount(401, 354);
            finalPage.drawString("Thank You");
            finalPage.endText();
            
            PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
            borderULine.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
            PDAnnotationLink txtLink = new PDAnnotationLink();
            txtLink.setBorderStyle(borderULine);
            PDColor blueColor = new PDColor(new float[] { 0, 0, 1 }, PDDeviceRGB.INSTANCE);
            txtLink.setColor(blueColor);

            // add an action
            PDActionURI action = new PDActionURI();
            action.setURI("mailto:support@strattoopencell.com"); // changed on 4/6/2019 at Andy's request from support@opencell.co.uk as per https://dataduct.atlassian.net/browse/OC-87
            txtLink.setAction(action);
            
                PDRectangle position = new PDRectangle();
                position.setLowerLeftX(508); // was 530
                position.setLowerLeftY(290); 
                position.setUpperRightX(795);  // was 770
                position.setUpperRightY(310); 
                txtLink.setRectangle(position);
                openCellPage6.getAnnotations().add(txtLink);

        
            //Comment
            finalPage.beginText();
            finalPage.setFont(fontCG, 20);
            finalPage.setNonStrokingColor(openCellFontGrey);
            finalPage.moveTextPositionByAmount(166, 293); // was , 268
            finalPage.drawString("For more information contact us at");
            finalPage.endText();                
            
            //Comment
            finalPage.beginText();
            finalPage.setFont(fontCG, 20);
            finalPage.setNonStrokingColor(Color.BLUE);
            finalPage.moveTextPositionByAmount(508, 293); // was 530, 
            finalPage.drawString("support@strattoopencell.com"); // changed on 4/6/2019 at Andy's request from support@opencell.co.uk as per https://dataduct.atlassian.net/browse/OC-87
            finalPage.endText();     

            //Comment
            finalPage.beginText();
            finalPage.setFont(fontCG, 20);
            finalPage.setNonStrokingColor(openCellFontGrey);
            finalPage.moveTextPositionByAmount(166, 263);
            finalPage.drawString("or please use the following to contact the StrattoOpencell team:");
            finalPage.endText();       
            
            //drawImage 1 - Bar Chart
            PDImageXObject imageBar6 = PDImageXObject.createFromFile(PDFReportCreator.REPORT_TEMPLATE_DIR + "StrattoOpencellContact.png", openCellDocument);
            // finalPage.drawImage(imageBar6, 65, 43, imageBar6.getWidth()*(0.75f), imageBar6.getHeight()*(0.75f));     // before OC-87
            finalPage.drawImage(imageBar6, 65, 43, imageBar6.getWidth()*(0.28f), imageBar6.getHeight()*(0.28f));    
            
            // print out today's date
            finalPage.beginText();
            finalPage.setFont(fontCG, 10);
            finalPage.setNonStrokingColor(Color.WHITE);
            finalPage.moveTextPositionByAmount(29, 20);
            finalPage.drawString(date);
            finalPage.endText();       
                
            //page number
            finalPage.beginText();
            finalPage.setFont(fontCG, 10);
            finalPage.setNonStrokingColor(Color.WHITE);
            finalPage.moveTextPositionByAmount(925, 20);
            finalPage.drawString(Integer.toString(pageNumber++));
            finalPage.endText();       

            finalPage.close();
            System.out.println("PDF Creation Final Page done");            
            
            //Background, Header, Footer
            HashMap<Integer, String> overlayGuide = new HashMap<Integer, String>();
            for(int i=0; i<openCellDocument.getNumberOfPages(); i++){
                overlayGuide.put(i+1, PDFReportCreator.REPORT_TEMPLATE_DIR + "OpenCellPageTemplate.pdf");
            }
        
            overlay = new Overlay();
            overlay.setInputPDF(openCellDocument);
            overlay.setOverlayPosition(Overlay.Position.BACKGROUND);
            overlay.overlay(overlayGuide);

            File openCellFile = new File(imagesDir + reportName);
            openCellDocument.save(openCellFile);
            success = true;
        } catch (Exception e) {
            System.out.println("\nError writing to PDF report " + reportName + ", " + e.getMessage() + "\n");
            e.printStackTrace();
        } finally {
            try {
                if (titlePage != null) titlePage.close();
                if (callsPage != null) callsPage.close();
                if (downloadPage != null) downloadPage.close();
                if (uploadPage != null) uploadPage.close();
                if (cisPage != null) cisPage.close();
                if (finalPage != null) finalPage.close();
                if (overlay != null) overlay.close();
                openCellDocument.close();
            } catch (Exception ie) {
                System.out.println("Unable to close PDF report " + reportName + ", " + ie.getMessage());               
            }
        }
        return success;
    }          
    
    private String getAvailabilityDesc(float minAvailability) {
        String desc = "Under investigation";
        if (minAvailability == 100) {
            desc = "Perfect!";
        } else if (minAvailability > 99) {
            desc = "Excellent";            
        } else if (minAvailability > 95) {
            desc = "Good";            
        } 
        return desc;
    }

    private void addSiteToCustomerReport(PDDocument openCellDocument, String siteName, String siteId) throws Exception {
        PDPageContentStream siteTitlePage = null;
        PDPageContentStream siteCallsPage = null;
        PDPageContentStream siteDownloadPage = null;
        PDPageContentStream siteUploadPage = null;
        PDPageContentStream siteCisPage = null;

        try { 
            //Page 1, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage sitePage1 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(sitePage1);

            siteTitlePage = new PDPageContentStream(openCellDocument, sitePage1);

            siteTitlePage.beginText();
            siteTitlePage.setFont(fontCG, 48);
            siteTitlePage.setNonStrokingColor(this.openCellFontGrey);
            siteTitlePage.moveTextPositionByAmount(100, 380);
            siteTitlePage.drawString("StrattoOpencell Service Report");
            siteTitlePage.endText();

            siteTitlePage.beginText();
            siteTitlePage.setFont(fontCG, 48);
            siteTitlePage.setNonStrokingColor(this.openCellFontGrey);
            siteTitlePage.moveTextPositionByAmount(100, 320);
            siteTitlePage.drawString(month + " " + year);
            siteTitlePage.endText();

            siteTitlePage.beginText();
            siteTitlePage.setFont(fontCG, 36);
            siteTitlePage.setNonStrokingColor(this.openCellFontGrey);
            siteTitlePage.moveTextPositionByAmount(100, 180);
            siteTitlePage.drawString(siteName + siteId);
            siteTitlePage.endText();

            String date = new SimpleDateFormat("dd/MM/yy").format(new Date());
            siteTitlePage.beginText();
            siteTitlePage.setFont(fontCG, 10);
            siteTitlePage.setNonStrokingColor(Color.WHITE);
            siteTitlePage.moveTextPositionByAmount(29, 20);
            siteTitlePage.drawString(date);
            siteTitlePage.endText();       

            //page number
            siteTitlePage.beginText();
            siteTitlePage.setFont(fontCG, 10);
            siteTitlePage.setNonStrokingColor(Color.WHITE);
            siteTitlePage.moveTextPositionByAmount(925, 20);
            siteTitlePage.drawString(Integer.toString(pageNumber++));
            //siteTitlePage.drawString("1");
            siteTitlePage.endText();  

            //Page 2, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage sitePage2 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(sitePage2);

            siteCallsPage = new PDPageContentStream(openCellDocument, sitePage2);    

            //Page 2 Title 
            siteCallsPage.beginText();
            siteCallsPage.setFont(fontCG, 36);
            siteCallsPage.setNonStrokingColor(openCellFontBlue);
            siteCallsPage.moveTextPositionByAmount(31, 467);
            siteCallsPage.drawString("Traffic Statistics – Voice Calls");
            siteCallsPage.endText();

            //Comment
            siteCallsPage.beginText();
            siteCallsPage.setFont(fontCG, 20);
            siteCallsPage.setNonStrokingColor(openCellFontGrey);
            siteCallsPage.moveTextPositionByAmount(45, 432);
            String strVoiceCalls = DecimalFormat.getNumberInstance().format(creator.siteVoiceCallNums.get(siteName));
            siteCallsPage.drawString("\u2022" + strVoiceCalls + " total number of calls (" + interval+ "" +")");
            siteCallsPage.endText();                

            // print out today's date
            siteCallsPage.beginText();
            siteCallsPage.setFont(fontCG, 10);
            siteCallsPage.setNonStrokingColor(Color.WHITE);
            siteCallsPage.moveTextPositionByAmount(29, 20);
            siteCallsPage.drawString(date);
            siteCallsPage.endText();       

            //page number
            siteCallsPage.beginText();
            siteCallsPage.setFont(fontCG, 10);
            siteCallsPage.setNonStrokingColor(Color.WHITE);
            siteCallsPage.moveTextPositionByAmount(925, 20);
            siteCallsPage.drawString(Integer.toString(pageNumber++));
            //siteCallsPage.drawString("2");
            siteCallsPage.endText();       

            //drawImage 1 - Bar Chart
            PDImageXObject imageSiteBar = PDImageXObject.createFromFile(imagesDir+ "TotalNumCallsBarChart_" + siteName + ".png", openCellDocument);
            float scaleBar = 0.75f;
            siteCallsPage.drawImage(imageSiteBar, 57, 63, imageSiteBar.getWidth()*scaleBar, imageSiteBar.getHeight()*scaleBar);

            //drawImage 2 - Pie Chart
            PDImageXObject imageSitePie = PDImageXObject.createFromFile(imagesDir+ "TotalNumCallsPieChart_" + siteName + ".png", openCellDocument);                
            float scalePie = 0.75f;
            siteCallsPage.drawImage(imageSitePie, 510, 64, imageSitePie.getWidth()*scalePie, imageSitePie.getHeight()*scalePie);

            //System.out.println("PDF Creation Page 2 done");

            //Page 3, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage sitePage3 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(sitePage3);

            siteDownloadPage = new PDPageContentStream(openCellDocument, sitePage3);

            //Page 3 Title 
            siteDownloadPage.beginText();
            siteDownloadPage.setFont(fontCG, 36);
            siteDownloadPage.setNonStrokingColor(openCellFontBlue);
            siteDownloadPage.moveTextPositionByAmount(31, 467);
            siteDownloadPage.drawString("Traffic Statistics – Data Download");
            siteDownloadPage.endText();

            //Comment
            siteDownloadPage.beginText();
            siteDownloadPage.setFont(fontCG, 20);
            siteDownloadPage.setNonStrokingColor(openCellFontGrey);
            siteDownloadPage.moveTextPositionByAmount(45, 432);
            if (downloadNumGb < 1) {
                siteDownloadPage.drawString("\u2022" + " < 1 GB data download (Web browsing, video streaming, file downloads " + interval + ")");
            } else {
                siteDownloadPage.drawString("\u2022" + creator.siteDownloadVols.get(siteName) + "GB data download (Web browsing, video streaming, file downloads " + interval + ")");                
            }
            siteDownloadPage.endText();                

            // print out today's date
            siteDownloadPage.beginText();
            siteDownloadPage.setFont(fontCG, 10);
            siteDownloadPage.setNonStrokingColor(Color.WHITE);
            siteDownloadPage.moveTextPositionByAmount(29, 20);
            siteDownloadPage.drawString(date);
            siteDownloadPage.endText();       

            //page number
            siteDownloadPage.beginText();
            siteDownloadPage.setFont(fontCG, 10);
            siteDownloadPage.setNonStrokingColor(Color.WHITE);
            siteDownloadPage.moveTextPositionByAmount(925, 20);
            siteDownloadPage.drawString(Integer.toString(pageNumber++));
            //siteDownloadPage.drawString("3");
            siteDownloadPage.endText();  

            //drawImage 1 - Bar Chart
            PDImageXObject imageSiteBar2 = PDImageXObject.createFromFile(imagesDir + "DataInboundBarChart_" + siteName + ".png", openCellDocument);
            siteDownloadPage.drawImage(imageSiteBar2, 57, 63, imageSiteBar2.getWidth()*scaleBar, imageSiteBar2.getHeight()*scaleBar);

            //drawImage 2 - Pie Chart
            PDImageXObject imageSitePie2 = PDImageXObject.createFromFile(imagesDir + "DataInboundPieChart_" + siteName + ".png", openCellDocument);                
            siteDownloadPage.drawImage(imageSitePie2, 510, 64, imageSitePie2.getWidth()*scalePie, imageSitePie2.getHeight()*scalePie);

            //System.out.println("PDF Creation Page 3 done");

            //Page 4, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage sitePage4 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(sitePage4);

            siteUploadPage = new PDPageContentStream(openCellDocument, sitePage4);

            //Page 4 Title 
            siteUploadPage.beginText();
            siteUploadPage.setFont(fontCG, 36);
            siteUploadPage.setNonStrokingColor(openCellFontBlue);
            siteUploadPage.moveTextPositionByAmount(31, 467);
            siteUploadPage.drawString("Traffic Statistics – Data Upload");
            siteUploadPage.endText();

            //Comment
            siteUploadPage.beginText();
            siteUploadPage.setFont(fontCG, 20);
            siteUploadPage.setNonStrokingColor(openCellFontGrey);
            siteUploadPage.moveTextPositionByAmount(45, 432);
            if (downloadNumGb < 1) {
                siteUploadPage.drawString("\u2022" + " < 1 GB data upload (Email sending, file uploads, WhatsApp etc " + interval + ")");
            } else {
                siteUploadPage.drawString("\u2022" + creator.siteUploadVols.get(siteName) + "GB data upload (Email sending, file uploads, WhatsApp etc " + interval + ")");
            }
            siteUploadPage.endText();

            // print out today's date
            siteUploadPage.beginText();
            siteUploadPage.setFont(fontCG, 10);
            siteUploadPage.setNonStrokingColor(Color.WHITE);
            siteUploadPage.moveTextPositionByAmount(29, 20);
            siteUploadPage.drawString(date);
            siteUploadPage.endText();       

            //page number
            siteUploadPage.beginText();
            siteUploadPage.setFont(fontCG, 10);
            siteUploadPage.setNonStrokingColor(Color.WHITE);
            siteUploadPage.moveTextPositionByAmount(925, 20);
            siteUploadPage.drawString(Integer.toString(pageNumber++));
            //siteUploadPage.drawString("4");
            siteUploadPage.endText();       

            //drawImage 1 - Bar Chart
            PDImageXObject imageSiteBar3 = PDImageXObject.createFromFile(imagesDir + "DataOutboundBarChart_" + siteName + ".png", openCellDocument);
            siteUploadPage.drawImage(imageSiteBar3, 67, 63, imageSiteBar3.getWidth()*scaleBar, imageSiteBar3.getHeight()*scaleBar);

            //drawImage 2 - Pie Chart
            PDImageXObject imageSitePie3 = PDImageXObject.createFromFile(imagesDir + "DataOutboundPieChart_" + siteName + ".png", openCellDocument);                
            siteUploadPage.drawImage(imageSitePie3, 520, 63, imageSitePie3.getWidth()*scalePie, imageSitePie3.getHeight()*scalePie);

            //Page 5, custom size to match current OpenCell Report - landscape, widescreen 16:9
            PDPage sitePage5 = new PDPage(new PDRectangle(PDRectangle.A4.getHeight()*(1.14f), (PDRectangle.A4.getHeight()*(1.14f)*9/16)));
            openCellDocument.addPage(sitePage5);
        
            siteCisPage = new PDPageContentStream(openCellDocument, sitePage5); 
            
            siteCisPage.beginText();
            siteCisPage.setFont(fontCG, 36);
            siteCisPage.setNonStrokingColor(openCellFontBlue);
            siteCisPage.moveTextPositionByAmount(31, 467);
            siteCisPage.drawString("Availability Statistics – " + month + " " + year);
            siteCisPage.endText();
        
            //Comment
            siteCisPage.beginText();
            siteCisPage.setFont(fontCG, 16);
            siteCisPage.setNonStrokingColor(openCellFontGrey);
            siteCisPage.moveTextPositionByAmount(45, 432);
            siteCisPage.drawString("\u2022" + "Statistics from " + interval);
            siteCisPage.endText();                
            //Comment
            siteCisPage.beginText();
            siteCisPage.setFont(fontCG, 16);
            siteCisPage.setNonStrokingColor(openCellFontGrey);
            siteCisPage.moveTextPositionByAmount(45, 407);
            siteCisPage.drawString("\u2022" + "Availability " + getAvailabilityDesc(siteCisValues.get(siteName)[5]));
            siteCisPage.endText();                
            //Comment
            siteCisPage.beginText();
            siteCisPage.setFont(fontCG, 24);
            siteCisPage.setNonStrokingColor(PDFReportCreator.opcoColours[0]);
            siteCisPage.moveTextPositionByAmount(45, 375);
            //siteCisPage.drawString("O2 99.92%");
            if (siteCisValues.get(siteName)[0] > 0) {
                siteCisPage.drawString("VF " + String.format("%.2f", siteCisValues.get(siteName)[0]) + "%");
            } else {
                siteCisPage.drawString("VF");                
            }
            siteCisPage.endText();                
            
            siteCisPage.beginText();
            siteCisPage.setFont(fontCG, 24);
            siteCisPage.setNonStrokingColor(PDFReportCreator.opcoColours[1]);
            siteCisPage.moveTextPositionByAmount(45, 340);
            if (siteCisValues.get(siteName)[1] > 0) {
                siteCisPage.drawString("O2 " + String.format("%.2f", siteCisValues.get(siteName)[1]) + "%");
            } else {
                siteCisPage.drawString("O2");
            }                
            siteCisPage.endText();

            siteCisPage.beginText();
            siteCisPage.setFont(fontCG, 24);
            siteCisPage.setNonStrokingColor(PDFReportCreator.opcoColours[2]);
            siteCisPage.moveTextPositionByAmount(45, 305);
            if (siteCisValues.get(siteName)[2] > 0) {
                siteCisPage.drawString("THREE " + String.format("%.2f", siteCisValues.get(siteName)[2]) + "%");
            } else {
                siteCisPage.drawString("THREE");
            }
            siteCisPage.endText();                

            siteCisPage.beginText();
            siteCisPage.setFont(fontCG, 24);
            siteCisPage.setNonStrokingColor(PDFReportCreator.opcoColours[3]);
            siteCisPage.moveTextPositionByAmount(45, 270);
            if (siteCisValues.get(siteName)[3] > 0) {
                siteCisPage.drawString("EE " + String.format("%.2f", siteCisValues.get(siteName)[3]) + "%");
            } else {
                siteCisPage.drawString("EE");
            }    
            siteCisPage.endText();                
            
                     
            // print out today's date
            siteCisPage.beginText();
            siteCisPage.setFont(fontCG, 10);
            siteCisPage.setNonStrokingColor(Color.WHITE);
            siteCisPage.moveTextPositionByAmount(29, 20);
            siteCisPage.drawString(date);
            siteCisPage.endText();       
                
            //page number
            siteCisPage.beginText();
            siteCisPage.setFont(fontCG, 10);
            siteCisPage.setNonStrokingColor(Color.WHITE);
            siteCisPage.moveTextPositionByAmount(925, 20);
            siteCisPage.drawString(Integer.toString(pageNumber++));
            //siteCisPage.drawString("5");
            siteCisPage.endText();       

            // Make sure that the content stream is closed:
            siteCisPage.close();            
            
            //System.out.println("PDF Creation Page 4 done");        
        } catch (Exception e) {
            throw e;
        } finally {
            // Make sure that the content streams are closed:            
            try { if (siteTitlePage != null ) siteTitlePage.close(); } catch (Exception ie) {}
            try { if (siteCallsPage != null ) siteCallsPage.close(); } catch (Exception ie) {}
            try { if (siteDownloadPage != null ) siteDownloadPage.close(); } catch (Exception ie) {}
            try { if (siteUploadPage != null ) siteUploadPage.close(); } catch (Exception ie) {}            
        }
    }
    
}
