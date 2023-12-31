package fr.takehere.communicationplus;

import java.io.*;
import javafx.concurrent.Task;
import javax.sound.sampled.*;

public class JavaSoundRecorder extends Task<Void>
{

    private File lastRecording;

    // path of the wav file
    File wavFile = new File("RecordAudio.wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // the line from which audio data is captured
    TargetDataLine line;

    @Override
    protected Void call() throws Exception
    {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();   // start capturing

            System.out.println("Start capturing...");

            AudioInputStream ais = new AudioInputStream(line);

            System.out.println("Start recording...");

            // start recording
            AudioSystem.write(ais, fileType, wavFile);

        }
        catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Defines an audio format
     */
    AudioFormat getAudioFormat()
    {
        float sampleRate = 20000;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    /**
     * Closes the target data line to finish capturing and recording
     */
    void finish()
    {
        line.stop();
        line.close();
        System.out.println("Finished");

        lastRecording = wavFile;
    }


    public File getLastRecording() {
        return lastRecording;
    }
}