package com.exchangeinformant.repository;

import com.exchangeinformant.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
    Company findCompanyBySecureCode(String secureCode);
}
