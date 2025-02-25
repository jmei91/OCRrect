package edu.dal.ocrrect.util.lexicon;

import java.util.List;

import gnu.trove.set.hash.THashSet;

class StringSetLexicon implements Lexicon {
  private THashSet<String> dict;

  /**
   * Construct the dictionary with a set of words.
   *
   * @param set
   */
  public StringSetLexicon(THashSet<String> set) {
    dict = set;
  }

  private static THashSet<String> toTHashSet(List<String> list) {
    THashSet<String> set = new THashSet<>();
    list.forEach(w -> set.add(w));
    return set;
  }

  /**
   * Construct the dictionary with a list of words.
   *
   * @param  list  a word list.
   */
  public StringSetLexicon(List<String> list) {
    this(toTHashSet(list));
  }

  public boolean contains(String word) {
    return dict.contains(word);
  }
}
