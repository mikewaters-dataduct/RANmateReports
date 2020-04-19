package com.dataduct.invobroker.ranmatereports.servicereports;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;
import be.quodlibet.boxable.line.LineStyle;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
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
public class PdfServiceReportCommon {
    static public boolean addDatetoSlideFooter(PDPageContentStream contentPage, PDFont fontCG) {
        //date
        Date today = Calendar.getInstance().getTime();
        // print out today's date
        String date = new SimpleDateFormat("dd/MM/yy").format(new Date());
        try {
            contentPage.beginText();
            contentPage.setFont(fontCG, 10);
            contentPage.setNonStrokingColor(Color.WHITE);
            contentPage.moveTextPositionByAmount(29, 20);
            contentPage.drawString(date);
            contentPage.endText();
        return true;
        } catch (Exception e){
            return false;
        }
    }

    static public boolean addPageNoToSlideFooter(PDPageContentStream contentPage, PDFont fontCG, int noPage) {
        try {
            //page number
            contentPage.beginText();
            contentPage.setFont(fontCG, 10);
            contentPage.setNonStrokingColor(Color.WHITE);
            contentPage.moveTextPositionByAmount(925, 20);
            contentPage.drawString(Integer.toString(noPage));
            contentPage.endText();
        return true;
        } catch (Exception e){
           return false;
        }
    }

    static public boolean addTextToPage(PDPageContentStream contentPage, PDFont font, float fontSize, Color fontColor, int xTopLeft, int yTopLeft, String text) {
        try {
        contentPage.beginText();
        contentPage.setFont(font, fontSize);
        contentPage.setNonStrokingColor(fontColor);
        contentPage.moveTextPositionByAmount(xTopLeft, yTopLeft);
        contentPage.drawString(text);
        contentPage.endText();
        return true;
        } catch (Exception e){
           return false;
        }
    }

    static public Color getRedOrGreen(String target, String actual){
        String targetSign = target.substring(0, 2);
        //System.out.println("Target sign " + targetSign);

        String targetValue = target.substring(2, target.length() - 1);
        //System.out.println("Target value " + targetValue + ", Actual: = " + actual);

        double targetValueInt = Double.valueOf(targetValue);
        double actualValueInt = Double.valueOf(actual);
        //System.out.println("Doubles: " + targetValueInt + ", " + actualValueInt);

        if ( (targetSign.equalsIgnoreCase(">=") && actualValueInt < targetValueInt)
            || (targetSign.equalsIgnoreCase("<=") && actualValueInt > targetValueInt)) {
                return PdfServiceReportConfig.statusRed;
            } else {
                return PdfServiceReportConfig.statusGreen;
            }

    }

    static public boolean addDarkGreyHeader(BaseTable table, PDFont fontCG, String title) {
        Row<PDPage> headerRow = table.createRow(10);
        Cell<PDPage> cell = headerRow.createCell(100, title);
        cell.setFont(fontCG);
        cell.setFontSize(11);
        cell.setTextColor(Color.WHITE);
        cell.setTopPadding(2);
        cell.setTopPadding(1);
        cell.setFillColor(PdfServiceReportConfig.darkGreyHeader);
        // vertical alignment
        cell.setValign(VerticalAlignment.MIDDLE);
        cell.setAlign(HorizontalAlignment.CENTER);
        // brder style
        cell.setBorderStyle(new LineStyle(Color.WHITE, 1));
        return true;
    }

}
