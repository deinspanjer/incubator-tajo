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

import org.apache.hadoop.fs.Path;
import org.apache.tajo.catalog.function.Function;
import org.apache.tajo.catalog.partition.PartitionDesc;
import org.apache.tajo.catalog.partition.Specifier;
import org.apache.tajo.catalog.proto.CatalogProtos;
import org.apache.tajo.catalog.proto.CatalogProtos.FunctionType;
import org.apache.tajo.catalog.proto.CatalogProtos.IndexMethod;
import org.apache.tajo.catalog.proto.CatalogProtos.StoreType;
import org.apache.tajo.common.TajoDataTypes;
import org.apache.tajo.common.TajoDataTypes.Type;
import org.apache.tajo.conf.TajoConf;
import org.apache.tajo.util.CommonTestingUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TestCatalog {
	static final String FieldName1="f1";
	static final String FieldName2="f2";
	static final String FieldName3="f3";	

	Schema schema1;
	
	static CatalogServer server;
	static CatalogService catalog;

	@BeforeClass
	public static void setUp() throws Exception {
    TajoConf conf = new TajoConf();

    conf.set(CatalogConstants.CATALOG_URI, "jdbc:derby:target/test-data/TestCatalog/db;create=true");
    conf.setVar(TajoConf.ConfVars.CATALOG_ADDRESS, "127.0.0.1:0");

	  server = new CatalogServer();
    server.init(conf);
    server.start();
    catalog = new LocalCatalogWrapper(server);
	}
	
	@AfterClass
	public static void tearDown() throws IOException {
	  server.stop();
	}
	
	@Test
	public void testGetTable() throws Exception {
		schema1 = new Schema();
		schema1.addColumn(FieldName1, Type.BLOB);
		schema1.addColumn(FieldName2, Type.INT4);
		schema1.addColumn(FieldName3, Type.INT8);
    Path path = new Path(CommonTestingUtil.getTestDir(), "table1");
    TableDesc meta = CatalogUtil.newTableDesc(
        "getTable",
        schema1,
        StoreType.CSV,
        new Options(),
        path);

		assertFalse(catalog.existsTable("getTable"));
    catalog.addTable(meta);
    assertTrue(catalog.existsTable("getTable"));

    catalog.deleteTable("getTable");
    assertFalse(catalog.existsTable("getTable"));
	}
	
	@Test(expected = Throwable.class)
	public void testAddTableNoName() throws Exception {
	  schema1 = new Schema();
    schema1.addColumn(FieldName1, Type.BLOB);
    schema1.addColumn(FieldName2, Type.INT4);
    schema1.addColumn(FieldName3, Type.INT8);
    
	  TableMeta info = CatalogUtil.newTableMeta(StoreType.CSV);
	  TableDesc desc = new TableDesc();
	  desc.setMeta(info);
	  
	  catalog.addTable(desc);
	}

  static IndexDesc desc1;
  static IndexDesc desc2;
  static IndexDesc desc3;

  static {
    desc1 = new IndexDesc(
        "idx_test", "indexed", new Column("id", Type.INT4),
        IndexMethod.TWO_LEVEL_BIN_TREE, true, true, true);

    desc2 = new IndexDesc(
        "idx_test2", "indexed", new Column("score", Type.FLOAT8),
        IndexMethod.TWO_LEVEL_BIN_TREE, false, false, false);

    desc3 = new IndexDesc(
        "idx_test", "indexed", new Column("id", Type.INT4),
        IndexMethod.TWO_LEVEL_BIN_TREE, true, true, true);
  }
	
	@Test
	public void testAddAndDelIndex() throws Exception {
	  TableDesc desc = TestDBStore.prepareTable();
	  catalog.addTable(desc);
	  
	  assertFalse(catalog.existIndex(desc1.getName()));
	  assertFalse(catalog.existIndex("indexed", "id"));
	  catalog.addIndex(desc1);
	  assertTrue(catalog.existIndex(desc1.getName()));
	  assertTrue(catalog.existIndex("indexed", "id"));
	  
	  assertFalse(catalog.existIndex(desc2.getName()));
	  assertFalse(catalog.existIndex("indexed", "score"));
	  catalog.addIndex(desc2);
	  assertTrue(catalog.existIndex(desc2.getName()));
	  assertTrue(catalog.existIndex("indexed", "score"));
	  
	  catalog.deleteIndex(desc1.getName());
	  assertFalse(catalog.existIndex(desc1.getName()));
	  catalog.deleteIndex(desc2.getName());
	  assertFalse(catalog.existIndex(desc2.getName()));
	  
	  catalog.deleteTable(desc.getName());
	}
	
	public static class TestFunc1 extends Function {
		public TestFunc1() {
			super(					
					new Column [] {
							new Column("name", TajoDataTypes.Type.INT4)
					}
			);
		}

    public CatalogProtos.FunctionType getFunctionType() {
      return FunctionType.GENERAL;
    }
	}

  public static class TestFunc2 extends Function {
    public TestFunc2() {
      super(
          new Column [] {
              new Column("name", TajoDataTypes.Type.INT4),
              new Column("bytes", TajoDataTypes.Type.BLOB)
          }
      );
    }
    public CatalogProtos.FunctionType getFunctionType() {
      return FunctionType.GENERAL;
    }
  }

	@Test
	public final void testRegisterFunc() throws Exception { 
		assertFalse(catalog.containFunction("test2", FunctionType.UDF));
		FunctionDesc meta = new FunctionDesc("test2", TestFunc1.class, FunctionType.UDF,
        CatalogUtil.newSimpleDataType(Type.INT4),
        CatalogUtil.newSimpleDataTypeArray(Type.INT4));

    catalog.createFunction(meta);
		assertTrue(catalog.containFunction("test2", CatalogUtil.newSimpleDataTypeArray(Type.INT4)));
		FunctionDesc retrived = catalog.getFunction("test2", CatalogUtil.newSimpleDataTypeArray(Type.INT4));

		assertEquals(retrived.getSignature(),"test2");
		assertEquals(retrived.getFuncClass(),TestFunc1.class);
		assertEquals(retrived.getFuncType(),FunctionType.UDF);
	}

  @Test
  public final void testDropFunction() throws Exception {
    assertFalse(catalog.containFunction("test3", CatalogUtil.newSimpleDataTypeArray(Type.INT4)));
    FunctionDesc meta = new FunctionDesc("test3", TestFunc1.class, FunctionType.UDF,
        CatalogUtil.newSimpleDataType(Type.INT4),
        CatalogUtil.newSimpleDataTypeArray(Type.INT4));
    catalog.createFunction(meta);
    assertTrue(catalog.containFunction("test3", CatalogUtil.newSimpleDataTypeArray(Type.INT4)));
    catalog.dropFunction("test3");
    assertFalse(catalog.containFunction("test3", CatalogUtil.newSimpleDataTypeArray(Type.INT4)));

    assertFalse(catalog.containFunction("test3", CatalogUtil.newSimpleDataTypeArray(Type.INT4, Type.BLOB)));
    FunctionDesc overload = new FunctionDesc("test3", TestFunc2.class, FunctionType.GENERAL,
        CatalogUtil.newSimpleDataType(Type.INT4),
        CatalogUtil.newSimpleDataTypeArray(Type.INT4, Type.BLOB));
    catalog.createFunction(overload);
    assertTrue(catalog.containFunction("test3", CatalogUtil.newSimpleDataTypeArray(Type.INT4, Type.BLOB)));
  }

  @Test
  public final void testAddAndDeleteTablePartitionByHash1() throws Exception {
    Schema schema = new Schema();
    schema.addColumn("id", Type.INT4)
        .addColumn("name", Type.TEXT)
        .addColumn("age", Type.INT4)
        .addColumn("score", Type.FLOAT8);

    String tableName = "addedtable";
    Options opts = new Options();
    opts.put("file.delimiter", ",");
    TableMeta meta = CatalogUtil.newTableMeta(StoreType.CSV, opts);

    PartitionDesc partitionDesc = new PartitionDesc();
    partitionDesc.addColumn(new Column("id", Type.INT4));
    partitionDesc.setPartitionsType(CatalogProtos.PartitionsType.HASH);
    partitionDesc.setNumPartitions(2);

    TableDesc desc = new TableDesc(tableName, schema, meta, new Path(CommonTestingUtil.getTestDir(), "addedtable"));
    desc.setPartitions(partitionDesc);

    assertFalse(catalog.existsTable(tableName));
    catalog.addTable(desc);
    assertTrue(catalog.existsTable(tableName));
    TableDesc retrieved = catalog.getTableDesc(tableName);

    assertEquals(retrieved.getName(), tableName);
    assertEquals(retrieved.getPartitions().getPartitionsType(), CatalogProtos.PartitionsType.HASH);
    assertEquals(retrieved.getPartitions().getSchema().getColumn(0).getColumnName(), "id");
    assertEquals(retrieved.getPartitions().getNumPartitions(), 2);

    catalog.deleteTable(tableName);
    assertFalse(catalog.existsTable(tableName));
  }


  @Test
  public final void testAddAndDeleteTablePartitionByHash2() throws Exception {
    Schema schema = new Schema();
    schema.addColumn("id", Type.INT4)
        .addColumn("name", Type.TEXT)
        .addColumn("age", Type.INT4)
        .addColumn("score", Type.FLOAT8);

    String tableName = "addedtable";
    Options opts = new Options();
    opts.put("file.delimiter", ",");
    TableMeta meta = CatalogUtil.newTableMeta(StoreType.CSV, opts);

    PartitionDesc partitionDesc = new PartitionDesc();
    partitionDesc.addColumn(new Column("id", Type.INT4));
    partitionDesc.setPartitionsType(CatalogProtos.PartitionsType.HASH);
    partitionDesc.setNumPartitions(2);

    partitionDesc.addSpecifier(new Specifier("sub_part1"));
    partitionDesc.addSpecifier(new Specifier("sub_part2"));
    partitionDesc.addSpecifier(new Specifier("sub_part3"));

    TableDesc desc = new TableDesc(tableName, schema, meta, new Path(CommonTestingUtil.getTestDir(), "addedtable"));
    desc.setPartitions(partitionDesc);
    assertFalse(catalog.existsTable(tableName));
    catalog.addTable(desc);
    assertTrue(catalog.existsTable(tableName));

    TableDesc retrieved = catalog.getTableDesc(tableName);

    assertEquals(retrieved.getName(), tableName);
    assertEquals(retrieved.getPartitions().getPartitionsType(), CatalogProtos.PartitionsType.HASH);
    assertEquals(retrieved.getPartitions().getSchema().getColumn(0).getColumnName(), "id");
    assertEquals(retrieved.getPartitions().getNumPartitions(), 2);
    assertEquals(retrieved.getPartitions().getSpecifiers().get(0).getName(),
        "sub_part1");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(1).getName(),
        "sub_part2");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(2).getName(),
        "sub_part3");

    catalog.deleteTable(tableName);
    assertFalse(catalog.existsTable(tableName));
  }

  @Test
  public final void testAddAndDeleteTablePartitionByList() throws Exception {
    Schema schema = new Schema();
    schema.addColumn("id", Type.INT4)
        .addColumn("name", Type.TEXT)
        .addColumn("age", Type.INT4)
        .addColumn("score", Type.FLOAT8);

    String tableName = "addedtable";
    Options opts = new Options();
    opts.put("file.delimiter", ",");
    TableMeta meta = CatalogUtil.newTableMeta(StoreType.CSV, opts);

    PartitionDesc partitionDesc = new PartitionDesc();
    partitionDesc.addColumn(new Column("id", Type.INT4));
    partitionDesc.setPartitionsType(CatalogProtos.PartitionsType.LIST);

    partitionDesc.addSpecifier(new Specifier("sub_part1", "Seoul,서울"));
    partitionDesc.addSpecifier(new Specifier("sub_part2", "Busan,부산"));

    TableDesc desc = new TableDesc(tableName, schema, meta, new Path(CommonTestingUtil.getTestDir(), "addedtable"));
    desc.setPartitions(partitionDesc);
    assertFalse(catalog.existsTable(tableName));
    catalog.addTable(desc);
    assertTrue(catalog.existsTable(tableName));

    TableDesc retrieved = catalog.getTableDesc(tableName);

    assertEquals(retrieved.getName(), tableName);
    assertEquals(retrieved.getPartitions().getPartitionsType(), CatalogProtos.PartitionsType.LIST);
    assertEquals(retrieved.getPartitions().getSchema().getColumn(0).getColumnName(), "id");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(0).getName(),
        "sub_part1");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(0).getExpressions(),
        "Seoul,서울");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(1).getName(),
        "sub_part2");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(1).getExpressions(),
        "Busan,부산");

    catalog.deleteTable(tableName);
    assertFalse(catalog.existsTable(tableName));
  }

  @Test
  public final void testAddAndDeleteTablePartitionByRange() throws Exception {
    Schema schema = new Schema();
    schema.addColumn("id", Type.INT4)
        .addColumn("name", Type.TEXT)
        .addColumn("age", Type.INT4)
        .addColumn("score", Type.FLOAT8);

    String tableName = "addedtable";
    Options opts = new Options();
    opts.put("file.delimiter", ",");
    TableMeta meta = CatalogUtil.newTableMeta(StoreType.CSV, opts);

    PartitionDesc partitionDesc = new PartitionDesc();
    partitionDesc.addColumn(new Column("id", Type.INT4));
    partitionDesc.setPartitionsType(CatalogProtos.PartitionsType.RANGE);

    partitionDesc.addSpecifier(new Specifier("sub_part1", "2"));
    partitionDesc.addSpecifier(new Specifier("sub_part2", "5"));
    partitionDesc.addSpecifier(new Specifier("sub_part3"));

    TableDesc desc = new TableDesc(tableName, schema, meta, new Path(CommonTestingUtil.getTestDir(), "addedtable"));
    desc.setPartitions(partitionDesc);
    assertFalse(catalog.existsTable(tableName));
    catalog.addTable(desc);
    assertTrue(catalog.existsTable(tableName));

    TableDesc retrieved = catalog.getTableDesc(tableName);

    assertEquals(retrieved.getName(), tableName);
    assertEquals(retrieved.getPartitions().getPartitionsType(), CatalogProtos.PartitionsType.RANGE);
    assertEquals(retrieved.getPartitions().getSchema().getColumn(0).getColumnName(), "id");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(0).getName(),
        "sub_part1");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(0).getExpressions(),
        "2");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(1).getName(),
        "sub_part2");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(1).getExpressions(),
        "5");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(2).getName(),
        "sub_part3");
    assertEquals(retrieved.getPartitions().getSpecifiers().get(2).getExpressions(),
        "");

    catalog.deleteTable(tableName);
    assertFalse(catalog.existsTable(tableName));
  }

  @Test
  public final void testAddAndDeleteTablePartitionByColumn() throws Exception {
    Schema schema = new Schema();
    schema.addColumn("id", Type.INT4)
        .addColumn("name", Type.TEXT)
        .addColumn("age", Type.INT4)
        .addColumn("score", Type.FLOAT8);

    String tableName = "addedtable";
    Options opts = new Options();
    opts.put("file.delimiter", ",");
    TableMeta meta = CatalogUtil.newTableMeta(StoreType.CSV, opts);

    PartitionDesc partitionDesc = new PartitionDesc();
    partitionDesc.addColumn(new Column("id", Type.INT4));
    partitionDesc.setPartitionsType(CatalogProtos.PartitionsType.COLUMN);

    TableDesc desc = new TableDesc(tableName, schema, meta, new Path(CommonTestingUtil.getTestDir(), "addedtable"));
    desc.setPartitions(partitionDesc);
    assertFalse(catalog.existsTable(tableName));
    catalog.addTable(desc);
    assertTrue(catalog.existsTable(tableName));

    TableDesc retrieved = catalog.getTableDesc(tableName);

    assertEquals(retrieved.getName(), tableName);
    assertEquals(retrieved.getPartitions().getPartitionsType(), CatalogProtos.PartitionsType.COLUMN);
    assertEquals(retrieved.getPartitions().getSchema().getColumn(0).getColumnName(), "id");

    catalog.deleteTable(tableName);
    assertFalse(catalog.existsTable(tableName));
  }

}
