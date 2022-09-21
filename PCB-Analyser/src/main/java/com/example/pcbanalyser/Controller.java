package com.example.pcbanalyser;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.*;


public class Controller {

    @FXML
    private Button BrowseFiles;
    @FXML
    private ImageView DisplayImage;
    @FXML
    private ImageView EditedImage;
    @FXML
    private AnchorPane AnchorPlane;
    @FXML
    private Label ImageName;
    @FXML
    public FileChooser fileChooser;
    @FXML
    private PixelReader pixelReader;
    @FXML
    private PixelReader pixelReader2;
    @FXML
    private PixelWriter pixelWriter;
    @FXML
    private Label RedValueDisplay;
    @FXML
    private Label GreenValueDisplay;
    @FXML
    private Label BlueValueDisplay;
    @FXML
    private Label HueValueDisplay;
    @FXML
    private Label BrightnessValueDisplay;
    @FXML
    private Label SaturationValueDisplay;
    @FXML
    private Button SelectPartButton;
    @FXML
    private Button ExitButton;
    @FXML
    private Button noisesuppression;
    @FXML
    private Label PixelCount, NoiseLabel;
    @FXML
    private ComboBox SelectColour;
    @FXML
    private Slider NoiseSlider;
    @FXML
    private Pane displayimagepane;
    @FXML
    private Button randomColourButton;
    @FXML
    private CheckBox checkbox;

    double NoiseSuppressionValue;
    int[] disjointarray;
    int width;
    int height;
    static Image image;

    @FXML
    public WritableImage wrImage;
    @FXML
    public TextField partNames;
    @FXML
    public Label DisplayRecCount;

    HashMap<Integer, ArrayList<Integer>> disjointMap;

    public void initialize() {
        noiseSliderListener();
        checkbox.isSelected();

    }

    //Select an image from PC and set it to Displayimage
    @FXML
    public void SelectImage(ActionEvent MouseEvent) {
        final FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) AnchorPlane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.out.println("Path: " + file.getAbsolutePath());
            ImageName.setText(file.getAbsolutePath());
            DisplayImage.setFitWidth(400);
            DisplayImage.setFitHeight(400);
            DisplayImage.setImage(new Image(file.getAbsolutePath(), 400, 400, false, false));
        }
    }

//Exit button controls
    @FXML
    public void Exit() {
        ExitButton.setOnAction(e -> Platform.exit());
    }

