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

/**
 * 
 */
package org.apache.tajo.engine.planner.physical;

import org.apache.tajo.engine.planner.logical.Projectable;
import org.apache.tajo.worker.TaskAttemptContext;
import org.apache.tajo.engine.eval.EvalContext;
import org.apache.tajo.engine.planner.Projector;
import org.apache.tajo.storage.Tuple;
import org.apache.tajo.storage.VTuple;

import java.io.IOException;

public class ProjectionExec extends UnaryPhysicalExec {
  private final Projectable plan;

  // for projection
  private Tuple outTuple;
  private EvalContext[] evalContexts;
  private Projector projector;
  
  public ProjectionExec(TaskAttemptContext context, Projectable plan,
      PhysicalExec child) {
    super(context, plan.getInSchema(), plan.getOutSchema(), child);
    this.plan = plan;
  }

  public void init() throws IOException {
    super.init();

    this.outTuple = new VTuple(outSchema.getColumnNum());
    this.projector = new Projector(inSchema, outSchema, this.plan.getTargets());
    this.evalContexts = projector.newContexts();
  }

  @Override
  public Tuple next() throws IOException {
    Tuple tuple = child.next();

    if (tuple ==  null) {
      return null;
    }

    projector.eval(evalContexts, tuple);
    projector.terminate(evalContexts, outTuple);
    return outTuple;
  }
}
