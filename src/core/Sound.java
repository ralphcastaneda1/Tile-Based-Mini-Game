package core;

import javax.sound.sampled.*;
import java.io.File;

public class Sound {
    private volatile boolean stopRequested = false;

    public void playWav(String filePath) {
        stopRequested = false;
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile())) {
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                byte[] bytesBuffer = new byte[1024];
                int bytesRead;
                while (!stopRequested && (bytesRead = audioInputStream.read(bytesBuffer)) != -1) {
                    line.write(bytesBuffer, 0, bytesRead);
                    if (stopRequested) {
                        line.flush();
                        break;
                    }
                }
                line.drain();
                line.close();
            }
        } catch (Exception ex) {
            System.out.println("Error playing sound: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void stopPlayback() {
        stopRequested = true;
    }
}