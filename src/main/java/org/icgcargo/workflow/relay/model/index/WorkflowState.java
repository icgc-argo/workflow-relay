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

package org.icgcargo.workflow.relay.model.index;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum WorkflowState {
  UNKNOWN("UNKNOWN"),

  QUEUED("QUEUED"),

  INITIALIZING("INITIALIZING"),

  RUNNING("RUNNING"),

  PAUSED("PAUSED"),

  CANCELING("CANCELING"),

  CANCELED("CANCELED"),

  COMPLETE("COMPLETE"),

  EXECUTOR_ERROR("EXECUTOR_ERROR"),

  SYSTEM_ERROR("SYSTEM_ERROR");

  @NonNull private final String value;

  public static WorkflowState fromNextflowEventAndSuccess(
      @NonNull String nextflowEvent, @NonNull boolean success) {
    if (nextflowEvent.equalsIgnoreCase("started")) {
      return WorkflowState.RUNNING;
    } else if (nextflowEvent.equalsIgnoreCase("completed") && success) {
      return WorkflowState.COMPLETE;
    } else if ((nextflowEvent.equalsIgnoreCase("completed") && !success)
        || nextflowEvent.equalsIgnoreCase("failed")
        || nextflowEvent.equalsIgnoreCase("error")) {
      return WorkflowState.EXECUTOR_ERROR;
    } else {
      return WorkflowState.UNKNOWN;
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
