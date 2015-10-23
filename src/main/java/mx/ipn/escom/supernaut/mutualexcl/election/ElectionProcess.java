package mx.ipn.escom.supernaut.mutualexcl.election;


import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementación del algoritmo de exclusión mutua distribuída por
 * elección.
 *
 */
public class ElectionProcess extends Process {
    protected final short totalPeers;
    protected final boolean absolute;
    protected final InetAddress group;
    protected int pid;
    protected short votes;
    protected Lock lock;
    protected MulticastSocket socket;
    protected Queue requests;

    protected long clock() {
        return System.currentTimeMillis() / 1000l;
    }


    class ElectionThread extends Process.AlgorithmThread {
        public void csRequested() {
            String content;
            DatagramPacket packet;
            byte[512] buffer;
            content = "REQUEST " + pid + " " + clock();
            packet = new DatagramPacket(content.getBytes(),
                                        content.length(), group);
            try {
                socket.send(packet);
            } catch (IOException ex) {
                System.err.println("error al enviar petición");
            }
            lock.lock();
            content = "TAKEOVER " + pid;
            packet = new DatagramPacket(content.getBytes(), content.length(),
                                        group)
            try {
                socket.send(packet);
            } catch (IOException ex) {
                System.err.println("error al enviar aviso de adquisición");
            }
        }

        public void csFreed() {
            final String content;
            DatagramPacket packet;
            byte[512] buffer;
            content = "RELEASE";
            packet = new DatagramPacket(content.getBytes(), content.length(),
                                        group);
            try {
                socket.send(packet);
            } catch (IOException ex) {
                System.err.println("error al enviar aviso de liberación");
            }
            lock.unlock();
        }

        public void run() {
            DatagramPacket packet;
            byte[512] buffer;
            String[] content_arr;
            while(!stopped) {
                packet = new DatagramPacket(buffer, 512, group);
                socket.receive(packet);
                content_arr = (new String(buffer).trim()).split(" ");
                switch(content_arr[0]) {
                    case "OK":
                        if(Integer.parseInt(content_arr[1]) == pid) {
                            votes += 1;
                            if(absolute) {
                                if(votes >= totalPeers-1) {
                                    lock.unlock()
                                }
                            } else {
                                if(votes >= totalPeers/2+1) {
                                    lock.unlock()
                                }
                            }
                        }
                        break;
                    case "RELEASE":
                        lock.unlock();
                    break;
                    case "REQUEST":
                    break;
                    case "TAKEOVER":
                        lock.lock();
                    break;
                }
            }
        }
    }

    public ElectionProcess(String group_name, int totalPeers) {
        lock = new ReentrantLock();
        votes = 0;
        work = new WorkThread();
        group = InetAddress.getByName(group_name);
        socket = new MulticastSocket();
        socket.joinGroup(group);
        pid = socket.getLocalAddress().hashCode();
    }

    public void run() {
        csStuff.start();
        algorithm.start();
        csStuff.join();
        algorithm.join();
        socket.close();
    }
}
