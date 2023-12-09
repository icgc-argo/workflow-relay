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

package org.icgcargo.workflow.relay.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigInteger;
import java.security.MessageDigest;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class Sanitize {

  /**
   * Sanitizes a given JSON path through a mutation, assuming it is a value node.
   *
   * @param jsonPath Path to value node as a JSON path (dot delimited path)
   * @param json The JSON data
   */
  public static void sanitize(@NonNull String jsonPath, @NonNull JsonNode json) {
    val paths = jsonPath.split("\\.");

    // Maintain parent node as we traverse since jackson is optimized for mem and doesn't doubly
    // link
    JsonNode parent = null;
    JsonNode acc = json;
    for (val path : paths) {
      if (acc.isMissingNode()) return; // Short circuit
      parent = acc;
      acc = acc.path(path);
    }

    if (acc.isValueNode()) {
      val hash = md5(acc.asText());
      if (parent == null) {
        return; // Guard and short return
      }
      ((ObjectNode) parent).put(paths[paths.length - 1], hash);
    }
  }

  @SneakyThrows
  static String md5(String text) {
    val messageDigest = MessageDigest.getInstance("MD5");
    messageDigest.update(text.getBytes());
    val digest = messageDigest.digest();
    val number = new BigInteger(1, digest);
    return String.format("%032x", number);
  }
}
