import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.abs;

/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */

//Interface Used to create a new thread after game finishes.
interface GameThreadCallback {
   void onGameThreadFinish(Socket socket);
}
public class GameServer implements GameThreadCallback {

    private ReentrantLock lock = new ReentrantLock();

    //Stores all clients, it writes and reads from database.
    private Map<String, User> clients =  new HashMap<String, User>();

    //Handles the normal matchmaking queue
    private Queue<User> normal_queue = new LinkedList<User>();

    private GameThreadCallback callback;

    private Map<Socket, Integer> queuePositions = new HashMap<>();

    //Function Used to create a new thread after game finishes. Typically to allow new play after game stoppes.
    @Override
    public void onGameThreadFinish(Socket socket) {
        Thread matchmakingThread = new Thread(() -> {
            try {
                this.handleMatchmaking(socket);
            } catch (IOException exception) {
                System.out.println("Error handling client: " + exception.getMessage());
            }
        });
        matchmakingThread.start();
    }

    //Custom class based on user and time to handle rank matchmaking
    public class User_Rank{
        public User_Rank(User user,int time){
            this.user =user;
            this.time = time;
        }
        public User getUser(){return this.user;}

        public int getTime(){return this.time;}

        public void setTime(int time){this.time=time;}
        private int time;
        private User user;
    }
    Comparator<User_Rank> comparator = new Comparator<User_Rank>() {
          @Override
          public int compare(User_Rank num1, User_Rank num2) {
              return num1.getUser().getRank() - num2.getUser().getRank();
          }
    };

