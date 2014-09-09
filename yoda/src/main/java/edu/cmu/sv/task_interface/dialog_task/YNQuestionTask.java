package edu.cmu.sv.task_interface.dialog_task;

import edu.cmu.sv.task_interface.dialog_task.DialogTask;
import edu.cmu.sv.task_interface.dialog_task.DialogTaskPreferences;

/**
 * Created by David Cohen on 9/3/14.
 *
 * This task answers a yes/no question by performing appropriate database lookups
 */
public class YNQuestionTask implements DialogTask {
    private static DialogTaskPreferences preferences = new DialogTaskPreferences(1.0, 5.0, 3.0);

    @Override
    public DialogTaskPreferences getPreferences() {
        return preferences;
    }
}