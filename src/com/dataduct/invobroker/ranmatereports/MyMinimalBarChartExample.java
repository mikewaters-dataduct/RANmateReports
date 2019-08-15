/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author zorana
 */
package com.dataduct.invobroker.ranmatereports;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import java.awt.Color;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.io.File;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;

public class MyMinimalBarChartExample {

    public static void main(String[] args) {
        double vfValue = 7500;
        double o2Value = 3800;
        double threeValue = 0;
        double eeValue = 150;
        
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();  
        dataset.addValue(vfValue, "VF", "");
        dataset.addValue(o2Value, "O2", "");
        dataset.addValue(threeValue, "THREE", "");
        dataset.addValue(eeValue, "EE", "");

        JFreeChart barChart = ChartFactory.createBarChart("", "", "MBytes", dataset, 
                PlotOrientation.VERTICAL, true, false, false);
           
        ChartPanel chartPanel = new ChartPanel(barChart);        
        //chartPanel.setPreferredSize(new java.awt.Dimension(560 , 367));  
        chartPanel.setBackground(Color.white);
        barChart.getLegend().setFrame(BlockBorder.NONE);
        
        CategoryPlot plot = (CategoryPlot)barChart.getPlot();
        plot.setBackgroundPaint(Color.white);

        //set  bar chart color
        ((BarRenderer)plot.getRenderer()).setBarPainter(new StandardBarPainter());
        BarRenderer renderer = (BarRenderer)barChart.getCategoryPlot().getRenderer();
        plot.setBackgroundPaint(Color.white);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(Color.gray);
             
        renderer.setSeriesPaint(0, new Color(255,36,36));
        renderer.setSeriesPaint(1, new Color(42,137,192));
        renderer.setSeriesPaint(2, new Color(182,95,194));
        renderer.setSeriesPaint(3, new Color(43,172,177));
        renderer.setShadowVisible(false);
        barChart.getLegend().setFrame(BlockBorder.NONE);
        
         File barChartImage = new File("/temp/PDFBoxExample/jfreeBartest.png");
        //Save chart as PNG
        try{
            ChartUtilities.saveChartAsPNG(barChartImage, barChart, 450, 450);
        } catch (Exception e) {
            System.out.println("Exception when saving PieChart as png");
        }

        /*JFrame f = new JFrame("Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new ChartPanel(barChart) {
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
