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

package org.icgc_argo.workflow.relay.entities.nextflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.OffsetDateTime;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Workflow {

  /** When the command started executing */
  @NonNull private OffsetDateTime start;

  /** When the command stopped executing (completed, failed, or cancelled) */
  private OffsetDateTime complete;

  /** The repository url */
  private String repository;

  /** The repository release/tag/branch */
  private String revision;

  /** The run's launch directory (where nextflow config and cache are stored) */
  @NonNull private String launchDir;

  /** The run's project directory (where the git repo is stored) */
  @NonNull private String projectDir;

  /** The run's working directory (scratch space) */
  @NonNull private String workDir;

  /** Exit code of the program */
  private Integer exitStatus;

  /** The command line that was executed */
  @NonNull private String commandLine;

  /** A URL to retrieve standard error logs of the workflow run or task */
  private String errorReport;

  /** Was this a resume run */
  @NonNull private String resume;

  /** Did the workflow succeed */
  @NonNull private Boolean success;

  /** Workflow duration */
  private Long duration;
}
