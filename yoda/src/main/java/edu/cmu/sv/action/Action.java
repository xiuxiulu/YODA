package edu.cmu.sv.action;

import edu.cmu.sv.action.dialog_act.DialogAct;
import edu.cmu.sv.action.dialog_task.DialogTask;
import edu.cmu.sv.action.non_dialog_task.NonDialogTask;

import java.util.HashSet;

/**
 * Created by David Cohen on 9/11/14.
 */
public interface Action {
    /**
     * "this" is the ground truth correct action
     * evaluationMatch returns true if other is an acceptable match
     * for the purposes of some current evaluation
     */
    public default boolean evaluationMatch(Action other){
        if (!this.getClass().equals(other.getClass()))
            return false;
        if (this instanceof DialogAct) {
            DialogAct da1 = (DialogAct) this;
            DialogAct da2 = (DialogAct) other;
            return (da1.getBindings().equals(da2.getBindings())
                    && new HashSet<>(da1.getBindings().values()).equals(
                    new HashSet<>(da2.getBindings().values())));
        }
        if (this instanceof DialogTask)
            return ((DialogTask) this).getTaskSpec().equals(((DialogTask) other).getTaskSpec());
        if (this instanceof NonDialogTask)
            return ((NonDialogTask) this).getTaskSpec().equals(((NonDialogTask) other).getTaskSpec());
        return true;
    }
}