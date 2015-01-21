package edu.cmu.sv;

import edu.cmu.sv.spoken_language_understanding.regex_plus_keyword_understander.RegexPlusKeywordUnderstander;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by David Cohen on 10/29/14.
 *
 * Generate an artificial corpus and use it to train language components (SLU / LM)
 */
public class TestRegexPlusKeywordUnderstander {
    @Test
    public void Test() throws FileNotFoundException, UnsupportedEncodingException {
        YodaEnvironment yodaEnvironment = YodaEnvironment.dialogTestingEnvironment();
        RegexPlusKeywordUnderstander understander = new RegexPlusKeywordUnderstander(yodaEnvironment);

        List<String> testUtterances = new LinkedList<>();
        testUtterances.add("is the chinese restaurant near red rock expensive");
        testUtterances.add("is the chinese restaurant near red rock cheap");
        testUtterances.add("how expensive is the chinese restaurant near red rock");
        testUtterances.add("yes");
        testUtterances.add("no");
        testUtterances.add("harvard law school");
        testUtterances.add("is expensive");
        testUtterances.add("give me directions to castro street");
        testUtterances.add("directions");

        for (String testUtterance : testUtterances){
            System.out.println("understanding utterance: " + testUtterance);
            understander.process1BestAsr(testUtterance);
        }

    }

}