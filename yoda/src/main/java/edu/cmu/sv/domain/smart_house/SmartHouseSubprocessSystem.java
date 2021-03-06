package edu.cmu.sv.domain.smart_house;

import edu.cmu.sv.domain.DatabaseRegistry;
import edu.cmu.sv.domain.DomainSpec;
import edu.cmu.sv.domain.NonDialogTaskRegistry;
import edu.cmu.sv.domain.smart_house.GUI.Simulator;
import edu.cmu.sv.domain.yoda_skeleton.YodaSkeletonOntologyRegistry;
import edu.cmu.sv.domain.yoda_skeleton.YodaSkeletonLexicon;
import edu.cmu.sv.yoda_environment.SubprocessYodaSystem;

/**
 * Created by David Cohen on 3/4/15.
 */
public class SmartHouseSubprocessSystem extends SubprocessYodaSystem {
    static {
        Simulator.runningGUI = false;
        // skeleton domain
        domainSpecs.add(new DomainSpec(
                "YODA skeleton domain",
                new YodaSkeletonLexicon(),
                new YodaSkeletonOntologyRegistry(),
                new NonDialogTaskRegistry(),
                new DatabaseRegistry()));
        // smart house domain
        domainSpecs.add(new DomainSpec(
                "Smart house domain",
                new SmartHouseLexicon(),
                new SmartHouseOntologyRegistry(),
                new SmartHouseNonDialogTaskRegistry(),
                new SmartHouseDatabaseRegistry()));
    }
}


