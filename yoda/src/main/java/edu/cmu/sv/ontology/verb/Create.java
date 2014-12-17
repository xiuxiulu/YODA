package edu.cmu.sv.ontology.verb;

import edu.cmu.sv.ontology.role.Agent;
import edu.cmu.sv.ontology.role.Patient;
import edu.cmu.sv.ontology.role.Role;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by David Cohen on 9/20/14.
 */
public class Create extends Verb {
    @Override
    public Set<Class<? extends Role>> getRequiredGroundedRoles() {
        return new HashSet<>(Arrays.asList(Agent.class));
    }

    @Override
    public Set<Class<? extends Role>> getRequiredDescriptions() {
        return new HashSet<>(Arrays.asList(Patient.class));
    }

}
