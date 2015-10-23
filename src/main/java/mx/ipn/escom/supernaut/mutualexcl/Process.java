package mx.ipn.escom.supernaut.mutualexcl;


import java.lang.Thread;

/**
 * Proceso abstracto del sistema distribuido. Ejecuta un mecanismo de
 * exclusión mutua por implementar. A la vez, y en otro hilo simula la
 * una función que ocupa a ratos la región crítica. Éste pregunta
 * constantemente si se requiere el acceso a la región crítica. De
 * requerirse, llama al mecanismo de exclusión y al tener el recurso,
 * lo bloquea hasta que el usuario indique.
 *
 */
public abstract class Process {
    protected WorkThread work;
    protected AlgorithmThread algorithm;
    protected boolean stopped;

    protected int clock();


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
                    System.out.print("enter para liberar ");
                    scanner.nextLine();
                    algorithm.csFreed();
                }
                if(answer.indexOf('x') > 0) {
                    stopped = true;
                }
            }
        }
    }


    abstract class AlgorithmThread extends Thread {
        public void csRequested();
        public void csFreed();
        public void run();
    }

    public void run();
}
