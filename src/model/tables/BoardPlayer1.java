package model.tables;

import model.database.annotations.Column;
import model.database.annotations.ForeignKey;
import model.database.annotations.PrimaryKey;
import model.database.annotations.Table;

@Table("boardplayer1")
public class BoardPlayer1 {

    public TurnPlayer1 turnPlayer1;
    public Letter letter;
    public Turn turn;
    public Tile tile;
    @PrimaryKey
    @Column("game_id")
    @ForeignKey(type = TurnPlayer1.class, field = "game_id", output = "turnPlayer1")
    private Integer _gameId;
    @PrimaryKey
    @Column("username")
    @ForeignKey(type = TurnPlayer1.class, field = "username_player1", output = "turnPlayer1")
    private String _username;
    @PrimaryKey
    @Column("turn_id")
    @ForeignKey(type = TurnPlayer1.class, field = "turn_id", output = "turnPlayer1")
    private Integer _turnId;
    @PrimaryKey
    @Column("letter_id")
    @ForeignKey(type = Letter.class, field = "letter_id", output = "letter")
    private Integer _letterId;
    @Column("tile_x")
    @ForeignKey(type = Tile.class, field = "x", output = "tile")
    private Integer _tileX;
    @Column("tile_y")
    @ForeignKey(type = Tile.class, field = "y", output = "tile")
    private Integer _tileY;

    public BoardPlayer1() {
    }

    public BoardPlayer1(Integer gameId, String username, Integer turnId, Integer letterId, Integer x, Integer y) {
        this._gameId = gameId;
        this._username = username;
        this._turnId = turnId;
        this._letterId = letterId;
        this._tileX = x;
        this._tileY = y;
    }

}
