package eu.fbk.dh.tint.digimorph.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.digimorph.DigiMorph;

import java.util.*;

/**
 * Created by giovannimoretti on 18/05/16.
 */
public class DigiMorphAnnotator implements Annotator {

    DigiMorph dm;

    public DigiMorphAnnotator(String annotatorName, Properties prop) {
        String model_path = prop.getProperty(annotatorName + ".model");
        this.dm = DigiMorphModel.getInstance(model_path);
    }

    public void annotate(Annotation annotation) {

        List<String> token_word = new LinkedList<String>();

        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel c : tokens) {
                    token_word.add(c.word());
                }
            }

            token_word = dm.getMorphology(token_word);

            try {
                if (token_word.size() > 0) {
                    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                        for (CoreLabel c : tokens) {
                            c.set(DigiMorphAnnotations.MorphoAnnotation.class, token_word.get(0));
                            token_word.remove(0);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(token_word);
                e.printStackTrace();
            }

        }
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(DigiMorphAnnotations.MorphoAnnotation.class);
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));
    }

}


