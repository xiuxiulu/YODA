package edu.cmu.sv.database;

import org.kohsuke.MetaInfServices;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * Created by David Cohen on 11/2/14.
 * Function to compute the transient distance between two physical objects.
 * NOTE: this uses 1- exp(dist) to normalize values between 0 and 1
 */
@MetaInfServices
public class DistanceFunction implements Function{
    static int R = 6371;
    @Override
    public String getURI() {
        return Database.baseURI+this.getClass().getSimpleName();
    }

    @Override
    public Value evaluate(ValueFactory valueFactory, Value... values) throws ValueExprEvaluationException {
        if (values.length != 4) {
            throw new ValueExprEvaluationException(getURI()+" requires" +
                    "exactly 4 arguments, got " + values.length);
        }

        double lat1 = ((Literal)values[0]).doubleValue();
        double lon1 = ((Literal)values[1]).doubleValue();
        double lat2 = ((Literal)values[2]).doubleValue();
        double lon2 = ((Literal)values[3]).doubleValue();
        double unscaledDistanceSq = 0.5 - Math.cos((lat2 - lat1) * Math.PI / 180)/2 +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                (1 - Math.cos((lon2 - lon1) * Math.PI / 180))/2;
        double distanceInKilometers = R * 2 * Math.asin(Math.sqrt(unscaledDistanceSq));
        return valueFactory.createLiteral(1.0 - Math.exp(-1*distanceInKilometers));
    }
}