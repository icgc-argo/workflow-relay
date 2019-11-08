package org.icgc_argo.workflow.relay.entities.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trace {

  /** When the command started executing */
  @NonNull private Date start;

  /** When the command stopped executing (completed, failed, or cancelled) */
  @NonNull private Date complete;

  /** Exit code of the program */
  @NonNull private Integer exit;

  /** The task or workflow name */
  @NonNull private String name;

  /** The command line that was executed */
  @NonNull private String script;

}
