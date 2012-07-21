package rbl.monitoring.suhu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

/**
 *
 * @author Puradidjaja
 */
public class Grafik extends ApplicationFrame {

    private TimeSeries series;
    private double lastValue = 100.0;
    JFreeChart chart;

    public Grafik(final String title) {
        super(title);
        this.series = new TimeSeries("Suhu", Millisecond.class);
        final TimeSeriesCollection dataset = new TimeSeriesCollection(this.series);
        chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        final JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        setContentPane(content);
    }

    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart("Grafik Suhu","Waktu","°Celsius",
                                  dataset,true,true,false);
        final XYPlot plot = result.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60000.0);  
        axis = plot.getRangeAxis();
        axis.setRange(0.0, 50.0);
        return result;
    }

    public void setData(double x) {
        this.lastValue = x;
        this.series.add(new Millisecond(), this.lastValue);
    }
}