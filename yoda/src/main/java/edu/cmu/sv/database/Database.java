package edu.cmu.sv.database;


import edu.cmu.sv.YodaEnvironment;
import edu.cmu.sv.ontology.OntologyRegistry;
import edu.cmu.sv.ontology.Thing;
import edu.cmu.sv.ontology.ThingWithRoles;
import edu.cmu.sv.ontology.adjective.AbsoluteQualityDegree;
import edu.cmu.sv.ontology.quality.TransientQuality;
import edu.cmu.sv.ontology.preposition.TransientPairwiseRole;
import edu.cmu.sv.ontology.quality.binary_quality.TransientPairwiseQuality;
import edu.cmu.sv.ontology.verb.Verb;
import edu.cmu.sv.ontology.role.Role;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;
import org.openrdf.model.Value;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Object;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by David Cohen on 6/21/14.
 */
public class Database {

    public YodaEnvironment yodaEnvironment;
//    Repository repository;
    // a counter used to create new URIs
    private long URICounter = 0;
    RepositoryConnection connection;
    public final static String baseURI = "http://sv.cmu.edu/yoda#";
    public final static String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX base: <"+baseURI+">\n";

    public Database(YodaEnvironment yodaEnvironment) {
        this.yodaEnvironment = yodaEnvironment;
//        // non-inferencing triple store
//        repository = new SailRepository(new MemoryStore());
        // inferencing rdf database
        Repository repository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
        try {
            repository.initialize();
            connection = repository.getConnection();

            // generate the class hierarchy
            Set<Class> databaseClasses = new HashSet<>(OntologyRegistry.objectClasses);
            databaseClasses.addAll(OntologyRegistry.verbClasses);
            databaseClasses.addAll(OntologyRegistry.qualityClasses);
            Set<Class> databaseProperties = new HashSet<>(OntologyRegistry.roleClasses);
            databaseProperties.addAll(OntologyRegistry.roleClasses);
            generateClassHierarchy(databaseClasses, databaseProperties);

            // generate the special individuals
            addIndividuals(OntologyRegistry.individualNameMap);

            // load the registered databases
            for (String filename : DatabaseRegistry.turtleDatabaseSources){
                connection.add(new InputStreamReader(new FileInputStream(filename), "UTF-8"),
                        baseURI, RDFFormat.TURTLE);
            }

        } catch (RepositoryException | RDFParseException | IOException | MalformedQueryException | UpdateExecutionException e) {
            e.printStackTrace();
        }

    }

    public void addIndividuals(Map<String, Thing> individuals)
            throws UpdateExecutionException, MalformedQueryException, RepositoryException {
        String insertString = prefixes+"INSERT DATA\n{\n";
        for (String key : individuals.keySet()){
            Class<? extends Thing> cls = individuals.get(key).getClass();
            insertString += "base:"+key+" rdf:type base:"+cls.getSimpleName()+" .\n";
        }
        insertString+="}";
        Update update = connection.prepareUpdate(QueryLanguage.SPARQL, insertString);
        update.execute();
    }

    /*
    * Insert all the classes and properties of an ontology into the database.
    * Insert all direct parent-child relationships (assume that the database does its own class hierarchy inference)
    * Insert rdfs:class and rdfs:property as required
    * */
    public void generateClassHierarchy(Set<Class> classes, Set<Class> properties)
            throws MalformedQueryException, RepositoryException, UpdateExecutionException {

        String insertString = prefixes+"INSERT DATA\n{\n";
        for (Class cls : classes){
            insertString += generateTriple(cls, "rdf:type", "rdfs:Class")+".\n";
            if (cls != edu.cmu.sv.ontology.noun.Object.class && cls != Verb.class) {
                insertString += generateTriple(cls, "rdfs:subClassOf", cls.getSuperclass()) + ".\n";
            }
        }
        for (Class prop : properties){
            insertString += generateTriple(prop, "rdf:type", "rdf:Property")+".\n";
            if (prop != Role.class) {
                insertString += generateTriple(prop, "rdfs:subPropertyOf", prop.getSuperclass()) + ".\n";
            }
        }
        insertString += "}";
//        System.out.println("Creating ontology, insert string:\n" + insertString);
        Update update = connection.prepareUpdate(QueryLanguage.SPARQL, insertString);
        update.execute();
    }




