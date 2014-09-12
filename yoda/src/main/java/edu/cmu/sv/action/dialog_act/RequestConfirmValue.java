package edu.cmu.sv.action.dialog_act;

import edu.cmu.sv.dialog_management.DiscourseUnit;
import edu.cmu.sv.dialog_management.RewardAndCostCalculator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David Cohen on 9/8/14.
 */
public class RequestConfirmValue implements DialogAct{
    private Map<String, String> boundVariables = null;
    static Map<String, String> parameters = new HashMap<>();
    static {
        parameters.put("v1", "value");
    }

    // template "<v1> ?"

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public Map<String, String> getBindings() {
        return boundVariables;
    }

    @Override
    public DialogAct bindVariables(Map<String, String> bindings) {
        boundVariables = bindings;
        return this;
    }

    @Override
    public Double reward(DiscourseUnit DU) {
        Double expectedConfidence = RewardAndCostCalculator.predictConfidenceAfterValueGain(
                DU, .5, boundVariables.get("v1"), null);

        Double relativeConfidenceGain = RewardAndCostCalculator.predictedJointToRelative(DU, expectedConfidence);
        try {
            return RewardAndCostCalculator.clarificationDialogActReward(DU, relativeConfidenceGain);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Double cost(DiscourseUnit DU) {
        // we oblige the user to a simple yes/no, which is < one phrase
        return RewardAndCostCalculator.penaltyForObligingUserPhrase*.75;
    }

    @Override
    public String toString() {
        return "RequestConfirmValue{" + "boundVariables=" + boundVariables +'}';
    }
}