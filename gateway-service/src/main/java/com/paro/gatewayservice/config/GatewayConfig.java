package com.paro.gatewayservice.config;

import com.paro.gatewayservice.handler.GatewayHandler;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/patient/**")                                                   //When request is made to the Gateway at "/patient/**" it will be routed to uri below
                        .filters(f -> f.rewritePath("/patient/(?<path>.*)", "/service/$\\{path}")
                        .hystrix(c -> c.setName("hystrixCommand").setFallbackUri("forward:/fallback/patient")))
                        .uri("lb://patient-service")
                        .id("patient-service"))
                .route(r -> r.path("/department/**")
                        .filters(f -> f.rewritePath("/department/(?<path>.*)", "/service/$\\{path}")
                        .hystrix(c -> c.setName("hystrixCommand").setFallbackUri("forward:/fallback/department")))
                        .uri("lb://department-service")
                        .id("department-service"))
                .route(r -> r.path("/hospital/**")
                        .filters(f -> f.rewritePath("/hospital/(?<path>.*)", "/service/$\\{path}")
                        .hystrix(c -> c.setName("hystrixCommand").setFallbackUri("forward:/fallback/hospital")))
                        .uri("lb://hospital-service")
                        .id("hospital-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> departmentServiceRouting( GatewayHandler gatewayHandler) {
        RouterFunction<ServerResponse> apiCompositionRoutes= RouterFunctions
                .route(GET("department/hospital/{hospitalId}/with-patients"), gatewayHandler::getByHospitalWithPatients)
                .andRoute(GET("hospital/{id}/with-departments"), gatewayHandler::getHospitalWithDepartments)
                .andRoute(GET("hospital/{id}/with-departments-and-patients"), gatewayHandler::getHospitalWithDepartmentsAndPatients)
                .andRoute(GET("hospital/{id}/with-patients"), gatewayHandler::getHospitalWithPatients);

        return apiCompositionRoutes;
    }


}