    /*
    * Create a SPARQL triple for a given java triple of referents
    *
    * Each referent can be either:
    *  - a string, which means it is already grounded to a URI in the database,
    *  - a Class <? extends ontology.Thing>, which means it needs to have a string created for it
    * and needs a prefix.
    *
    * */
    public String generateTriple(Object subject, Object predicate, Object obj) {
        assert (subject instanceof String || subject instanceof Class);
        assert (predicate instanceof String || predicate instanceof Class);
        assert (obj instanceof String || obj instanceof Class);

        String ans = "";
        if (subject instanceof String) {
            ans += subject + " ";
        } else {
            ans += "base:" + ((Class) subject).getSimpleName() + " ";
        }

        if (predicate instanceof String) {
            ans += predicate + " ";
        } else {
            ans += "base:" + ((Class) predicate).getSimpleName() + " ";
        }

        if (obj instanceof String) {
            ans += obj + " ";
        } else {
            ans += "base:"+((Class)obj).getSimpleName()+" ";
        }

        return ans;
    }

    public void insertTriple(String subject, String predicate, String object)
            throws MalformedQueryException, RepositoryException, UpdateExecutionException {

        String updateString = prefixes+"INSERT DATA \n{ base:"+subject+" "+predicate+" base:"+object+" }";
//        System.out.println("attempting the following sparql update string:\n"+updateString);
        Update update = connection.prepareUpdate(QueryLanguage.SPARQL, updateString, baseURI);
        update.execute();
    }

    public void insertStatement(String updateString) {
        Update update = null;
        try {
            update = connection.prepareUpdate(QueryLanguage.SPARQL, updateString, baseURI);
            update.execute();
        } catch (RepositoryException | MalformedQueryException | UpdateExecutionException e) {
            e.printStackTrace();
        }
    }


    /*
    * Insert a data value to the triple store, return the unique identifier
    * */
    public String insertValue(Object obj) throws MalformedQueryException, RepositoryException, UpdateExecutionException {
        String newURI = "auto_generated_value_URI"+URICounter++;
        if (obj instanceof String){
            String updateString = prefixes+"INSERT DATA \n{ base:"+newURI+" rdf:value \""+obj+"\"^^xsd:string}";
//            System.out.println("Database.insertValue: updateString:"+updateString);
            Update update = connection.prepareUpdate(QueryLanguage.SPARQL, updateString, baseURI);
            update.execute();
        } else if (obj instanceof Integer){
            String updateString = prefixes+"INSERT DATA \n{ base:"+newURI+" rdf:value \""+obj+"\"^^xsd:int}";
//            System.out.println("Database.insertValue: updateString:"+updateString);
            Update update = connection.prepareUpdate(QueryLanguage.SPARQL, updateString, baseURI);
            update.execute();
        } else {
            throw new Error("Can't insertValue of that type");
        }
        return newURI;
    }

    /*
    * Run a sparql query on the database and return all values for the variable x
    * */
    public Set<String> runQuerySelectX(String queryString){
        Set<String> ans = new HashSet<>();
        try {
            TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result = query.evaluate();

            while (result.hasNext()){
                BindingSet bindings = result.next();
                ans.add(bindings.getValue("x").stringValue());
            }

        } catch (RepositoryException | QueryEvaluationException | MalformedQueryException e) {
            e.printStackTrace();
        }
        return ans;
    }

    /*
    * Run a sparql query on the database and return all value pairs for the variables x and y
    * */
    public Set<Pair<String, String>> runQuerySelectXAndY(String queryString){
        Set<Pair<String, String>> ans = new HashSet<>();
        try {
            TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result = query.evaluate();

            while (result.hasNext()){
                BindingSet bindings = result.next();
                ans.add(new ImmutablePair<>(bindings.getValue("x").stringValue(),
                        bindings.getValue("y").stringValue()));
            }

        } catch (RepositoryException | QueryEvaluationException | MalformedQueryException e) {
            e.printStackTrace();
        }
        return ans;
    }



    public static String getLocalName(String fullName){
        return fullName.split("#")[1];
    }

    public void outputEntireDatabase(){
        System.out.println("Outputting Entire Database");
        String queryString = "SELECT ?x ?y ?z WHERE {?x ?y ?z} ";
        try {
            TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result = query.evaluate();


            while (result.hasNext()){
                BindingSet bindings = result.next();
                Value valueOfX = bindings.getValue("x");
                Value valueOfY = bindings.getValue("y");
                Value valueOfZ = bindings.getValue("z");
                System.out.println(valueOfX.stringValue() + " " + valueOfY.stringValue() + " " + valueOfZ.stringValue());
            }

        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
            e.printStackTrace();
        }
    }

