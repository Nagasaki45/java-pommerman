package players.heuristics;

import core.ForwardModel;
import core.GameState;
import objects.GameObject;
import utils.Types;
import utils.Vector2d;

public class LobsterHeuristics extends StateHeuristic {
    private BoardStats rootBoardStats;

    public LobsterHeuristics(GameState root) {
        rootBoardStats = new BoardStats(root);
    }

    @Override
    public double evaluateState(GameState gs) {
        boolean gameOver = gs.isTerminal();
        Types.RESULT win = gs.winner();

        // Compute a score relative to the root's state.
        BoardStats lastBoardState = new BoardStats(gs);
        double rawScore = rootBoardStats.score(lastBoardState);

        if(gameOver && win == Types.RESULT.LOSS)
            rawScore = -1;

        if(gameOver && win == Types.RESULT.WIN)
            rawScore = 1;

        return rawScore;
    }

    public static class BoardStats
    {
        int tick, nTeammates, nEnemies, blastStrength, bombsInProx;
        boolean canKick;
        int nWoods, blockedPositions;
        static double maxWoods = -1;
        static double maxBlastStrength = 10;

        double FACTOR_ENEMY;
        double FACTOR_TEAM;
        double FACTOR_WOODS = 0.1;
        double FACTOR_CANKCIK = 0.15;
        double FACTOR_BLAST = 0.15;
        GameState gameState;

        BoardStats(GameState gs) {
            nEnemies = gs.getAliveEnemyIDs().size();

            // Init weights based on game mode
            if (gs.getGameMode() == Types.GAME_MODE.FFA) {
                FACTOR_TEAM = 0;
                FACTOR_ENEMY = 0.5;
            } else {
                FACTOR_TEAM = 0.1;
                FACTOR_ENEMY = 0.4;
                nTeammates = gs.getAliveTeammateIDs().size();  // We only need to know the alive teammates in team modes
                nEnemies -= 1;  // In team modes there's an extra Dummy agent added that we don't need to care about
            }

            // Save game state information
            this.tick = gs.getTick();
            this.blastStrength = gs.getBlastStrength();
            this.canKick = gs.canKick();
            this.blockedPositions = countBlockedPositions(gs);
            this.bombsInProx = bombsInProximity(gs);

            // Count the number of wood walls
            this.nWoods = 1;
            for (Types.TILETYPE[] gameObjectsTypes : gs.getBoard()) {
                for (Types.TILETYPE gameObjectType : gameObjectsTypes) {
                    if (gameObjectType == Types.TILETYPE.WOOD)
                        nWoods++;
                }
            }
            if (maxWoods == -1) {
                maxWoods = nWoods;
            }
        }

        int bombsInProximity(GameState gs)
        {
            utils.Vector2d playerPos = gs.getPosition();
            int[][] blastStrength = gs.getBombBlastStrength();
            int bombProx = 0;
            for (int row=0; row < blastStrength.length; row++) {
                for (int col = 0; col < blastStrength[row].length; col++) {
                    int thisBombStrength = blastStrength[row][col];
                    if (thisBombStrength > 1) {
                        for (int r2 = row - thisBombStrength; r2 < row + thisBombStrength; r2++) {
                            for (int c2 = col - thisBombStrength; c2 < col + thisBombStrength; c2++) {
                                if (r2 >= 0 && r2 < blastStrength.length && c2 >= 0 && c2 < blastStrength[row].length) {
                                    if (playerPos.x == c2 && playerPos.y == r2) {
                                        bombProx = bombProx - 1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return bombProx;
        }


        boolean isBlockerTile(Types.TILETYPE type)
        {
            if(type == Types.TILETYPE.FLAMES || type == Types.TILETYPE.RIGID || type == Types.TILETYPE.BOMB || type == Types.TILETYPE.WOOD)
                return true;

            return false;
        }

        int countBlockedPositions(GameState gs)
        {
            int playerId = gs.getPlayerId();
            Types.TILETYPE[][] tiles = gs.getBoard();

            int xLength = tiles.length;
            int yLength = tiles[0].length;
            Vector2d pos = gs.getPosition();

            int numOccupiedTiles = 0;
            if(pos.x == 0 || isBlockerTile(tiles[pos.x-1][pos.y]))
                numOccupiedTiles++;

            if(pos.y == 0 || isBlockerTile(tiles[pos.x][pos.y-1]))
                numOccupiedTiles++;

            if(pos.x == xLength-1 || isBlockerTile(tiles[pos.x+1][pos.y]))
                numOccupiedTiles++;

            if(pos.y == yLength-1 || isBlockerTile(tiles[pos.x][pos.y+1]))
                numOccupiedTiles++;

            return numOccupiedTiles;

        }

        double calculateBlockedPathsMultiplier(int blockedCount)
        {
            if(blockedCount<=1)
                return 1.0;
            else if(blockedCount==2)
                return 0.5;
            else
                return 0.0;
        }


        /**
         * Computes score for a game, in relation to the initial state at the root.
         * Minimizes number of opponents in the game and number of wood walls. Maximizes blast strength and
         * number of teammates, wants to kick.
         * @param futureState the stats of the board at the end of the rollout.
         * @return a score [0, 1]
         */
        double score(BoardStats futureState)
        {
            int diffTeammates = futureState.nTeammates - this.nTeammates;
            int diffEnemies = - (futureState.nEnemies - this.nEnemies);
            int diffWoods = - (futureState.nWoods - this.nWoods);
            int diffCanKick = futureState.canKick ? 1 : 0;
            int diffBlastStrength = futureState.blastStrength - this.blastStrength;

            double score = (diffEnemies / 3.0) * FACTOR_ENEMY + diffTeammates * FACTOR_TEAM + (diffWoods / maxWoods) * FACTOR_WOODS
                    + diffCanKick * FACTOR_CANKCIK + (diffBlastStrength / maxBlastStrength) * FACTOR_BLAST;

            int maxBlockedPaths = java.lang.Math.max(this.blockedPositions, futureState.blockedPositions);
            score = score * calculateBlockedPathsMultiplier(maxBlockedPaths);

            if(futureState.bombsInProx<0 || this.bombsInProx<0)
                score = 0;

            return score;

        }
    }
}
