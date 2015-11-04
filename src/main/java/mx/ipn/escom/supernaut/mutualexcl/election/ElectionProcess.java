package mx.ipn.escom.supernaut.mutualexcl.election;


import mx.ipn.escom.supernaut.mutualexcl.CSRequest;
import mx.ipn.escom.supernaut.mutualexcl.DistributedProcess;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementación del algoritmo de exclusión mutua distribuída por
 * elección.
 *
 */
public class ElectionProcess extends DistributedProcess {
    final class ElectionThread extends DistributedProcess.AlgorithmThread {
        final short totalPeers;
        final boolean absolute;
        final InetSocketAddress group;
        int pid;
        short votes;
        boolean available;
        MulticastSocket socket;
        Queue<CSRequest> requests;

        long clock() {
            return System.currentTimeMillis() / 1000l;
        }

        public void csRequested() {
            String content;
            DatagramPacket packet;
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
                                        group);
            try {
                socket.send(packet);
            } catch (IOException ex) {
                System.err.println("error al enviar aviso de adquisición");
            }
        }

        public void csFreed() {
            final String content;
            DatagramPacket packet;
            content = "RELEASE";
            packet = new DatagramPacket(content.getBytes(), content.length(),
                                        group);
            try {
                socket.send(packet);
            } catch (IOException ex) {
                System.err.println("error al enviar aviso de liberación");
            }
        }

        public void run() {
            DatagramPacket packet;
            byte[] buffer;
            String[] query;
            String reply;
            CSRequest request;
            buffer = new byte[512];
            lock.lock();
            while(!stopped) {
                packet = new DatagramPacket(buffer, 512, group);
                try {
                    socket.receive(packet);
                } catch (IOException ex) {
                    System.err.println("error al enviar aviso de liberación");
                }
                query = (new String(buffer).trim()).split(" ");
                switch(query[0]) {
                    case "OK":
                        if(Integer.parseInt(query[1]) == pid) {
                            votes += 1;
                            if(absolute) {
                                if(votes >= totalPeers-1) {
                                    votes = 0;
                                    lock.unlock();
                                }
                            } else {
                                if(votes >= totalPeers/2+1) {
                                    votes = 0;
                                    lock.unlock();
                                }
                            }
                        }
                        break;
                    case "REQUEST":
                        requests.offer(new CSRequest(Integer.parseInt(query[1]), 
                                                     Long.parseLong(query[2]))
                                      );
                        if(available) {
                            request = requests.poll();
                            if(request != null) {
                                reply = "OK " + request.pid;
                            }
                        }
                        break;
                    case "RELEASE":
                        available = true;
                        request = requests.poll();
                        if(request != null) {
                            reply = "OK " + request.pid;
                            packet = new DatagramPacket(reply.getBytes(),
                                                        reply.length(),
                                                        group);
                            try {
                                socket.send(packet);
                            } catch (IOException ex) {
                                System.err.println("error al enviar voto");
                            }
                        }
                        break;
                    case "TAKEOVER":
                        available = false;
                        for(CSRequest r : requests) {
                            if(r.pid == Integer.parseInt(query[1])) {
                                requests.remove(r);
                            }
                        }
                        break;
                }
            }
            socket.close();
        }

        public ElectionThread(String groupName, int groupPort,
                short totalPeers) throws IOException {
            InetAddress groupAddr;
            votes = 0;
            absolute = false;
            available = false;
            this.totalPeers = totalPeers;
            requests = new PriorityQueue<>();
            lock = new ReentrantLock();
            work = new WorkThread();
            socket = new MulticastSocket();
            pid = socket.getLocalAddress().hashCode();
            groupAddr = InetAddress.getByName(groupName);
            socket.joinGroup(groupAddr);
            group = new InetSocketAddress(groupAddr, groupPort);
        }

        public ElectionThread(String groupName, int groupPort,
                short totalPeers, boolean absolute) throws IOException {
            InetAddress groupAddr;
            this.totalPeers = totalPeers;
            this.absolute = absolute;
            available = false;
            requests = new PriorityQueue<>();
            lock = new ReentrantLock();
            votes = 0;
            work = new WorkThread();
            socket = new MulticastSocket();
            pid = socket.getLocalAddress().hashCode();
            groupAddr = InetAddress.getByName(groupName);
            socket.joinGroup(groupAddr);
            group = new InetSocketAddress(groupAddr, groupPort);
        }
    }


    public ElectionProcess(String groupName, int groupPort, short totalPeers) {
        work = new WorkThread();
        try {
            algorithm = new ElectionThread(groupName, groupPort, totalPeers);
        } catch(IOException ex) {
            System.err.println("error al inicializar hilo del algoritmo");
        }
    }

    public ElectionProcess(String groupName, int groupPort, short totalPeers,
            boolean absolute) {
        work = new WorkThread();
        try {
            algorithm = new ElectionThread(groupName, groupPort, totalPeers,
                                           absolute);
        } catch(IOException ex) {
            System.err.println("error al inicializar hilo del algoritmo");
        }
    }

    public static void main(String[] args) {
        ElectionProcess process;
        if(args.length < 3) {
            System.out.println("uso: ElectionProcess <groupName> <groupPort>" +
                               " <totalPeers> <absolute>");
            return;
        }
        if(args.length > 3) {
            process = new ElectionProcess(args[0], Integer.parseInt(args[1]),
                                          Short.parseShort(args[2]));
        } else {
            process = new ElectionProcess(args[0], Integer.parseInt(args[1]),
                                          Short.parseShort(args[2]),
                                          Boolean.parseBoolean(args[3]));
        }
        process.run();
    }
}
