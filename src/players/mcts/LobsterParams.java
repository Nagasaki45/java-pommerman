package players.mcts;

import javafx.util.Pair;
import players.mcts.MCTSParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class LobsterParams extends MCTSParams {


    @Override
    public void setParameterValue(String param, Object value) {
        switch(param) {
            case "K": K = (double) value; break;
            case "rollout_depth": rollout_depth = (int) value; break;
            case "heuristic_method": heuristic_method = (int) value; break;
        }
    }

    @Override
    public Object getParameterValue(String param) {
        switch(param) {
            case "K": return K;
            case "rollout_depth": return rollout_depth;
            case "heuristic_method": return heuristic_method;
        }
        return null;
    }

    @Override
    public ArrayList<String> getParameters() {
        ArrayList<String> paramList = new ArrayList<>();
        paramList.add("K");
        paramList.add("rollout_depth");
        paramList.add("heuristic_method");
        return paramList;
    }

    @Override
    public Map<String, Object[]> getParameterValues() {
        HashMap<String, Object[]> parameterValues = new HashMap<>();
        parameterValues.put("K", new Double[]{1.0, Math.sqrt(2), 2.0});
        parameterValues.put("rollout_depth", new Integer[]{5, 8, 10, 12, 15});
        parameterValues.put("heuristic_method", new Integer[]{CUSTOM_HEURISTIC, ADVANCED_HEURISTIC, LOBSTER_HEURISTIC});
        return parameterValues;
    }

    @Override
    public Pair<String, ArrayList<Object>> getParameterParent(String parameter) {
        return null;  // No parameter dependencies
    }

    @Override
    public Map<Object, ArrayList<String>> getParameterChildren(String root) {
        return new HashMap<>();  // No parameter dependencies
    }

    @Override
    public Map<String, String[]> constantNames() {
        HashMap<String, String[]> names = new HashMap<>();
        names.put("heuristic_method", new String[]{"CUSTOM_HEURISTIC", "ADVANCED_HEURISTIC", "LOBSTER_HEURISTIC"});
        return names;
    }
}