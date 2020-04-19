package com.dataduct.invobroker.ranmatereports.servicereports;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zorana
 */
import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import be.quodlibet.boxable.*;
import be.quodlibet.boxable.line.LineStyle;
import be.quodlibet.boxable.text.WrappingFunction;

public class SimpleTable {

    public static void main (String[] args) throws Exception {
        String outputFileName = "/temp/PDFBoxExample/SimpleTable.pdf";
        if (args.length > 0)
            outputFileName = args[0];

        // Create a new font object selecting one of the PDF base fonts
        PDFont fontPlain = PDType1Font.HELVETICA;
        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        PDFont fontItalic = PDType1Font.HELVETICA_OBLIQUE;
        PDFont fontMono = PDType1Font.COURIER;

        // Create a document and add a page to it
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        // PDRectangle.LETTER and others are also possible
        PDRectangle rect = page.getMediaBox();
        // rect can be used to get the page width and height
        document.addPage(page);

        // Start a new content stream which will "hold" the to be created content
        PDPageContentStream cos = new PDPageContentStream(document, page);

        //Dummy Table
        float margin = 50;
        // starting y position is whole page height subtracted by top and bottom margin
        float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
        // we want table across whole page width (subtracted by left and right margin ofcourse)
        float tableWidth = page.getMediaBox().getWidth() - (2 * margin);

        boolean drawContent = true;
        float yStart = yStartNewPage;
        float bottomMargin = 70;
        // y position is your coordinate of top left corner of the table
        float yPosition = 550;

        BaseTable table = new BaseTable(yPosition, yStartNewPage,
            bottomMargin, tableWidth, margin, document, page, true, drawContent);

        // the parameter is the row height
        Row<PDPage> headerRow = table.createRow(50);
        // the first parameter is the cell width
        Cell<PDPage> cell = headerRow.createCell(100, "Header");
        cell.setFont(fontBold);
        cell.setFontSize(20);
        // vertical alignment
        cell.setValign(VerticalAlignment.MIDDLE);
        // border style
        cell.setTopBorderStyle(new LineStyle(Color.BLACK, 10));
        table.addHeaderRow(headerRow);

        Row<PDPage> row = table.createRow(20);
        cell = row.createCell(30, "black left plain");
        cell.setFontSize(15);
        cell = row.createCell(70, "black left bold");
        cell.setFontSize(15);
        cell.setFont(fontBold);

        row = table.createRow(20);
        cell = row.createCell(50, "red right mono");
        cell.setTextColor(Color.RED);
        cell.setFontSize(15);
        cell.setFont(fontMono);
        // horizontal alignment
        cell.setAlign(HorizontalAlignment.RIGHT);
        cell.setBottomBorderStyle(new LineStyle(Color.RED, 5));
        cell = row.createCell(50, "green centered italic");
        cell.setTextColor(Color.GREEN);
        cell.setFontSize(15);
        cell.setFont(fontItalic);
        cell.setAlign(HorizontalAlignment.CENTER);
        cell.setBottomBorderStyle(new LineStyle(Color.GREEN, 5));

        row = table.createRow(20);
        cell = row.createCell(40, "rotated");
        cell.setFontSize(15);
        // rotate the text
        cell.setTextRotated(true);
        cell.setAlign(HorizontalAlignment.RIGHT);
        cell.setValign(VerticalAlignment.MIDDLE);
        // long text that wraps
        cell = row.createCell(30, "long text long text long text long text long text long text long text");
        cell.setFontSize(12);
        // long text that wraps, with more line spacing
        cell = row.createCell(30, "long text long text long text long text long text long text long text");
        cell.setFontSize(12);
        cell.setLineSpacing(2);

        table.draw();

        float tableHeight = table.getHeaderAndDataHeight();
        System.out.println("tableHeight = "+tableHeight);

        // close the content stream
        cos.close();

        // Save the results and ensure that the document is properly closed:
        document.save(outputFileName);
        document.close();
    }
}