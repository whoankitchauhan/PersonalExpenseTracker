import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChartManager {

    public static void showCategoryPieChart(String username) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        String sql = "SELECT category, SUM(amount) AS total FROM expenses WHERE username = ? GROUP BY category";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dataset.setValue(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (Exception e) {
            System.out.println("Error loading chart: " + e.getMessage());
            return;
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Expense Breakdown by Category",
                dataset,
                true,
                true,
                false);

        // Enhance pie chart appearance
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null); // Flat design

        ChartPanel panel = new ChartPanel(chart);
        JFrame frame = new JFrame("Expense Chart");
        frame.setContentPane(panel);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void showMonthlyBarChart(String username) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String sql = "SELECT DATE_FORMAT(date, '%Y-%m') AS month, SUM(amount) AS total " +
                "FROM expenses WHERE username = ? GROUP BY month ORDER BY month ASC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String month = rs.getString("month");
                double total = rs.getDouble("total");
                dataset.addValue(total, "Expenses", month);
            }
        } catch (Exception e) {
            System.out.println("Error generating bar chart: " + e.getMessage());
            return;
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Monthly Expenses",
                "Month",
                "Total Spent",
                dataset);

        // Enhance bar chart appearance
        CategoryPlot plot = (CategoryPlot) barChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189)); // Modern blue
        renderer.setDrawBarOutline(false);
        renderer.setBarPainter(new StandardBarPainter());

        ChartPanel chartPanel = new ChartPanel(barChart);
        JFrame frame = new JFrame("Monthly Expense Chart");
        frame.setContentPane(chartPanel);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
