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

/**
 * Internal SmartChineseAnalyzer character type constants.
 * <p><font color="#FF0000">
 * WARNING: The status of the analyzers/smartcn <b>analysis.cn.smart</b> package is experimental. 
 * The APIs and file formats introduced here might change in the future and will not be 
 * supported anymore in such a case.</font>
 * </p>
 */
public class CharType {

  /**
   * Punctuation Characters
   */
  public final static int DELIMITER = 0;

  /**
   * Letters
   */
  public final static int LETTER = 1;

  /**
   * Numeric Digits
   */
  public final static int DIGIT = 2;

  /**
   * Han Ideographs
   */
  public final static int HANZI = 3;

  /**
   * Characters that act as a space
   */
  public final static int SPACE_LIKE = 4;

  /**
   * Full-Width letters
   */
  public final static int FULLWIDTH_LETTER = 5;

  /**
   * Full-Width alphanumeric characters
   */
  public final static int FULLWIDTH_DIGIT = 6;

  /**
   * Other (not fitting any of the other categories)
   */
  public final static int OTHER = 7;

}
