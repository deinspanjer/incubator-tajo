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

package org.apache.tajo.engine.planner.logical;

import com.google.gson.annotations.Expose;
import org.apache.tajo.catalog.Column;
import org.apache.tajo.engine.planner.PlanString;
import org.apache.tajo.engine.planner.PlannerUtil;
import org.apache.tajo.engine.planner.Target;
import org.apache.tajo.util.TUtil;

public class GroupbyNode extends UnaryNode implements Projectable, Cloneable {
	@Expose private Column [] columns;
	@Expose private Target [] targets;
  @Expose private boolean hasDistinct = false;

  public GroupbyNode(int pid) {
    super(pid, NodeType.GROUP_BY);
  }

  public final boolean isEmptyGrouping() {
    return columns == null || columns.length == 0;
  }

  public void setGroupingColumns(Column [] groupingColumns) {
    this.columns = groupingColumns;
  }

	public final Column [] getGroupingColumns() {
	  return this.columns;
	}

  public final boolean isDistinct() {
    return hasDistinct;
  }

  public void setDistinct(boolean distinct) {
    hasDistinct = distinct;
  }

  @Override
  public boolean hasTargets() {
    return this.targets != null;
  }

  @Override
  public void setTargets(Target[] targets) {
    this.targets = targets;
    setOutSchema(PlannerUtil.targetToSchema(targets));
  }

  @Override
  public Target[] getTargets() {
    return this.targets;
  }
  
  public void setChild(LogicalNode subNode) {
    super.setChild(subNode);
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("\"GroupBy\": {\"grouping fields\":[");
    for (int i=0; i < columns.length; i++) {
      sb.append("\"").append(columns[i]).append("\"");
      if(i < columns.length - 1)
        sb.append(",");
    }

    if(hasTargets()) {
      sb.append(", \"target\": [");
      for (int i = 0; i < targets.length; i++) {
        sb.append("\"").append(targets[i]).append("\"");
        if( i < targets.length - 1) {
          sb.append(",");
        }
      }
      sb.append("],");
    }
    sb.append("\n  \"out schema\": ").append(getOutSchema()).append(",");
    sb.append("\n  \"in schema\": ").append(getInSchema());
    sb.append("}");
    
    return sb.toString() + "\n" + getChild().toString();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GroupbyNode) {
      GroupbyNode other = (GroupbyNode) obj;
      boolean eq = super.equals(other);
      eq = eq && TUtil.checkEquals(columns, other.columns);
      eq = eq && TUtil.checkEquals(targets, other.targets);
      return eq;
    } else {
      return false;  
    }
  }
  
  @Override
  public Object clone() throws CloneNotSupportedException {
    GroupbyNode grp = (GroupbyNode) super.clone();
    if (columns != null) {
      grp.columns = new Column[columns.length];
      for (int i = 0; i < columns.length; i++) {
        grp.columns[i] = (Column) columns[i].clone();
      }
    }

    if (targets != null) {
      grp.targets = new Target[targets.length];
      for (int i = 0; i < targets.length; i++) {
        grp.targets[i] = (Target) targets[i].clone();
      }
    }

    return grp;
  }

  @Override
  public PlanString getPlanString() {
    PlanString planStr = new PlanString("Aggregation");

    StringBuilder sb = new StringBuilder();
    sb.append("(");
    Column [] groupingColumns = columns;
    for (int j = 0; j < groupingColumns.length; j++) {
      sb.append(groupingColumns[j].getColumnName());
      if(j < groupingColumns.length - 1) {
        sb.append(",");
      }
    }

    sb.append(")");

    planStr.appendTitle(sb.toString());

    sb = new StringBuilder("target list: ");
    for (int i = 0; i < targets.length; i++) {
      sb.append(targets[i]);
      if( i < targets.length - 1) {
        sb.append(", ");
      }
    }
    planStr.addExplan(sb.toString());

    planStr.addDetail("out schema:").appendDetail(getOutSchema().toString());
    planStr.addDetail("in schema:").appendDetail(getInSchema().toString());

    return planStr;
  }
}
