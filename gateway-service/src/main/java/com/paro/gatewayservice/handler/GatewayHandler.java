package com.paro.gatewayservice.handler;

import com.paro.gatewayservice.model.Department;
import com.paro.gatewayservice.model.Hospital;
import com.paro.gatewayservice.model.Patient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class GatewayHandler {

    private final WebClient.Builder webClientBuilder;

    public GatewayHandler(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private String departmentClient="http://department-service";        //The host address will be fetched from Eureka
    private String patientClient="http://patient-service";
    private String hospitalClient="http://hospital-service";
    private static final String RESOURCE_PATH_Department="/service/department/";
    private static final String RESOURCE_PATH_Hospital="/service/hospital/";
    private String REQUEST_URI_Department=departmentClient+RESOURCE_PATH_Hospital;
    private String REQUEST_URI_Patient=patientClient+RESOURCE_PATH_Department;
    private String REQUEST_URI_Patient2=patientClient+RESOURCE_PATH_Hospital;
    private String REQUEST_URI_Hospital=hospitalClient+"/service/";

    public Mono<ServerResponse> getByHospitalWithPatients(ServerRequest request) {
        Long hospitalId= Long.valueOf(request.pathVariable("hospitalId"));
        Flux<Department> departmentList = webClientBuilder.build().get().uri(REQUEST_URI_Department + hospitalId).exchange().flatMapMany(response -> response.bodyToFlux(Department.class));
        Flux<Department> departmentFlux=departmentList.flatMap(department -> {
            Flux<Patient> patientFlux = webClientBuilder.build().get().uri(REQUEST_URI_Patient + department.getDepartmentId()).exchange().flatMapMany(response -> response.bodyToFlux(Patient.class));
            Mono<Department> departmentMono= patientFlux.collectList().map(list -> {
                department.setPatientList(list);
                return department;
            });
            return departmentMono;
        });

        return ok().contentType(MediaType.APPLICATION_JSON).body(fromPublisher(departmentFlux, Department.class));

    }


    public Mono<ServerResponse> getHospitalWithDepartments(ServerRequest request) {
        Long hospitalId= Long.valueOf(request.pathVariable("id"));
        Mono<Hospital> hospitalList=webClientBuilder.build().get().uri(REQUEST_URI_Hospital + hospitalId).exchange().flatMap(response -> response.bodyToMono(Hospital.class));
        Mono<Hospital> hospitalMono= hospitalList.flatMap(hospital ->{
            Flux<Department> departmentFlux = webClientBuilder.build().get().uri(REQUEST_URI_Department + hospital.getHospitalId()).exchange().flatMapMany(response -> response.bodyToFlux(Department.class));
            return departmentFlux.collectList()
                    .map(list->{
                        hospital.setDepartmentList(list);
                        return hospital;
                    });
        });

        return ok().contentType(MediaType.APPLICATION_JSON).body(fromPublisher(hospitalMono, Hospital.class));

    }

    public Mono<ServerResponse> getHospitalWithDepartmentsAndPatients(ServerRequest request) {
        Long hospitalId= Long.valueOf(request.pathVariable("id"));
        Mono<Hospital> hospitalList=webClientBuilder.build().get().uri(REQUEST_URI_Hospital + hospitalId).exchange().flatMap(response -> response.bodyToMono(Hospital.class));
        Mono<Hospital> hospitalMono= hospitalList.flatMap(hospital ->{
            Flux<Department> departmentList = webClientBuilder.build().get().uri(REQUEST_URI_Department + hospital.getHospitalId()).exchange().flatMapMany(response -> response.bodyToFlux(Department.class));
            Flux<Department> departmentFlux=departmentList.flatMap(department -> {
                Flux<Patient> patientFlux = webClientBuilder.build().get().uri(REQUEST_URI_Patient + department.getDepartmentId()).exchange().flatMapMany(response -> response.bodyToFlux(Patient.class));
                Mono<Department> departmentMono= patientFlux.collectList().map(list -> {
                    department.setPatientList(list);
                    return department;
                });
                return departmentMono;
            });
            return departmentFlux.collectList()
                    .map(list->{
                        hospital.setDepartmentList(list);
                        return hospital;
                    });
        });

        return ok().contentType(MediaType.APPLICATION_JSON).body(fromPublisher(hospitalMono, Hospital.class));
    }

    public Mono<ServerResponse> getHospitalWithPatients(ServerRequest request) {
        Long hospitalId= Long.valueOf(request.pathVariable("id"));
        Mono<Hospital> hospitalList=webClientBuilder.build().get().uri(REQUEST_URI_Hospital + hospitalId).exchange().flatMap(response -> response.bodyToMono(Hospital.class));
        Mono<Hospital> hospitalMono= hospitalList.flatMap(hospital ->{
            Flux<Patient> patientFlux = webClientBuilder.build().get().uri(REQUEST_URI_Patient2 + hospital.getHospitalId()).exchange().flatMapMany(response -> response.bodyToFlux(Patient.class));
            return patientFlux.collectList()
                    .map(list->{
                        hospital.setPatientList(list);
                        return hospital;
                    });
        });
        return ok().contentType(MediaType.APPLICATION_JSON).body(fromPublisher(hospitalMono, Hospital.class));

    }
}
