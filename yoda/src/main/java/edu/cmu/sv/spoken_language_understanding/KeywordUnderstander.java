package edu.cmu.sv.spoken_language_understanding;

import edu.cmu.sv.dialog_state_tracking.Turn;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.utils.StringDistribution;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by David Cohen on 11/21/14.
 *
 * A simple keyword-based SLU system for quick-n-dirty tests
 *
 */
public class KeywordUnderstander implements SpokenLanguageUnderstander{
    Calendar calendar = Calendar.getInstance();

    @Override
    public void process1BestAsr(String asrResult) {
        if (asrResult.length()==0)
            return;
        String dialogAct = "Command";
        String[] words = asrResult.split(" ");
        if (Arrays.asList("is", "am", "are").contains(words[0]))
            dialogAct="YNQuestion";
        if (Arrays.asList("what", "who", "when", "where").contains(words[0]))
            dialogAct="WHQuestion";
        SemanticsModel interpretation = new SemanticsModel("{\"dialogAct\":\""+dialogAct+"\", \"verb\":{\"class\":\"HasProperty\"}}");

        // create a turn and update the DST
        Map<String, SemanticsModel> hypotheses = new HashMap<>();
        hypotheses.put("hyp1", interpretation);
        StringDistribution hypothesisDistribution = new StringDistribution();
        hypothesisDistribution.put("hyp1", 1.0);
        Turn newTurn = new Turn("user", null, hypotheses, hypothesisDistribution);
        yodaEnvironment.DstInputQueue.add(new ImmutablePair<>(newTurn, calendar.getTimeInMillis()));
//        yodaEnvironment.dst.updateDialogState(newTurn, calendar.getTimeInMillis());
    }

    @Override
    public void processNBestAsr(StringDistribution asrNBestResult) {
        process1BestAsr(asrNBestResult.getTopHypothesis());
    }

    YodaEnvironment yodaEnvironment;
    public KeywordUnderstander(YodaEnvironment yodaEnvironment) {
        this.yodaEnvironment = yodaEnvironment;
    }
}