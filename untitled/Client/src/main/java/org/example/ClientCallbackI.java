package org.example;

import SITM.*;
import com.zeroc.Ice.Current;

public class ClientCallbackI implements ClientCallback {

    @Override
    public void onFinished(String resultado, Current current) {
        System.out.println("Resultado recibido: " + resultado);
    }

    @Override
    public void onError(String mensaje, Current current) {
        System.err.println("Error recibido: " + mensaje);
    }
}