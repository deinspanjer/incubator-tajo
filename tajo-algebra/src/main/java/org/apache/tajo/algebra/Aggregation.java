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
import org.apache.tajo.util.TUtil;

public class Aggregation extends UnaryOperator {
  private NamedExpr[] namedExprs;
  private GroupElement [] groups;

  public Aggregation() {
    super(OpType.Aggregation);
  }

  public NamedExpr[] getTargets() {
    return this.namedExprs;
  }

  public void setTargets(NamedExpr[] namedExprs) {
    this.namedExprs = namedExprs;
  }

  public void setGroups(GroupElement [] groups) {
    this.groups = groups;
  }

  public boolean isEmptyGrouping() {
    return groups == null || groups.length == 0;
  }

  public GroupElement [] getGroupSet() {
    return groups;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(namedExprs, groups, getChild());
  }

  @Override
  public boolean equalsTo(Expr expr) {
    Aggregation another = (Aggregation) expr;
    boolean a = TUtil.checkEquals(groups, another.groups);
    boolean b = TUtil.checkEquals(namedExprs, another.namedExprs);

    return a && b;
  }

  public static class GroupElement implements JsonSerializable {
    private GroupType group_type;
    private Expr [] grouping_sets;

    public GroupElement(GroupType groupType, Expr[] grouping_sets) {
      this.group_type = groupType;
      this.grouping_sets = grouping_sets;
    }

    public GroupType getType() {
      return this.group_type;
    }

    public Expr[] getGroupingSets() {
      return this.grouping_sets;
    }

    public String toString() {
      return toJson();
    }

    @Override
    public String toJson() {
      return JsonHelper.toJson(this);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(group_type, grouping_sets);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof GroupElement) {
        GroupElement other = (GroupElement) obj;
        return group_type.equals(other) &&
            TUtil.checkEquals(grouping_sets, other.grouping_sets);
      }

      return false;
    }
  }

  public static enum GroupType {
    OrdinaryGroup(""),
    Cube("Cube"),
    Rollup("Rollup"),
    EmptySet("()");

    String displayName;
    GroupType(String displayName) {
      this.displayName = displayName;
    }

    public String toString() {
      return displayName;
    }
  }
}