//Black and White Conversion
    public void BlackandWhiteConversion(MouseEvent mouseEvent) {
        width = (int) DisplayImage.getImage().getWidth();
        height = (int) DisplayImage.getImage().getHeight();
        disjointarray = new int[height * width];
        DisplayImage.setOnMousePressed((e) -> {
            PixelReader pr = DisplayImage.getImage().getPixelReader();
            Color[] col = {pr.getColor((int) e.getX(), (int) e.getY())};

            System.out.println("hue: " + col[0].getHue());
            System.out.println("sat: " + col[0].getSaturation());
            System.out.println("bri: " + col[0].getBrightness());
            System.out.println("Green: " + col[0].getGreen());
            System.out.println("Red: " + col[0].getRed());
            System.out.println("Blue: " + col[0].getBlue());
            RedValueDisplay.setText(String.valueOf(col[0].getRed()));
            BlueValueDisplay.setText(String.valueOf(col[0].getBlue()));
            GreenValueDisplay.setText(String.valueOf(col[0].getGreen()));
            HueValueDisplay.setText(String.valueOf(col[0].getHue()));
            SaturationValueDisplay.setText(String.valueOf(col[0].getSaturation()));
            BrightnessValueDisplay.setText(String.valueOf(col[0].getBrightness()));

            WritableImage wImage = new WritableImage(DisplayImage.getImage().getPixelReader(), width, height);
            PixelWriter pixelWriter = wImage.getPixelWriter();
            PixelReader pixelReader = wImage.getPixelReader();

            for (int i = 0; i < wImage.getWidth(); i++) {
                for (int j = 0; j < wImage.getHeight(); j++) {
                    disjointarray[((j * width) + i)] = ((j * width) + i);
                    Color c = pixelReader.getColor(j, i);

                    if (
                            (c.getHue() <= (col[0].getHue() + 35) && c.getBrightness() <= (col[0].getBrightness() + 0.1) && c.getSaturation() <= (col[0].getSaturation() + 0.35)) &&
                                    (c.getHue() >= (col[0].getHue() - 35) && c.getBrightness() >= (col[0].getBrightness() - 0.1) && c.getSaturation() >= (col[0].getSaturation() - 0.35))
                    ) {
                        pixelWriter.setColor(j, i, Color.BLACK);
                    } else {
                        pixelWriter.setColor(j, i, Color.WHITE);
                        disjointarray[((j * width) + i)] = -1;
                    }
                }
            }
            EditedImage.setImage(wImage);
            wrImage = wImage;
            //calls group method
            pixelgroupthingy();
        });
    }

    //Union method from notes
    public void union(int[] a, int p, int q) {
        a[find(a, q)] = find(a, p);
    }

    //Find method from notes
    public int find(int[] a, int root) {
        if (a[root] < 0) return a[root];
        if (a[root] == root) {
            return root;
        } else {
            return find(a, a[root]);
        }
    }

    //Pixel grouper,
    public void pixelgroupthingy() {
        disjointMap = new HashMap<>();
        for (int i = 0; i < disjointarray.length; i++) {
            if (disjointarray[i] >= 0) {
                if (((i + 1) % width != 0) && (disjointarray[i + 1] != -1)) {
                    union(disjointarray, i, i + 1);
                }
                if (((i + width) < disjointarray.length) && (disjointarray[(i + width)] != -1)) {
                    union(disjointarray, i, (i + width));
                }
            }
        }
        for (int i = 0; i < disjointarray.length; i++) {
            if (disjointarray[i] != -1) {
                int realroot = find(disjointarray, disjointarray[i]);
                if (disjointMap.containsKey(realroot)) {
                    ArrayList<Integer> HashKeyValues = disjointMap.get(realroot);
                    HashKeyValues.add(i);
                } else {
                    ArrayList<Integer> HashKeyValues = new ArrayList<>();
                    HashKeyValues.add(i);
                    disjointMap.put(realroot, HashKeyValues);
                }
            }
        }
        System.out.println("\n No. Disjoint Sets: " + disjointMap.size());
    }

    //Noise Reduction
    public void FilterPixels() {
        WritableImage writableImage = wrImage;
        width = (int) DisplayImage.getImage().getWidth();
        NoiseSuppressionValue = NoiseSlider.getValue();
        for (Integer Key : disjointMap.keySet()) {
            if (disjointMap.get(Key).size() <= NoiseSuppressionValue) {
                for (int i = 0; i < disjointMap.get(Key).size(); i++) {
                    int col = disjointMap.get(Key).get(i) / width;
                    int row = disjointMap.get(Key).get(i) % width;
                    writableImage.getPixelWriter().setColor(col, row, Color.WHITE);
                }
            }
        }
        EditedImage.setImage(writableImage);
        disjointMap.keySet().removeIf(row -> disjointMap.get(row).size() <= NoiseSuppressionValue);
        System.out.println("\n Noise Suppression Value: " + NoiseSuppressionValue);
        System.out.println("Hashmap size: " + disjointMap.size());
    }

    //Ref for the Slider listener
    //https://stackoverflow.com/questions/40593284/how-can-i-define-a-method-for-slider-change-in-controller-of-a-javafx-program
    private void noiseSliderListener() {
        NoiseSlider.valueProperty().addListener((observable, noiseOld, noiseNew) -> {
            FilterPixels();
            createRectangles();
            NoiseLabel.setText(Integer.toString(noiseNew.intValue()));
        });
    }


    //Creation of Rectangles
    public void createRectangles() {
        if (!checkbox.isSelected()) {
            ((Pane) DisplayImage.getParent()).getChildren().removeIf(x -> x instanceof Rectangle);
            ((Pane) DisplayImage.getParent()).getChildren().removeIf(x -> x instanceof Text);
        }
        int noRec = 0;
        ArrayList<Integer> rectangles = new ArrayList<>();
        rectangles = componentSize();
        for (int root : rectangles) {
            ArrayList<Integer> vals = disjointMap.get(root);
            int startx, starty, endx, endy;
            startx = starty = Integer.MAX_VALUE;
            endx = endy = Integer.MIN_VALUE;
            for (int element : vals) {
                int col = element / width;
                int row = element % width;

                if (startx > col) {
                    startx = col;
                }
                if (starty > row) {
                    starty = row;
                }
                if (endx < col) {
                    endx = col;
                }
                if (endy < row) {
                    endy = row;
                }
            }
            Rectangle rectangle = new Rectangle(startx, starty, (endx - startx), (endy - starty));
            noRec++;
            double recX = rectangle.getX();
            double recY = rectangle.getY() + (endx - startx);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setStroke(Color.RED);
            rectangle.setTranslateX(DisplayImage.getLayoutX());
            rectangle.setTranslateY(DisplayImage.getLayoutY());
            Font font = Font.font("Roboto Flex", FontWeight.BOLD, FontPosture.REGULAR, 15);
            Text text = new Text();
            text.setLayoutX(startx);
            text.setLayoutY(starty + 15);
            text.setStroke(Color.WHITE);
            text.setFill(Color.RED);
            //text.setTranslateX(displayimagepane.getLayoutX());
            //text.setTranslateY(displayimagepane.getLayoutY());
            text.setFont(font);
            text.setText(String.valueOf(rectangles.indexOf(root) + 1));

            Tooltip tooltip = new Tooltip(partNames.getText() + "\nComponent Number: " + (rectangles.indexOf(root) + 1) + "\nComponent Size: " + disjointMap.get(root).size());
            Tooltip.install(rectangle, tooltip);

            double Rectanglesize = rectangle.getHeight() * rectangle.getWidth();
            ((Pane) DisplayImage.getParent()).getChildren().add(rectangle);
            ((Pane) DisplayImage.getParent()).getChildren().add(text);
            System.out.println("Start x " + startx);
            System.out.println("Start y " + starty);
            System.out.println("End x " + endx);
            System.out.println("End y " + endy);

        }
        DisplayRecCount.setText(String.valueOf(noRec));
    }

    //selection sort
    private ArrayList<Integer> componentSize() {
        ArrayList<Integer> keys = new ArrayList<>(disjointMap.keySet());
        for (int i = 0; i < keys.size() - 1; i++) {
            for (int j = keys.size() - 1; j > i; j--) {
                int big = disjointMap.get(keys.get(i)).size();
                int bigger = disjointMap.get(keys.get(j)).size();
                if (bigger > big) {
                    int tempbig = keys.get(i);
                    keys.set(i, keys.get(j));
                    keys.set(j, tempbig);
                }

            }

        }
        return keys;

    }

    //Random Colour generator and Applies it to the black and white image
    @FXML
    private void randomcolor() {
        PixelReader pixelReader = EditedImage.getImage().getPixelReader();
        WritableImage writableImage = new WritableImage(width, height);
        Random random = new Random();
        float r = random.nextFloat();
        float g = random.nextFloat();
        float b = random.nextFloat();
        Color color = new Color(r, g, b, 1);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double red = pixelReader.getColor(i, j).getRed();
                double green = pixelReader.getColor(i, j).getGreen();
                double blue = pixelReader.getColor(i, j).getBlue();
                if ((red != Color.WHITE.getRed()) && (green != Color.WHITE.getGreen()) && (blue != Color.WHITE.getBlue())) {
                    writableImage.getPixelWriter().setColor(i, j, color);
                } else {
                    writableImage.getPixelWriter().setColor(i, j, Color.WHITE);
                }
            }
            EditedImage.setImage(writableImage);
        }
    }

    //Applies the sampled Colour to the parts in the black and white image
    @FXML
    private void sampleColour() {
        PixelReader pixelReader = EditedImage.getImage().getPixelReader();
        PixelReader pixelReader1 = DisplayImage.getImage().getPixelReader();
        WritableImage writableImage = new WritableImage(width, height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //Checks if the pixel is black
                if (pixelReader.getColor(i, j).getRed() < 1 && pixelReader.getColor(i, j).getBlue() < 1 && pixelReader.getColor(i, j).getGreen() < 1) {
                    writableImage.getPixelWriter().setColor(i, j, pixelReader1.getColor(i, j));
                } else {
                    writableImage.getPixelWriter().setColor(i, j, Color.WHITE);
                }
            }
            EditedImage.setImage(writableImage);
        }

    }
}