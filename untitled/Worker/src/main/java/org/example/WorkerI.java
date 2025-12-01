package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zeroc.Ice.Current;
import SITM.Worker;

public class WorkerI implements Worker {

    private final Map<String, ArcoData> arcoInfo;
    
    private final String ARC_FILE = "arcos.csv";
    private List<String> arcs = new ArrayList<>();


    public WorkerI(Map<String, Double> distancias) {
        this.distancias = distancias;
    }

    private void loadEdgeCSV() {
        File file = new File(ARC_FILE);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // header

            arcs.clear();
            while ((line = br.readLine()) != null) {
                arcs.add(line.trim()); // Cada línea representa un arco y su distancia
            }
        } catch (Exception e) {
            System.out.println("ERROR cargando arcos: " + e.getMessage());
        }
    }

    @Override
    public long calcularVelocidadesPorArco(String[] datagramas, Current current) {

        long startTime = System.nanoTime();
        Map<String, Long> tiemposPorParadaYLinea = new HashMap<>();

        Map<String, List<Double>> acumuladoPorArco = new HashMap<>();
        
        //Ver linea por linea 
        //selecciona la primer linea y busca lineId sean igual y la parada, mira su secuencia, 
        // Busca la secuencia -1 en la misma linea
        // distancia / (tiempo - tiempo)
        // Lista resultado
        //enviar lista al server

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

        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }
}

class ArcoData {
    public final String paradaFromId;
    public final double distancia;
    public ArcoData(String paradaFromId, double distancia) {
        this.paradaFromId = paradaFromId;
        this.distancia = distancia;
    }
}
