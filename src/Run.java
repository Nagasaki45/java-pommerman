import core.Game;
import players.*;
import players.mcts.MCTSParams;
import players.mcts.MCTSPlayer;
import players.mcts.LobsterPlayer;
import players.mcts.LobsterParams;
import players.rhea.RHEALobsterPlayer;
import players.rhea.utils.RHEALobsterParams;
import players.rhea.RHEAPlayer;
import players.rhea.utils.Constants;
import players.rhea.utils.RHEAParams;
import utils.*;

import java.util.*;

import static utils.Types.VISUALS;

public class Run {

    private static void printHelp()
    {
        System.out.println("Usage: java Run [args]");
        System.out.println("\t [arg index = 0] Game Mode. 0: FFA; 1: TEAM");
        System.out.println("\t [arg index = 1] Repetitions per seed [N]");
        System.out.println("\t [arg index = 2] Vision Range [R]");
        System.out.println("\t [arg index = 3-6] Agents:");
        System.out.println("\t\t 0 DoNothing");
        System.out.println("\t\t 1 Random");
        System.out.println("\t\t 2 OSLA");
        System.out.println("\t\t 3 SimplePlayer");
        System.out.println("\t\t 4 RHEA 200 itereations, shift buffer, pop size 1, random init, length: 12, custom heuristic");
        System.out.println("\t\t 5 RHEA 200 itereations, shift buffer, pop size 1, random init, length: 12, advanced heuristic");
        System.out.println("\t\t 6 MCTS 200 iterations, length: 12, custom heuristic");
        System.out.println("\t\t 7 MCTS 200 iterations, length: 12, advanced heuristic");
        System.out.println("\t\t 8 LOBSTER");
    }

    public static void main(String[] args) {

//        Random rnd = new Random();
//        for(int i = 0; i < 20; i++)
//        {
//            System.out.println(rnd.nextInt(100000));
//        }

        long seeds[] = new long[] {93988, 19067, 64416, 83884, 55636, 27599, 44350, 87872, 40815,
                11772, 58367, 17546, 75375, 75772, 58237, 30464, 27180, 23643, 67054, 19508};


        int RHEA_CUSTOM_HEURISTIC = 0;
        int RHEA_ADVANCED_HEURISTIC = 1;

        if(args.length != 7) {
            printHelp();
            return;
        }

        try {

            // Create players
            ArrayList<Player> players = new ArrayList<>();
            int playerID = Types.TILETYPE.AGENT0.getKey();

            // Init game, size and seed.
            long seed = System.currentTimeMillis();
            int boardSize = Types.BOARD_SIZE;

            Types.GAME_MODE gMode = Types.GAME_MODE.FFA;
            if(Integer.parseInt(args[0]) == 1)
                gMode = Types.GAME_MODE.TEAM;

            Types.DEFAULT_VISION_RANGE = Integer.parseInt(args[2]);

            int N = Integer.parseInt(args[1]);
            String[] playerStr = new String[4];

            for(int i = 3; i <= 6; ++i) {
                int agentType = Integer.parseInt(args[i]);
                Player p = null;

                RHEAParams rheaParams = new RHEAParams();
                MCTSParams mctsParams = new MCTSParams();
                LobsterParams lobsterParams = new LobsterParams();
                RHEALobsterParams RHEAlobsterParams = new RHEALobsterParams();

                mctsParams.stop_type = mctsParams.STOP_ITERATIONS;
                mctsParams.rollout_depth = 12;


                //IDEA: Run -> Edit Configuration -> Programme Arugments: 0 1 -1 4 5 6 7
                switch(agentType) {
                    case 0:
                        p = new DoNothingPlayer(playerID++);
                        playerStr[i-3] = "DoNothing";
                        break;
                    case 1:
                        p = new RandomPlayer(seed, playerID++);
                        playerStr[i-3] = "Random";
                        break;
                    case 2:
                        p = new OSLAPlayer(seed, playerID++);
                        playerStr[i-3] = "OSLA";
                        break;
                    case 3:
                        p = new SimplePlayer(seed, playerID++);
                        playerStr[i-3] = "SimplePlayer";
                        break;
                    case 4:
                        rheaParams.heurisic_type = Constants.CUSTOM_HEURISTIC;
                        p = new RHEAPlayer(seed, playerID++, rheaParams);
                        playerStr[i-3] = "RHEA-Custom";
                        break;
                    case 5:
                        rheaParams.heurisic_type = Constants.CUSTOM_HEURISTIC;//Constants.ADVANCED_HEURISTIC;
                        p = new RHEAPlayer(seed, playerID++, rheaParams);
                        playerStr[i-3] = "RHEA-Advanced";
                        break;
                    case 6:
                        mctsParams.heuristic_method = mctsParams.CUSTOM_HEURISTIC;
                        p = new MCTSPlayer(seed, playerID++, mctsParams);
                        playerStr[i-3] = "MCTS-Custom";
                        break;
                    case 7:
                        mctsParams.heuristic_method = mctsParams.ADVANCED_HEURISTIC;//mctsParams.ADVANCED_HEURISTIC;
                        p = new MCTSPlayer(seed, playerID++, mctsParams);
                        playerStr[i-3] = "MCTS-ADVANCED";
                        break;
                     //SEND IN THE LOBSTER
                    case 8:
                        lobsterParams.heuristic_method = lobsterParams.LOBSTER_HEURISTIC;//LobsterParams.ADVANCED_HEURISTIC;
                        p = new LobsterPlayer(seed, playerID++, lobsterParams);
                        playerStr[i-3] = "MTCS LOBSTER";
                        break;
                    case 9:
                        p = new RHEALobsterPlayer(seed, playerID++, RHEAlobsterParams);
                        playerStr[i-3] = "RHEA LOBSTER";
                        break;
                    default:
                        System.out.println("WARNING: Invalid agent ID: " + agentType );
                }

                players.add(p);
            }

            String gameIdStr = "";
            for(int i = 0; i <= 6; ++i) {
                gameIdStr += args[i];
                if(i != 6)
                    gameIdStr+="-";
            }

            Game game = new Game(seeds[0], boardSize, gMode, gameIdStr);

            // Make sure we have exactly NUM_PLAYERS players
            assert players.size() == Types.NUM_PLAYERS;
            game.setPlayers(players);

            System.out.print(gameIdStr + " [");
            for(int i = 0; i < playerStr.length; ++i) {
                System.out.print(playerStr[i]);
                if(i != playerStr.length-1)
                    System.out.print(',');

            }
            System.out.println("]");

//            runGame(game, new KeyController(true), new KeyController(false));
            runGames(game, seeds, N, false);

        } catch(Exception e) {
            e.printStackTrace();
            printHelp();
        }
    }

