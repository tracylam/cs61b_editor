package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.event.EventHandler;
import javafx.scene.text.Text;
import javafx.scene.Group;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.Stack;

import editor.LLDeque;
import editor.LLDeque.OneNode;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


import java.util.HashMap;




public class Editor extends Application {

    private Rectangle rect;
    private static int windowWidth = 500;
    private static int windowHeight = 500;
    public static int fontSize = 12;
    public static HashMap<Integer, OneNode> pageLines;
    private static int currentPosX = 5;
    private static int currentPosY = 0;
    private static int hmindex = 1;
    private static boolean prevButton;
    private double mousePressedY;
    private double mousePressedX;
    private int mousePosXRound;
    private int mousePosYRound;
    private String textFile;
    private static FileWriter writer;
    public LLDeque<Text> pageText;
    private SizedStack<Text> undo;
    private SizedStack<Text> redo;

    private class SizedStack<T> extends Stack<T> {
        private int maxSize;

        public SizedStack(int size) {
            super();
            this.maxSize = size;
        }
    }



    private Group rootz;
    private String fontName = "Verdana";


    public void render(LLDeque<Text> toRender) {
        if (toRender.size() > 0) {
            int rightMargin = windowWidth - 5;
            hmindex = 1;
            int position = 5;
            int positionY = 0;
            pageLines.put(hmindex, toRender.getNode(0));
            for (int i = 0; i < toRender.size(); i++) {
                Text currentLetter = toRender.get(i);
                currentLetter.setX(position);
                currentLetter.setY(positionY);
                position = position + (int) Math.round(currentLetter.getLayoutBounds().getWidth());
                currentLetter.setFont(Font.font(fontName, fontSize));

                if (position > rightMargin) {
                    hmindex += 1;
                    boolean whitespace = (toRender.get(i).getText().equals(" "));
//                    boolean whitespace = (toRender.get(i).toString().charAt(0) == 32);
                    int j = i;
                    position = 5;
                    positionY += fontSize;
                    while (whitespace != true) {
                        j--;
                        whitespace = (toRender.get(j).getText().equals(" "));
                    }
                    j = j + 1;
                    pageLines.put(hmindex, pageText.getNode(j));

                    while (j <= i) {
                        if (position > rightMargin) {
                            position = 5;
                            positionY += fontSize;
                        }
                        toRender.get(j).setY(positionY);
                        toRender.get(j).setX(position);

                        position = position + (int) Math.round(toRender.get(j).getLayoutBounds().getWidth());
                        j++;


                    }

                }
                if (toRender.get(i).getText().equals("\r") || toRender.get(i).getText().equals("\n") || toRender.get(i).getText().equals("\r\n")) {
                    positionY += fontSize;
                    position = 5;
                    hmindex += 1;
                    pageLines.put(hmindex, pageText.getNode(i + 1));
                }

            }
//        currentPosX = rect.getX();
            currentPosY = positionY;
        }
    }

    private class KeyEventHandler implements EventHandler<KeyEvent> {

        public KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            rootz = root;
//            pageText = new LLDeque<>();
            pageLines = new HashMap<>();
            undo = new SizedStack<>(100);
            redo = new SizedStack<>(100);
        }



        public void Enter() {
            currentPosY += fontSize;
            currentPosX = 5;
            rect.relocate(currentPosX, currentPosY);
            Text lineBreak = new Text("\r");
            pageText.addPrev(lineBreak);
        }

//        public void updateHash(HashMap<Integer, LLDeque<Text>> lines) {
//            int numLines = currentPosY/fontSize;
//            for (int i = 0; i < numLines; i++ ) {
//                lines.put(i, pageText);
//            }
//        }

