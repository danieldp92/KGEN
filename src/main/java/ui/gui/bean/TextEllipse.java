package ui.gui.bean;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Text;

public class TextEllipse extends StackPane {
    private final Ellipse bubble;
    private final Text text;

    public TextEllipse(String text, double x, double y, double radiusX, double radiusY)
    {
        this.setLayoutX(x - radiusX);
        this.setLayoutY(y - radiusY);
        this.setWidth(radiusX);
        this.setHeight(radiusY);
        this.text = new Text(text);
        this.text.setStyle("-fx-font: 20 calibri;");
        bubble = new Ellipse ();
        bubble.setCenterX(x);
        bubble.setCenterY(y);
        bubble.setRadiusX(radiusX);
        bubble.setRadiusY(radiusY);
        bubble.setFill(Color.WHITE);
        bubble.setStroke(Color.BLACK);
        bubble.setId(text);
        getChildren().addAll(bubble, this.text);
    }

    public Ellipse getBubble() {
        return bubble;
    }

    public Text getText() {
        return text;
    }
}
