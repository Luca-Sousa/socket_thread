package thread;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class chatCliente {

    public static final int PORT = 4000;
    private ClienteSocket clienteSocket = null;
    private final String SERVER_ADDRESS = "127.0.0.1";
    private Scanner scanner;

    public chatCliente() {
        scanner = new Scanner(System.in);
    }

    public void start() throws UnknownHostException, IOException {
        Socket socket = new Socket(SERVER_ADDRESS, chatServer.PORT);
        clienteSocket = new ClienteSocket(socket);
        scanner = new Scanner(System.in);

        System.out.print("Digite seu nome de usuário: ");
        String nome = scanner.nextLine();
        clienteSocket.send(nome);

        System.out.println("Conectado ao servidor como " + nome + "!");


        // 🧵 Inicia a thread para escutar mensagens do servidor
        new Thread(() -> {
            try {
                String msg;
                while ((msg = clienteSocket.getMessage()) != null) {
                    System.out.println("\n[Mensagem recebida] " + msg);
                    System.out.print("Escreva sua mensagem ou 'sair' para sair:\n");
                }
            } catch (IOException e) {
                System.out.println("Conexão encerrada pelo servidor.");
            }
        }).start();

        // Loop para enviar mensagens
        messageLoop();
    }

    private void messageLoop() {
        String msg;
        do {
            System.out.print("Escreva sua mensagem ou 'sair' para sair:\n");
            msg = scanner.nextLine();

            // Envia a mensagem
            clienteSocket.send(msg);

            // Exibe a própria mensagem no chat do cliente
            System.out.println("Você: " + msg);
        } while (!msg.equalsIgnoreCase("sair"));

        if (scanner != null) {
            scanner.close();
        }
    }

    public static void main(String[] args) {
        chatCliente cliente = new chatCliente();
        try {
            cliente.start();
        } catch (UnknownHostException e) {
            System.err.println("Erro ao conectar ao servidor: Host desconhecido.");
        } catch (IOException e) {
            System.err.println("Erro de I/O durante a conexão ou comunicação.");
        } finally {
            System.out.println("Cliente encerrado!");
        }
    }
}
