package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.TextualUnit;

public class Text extends TextualUnit {
  public Text(String text) {
    super(text);
  }

  public edu.dal.ocrrect.Text process(Processor<edu.dal.ocrrect.Text> processor) {
    return processor.process(this);
  }

  public TextSegments segment(WordSegmenter segmentor) {
    return segmentor.segment(this);
  }
}
