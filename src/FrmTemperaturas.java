import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import datechooser.beans.DateChooserCombo;
import entidades.Temperatura;
import servicios.TemperaturaServicio;

public class FrmTemperaturas extends JFrame {

    private DateChooserCombo dccDesde, dccHasta, dccFechaEspecifica;
    private JTabbedPane tp;
    private JPanel pnlGrafica, pnlEstadisticas;
    private List<Temperatura> datos;

    public FrmTemperaturas() {

        setTitle("Análisis de Temperaturas por Ciudad");
        setSize(800, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JToolBar tb = new JToolBar();

        JButton btnGraficar = new JButton();
        btnGraficar.setIcon(new ImageIcon(getClass().getResource("/iconos/Grafica.png")));
        btnGraficar.setToolTipText("Graficar promedio por ciudad en rango");
        btnGraficar.addActionListener((ActionEvent evt) -> btnGraficarClick());
        tb.add(btnGraficar);

        JButton btnClima = new JButton();
        btnClima.setIcon(new ImageIcon(getClass().getResource("/iconos/Temperatura.png")));
        btnClima.setToolTipText("Calcular ciudad más calurosa y más fría para una fecha");
        btnClima.addActionListener((ActionEvent evt) -> btnExtremosClick());
        tb.add(btnClima);

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));

        JPanel pnlControles = new JPanel();
        pnlControles.setPreferredSize(new Dimension(pnlControles.getWidth(), 60));
        pnlControles.setLayout(null);

        JLabel lblDesde = new JLabel("Desde:");
        lblDesde.setBounds(10, 10, 50, 25);
        pnlControles.add(lblDesde);

        dccDesde = new DateChooserCombo();
        dccDesde.setBounds(60, 10, 120, 25);
        pnlControles.add(dccDesde);

        JLabel lblHasta = new JLabel("Hasta:");
        lblHasta.setBounds(200, 10, 50, 25);
        pnlControles.add(lblHasta);

        dccHasta = new DateChooserCombo();
        dccHasta.setBounds(240, 10, 120, 25);
        pnlControles.add(dccHasta);

        JLabel lblFecha = new JLabel("Fecha específica:");
        lblFecha.setBounds(380, 10, 110, 25);
        pnlControles.add(lblFecha);

        dccFechaEspecifica = new DateChooserCombo();
        dccFechaEspecifica.setBounds(490, 10, 120, 25);
        pnlControles.add(dccFechaEspecifica);

        pnlGrafica = new JPanel();
        JScrollPane spGraf = new JScrollPane(pnlGrafica);

        pnlEstadisticas = new JPanel();

        tp = new JTabbedPane();
        tp.addTab("Gráfica", spGraf);
        tp.addTab("Caliente y Fria", pnlEstadisticas);

        pnlMain.add(pnlControles);
        pnlMain.add(tp);

        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(pnlMain, BorderLayout.CENTER);

        cargarDatos();
    }

    private void cargarDatos() {
        String archivo = System.getProperty("user.dir") + "/src/datos/Temperaturas.csv";
        datos = TemperaturaServicio.getDatos(archivo);
    }

    private void btnGraficarClick() {
        var desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        var hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(this, "Rango de fechas inválido.");
            return;
        }

        var filtrados = TemperaturaServicio.filtrarPorRango(datos, desde, hasta);
        if (filtrados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos en el rango seleccionado.");
            return;
        }

        Map<String, Double> promedios = TemperaturaServicio.promedioPorCiudad(filtrados);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        promedios.forEach((ciudad, valor) -> dataset.addValue(valor, "Promedio", ciudad));

        JFreeChart chart = ChartFactory.createBarChart(
                "Promedio de temperatura por ciudad",
                "Ciudad",
                "Temperatura (°C)",
                dataset);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(750, 380));

        pnlGrafica.removeAll();
        pnlGrafica.setLayout(new BorderLayout());
        pnlGrafica.add(chartPanel, BorderLayout.CENTER);
        pnlGrafica.revalidate();
        pnlGrafica.repaint();
    }

    private void btnExtremosClick() {
        LocalDate fecha = dccFechaEspecifica.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        var resultados = TemperaturaServicio.extremosPorFecha(datos, fecha);
        if (resultados == null) {
            JOptionPane.showMessageDialog(this, "No hay datos para la fecha " + fecha);
            return;
        }

        var max = resultados.get("max");
        var min = resultados.get("min");

        pnlEstadisticas.removeAll();
        pnlEstadisticas.add(new JLabel("Fecha: " + fecha));
        pnlEstadisticas.add(new JLabel("Más Calurosa: " + max.getKey() + "  (" + String.format("%.2f", max.getValue()) + " °C)"));
        pnlEstadisticas.add(new JLabel("Más Fría: " + min.getKey() + "  (" + String.format("%.2f", min.getValue()) + " °C)"));
        pnlEstadisticas.revalidate();
        pnlEstadisticas.repaint();

        tp.setSelectedIndex(1);
    }
}
