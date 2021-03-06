package ai.labs.resources.rest.config.http.model;

import ai.labs.models.PropertyInstruction;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PreRequest {
    private List<PropertyInstruction> propertyInstructions;
    private BatchRequestBuildingInstruction batchRequests;
    private Integer delayBeforeExecutingInMillis = 0;
}
