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

package org.icgc_argo.workflow.relay.model.index;

import static org.icgc_argo.workflow.relay.model.index.TaskState.isNextState;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * While all this is just testing is the static method wrapping a compareTo call, the real intention
 * is that it tests whether or not someone is mucking with the ordering of the enum declarations.
 */
public class TaskStateTest {

  @Test
  public void testIsNextStateUnknownQueued_true() {
    assertTrue(isNextState(TaskState.UNKNOWN, TaskState.QUEUED));
  }

  @Test
  public void testIsNextStateCompletedQueued_false() {
    assertFalse(isNextState(TaskState.COMPLETE, TaskState.QUEUED));
  }

  @Test
  public void testIsNextStateQueuedCompleted_true() {
    assertTrue(isNextState(TaskState.QUEUED, TaskState.COMPLETE));
  }

  @Test
  public void testIsNextStateExecutorErrorCompleted_false() {
    assertFalse(isNextState(TaskState.EXECUTOR_ERROR, TaskState.COMPLETE));
  }

  /** This should never happen, but test anyways */
  @Test
  public void testIsNextStateCompletedCompleted_false() {
    assertFalse(isNextState(TaskState.COMPLETE, TaskState.COMPLETE));
  }
}
