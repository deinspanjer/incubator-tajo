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

package org.apache.tajo.catalog;

import org.apache.tajo.catalog.partition.PartitionDesc;
import org.apache.tajo.catalog.partition.Specifier;
import org.apache.tajo.catalog.proto.CatalogProtos;
import org.apache.tajo.common.TajoDataTypes;

import java.util.Map;

public class DDLBuilder {

  public static String buildDDL(TableDesc desc) {
    StringBuilder sb = new StringBuilder();

    sb.append("--\n")
      .append("-- Name: ").append(desc.getName()).append("; Type: TABLE;")
      .append(" Storage: ").append(desc.getMeta().getStoreType().name());
    sb.append("\n-- Path: ").append(desc.getPath());
    sb.append("\n--\n");
    sb.append("CREATE EXTERNAL TABLE ").append(desc.getName());
    buildSchema(sb, desc.getSchema());
    buildUsingClause(sb, desc.getMeta());
    buildWithClause(sb, desc.getMeta());
    buildLocationClause(sb, desc);

    if (desc.getPartitions() != null) {
      buildPartitionClause(sb, desc);
    }

    sb.append(";");
    return sb.toString();
  }

  private static void buildSchema(StringBuilder sb, Schema schema) {
    boolean first = true;

    sb.append(" (");
    for (Column column : schema.toArray()) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }

      sb.append(column.getColumnName()).append(" ");
      TajoDataTypes.DataType dataType = column.getDataType();
      sb.append(dataType.getType().name());
      if (column.getDataType().hasLength() && column.getDataType().getLength() > 0) {
        sb.append(" (").append(column.getDataType().getLength()).append(")");
      }
    }
    sb.append(")");
  }

  private static void buildUsingClause(StringBuilder sb, TableMeta meta) {
    sb.append(" USING " + meta.getStoreType().name());
  }

  private static void buildWithClause(StringBuilder sb, TableMeta meta) {
    Options options = meta.getOptions();
    if (options != null && options.size() > 0) {
      boolean first = true;
      sb.append(" WITH (");
      for (Map.Entry<String, String> entry : meta.getOptions().getAllKeyValus().entrySet()) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append("'").append(entry.getKey()).append("'='").append(entry.getValue()).append("'");
      }
      sb.append(")");
    }
  }

  private static void buildLocationClause(StringBuilder sb, TableDesc desc) {
    sb.append(" LOCATION '").append(desc.getPath()).append("'");
  }

  private static void buildPartitionClause(StringBuilder sb, TableDesc desc) {
    PartitionDesc partitionDesc = desc.getPartitions();

    sb.append(" PARTITION BY ");
    sb.append(partitionDesc.getPartitionsType().name());

    // columns
    sb.append("(");
    int columnCount = 0;
    for(Column column: partitionDesc.getColumns()) {
      for(Column targetColumn: desc.getSchema().getColumns()) {
        if (column.getColumnName().equals(targetColumn.getColumnName()))  {
          if (columnCount > 0)
            sb.append(",");

          sb.append(column.getColumnName());
          columnCount++;
        }
      }
    }
    sb.append(")");

    // specifier
    if (partitionDesc.getSpecifiers() != null
        && !partitionDesc.getPartitionsType().equals(CatalogProtos.PartitionsType.COLUMN)) {

      sb.append(" (");
      for(int i = 0; i < partitionDesc.getSpecifiers().size(); i++) {
        Specifier specifier = partitionDesc.getSpecifiers().get(i);
        if (i > 0)
          sb.append(",");

        sb.append(" PARTITION");

        if (!specifier.getName().isEmpty())
          sb.append(" ").append(specifier.getName());

        if (partitionDesc.getPartitionsType().equals(CatalogProtos.PartitionsType.LIST)) {
          if (!specifier.getExpressions().isEmpty()) {
            sb.append(" VALUES (");
            String[] expressions = specifier.getExpressions().split("\\,");
            for(int j = 0; j < expressions.length; j++) {
              if (j > 0)
                sb.append(",");
              sb.append("'").append(expressions[j]).append("'");
            }
            sb.append(")");

          }
        } else if (partitionDesc.getPartitionsType().equals(CatalogProtos.PartitionsType.RANGE))  {
          sb.append(" VALUES LESS THAN (");
          if (!specifier.getExpressions().isEmpty()) {
            sb.append(specifier.getExpressions());
          } else {
            sb.append("MAXVALUE");
          }
          sb.append(")");
        }
      }
      sb.append(")");
    }
  }
}
