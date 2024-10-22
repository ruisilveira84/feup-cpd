import java.net.*;
import java.util.Scanner;
import java.io.*;
 
/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class CalculatorClient {
 
    public static void main(String[] args) {
        if (args.length < 2) return;
 
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
 
        try (Socket socket = new Socket(hostname, port)) {
 
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("Iniciate Connection".toString());

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            String time = reader.readLine();
            String answer;
            
            if(time.equals("Connection Ready, Send it")){
                while (true){
                    System.out.println("Send Number:");
                    Scanner myScanner = new Scanner(System.in);
                    String number = myScanner.nextLine();
                    int numInt;
                    try{
                        numInt = Integer.parseInt(number);
                    }
                    catch (NumberFormatException ex){
                        System.out.println("Not a number, try again!");
                        continue;
                    }
                    writer.println(number.toString());
                    
                    input = socket.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(input));

                    answer = reader.readLine();
                    System.out.println("The partial sum is: " + answer);
                }
                

            }
 
 
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}