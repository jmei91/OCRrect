package edu.dal.ocrrect.suggest.batch;

import edu.dal.ocrrect.suggest.feature.Detectable;
import edu.dal.ocrrect.util.Word;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 2017.04.20
 */
public interface BatchDetectMixin extends Detectable {
  /**
   * Check whether a word is an error and requires further correction.
   *
   * @param  words  A list of words.
   * @return A list of judgments for word listed in ordered. {@code true} if
   *    this word is an error; {@code false} otherwise.
   */
  default List<Boolean> detect(List<Word> words) {
    return words
        .parallelStream()
        .map(w -> detect(w))
        .collect(Collectors.toList());
  }
}
