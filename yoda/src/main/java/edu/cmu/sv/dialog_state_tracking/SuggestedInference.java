package edu.cmu.sv.dialog_state_tracking;

import edu.cmu.sv.dialog_management.DialogRegistry;
import edu.cmu.sv.ontology.misc.Suggested;
import edu.cmu.sv.ontology.role.HasValue;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.system_action.dialog_act.clarification_dialog_acts.RequestConfirmValue;
import edu.cmu.sv.system_action.dialog_act.clarification_dialog_acts.RequestDisambiguateValues;
import edu.cmu.sv.system_action.dialog_act.core_dialog_acts.Fragment;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by David Cohen on 10/17/14.
 *
 * If a clarification dialog act is spoken which includes a suggested value,
 * Extend the dialog state by copying over the new content,
 * and wrapping the suggestion in a Suggested entity description
 *
 */
public class SuggestedInference implements DiscourseUnitUpdateInference {
    static Double penaltyForReinterpretingFragment = .9;
    @Override
    public DiscourseUnit2 applyAll(DiscourseUnit2.DialogStateHypothesis currentState, Turn turn, float timeStamp) {
        int newDUHypothesisCounter = 0;
        DiscourseUnit2 ans = new DiscourseUnit2();

        // if the opposite speaker has not yet said anything in this DU,
        // the SuggestedInference doesn't make sense
        if ((turn.speaker.equals("user") && currentState.timeOfLastActByMe==null) ||
                (turn.speaker.equals("system") && currentState.timeOfLastActByThem==null))
            return ans;

        if (turn.speaker.equals("user")){
            for (String sluHypothesisID : turn.hypothesisDistribution.keySet()){
                SemanticsModel hypModel = turn.hypotheses.get(sluHypothesisID);
                String dialogAct = hypModel.getSlotPathFiller("dialogAct");

                // Don't currently support interpreting fragments which are conjunctions,
                // since they correspond to different dialog acts
                if (DialogRegistry.dialogActNameMap.get(dialogAct).equals(Fragment.class)){
                    if ("Or".equals(hypModel.newGetSlotPathFiller("topic.class")) ||
                            "And".equals(hypModel.newGetSlotPathFiller("topic.class")))
                        continue;

                    JSONObject daContent = (JSONObject) hypModel.newGetSlotPathFiller("topic");

                    Map<String, Double> attachmentPoints = Utils.findPossiblePointsOfAttachment(
                            currentState.getSpokenByThem(), daContent);
                    SemanticsModel wrapped = new SemanticsModel(daContent.toJSONString());
                    SemanticsModel.wrap((JSONObject) wrapped.newGetSlotPathFiller(""),
                            Suggested.class.getSimpleName(), HasValue.class.getSimpleName());

                    for (String attachmentPoint : attachmentPoints.keySet()){
                        String newDUHypothesisID = "du_hyp_" + newDUHypothesisCounter++;
                        DiscourseUnit2.DialogStateHypothesis newDUHypothesis =
                                new DiscourseUnit2.DialogStateHypothesis();
                        SemanticsModel newSpokenByThemHypothesis = currentState.getSpokenByThem().deepCopy();
                        newSpokenByThemHypothesis.extendAndOverwriteAtPoint(attachmentPoint, wrapped);
                        SemanticsModel.wrap((JSONObject) newSpokenByThemHypothesis.newGetSlotPathFiller(attachmentPoint),
                                Suggested.class.getSimpleName(), HasValue.class.getSimpleName());
                        ans.getHypothesisDistribution().put(newDUHypothesisID, attachmentPoints.get(attachmentPoint) *
                                penaltyForReinterpretingFragment);
                        newDUHypothesis.timeOfLastActByThem = timeStamp;
                        newDUHypothesis.spokenByThem = newSpokenByThemHypothesis;
                        ans.hypotheses.put(newDUHypothesisID, newDUHypothesis);
                    }
                }
            }
        } else { // if turn.speaker.equals("system")
            String dialogAct = turn.systemUtterance.getSlotPathFiller("dialogAct");

            if (DialogRegistry.dialogActNameMap.get(dialogAct).equals(RequestConfirmValue.class)){
                JSONObject daContent = (JSONObject) turn.systemUtterance.newGetSlotPathFiller("topic");
                Map<String, Double> attachmentPoints = Utils.findPossiblePointsOfAttachment(
                        currentState.getSpokenByThem(), daContent);
                SemanticsModel wrapped = new SemanticsModel(daContent.toJSONString());
                SemanticsModel.wrap((JSONObject) wrapped.newGetSlotPathFiller(""),
                        Suggested.class.getSimpleName(), HasValue.class.getSimpleName());

                for (String attachmentPoint : attachmentPoints.keySet()){
                    String newDUHypothesisID = "du_hyp_" + newDUHypothesisCounter++;
                    DiscourseUnit2.DialogStateHypothesis newDUHypothesis =
                            new DiscourseUnit2.DialogStateHypothesis();
                    SemanticsModel newSpokenByMeHypothesis = currentState.getSpokenByThem().deepCopy();
                    SemanticsModel.wrap((JSONObject)newSpokenByMeHypothesis.newGetSlotPathFiller(attachmentPoint),
                            Suggested.class.getSimpleName(), HasValue.class.getSimpleName());
                    newSpokenByMeHypothesis.extendAndOverwriteAtPoint(attachmentPoint, wrapped);
                    ans.getHypothesisDistribution().put(newDUHypothesisID, attachmentPoints.get(attachmentPoint));
                    newDUHypothesis.timeOfLastActByMe = timeStamp;
                    newDUHypothesis.spokenByMe = newSpokenByMeHypothesis;
                    ans.hypotheses.put(newDUHypothesisID, newDUHypothesis);
                }
            } else if (DialogRegistry.dialogActNameMap.get(dialogAct).equals(RequestDisambiguateValues.class)) {
                throw new Error("Not yet implemented: SuggestedInference for RequestDisambiguateValues dialogAct");
            }

        }
        return ans;
    }
}
