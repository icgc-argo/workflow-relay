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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Fixture {

  private static final String BASE_PATH = "fixtures" + File.separator;

  @SneakyThrows
  public static <T> T loadJsonFixture(
      Class clazz, String fileName, Class<T> targetClass, ObjectMapper mapper) {
    String json = loadJsonString(clazz, fileName);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(OffsetDateTimeDeserializer.getOffsetDateTimeModule());

    return mapper.readValue(json, targetClass);
  }

  public static String loadJsonString(Class clazz, String fileName) throws IOException {
    return StringUtilities.inputStreamToString(
        Optional.ofNullable(clazz.getClassLoader().getResource(BASE_PATH + fileName))
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "fixture not found. make sure you created the correct "
                            + "folder if this is a new class or if you renamed the class"))
            .openStream());
  }
}
