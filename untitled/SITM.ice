module SITM {

    sequence<string> StringList;
    dictionary<string, double> Velocidades;

    interface ClientCallback {
        void onFinished(string resultado);
        void onError(string mensaje);
    };

    interface Worker {
        Velocidades calcularVelocidadesPorArco(string chunk);
    };

    interface Service {
        void registrarWorker(Worker* w);
        void solicitarCalculoAsync(string datagrama, ClientCallback* cb);
        void generateArcs();
        StringList getArcos();
    };

    interface QueueService {
            void enqueue(string msg);
            string dequeue();
            int size();
    };
};
