/*
 * Copyright (c) 2020 The Ontario Institute for Cancer Research. All rights reserved
 *
 * This program and the accompanying materials are made available under the terms of the GNU Affero General Public License v3.0.
 * You should have received a copy of the GNU Affero General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.icgc_argo.workflow.relay.config.elastic;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.asList;
import static org.icgc_argo.workflow.relay.util.StringUtilities.inputStreamToString;

@Profile({"index", "graphlog"})
@Slf4j
@Component
public class ElasticsearchStartupListener implements ApplicationListener<ContextRefreshedEvent> {

  private final RestHighLevelClient client;
  private final ElasticsearchProperties properties;
  private final List<String> activeProfiles;

  @Value("classpath:run_log_mapping.json")
  private Resource workflowIndexMapping;

  @Value("classpath:task_log_mapping.json")
  private Resource taskIndexMapping;

  @Value("classpath:workflow_graph_log_mapping.json")
  private Resource graphLogIndexMapping;

  @Autowired
  public ElasticsearchStartupListener(
      @NonNull RestHighLevelClient client,
      @NonNull ElasticsearchProperties properties,
      @Autowired Environment environment) {
    this.client = client;
    this.properties = properties;
    this.activeProfiles = asList(environment.getActiveProfiles());
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    log.info("Ensuring index initialization...");

    if (activeProfiles.contains("index")) {
      ensureIndex(properties.workflowIndex, event);
      ensureIndex(properties.taskIndex, event);
    }

    if (activeProfiles.contains("graphlog")) {
      ensureIndex(properties.graphLogInfoDebugIndex, event);
      ensureIndex(properties.graphLogErrorWarningIndex, event);
    }
  }

  @SneakyThrows
  private void ensureIndex(String indexName, ContextRefreshedEvent event) {
    val request = new GetIndexRequest(indexName);
    if (!client.indices().exists(request, RequestOptions.DEFAULT)) {
      val indexSource = loadIndexSourceAsString(indexName);
      val createRequest =
          new CreateIndexRequest(indexName)
              .settings(
                  Settings.builder()
                      .put("index.number_of_shards", properties.numberOfShards)
                      .put("index.number_of_replicas", properties.numberOfReplicas));
      log.info("Creating index {}", indexName);
      createRequest.source(indexSource, XContentType.JSON);
      val response = client.indices().create(createRequest, RequestOptions.DEFAULT);
      if (!response.isAcknowledged()) {
        log.error("Could not ensure required indices when creating index: {}", indexName);
        SpringApplication.exit(event.getApplicationContext(), () -> 1);
      }
    }
  }

  @SneakyThrows
  private String loadIndexSourceAsString(String indexName) {
    log.debug("in loadIndexSourceAsString: {}", indexName);
    if (indexName.equals(properties.workflowIndex)) {
      return inputStreamToString(workflowIndexMapping.getInputStream());
    } else if (indexName.equals(properties.taskIndex)) {
      return inputStreamToString(taskIndexMapping.getInputStream());
    } else if (indexName.equals(properties.graphLogInfoDebugIndex)
        || indexName.equals(properties.graphLogErrorWarningIndex)) {
      return inputStreamToString(graphLogIndexMapping.getInputStream());
    } else
      throw new RuntimeException(
          "Failed to load index source: index name must be workflow or task.");
  }
}
