package mx.ipn.escom.supernaut.mutualexcl.centralized;

import mx.ipn.escom.supernaut.mutualexcl.CSRequest;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class ExclusionServer extends Server {
    Server server;
    Queue<CSRequest> requests;
    Map<Integer, Connection> connections;
    Integer csHolder;


    class ExclusionListener extends Listener {
        public void recieved(Connection connection, Object object) {
            if(object instanceof CSRequest) {
                if(!connections.containsValue(connection)) {
                    connections.put(((CSRequest) object).pid, connection);
                }
                requests.offer((CSRequest) object);
                attendNext();
            } else if(object instanceof CSRelease) {
                if(!connections.containsValue(connection)) {
                    connections.put(((CSRelease) object).pid, connection);
                }
                if(csHolder != ((CSRelease) object).pid) {
                    throw new IllegalArgumentException();
                }
                csHolder = null;
                attendNext();
            }
        }

        public void disconnected(Connection connection) {
            connections.remove(connection);
        }

        void attendNext() throws NoSuchElementException {
            CSRequest next;
            Connection connection;
            next = requests.poll();
            if(next != null && csHolder == null) {
                connection = connections.get(next.pid);
                if(connection == null) throw new NoSuchElementException();
                connection.sendTCP(new CSConcession(next.pid));
                csHolder = next.pid;
            }
        }
    }


    public ExclusionServer() throws IOException {
        csHolder = null;
        requests = new PriorityQueue<>();
        connections = new HashMap<>();
        Network.register(server);
        server.addListener(new ExclusionListener());
        server.bind(Network.port);
        server.start();
    }

    public static void main(String[] args) {
        Log.set(Log.LEVEL_DEBUG);
        try {
            new ExclusionServer();
        } catch(IOException e) {
            System.err.println("error al inicializar servidor");
        }
    }
}
