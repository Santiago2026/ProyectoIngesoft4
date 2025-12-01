package org.example;

import java.util.Map;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import SITM.ServicePrx;
import SITM.WorkerPrx;
public class WorkerMain {
    public static void main(String[] args) {
        try (Communicator ic = Util.initialize(args)) {
            System.out.println("Worker corriendo...");
            ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints("WorkerAdapter", "tcp -p 6003 -h 192.168.131.107");
            adapter.add(new WorkerI(), Util.stringToIdentity("worker"));
            adapter.activate();
           
            ServicePrx service = SITM.ServicePrx.checkedCast(
                ic.stringToProxy("service:tcp -h 192.168.131.101 -p 5000")
            );
            if (service != null) {
                WorkerPrx workerPrx = WorkerPrx.checkedCast(ic.stringToProxy("worker:tcp -h 192.168.131.107 -p 6003"));
                service.registrarWorker(workerPrx);
                System.out.println("Worker registrado en el Service!");
            } else {
                System.out.println("No se pudo conectar al Service");
            }
            ic.waitForShutdown();
        }
    }
}
