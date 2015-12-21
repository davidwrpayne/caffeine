/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache.simulator.admission.countmin4;

import javax.annotation.Nonnegative;

import com.typesafe.config.Config;

/**
 * A sketch where the aging process is a periodic reset.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class PeriodicResetCountMin4 extends CountMin4 {
  static final long ONE_MASK = 0x1111111111111111L;

  int additions;
  int period;

  public PeriodicResetCountMin4(Config config) {
    super(config);
  }

  @Override
  public void ensureCapacity(@Nonnegative long maximumSize) {
    super.ensureCapacity(maximumSize);
    period = (maximumSize == 0) ? 10 : (10 * table.length);
    if (period <= 0) {
      period = Integer.MAX_VALUE;
    }
  }

  /**
   * Reduces every counter by half of its original value. To reduce the truncation error, the sample
   * is reduced by the number of counters with an odd value.
   */
  @Override
  void tryReset(boolean added) {
    if (!added) {
      return;
    }

    additions++;
    if (additions != period) {
      return;
    }

    int count = 0;
    for (int i = 0; i < table.length; i++) {
      count += Long.bitCount(table[i] & ONE_MASK);
      table[i] = (table[i] >>> 1) & RESET_MASK;
    }
    additions = (additions >>> 1) - (count >>> 2);
  }
}
