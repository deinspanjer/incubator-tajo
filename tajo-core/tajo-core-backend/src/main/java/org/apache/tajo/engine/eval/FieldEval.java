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

package org.apache.tajo.engine.eval;

import com.google.gson.annotations.Expose;
import org.apache.tajo.catalog.Column;
import org.apache.tajo.catalog.Schema;
import org.apache.tajo.common.TajoDataTypes.DataType;
import org.apache.tajo.datum.Datum;
import org.apache.tajo.storage.Tuple;

public class FieldEval extends EvalNode implements Cloneable {
	@Expose private Column column;
	@Expose	private int fieldId = -1;
	
	public FieldEval(String columnName, DataType domain) {
		super(EvalType.FIELD);
		this.column = new Column(columnName, domain);
	}
	
	public FieldEval(Column column) {
	  super(EvalType.FIELD);
	  this.column = column;
	}

	@Override
	public void eval(EvalContext ctx, Schema schema, Tuple tuple) {
	  if (fieldId == -1) {
	    fieldId = schema.getColumnId(column.getQualifiedName());
      if (fieldId == -1) {
        throw new IllegalStateException("No Such Column Reference: " + column + ", schema: " + schema);
      }
	  }
    FieldEvalContext fieldCtx = (FieldEvalContext) ctx;
	  fieldCtx.datum = tuple.get(fieldId);
	}

  @Override
  public Datum terminate(EvalContext ctx) {
    return ((FieldEvalContext)ctx).datum;
  }

  @Override
  public EvalContext newContext() {
    return new FieldEvalContext();
  }

  private static class FieldEvalContext implements EvalContext {
    private Datum datum;

    public FieldEvalContext() {
    }
  }

  @Override
	public DataType getValueType() {
		return column.getDataType();
	}
	
  public Column getColumnRef() {
    return column;
  }
	
	public String getQualifier() {
	  return column.getQualifier();
	}
	
	public String getColumnName() {
	  return column.getColumnName();
	}
	
	public void replaceColumnRef(String columnName) {
	  this.column.setName(columnName);
	}

	@Override
	public String getName() {
		return this.column.getQualifiedName();
	}
	
	public String toString() {
	  return this.column.toString();
	}
	
  public boolean equals(Object obj) {
    if (obj instanceof FieldEval) {
      FieldEval other = (FieldEval) obj;
      
      return column.equals(other.column);      
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return column.hashCode();
  }
  
  @Override
  public Object clone() throws CloneNotSupportedException {
    FieldEval eval = (FieldEval) super.clone();
    eval.column = (Column) this.column.clone();
    eval.fieldId = fieldId;
    
    return eval;
  }

  public void preOrder(EvalNodeVisitor visitor) {
    visitor.visit(this);
  }
  
  @Override
  public void postOrder(EvalNodeVisitor visitor) {
    visitor.visit(this);
  }
}