    /**
     * Runs 1 game.
     * @param g - game to run
     * @param ki1 - primary key controller
     * @param ki2 - secondary key controller
     * @param separateThreads - if separate threads should be used for the agents or not.
     */
    public static void runGame(Game g, KeyController ki1, KeyController ki2, boolean separateThreads) {
        WindowInput wi = null;
        GUI frame = null;
        if (VISUALS) {
            frame = new GUI(g, "Java-Pommerman", ki1, false, true);
            wi = new WindowInput();
            wi.windowClosed = false;
            frame.addWindowListener(wi);
            frame.addKeyListener(ki1);
            frame.addKeyListener(ki2);
        }

        g.run(frame, wi, separateThreads);
    }


    public static void runGames(Game g, long seeds[], int repetitions, boolean useSeparateThreads){
        int numPlayers = g.getPlayers().size();
        int[] winCount = new int[numPlayers];
        int[] tieCount = new int[numPlayers];
        int[] lossCount = new int[numPlayers];
        int numSeeds = seeds.length;
        int totalNgames = numSeeds * repetitions;

        for(int s = 0; s<numSeeds; s++) {

            for (int i = 0; i < repetitions; i++) {
                long seed = seeds[s];

                System.out.print( seed + ", " + (s*repetitions + i) + "/" + totalNgames + ", ");

                g.reset(seed);
                EventsStatistics.REP = i;
                GameLog.REP = i;

                Types.RESULT[] results = g.run(useSeparateThreads);

                for (int pIdx = 0; pIdx < numPlayers; pIdx++) {
                    switch (results[pIdx]) {
                        case WIN:
                            winCount[pIdx]++;
                            break;
                        case TIE:
                            tieCount[pIdx]++;
                            break;
                        case LOSS:
                            lossCount[pIdx]++;
                            break;
                    }
                }
            }
        }

        //Done, show stats
        System.out.println("N \tWin \tTie \tLoss \tPlayer");
        for (int pIdx = 0; pIdx < numPlayers; pIdx++) {
            String player = g.getPlayers().get(pIdx).getClass().toString().replaceFirst("class ", "");

            double winPerc = winCount[pIdx] * 100.0 / (double)totalNgames;
            double tiePerc = tieCount[pIdx] * 100.0 / (double)totalNgames;
            double lossPerc = lossCount[pIdx] * 100.0 / (double)totalNgames;

            System.out.println(totalNgames + "\t" + winPerc + "%\t" + tiePerc + "%\t" + lossPerc + "%\t" + player );
        }
    }
}
