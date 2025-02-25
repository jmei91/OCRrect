package edu.dal.ocrrect.suggest.batch;

import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.dal.ocrrect.util.Context;
import edu.dal.ocrrect.util.Word;

/**
 * @since 2017.04.20
 */
public interface ContextSensitiveBatchSuggestMixin extends BatchSuggestMixin {
  /**
   * Generate suggestions for a list of contexts with the same first gram.
   *
   * @param first the first gram.
   * @param context a list of context starting which first gram is {@code first}.
   * @return a map from suggested candidates to their according suggestion confidence values.
   */
  List<TObjectFloatMap<String>> suggest(String first, List<Context> contexts);

  /**
   * Define the size of the suggestion context. This method will be used in context creation during
   * benchmark.
   *
   * @return the size of the n-gram in context.
   */
  int suggestionContextSize();

  /**
   * Generate suggestions for one context.
   *
   * <p>This method is a shortcut for suggesting one context in the benchmark approach. This method
   * calls {@link #suggest(String, List)} in the subroutine.
   *
   * @param context a context.
   * @return a map from suggested candidates to their according suggestion confidence values.
   */
  default TObjectFloatMap<String> suggest(Context context) {
    return suggest(context.words()[0], Arrays.asList(context)).get(0);
  }

  /**
   * Merge the {@code <k, v>} pairs from two maps into one. Note that input map could potentially be
   * a {@code null} object.
   *
   * @param mapA a map.
   * @param mapB another map.
   * @return a map which includes all the mappings from both input maps.
   */
  default TObjectFloatMap<String> mergeContextSuggests(
      TObjectFloatMap<String> mapA, TObjectFloatMap<String> mapB) {
    TObjectFloatMap<String> merged = new TObjectFloatHashMap<>();
    Arrays.asList(mapA, mapB).forEach(map -> {
      if (map != null) {
        map.keySet().forEach(k -> {
          float freq = map.get(k);
          merged.adjustOrPutValue(k, freq, freq);
        });
      }
    });
    return merged;
  }

  @Override
  default List<TObjectFloatMap<String>> suggest(List<Word> words) {
    // Construct a mapping from word to ngram contexts start with such word.
    Map<String, List<Context>> wordContextMap = new HashMap<>();
    words.forEach(w -> {
      w.getContexts(suggestionContextSize()).forEach(c -> {
        String first = c.words()[0];
        if (first.length() != 0) {  // Omit n-grams with an empty first gram.
          List<Context> contexts = null;
          if ((contexts = wordContextMap.get(first)) == null) {
            contexts = new ArrayList<>();
            wordContextMap.put(first, contexts);
          }
          contexts.add(c);
        }
      });
    });

    // Create search benchmarks for contexts separated by their first words.
    // Compute using the parallel streaming approach.
    Map<Context, TObjectFloatMap<String>> suggestMap = Collections.synchronizedMap(new HashMap<>());
    wordContextMap
        .keySet()
        .parallelStream()
        .forEach(str -> {
          List<Context> contextList = wordContextMap.get(str);
          List<TObjectFloatMap<String>> mapList = suggest(str, contextList);
          for (int i = 0; i < contextList.size(); i++) {
            suggestMap.put(contextList.get(i), mapList.get(i));
          }
        });

    // Collect the results.
    return words.stream()
      .map(w -> w.getContexts(suggestionContextSize()).stream()
        .map(suggestMap::get)
        .reduce(new TObjectFloatHashMap<String>(), (a, b) -> {
          return mergeContextSuggests(a, b);
        }))
      .collect(Collectors.toList());
  }

  @Override
  default TObjectFloatMap<String> suggest(Word word) {
    return suggest(Arrays.asList(word)).get(0);
  }
}
