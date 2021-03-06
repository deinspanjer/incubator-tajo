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

package org.apache.tajo;

import com.google.protobuf.ServiceException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.tajo.algebra.CreateTable;
import org.apache.tajo.algebra.DropTable;
import org.apache.tajo.algebra.Expr;
import org.apache.tajo.algebra.OpType;
import org.apache.tajo.client.TajoClient;
import org.apache.tajo.conf.TajoConf;
import org.apache.tajo.engine.parser.SQLAnalyzer;
import org.apache.tajo.storage.StorageUtil;
import org.apache.tajo.util.FileUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * (Note that this class is not thread safe. Do not execute maven test in any parallel mode.)
 * <br />
 * <code>QueryTestCaseBase</code> provides useful methods to easily execute queries and verify their results.
 *
 * This class basically uses four resource directories:
 * <ul>
 *   <li>src/test/resources/dataset - contains a set of data files. It contains sub directories, each of which
 *   corresponds each test class. All data files in each sub directory can be used in the corresponding test class.</li>
 *
 *   <li>src/test/resources/queries - This is the query directory. It contains sub directories, each of which
 *   corresponds each test class. All query files in each sub directory can be used in the corresponding test
 *   class.</li>
 *
 *   <li>src/test/resources/results - This is the result directory. It contains sub directories, each of which
 *   corresponds each test class. All result files in each sub directory can be used in the corresponding test class.
 *   </li>
 * </ul>
 *
 * For example, if you create a test class named <code>TestJoinQuery</code>, you should create a pair of query and
 * result set directories as follows:
 *
 * <pre>
 *   src-|
 *       |- resources
 *             |- dataset
 *             |     |- TestJoinQuery
 *             |              |- table1.tbl
 *             |              |- table2.tbl
 *             |
 *             |- queries
 *             |     |- TestJoinQuery
 *             |              |- TestInnerJoin.sql
 *             |              |- table1_ddl.sql
 *             |              |- table2_ddl.sql
 *             |
 *             |- results
 *                   |- TestJoinQuery
 *                            |- TestInnerJoin.result
 * </pre>
 *
 * <code>QueryTestCaseBase</code> basically provides the following methods:
 * <ul>
 *  <li><code>{@link #executeQuery()}</code> - executes a corresponding query and returns an ResultSet instance</li>
 *  <li><code>{@link #executeQuery(String)}</code> - executes a given query file included in the corresponding query
 *  file in the current class's query directory</li>
 *  <li><code>assertResultSet()</code> - check if the query result is equivalent to the expected result included
 *  in the corresponding result file in the current class's result directory.</li>
 *  <li><code>cleanQuery()</code> - clean up all resources</li>
 *  <li><code>executeDDL()</code> - execute a DDL query like create or drop table.</li>
 * </ul>
 *
 * In order to make use of the above methods, query files and results file must be as follows:
 * <ul>
 *  <li>Each query file must be located on the subdirectory whose structure must be src/resources/queries/${ClassName},
 *  where ${ClassName} indicates an actual test class's simple name.</li>
 *  <li>Each result file must be located on the subdirectory whose structure must be src/resources/results/${ClassName},
 *  where ${ClassName} indicates an actual test class's simple name.</li>
 * </ul>
 *
 * Especially, {@link #executeQuery() and {@link #assertResultSet(java.sql.ResultSet)} methods automatically finds
 * a query file to be executed and a result to be compared, which are corresponding to the running class and method.
 * For them, query and result files additionally must be follows as:
 * <ul>
 *  <li>Each result file must have the file extension '.result'</li>
 *  <li>Each query file must have the file extension '.sql'.</li>
 * </ul>
 */
public class QueryTestCaseBase {

  protected static final TpchTestBase testingCluster;
  protected static TajoConf conf;
  protected static TajoClient client;
  protected static SQLAnalyzer sqlParser = new SQLAnalyzer();

  /** the base path of dataset directories */
  protected static final Path datasetBasePath;
  /** the base path of query directories */
  protected static final Path queryBasePath;
  /** the base path of result directories */
  protected static final Path resultBasePath;

  static {
    testingCluster = TpchTestBase.getInstance();
    conf = testingCluster.getTestingCluster().getConfiguration();
    URL datasetBaseURL = ClassLoader.getSystemResource("dataset");
    datasetBasePath = new Path(datasetBaseURL.toString());
    URL queryBaseURL = ClassLoader.getSystemResource("queries");
    queryBasePath = new Path(queryBaseURL.toString());
    URL resultBaseURL = ClassLoader.getSystemResource("results");
    resultBasePath = new Path(resultBaseURL.toString());
  }

  /** It transiently contains created tables for the running test class. */
  private static Set<String> createdTableSet = new HashSet<String>();
  // queries and results directory corresponding to subclass class.
  private Path currentQueryPath;
  private Path currentResultPath;
  private Path currentDatasetPath;

  // for getting a method name
  @Rule public TestName name= new TestName();

  @BeforeClass
  public static void setUpClass() throws IOException {
    conf = testingCluster.getTestingCluster().getConfiguration();
    client = new TajoClient(conf);
  }

  @AfterClass
  public static void tearDownClass() throws ServiceException {
    for (String tableName : createdTableSet) {
      client.dropTable(tableName, false);
    }
    createdTableSet.clear();
    client.close();
  }

  @Before
  public void setUp() {
    String className = getClass().getSimpleName();
    currentQueryPath = new Path(queryBasePath, className);
    currentResultPath = new Path(resultBasePath, className);
    currentDatasetPath = new Path(datasetBasePath, className);
  }

  /**
   * Execute a query contained in the file located in src/test/resources/results/<i>ClassName</i>/<i>MethodName</i>.
   * <i>ClassName</i> and <i>MethodName</i> will be replaced by actual executed class and methods.
   *
   * @return ResultSet of query execution.
   */
  public ResultSet executeQuery() throws Exception {
    return executeQuery(name.getMethodName() + ".sql");
  }

  /**
   * Execute a query contained in the given named file. This methods tries to find the given file within the directory
   * src/test/resources/results/<i>ClassName</i>.
   *
   * @param queryFileName The file name to be used to execute a query.
   * @return ResultSet of query execution.
   */
  public ResultSet executeQuery(String queryFileName) throws Exception {
    Path queryFilePath = getQueryFilePath(queryFileName);
    FileSystem fs = currentQueryPath.getFileSystem(testingCluster.getTestingCluster().getConfiguration());
    assertTrue(queryFilePath.toString() + " existence check", fs.exists(queryFilePath));
    ResultSet result = testingCluster.execute(FileUtil.readTextFile(new File(queryFilePath.toUri())));
    assertNotNull("Query succeeded test", result);
    return result;
  }

  /**
   * Assert the equivalence between the expected result and an actual query result.
   * If it isn't it throws an AssertionError.
   *
   * @param result Query result to be compared.
   */
  public final void assertResultSet(ResultSet result) throws IOException {
    assertResultSet("Result Verification", result, name.getMethodName() + ".result");
  }

  /**
   * Assert the equivalence between the expected result and an actual query result.
   * If it isn't it throws an AssertionError.
   *
   * @param result Query result to be compared.
   * @param resultFileName The file name containing the result to be compared
   */
  public final void assertResultSet(ResultSet result, String resultFileName) throws IOException {
    assertResultSet("Result Verification", result, resultFileName);
  }

  /**
   * Assert the equivalence between the expected result and an actual query result.
   * If it isn't it throws an AssertionError with the given message.
   *
   * @param message message The message to printed if the assertion is failed.
   * @param result Query result to be compared.
   */
  public final void assertResultSet(String message, ResultSet result, String resultFileName) throws IOException {
    FileSystem fs = currentQueryPath.getFileSystem(testingCluster.getTestingCluster().getConfiguration());
    Path resultFile = getResultFile(resultFileName);
    assertTrue(resultFile.toString() + " existence check", fs.exists(resultFile));
    try {
      verifyResult(message, result, resultFile);
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  /**
   * Release all resources
   *
   * @param resultSet ResultSet
   */
  public final void cleanupQuery(ResultSet resultSet) throws IOException {
    try {
      resultSet.close();
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  public void assertTableExists(String tableName) throws ServiceException {
    assertTrue(client.existTable(tableName));
  }

  /**
   * It transforms a ResultSet instance to rows represented as strings.
   *
   * @param resultSet ResultSet that contains a query result
   * @return String
   * @throws SQLException
   */
  public String resultSetToString(ResultSet resultSet) throws SQLException {
    StringBuilder sb = new StringBuilder();
    ResultSetMetaData rsmd = resultSet.getMetaData();
    int numOfColumns = rsmd.getColumnCount();

    for (int i = 1; i <= numOfColumns; i++) {
      if (i > 1) sb.append(",");
      String columnName = rsmd.getColumnName(i);
      sb.append(columnName);
    }
    sb.append("\n-------------------------------\n");

    while (resultSet.next()) {
      for (int i = 1; i <= numOfColumns; i++) {
        if (i > 1) sb.append(",");
        String columnValue = resultSet.getObject(i).toString();
        sb.append(columnValue);
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  private void verifyResult(String message, ResultSet res, Path resultFile) throws SQLException, IOException {
    String actualResult = resultSetToString(res);
    String expectedResult = FileUtil.readTextFile(new File(resultFile.toUri()));
    assertEquals(message, expectedResult.trim(), actualResult.trim());
  }

  private Path getQueryFilePath(String fileName) {
    return StorageUtil.concatPath(currentQueryPath, fileName);
  }

  private Path getResultFile(String fileName) {
    return StorageUtil.concatPath(currentResultPath, fileName);
  }

  private Path getDataSetFile(String fileName) {
    return StorageUtil.concatPath(currentDatasetPath, fileName);
  }

  /**
   *
   * Execute a data definition language (DDL) template. A general SQL DDL statement can be included in this file. But,
   * for user-specified table name or exact external table path, you must use some format string to indicate them.
   * The format string will be replaced by the corresponding arguments.
   *
   * The below is predefined format strings:
   * <ul>
   *   <li>${table.path} - It is replaced by the absolute file path that <code>dataFileName</code> points. </li>
   *   <li>${i} - It is replaced by the corresponding element of <code>args</code>. For example, ${0} and ${1} are
   *   replaced by the first and second elements of <code>args</code> respectively</li>. It uses zero-based index.
   * </ul>
   *
   * @param ddlFileName A file name, containing a data definition statement.
   * @param dataFileName A file name, containing data rows, which columns have to be separated by vertical bar '|'.
   *                     This file name is used for replacing some format string indicating an external table location.
   * @param args A list of arguments, each of which is used to replace corresponding variable which has a form of ${i}.
   * @return The table name created
   */
  public String executeDDL(String ddlFileName, String dataFileName, String ... args) throws Exception {
    return executeDDL(ddlFileName, dataFileName, true, args);
  }

  private String executeDDL(String ddlFileName, String dataFileName, boolean isLocalTable, String ... args)
      throws Exception {

    Path ddlFilePath = new Path(currentQueryPath, ddlFileName);
    FileSystem fs = ddlFilePath.getFileSystem(conf);
    assertTrue(ddlFilePath + " existence check", fs.exists(ddlFilePath));

    String template = FileUtil.readTextFile(new File(ddlFilePath.toUri()));
    String dataFilePath = null;
    if (dataFileName != null) {
      dataFilePath = getDataSetFile(dataFileName).toString();
    }
    String compiled = compileTemplate(template, dataFilePath, args);

    // parse a statement
    Expr expr = sqlParser.parse(compiled);
    assertNotNull(ddlFilePath + " cannot be parsed", expr);

    String tableName = null;
    if (expr.getType() == OpType.CreateTable) {
      CreateTable createTable = (CreateTable) expr;
      tableName = createTable.getTableName();
      client.updateQuery(compiled);
      assertTrue("table '" + tableName  + "' creation check", client.existTable(tableName));
      if (isLocalTable) {
        createdTableSet.add(tableName);
      }
    } else if (expr.getType() == OpType.DropTable) {
      DropTable dropTable = (DropTable) expr;
      tableName = dropTable.getTableName();
      assertTrue("table '" + tableName + "' existence check", client.existTable(tableName));
      client.updateQuery(compiled);
      assertFalse("table '" + tableName + "' dropped check", client.existTable(tableName));
      if (isLocalTable) {
        createdTableSet.remove(tableName);
      }
    } else {
      assertTrue(ddlFilePath + " is not a Create or Drop Table statement", false);
    }

    return tableName;
  }

  /**
   * Replace format strings by a given parameters.
   *
   * @param template
   * @param dataFileName The data file name to replace <code>${table.path}</code>
   * @param args The list argument to replace each corresponding format string ${i}. ${i} uses zero-based index.
   * @return A string compiled
   */
  private String compileTemplate(String template, String dataFileName, String... args) {
    String result;
    if (dataFileName != null) {
      result = template.replace("${table.path}", "\'" + dataFileName + "'");
    } else {
      result = template;
    }

    for (int i = 0; i < args.length; i++) {
      result = result.replace("${" + i + "}", args[i]);
    }
    return result;
  }
}
