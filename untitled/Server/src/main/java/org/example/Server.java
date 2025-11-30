package org.example;

import java.util.ArrayList;
import java.util.List;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import SITM.WorkerPrx;

public class Server {
    private static List<WorkerPrx> workers = new ArrayList<>();
    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();
        
        try (Communicator ic = Util.initialize(args, "config.server", extraArgs)) {

            if (!extraArgs.isEmpty()) {
                System.err.println("Demasiados argumentos:");
                for (String v : extraArgs) {
                    System.out.println(v);
                }
            }

            // Creamos el adapter del Server 
            ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints(
                "ServiceAdapter", "tcp -h localhost -p 5000"
            );

            // Añadimos nuestro objeto de servicio al adapter
            ServiceI serviceObj = new ServiceI(workers);
            adapter.add(serviceObj, Util.stringToIdentity("service"));
            adapter.activate();

            System.out.println("SERVICE listo...");
            ic.waitForShutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
