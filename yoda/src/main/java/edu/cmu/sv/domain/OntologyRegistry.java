package edu.cmu.sv.domain;

import edu.cmu.sv.domain.yoda_skeleton.ontology.Thing;
import edu.cmu.sv.domain.yoda_skeleton.ontology.adjective.Adjective;
import edu.cmu.sv.domain.yoda_skeleton.ontology.noun.Noun;
import edu.cmu.sv.domain.yoda_skeleton.ontology.preposition.Preposition;
import edu.cmu.sv.domain.yoda_skeleton.ontology.quality.TransientQuality;
import edu.cmu.sv.domain.yoda_skeleton.ontology.role.Role;
import edu.cmu.sv.domain.yoda_skeleton.ontology.verb.Verb;

import java.util.Set;

/**
 * Created by David Cohen on 3/3/15.
 */
public abstract class OntologyRegistry {
    public abstract Set<Class<? extends Verb>> getVerbClasses();
    public abstract Set<Class<? extends Noun>> getNounClasses();
    public abstract Set<Class<? extends Adjective>> getAdjectiveClasses();
    public abstract Set<Class<? extends Preposition>> getPrepositionClasses();
    public abstract Set<Class<? extends Role>> getRoleClasses();
    public abstract Set<Class<? extends TransientQuality>> getQualityClasses();
    public abstract Set<Class<? extends Thing>> getMiscClasses();
}