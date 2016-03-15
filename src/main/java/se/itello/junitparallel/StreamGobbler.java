package se.itello.junitparallel;

import java.io.*;

class StreamGobbler extends Thread {
    private final InputStream is;
    private final PrintStream output;
    private volatile boolean shouldStop = false;

    StreamGobbler(InputStream is, PrintStream output) {
        this.is = is;
        this.output = output;
    }

    void pleaseStop() {
        shouldStop = true;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while (!shouldStop && (line = br.readLine()) != null) {
                output.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}