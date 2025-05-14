# Chat Secure - Sistema de Chat Seguro

Este projeto implementa um sistema de chat entre cliente e servidor utilizando conexões seguras via SSL (Secure Sockets Layer). O chat permite a comunicação entre múltiplos clientes, com a possibilidade de enviar mensagens públicas e privadas. O sistema é dividido em três partes principais: o **servidor** que gerencia as conexões, o **cliente** que interage com o servidor, e o **cliente com interface gráfica** para facilitar a interação do usuário.

## Arquitetura

1. **Servidor (`chatServer`)**:

   * Cria um servidor SSL que escuta conexões na porta 4000.
   * Gerencia múltiplos clientes simultaneamente usando threads.
   * Permite comunicação tanto pública quanto privada entre os clientes.
   * Usa um `KeyStore` para configuração de criptografia e autenticação segura.

2. **Cliente (`chatCliente` e `chatClienteGUI`)**:

   * O **`chatCliente`** é uma versão de linha de comando do cliente, permitindo enviar e receber mensagens.
   * O **`chatClienteGUI`** oferece uma interface gráfica mais amigável para o usuário, implementada com a biblioteca `Swing`.

3. **ClienteSocket**:

   * Classe que encapsula a comunicação entre o cliente e o servidor, fornecendo métodos para enviar e receber mensagens através do socket.

## Funcionalidades

* **Conexão Segura (SSL)**:
  O servidor e o cliente utilizam SSL para garantir que as comunicações sejam seguras e criptografadas.

* **Mensagens Públicas e Privadas**:

  * **Mensagens Públicas**: Enviadas para todos os clientes conectados.
  * **Mensagens Privadas**: Enviadas para um usuário específico, precedidas pelo símbolo `@`.

* **Thread para Escuta**:
  O servidor cria uma nova thread para cada cliente, permitindo que ele envie e receba mensagens de forma independente.

* **Desconexão**:
  O cliente pode se desconectar do servidor digitando `sair`. O servidor também detecta quando um cliente se desconecta de forma inesperada.

* **Interface Gráfica (GUI)**:
  A versão GUI do cliente permite ao usuário digitar seu nome de usuário, conectar-se ao servidor, enviar e receber mensagens de maneira intuitiva.

## Como Executar

### Pré-requisitos

* Java 8 ou superior instalado.
* Um keystore SSL para o servidor. Você pode gerar um keystore utilizando o `keytool` do Java:

  ```bash
  keytool -genkey -keyalg RSA -keysize 2048 -keystore chatkeystore.jks -validity 365 -alias chatserver
  ```

### Passos para executar o servidor

1. Compile o projeto:

   ```bash
   javac -d . *.java
   ```

2. Inicie o servidor:

   ```bash
   java thread.chatServer
   ```

O servidor estará escutando na porta `4000` e aguardando por conexões de clientes.

### Passos para executar o cliente (linha de comando)

1. Compile o projeto:

   ```bash
   javac -d . *.java
   ```

2. Execute o cliente:

   ```bash
   java thread.chatCliente
   ```

   O cliente pedirá o nome do usuário e se conectará automaticamente ao servidor.

### Passos para executar o cliente com interface gráfica (GUI)

1. Compile o projeto:

   ```bash
   javac -d . *.java
   ```

2. Execute o cliente GUI:

   ```bash
   java thread.chatClienteGUI
   ```

   O cliente abrirá uma janela para o usuário inserir seu nome de usuário e conectar-se ao servidor.

## Estrutura do Projeto

* **chatServer.java**: Contém a implementação do servidor que gerencia os clientes e mensagens.
* **chatCliente.java**: Contém a implementação do cliente em modo texto, permitindo enviar e receber mensagens.
* **chatClienteGUI.java**: Contém a implementação da interface gráfica para o cliente.
* **ClienteSocket.java**: Classe comum para o cliente e servidor, que gerencia a comunicação via socket.
* **chatkeystore.jks**: Arquivo keystore utilizado para criptografia SSL.

## Como Funciona

### 1. **Servidor**

* O servidor é responsável por aceitar conexões de clientes, atribuir a cada cliente uma thread para processar suas mensagens e enviar mensagens para todos os clientes conectados.
* Ele utiliza um keystore SSL para garantir a comunicação segura.

### 2. **Cliente**

* O cliente conecta-se ao servidor via SSL, enviando seu nome como a primeira mensagem.
* Ele pode enviar mensagens públicas (para todos os clientes) ou privadas (para um cliente específico).
* O cliente escuta as mensagens do servidor e exibe as mensagens recebidas.

### 3. **Comunicação SSL**

* A comunicação entre o cliente e o servidor é criptografada utilizando o protocolo SSL, garantindo que as mensagens não sejam interceptadas ou manipuladas durante a transmissão.

### 4. **Mensagens Privadas**

* Para enviar uma mensagem privada, o cliente deve iniciar a mensagem com o símbolo `@` seguido do nome do destinatário.

Exemplo:

```
@joao Olá, como você está?
```

Se o destinatário estiver conectado, a mensagem será entregue somente a ele.

### 5. **Interface Gráfica (GUI)**

* O cliente GUI oferece um campo para inserir o nome de usuário e uma área para digitar e exibir mensagens.
* Ele também gerencia a conexão segura, exibe mensagens recebidas e permite enviar mensagens via interface gráfica.

## Exemplo de Uso

### Servidor:

```bash
$ java thread.chatServer
Servidor iniciado na porta 4000
```

### Cliente (linha de comando):

```bash
$ java thread.chatCliente
Digite seu nome de usuário: usuario1
Conectado ao servidor como usuario1!
Escreva sua mensagem ou 'sair' para sair:
```

### Cliente (GUI):

Ao iniciar o `chatClienteGUI`, o cliente exibirá a interface gráfica onde o usuário pode inserir seu nome e interagir com o servidor através de uma interface amigável.

---

## Considerações Finais

Este é um exemplo simples de um sistema de chat com conexão segura. Pode ser expandido com funcionalidades adicionais, como autenticação de usuário, armazenamento de histórico de mensagens ou integração com bancos de dados.

Este projeto é útil para entender a utilização de SSL em comunicação cliente-servidor, além de demonstrar a implementação de chats com funcionalidades como mensagens privadas e públicas.

Se houver alguma dúvida ou sugestão, fique à vontade para contribuir ou melhorar o projeto!

## Licença

Este projeto é licenciado sob a [Licença MIT](https://opensource.org/licenses/MIT).
