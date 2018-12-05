package view;

import controller.GameController;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import model.Tile;

public class BoardView extends View {

    @FXML
    private GridPane _gridPane;

    private GameController _controller;

    @Override
    protected void loadFinished() {
        _controller = this.getController(GameController.class);
        init();
    }

    public void init() {
        Tile[][] tiles = _controller.getTiles();

        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                StackPane stackPane = new StackPane();
                Rectangle rect = new Rectangle(30, 30);

                rect.setFill(tiles[x][y].getColor());
                rect.setArcWidth(10);
                rect.setArcHeight(10);

                Text text = new Text();

                var letter = tiles[x][y].getLetterType().getLetter();
                text.setText(letter.equals("") ? tiles[x][y].getType().toString() : tiles[x][y].getLetterType().getLetter());

                GridPane.setRowIndex(stackPane, x);
                GridPane.setColumnIndex(stackPane, y);

                stackPane.getChildren().addAll(rect, text);
                _gridPane.getChildren().add(stackPane);
            }
        }
    }


}
