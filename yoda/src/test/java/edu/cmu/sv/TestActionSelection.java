package edu.cmu.sv;

import edu.cmu.sv.action.Action;
import edu.cmu.sv.action.dialog_act.RequestRephrase;
import edu.cmu.sv.action.dialog_task.DialogTask;
import edu.cmu.sv.action.dialog_task.RespondToWHQuestionTask;
import edu.cmu.sv.dialog_management.DialogManager;
import edu.cmu.sv.action.dialog_act.RequestDisambiguateRole;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.utils.EvaluationTools;
import edu.cmu.sv.utils.StringDistribution;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by David Cohen on 9/4/14.
 */
public class TestActionSelection {

    private class EvaluationResult{
        boolean correctIsTopAction;
        boolean correctInTopNActions;
        int correctActionRank;
        double correctActionRelativeReward; // reward(correct action) / reward(top action)
        Pair<Class<? extends Action>, Class<? extends Action>> confusion;

        private EvaluationResult(boolean correctIsTopAction, boolean correctInTopNActions, int correctActionRank, double correctActionRelativeReward, Pair<Class<? extends Action>, Class<? extends Action>> confusion) {
            this.correctIsTopAction = correctIsTopAction;
            this.correctInTopNActions = correctInTopNActions;
            this.correctActionRank = correctActionRank;
            this.correctActionRelativeReward = correctActionRelativeReward;
            this.confusion = confusion;
        }

        public Pair<Class<? extends Action>, Class<? extends Action>> getConfusion() {
            return confusion;
        }

        @Override
        public String toString() {
            return "correct is top action:"+correctIsTopAction+", correct in top N actions:"+correctInTopNActions+
                    ", correct action rank:"+correctActionRank+", correct action relative reward:"+
                    correctActionRelativeReward + ", confusion:"+confusion;
        }
    }

    private class TestCase{
        Map<String, SemanticsModel> hypotheses;
        StringDistribution hypothesisDistribution;
        Action bestActionDescriptor;

        private TestCase(Map<String, SemanticsModel> hypotheses, StringDistribution hypothesisDistribution, Action bestActionDescriptor) {
            this.hypotheses = hypotheses;
            this.hypothesisDistribution = hypothesisDistribution;
            this.bestActionDescriptor = bestActionDescriptor;
        }

        private EvaluationResult evaluate(List<Pair<Action, Double>> nBestActions){
            boolean correctInTopNActions = false;
            boolean correctIsTopAction = false;
            double topConfidence = nBestActions.get(0).getRight();
            int correctActionRank = -1;
            double correctActionRelativeReward = 0.0;
            for (int i = 0; i < nBestActions.size(); i++) {
                Pair<Action, Double> candidateActionAndReward = nBestActions.get(i);
                if (candidateActionAndReward.getLeft()==null)
                    continue;
                // right now I don't distinguish between parameters
                if (candidateActionAndReward.getLeft().evaluationMatch(bestActionDescriptor)) {
                    if (i==0)
                        correctIsTopAction = true;
                    correctInTopNActions = true;
                    correctActionRank = i;
                    correctActionRelativeReward = candidateActionAndReward.getRight() / topConfidence;
                    break;
                }
            }

            Pair<Class<? extends Action>, Class<? extends Action>> confusion = null;
            if (!bestActionDescriptor.getClass().equals(nBestActions.get(0).getLeft().getClass()))
                confusion = new ImmutablePair<>(bestActionDescriptor.getClass(),
                        nBestActions.get(0).getLeft().getClass());

//            nBestActions.forEach(System.out::println);
            return new EvaluationResult(correctIsTopAction, correctInTopNActions, correctActionRank, correctActionRelativeReward, confusion);
        }

    }

    @Test
    public void Test() throws InstantiationException, IllegalAccessException {

        List<TestCase> testCases = basicClarificationTestSet();
        List<EvaluationResult> evaluationResults = new LinkedList<>();
        for (TestCase testCase : testCases) {
            DialogManager dialogManager = new DialogManager();
            dialogManager.getTracker().updateDialogState(testCase.hypotheses, testCase.hypothesisDistribution, (float) 0);
            List<Pair<Action, Double>> topActions = dialogManager.selectAction();
//            System.out.println("Test case evaluation:");
            EvaluationResult result = testCase.evaluate(topActions);
            evaluationResults.add(result);
//            System.out.println(result);
        }

        // aggregate evaluation results
        EvaluationTools.ConfusionCounter confusionCounter = new EvaluationTools.ConfusionCounter<>(
                evaluationResults.stream().
                        map(EvaluationResult::getConfusion).
                        filter(x -> x != null).
                        map(x -> new ImmutablePair<>(x.getKey().getSimpleName(), x.getValue().getSimpleName())).
                        collect(Collectors.toList()));

        System.out.println("Confusion Counter:\n");
        System.out.println(confusionCounter);

    }

