package edu.cmu.sv.system_action.dialog_act.grounding_dialog_acts;

import edu.cmu.sv.dialog_state_tracking.DialogStateHypothesis;
import edu.cmu.sv.dialog_state_tracking.DiscourseUnitHypothesis;
import edu.cmu.sv.ontology.Thing;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.system_action.SystemAction;
import edu.cmu.sv.system_action.dialog_act.DialogAct;
import edu.cmu.sv.utils.StringDistribution;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David Cohen on 9/2/14.
 *
 * Define the dialog acts for the YODA dialog system
 * Define information to support decision-making by the dialog manager, and to generate NLG input
 *
 * For class bindings, bindings are bound as Class objects
 * For individual bindings, bindings are bound as URIs
 *
 */
public abstract class ClarificationDialogAct extends DialogAct {
    private Map<String, Object> boundClasses = new HashMap<>();
    private Map<String, Object> boundIndividuals = new HashMap<>();

    public abstract Map<String, Class<? extends Thing>> getClassParameters();
    public abstract Map<String, Class<? extends Thing>> getIndividualParameters();

    public Map<String, Object> getBoundClasses(){return boundClasses;}
    public Map<String, Object> getBoundIndividuals(){return boundIndividuals;}

    public void bindVariables(Map<String, Object> bindings){
        boundClasses = new HashMap<>();
        boundIndividuals = new HashMap<>();
        for (String key : bindings.keySet()){
            if (this.getClassParameters().containsKey(key))
                boundClasses.put(key, bindings.get(key));
            else if (this.getIndividualParameters().containsKey(key))
                boundIndividuals.put(key, bindings.get(key));
            else
                throw new Error("this binding isn't a parameter for this class: "+ key + ", "+this.getClass().getSimpleName());
        }
    }

    @Override
    public Double reward(DialogStateHypothesis dialogStateHypothesis,
                         DiscourseUnitHypothesis discourseUnitHypothesis) {
        return null;
    }

    public abstract Double reward(StringDistribution dialogStateDistribution,
                                  Map<String, DialogStateHypothesis> dialogStateHypotheses);


    public SemanticsModel getNlgCommand(){
        String dA = this.getClass().getSimpleName();
        return new SemanticsModel("{\"dialogAct\":\""+dA+"\"}");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ "{" +
                "boundVariables=" + getBoundClasses() + getBoundIndividuals() +
                '}';
    }

}