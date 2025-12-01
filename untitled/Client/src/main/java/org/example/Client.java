package org.example;

import java.util.Scanner;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import SITM.ClientCallbackPrx;
import SITM.ServicePrx;
    
public class Client {
    public static void main(String[] args) {

        try (Communicator ic = Util.initialize(args)) {

            ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints(
                "ClientAdapter", "tcp -h  192.168.131.102 -p 9090"
            );

            ClientCallbackI cbObj = new ClientCallbackI();
            ClientCallbackPrx cbPrx = ClientCallbackPrx.uncheckedCast(
                adapter.addWithUUID(cbObj)
            );
            adapter.activate();

            ServicePrx service = ServicePrx.checkedCast(
                ic.stringToProxy("service:tcp -h 192.168.131.101 -p 5000")
            );

            if (service == null) {
                throw new Error("Cliente → No se pudo obtener el proxy del Service");
            }

            Scanner sc = new Scanner(System.in);

            System.out.println("¿Desea calcular velocidad promedio?");
            System.out.println("0 = No");
            System.out.println("1 = Sí");
            System.out.print("Ingrese opción: ");
            int opcion = sc.nextInt();
            
            if (opcion == 1) {
                service.solicitarCalculoAsync(cbPrx);
            }
            System.out.println("Cliente → solicitud enviada (async).");

            ic.waitForShutdown();
        }
    }
}
