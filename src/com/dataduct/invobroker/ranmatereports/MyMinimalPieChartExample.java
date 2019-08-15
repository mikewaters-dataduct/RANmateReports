/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dataduct.invobroker.ranmatereports;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import java.awt.Color;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import java.text.DecimalFormat;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.block.BlockBorder;
import org.jfree.data.general.PieDataset;

/**
 *
 * @author zorana
 */
public class MyMinimalPieChartExample {

    public static void main(String[] args) {
        double vfValue = 7500;
        double o2Value = 3800;
        double threeValue = 0;
        double eeValue = 150;
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("VF", vfValue);
        dataset.setValue("O2", o2Value);
        dataset.setValue("THREE", threeValue);
        dataset.setValue("EE", eeValue);

        JFreeChart pieChart = ChartFactory.createPieChart(
            "", dataset, true, false, false);
        PiePlot plot = (PiePlot) pieChart.getPlot();
 
        plot.setBackgroundPaint(null);
        plot.setInteriorGap(0.04);
        plot.setSectionPaint("VF", new Color(255,36,36));
        plot.setSectionPaint("O2", new Color(42,137,192));
        plot.setSectionPaint("THREE", new Color(182,95,194));
        plot.setSectionPaint("EE", new Color(43,172,177));
        plot.setOutlineVisible(false);

        plot.setShadowPaint(Color.WHITE);
        plot.setBaseSectionOutlinePaint(Color.white);
        plot.setSectionOutlinesVisible(true);
        plot.setBaseSectionOutlineStroke(new BasicStroke(2.0f));
        plot.setSimpleLabels(true);
        plot.setLegendItemShape(new Rectangle(10,10));
        
        plot.setLabelFont(new Font("Arial", Font.BOLD, 14));
        plot.setLabelOutlineStroke(null);
        plot.setLabelPaint(Color.WHITE);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelShadowPaint(null);
        pieChart.getLegend().setFrame(BlockBorder.NONE);

        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator(
            "{2}", new DecimalFormat("0"), new DecimalFormat("0%")){
            @Override
            public String generateSectionLabel(PieDataset dataset, Comparable key) {
                if (dataset.getValue(key) == null || dataset.getValue(key).intValue() == 0) {
                    return null;
                }
                return super.generateSectionLabel(dataset, key);
            }
        };
        plot.setLabelGenerator(gen);
        
        File pieChartImage = new File("/temp/PDFBoxExample/jfreePietest.png");
        //Save chart as PNG
        try{
            ChartUtilities.saveChartAsPNG(pieChartImage, pieChart, 450, 450);
        } catch (Exception e) {
            System.out.println("Exception when saving PieChart as png");
        }

        /*JFrame f = new JFrame("Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new ChartPanel(pieChart) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(400, 300);
            }
        });
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);*/
    }
}