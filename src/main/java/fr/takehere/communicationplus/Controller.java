package fr.takehere.communicationplus;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ResourceBundle;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

public class Controller implements Initializable {

    @FXML
    private ToggleButton recordButton;

    JavaSoundRecorder javaSoundRecorder;
    boolean isRecording = false;
    ColorAdjust colorAdjust;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0.0);
        recordButton.setEffect(colorAdjust);

        GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook(true);
        keyboardHook.addKeyListener(new GlobalKeyAdapter() {
            @Override
            public void keyPressed(GlobalKeyEvent event) {
                if (event.isControlPressed() && event.isMenuPressed()){
                    if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_M){
                        Platform.runLater(() -> recordButton.fire());
                    }
                }
            }
        });
    }

    @FXML
    protected void recordButtonClick() {
        isRecording = !isRecording;

        //Handle anim
        if (isRecording){
            recordButton.setText("Stop");

            Timeline fadeInTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(0),
                            new KeyValue(colorAdjust.brightnessProperty(), colorAdjust.brightnessProperty().getValue(), Interpolator.LINEAR)),
                    new KeyFrame(Duration.seconds(0.1f), new KeyValue(colorAdjust.brightnessProperty(), -0.3f, Interpolator.LINEAR)
                    ));
            fadeInTimeline.setCycleCount(1);
            fadeInTimeline.setAutoReverse(false);
            fadeInTimeline.play();
        }else {
            recordButton.setText("Launch");


            Timeline fadeOutTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(0),
                            new KeyValue(colorAdjust.brightnessProperty(), colorAdjust.brightnessProperty().getValue(), Interpolator.LINEAR)),
                    new KeyFrame(Duration.seconds(0.1f), new KeyValue(colorAdjust.brightnessProperty(), 0, Interpolator.LINEAR)
                    ));
            fadeOutTimeline.setCycleCount(1);
            fadeOutTimeline.setAutoReverse(false);
            fadeOutTimeline.play();
        }

        if (isRecording){
            javaSoundRecorder = new JavaSoundRecorder();
            Thread thread = new Thread(javaSoundRecorder);
            thread.start();
        }else {
            javaSoundRecorder.finish();
            javaSoundRecorder.cancel();

            copyToClipboard();
        }
    }

    private void copyToClipboard(){
        File wavFile = javaSoundRecorder.getLastRecording();

        try {
            byte[] binaryData = Files.readAllBytes(wavFile.toPath());

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            Transferable transferable = new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[] { DataFlavor.javaFileListFlavor };
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return flavor.equals(DataFlavor.javaFileListFlavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                        ArrayList<File> files = new ArrayList<>();

                        try {
                            File file = new File(System.getProperty("java.io.tmpdir") + "Message-Vocal.wav");
                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                fos.write(binaryData);
                            }
                            files.add(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return files;
                    } else {
                        throw new UnsupportedFlavorException(flavor);
                    }
                }
            };

            clipboard.setContents(transferable, null);

            wavFile.delete();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
