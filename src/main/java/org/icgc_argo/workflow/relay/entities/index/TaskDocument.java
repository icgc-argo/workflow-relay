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

package org.icgc_argo.workflow.relay.entities.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskDocument {

  /** Workflow run ID */
  @NonNull private String runId;

  /** Workflow session ID */
  @NonNull private String sessionId;

  /** Task id */
  @NonNull private Integer taskId;

  /** Task name */
  @NonNull private String name;

  /** Process name */
  @NonNull private String process;

  /** Task tag */
  private String tag;

  /** Task container */
  @NonNull private String container;

  /** Attempt */
  @NonNull private Integer attempt;

  /** The overall state of the task run, mapped to Trace's "status" */
  @NonNull private TaskState state;

  /** When the command was submitted */
  @NonNull private Instant submitTime;

  /** When the command started executing */
  @NonNull private Instant startTime;

  /** When the command stopped executing (completed, failed, or cancelled) */
  private Instant completeTime;

  /** Exit code of the program */
  @NonNull private Integer exit;

  /** The command line that was executed */
  @NonNull private String script;

  /** Task filesystem working directory */
  @NonNull private String workdir;

  /** The cpus number request for the task execution */
  private Integer cpus;

  /** The memory request for the task execution */
  private Long memory;

  /** Time elapsed to complete since the submission */
  private Long duration;

  /** Task execution time i.e. delta between completion and start timestamp */
  private Long realtime;

  /** Real memory (resident set) size of the process. Equivalent to `ps -o rss` */
  private Long rss;

  /** Peak of real memory. This data is read from field `VmHWM` in `/proc/$pid/status file` */
  private Long peakRss;

  /** Virtual memory size of the process. Equivalent to `ps -o vsize` */
  private Long vmem;

  /** Peak of virtual memory. This data is read from field `VmPeak` in `/proc/$pid/status file` */
  private Long peakVmem;

  /**
   * Number of bytes the process directly read from disk. This data is read from file
   * `/proc/$pid/io`
   */
  private Long readBytes;

  /**
   * Number of bytes the process originally dirtied in the page-cache (assuming they will go to disk
   * later). This data is read from file `/proc/$pid/io`
   */
  private Long writeBytes;
}
