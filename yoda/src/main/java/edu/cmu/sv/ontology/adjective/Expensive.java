package edu.cmu.sv.ontology.adjective;

import edu.cmu.sv.natural_language_generation.LexicalEntry;
import edu.cmu.sv.ontology.role.Role;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by David Cohen on 11/2/14.
 */
public class Expensive extends ExpensivenessAdjective {
    private static LexicalEntry lexicalEntry = new LexicalEntry();
    static {
        lexicalEntry.adjectives.add("expensive");
    }

    @Override
    public Set<LexicalEntry> getLexicalEntries() {
        return new HashSet<>(Arrays.asList(lexicalEntry));
    }

    @Override
    public double getCenter() {
        return 1;
    }

    @Override
    public double getSlope() {
        return 1;
    }

}