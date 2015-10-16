package mx.ipn.escom.supernaut.mutualexcl;

import java.lang.Thread;
import java.util.concurrent.locks.Lock;

/**
 * Proceso abstracto del sistema distribuido. Ejecuta un mecanismo de
 * exclusión mutua por implementar. A la vez, pregunta constantemente
 * si se requiere el acceso a la región crítica. De requerirse, llama
 * al mecanismo de exclusión y al tener el recurso, lo bloquea hasta
 * que el usuario indique.
 *
 */
public abstract class Process {
    protected int logicalClock;
    protected Lock lock;
    protected WorkThread work;
    protected AlgorithmThread algorithm;
    protected boolean stopped;


    class WorkThread extends Thread {
        Scanner scanner;

        public WorkThread() {
            scanner = new Scanner(System.in);
            stopped = false;
        }

        public void run() {
            String answer;
            while(!stopped) {
                System.out.print("¿necesito región crítica? (y/n/x) ");
                answer = scanner.nextLine();
                if(answer.indexOf('y') > 0) {
                    algorithm.csRequested();
                    lock.lock();
                    System.out.print("enter para liberar ");
                    scanner.nextLine();
                    lock.unlock();
                }
                if(answer.indexOf('x') > 0) {
                    stopped = true;
                }
                logicalClock++;
            }
        }
    }


    abstract class AlgorithmThread extends Thread {
        public void csRequested();
        public void run();
    }

    public void start();
}
