package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import SITM.QueueServicePrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import SITM.WorkerPrx;

public class Server {
    private static List<WorkerPrx> workers = new ArrayList<>();
    private static QueueServicePrx queueService = null;
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

            ObjectAdapter queueAdapter = ic.createObjectAdapterWithEndpoints(
                    "QueueAdapter", "tcp -h localhost -p 6000"
            );


            QueueServiceI queueServiceObj = new QueueServiceI();


            queueAdapter.add(queueServiceObj, Util.stringToIdentity("queue"));
            queueAdapter.activate();


            queueService = QueueServicePrx.checkedCast(
                    queueAdapter.createProxy(Util.stringToIdentity("queue"))
            );

            // Añadimos nuestro objeto de servicio al adapter

            ServiceI serviceObj = new ServiceI(workers,queueService);
            System.out.println("Working directory: " + new File(".").getAbsolutePath());
            File f1 = new File("linestops-241.csv");
            File f2 = new File("stops-241.csv");

            System.out.println("¿linestops.csv existe? " + f1.exists());
            System.out.println("¿stops.csv existe? " + f2.exists());
            System.out.println("Ruta 1: " + f1.getAbsolutePath());
            System.out.println("Ruta 2: " + f2.getAbsolutePath());
            Scanner sc = new Scanner(System.in);
            System.out.println("¿Desea regenerar arcos?");
            System.out.println("0 = No (cargar archivo existente)");
            System.out.println("1 = Sí (recalcular desde CSV)");
            System.out.print("Ingrese opción: ");

            int opcion = sc.nextInt();
            if (opcion == 1) {
                serviceObj.generateArcs(null);
            } else {
                System.out.println("Cargando arcos existentes...");
                serviceObj.generateArcs(null);
            }

            adapter.add(serviceObj, Util.stringToIdentity("service"));
            adapter.activate();

            System.out.println("SERVICE listo...");
            for (var ep : adapter.getEndpoints()) {
                System.out.println("Endpoint -> " + ep);
            }

            ic.waitForShutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
