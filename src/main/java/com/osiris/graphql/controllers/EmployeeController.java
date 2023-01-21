package com.osiris.graphql.controllers;

import com.osiris.graphql.filters.EmployeeFilter;
import com.osiris.graphql.filters.FilterField;
import com.osiris.graphql.models.Department;
import com.osiris.graphql.models.Employee;
import com.osiris.graphql.models.Organization;
import com.osiris.graphql.models.inputs.EmployeeInput;
import com.osiris.graphql.repositories.DepartmentRepository;
import com.osiris.graphql.repositories.EmployeeRepository;
import com.osiris.graphql.repositories.OrganizationRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class EmployeeController {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;

    public EmployeeController(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, OrganizationRepository organizationRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
    }

    @QueryMapping
    public Iterable<Employee> employees(){
        return employeeRepository.findAll();
    }

    @QueryMapping
    public Employee employee(@Argument Integer id){
        return employeeRepository.findById(id).orElseThrow();
    }

    @MutationMapping
    public Employee newEmployee(@Argument EmployeeInput employee){
        Department department = departmentRepository.findById(employee.getDepartmentId()).get();
        Organization organization = organizationRepository.findById(employee.getOrganizationId()).get();
        Employee employee1 = Employee.builder()
                .id(null)
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .age(employee.getAge())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .department(department)
                .organization(organization)
                .build();
        return employeeRepository.save(employee1);
    }
    
    @QueryMapping
    public Iterable<Employee> employeesWithFilter(@Argument EmployeeFilter filter){
        Specification<Employee> spec = null;
        if(filter.getSalary() != null)
            spec = bySalary(filter.getSalary());
        if(filter.getAge() != null)
            spec = (spec == null ? byAge(filter.getAge()) : spec.and(byAge(filter.getAge())));
        if(filter.getPosition() != null)
            spec = (spec == null ? byPosition(filter.getPosition()) : spec.and(byPosition(filter.getPosition())));
        if(spec != null)
            return employeeRepository.findAll(spec);
        else
            return employeeRepository.findAll();
    }

    private Specification<Employee> byAge(FilterField age) {
        return (root, query, builder) -> age.generateCriteria(builder, root.get("age"));
    }

    private Specification<Employee> bySalary(FilterField salary) {
        return (root, query, builder) -> salary.generateCriteria(builder, root.get("salary"));
    }

    private Specification<Employee> byPosition(FilterField position){
        return (root, query, builder) -> position.generateCriteria(builder, root.get("position"));
    }
}