    public String mostSpecificClass(String entityName){
        String queryString = prefixes + "SELECT ?x WHERE { <"+entityName+"> rdf:type ?x . }";
        Set<Class> classes = runQuerySelectX(queryString).stream().
                map(Database::getLocalName).
                filter(x -> OntologyRegistry.thingNameMap.containsKey(x)).
                map(OntologyRegistry.thingNameMap::get).
                collect(Collectors.toSet());
        for (Class cls : classes){
            boolean anyChildren = false;
            for (Class cls2 : classes){
                if (cls==cls2)
                    continue;
                if (cls.isAssignableFrom(cls2)){
                    anyChildren = true;
                    break;
                }
            }
            if (!anyChildren)
                return cls.getSimpleName();
        }
        return null;
    }

    public double evaluateQualityDegree(List<String> entityURIs, Class<? extends Role> hasQualityRoleClass,
                                        JSONObject qualityDescription, Class<? extends ThingWithRoles> degreeClass){
        // use the quality description to decide on which modifiers to apply to the fuzzy map / query
        //todo: extend the: run the getQualitySparql query with the apply fuzzy map query
    }

    // todo: replace with evaluateQualityDegree
    public double evaluateAbsoluteQualityDegree(String entityURI, Class<? extends Role> hasQualityRoleClass,
                                                Class<? extends AbsoluteQualityDegree> degreeCls){
//        System.out.println(hasQualityRoleClass.getSimpleName() + ", " + degreeCls.getSimpleName());
        assert OntologyRegistry.inRange(hasQualityRoleClass, degreeCls);
        List<Double> ans = new LinkedList<>();
        try {
            AbsoluteQualityDegree tmp = degreeCls.newInstance();
            String queryString = prefixes +
                    "SELECT ?x WHERE {<"+entityURI+"> base:"+hasQualityRoleClass.getSimpleName()+" ?y . "+
                    "?y base:quantifiedAs ?actual . " +
                    "BIND(base:LinearFuzzyMap("+tmp.getCenter()+", "+tmp.getSlope()+", ?actual) AS ?x)}";
//            System.out.println(queryString);
            TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result = query.evaluate();

            while (result.hasNext()){
                BindingSet bindings = result.next();
                ans.add(Double.parseDouble(bindings.getValue("x").stringValue()));
            }

        } catch (InstantiationException | IllegalAccessException | MalformedQueryException | RepositoryException | QueryEvaluationException e) {
            e.printStackTrace();
        }
        return ans.get(0);
    }


    //TODO: overwrite if the quality is already assigned
    public void assignQuantityToEntityQuality(String entityURI, Class<? extends Role> hasQualityRoleClass,
                                              Class<? extends TransientQuality> qualityClass, double value){
        try {
            String newQualityURI = "auto_generated_quality_URI" + URICounter++;
            String updateString = prefixes + "INSERT DATA\n{<" + entityURI + "> base:" + hasQualityRoleClass.getSimpleName() +
                    " base:" + newQualityURI + " . base:" + newQualityURI + " rdf:type base:" + qualityClass.getSimpleName() +
                    " ; base:quantifiedAs " + value + " .}";
//            System.out.println(updateString);
            Update update = connection.prepareUpdate(QueryLanguage.SPARQL, updateString, baseURI);
            update.execute();
        } catch (UpdateExecutionException | RepositoryException | MalformedQueryException e) {
            e.printStackTrace();
        }
    }

    // todo: replace with evaluateQualityDegree
    public double evaluateTransientPairwiseRoleDegree(String entity1URI, String entity2URI,
                                                      Class<? extends TransientPairwiseRole> roleCls,
                                                      Class<? extends TransientPairwiseQuality> qualityCls){

        assert OntologyRegistry.inRange(qualityCls, roleCls);
        // determine the quality degree
        double transientPairwiseQualityDegree = -1.0;
        try {
            TransientPairwiseQuality tmp = qualityCls.newInstance();
            TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL,
                    tmp.getQualityCalculatorSPARQLQuery().apply(new ImmutablePair<>(entity1URI, entity2URI)));
            TupleQueryResult result = query.evaluate();

            while (result.hasNext()){
                BindingSet bindings = result.next();
                transientPairwiseQualityDegree = Double.parseDouble(bindings.getValue("x").stringValue());
                break;
            }

        } catch (InstantiationException | IllegalAccessException | MalformedQueryException | RepositoryException | QueryEvaluationException e) {
            e.printStackTrace();
        }

        for ()


    }

}
