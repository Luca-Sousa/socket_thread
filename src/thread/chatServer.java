package thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class chatServer {

    public static final int PORT = 4000;
    private ServerSocket serverSocket = null;
    private final List<ClienteSocket> clientesConectados = new CopyOnWriteArrayList<>();
    private static final Map<String, ClienteSocket> mapaDeClientes = new ConcurrentHashMap<>();

    private SSLServerSocket createSSLServerSocket() throws Exception {
        char[] senha = "123456".toCharArray(); // mesma senha do keytool

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new java.io.FileInputStream("chatkeystore.jks"), senha);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, senha);

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(kmf.getKeyManagers(), null, null);

        SSLServerSocketFactory ssf = sc.getServerSocketFactory();
        return (SSLServerSocket) ssf.createServerSocket(PORT);
    }

    public void start() throws Exception {
        serverSocket = createSSLServerSocket();
        System.out.println("Servidor iniciado na porta " + PORT);
        clientConnectionLoop();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void clientConnectionLoop() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> {
                try {
                    ClienteSocket clienteSocketWrapper = new ClienteSocket(clientSocket);

                    // ➕ Recebe o nome do cliente como primeira mensagem
                    String nome = clienteSocketWrapper.getMessage();
                    clienteSocketWrapper.setNome(nome);

                    mapaDeClientes.put(nome, clienteSocketWrapper);
                    clientesConectados.add(clienteSocketWrapper);

                    System.out.println("Cliente " + nome + " conectado de " + clienteSocketWrapper.getRemoteSocketAddress());

                    clienteMessageLoop(clienteSocketWrapper);

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void clienteMessageLoop(ClienteSocket clienteSocket) throws IOException {
        String msg;
        try {
            while ((msg = clienteSocket.getMessage()) != null) {
                if ("sair".equalsIgnoreCase(msg)) {
                    System.out.printf("Cliente %s desconectou.\n", clienteSocket.getRemoteSocketAddress());
                    return;
                }

                String fullMessage = String.format("%s: %s", clienteSocket.getNome(), msg);

                // 🔄 Loga todas as mensagens no servidor (inclusive privadas)
                System.out.println("[LOG] Mensagem recebida de " + clienteSocket.getNome() + ": " + fullMessage);

                if (msg.startsWith("@")) {
                    // Mensagem privada
                    String[] partes = msg.split(" ", 2);
                    if (partes.length >= 2) {
                        String destinatario = partes[0].substring(1); // remove o '@'
                        String corpo = partes[1];
                        ClienteSocket alvo = mapaDeClientes.get(destinatario);

                        if (alvo != null) {
                            String msgPrivada = "[Privado] " + clienteSocket.getNome() + ": " + corpo;
                            // Envia a mensagem privada para o destinatário
                            alvo.send(msgPrivada);
                        } else {
                            // Se o usuário não for encontrado, informa o cliente
                            clienteSocket.send("[Servidor] Usuário '" + destinatario + "' não encontrado.");
                        }
                    } else {
                        // Mensagem mal formatada
                        clienteSocket.send("[Servidor] Uso correto: @usuario mensagem");
                    }
                    continue; // Evita continuar com o envio de mensagens públicas
                }

                // 🔄 Mensagem pública: envia para todos os outros
                for (ClienteSocket cs : clientesConectados) {
                    if (cs != clienteSocket) {
                        cs.send(fullMessage);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro na comunicação com " + clienteSocket.getNome() + ": " + e.getMessage());
        } finally {
            // Limpeza após desconexão
            clientesConectados.remove(clienteSocket);
            mapaDeClientes.remove(clienteSocket.getNome());
            clienteSocket.close();
            System.out.println("Conexão com " + clienteSocket.getRemoteSocketAddress() + " fechada.");
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        try {
            chatServer server = new chatServer();
            server.start();
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
