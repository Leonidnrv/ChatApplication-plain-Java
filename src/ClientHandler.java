import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>; //va stoca toti clientii. Cand un client va trimit un mesaj, vom face loop pe aceasta lista si vom trimite acelasi mesaj la fiecare din clientii conectati. Este statica pentru ca vrem sa apartina clasei, nu fiecarui obiect.
    private Socket socket; //va fi responsabila pentru stabilirea conexiunii dintre client si server
    private BufferedReader bufferedReader; //va fi folosita pentru a citi datele trimise de la client
    private BufferedWriter bufferedWriter; //va fi folosita pentru a trimite datele catre client
    private String clientUserName; //va distinge fiecare client

    public ClientHandler(Socket socket){
        try{
            //Fiecare socket va avea un inputstream de unde va primi date si un outputstream unde va trimite date
            this.socket = socket;
            //stream de caractere (in Java observam streamurile de caractere prin cuvantul Writer). OutputStreamWriter = charcater stream / socket.getOutputStream = byte stream
            //vom folosi buffer pentru eficienta
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = bufferedReader.readLine(); //programul se va opri aici si va astepta sa introducem numele noului client.
            clientHandlers.add(this); //dupa ce s-a scris numele utilizatorului, adaugam la ArrayList obiectul curent (this)
            broadcastMessage("SERVER: " + clientUserName + " has entered the chat!")
        }catch(IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while(socket.isConnected()){
            try{
                //acest thread se va bloca aici si va astepta un mesaj de la clienti. Pentru ca va fi un thread separat, nu va in curca aplicatia principala
                messageFromClient = bufferedReader.readLine(); //datele de la alti clienti vor ajunge in buffer
                broadcastMessage(messageFromClient); //vom distribui mesajul catre toti clienti din ArrayList
            }catch(IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                break; //daca vom primi exceptie va trebui sa iesim din while
            }
        }
    }

    public void broadcastMessage(String messageToSend){
        for(ClientHandler clientHandler : clientHandlers){
            try{
                if(!clientHandler.clientUserName.equals(clientUserName)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch(IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    //aceasta metoda va semnala cand un utilizator a parasit chat-ul
    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUserName + " has left the chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
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
}
