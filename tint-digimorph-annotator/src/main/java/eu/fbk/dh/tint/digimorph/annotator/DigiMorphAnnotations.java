package eu.fbk.dh.tint.digimorph.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class DigiMorphAnnotations {

    @JSONLabel("full_morpho")
    public static class MorphoAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

    @JSONLabel("selected_morpho")
    public static class SelectedMorphoAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

    @JSONLabel("comp_morpho")
    public static class MorphoCompAnnotation implements CoreAnnotation<List<String>> {

        public Class<List<String>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }
    }

    @JSONLabel("guessed_lemma")
    public static class GuessedLemmaAnnotation implements CoreAnnotation<Boolean> {

        public Class<Boolean> getType() {
            return ErasureUtils.uncheckedCast(Boolean.class);
        }
    }

}
