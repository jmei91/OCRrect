package edu.dal.ocrrect;

import edu.dal.ocrrect.suggest.NgramBoundedReaderSearcher;
import edu.dal.ocrrect.suggest.NgramBoundedReaders;
import edu.dal.ocrrect.util.PathUtils;
import edu.dal.ocrrect.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @since 2017.04.20
 */
public class Preproc {
  private static Path BIGRAM_SEARCHER_FILE   = PathUtils.getTempPath("2gm.search");
  private static Path TRIGRAM_SEARCHER_FILE  = PathUtils.getTempPath("3gm.search");
  private static Path FOURGRAM_SEARCHER_FILE = PathUtils.getTempPath("4gm.search");
  private static Path FIVEGRAM_SEARCHER_FILE = PathUtils.getTempPath("5gm.search");

  /**
   * Generate n-gram searchers using n-gram resources (n &gt; 1).
   *
   * @param ngramData path to a list of n-gram data files.
   * @param preproc path to the pre-processed searcher object file.
   * @throws IOException if I/O error occurs
   * @throws FileNotFoundException if file not found.
   */
  public static void genNgramSearcher(List<Path> ngramData, Path preproc)
      throws FileNotFoundException, IOException {
    NgramBoundedReaderSearcher searcher = new NgramBoundedReaderSearcher(ngramData);
    NgramBoundedReaders.write(searcher, preproc);
  }

  public static void main(String[] args) throws Exception {
    Files.createDirectories(PathUtils.TEMP_DIR);
    genNgramSearcher(ResourceUtils.BIGRAM,   BIGRAM_SEARCHER_FILE);
    genNgramSearcher(ResourceUtils.TRIGRAM,  TRIGRAM_SEARCHER_FILE);
    genNgramSearcher(ResourceUtils.FOURGRAM, FOURGRAM_SEARCHER_FILE);
    genNgramSearcher(ResourceUtils.FIVEGRAM, FIVEGRAM_SEARCHER_FILE);
  }
}
