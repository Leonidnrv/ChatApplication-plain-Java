import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket; //responsabil pentru a asculta conexiunile sau clientii ce se vor conecta si intoarce un obiect tip Socket.
    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try{
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept(); //programul se va bloca aici pana cand un client se va conecta.
                // un obiect tip socket va fi returnat si va fi folosit la comunicarea cu clientul.
                System.out.println("Un nou client s-a conectat!");
                ClientHandler clientHandler = new ClientHandler(socket); //fiecare obiect din aceasta clasa va fi responsabil cu comunicarea cu clientul. Implementeaza interfata Runnable.
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }catch(IOException e){

        }
    }

    //Daca va aparea o eroare vom inchide serverul cu aceasta metoda
    public void closeServerSocket(){
        try{
            if(serverSocket != null){
                serverSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(4444); //clientii vor putea sa se conecteze cu adresa si portul setat aici
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
