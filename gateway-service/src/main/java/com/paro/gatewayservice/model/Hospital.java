package com.paro.gatewayservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor

@Getter
@Setter
@ToString
public class Hospital {

    private String id;
    private Long hospitalId;
    private String name;
    private String address;
    private List<Department> departmentList=new ArrayList<>();
    private List<Patient> patientList=new ArrayList<>();

    public Hospital(Long hospitalId, String name, String address) {
        this.hospitalId = hospitalId;
        this.name = name;
        this.address = address;
    }
}
