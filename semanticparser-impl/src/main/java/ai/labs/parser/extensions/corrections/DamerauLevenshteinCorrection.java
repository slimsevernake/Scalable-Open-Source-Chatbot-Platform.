package ai.labs.parser.extensions.corrections;

import ai.labs.parser.extensions.corrections.similarities.DamerauLevenshteinDistance;
import ai.labs.parser.extensions.corrections.similarities.IDistanceCalculator;
import ai.labs.parser.extensions.dictionaries.IDictionary;
import ai.labs.parser.model.FoundWord;
import ai.labs.parser.model.Word;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ginccc
 */
@NoArgsConstructor
public class DamerauLevenshteinCorrection implements ICorrection {
    private int maxDistance = 2;
    private boolean lookupIfKnown;
    private final IDistanceCalculator distanceCalculator = new DamerauLevenshteinDistance();
    private List<IDictionary> dictionaries;

    public DamerauLevenshteinCorrection(int maxDistance, boolean lookupIfKnown) {
        this.maxDistance = maxDistance;
        this.lookupIfKnown = lookupIfKnown;
    }

    @Override
    public void init(List<IDictionary> dictionaries) {
        this.dictionaries = dictionaries;
    }

    @Override
    public List<IDictionary.IFoundWord> correctWord(String word) {
        return correctWord(word, new LinkedList<>());
    }

    @Override
    public List<IDictionary.IFoundWord> correctWord(String lookup, List<IDictionary> temporaryDictionaries) {
        List<WordDistanceWrapper> foundWords = new LinkedList<>();
        lookup = lookup.toLowerCase();

        List<IDictionary> allDictionaries = new LinkedList<>();
        allDictionaries.addAll(temporaryDictionaries);
        allDictionaries.addAll(dictionaries);

        for (IDictionary dictionary : allDictionaries) {
            for (IDictionary.IWord word : dictionary.getWords()) {
                final int distance = calculateDistance(lookup, word.getValue().toLowerCase());

                if (distance > -1) {
                    Word entry = new Word(word.getValue(),
                            word.getExpressions(),
                            word.getIdentifier(),
                            word.getFrequency(),
                            word.isPartOfPhrase());

                    foundWords.add(new WordDistanceWrapper(distance, entry));
                }
            }
        }

        Collections.sort(foundWords);

        return foundWords.stream().map(foundWord -> {
            double matchingAccuracy = 1.0 - foundWord.distance;
            return new FoundWord(foundWord.word, true, matchingAccuracy);
        }).collect(Collectors.toList());
    }

    @Override
    public boolean lookupIfKnown() {
        return lookupIfKnown;
    }

    private int calculateDistance(String inputPart, String word) {
        int lengthWord = word.length();
        int lengthPart = inputPart.length();
        int distance;
        if (lengthWord < (lengthPart - maxDistance) || lengthWord > (lengthPart + maxDistance) ||
                (distance = distanceCalculator.calculate(word, inputPart)) > maxDistance) {
            distance = -1;
        }

        return distance;
    }

    private static class WordDistanceWrapper implements Comparable<WordDistanceWrapper> {
        private WordDistanceWrapper(int distance, IDictionary.IWord word) {
            this.distance = distance;
            this.word = word;
        }

        private final int distance;
        private final IDictionary.IWord word;

        @Override
        public int compareTo(WordDistanceWrapper o) {
            return Integer.compare(distance, o.distance);
        }
    }
}
