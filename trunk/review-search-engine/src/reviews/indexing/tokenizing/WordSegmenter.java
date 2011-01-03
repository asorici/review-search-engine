/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reviews.indexing.tokenizing;

import java.util.Collections;
import java.util.List;

import reviews.indexing.hhmm.HHMMSegmenter;
import reviews.indexing.hhmm.SegToken;
import reviews.indexing.hhmm.SegTokenFilter;

/**
 * Segment a sentence of Chinese text into words.
 * <p><font color="#FF0000">
 * WARNING: The status of the analyzers/smartcn <b>analysis.cn.smart</b> package is experimental. 
 * The APIs and file formats introduced here might change in the future and will not be 
 * supported anymore in such a case.</font>
 * </p>
 */
class WordSegmenter {

  private HHMMSegmenter hhmmSegmenter = new HHMMSegmenter();

  private SegTokenFilter tokenFilter = new SegTokenFilter();

  /**
   * Segment a sentence into words with {@link HHMMSegmenter}
   * 
   * @param sentence input sentence
   * @param startOffset start offset of sentence
   * @return {@link List} of {@link SegToken}
   */
  public List<SegToken> segmentSentence(String sentence, int startOffset) {

    List<SegToken> segTokenList = hhmmSegmenter.process(sentence);
    // tokens from sentence, excluding WordType.SENTENCE_BEGIN and WordType.SENTENCE_END
    List<SegToken> result = Collections.emptyList();
    
    if (segTokenList.size() > 2) // if its not an empty sentence
      result = segTokenList.subList(1, segTokenList.size() - 1);
    
    for (SegToken st : result)
      convertSegToken(st, sentence, startOffset);
    
    return result;
  }

  /**
   * Process a {@link SegToken} so that it is ready for indexing.
   * 
   * This method calculates offsets and normalizes the token with {@link SegTokenFilter}.
   * 
   * @param st input {@link SegToken}
   * @param sentence associated Sentence
   * @param sentenceStartOffset offset into sentence
   * @return Lucene {@link SegToken}
   */
  public SegToken convertSegToken(SegToken st, String sentence,
      int sentenceStartOffset) {

    switch (st.wordType) {
      case WordType.STRING:
      case WordType.NUMBER:
      case WordType.FULLWIDTH_NUMBER:
      case WordType.FULLWIDTH_STRING:
        st.charArray = sentence.substring(st.startOffset, st.endOffset)
            .toCharArray();
        break;
      default:
        break;
    }

    st = tokenFilter.filter(st);
    st.startOffset += sentenceStartOffset;
    st.endOffset += sentenceStartOffset;
    return st;
  }
}
