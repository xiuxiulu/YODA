package edu.cmu.sv.natural_language_generation.top_level_templates;

import edu.cmu.sv.utils.Assert;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import edu.cmu.sv.natural_language_generation.GenerationUtils;
import edu.cmu.sv.natural_language_generation.Template;
import edu.cmu.sv.ontology.misc.UnknownThingWithRoles;
import edu.cmu.sv.ontology.misc.WebResource;
import edu.cmu.sv.ontology.role.Agent;
import edu.cmu.sv.ontology.role.Patient;
import edu.cmu.sv.ontology.verb.HasProperty;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.system_action.dialog_act.core_dialog_acts.YNQuestion;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;

import java.util.*;


/**
 * Created by David Cohen on 11/13/14.
 */
public class HasPropertyYNQTemplate0 implements Template {

    @Override
    public Map<String, JSONObject> generateAll(JSONObject constraints, YodaEnvironment yodaEnvironment, int remainingDepth) {
        // required information to generate
        JSONObject agentConstraint;
        JSONObject patientConstraint;

        // ensure that the constraints match this template
        try {
            Assert.verify(constraints.keySet().size()==2);
            Assert.verify(constraints.containsKey("dialogAct") && constraints.containsKey("verb"));
            Assert.verify(constraints.get("dialogAct").equals(YNQuestion.class.getSimpleName()));
            JSONObject verbConstraint = (JSONObject) constraints.get("verb");
            Assert.verify(verbConstraint.keySet().size()==3);
            Assert.verify(verbConstraint.get("class").equals(HasProperty.class.getSimpleName()));
            Assert.verify(verbConstraint.containsKey(Agent.class.getSimpleName()));
            Assert.verify(verbConstraint.containsKey(Patient.class.getSimpleName()));
            agentConstraint = (JSONObject) verbConstraint.get(Agent.class.getSimpleName());
            Assert.verify(agentConstraint.get("class").equals(WebResource.class.getSimpleName()));
            patientConstraint = (JSONObject) verbConstraint.get(Patient.class.getSimpleName());
            Assert.verify(patientConstraint.get("class").equals(UnknownThingWithRoles.class.getSimpleName()));
        } catch (Assert.AssertException e) {
            return new HashMap<>();
        }

        Map<String, JSONObject> toBeChunks = new HashMap<>();
        toBeChunks.put("is", new JSONObject());

        Map<String, JSONObject> agentChunks = yodaEnvironment.nlg.
                generateAll(agentConstraint, yodaEnvironment, yodaEnvironment.nlg.grammarPreferences.maxNounPhraseDepth);
        Map<String, JSONObject> patientChunks = yodaEnvironment.nlg.
                generateAll(patientConstraint, yodaEnvironment, yodaEnvironment.nlg.grammarPreferences.maxNounPhraseDepth);


        Map<String, Pair<Integer, Integer>> childNodeChunks = new HashMap<>();
        childNodeChunks.put("verb."+Agent.class.getSimpleName(), new ImmutablePair<>(1,1));
        childNodeChunks.put("verb."+Patient.class.getSimpleName(), new ImmutablePair<>(2,2));
        return GenerationUtils.simpleOrderedCombinations(Arrays.asList(toBeChunks, agentChunks, patientChunks),
                HasPropertyYNQTemplate0::compositionFunction, childNodeChunks, yodaEnvironment);
    }

    private static JSONObject compositionFunction(List<JSONObject> children){
        String empty = "{\"class\":\""+UnknownThingWithRoles.class.getSimpleName()+"\"}";
        JSONObject toBe = children.get(0);
        JSONObject agent = children.get(1);
        JSONObject patient = children.get(2);

        SemanticsModel ans = new SemanticsModel("{\"dialogAct\":\""+YNQuestion.class.getSimpleName()+
                "\", \"verb\": {\"class\":\""+
                HasProperty.class.getSimpleName()+"\", \""+
                Agent.class.getSimpleName()+"\":"+empty+", \""+
                Patient.class.getSimpleName()+"\":"+empty+"}}");

        ans.extendAndOverwriteAtPoint("verb."+Agent.class.getSimpleName(), new SemanticsModel(agent.toJSONString()));
        ans.extendAndOverwriteAtPoint("verb."+Patient.class.getSimpleName(), new SemanticsModel(patient.toJSONString()));
        return ans.getInternalRepresentation();
    }
}