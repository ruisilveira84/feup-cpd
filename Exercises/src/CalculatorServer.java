import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
 
/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
public class CalculatorServer {
    private ReentrantLock lock = new ReentrantLock();
    private Map<String, Integer> clients =  new HashMap<String, Integer>();
 
    public void handleClient(Socket clientSocket) throws IOException {
        
        PrintWriter sender = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String clientName = clientSocket.getInetAddress().getHostName();
        System.out.println("here!");

        int partialSum;
        this.lock.lock();
        if (this.clients.containsKey(clientName)) {
            partialSum = this.clients.get(clientName);
        } else {
            this.clients.put(clientName, 0);
            partialSum = 0;
            
        }
        this.lock.unlock();

        String clientInput;
        int clientNumber;
        while ((clientInput = receiver.readLine()) != null) {
            System.out.println(clientInput);
            clientNumber = Integer.parseInt(clientInput);
            partialSum += clientNumber;
            sender.println(partialSum);
        }

        this.lock.lock();
        this.clients.put(clientName, partialSum);
        int totalSum = 0;
        for (int value : this.clients.values()) {
            totalSum += value;
        }
        sender.println(totalSum);
        this.lock.unlock();
    }
    public static void main(String[] args) {
        if (args.length < 1) return;
 
        int port = Integer.parseInt(args[0]);
 
        try (ServerSocket serverSocket = new ServerSocket(port)) {
 
            System.out.println("Server is listening on port " + port);
 
            while (true) {
                Socket socket = serverSocket.accept();
 
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
                String time = reader.readLine();

                System.out.println("New client connected: "+ time);

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                writer.println("Connection Ready, Send it".toString());

                Thread clientThread = new Thread(() -> {
                    try {
                        CalculatorServer server = new CalculatorServer();
                        server.handleClient(socket);
                    } catch (IOException exception) {
                        System.out.println("Error handling client: " + exception.getMessage());
                    }
                });
                clientThread.start();
 
                //writer.println(new Date().toString());
            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}