    //Handles the rank queue
    private LinkedList<User_Rank> rank_queue = new LinkedList<User_Rank>();
    /*
    Add the user_rank to the rank queue and sorts it.
     */
    public void add_to_rank(User_Rank obj) {
        this.rank_queue.add(obj);
        sort();
    }
    /*
   Removes the user_rank to the rank queue and sorts it.
     */
    public User_Rank remove_rank(int index) {
        if (index < 0 || index >= this.rank_queue.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        User_Rank removed = this.rank_queue.remove(index);
        sort();
        return removed;
    }

    /*
    Sorts Rank queue.
     */
    private void sort() {
        this.rank_queue.sort(comparator);
    }


    /**
     * This function handles basic matchmaking, order by first user to arrive.
     * @throws IOException
     */
    private void matchmaking_basic() throws IOException {
        while (true) {
            synchronized (this.normal_queue) {
                while (this.normal_queue.size() < 2) {
                    try {
                        this.normal_queue.wait(); // Wait until the queue has at least 2 players
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Once we have at least 2 players, remove them and start the game
                User first = this.normal_queue.remove();
                User second = this.normal_queue.remove();
                System.out.println("Found Game!");
                Thread gameThread = new Thread(() -> {
                    try {
                        TicTacToe ticTacToe = new TicTacToe();
                        ticTacToe.start(first, second, first.getToken(), second.getToken());
                    } catch (IOException exception) {
                        System.out.println("Error handling client: " + exception.getMessage());
                    }
                    finally {
                        this.onGameThreadFinish(first.getToken()); // Call the callback method when the game thread finishes
                        this.onGameThreadFinish(second.getToken());
                    }
                    
                });
                gameThread.start();
            }
        }
    }
    /*
    To put it basically, we are simply applying a function to convert a second interval, to an acceptable rank interval
     */
    private int convert_time_to_rank_acceptable(int seconds){
        //Yeah, so basically until one minute that will be different.
        System.out.println(seconds);
        if(seconds<60){
            return 50;
        }else if(seconds<120){
            return 100;
        }
        else if(seconds<180){
            return 150;
        }
        else if(seconds<240){
            return 200;
        }
        else if(seconds<300){
            return 250;
        }
        else if(seconds<360){
            return 300;
        }
        else{
            return 999999999;
        }

    }

    private int not_zero(int val){
        if(val<=0){
            return 0;
        }
        return val;
    }

    /**
     * This function handles matchmaking ranked, ordered by rank user ascendant.
     * @throws IOException
     */

    private void matchmaking_rank() throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        while (true) {
            synchronized (this.rank_queue) {
                while (this.rank_queue.size() < 2) {
                    try {
                        this.rank_queue.wait(); // Wait until the queue has at least 2 players
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


                // Once we have at least 2 players, remove them and start the game
                for(int i = 0;i<(this.rank_queue.size()-1);i++){
                    var first = this.rank_queue.get(i);
                    var second = this.rank_queue.get(i+1);
                    int diff_acceptable = this.convert_time_to_rank_acceptable((int) System.currentTimeMillis()/1000 - first.getTime());


                    //Checking if it is.
                    if(diff_acceptable>=abs(second.getUser().getRank()-first.getUser().getRank())){
                        this.rank_queue.remove(first);
                        this.rank_queue.remove(second);
                        System.out.println("Found Game!");

                        Future<?> future = executor.submit(() -> {
                            try {
                                TicTacToe ticTacToe = new TicTacToe();
                                return ticTacToe.start(first.getUser(), second.getUser(), first.getUser().getToken(), second.getUser().getToken());
                            } catch (IOException exception) {
                                System.out.println("Error handling client: " + exception.getMessage());
                                return null;
                            }
                            
                            finally {
                                this.onGameThreadFinish(first.getUser().getToken()); // Call the callback method when the game thread finishes
                                this.onGameThreadFinish(second.getUser().getToken());
                            }
                            
                        });

                        try {
                            var returnValue = future.get(); // This will block until the game finishes
                            if (returnValue.equals(second.getUser())){
                                first.getUser().setRank(not_zero(30 + first.getUser().getRank()));
                                second.getUser().setRank(not_zero(- 30 + second.getUser().getRank()));
                            }
                            else if(returnValue.equals(first.getUser())){
                                first.getUser().setRank(not_zero(- 30 + first.getUser().getRank()));
                                second.getUser().setRank(not_zero(30 + second.getUser().getRank()));
                            }
                            this.lock.lock();
                            this.clients.put(first.getUser().getUsername(),first.getUser());
                            this.WriteToUserLogsJson(first.getUser());
                            this.clients.put(second.getUser().getUsername(),second.getUser());
                            this.WriteToUserLogsJson(second.getUser());
                            this.lock.unlock();


                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace(); // Handle the exception appropriately
                        }
                    }
                }
            }
        }
    }

    /**
     * This function handles the connection of the user.
     * @param socket Socket of the user
     * @throws IOException
     */
    public void handleConnection(Socket socket) throws IOException {
            OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                writer.println("Connection Ready, Send it".toString());

                Thread clientThread = new Thread(() -> {
                    try {
                        this.handleClient(socket);
                    } catch (IOException exception) {
                        System.out.println("Error handling client: " + exception.getMessage());
                    }
                });
                clientThread.start();
                /*while(clientThread.isAlive()) {
                    //Just wait to perform the next thread
                }

                    Thread matchmakingThread = new Thread(() -> {
                        try {
                            this.handleMatchmaking(socket);
                        } catch (IOException exception) {
                            System.out.println("Error handling client: " + exception.getMessage());
                        }
                    });
                    matchmakingThread.start();*/


    }

    /**
     * This function simply starts the server and catches any new users trying to connect.
     * @param port
     */
    public void start(int port) {
        // Code to start the game
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);


            while (true) {
                Socket socket = serverSocket.accept();

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String time = reader.readLine();

                System.out.println("New client connected: "+ time);

                this.fillClients();
                /*if(this.normal_queue.size()>=2){
                    User first = this.normal_queue.remove();
                    User second = this.normal_queue.remove();
                    System.out.println("FOUND MATCH!");
                }*/
                try{
                    this.handleConnection(socket);
                }
                catch (IOException ex){
                    System.out.println("Client exception: " + ex.getMessage());
                    ex.printStackTrace();
                }




                //writer.println(new Date().toString());
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * This function basically asks user which gamemode he wants (rank ou simple)
     *
     * @param clientSocket
     * @throws IOException
     */
    private void handleMatchmaking(Socket clientSocket) throws IOException {
        PrintWriter sender = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        User user = findBySocket(clientSocket);
        Socket token = user.getToken();

        while (true) {
            sender.println("Choose the gamemode you want to play: 1 - Simple or 2 - Rank");
            sender.println("Waiting for answer!");
            String gamemode = receiver.readLine();
            if (gamemode.equals("1")) {
                this.lock.lock();
                try {
                    if (!this.queuePositions.containsKey(token)) {
                        int queueSize = this.normal_queue.size();
                        this.queuePositions.put(token, queueSize);
                        this.normal_queue.add(user);
                    } else {
                        int position = this.queuePositions.get(token);
                        List<User> tempList = new ArrayList<>(this.normal_queue);
                        if (position < tempList.size()) {
                            tempList.add(position, user);
                        } else {
                            tempList.add(user);
                        }
                        this.normal_queue.clear();
                        this.normal_queue.addAll(tempList);
                    }
                } finally {
                    this.lock.unlock();
                }
                break;
            } else if (gamemode.equals("2")) {
                this.lock.lock();
                try {
                    boolean found = false;
                    for (User_Rank userRank : this.rank_queue) {
                        if (userRank.getUser().getToken().equals(token)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        this.add_to_rank(new User_Rank(user, (int) System.currentTimeMillis()/1000));
                    }
                } finally {
                    this.lock.unlock();
                }
                break;
            } else {
                sender.println("Gamemode wrong, do it again!");
            }
        }

        synchronized (this.normal_queue) {
            this.normal_queue.notify(); // Notify the matchmaking thread
        }
        synchronized (this.rank_queue) {
            this.rank_queue.notify(); // Notify the matchmaking thread
        }
    }


    /**
     * Auxiliar function to find a user by his socket in clients.
     * @param clientSocket
     * @return
     */
    private User findBySocket(Socket clientSocket){
        for(var e: this.clients.entrySet()){
            if(e.getValue().getToken().equals(clientSocket)){
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * This function reads from UserLogs.json and fills this.clients.
     */
    private void fillClients(){
        JSONParser parser= new JSONParser();
        JSONObject a = null;

        try {
            a = (JSONObject) parser.parse(new FileReader(System.getProperty("user.dir")+"/src/userLogs.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        JSONArray users = (JSONArray) a.get("Users");


        for (Object o : users)
        {
            JSONObject person = (JSONObject) o;

            String username = (String) person.get("username");
            String password = (String) person.get("password");
            int rank = Math.toIntExact((long)person.get("rank"));
            String token = (String) person.get("token");
            Socket socket = new Socket();
            User user = new User(username,password,rank,socket);
            clients.put(username,user);

        }
    }
    /**
     * This function writes a new User to userlogs.json
     * @param user
     */
    private void WriteToUserLogsJson(User user){
        JSONParser parser= new JSONParser();
        JSONObject a = null;
        try {
            a = (JSONObject) parser.parse(new FileReader(System.getProperty("user.dir")+"/src/userLogs.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        boolean found = false;
        JSONArray users = (JSONArray) a.get("Users");
        for (int i = 0; i < users.size(); i++) {
            JSONObject obj= null;
                obj = (JSONObject) users.get(i);
                if(obj.get("username").equals(user.getUsername()))
                {
                    found = true;
                    obj.put("password", user.getPassword());
                    obj.put("rank", user.getRank());
                    obj.put("token", giveRandomToken());
                    break;
                }
        }
        if(!found){
            JSONObject user_json  = new JSONObject();
            user_json.put("username",user.getUsername());
            user_json.put("password",user.getPassword());
            user_json.put("rank",user.getRank());
            user_json.put("token",giveRandomToken());



            // Add the new user object to the array
            users.add(user_json);
        }


        a.put("Users",users);
            try (FileWriter fileWriter = new FileWriter(System.getProperty("user.dir")+"/src/userLogs.json")) {
                fileWriter.write(a.toString()); // Use 4 spaces for indentation
                System.out.println("User added successfully.");
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
    }
    /**
     * This function generates a random token.
     * @return
     */
    public String giveRandomToken() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 20;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
        .limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();

        return generatedString;
    }
    /**
     * This function gets a username and a password and checks, in clients data, if there is a user with that specific password.
     * @param username
     * @param password
     * @return boolean true, if found, or false if not.
     */
    private boolean VerifyLogin(String username, String password){
        for(int i = 0;i<(this.clients.size());i++){
            if(this.clients.get(username).getPassword().equals(password)){
                return true;
            }
        }
        return false;
    }

    /**
     * This function handles each User's authentication
     * @param clientSocket
     * @throws IOException
     */
    public void handleClient(Socket clientSocket) throws IOException {
        
        PrintWriter sender = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader receiver = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


        // Register Or Login
        System.out.println("here");
        sender.println("Need to Perform Login/Register, insert your Username:");
        sender.println("Waiting for answer!");
        String username = receiver.readLine();
        System.out.println("New username: "+ username);
        User newUser;
        if (!this.clients.containsKey(username)) {
            sender.println("Password: ");
            sender.println("Waiting for answer!");
            String password = receiver.readLine();
            newUser = new User(username,password,0,clientSocket);
            this.lock.lock();
            this.clients.put(username,newUser);
            this.WriteToUserLogsJson(newUser);
            this.lock.unlock();
            System.out.println("New User Created!");
        } else {
            sender.println("There is a account registered into this username, please insert password: ");
            sender.println("Waiting for answer!");
            String password = receiver.readLine();
            this.lock.lock();
            User user = this.clients.get(username);
            this.lock.unlock();
            if(VerifyLogin(username,password)){
                newUser = new User(username,password,this.clients.get(username).getRank(),clientSocket);
                this.lock.lock();
                this.clients.put(username,newUser);
                this.lock.unlock();
            }
            else{
                while(true){
                    sender.println("Failed login, please re-enter password: ");
                    sender.println("Waiting for answer!");
                    password = receiver.readLine();
                    this.lock.lock();
                    user = this.clients.get(username);
                    this.lock.unlock();
                    if(VerifyLogin(username,password)){
                        this.lock.lock();
                        newUser = new User(username,password,this.clients.get(username).getRank(),clientSocket);
                        this.clients.put(username,newUser);
                        this.lock.unlock();
                        break;
                    }
                    else{
                        continue;
                    }

                }
            }


            System.out.println("New User Created!");

        }
        this.handleMatchmaking(clientSocket);
        /*sender.println("Choose the gamemode you want to play: 1 - Simple or 2 - Rank");
         String gamemode = receiver.readLine();
         if(!gamemode.equals("1")){
             throw new IOException("Not Implemented Yet!");
         }
         else {
             this.normal_queue.add(newUser);
         }*/
        /*if(!gamemode.equals("1")){
            throw new IOException("Not Implemented Yet!");
        }
        else{
        while(true){
            if(this.normal_queue.size()>=2){
                User first = this.normal_queue.remove();
                User second = this.normal_queue.remove();
                if(first.equals(newUser)){
                    opponent = second;
                }
                else{
                    opponent = first;
                }
                System.out.println("FOUND MATCH!");
            }
        }
        }*/

        /**String clientInput;
        int clientNumber;
        while ((clientInput = receiver.readLine()) != null) {
            System.out.println(clientInput);
            clientNumber = Integer.parseInt(clientInput);
           // partialSum += clientNumber;
            // sender.println(partialSum);
        }

        this.lock.lock();
        //this.clients.put(clientName, partialSum);
        int totalSum = 0;
        for (int value : this.clients.values()) {
            totalSum += value;
        }
        sender.println(totalSum);
        this.lock.unlock();*/
    }

    public static void main(String[] args) {
        if (args.length < 1) return;
 
        int port = Integer.parseInt(args[0]);


        GameServer gameServer = new GameServer();
        Thread matchmakingBasic = new Thread(() -> {
            try {
                gameServer.matchmaking_basic();
            } catch (IOException exception) {
                System.out.println("Error handling client: " + exception.getMessage());
            }
        });
        matchmakingBasic.start();
        Thread matchmakingRank = new Thread(() -> {
            try {
                gameServer.matchmaking_rank();
            } catch (IOException exception) {
                System.out.println("Error handling client: " + exception.getMessage());
            }
        });
        matchmakingRank.start();
        gameServer.start(port);

    }
}
