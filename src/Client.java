import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

//Acesta clasa va reprezenta un client/utilizator care se conecteaza la chat
//va putea trimite si primi mesaje catre server (de la ClientHandler)
public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;

    public Client(Socket socket, String userName){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userName = userName;
        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    //cu aceasta metoda vom trimite mesaje
    public void sendMessage(){
        try{
            bufferedWriter.write(userName);
            bufferedWriter.newLine();
            bufferedWriter.flush(); //clientHandle va astepta acest mesaj in expresia "this.clientUserName = bufferedReader.readLine(); "

            Scanner scanner = new Scanner(System.in);
            while(socket.isConnected()){
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(userName + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }catch(IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    //cu aceasta metoda vom primi mesaje de la server
    //are un thread separat care se ocupa doar cu ascultarea mesajelor si va printa in consola cand primeste un text
    public void listenForMessage(){
        //pentru ca readLine este o metoda care blocheaza programul vom folosi un thread separat care ne va ajuta la ascultarea mesajelor
        new Thread(new Runnable(){
            @Override
            public void run(){
                String messageFromGroupChat;
                while(socket.isConnected()){
                    try{
                        messageFromGroupChat = bufferedReader.readLine();
                        System.out.println(messageFromGroupChat);
                    }catch(IOException e){
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            //nu este nevoie sa inchidem si input/outputstream. BufferedReader/Writer le va inchide automat in cascada
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){ //aceeasi regula se aplica si la socket. Nu trebuie sa mai inchidem socket input/outputstream
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 4444);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }
}