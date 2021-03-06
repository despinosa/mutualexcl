package mx.ipn.escom.supernaut.mutualexcl.centralized;

import mx.ipn.escom.supernaut.mutualexcl.DistributedProcess;
import mx.ipn.escom.supernaut.mutualexcl.CSRequest;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class ClientProcess extends DistributedProcess {
    int pid;


    class ClientListener extends Listener {
        public void recieved(Connection connection, Object object) {
            if(object instanceof CSConcession) { /*
                if(CSConcession.pid != pid) {
                    throw new IllegalArgumentException();
                } */
                lock.unlock();
            }
        }

        public void disconnected(Connection conection) {
            System.err.println("servidor desconectado");
            stopped = true;
        }
    }


    final class ClientThread extends DistributedProcess.AlgorithmThread {
        Client client;

        long clock() {
            return System.currentTimeMillis() / 1000l;
        }

        public void csRequested() {
            client.sendTCP(new CSRequest(pid, clock()));
            lock.lock();
        }

        public void csFreed() {
            client.sendTCP(new CSRelease(pid));
        }

        public void run() {
            while(!stopped) {}
            client.close();
        }

        public ClientThread(String host) throws IOException {
            lock = new ReentrantLock();
            client = new Client();
            client.start();
            Network.register(client);
            client.addListener(new ClientListener());
            client.connect(5000, host, Network.port);
        }
    }


    public ClientProcess(String host) {
        work = new WorkThread();
        try {
            algorithm = new ClientThread(host);
        } catch(IOException e) {
            System.err.println("error al inicializar hilo del algoritmo");
        }
    }

    public static void main(String[] args) {
        ClientProcess process;
        if(args.length < 1) {
            System.out.println("uso: ClientProcess <host>");
            return;
        }
        process = new ClientProcess(args[0]);
        process.run();
    }
}
