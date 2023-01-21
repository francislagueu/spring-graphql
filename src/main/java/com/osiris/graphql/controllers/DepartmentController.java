package com.osiris.graphql.controllers;

import com.osiris.graphql.models.Department;
import com.osiris.graphql.models.Employee;
import com.osiris.graphql.models.Organization;
import com.osiris.graphql.models.inputs.DepartmentInput;
import com.osiris.graphql.repositories.DepartmentRepository;
import com.osiris.graphql.repositories.OrganizationRepository;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;


import java.util.NoSuchElementException;

@Controller
public class DepartmentController {
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;


    public DepartmentController(DepartmentRepository departmentRepository, OrganizationRepository organizationRepository) {
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
    }

    @MutationMapping
    public Department newDepartment(@Argument DepartmentInput department){
        Organization organization = organizationRepository.findById(department.getOrganizationId()).get();
        return departmentRepository.save(new Department(null, department.getName(), organization, null));
    }

    @QueryMapping
    public Iterable<Department> departments(DataFetchingEnvironment environment) {
        DataFetchingFieldSelectionSet set = environment.getSelectionSet();
        if(set.contains("employees") && !set.contains("organization"))
            return departmentRepository.findAll(fetchEmployees());
        else if(!set.contains("employees") && set.contains("organization"))
            return departmentRepository.findAll(fetchOrganization());
        else if(set.contains("employees") && set.contains("organization"))
            return departmentRepository.findAll(fetchEmployees().and(fetchOrganization()));
        else 
            return departmentRepository.findAll();
    }
    
    @QueryMapping
    public Department department(@Argument Integer id, DataFetchingEnvironment environment){
        Specification<Department> spec = byId(id);
        DataFetchingFieldSelectionSet set = environment.getSelectionSet();
        if(set.contains("employees"))
            spec = spec.and(fetchEmployees());
        if(set.contains("organization"))
            spec = spec.and(fetchOrganization());
        return departmentRepository.findOne(spec).orElseThrow(NoSuchElementException::new);
    }

    private Specification<Department> byId(Integer id) {
        return (root, query, builder) -> builder.equal(root.get("id"), id);
    }

    private Specification<Department> fetchOrganization() {
        return (root, query, builder) -> {
            Fetch<Department, Organization> fetch = root.fetch("organization", JoinType.LEFT);
            Join<Department, Organization> join = (Join<Department, Organization>) fetch;
            return join.getOn();
        };
    }

    private Specification<Department> fetchEmployees() {
        return (root, query, builder) -> {
            Fetch<Department, Employee> fetch = root.fetch("employees", JoinType.LEFT);
            Join<Department, Employee> join = (Join<Department, Employee>) fetch;
            return join.getOn();
        };
    }
}
