package com.akouma.veyuzwebapp.utils;

import com.akouma.veyuzwebapp.model.Client;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {

    public static Specification<Client> nameContains(String keyword) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("denomination"), "%" + keyword + "%");
    }

    public static Specification<Client> phoneContains(String keyword) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("telephone"), "%" + keyword + "%");
    }

    public static Specification<Client> emailContains(String keyword) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("numeroContribuable"), "%" + keyword + "%");
    }

    public static Specification<Client> surnameContains(String keyword) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("denomination"), "%" + keyword + "%");
    }

}
