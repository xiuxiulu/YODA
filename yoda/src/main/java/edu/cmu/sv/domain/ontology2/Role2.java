package edu.cmu.sv.domain.ontology2;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by David Cohen on 6/15/15.
 */
public class Role2 {
    public boolean isQualityRole;
    public String name;
    public Set<Object> domain = new HashSet<>();
    public Set<Object> range = new HashSet<>();

    public Set<Object> getDomain() {
        return domain;
    }

    public Set<Object> getRange() {
        return range;
    }

    public Role2(String name, boolean isQualityRole) {
        this.name = name;
        this.domain.addAll(domain);
        this.range.addAll(range);
        this.isQualityRole = isQualityRole;
    }
}
