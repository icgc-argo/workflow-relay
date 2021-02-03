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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.icgc_argo.workflow.relay.config.stream.WebLogStream;
import org.icgc_argo.workflow.relay.config.weblog.SanitizeProperties;
import org.icgc_argo.workflow.relay.util.Sanitize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.support.MessageBuilder;

@Slf4j
@Profile("weblog")
@EnableBinding(WebLogStream.class)
public class WebLogService {

  private WebLogStream webLogStream;
  private SanitizeProperties sanitizeProperties;

  @Autowired
  public WebLogService(WebLogStream webLogStream, SanitizeProperties sanitizeProperties) {
    this.webLogStream = webLogStream;
    this.sanitizeProperties = sanitizeProperties;
    log.info("Paths to Sanitize {}", sanitizeProperties.getPaths().toString());
  }

  public Boolean handleEvent(@NonNull JsonNode event) {
    log.info("handling event: {}", event);
    for (String path : sanitizeProperties.getPaths()) {
      Sanitize.sanitize(path, event);
    }
    return webLogStream.webLogOutput().send(MessageBuilder.withPayload(event).build());
  }
}
