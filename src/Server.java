import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

//限制房间只能有2个人
public class Server {
    //绑定Server端口
    private static int port = 7500;
    //用户的对话，点到点通讯，发送后即删
    private static Map<String, String> map = new HashMap<>();


    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                String firs = input.readUTF();
                if((map.get(firs+"@host") == null)){
                    map.put(firs+"@host","++++0000");
                    new Thread(new Receive(socket, firs+"@host")).start();
                    new Thread(new Send(socket, firs+"@host")).start();
                    //new Thread(new Transfer(socket,firs+"@host")).start();
                }else {
                    //*********
                    //限制代码
                    if(map.get(firs+"@guest") != null) {
                        socket.close();
                        continue;
                    }
                    map.put(firs+"@guest","////1111");
                    new Thread(new Receive(socket, firs+"@guest")).start();
                    new Thread(new Send(socket, firs+"@guest")).start();
                    //new Thread(new Transfer(socket,firs+"@guest")).start();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static class Receive implements Runnable{
        private Socket socket;
        private String role;
        Receive(Socket socket, String role){
            this.socket = socket;
            this.role = role;
        }

        public void run(){
            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                while (true){
                    System.out.println(map.get(role));
                    String receive = input.readUTF();
                    if(receive.equals("quit")){
                        System.out.println(map.toString());
                        map.remove(role);
                        System.out.println(map.toString());
                        return;
                    }
                    map.put(role,receive);
                    System.out.println(role+":  "+map.get(role));
                    Thread.sleep(2);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    static class Send implements Runnable{
        private Socket socket;
        private String role;
        Send(Socket socket, String role){
            this.socket = socket;
            String temp = role.split("@")[1];
            if(temp.equals("host"))
                this.role = role.split("@")[0]+"@guest";
            else
                this.role = role.split("@")[0]+"@host";
        }

        public void run(){
            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                while(true){
                    if(map.get(role) != null) {
                        if (map.get(role) != "") {
                            System.out.println(role+":  "+ map.get(role).toString());
                            out.writeUTF(map.get(role).toString());
                            map.put(role,"");
                        }
                    }
                    Thread.sleep(2);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}