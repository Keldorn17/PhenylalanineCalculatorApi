package com.keldorn.phenylalaninecalculatorapi.utils;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.dto.params.QueryRequest;
import com.keldorn.phenylalaninecalculatorapi.exception.InvalidRSQLException;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import lombok.experimental.UtilityClass;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.Node;
import io.github.perplexhub.rsql.RSQLJPAPredicateConverter;
import io.github.perplexhub.rsql.RSQLJPASupport;
import io.github.perplexhub.rsql.RSQLOperators;
import io.github.perplexhub.rsql.SortSupport;

@UtilityClass
public class FoodQueryParamsUtil {

    private static final Map<String, String> PROPERTY_MAP = new HashMap<>();
    private static final Map<String, JoinType> JOIN_HINTS = Map.of(
            "Food.foodType", JoinType.LEFT,
            "Food.user", JoinType.LEFT
    );

    static {
        PROPERTY_MAP.put("id", "id");
        PROPERTY_MAP.put("name", "name");
        PROPERTY_MAP.put("protein", "protein");
        PROPERTY_MAP.put("calories", "calories");
        PROPERTY_MAP.put("phenylalanine", "phenylalanine");
        PROPERTY_MAP.put("foodTypeName", "foodType.name");
        PROPERTY_MAP.put("foodTypeMultiplier", "foodType.multiplier");
        PROPERTY_MAP.put("username", "user.username");
    }

    public static Specification<Food> createQuerySpecification(QueryRequest request, Long currentUserId) {
        Specification<Food> spec = ((root, _, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("isDeleted")));
        if (isNotBlank(request.getQuery())) {
            spec = spec.and(toSpecification(request.getQuery(), currentUserId));
        }
        if (isNotBlank(request.getSort())) {
            spec = spec.and(RSQLJPASupport.toSort(SortSupport.builder()
                    .sortQuery(request.getSort())
                    .propertyPathMapper(PROPERTY_MAP)
                    .joinHints(JOIN_HINTS)
                    .build()));
        }
        return spec;
    }

    private static Specification<Food> toSpecification(String rsqlQuery, Long currentUserId) {
        return (root, _, cb) -> {
            Node rsql = new RSQLParser(RSQLOperators.supportedOperators()).parse(rsqlQuery);
            RSQLJPAPredicateConverter visitor = new RSQLJPAPredicateConverter(cb, PROPERTY_MAP, null, JOIN_HINTS) {
                @Override
                public Predicate visit(ComparisonNode node, From root) {
                    if ("own".equals(node.getSelector())) {
                        return createOwnPredicate(node, root, cb, currentUserId);
                    }
                    return super.visit(node, root);
                }
            };
            return rsql.accept(visitor, root);
        };
    }

    private static Predicate createOwnPredicate(ComparisonNode node, From<?, ?> root, CriteriaBuilder cb,
            Long currentUserId) {
        var op = node.getOperator();
        if ((!op.equals(RSQLOperators.EQUAL) && !op.equals(RSQLOperators.NOT_EQUAL))
                || node.getArguments().size() != 1) {
            throw new InvalidRSQLException("Invalid operator for own");
        }
        String arg = node.getArguments().getFirst();
        if (!"true".equalsIgnoreCase(arg) && !"false".equalsIgnoreCase(arg)) {
            throw new InvalidRSQLException("Invalid argument for own");
        }
        boolean isOwn = Boolean.parseBoolean(arg);
        if (op.equals(RSQLOperators.NOT_EQUAL)) {
            isOwn = !isOwn;
        }
        Path<Long> userIdPath = root.get("user").get("userId");
        return isOwn
                ? cb.equal(userIdPath, currentUserId)
                : cb.or(cb.isNull(userIdPath), cb.notEqual(userIdPath, currentUserId));
    }

    private static boolean isNotBlank(String input) {
        return input != null && !input.isBlank();
    }

}
