package com.companylib.api.service;

import com.companylib.api.domain.dto.*;
import com.companylib.api.domain.dto.search.AdvancedSearchRequest;
import com.companylib.api.domain.dto.search.CompanyFieldRegistry;
import com.companylib.api.domain.dto.search.SortDirection;
import com.companylib.api.domain.entity.Company;
import com.companylib.api.repository.CompanyRepository;
import com.companylib.api.repository.CompanyWorkplaceRepository;
import com.companylib.api.repository.spec.CompanySpecification;
import com.companylib.api.repository.spec.FuzzyQueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyWorkplaceRepository companyWorkplaceRepository;
    private final FuzzyQueryBuilder fuzzyQueryBuilder;

    @Transactional(readOnly = true)
    public PageResponse<CompanySummaryDto> search(CompanySearchRequest req) {
        PageRequest pageable = PageRequest.of(
            req.getPage(), req.getSize(),
            Sort.by(nullsLast(Sort.Direction.DESC, "capitalStock"))
        );
        Page<CompanySummaryDto> result = companyRepository.searchCompanies(
            req.getName(),
            req.getLocation(),
            req.getMinCapital(),
            req.getMinEmployees(),
            pageable
        ).map(CompanySummaryDto::new);
        log.debug("Company search: name={}, location={}, results={}", req.getName(), req.getLocation(), result.getTotalElements());
        return new PageResponse<>(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<CompanySummaryDto> advancedSearch(AdvancedSearchRequest req) {
        // フィールド・matchType の妥当性を事前検証
        req.conditions().forEach(c -> CompanyFieldRegistry.validate(c.field(), c.matchType()));

        // ソートフィールドのホワイトリスト検証
        req.sort().forEach(s -> {
            if (!CompanyFieldRegistry.ALLOWED_FIELDS.containsKey(s.field())) {
                throw new IllegalArgumentException("不正なソートフィールド: " + s.field());
            }
        });

        PageRequest pageable = buildPageable(req);

        if (CompanySpecification.hasFuzzy(req)) {
            Page<Company> page = fuzzyQueryBuilder.search(req, pageable);
            log.debug("Advanced search (FUZZY): conditions={}, results={}", req.conditions().size(), page.getTotalElements());
            return new PageResponse<>(page.map(CompanySummaryDto::new));
        }

        Specification<Company> spec = CompanySpecification.from(req);
        Page<Company> page = companyRepository.findAll(spec, pageable);
        log.debug("Advanced search: conditions={}, logic={}, results={}", req.conditions().size(), req.logic(), page.getTotalElements());
        return new PageResponse<>(page.map(CompanySummaryDto::new));
    }

    @Transactional(readOnly = true)
    public CompanyDetailDto getDetail(String corporateNumber) {
        log.debug("Fetching company detail: corporateNumber={}", corporateNumber);
        Company company = companyRepository.findByIdWithDetails(corporateNumber)
            .orElseThrow(() -> {
                log.warn("Company not found: corporateNumber={}", corporateNumber);
                return new CompanyNotFoundException(corporateNumber);
            });
        WorkplaceDto workplace = companyWorkplaceRepository
            .findByCompanyCorporateNumber(corporateNumber)
            .map(WorkplaceDto::new)
            .orElse(null);
        return new CompanyDetailDto(company, workplace);
    }

    private PageRequest buildPageable(AdvancedSearchRequest req) {
        Sort sort;
        if (req.sort().isEmpty()) {
            sort = Sort.by(nullsLast(Sort.Direction.DESC, "capitalStock"));
        } else {
            List<Sort.Order> orders = req.sort().stream()
                .map(s -> nullsLast(
                    s.direction() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC,
                    s.field()
                ))
                .toList();
            sort = Sort.by(orders);
        }
        return PageRequest.of(req.page(), req.size(), sort);
    }

    /**
     * 昇順・降順ともに NULL を末尾に追加する Sort.Order を生成する。
     *
     * PostgreSQL のデフォルトは ASC NULLS LAST / DESC NULLS FIRST であるため、
     * 降順時に明示的に NULLS LAST を指定しないと NULL が先頭に来てしまう。
     * Spring Data JPA の NullHandling.NULLS_LAST は Hibernate 6 以降で
     * JPQL の NULLS LAST 句として出力される。
     */
    private static Sort.Order nullsLast(Sort.Direction direction, String field) {
        return new Sort.Order(direction, field, Sort.NullHandling.NULLS_LAST);
    }
}
