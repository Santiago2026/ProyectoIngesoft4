package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

            ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints(
                "ServiceAdapter", "tcp -h localhost -p 5000"
            );

            Scanner sc = new Scanner(System.in);
            System.out.println("¿Desea regenerar arcos?");
            System.out.println("0 = No (cargar archivo existente)");
            System.out.println("1 = Sí (recalcular desde CSV)");
            System.out.print("Ingrese opción: ");

            // Añadimos nuestro objeto de servicio al adapter
            ServiceI serviceObj = new ServiceI(workers);
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
            ic.waitForShutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
