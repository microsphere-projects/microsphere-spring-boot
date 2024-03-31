package io.microsphere.spring.boot.actuate.endpoint;

import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.OperationType;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.annotation.AbstractDiscoveredOperation;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.invoke.reflect.OperationMethod;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoint;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An aggregation {@link WebEndpoint @WebEndpoint} for all {@link WebEndpoint WebEndpoints}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Endpoint
 * @see WebEndpoint
 * @see PathMappedEndpoint
 * @see PathMappedEndpoints
 * @since 1.0.0
 */
@WebEndpoint(id = "webEndpoints")
public class WebEndpoints {

    private final WebEndpointsSupplier webEndpointsSupplier;

    public WebEndpoints(WebEndpointsSupplier webEndpointsSupplier) {
        this.webEndpointsSupplier = webEndpointsSupplier;
    }

    /**
     * all {@link OperationType#READ Read Opeartion} {@link WebEndpoint WebEndpoints} for Java {@link Method} without arguments
     *
     * @return
     */
    @ReadOperation
    public Map<String, Object> invokeReadOperations() {
        Collection<ExposableWebEndpoint> webEndpoints = this.webEndpointsSupplier.getEndpoints();

        Map<String, Object> readWebOperationResults = new HashMap<>(webEndpoints.size());

        InvocationContext context = createInvocationContext();

        for (ExposableWebEndpoint webEndpoint : webEndpoints) {
            if (webEndpoint instanceof DiscoveredEndpoint) {
                DiscoveredEndpoint discoveredEndpoint = (DiscoveredEndpoint) webEndpoint;
                Object endpointBean = discoveredEndpoint.getEndpointBean();
                if (endpointBean == this) { // ignore self
                    continue;
                }

                Collection<WebOperation> webOperations = webEndpoint.getOperations();
                for (WebOperation webOperation : webOperations) {
                    if (isReadWebOperationCandidate(webOperation)) {
                        String readWebOperationId = webOperation.getId();
                        Object readWebOperationResult = webOperation.invoke(context);
                        readWebOperationResults.put(readWebOperationId, readWebOperationResult);
                    }
                }
            }
        }

        return readWebOperationResults;
    }

    private InvocationContext createInvocationContext() {
        return new InvocationContext(SecurityContext.NONE, Collections.emptyMap());
    }

    private boolean isReadWebOperationCandidate(WebOperation webOperation) {
        if (webOperation instanceof AbstractDiscoveredOperation) {
            AbstractDiscoveredOperation discoveredOperation = (AbstractDiscoveredOperation) webOperation;
            OperationMethod operationMethod = discoveredOperation.getOperationMethod();
            if (OperationType.READ.equals(operationMethod.getOperationType())) {
                Method method = operationMethod.getMethod();
                if (method.getParameterCount() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

}
