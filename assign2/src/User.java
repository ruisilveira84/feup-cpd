import java.net.Socket;

public class User {
    public User(String username, String password, int Rank,Socket token) {
        this.username = username;
        this.password = password;
        this.rank = Rank;
        this.token = token;
    }

    public User() {
    }

    public String getUsername() { return this.username; }
    public String getPassword() { return this.password; }
    public int getRank() { return this.rank; }

    public Socket getToken() { return this.token; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRank(int Rank) { this.rank = Rank; }

    public void setToken(Socket socket) { this.token = socket; }

    private String username;
    private String password;
    private int rank;

    private Socket token;
}
