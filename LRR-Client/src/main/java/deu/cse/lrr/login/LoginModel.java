package deu.cse.lrr.login;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class LoginModel {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public String authenticate(String id, String password, String role) {
        try {
            socket = new Socket("127.0.0.1", 9999);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.write("LOGIN:" + id + "," + password + "," + role + "\n");
            out.flush();
            
            return in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR:" + e.getMessage();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getIn() {
        return in;
    }

    public BufferedWriter getOut() {
        return out;
    }
}
