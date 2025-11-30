module SITM {

    interface ClientCallback {
        void onFinished(string resultado);
        void onError(string mensaje);
    };

    dictionary<string, double> Velocidades;

    interface Worker {
        Velocidades calcularVelocidadesPorArco(string chunk);
    };

    interface Service {
        void registrarWorker(Worker* w);
        void solicitarCalculoAsync(string datagrama, ClientCallback* cb);
    };

    interface QueueService {
            void enqueue(string msg);
            string dequeue();
            int size();
    };
};
