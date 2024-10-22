import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TicTacToe {
    private char[][] board;

    private Socket first_player;

    private Socket second_player;

    public TicTacToe() {
        board = new char[3][3];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '-';
            }
        }
    }

    public void displayBoard() throws IOException {
        //System.out.println("|---|---|---|");
        PrintWriter sender_first_player = new PrintWriter(first_player.getOutputStream(), true);

        PrintWriter sender_second_player = new PrintWriter(second_player.getOutputStream(), true);
        sender_first_player.println("\n");
        sender_second_player.println("\n");
        for (int i = 0; i < 3; i++) {
            var code = new StringBuilder();
            code.append("| ");
            for (int j = 0; j < 3; j++) {
                code.append(board[i][j] + " | ");
            }
            sender_first_player.println(code.toString());
            sender_second_player.println(code.toString());
            if (i < 2) {
                sender_first_player.println("|---|---|---|");
                sender_second_player.println("|---|---|---|");
            }
        }
        sender_first_player.println("\n");
        sender_second_player.println("\n");
    }

    public boolean makeMove(char playerSymbol, int row, int col) {
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            return false;
        }
        if (board[row][col] != '-') {
            return false;
        }
        board[row][col] = playerSymbol;
        return true;
    }

    public boolean checkWin(char playerSymbol) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == playerSymbol && board[i][1] == playerSymbol && board[i][2] == playerSymbol) {
                return true;
            }
        }
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (board[0][i] == playerSymbol && board[1][i] == playerSymbol && board[2][i] == playerSymbol) {
                return true;
            }
        }
        // Check diagonals
        if ((board[0][0] == playerSymbol && board[1][1] == playerSymbol && board[2][2] == playerSymbol) ||
            (board[0][2] == playerSymbol && board[1][1] == playerSymbol && board[2][0] == playerSymbol)) {
            return true;
        }
        return false;
    }
    public boolean checkTie() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            for(int j = 0;j<3;j++){
                if(board[i][j]!='-'){
                    return false;
                }
            }
        }
        return true;
    }
    public static void main(String[] args) {
        /*TicTacToe game = new TicTacToe();
        char currentPlayer = 'X';
        boolean gameOver = false;
    
        while (!gameOver) {
            try {
                game.displayBoard();
            }
            catch (IOException e){

            }
            System.out.println("Player " + currentPlayer + ", enter your move (row and column):");
            Scanner scanner = new Scanner(System.in);
            System.out.print("Row:");
            int row = scanner.nextInt();
            System.out.print("Column:");
            int col = scanner.nextInt();
            row = row - 1;
            col = col - 1;
            if (game.makeMove(currentPlayer, row, col)) {
                if (game.checkWin(currentPlayer)) {
                    System.out.println("Player " + currentPlayer + " wins!");
                    gameOver = true;
                } else {
                    currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                }
            }
        }
        game.displayBoard();*/
    }
    public User start(User player_1, User player_2, Socket clientSocket_player1, Socket clientSocket_player2) throws IOException {
        String currentPlayer = player_1.getUsername();
        boolean gameOver = false;

        //We will assume that player_1 always start first, maybe change that in future

        PrintWriter sender_first_player = new PrintWriter(clientSocket_player1.getOutputStream(), true);
        BufferedReader receiver_first_player = new BufferedReader(new InputStreamReader(clientSocket_player1.getInputStream()));

        PrintWriter sender_second_player = new PrintWriter(clientSocket_player2.getOutputStream(), true);
        BufferedReader receiver_second_player = new BufferedReader(new InputStreamReader(clientSocket_player2.getInputStream()));

        this.first_player = clientSocket_player1;
        this.second_player = clientSocket_player2;

        User winner = new User();

        char char_first_player = 'X';
        char char_second_player = 'O';

        char char_current_player = char_first_player;

        sender_first_player.println("Player "+player_1.getUsername()+" will play first and will be the 'X'. Player "+player_2.getUsername()+" will be the 'O'.");
        sender_second_player.println("Player "+player_1.getUsername()+" will play first and will be the 'X'. Player "+player_2.getUsername()+" will be the 'O'.");


        while (!gameOver) {
            try {
                this.displayBoard();
            }
            catch (IOException e){
                System.out.println("Something went wrong!");
            }
            sender_first_player.println("Player " + currentPlayer + " is playing, enter your move (row and column):");
            sender_second_player.println("Player " + currentPlayer + " is playing first, wait for your time.");

            // Row
            sender_first_player.println("Row:");
            sender_first_player.println("Waiting for answer!");
            String rowLine = receiver_first_player.readLine();
            int row = Integer.parseInt(rowLine);

            // Column
            sender_first_player.println("Column:");
            sender_first_player.println("Waiting for answer!");
            String Column = receiver_first_player.readLine();
            int col = Integer.parseInt(Column);
            row = row - 1;
            col = col - 1;
            if (this.makeMove(char_current_player, row, col)) {
                if (this.checkWin(char_current_player)) {
                    sender_first_player.println("Player " + currentPlayer + " wins!");
                    sender_second_player.println("Player " + currentPlayer + " wins!");
                    winner = sender_second_player.equals(new PrintWriter(clientSocket_player1.getOutputStream(), true)) ? player_1 : player_2;
                    gameOver = true;
                } else if(this.checkTie()) {
                    sender_first_player.println("Its a tie!");
                    sender_second_player.println("Its a tie!");
                    gameOver = true;
                }
                else{
                    //Change char of current player
                    char_current_player = (char_current_player == char_first_player) ? char_second_player : char_first_player;

                    //Change currentplayers name
                    currentPlayer = (currentPlayer.equals(player_1.getUsername())) ? player_2.getUsername() : player_1.getUsername();
                    //Change Senders!
                    var socket = sender_first_player;
                    var socket2 = sender_second_player;
                    sender_second_player = socket;
                    sender_first_player = socket2;

                    //Change Receivers!
                    var socket3 = receiver_first_player;
                    var socket4 = receiver_second_player;
                    receiver_second_player = socket3;
                    receiver_first_player = socket4;

                }
            }else{
                sender_first_player.println("You just entered an invalid spot, try again!");
            }
        }
        System.out.println(winner);
        this.displayBoard();
        return winner;

    }
    
}
