package org.example;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import SITM.ClientCallbackPrx;
import SITM.ServicePrx;
    
public class Client {
    public static void main(String[] args) {

        try (Communicator ic = Util.initialize(args)) {

            ObjectAdapter adapter = ic.createObjectAdapterWithEndpoints(
                "ClientAdapter", "tcp -h localhost -p 9090"
            );

            ClientCallbackI cbObj = new ClientCallbackI();
            ClientCallbackPrx cbPrx = ClientCallbackPrx.uncheckedCast(
                adapter.addWithUUID(cbObj)
            );
            adapter.activate();

            ServicePrx service = ServicePrx.checkedCast(
                ic.stringToProxy("service:tcp -h localhost -p 5000")
            );

            if (service == null) {
                throw new Error("Cliente → No se pudo obtener el proxy del Service");
            }
            int option = 1; // Cambia este valor para probar diferentes opciones

            String datagrama = "ejemplo123"; // ← Aquí va lo que quieras procesar


            for (int i = 0; i < 10; i++) {
                service.solicitarCalculoAsync("msg" + i, cbPrx);
                System.out.println("Enviado msg" + i);
            }



            System.out.println("Cliente → solicitud enviada (async).");

            ic.waitForShutdown();
        }
    }
}
