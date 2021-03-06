package model.match;

import model.GameModel;
import model.GameSession;
import model.database.DocumentSession;
import model.database.classes.Clause;
import model.database.classes.TableAlias;
import model.database.enumerators.CompareMethod;
import model.database.enumerators.LinkMethod;
import model.database.services.Database;
import model.helper.Log;
import model.tables.AccountInfo;
import model.tables.Game;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class MatchFixer {

    private Database _db;

    private List<AccountInfo> cachedAccounts;
    private List<Game> cachedGames;

    public MatchFixer() {
        try {
            this._db = DocumentSession.getDatabase();
        } catch (SQLException e) {
            Log.error(e);
        }
    }

    public void invitePlayer(String Player) {
        try {
            var game = new Game("request", "NL", GameSession.getUsername(), Player.toLowerCase(), "unknown");
            this._db.insert(game);


            var clauses = new ArrayList<Clause>();

            clauses.add(new Clause(new TableAlias("game", -1), "username_player1", CompareMethod.EQUAL, GameSession.getUsername()));
            clauses.add(new Clause(new TableAlias("game", -1), "username_player2", CompareMethod.EQUAL, Player));

            var createdGame = this._db.select(Game.class, clauses).get(0);
            var gameModel = new GameModel(createdGame);
        } catch (Exception e) {
            Log.error(e);
        }

    }

    public List<AccountInfo> searchPlayers(String name) {
        return this.filteredPlayers(name);
    }

    private void cacheAccounts() {
        var clauses = new ArrayList<Clause>();

        clauses.add(new Clause(new TableAlias("accountrole", -1), "role", CompareMethod.EQUAL, "player"));

        try {
            this.cachedAccounts = this._db.select(AccountInfo.class, clauses);
        } catch (Exception e) {
            Log.error(e, true);
        }

    }

    private void cacheGames() {
        var clauses = new ArrayList<Clause>();

        clauses.add(new Clause(new TableAlias("game", -1), "username_player1", CompareMethod.EQUAL, GameSession.getUsername(), LinkMethod.OR));
        clauses.add(new Clause(new TableAlias("game", -1), "username_player2", CompareMethod.EQUAL, GameSession.getUsername(), LinkMethod.OR));

        try {
            this.cachedGames = this._db.select(Game.class, clauses);
        } catch (Exception e) {
            Log.error(e, true);
        }
    }

    private List<AccountInfo> filteredPlayers(String username) {
        try {
            var foundPlayers = new ArrayList<AccountInfo>();

            for (AccountInfo account : this.cachedAccounts) {
                Optional<Game> game = this.cachedGames.stream().filter(x -> x.isParticipating(account.getUsername())).findFirst();

                if (account.getUsername().toLowerCase().contains(username.toLowerCase()) && !game.isPresent())
                    foundPlayers.add(account);
            }

            return foundPlayers;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    public void refreshCache() {
        Log.info("Fetching accounts and games for invitation view...");
        this.cacheAccounts();
        this.cacheGames();
        Log.info("Fetch complete!");
    }
}