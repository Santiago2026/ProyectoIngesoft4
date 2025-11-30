package org.example;

import java.util.Map;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class WorkerMain {
    public static void main(String[] args) {
        try (Communicator ic = Util.initialize(args)) {
            System.out.println("Worker corriendo...");
            double distanciaArcoA = 10.0; // ejemplo de distancia
            double distanciaArcoB = 15.0; 
            Map<String, Double> distancias = Map.of(
                "ArcoA", distanciaArcoA,
                "ArcoB", distanciaArcoB
            );
            ObjectAdapter adapter = ic.createObjectAdapter("WorkerAdapter");
            adapter.add(new WorkerI(distancias ), Util.stringToIdentity("worker"));
            adapter.activate();

            ic.waitForShutdown();
        }
    }
}
