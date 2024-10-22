import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Test {
    /*private void fillClients(){
        JSONParser parser= new JSONParser();
        JSONObject a = null;

        try {
            a = (JSONObject) parser.parse(new FileReader(System.getProperty("user.dir")+"/assign2/src/userLogs.json"));
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
            String rankString = (String) person.get("rank");


            User user = new User(username,password,rank);
            String token = (String) person.get("token");

        }
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.fillClients();
    }*/
}
