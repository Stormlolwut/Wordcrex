package model;

import model.database.DocumentSession;
import model.database.classes.Clause;
import model.database.classes.TableAlias;
import model.database.enumerators.CompareMethod;
import model.database.enumerators.LinkMethod;
import model.database.services.Database;
import model.helper.Log;
import model.tables.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Add comments
 * TODO: Clean up code
 */

public class MatchOverviewModel {
    private Database _db;

    private String _username = GameSession.getUsername();

    private static HashMap<Game, Boolean> currentTurns = new HashMap<>();

    public List<Game> getCurrentPlayerGames(String username) {
        return findCurrentPlayerGame(username);
    }

    public MatchOverviewModel() {
        try {
            this._db = DocumentSession.getDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isMyTurn(Game game) throws NullPointerException{
        if(MatchOverviewModel.currentTurns.containsKey(game)){
            return MatchOverviewModel.currentTurns.get(game);
        }
        else {
            throw new NullPointerException();
        }
    }

    private List<Game> findCurrentPlayerGame(String username) {
        try {
            List<Game> games = new ArrayList<Game>();

            var clauses = new ArrayList<Clause>();

            clauses.add(new Clause(new TableAlias("game", -1),"username_player1",CompareMethod.EQUAL, username, LinkMethod.OR));
            clauses.add(new Clause(new TableAlias("game", -1),"username_player2",CompareMethod.EQUAL, username, LinkMethod.OR));

            games = _db.select(Game.class, clauses);

            for(var game : games){

                String player = GameSession.getUsername();
                String player1 =  game.player1.getUsername();
                String player2 =  game.player2.getUsername();

                boolean isPlayer1 = player.equals(player1);

                if(isPlayer1){
                    if(currentTurnHasAction(game) == false && currentTurnPlayer2HasAction(game) == false){
                        MatchOverviewModel.currentTurns.put(game,true);
                    }
                    else{
                        MatchOverviewModel.currentTurns.put(game,currentTurnHasAction(game));
                    }
                }
                else {
                    if(currentTurnHasAction(game) == false && currentTurnPlayer2HasAction(game) == false){
                        MatchOverviewModel.currentTurns.put(game,true);
                    }
                    else {
                        MatchOverviewModel.currentTurns.put(game, currentTurnPlayer2HasAction(game));
                    }
                }
            }

            return games;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }


    public void loadTurns(){

    }

    public boolean currentTurnHasAction(Game game) {
        Integer latestTurn = GetLatestTurnOfGame(game);

        var clauses = new ArrayList<Clause>();
        clauses.add(new Clause(new TableAlias("turnplayer1", -1), "username_player1", CompareMethod.EQUAL, game.player1.getUsername(), LinkMethod.AND));
        clauses.add(new Clause(new TableAlias("turnplayer1", -1), "turn_id", CompareMethod.EQUAL, latestTurn));

        try {
            var turnList = _db.select(TurnPlayer1.class, clauses);
            if(turnList.size() > 0)
            {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean currentTurnPlayer2HasAction(Game game) {
        Integer latestTurn = GetLatestTurnOfGame(game);

        var clauses = new ArrayList<Clause>();
        clauses.add(new Clause(new TableAlias("turnplayer2", -1), "username_player2", CompareMethod.EQUAL, game.player2.getUsername(), LinkMethod.AND));
        clauses.add(new Clause(new TableAlias("turnplayer2", -1), "turn_id", CompareMethod.EQUAL, latestTurn ));

        try {
            var turnList = _db.select(TurnPlayer2.class, clauses);
            if(turnList.size() > 0)
            {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    private int GetLatestTurnOfGame(Game game)
    {
        int latestTurn = 0;

        var turnClauses = new ArrayList<Clause>();
        turnClauses.add(new Clause(new TableAlias("turn", -1), "game_id", CompareMethod.EQUAL, game.getGameID()));

        try
        {
            for (Turn turn : _db.select(Turn.class, turnClauses)) {
                Integer id = turn.getTurnID();
                if(id > latestTurn)
                {
                    latestTurn = id;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return latestTurn;
    }

    public ArrayList<Game> getAllGames() {
        try {
            ArrayList<Game> games = new ArrayList<Game>();

            var clauses = new ArrayList<Clause>();

            for (Game game : _db.select(Game.class, clauses))
            {
                if(!game.gameState.isPlaying())
//                    continue;

                games.add(game);
            }

            return games;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }



    public ArrayList<Game> searchForGamesAsObserver(String gamesToSearch) {
        var clauses = new ArrayList<Clause>();
        clauses.add(new Clause(new TableAlias("game", -1), "username_player1", CompareMethod.LIKE, "%" + gamesToSearch + "%", LinkMethod.OR));
        clauses.add(new Clause(new TableAlias("game", -1), "username_player2", CompareMethod.LIKE, "%" + gamesToSearch + "%"));

        try {
            Map<Integer, Game> map = new HashMap<>();

            for (Game game : _db.select(Game.class, clauses))
            {
                if(game.gameState.isRequest())
                    continue;

                if(map.containsKey(game.getGameID()))
                    continue;

                map.put(game.getGameID(), game);
            }

            return new ArrayList<Game>(map.values());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Game> searchForGamesAsPlayer(String currentGamesToSearch) {
        var clauses = new ArrayList<Clause>();
        clauses.add(new Clause(new TableAlias("game", -1), "username_player1", CompareMethod.EQUAL, _username));
        clauses.add(new Clause(new TableAlias("game", -1), "username_player2", CompareMethod.LIKE, "%" + currentGamesToSearch + "%"));

        try {
            var foundGames = new ArrayList<Game>();
            for (Game game : _db.select(Game.class, clauses))
            {
                foundGames.add(game);
            }

            return foundGames;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<String> getPlayerRoles() {
        var clauses = new ArrayList<Clause>();
        clauses.add(new Clause(new TableAlias("accountrole", -1), "username", CompareMethod.EQUAL, _username));

        try {
            ArrayList<String> accountRoles = new ArrayList<>();

            for (AccountInfo acc : _db.select(AccountInfo.class, clauses))
            {
                accountRoles.add(acc.role.getRole());
            }

            return accountRoles;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public void surrenderGame(Game game){
        String player = GameSession.getUsername();
        String player1 =  game.player1.getUsername();
        String player2 =  game.player2.getUsername();
        String enemy  = player1.equals(player) ?  player2 : player1;
        game.setState("resigned");
        game.setWinner(enemy);

        try{
            this._db.update(game);
        }
        catch (Exception e){
            Log.error(e);
        }
    }

    public void acceptInvite(Game game){
        game.setState("playing");
        game.setAnswer("accepted");
        try{
            this._db.update(game);
        }
        catch (Exception e){
            Log.error(e);
        }
    }

    public void declineInvite(Game game){
        game.setAnswer("rejected");
        try{
            this._db.update(game);
        }
        catch (Exception e){
            Log.error(e);
        }
    }

    public GameScore getPlayerScores(Game game) {
        GameScore score = new GameScore();

        ArrayList<Integer> player1 = new ArrayList<>();
        ArrayList<Integer> player2 = new ArrayList<>();

        var clauses1 = new ArrayList<Clause>();
        clauses1.add(new Clause(new TableAlias("turnplayer1", -1), "game_id", CompareMethod.EQUAL, game.getGameID()));

        var clauses2 = new ArrayList<Clause>();
        clauses2.add(new Clause(new TableAlias("turnplayer2", -1), "game_id", CompareMethod.EQUAL, game.getGameID()));

        try {
            for (TurnPlayer1 turn1 : _db.select(TurnPlayer1.class, clauses1))
            {
                score.player1 += turn1.getScore() + turn1.getBonus();
            }

            for (TurnPlayer2 turn2 : _db.select(TurnPlayer2.class, clauses2))
            {
                score.player2 += turn2.getScore() + turn2.getBonus();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return score;
    }

    public void updateGameState(GameState state){
        try{
            _db.update(state);
        }
        catch(Exception e){

        }
    }

    public class GameScore
    {
        public int player1;
        public int player2;
    }

}
