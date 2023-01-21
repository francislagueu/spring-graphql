package com.osiris.graphql.repositories;


import com.osiris.graphql.models.Organization;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface OrganizationRepository extends CrudRepository<Organization, Integer>, JpaSpecificationExecutor<Organization> {
}