    private List<TestCase> basicClarificationTestSet(){
        List<TestCase> ans = new LinkedList<>();
        Map<String, SemanticsModel> utterances;
        StringDistribution weights;
        TestCase testCase;
        Map<String, String> bestDialogActionParameters;
        Action bestDialogAction;

        SemanticsModel hyp1;
        SemanticsModel hyp2;
        SemanticsModel child1;
        SemanticsModel child2;

        // test case 0: role ambiguity with different depths
        utterances = new HashMap<>();
        weights = new StringDistribution();
        bestDialogActionParameters = new HashMap<>();
        bestDialogActionParameters.put("r1", "theme.endTime");
        bestDialogActionParameters.put("r2", "toTime");
        bestDialogAction = new RequestDisambiguateRole().bindVariables(bestDialogActionParameters);

        hyp1 = new SemanticsModel();
        child1 = new SemanticsModel();
        hyp1.getSlots().put("dialogAct", "WHQuestion");
        hyp1.getSlots().put("theme", "meeting0");
        hyp1.getSlots().put("fromTime", "t0");
        hyp1.getChildren().put("meeting0", child1);
        child1.getSlots().put("endTime", "t1");
        utterances.put("hyp1", hyp1);
        weights.extend("hyp1", .6);

        hyp2 = new SemanticsModel();
        child2 = new SemanticsModel();
        hyp2.getSlots().put("dialogAct", "WHQuestion");
        hyp2.getSlots().put("theme", "meeting1");
        hyp2.getSlots().put("fromTime", "t0");
        hyp2.getSlots().put("toTime", "t1");
        hyp2.getChildren().put("meeting1", child2);
        utterances.put("hyp2", hyp2);
        weights.extend("hyp2", .4);


        testCase = new TestCase(utterances, weights, bestDialogAction);
        ans.add(testCase);

        // test case 1: respond to WH question
        utterances = new HashMap<>();
        weights = new StringDistribution();
        bestDialogActionParameters = new HashMap<>();
        bestDialogActionParameters.put("r1", "theme.endTime");
        bestDialogActionParameters.put("r2", "toTime");
        bestDialogAction = new RespondToWHQuestionTask();

        hyp1 = new SemanticsModel();
        child1 = new SemanticsModel();
        hyp1.getSlots().put("dialogAct", "WHQuestion");
        hyp1.getSlots().put("theme", "meeting0");
        hyp1.getSlots().put("fromTime", "t0");
        hyp1.getChildren().put("meeting0", child1);
        child1.getSlots().put("endTime", "t1");
        utterances.put("hyp1", hyp1);
        weights.extend("hyp1", .9);

        ((DialogTask) bestDialogAction).setTaskSpec(hyp1.deepCopy());

        hyp2 = new SemanticsModel();
        child2 = new SemanticsModel();
        hyp2.getSlots().put("dialogAct", "WHQuestion");
        hyp2.getSlots().put("theme", "meeting1");
        hyp2.getSlots().put("fromTime", "t0");
        hyp2.getSlots().put("toTime", "t1");
        hyp2.getChildren().put("meeting1", child2);
        utterances.put("hyp2", hyp2);
        weights.extend("hyp2", .1);

        testCase = new TestCase(utterances, weights, bestDialogAction);
        ans.add(testCase);



        // test case 2: ask for a rephrase
        utterances = new HashMap<>();
        weights = new StringDistribution();
        bestDialogActionParameters = new HashMap<>();
        bestDialogActionParameters.put("r1", "theme.endTime");
        bestDialogActionParameters.put("r2", "toTime");
        bestDialogAction = new RequestRephrase().bindVariables(new HashMap<>());

        hyp1 = new SemanticsModel();
        child1 = new SemanticsModel();
        hyp1.getSlots().put("dialogAct", "WHQuestion");
        hyp1.getSlots().put("theme", "meeting0");
        hyp1.getSlots().put("fromTime", "t0");
        hyp1.getChildren().put("meeting0", child1);
        child1.getSlots().put("endTime", "t1");
        utterances.put("hyp1", hyp1);
        weights.extend("hyp1", .1);

        hyp2 = new SemanticsModel();
        child2 = new SemanticsModel();
        hyp2.getSlots().put("dialogAct", "WHQuestion");
        hyp2.getSlots().put("theme", "meeting1");
        hyp2.getSlots().put("fromTime", "t0");
        hyp2.getSlots().put("toTime", "t1");
        hyp2.getChildren().put("meeting1", child2);
        utterances.put("hyp2", hyp2);
        weights.extend("hyp2", .1);

        testCase = new TestCase(utterances, weights, bestDialogAction);
        ans.add(testCase);




        return ans;
    }


}