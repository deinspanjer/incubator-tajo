/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tajo.algebra;

import com.google.common.base.Objects;

public class BetweenPredicate extends Expr {
  private boolean not;
  // if symmetric is not set, asymmetric is implicit.
  private boolean symmetric = false;
  private Expr predicand;
  private Expr begin;
  private Expr end;


  public BetweenPredicate(boolean not, boolean symmetric, Expr predicand, Expr begin, Expr end) {
    super(OpType.Between);
    this.not = not;
    this.symmetric = symmetric;
    this.predicand = predicand;
    this.begin = begin;
    this.end = end;
  }

  public boolean isNot() {
    return not;
  }

  public boolean isSymmetric() {
    return symmetric;
  }

  public Expr predicand() {
    return predicand;
  }

  public Expr begin() {
    return begin;
  }

  public Expr end() {
    return end;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(not, symmetric, predicand, begin, end);
  }

  @Override
  boolean equalsTo(Expr expr) {
    BetweenPredicate another = (BetweenPredicate) expr;
    return symmetric == another.symmetric && predicand.equals(another.predicand) && begin.equals(another.begin) &&
        end.equals(another.end);
  }
}
