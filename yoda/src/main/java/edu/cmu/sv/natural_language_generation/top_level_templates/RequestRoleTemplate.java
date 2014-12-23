package edu.cmu.sv.natural_language_generation.top_level_templates;

import edu.cmu.sv.natural_language_generation.GenerationUtils;
import edu.cmu.sv.natural_language_generation.Template;
import edu.cmu.sv.ontology.OntologyRegistry;
import edu.cmu.sv.ontology.misc.Requested;
import edu.cmu.sv.ontology.role.Role;
import edu.cmu.sv.ontology.verb.HasProperty;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.system_action.dialog_act.slot_filling_dialog_acts.RequestRole;
import edu.cmu.sv.system_action.dialog_act.slot_filling_dialog_acts.RequestRoleGivenRole;
import edu.cmu.sv.utils.Assert;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * Created by David Cohen on 11/1/14.
 *
 * NLG template for requesting roles
 *
 */
public class RequestRoleTemplate implements Template {

    @Override
    public Map<String, JSONObject> generateAll(JSONObject constraints, YodaEnvironment yodaEnvironment, int remainingDepth) {
        SemanticsModel constraintsModel = new SemanticsModel(constraints);
        String verbClassString;
        String requestedSlotPath;
        Class<? extends Role> roleClass;
        try{
            Assert.verify(constraints.get("dialogAct").equals(RequestRole.class.getSimpleName()));
            Assert.verify(constraints.containsKey("verb"));
            JSONObject verbObject = (JSONObject)constraints.get("verb");
            verbClassString = (String)verbObject.get("class");
            Assert.verify(constraintsModel.findAllPathsToClass(Requested.class.getSimpleName()).size()==1);
            requestedSlotPath = new LinkedList<>(constraintsModel.findAllPathsToClass(Requested.class.getSimpleName())).get(0);
            String[] fillerPath = requestedSlotPath.split("\\.");
            Assert.verify(OntologyRegistry.roleNameMap.containsKey(fillerPath[fillerPath.length - 1]));
            roleClass = OntologyRegistry.roleNameMap.get(fillerPath[fillerPath.length - 1]);
        } catch (Assert.AssertException e){
            return new HashMap<>();
        }

        //todo: implement the rest


        Map<String, JSONObject> whChunks = new HashMap<>();
        whChunks.put("what", SemanticsModel.parseJSON("{}"));

        Map<String, JSONObject> verbChunks = new HashMap<>();
        Set<String> verbStrings = GenerationUtils.getPOSForClass(OntologyRegistry.thingNameMap.get(verbClassString),
                "presentSingularVerbs", yodaEnvironment);
        for (String verbString : verbStrings) {
            verbChunks.put(verbString, SemanticsModel.parseJSON(constraints.toJSONString()));
        }

        Map<String, JSONObject> descriptionChunks = yodaEnvironment.nlg.
                generateAll(patientDescription, yodaEnvironment, yodaEnvironment.nlg.grammarPreferences.maxNounPhraseDepth);

//        System.out.println("RequestAgentTemplate:\nwhChunks:"+whChunks+"\nverbChunks:"+verbChunks+"\ndescriptionChunks:"+descriptionChunks);


        Map<String, Pair<Integer, Integer>> childNodeChunks = new HashMap<>();
        childNodeChunks.put("verb.Agent", new ImmutablePair<>(1,1));
        childNodeChunks.put("verb.Patient", new ImmutablePair<>(2,2));
        return GenerationUtils.simpleOrderedCombinations(Arrays.asList(verbChunks, whChunks, descriptionChunks),
                RequestRoleTemplate::compositionFunction, childNodeChunks, yodaEnvironment);
    }

    private static JSONObject compositionFunction(List<JSONObject> children) {
        JSONObject verbPhrase = children.get(0);
        JSONObject whPhrase = children.get(1);
        JSONObject descriptionPhrase = children.get(2);
        return SemanticsModel.parseJSON(verbPhrase.toJSONString());
    }
}