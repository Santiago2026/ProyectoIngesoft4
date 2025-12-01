package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

import SITM.QueueServicePrx;
import com.zeroc.Ice.Current;

import SITM.ClientCallbackPrx;
import SITM.Service;
import SITM.WorkerPrx;

public class ServiceI implements Service {


    private final QueueServicePrx queue;
    private final String ARC_FILE = "arcos.csv";
    private List<WorkerPrx> workers = new ArrayList<>();
    private List<String> arcs = new ArrayList<>();

    @Override
    public void registrarWorker(WorkerPrx w, Current current) {
        System.out.println("Service → Worker registrado: " + w);
        workers.add(w);
    }

    public ServiceI(List<WorkerPrx> workers, QueueServicePrx queue) {
        this.queue = queue;
        this.workers = workers;
    }

    @Override
    public void solicitarCalculoAsync(ClientCallbackPrx cb, Current current) {
        System.out.println("Service → Solicitud async recibida");

        long start = System.currentTimeMillis();

        if (workers.isEmpty()) {
            System.out.println("No hay workers registrados!");
            cb.onFinished("{}");
            return;
        }

        // Cargar datagramas desde CSV
        List<String> datagrama = cargarDatagramas();

        // Dividir los datagramas en partes para cada worker
        List<String[]> partes = dividirDatasetPorWorkers(datagrama, workers.size());

        List<Map<String, Double>> resultadosParciales = Collections.synchronizedList(new ArrayList<>());
        List<Thread> hilos = new ArrayList<>();

        // Enviar cada parte a su worker correspondiente
        for (int i = 0; i < partes.size(); i++) {
            int idx=i;

            Thread t = new Thread(() -> {
                try {
                    WorkerPrx worker = workers.get(idx);
                    String[] parte = partes.get(idx);
                    Map<String, Double> parcial = worker.calcularVelocidadesPorArco(parte);
                    resultadosParciales.add(parcial);  // synchronizedList evita problemas de concurrencia
                } catch (Exception e) {
                    System.out.println("Error con worker " + idx + ": " + e.getMessage());
                }
            });
            hilos.add(t);
            t.start();
        }

        for (Thread t : hilos) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Combinar resultados parciales en un resultado final
        Map<String, Double> resultadoFinal = combinarVelocidades(resultadosParciales);
        cb.onFinished(serializarResultado(resultadoFinal));

        long end = System.currentTimeMillis();
        System.out.println("Tiempo total de ejecución: " + (end - start) + " ms");
    }

    private List<String> cargarDatagramas() {
        List<String> res = new ArrayList<>(); 
        try (BufferedReader br = new BufferedReader(new FileReader("datagramas.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                res.add(line.trim());
            }
        } catch (Exception e) {
            System.out.println("ERROR cargando datagramas: " + e.getMessage());
        }
        return res;
    }


    private List<String[]> dividirDatasetPorWorkers(List<String> dataset, int numWorkers) {
        int total = dataset.size();
        int chunkSize = (int) Math.ceil((double) total / numWorkers);

        List<String[]> partes = new ArrayList<>();
        for (int i = 0; i < total; i += chunkSize) {
            int fin = Math.min(i + chunkSize, total);
            List<String> sublist = dataset.subList(i, fin);
            partes.add(sublist.toArray(new String[0])); // <-- ya es String[]
        }
        return partes;
    }

     private Map<String, Double> combinarVelocidades(List<Map<String, Double>> parciales) {
        Map<String, List<Double>> acumulado = new HashMap<>();

        for (Map<String, Double> parcial : parciales) {
            for (String arco : parcial.keySet()) {
                acumulado.computeIfAbsent(arco, k -> new ArrayList<>()).add(parcial.get(arco));
            }
        }

        Map<String, Double> resultadoFinal = new HashMap<>();
        for (String arco : acumulado.keySet()) {
            List<Double> velocidades = acumulado.get(arco);
            double promedio = velocidades.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            resultadoFinal.put(arco, promedio);
        }

        return resultadoFinal;
    }


    private String serializarResultado(Map<String, Double> resultadoFinal) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        int i = 0;
        int size = resultadoFinal.size();

        for (Map.Entry<String, Double> entry : resultadoFinal.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\": ")
            .append(entry.getValue());

            if (i < size - 1) sb.append(", ");
            i++;
        }

        sb.append("}");
        return sb.toString();
    }

    // Generar o cargar arcos con distancias
    @Override
    public void generateArcs(Current current) {
        initArcs();
        System.out.println("Service → Arcos generados o cargados.");
    }
    // Obtener lista de arcos
    @Override
    public String[] getArcos(Current current) {
        return arcs.toArray(new String[0]);
    }
    // Carga los arcos desde un archivo CSV
    private void loadEdgeCSV(File file) {
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
    // Guarda los arcos generados en un archivo CSV
    private void guardarArcosCSV(File file) {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("lineId,from,to,distance"); // header
            for (String arc : arcs) {
                pw.println(arc);
            }
        } catch (Exception e) {
            System.out.println("ERROR guardando CSV de arcos: " + e.getMessage());
        }
    }
    // Inicializa los arcos, cargándolos desde archivo o generándolos
    public void initArcs() {
        File f = new File(ARC_FILE);

        if (f.exists()) {
            System.out.println(">> Cargando arcos desde archivo...");
            loadEdgeCSV(f);
        } else {
            System.out.println(">> Generando arcos con distancia...");
            generarArcosConDistancia("linestops-241.csv", "stops-241.csv");
            guardarArcosCSV(f);
        }
        
    }
    // Carga stops desde CSV y devuelve mapa <stopId, [lat, lon]>
    private Map<Integer, double[]> cargarStops(String path) {
        Map<Integer, double[]> map = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine(); // header

            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");

                int stopId = Integer.parseInt(p[0].trim());
                double lon = Double.parseDouble(p[6].trim().replace("\"", ""));
                double lat = Double.parseDouble(p[7].trim().replace("\"", ""));

                map.put(stopId, new double[]{lat, lon});
            }

        } catch (Exception e) {
            System.out.println("ERROR cargando stops: " + e.getMessage());
        }

        return map;
    }
    // Genera arcos con distancia entre stops y los guarda en 'arcs'
    private void generarArcosConDistancia(String lineStopsPath, String stopsPath) {

        Map<Integer, double[]> stops = cargarStops(stopsPath);

        Map<Integer, List<Integer>> rutas = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(lineStopsPath))) {

            String line = br.readLine(); // header

            while ((line = br.readLine()) != null) {

                String[] p = line.split(",");

                int lineId = Integer.parseInt(p[3].trim());
                int stopId = Integer.parseInt(p[4].trim());

                rutas.computeIfAbsent(lineId, k -> new ArrayList<>()).add(stopId);
            }

        } catch (Exception e) {
            System.out.println("ERROR leyendo lineStops: " + e.getMessage());
        }

        // Recorrer rutas y calcular distancias entre stops consecutivos
        for (var entry : rutas.entrySet()) {
            List<Integer> lista = entry.getValue();

            for (int i = 0; i < lista.size() - 1; i++) {
                int from = lista.get(i);
                int to = lista.get(i + 1);
                int lineId = entry.getKey();
                double[] sf = stops.get(from);
                double[] st = stops.get(to);


                if (sf != null && st != null && sf != st) {
                    double dist = distanciaHaversine(sf[0], sf[1], st[0], st[1]);

                    arcs.add(lineId +","+from + "," + to + "," + dist);
                }
            }
        }

        System.out.println(">> Arcos generados: " + arcs.size());
    }
    // devuelve distancia en metros
    private double distanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000; // radio de la tierra en metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }

}
