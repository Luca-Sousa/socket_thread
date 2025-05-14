package thread;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;

public class chatClienteGUI extends JFrame {

    private ClienteSocket clienteSocket;
    private final JTextArea areaMensagens;
    private final JTextField campoMensagem;
    private final JTextField campoUsuario;
    private final JButton botaoEnviar;
    private final JButton botaoConectar;

    private static final int PORT = 4000;
    private static final String SERVER = "127.0.0.1";

    public chatClienteGUI() {
        setTitle("Chat - Cliente");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Topo - Nome do usuário e botão conectar
        JPanel painelTopo = new JPanel(new BorderLayout());
        campoUsuario = new JTextField();
        botaoConectar = new JButton("Conectar");
        painelTopo.add(new JLabel("Usuário: "), BorderLayout.WEST);
        painelTopo.add(campoUsuario, BorderLayout.CENTER);
        painelTopo.add(botaoConectar, BorderLayout.EAST);
        add(painelTopo, BorderLayout.NORTH);

        // Centro - Área de mensagens
        areaMensagens = new JTextArea();
        areaMensagens.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaMensagens);
        add(scroll, BorderLayout.CENTER);

        // Inferior - Campo de mensagem e botão enviar
        JPanel painelInferior = new JPanel(new BorderLayout());
        campoMensagem = new JTextField();
        botaoEnviar = new JButton("Enviar");
        painelInferior.add(campoMensagem, BorderLayout.CENTER);
        painelInferior.add(botaoEnviar, BorderLayout.EAST);
        add(painelInferior, BorderLayout.SOUTH);

        // Eventos
        botaoConectar.addActionListener(e -> conectar());
        botaoEnviar.addActionListener(e -> enviarMensagem());
        campoMensagem.addActionListener(e -> enviarMensagem());

        setVisible(true);
    }

    private SSLSocket createSSLSocket() throws Exception {
        TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAll, new SecureRandom());

        SSLSocketFactory factory = sc.getSocketFactory();
        return (SSLSocket) factory.createSocket(SERVER, PORT);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void conectar() {
        try {
            String nome = campoUsuario.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Digite seu nome de usuário.");
                return;
            }

            // Criação segura do socket SSL
            Socket socket = createSSLSocket();

            clienteSocket = new ClienteSocket(socket);
            clienteSocket.send(nome);

            campoUsuario.setEditable(false);
            botaoConectar.setEnabled(false);
            appendMensagem("[Sistema] Conectado ao servidor como " + nome);

            // Thread para escutar mensagens do servidor
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = clienteSocket.getMessage()) != null) {
                        appendMensagem(msg);  // Exibe as mensagens recebidas do servidor
                    }
                } catch (IOException e) {
                    appendMensagem("[Aviso] Conexão perdida com o servidor.");
                } finally {
                    desconectar();
                }
            }).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar com segurança: " + e.getMessage());
            e.printStackTrace(); // Imprime o stack trace completo
        }
    }

    private void enviarMensagem() {
        if (clienteSocket == null) {
            JOptionPane.showMessageDialog(this, "Conecte-se ao servidor primeiro.");
            return;
        }

        String msg = campoMensagem.getText().trim();
        if (!msg.isEmpty()) {
            try {
                boolean ok = clienteSocket.send(msg);
                if (!ok) {
                    throw new IOException("Erro ao enviar a mensagem.");
                }

                // Exibe a própria mensagem no chat
                appendMensagem("Você: " + msg);  // Aqui o cliente vê a própria mensagem

            } catch (IOException e) {
                appendMensagem("[Erro] Não foi possível enviar a mensagem.");
                desconectar();
            }
            campoMensagem.setText("");
        }
    }

    private void desconectar() {
        try {
            if (clienteSocket != null) {
                clienteSocket.close();
            }
        } catch (IOException ignored) {
        }

        clienteSocket = null;
        campoUsuario.setEditable(true);
        botaoConectar.setEnabled(true);
        appendMensagem("[Sistema] Desconectado do servidor.");
    }

    private void appendMensagem(String msg) {
        SwingUtilities.invokeLater(() -> {
            areaMensagens.append(msg + "\n");
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new chatClienteGUI());
    }
}
