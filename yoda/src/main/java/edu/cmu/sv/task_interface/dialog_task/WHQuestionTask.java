package edu.cmu.sv.task_interface.dialog_task;

import edu.cmu.sv.task_interface.dialog_task.DialogTask;
import edu.cmu.sv.task_interface.dialog_task.DialogTaskPreferences;

/**
 * Created by David Cohen on 9/3/14.
 */
public class WHQuestionTask implements DialogTask {
    private static DialogTaskPreferences preferences = new DialogTaskPreferences(1,5,3);

    @Override
    public DialogTaskPreferences getPreferences() {
        return preferences;
    }
}