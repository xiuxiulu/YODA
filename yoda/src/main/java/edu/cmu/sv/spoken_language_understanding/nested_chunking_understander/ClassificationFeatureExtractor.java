package edu.cmu.sv.spoken_language_understanding.nested_chunking_understander;

import org.json.simple.JSONObject;

import java.util.List;

/**
 * Created by David Cohen on 12/29/14.
 */
public interface ClassificationFeatureExtractor {
    public List<Double> generateFeatures(NestedChunkingUnderstander.NodeClassificationProblem classificationProblem);
}
