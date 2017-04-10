/*
  Copyright 2016, Google, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.example.bigquery;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for synchronous query sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QuerySampleIT {
  private static final String LEGACY_SQL_QUERY =
      "SELECT corpus FROM [bigquery-public-data:samples.shakespeare] GROUP BY corpus;";
  private static final String STANDARD_SQL_QUERY =
      "SELECT corpus FROM `bigquery-public-data.samples.shakespeare` GROUP BY corpus;";
  private static final String CORPUS_NAME = "romeoandjuliet";
  private static final String TEST_DATASET = "query_sample_test";
  private static final String TEST_TABLE = "query_sample_test";
  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static final void deleteTestDataset() {
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    DatasetId datasetId = DatasetId.of(TEST_DATASET);
    BigQuery.DatasetDeleteOption deleteContents = BigQuery.DatasetDeleteOption.deleteContents();
    bigquery.delete(datasetId, deleteContents);
  }

  private static final void createTestDataset() {
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    DatasetId datasetId = DatasetId.of(TEST_DATASET);
    bigquery.create(DatasetInfo.newBuilder(datasetId).build());
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testSimpleQuery() throws Exception {
    QuerySample.main(new String[]{"--query", LEGACY_SQL_QUERY, "--runSimpleQuery"});
    String got = bout.toString();
    assertThat(got).contains(CORPUS_NAME);
  }

  @Test
  public void testStandardSqlQuery() throws Exception {
    QuerySample.main(new String[]{"--query", STANDARD_SQL_QUERY, "--runStandardSqlQuery"});
    String got = bout.toString();
    assertThat(got).contains(CORPUS_NAME);
  }

  @Test
  public void testUncachedQuery() throws Exception {
    QuerySample.main(new String[]{"--query", LEGACY_SQL_QUERY, "--runSimpleQuery"});
    String got = bout.toString();
    assertThat(got).contains(CORPUS_NAME);
  }

  @Test
  public void testPermanentTableQuery() throws Exception {
    // Setup the test data.
    deleteTestDataset();
    createTestDataset();

    QuerySample.main(
        new String[]{
            "--query",
            LEGACY_SQL_QUERY,
            "--runPermanentTableQuery",
            "--destDataset",
            TEST_DATASET,
            "--destTable",
            TEST_TABLE,
            "--allowLargeResults"});
    String got = bout.toString();
    assertThat(got).contains(CORPUS_NAME);

    // Cleanup the test data.
    deleteTestDataset();
  }
}
