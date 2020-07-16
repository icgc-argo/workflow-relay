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

package org.icgc_argo.workflow.relay.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc_argo.workflow.relay.config.stream.SplitStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@ActiveProfiles({"splitter"})
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SplitterServiceTest {

  @Autowired private SplitStream splitStream;
  @Autowired private MessageCollector messageCollector;

  @Test
  @SneakyThrows
  public void testWorkflowSplit() {
    val resource = this.getClass().getClassLoader().getResource("fixtures/nextflow_workflow_event.json");
    assert resource != null;
    val content = Files.readString(Path.of(resource.toURI()));

    splitStream.webLogOutInput().send(MessageBuilder.withPayload(content).build());
    val received = messageCollector.forChannel(splitStream.workflowOutput()).poll();
    assertNotNull(received);
    log.info("Workflow Event Published: {}", received.getPayload());

    val notReceived = messageCollector.forChannel(splitStream.taskOutput()).poll();
    assertNull(notReceived);
  }

  @Test
  @SneakyThrows
  public void testTaskSplit() {
    val resource = this.getClass().getClassLoader().getResource("fixtures/nextflow_task_event.json");
    assert resource != null;
    val content = Files.readString(Path.of(resource.toURI()));

    splitStream.webLogOutInput().send(MessageBuilder.withPayload(content).build());
    val received = messageCollector.forChannel(splitStream.taskOutput()).poll();
    assertNotNull(received);
    log.info("Task Event Published: {}", received.getPayload());

    val notReceived = messageCollector.forChannel(splitStream.workflowOutput()).poll();
    assertNull(notReceived);
  }
}
