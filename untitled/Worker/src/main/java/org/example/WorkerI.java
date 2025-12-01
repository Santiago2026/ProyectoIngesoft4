package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zeroc.Ice.Current;
import SITM.Worker;

public class WorkerI implements Worker {

    private final Map<String, Double> distancias;

    public WorkerI(Map<String, Double> distancias) {
        this.distancias = distancias;
    }

    @Override
    public Map<String, Double> calcularVelocidadesPorArco(String[] datagramas, Current current) {

        Map<String, List<Double>> acumuladoPorArco = new HashMap<>();

        for (String linea : datagramas) {
            if (linea.trim().isEmpty()) continue;

            // Suponiendo formato CSV: arco,bus,tiempoEntrada,tiempoSalida
            String[] partes = linea.split(",");
            if (partes.length < 4) continue;

            String arco = partes[0].trim();
            long entrada = Long.parseLong(partes[2].trim());
            long salida  = Long.parseLong(partes[3].trim());

            if (salida <= entrada) continue;

            double distancia = distancias.getOrDefault(arco, 0.0);
            if (distancia == 0) continue;

            double tiempoHoras = (salida - entrada) / 3600.0;
            double velocidad = distancia / tiempoHoras;

            acumuladoPorArco
                .computeIfAbsent(arco, k -> new ArrayList<>())
                .add(velocidad);
        }

        Map<String, Double> resultado = new HashMap<>();
        for (String arco : acumuladoPorArco.keySet()) {
            List<Double> velocidades = acumuladoPorArco.get(arco);
            double promedio = velocidades.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            resultado.put(arco, promedio);
        }

        return resultado;
    }
}
