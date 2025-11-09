package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import entidades.Temperatura;

public class TemperaturaServicio {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("d/M/yyyy");

    public static List<Temperatura> getDatos(String nombreArchivo) {
        try (Stream<String> lineas = Files.lines(Paths.get(nombreArchivo))) {
            return lineas.skip(1)
                    .map(String::trim)
                    .filter(l -> !l.isEmpty())
                    .map(linea -> linea.split(","))
                    .map(cols -> new Temperatura(
                            cols[0].trim(),
                            LocalDate.parse(cols[1].trim(), FORMATO_FECHA),
                            Double.parseDouble(cols[2].trim())))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public static List<Temperatura> filtrarPorRango(List<Temperatura> datos, LocalDate desde, LocalDate hasta) {
        return datos.stream()
                .filter(t -> !(t.getFecha().isBefore(desde) || t.getFecha().isAfter(hasta)))
                .collect(Collectors.toList());
    }

    public static Map<String, Double> promedioPorCiudad(List<Temperatura> datos) {
        return datos.stream()
                .collect(Collectors.groupingBy(
                        Temperatura::getCiudad,
                        Collectors.averagingDouble(Temperatura::getTemperatura)));
    }

    public static Map<String, Map.Entry<String, Double>> extremosPorFecha(List<Temperatura> datos, LocalDate fecha) {
        Map<String, Double> porCiudad = datos.stream()
                .filter(t -> t.getFecha().equals(fecha))
                .collect(Collectors.groupingBy(
                        Temperatura::getCiudad,
                        Collectors.averagingDouble(Temperatura::getTemperatura)));

        if (porCiudad.isEmpty()) return null;

        Map.Entry<String, Double> max = porCiudad.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        Map.Entry<String, Double> min = porCiudad.entrySet().stream().min(Map.Entry.comparingByValue()).orElse(null);

        return Map.of("max", max, "min", min);
    }
}
