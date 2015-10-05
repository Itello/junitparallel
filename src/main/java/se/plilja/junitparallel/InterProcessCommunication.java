package se.plilja.junitparallel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static se.plilja.junitparallel.TestUtil.snooze;

class InterProcessCommunication implements AutoCloseable {
    private static final int SOCKET_CONNECT_TIMEOUT = 2000;

    private final Socket socket;
    private final List<Closeable> extraCloseables;
    private final BufferedReader br;
    private PrintWriter out;

    private InterProcessCommunication(Socket socket, List<Closeable> extraCloseables) throws IOException {
        this.socket = socket;
        this.extraCloseables = extraCloseables;
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    static InterProcessCommunication createServer(int port) throws IOException {
        ServerSocket server = null;
        Socket socket = null;
        try {
            System.out.println(port);
            server = new ServerSocket(port);
            socket = server.accept();
            return new InterProcessCommunication(socket, singletonList(server));
        } catch (IOException e) {
            System.out.println("SMALLER");
            if (socket != null) {
                socket.close();
            }
            if (server != null) {
                server.close();
            }
            throw new RuntimeException(e);
        }
    }

    static InterProcessCommunication createClient(int port) throws IOException {
        Socket socket = connectToServerSocket(port);
        return new InterProcessCommunication(socket, emptyList());
    }

    private static Socket connectToServerSocket(int port) throws IOException {
        int elapsedTime = 0;
        while (elapsedTime < SOCKET_CONNECT_TIMEOUT) {
            try {
                return new Socket("localhost", port);
            } catch (IOException ioe) {
                // This is expected if the server isn't ready yet
            }
            elapsedTime += 10;
            snooze(10);
        }
        // One last try, if we still can't create the socket the error will propagate to the caller
        return new Socket("localhost", port);
    }

    @Override
    public void close() throws IOException {
        out.close();
        br.close();
        socket.close();
        for (Closeable extraCloseable : extraCloseables) {
            extraCloseable.close();
        }
    }

    boolean hasInput() throws IOException {
        return br.ready();
    }

    void sendMessage(String message) throws IOException {
        out.println(message);
    }

    String receiveMessage() throws IOException {
        return br.readLine();
    }

    @SuppressWarnings("unchecked")
    <T> T receiveObject() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        return (T) ois.readObject();
    }

    <T extends Serializable> void sendObject(T object) throws IOException, ClassNotFoundException {
        ObjectOutputStream ous = new ObjectOutputStream(socket.getOutputStream());
        ous.writeObject(object);
    }

}
