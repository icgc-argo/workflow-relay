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

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.icgc_argo.workflow.relay.config.stream.SplitStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Profile("splitter")
@Slf4j
@Service
@EnableBinding(SplitStream.class)
public class SplitterService {

  private MessageChannel workflowOutput;
  private MessageChannel taskOutput;

  @Autowired
  public SplitterService(SplitStream splitStream) {
    this.workflowOutput = splitStream.workflowOutput();
    this.taskOutput = splitStream.taskOutput();
  }

  @StreamListener(SplitStream.WEBLOG)
  public void split(JsonNode event) {
    if (event.has("trace")) {
      // NEXTFLOW TASK EVENT
      log.debug(format("Processing task (Nextflow) event for runId: { %s }, runName: { %s }", event.path("runId").asText(), event.path("runName").asText()));
      taskOutput.send(MessageBuilder.withPayload(event).build());
    } else if (!event.path("metadata").path("workflow").isMissingNode()) {
      // NEXTFLOW WORKFLOW EVENT
      log.debug(format("Processing workflow (Nextflow) event for runId: { %s }, runName: { %s }", event.path("runId").asText(), event.path("runName").asText()));
      workflowOutput.send(MessageBuilder.withPayload(event).build());
    } else {
      log.error("Unhandled event: {}", event.toString());
      throw new RuntimeException("Cannot handle event, please see DLQ for event information");
    }
  }
}
