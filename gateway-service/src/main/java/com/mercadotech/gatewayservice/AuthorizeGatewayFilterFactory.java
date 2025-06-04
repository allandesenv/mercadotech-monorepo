package com.mercadotech.gatewayservice;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GatewayFilterFactory para aplicar autorização baseada em roles.
 * Configurado no application.yml como:
 *
 * filters:
 *   - name: Authorize
 *     args:
 *       roles: GERENTE,CAIXA
 */
@Component
public class AuthorizeGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthorizeGatewayFilterFactory.Config> {

    private static final String ROLES_HEADER = "X-Auth-User-Roles";

    public AuthorizeGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            List<String> userRoles = exchange.getRequest()
                    .getHeaders()
                    .getOrDefault(ROLES_HEADER, List.of())
                    .stream()
                    .flatMap(header -> Arrays.stream(header.split(",")))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

            List<String> requiredRoles = config.getRoles() != null
                    ? config.getRoles().stream()
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .collect(Collectors.toList())
                    : List.of();

            boolean hasPermission = requiredRoles.isEmpty() ||
                    userRoles.stream().anyMatch(requiredRoles::contains);

            if (!hasPermission) {
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Insufficient roles."));
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
        private List<String> roles;

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
