package io.microsphere.spring.boot.actuate.endpoint;

import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.OperationType;
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
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.springframework.boot.actuate.endpoint.OperationType.READ;
import static org.springframework.boot.actuate.endpoint.SecurityContext.NONE;

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

    /**
     * Constructs a {@link WebEndpoints} aggregation backed by the given
     * {@link WebEndpointsSupplier}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   WebEndpointsSupplier supplier = applicationContext.getBean(WebEndpointsSupplier.class);
     *   WebEndpoints webEndpoints = new WebEndpoints(supplier);
     * }</pre>
     *
     * @param webEndpointsSupplier the supplier of {@link ExposableWebEndpoint} instances
     */
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
            if (isExposableWebEndpoint(webEndpoint)) {
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
        return new InvocationContext(NONE, emptyMap());
    }

    /**
     * Determines whether the given {@link ExposableWebEndpoint} is a {@link DiscoveredEndpoint}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ExposableWebEndpoint webEndpoint = // obtain endpoint
     *   if (WebEndpoints.isExposableWebEndpoint(webEndpoint)) {
     *       DiscoveredEndpoint discovered = (DiscoveredEndpoint) webEndpoint;
     *   }
     * }</pre>
     *
     * @param webEndpoint the web endpoint to check
     * @return {@code true} if the endpoint is a {@link DiscoveredEndpoint}, {@code false} otherwise
     */
    static boolean isExposableWebEndpoint(ExposableWebEndpoint webEndpoint) {
        return webEndpoint instanceof DiscoveredEndpoint;
    }

    /**
     * Determines whether the given {@link WebOperation} is a candidate for read invocation,
     * i.e., it is a {@link OperationType#READ READ} operation with no parameters.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   WebOperation operation = // obtain from ExposableWebEndpoint
     *   if (WebEndpoints.isReadWebOperationCandidate(operation)) {
     *       Object result = operation.invoke(context);
     *   }
     * }</pre>
     *
     * @param webOperation the web operation to check
     * @return {@code true} if the operation is a parameterless read operation, {@code false} otherwise
     */
    static boolean isReadWebOperationCandidate(WebOperation webOperation) {
        if (webOperation instanceof AbstractDiscoveredOperation) {
            AbstractDiscoveredOperation discoveredOperation = (AbstractDiscoveredOperation) webOperation;
            OperationMethod operationMethod = discoveredOperation.getOperationMethod();
            if (READ.equals(operationMethod.getOperationType())) {
                Method method = operationMethod.getMethod();
                if (method.getParameterCount() == 0) {
                    return true;
                }
            }
        }
        return false;
    }
}