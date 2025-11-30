package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zeroc.Ice.Current;

import SITM.ClientCallbackPrx;
import SITM.Service;
import SITM.WorkerPrx;

public class ServiceI implements Service {

    private List<WorkerPrx> workers = new ArrayList<>();

    @Override
    public void registrarWorker(WorkerPrx w, Current current) {
        System.out.println("Service → Worker registrado: " + w);
        workers.add(w);
    }

    @Override
    public void solicitarCalculoAsync(String datagrama, SITM.ClientCallbackPrx cb, Current current) {
        // tu implementación real aquí
        System.out.println("Service → Solicitud async recibida");
    }

    public ServiceI(List<WorkerPrx> workers) {
         this.workers = workers;
     }

//     @Override
//     public void solicitarCalculoAsync(String datagrama,
//                                   ClientCallbackPrx cb,
//                                   Current current)
//         {
//             List<String> partes = dividirDatasetPorWorkers(datagrama, workers.size());

//             List<Map<String, Double>> resultadosParciales = new ArrayList<>();

//             // for (int i = 0; i < partes.size(); i++) {
//             //     WorkerPrx worker = workers.get(i);       // Worker correspondiente al pedazo
//             //     String parte = partes.get(i);       // Dataset asignado a ese Worker

//             //     Map<String, Double> parcial = worker.calcularVelocidadesPorArco(parte);
//             //     resultadosParciales.add(parcial);
//             // }

//             Map<String, Double> resultadoFinal = combinarVelocidades(resultadosParciales);

//             cb.onFinished( serializarResultado(resultadoFinal) );
//     }

//     private List<String> dividirDatasetPorWorkers(String datagrama, int numWorkers) {
//     String[] lineas = datagrama.split("\n");
//     int total = lineas.length;

//     int chunkSize = (int) Math.ceil((double) total / numWorkers);

//     List<String> partes = new ArrayList<>();

//     for (int i = 0; i < total; i += chunkSize) {
//         int fin = Math.min(i + chunkSize, total);
//         String chunk = String.join("\n", Arrays.copyOfRange(lineas, i, fin));
//         partes.add(chunk);
//     }

//     return partes;
// }

//     private Map<String, Double> combinarVelocidades(List<Map<String, Double>> parciales) {
//         Map<String, List<Double>> acumulado = new HashMap<>();

//         for (Map<String, Double> parcial : parciales) {
//             for (String arco : parcial.keySet()) {
//                 acumulado
//                     .computeIfAbsent(arco, k -> new ArrayList<>())
//                     .add(parcial.get(arco));
//             }
//         }

//         Map<String, Double> resultadoFinal = new HashMap<>();

//         for (String arco : acumulado.keySet()) {
//             List<Double> velocidades = acumulado.get(arco);
//             double promedio = velocidades.stream().mapToDouble(Double::doubleValue).average().orElse(0);
//             resultadoFinal.put(arco, promedio);
//         }

//         return resultadoFinal;
//     }

//     private String serializarResultado(Map<String, Double> resultadoFinal) {
//         StringBuilder sb = new StringBuilder();
//         sb.append("{");

//         int i = 0;
//         int size = resultadoFinal.size();

//         for (Map.Entry<String, Double> entry : resultadoFinal.entrySet()) {
//             sb.append("\"").append(entry.getKey()).append("\": ")
//             .append(entry.getValue());

//             if (i < size - 1) sb.append(", ");
//             i++;
//         }

//         sb.append("}");
//         return sb.toString();
//     }
}
