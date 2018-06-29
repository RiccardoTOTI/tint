package eu.fbk.dh.tint.readability;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.readability.en.EnglishStandardReadability;
import eu.fbk.dh.tint.readability.es.SpanishStandardReadability;
import eu.fbk.dh.tint.readability.gl.GalicianStandardReadability;
import eu.fbk.dh.tint.readability.it.ItalianStandardReadability;
import eu.fbk.utils.core.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Created by alessio on 21/09/16.
 */

public class ReadabilityAnnotator implements Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadabilityAnnotator.class);
    public static Integer DEFAULT_MAX_SENTENCE_LENGTH = 25;

    private String language;
    private String className;
    private int maxSentenceLength;

    private Properties globalProperties;
    private Properties localProperties;

    public ReadabilityAnnotator(String annotatorName, Properties props) {
        globalProperties = props;
        localProperties = PropertiesUtils.dotConvertedProperties(props, annotatorName);

        language = globalProperties.getProperty(annotatorName + ".language");
        className = globalProperties.getProperty(annotatorName + ".className");
        maxSentenceLength = PropertiesUtils
                .getInteger(localProperties.getProperty("maxSentenceLength"), DEFAULT_MAX_SENTENCE_LENGTH);
    }

    /**
     * Given an Annotation, perform a task on this Annotation.
     *
     * @param annotation
     */
    @Override
    public void annotate(Annotation annotation) {

        Readability readability = null;

        if (className != null) {
            try {
                Class<? extends Readability> obj = (Class<? extends Readability>) Class.forName(className);
                Constructor<? extends Readability> constructor = obj.getConstructor(Properties.class, Properties.class, Annotation.class);
                readability = constructor.newInstance(globalProperties, localProperties, annotation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (readability == null) {
            if (language == null) {
                LOGGER.warn("Language variable is not defined, readability will be empty");
                return;
            }

            switch (language) {
                case "it":
                    readability = new ItalianStandardReadability(globalProperties, localProperties, annotation);
                    break;
                case "es":
                    readability = new SpanishStandardReadability(globalProperties, localProperties, annotation);
                    break;
                case "en":
                    readability = new EnglishStandardReadability(globalProperties, localProperties, annotation);
                    break;
                case "gl":
                    readability = new GalicianStandardReadability(globalProperties, localProperties, annotation);
                    break;
//        default:
//            readability = new EnglishReadability();
            }
        }

        if (readability == null) {
            return;
        }

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        int tokenCount = 0;
        int goodSentencesCount = 0;
        for (CoreMap sentence : sentences) {
            int sentenceID = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
            int wordsNow = readability.getWordCount();
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

            // Long sentences with a single word
            if (tokens.size() <= 1 && sentence.get(CoreAnnotations.TextAnnotation.class).length() > 25) {
                continue;
            }

            goodSentencesCount++;
            for (CoreLabel token : tokens) {
                readability.addWord(token);
                tokenCount++;
            }
            int words = readability.getWordCount() - wordsNow;
            if (words > maxSentenceLength) {
                readability.addTooLongSentence(sentenceID);
            }
        }

        readability.setTokenCount(tokenCount);
        readability.setSentenceCount(sentences.size());
        readability.setGoodSentenceCount(goodSentencesCount);

        readability.finalizeReadability();

        annotation.set(ReadabilityAnnotations.ReadabilityAnnotation.class, readability);
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(ReadabilityAnnotations.ReadabilityAnnotation.class);
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.LemmaAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));
    }
}
