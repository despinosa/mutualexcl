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
    protected int pid;
    protected MulticastSocket socket;
    protected final InetAddress group;
    protected Queue requests;


    class ElectionThread extends Process.AlgorithmThread {
        public void csRequested() {
            String content;
            DatagramPacket packet;
            byte[512] buffer;
            if(lock.isLocked()) {
                content = "REQUEST " + pid + " " + logicalClock;
                packet = DatagramPacket(content.getBytes(), content.length(),
                                        group);
                try {
                    socket.send(packet);
                } catch (IOException ex) {
                    System.err.println("error al enviar petición");
                    return;
                }
            }
        }

        public void run() {
            DatagramPacket packet;
            byte[512] buffer;
            String[] content_arr;
            while(!stopped) {
                packet = DatagramPacket(buffer, 512, group);
                socket.receive(packet);
                content = new String(buffer).trim();
                if(content.equals("OK " + pid)) {
                    lock.unlock();
                } else if(content.startsWith("REQUEST "))
            }
        }
    }

    public ElectionProcess() {
        lock = new ReentrantLock();
        work = new WorkThread();
    }

    public void start() {
        csStuff.start();
        algorithm.start();
        csStuff.join();
        algorithm.join();
        socket.close();
    }
}