        @Override
        public void handle(KeyEvent keyEvent) {


            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                Text characterTyped = new Text(keyEvent.getCharacter());
                characterTyped.setTextOrigin(VPos.TOP);

                if (characterTyped.getText().length() > 0 && characterTyped.getText().charAt(0) != 8 && characterTyped.getText().charAt(0) != 13) {
                    if (prevButton == false) {
                        pageText.addPrev(characterTyped);
                        currentPosX += characterTyped.getLayoutBounds().getWidth();
                        render(pageText);
//                        rect.setX(currentPosX);
                        rect.relocate(currentPosX, currentPosY);
                        rootz.getChildren().add(characterTyped);
                        undo.push(characterTyped);
                    }
                    prevButton = false;
                }


            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                KeyCode code = keyEvent.getCode();

                if (code == KeyCode.LEFT) {
                    pageText.cursorLeft();
                    Text letter = pageText.get(pageText.getCurrPos());
                    int prevWidth = (int) Math.round(letter.getLayoutBounds().getWidth());
                    currentPosX = currentPosX - prevWidth;
                    rect.relocate(currentPosX, currentPosY);

                }

                if (code == KeyCode.RIGHT) {
                    if (pageText.get(pageText.currPos) != null) {
                        Text letter = (Text) pageText.get(pageText.currPos);
                        int postWidth = (int) Math.round(letter.getLayoutBounds().getWidth());
                        currentPosX = currentPosX + postWidth;
                        pageText.cursorRight();
                        rect.relocate(currentPosX, currentPosY);
                    }
                }

                if (code == KeyCode.UP) {
                    if (pageLines.get(hmindex - 1) != null) {
                        int startingmargin = 5;
                        OneNode currentNode = pageLines.get(hmindex - 1);
                        while (startingmargin <= currentPosX) {
                            Text object = pageText.getItemNode(currentNode);
                            int prevWidth = (int) Math.round(object.getLayoutBounds().getWidth());
                            currentNode = pageText.getNext(currentNode);
                            startingmargin += prevWidth;
                        }
                        OneNode cursor = pageText.getCursor();
                        if (cursor.next != null) {
                            cursor.next.prev = cursor.prev;
                        }
                        if (cursor.prev != null) {
                            cursor.prev.next = cursor.next;
                        }
                        cursor.prev = currentNode.prev;
                        if (currentNode.prev != null) {
                            currentNode.prev.next = cursor;
                        }
                        currentNode.prev = cursor;
                        cursor.next = currentNode;

                        rect.relocate(mousePosXRound, Math.round((hmindex - 1) * fontSize));
                    }
                }

                if (code == KeyCode.DOWN) {
                    if (pageLines.get(hmindex + 1) != null) {
                        int wantedX = mousePosXRound;
                        int startingmargin = 5;
                        OneNode currentNode = pageLines.get(hmindex + 1);
                        while (startingmargin <= wantedX) {
                            Text object = pageText.getItemNode(currentNode);
                            int prevWidth = (int) Math.round(object.getLayoutBounds().getWidth());
                            currentNode = pageText.getNext(currentNode);
                            startingmargin += prevWidth;
                        }
                        OneNode cursor = pageText.getCursor();
                        if (cursor.next != null) {
                            cursor.next.prev = cursor.prev;
                        }
                        if (cursor.prev != null) {
                            cursor.prev.next = cursor.next;
                        }
                        cursor.prev = currentNode.prev;
                        if (currentNode.prev != null) {
                            currentNode.prev.next = cursor;
                        }
                        currentNode.prev = cursor;
                        cursor.next = currentNode;

                        rect.relocate(mousePosXRound, Math.round((hmindex + 1) * fontSize));
                    }
                }

                if (code == KeyCode.BACK_SPACE) {
//                    pageText.getCursor();
                    int pos = (pageText.getCurrPos()) - 1;
                    Text letter = pageText.get(pos);
                    int prevWidth = (int) Math.round(letter.getLayoutBounds().getWidth());
                    currentPosX = currentPosX - prevWidth;
                    rect.relocate(currentPosX, currentPosY);
//                    Text cursor = (Text) pageText.get(pageText.getCurrPos() - 1);
                    Text cursor = pageText.getItemNode(pageText.removePrev());
                    rootz.getChildren().remove(cursor);
                    undo.push(cursor);
                    render(pageText);

                }

                if (code == KeyCode.ENTER) {
                    Enter();
                }

                if (keyEvent.isShortcutDown()) {
                    prevButton = true;
                    if (keyEvent.getCode() == KeyCode.P) {
                        System.out.println(currentPosX + "," + currentPosY);
                    }

                    try {
                        if (keyEvent.getCode() == KeyCode.S) {
                            writer = new FileWriter(textFile);
                            for (int i = 0; i < pageText.size(); i++) {
                                writer.write(pageText.get(i).getText());
                            }
                            writer.close();
                        }
                    } catch (IOException ioException) {
                        System.out.println("Error when copying; exception was: " + ioException);
                    }

                    if (keyEvent.getCode() == KeyCode.PLUS || keyEvent.getCode() == KeyCode.EQUALS) {
                        fontSize += 4;
                        rect.resize(1, fontSize);
                        render(pageText);
                    }

                    if (keyEvent.getCode() == KeyCode.MINUS) {
                        if (fontSize > 4) {
                            fontSize -= 4;
                            rect.resize(1, fontSize);
                            render(pageText);
                        }
                    }
                }
            }

        }
    }


    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
            /** A Text object that will be used to print the current mouse position. */
            Text positionText;

            MouseClickEventHandler(Group root) {
                // For now, since there's no mouse position yet, just create an empty Text object.
                positionText = new Text("");
                root.getChildren().add(positionText);
            }

        @Override
        public void handle(MouseEvent mouseEvent) {

            mousePressedX = mouseEvent.getX();
            mousePressedY = mouseEvent.getY();

            mousePosXRound = (int) Math.round(mousePressedX);
            mousePosYRound = (int) Math.round(mousePressedY/fontSize);

            if (pageLines.get(mousePosYRound) != null) {
                int wantedX = mousePosXRound;
                int startingmargin = 5;
                OneNode currentNode = pageLines.get(mousePosYRound);
                while (startingmargin <= wantedX) {
                    Text object = pageText.getItemNode(currentNode);
                    int prevWidth = (int) Math.round(object.getLayoutBounds().getWidth());
                    currentNode = pageText.getNext(currentNode);
                    startingmargin += prevWidth;
                }
                OneNode cursor = pageText.getCursor();
                if (cursor.next != null) {
                    cursor.next.prev = cursor.prev;
                }
                if (cursor.prev != null) {
                    cursor.prev.next = cursor.next;
                }
                cursor.prev = currentNode.prev;
                if (currentNode.prev != null) {
                currentNode.prev.next = cursor;
                }
                currentNode.prev = cursor;
                cursor.next = currentNode;

                rect.relocate(mousePosXRound, Math.round(mousePressedY));
            }
        }
    }



    private class BlinkingCursor implements EventHandler<ActionEvent> {

        private int colorIndex = 0;
        private Color[] bw = {Color.BLACK, Color.WHITE};

        public BlinkingCursor() {

            rect = new Rectangle(5, 5, 1, fontSize);
            rect.setFill(Color.BLACK);
            blink();
        }

        private void blink() {
            rect.setFill(bw[colorIndex]);
            colorIndex = (colorIndex + 1) % bw.length;
        }

        @Override
        public void handle(ActionEvent event) {
            blink();
        }


        public void makeBlink() {
            final Timeline timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            BlinkingCursor cursorChange = new BlinkingCursor();
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(.5), cursorChange);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
        }
    }


    @Override
    public void start(Stage primaryStage) {
        BlinkingCursor cursor = new BlinkingCursor();
        Group root = new Group();
        Scene scene = new Scene(root, windowWidth, windowHeight, Color.WHITE);

        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, windowWidth, windowHeight);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);

        pageText = new LLDeque<Text>();

        textFile = getParameters().getRaw().get(0);
        try {

            File inputFile = new File(textFile);

            if (inputFile.isDirectory()) {
                System.err.println("Unable to open file nameThatIsADirectory");
            }
            if (!inputFile.exists()) {
                inputFile.createNewFile();
                render(pageText);
            }

            FileReader reader = new FileReader(inputFile);

            BufferedReader bufferedReader = new BufferedReader(reader);

             /*use when close program to write to save*/

            int intRead = -1;
            // Keep reading from the file input read() returns -1, which means the end of the file
            // was reached.
            while ((intRead = bufferedReader.read()) != -1) {
                // The integer read can be cast to a char, because we're assuming ASCII.
//                char charRead = (char) intRead;
//                char charRead2 = (char) intRead;

                Text charRead = new Text(Character.toString((char) intRead));
                charRead.setTextOrigin(VPos.TOP);
//                Text charRead = new Text("a");
                pageText.addPrev(charRead);
                root.getChildren().add(charRead);

            }
                render(pageText);
                bufferedReader.close();

//            writer.close();

        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);

        }
        catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }








            scene.setOnMouseClicked(new MouseClickEventHandler(root));

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                // Re-compute Allen's width.
//                int newWidth = (newScreenWidth.intValue());
                windowWidth = newScreenWidth.intValue();
                render(pageText);
            }

        });

        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
//                int newAllenHeight = getDimensionInsideMargin(newScreenHeight.intValue());
//                allenView.setFitHeight(newAllenHeight);
                windowHeight = newScreenHeight.intValue();
                render(pageText);
            }
        });



        primaryStage.setTitle("Editor");

        cursor.makeBlink();
        root.getChildren().add(rect);

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
                launch(args);
    }
}

