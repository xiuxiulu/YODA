package edu.cmu.sv;

import edu.cmu.sv.database.ReferenceResolution;
import edu.cmu.sv.natural_language_generation.Grammar;
import edu.cmu.sv.ontology.OntologyRegistry;
import edu.cmu.sv.ontology.Thing;
import edu.cmu.sv.ontology.ThingWithRoles;
import edu.cmu.sv.ontology.adjective.Adjective;
import edu.cmu.sv.ontology.adjective.Expensive;
import edu.cmu.sv.ontology.misc.UnknownThingWithRoles;
import edu.cmu.sv.ontology.misc.WebResource;
import edu.cmu.sv.ontology.noun.Noun;
import edu.cmu.sv.ontology.noun.PointOfInterest;
import edu.cmu.sv.ontology.noun.poi_types.Cafe;
import edu.cmu.sv.ontology.preposition.IsCloseTo;
import edu.cmu.sv.ontology.preposition.Preposition;
import edu.cmu.sv.ontology.quality.TransientQuality;
import edu.cmu.sv.ontology.role.*;
import edu.cmu.sv.ontology.role.has_quality_subroles.HasDistance;
import edu.cmu.sv.ontology.role.has_quality_subroles.HasExpensiveness;
import edu.cmu.sv.ontology.verb.HasProperty;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.system_action.dialog_act.core_dialog_acts.WHQuestion;
import edu.cmu.sv.system_action.dialog_act.core_dialog_acts.YNQuestion;
import edu.cmu.sv.utils.StringDistribution;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by David Cohen on 10/29/14.
 */
public class TestResolveReferences {

    @Test
    public void Test() throws FileNotFoundException, UnsupportedEncodingException {
        String empty = "{\"class\":\""+UnknownThingWithRoles.class.getSimpleName()+"\"}";

        YodaEnvironment yodaEnvironment = YodaEnvironment.dialogTestingEnvironment();

        String restaurantSelectionQuery = yodaEnvironment.db.prefixes +
                "SELECT ?x WHERE { ?x rdf:type base:Restaurant . \n }";
        List<String> restaurantURIList = new LinkedList<>(yodaEnvironment.db.runQuerySelectX(restaurantSelectionQuery));

        Random r = new Random();
        for (String restaurantURI : restaurantURIList){
            // randomly insert Expensiveness
            String expensivenessInsertString = yodaEnvironment.db.prefixes +
                    "INSERT DATA {<"+restaurantURI+"> base:expensiveness "+r.nextDouble()+"}";
            try {
                Update update = yodaEnvironment.db.connection.prepareUpdate(QueryLanguage.SPARQL, expensivenessInsertString, yodaEnvironment.db.baseURI);
                update.execute();
            } catch (RepositoryException | UpdateExecutionException | MalformedQueryException e) {
                e.printStackTrace();
            }
        }

        // simple named entity reference resolution
        String sluNamedEntityChunkURI = yodaEnvironment.db.insertValue("tied house");
        JSONObject reference = SemanticsModel.parseJSON(OntologyRegistry.WebResourceWrap(sluNamedEntityChunkURI));
        SemanticsModel.wrap(reference, Noun.class.getSimpleName(), HasName.class.getSimpleName());
        System.out.println(reference.toJSONString());

        StringDistribution possibleReferences = ReferenceResolution.resolveReference(yodaEnvironment, reference);
        for (String possibleReference : possibleReferences.keySet()){
            String labelQuery = "SELECT ?x WHERE { <"+possibleReference+"> rdfs:label ?x}";
            System.out.println("--- Possible Referent: --- (score = "+possibleReferences.get(possibleReference)+")");
            System.out.println(yodaEnvironment.db.runQuerySelectX(labelQuery));
        }


        // description with a nested PP reference resolution
        sluNamedEntityChunkURI = yodaEnvironment.db.insertValue("tied house");
        reference = SemanticsModel.parseJSON(OntologyRegistry.WebResourceWrap(sluNamedEntityChunkURI));
        SemanticsModel.wrap(reference, Noun.class.getSimpleName(), HasName.class.getSimpleName());
        SemanticsModel.wrap(reference, IsCloseTo.class.getSimpleName(), InRelationTo.class.getSimpleName());
        SemanticsModel.wrap(reference, Cafe.class.getSimpleName(), HasDistance.class.getSimpleName());
        System.out.println(reference.toJSONString());

        possibleReferences = ReferenceResolution.resolveReference(yodaEnvironment, reference);
        for (String possibleReference : possibleReferences.keySet()){
            String labelQuery = "SELECT ?x WHERE { <"+possibleReference+"> rdfs:label ?x}";
            System.out.println("--- Possible Referent: --- (score = "+possibleReferences.get(possibleReference)+")");
            System.out.println(yodaEnvironment.db.runQuerySelectX(labelQuery));
        }






    }

}
