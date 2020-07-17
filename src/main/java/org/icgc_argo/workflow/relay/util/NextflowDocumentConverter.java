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

package org.icgc_argo.workflow.relay.util;

import static org.icgc_argo.workflow.relay.exceptions.NotFoundException.checkNotFound;

import java.util.Objects;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc_argo.workflow.relay.model.index.*;
import org.icgc_argo.workflow.relay.model.nextflow.TaskEvent;
import org.icgc_argo.workflow.relay.model.nextflow.WorkflowEvent;

/** Utility class that converts metadata POJOs to index POJOs. */
@Slf4j
@NoArgsConstructor
public class NextflowDocumentConverter {

  public static WorkflowDocument buildWorkflowDocument(@NonNull WorkflowEvent workflowEvent) {

    checkNotFound(
        workflowEvent.getMetadata() != null,
        "Cannot convert workflow event to workflow document: metadata is null.");
    checkNotFound(
        workflowEvent.getMetadata().getWorkflow() != null,
        "Cannot convert workflow event to workflow document: workflow is null.");

    val workflow = workflowEvent.getMetadata().getWorkflow();

    val engineParams =
        EngineParameters.builder()
            .revision(workflow.getRevision())
            .resume(workflow.getResume())
            .launchDir(workflow.getLaunchDir())
            .projectDir(workflow.getProjectDir())
            .workDir(workflow.getWorkDir())
            .build();

    val success = workflow.getSuccess();

    val doc =
        WorkflowDocument.builder()
            .runId(workflowEvent.getRunName())
            .sessionId(workflowEvent.getRunId())
            .state(WorkflowState.fromValueAndSuccess(workflowEvent.getEvent(), success))
            .parameters(workflowEvent.getMetadata().getParameters())
            .engineParameters(engineParams)
            .startTime(workflow.getStart().toInstant())
            .repository(workflow.getRepository())
            .commandLine(workflow.getCommandLine())
            .errorReport(workflow.getErrorReport())
            .exitStatus(workflow.getExitStatus())
            .success(success)
            .duration(workflow.getDuration());

    val completeTime = workflow.getComplete();
    if (Objects.nonNull(completeTime)) {
      doc.completeTime(completeTime.toInstant());
    }

    return doc.build();
  }

  public static TaskDocument buildTaskDocument(@NonNull TaskEvent taskEvent) {

    checkNotFound(
        taskEvent.getTrace() != null, "Cannot convert task event to task document, trace is null.");
    val trace = taskEvent.getTrace();

    return TaskDocument.builder()
        .runId(taskEvent.getRunName())
        .sessionId(taskEvent.getRunId())
        .taskId(trace.getTaskId())
        .name(trace.getName())
        .process(trace.getProcess())
        .tag(trace.getTag())
        .container(trace.getContainer())
        .attempt(trace.getAttempt())
        .state(TaskState.fromValue(trace.getStatus()))
        .submitTime(trace.getSubmit())
        .startTime(trace.getStart())
        .completeTime(trace.getComplete())
        .exit(trace.getExit())
        .script(trace.getScript())
        .workdir(trace.getWorkdir())
        .cpus(trace.getCpus())
        .memory(trace.getMemory())
        .duration(trace.getDuration())
        .realtime(trace.getRealtime())
        .rss(trace.getRss())
        .peakRss(trace.getPeakRss())
        .vmem(trace.getVmem())
        .peakVmem(trace.getPeakVmem())
        .readBytes(trace.getReadBytes())
        .writeBytes(trace.getWriteBytes())
        .build();
  }
}
