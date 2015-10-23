package mx.ipn.escom.supernaut.mutualexcl.election;


import mx.ipn.escom.supernaut.mutualexcl.DistributedProcess;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementación del algoritmo de exclusión mutua distribuída por
 * elección.
 *
 */
public class ElectionProcess extends DistributedProcess {
    class Request {
        Integer pid;
        Integer time;

        Request(int pid, int time) {
            this.pid = pid;
            this.time = time;
        }

        public int compareTo(Request other) {
            return this.time.compareTo(other.time);
        }
    }

    final class ElectionThread extends DistributedProcess.AlgorithmThread {
        final short totalPeers;
        final boolean absolute;
        final InetSocketAddress group;
        int pid;
        short votes;
        Lock lock;
        boolean available;
        MulticastSocket socket;
        Queue<Request> requests;

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
            Request request;
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
                        requests.offer(new Request(Integer.parseInt(query[1]),
                                                   Integer.parseInt(query[2])));
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
                        for(Request r : requests) {
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
            System.err.println("error al inicializar hilo de algoritmos");
        }
    }

    public ElectionProcess(String groupName, int groupPort, short totalPeers,
            boolean absolute) {
        work = new WorkThread();
        try {
            algorithm = new ElectionThread(groupName, groupPort, totalPeers,
                                           absolute);
        } catch(IOException ex) {
            System.err.println("error al inicializar hilo de algoritmos");
        }
    }

    public static void main(String[] args) {
        ElectionProcess process;
        if(args.length < 3) {
            System.out.println("uso: ElectionProcess <groupName> <groupPort>" +
                               " <totalPeers> <absolute>");
            return;
        } else if(args.length > 3) {
            process = new ElectionProcess(args[0], Integer.parseInt(args[1]),
                                          Short.parseShort(args[2]));
        } else {
            process = new ElectionProcess(args[0], Integer.parseInt(args[1]),
                                          Short.parseShort(args[2]),
                                          Boolean.parseBoolean(args[3]));
        }
    }
}
