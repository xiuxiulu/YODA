package edu.cmu.sv.spoken_language_understanding.regex_plus_keyword_understander;

import edu.cmu.sv.database.Ontology;
import edu.cmu.sv.domain.ontology.Role;
import edu.cmu.sv.domain.ontology.Verb;
import edu.cmu.sv.natural_language_generation.Lexicon;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by David Cohen on 1/21/15.
 */
public class CommandKeywordInterpreter implements MiniLanguageInterpreter {
    Verb verbClass;
    String verbRegexString = "()";
    Map<Role, String> roleObj1PrefixPatterns = new HashMap<>();
    YodaEnvironment yodaEnvironment;

    public CommandKeywordInterpreter(Verb verbClass, YodaEnvironment yodaEnvironment) {
        this.verbClass = verbClass;
        this.yodaEnvironment = yodaEnvironment;
        Set<String> verbNounStrings = new HashSet<>();
        try {
            verbNounStrings.addAll(this.yodaEnvironment.lex.getPOSForClass(verbClass, Lexicon.LexicalEntry.PART_OF_SPEECH.SINGULAR_NOUN, true));
        } catch (Lexicon.NoLexiconEntryException e) {}
        try{
            verbNounStrings.addAll(this.yodaEnvironment.lex.getPOSForClass(verbClass, Lexicon.LexicalEntry.PART_OF_SPEECH.PLURAL_NOUN, true));
        } catch (Lexicon.NoLexiconEntryException e) {}
        this.verbRegexString = "("+String.join("|",verbNounStrings)+")";
        for (Role roleClass : Ontology.roles) {
            if (Ontology.inDomain(roleClass, verbClass)) {
                try {
                    Set<String> roleObj1PrefixStrings = this.yodaEnvironment.lex.getPOSForClass(roleClass, Lexicon.LexicalEntry.PART_OF_SPEECH.AS_OBJECT_PREFIX, true);
                    String regexString = "("+String.join("|",roleObj1PrefixStrings)+")";
                    if (!regexString.equals("()"))
                        roleObj1PrefixPatterns.put(roleClass, regexString);
                } catch (Lexicon.NoLexiconEntryException e) {}
            }
        }
    }

    @Override
    public Pair<JSONObject, Double> interpret(List<String> tokens, YodaEnvironment yodaEnvironment) {
        String utterance = String.join(" ", tokens);
        if (!verbRegexString.equals("()")) {
            // command with no roles provided
            {
                Pattern regexPattern = Pattern.compile("(.* |)" + verbRegexString + "(| .*)");
                Matcher matcher = regexPattern.matcher(utterance);
                if (matcher.matches()) {
                    String jsonString = "{\"dialogAct\":\"Command\",\"verb\":{\"class\":\""+verbClass.name+"\"}}";
                    return new ImmutablePair<>(SemanticsModel.parseJSON(jsonString), RegexPlusKeywordUnderstander.keywordInterpreterWeight);
                }
            }
        }
        return null;
    }
}
