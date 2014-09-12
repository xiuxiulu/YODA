package edu.cmu.sv.action.dialog_act;

import edu.cmu.sv.action.Action;
import edu.cmu.sv.dialog_management.DiscourseUnit;

import java.util.*;

/**
 * Created by David Cohen on 9/2/14.
 *
 * Define the illocutionary acts for the YODA dialog system
 *
 * Define information to support decision-making by the dialog manager
 *
 * Possibly in the future:
 *   - include templates used for NLG and SLU
 *
 */
public interface DialogAct extends Action {
    public Double reward(DiscourseUnit DU);
    public Double cost(DiscourseUnit DU);
    public Map<String, String> getParameters();
    public Map<String, String> getBindings();
    public DialogAct bindVariables(Map<String, String> bindings);

}