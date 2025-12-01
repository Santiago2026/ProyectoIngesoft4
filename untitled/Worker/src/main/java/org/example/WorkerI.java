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

    public WorkerI() {
        this.arcoInfo = new HashMap<>(); 
        loadArcs("arcos.csv");
        System.out.println(">> WorkerI inicializado. Arcos cargados para " + arcoInfo.size() + " arcos.");
    }

    private void loadArcs(String arcFile) {
        File file = new File(arcFile);

        if (!file.exists()) {
            System.err.println("ERROR: Archivo de arcos no encontrado");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); 
            String line;
            
            while ((line = br.readLine()) != null) {
                String[] partes = line.split(",");
                if (partes.length < 4) continue;
    
                String paradaFromId = partes[0].trim();
                String paradaToId = partes[1].trim();
                double distancia = Double.parseDouble(partes[2].trim());
                String lineId = partes[3].trim();
                
                String claveBusqueda = lineId + "," + paradaToId;

                ArcoData data = new ArcoData(paradaFromId, distancia);
                arcoInfo.put(claveBusqueda, data);
            }
        } catch (Exception e) {
            System.err.println("ERROR durante la carga de arcos en WorkerI: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Double> calcularVelocidadesPorArco(String[] datagramas, Current current) {

        long startTime = System.nanoTime();

        Map<String, Long> tiemposPorParadaYLinea = new HashMap<>();

        for (String linea : datagramas) { 
            String[] partes = linea.split(",");
            if (partes.length >= 3) {
                String paradaId = partes[0].trim();
                String lineId = partes[1].trim();
                long tiempo = Long.parseLong(partes[2].trim());
                tiemposPorParadaYLinea.put(paradaId + "," + lineId, tiempo);
            }
        }
        
        Map<String, List<Double>> acumuladoVelocidades = new HashMap<>();

        for (String lineaActual : datagramas) { 
            
            String[] partesTo = lineaActual.split(",");
            if (partesTo.length < 3) continue;

            String paradaToId = partesTo[0].trim();
            String lineId = partesTo[1].trim();
            long tiempoTo = Long.parseLong(partesTo[2].trim());

            String arcoKey = lineId + "," + paradaToId;
            ArcoData arcoData = arcoInfo.get(arcoKey);
            
            if (arcoData != null) {
                
                String paradaFromId = arcoData.paradaFromId; 
                double distancia = arcoData.distancia;       

                String keyFrom = paradaFromId + "," + lineId;
                
                if (tiemposPorParadaYLinea.containsKey(keyFrom)) {
                    long tiempoFrom = tiemposPorParadaYLinea.get(keyFrom);
 
                    long diffSegundos = tiempoTo - tiempoFrom;

                    if (diffSegundos > 0) {
                        double diffHoras = (double)diffSegundos / 3600.0;
                        double velocidad = distancia / diffHoras; 

                        String arco = paradaFromId + "-" + paradaToId;
                        acumuladoVelocidades
                            .computeIfAbsent(arco, k -> new ArrayList<>())
                            .add(velocidad);
                    }
                }
            }
        }

        Map<String, Double> promediosFinales = new HashMap<>();
        
        for (Map.Entry<String, List<Double>> entry : acumuladoVelocidades.entrySet()) {
            String arcoId = entry.getKey();
            List<Double> velocidades = entry.getValue();
            
            double suma = 0;
            for (double v : velocidades) {
                suma += v;
            }
            double promedio = suma / velocidades.size();
            promediosFinales.put(arcoId, promedio);
        }

        return promediosFinales;
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
