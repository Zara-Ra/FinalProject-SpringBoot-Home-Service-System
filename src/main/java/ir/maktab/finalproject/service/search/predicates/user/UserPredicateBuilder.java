package ir.maktab.finalproject.service.search.predicates.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import ir.maktab.finalproject.data.enums.Role;
import ir.maktab.finalproject.service.search.SearchCriteria;

import java.util.ArrayList;
import java.util.List;

public class UserPredicateBuilder {
    private List<SearchCriteria> params;

    public UserPredicateBuilder() {
        params = new ArrayList<>();
    }

    public void with(String key, String operation, Object value) {
        params.add(new SearchCriteria(key, operation, value));
    }

    public BooleanExpression build(Role role) {
        if (params.size() == 0)
            return null;

        List<BooleanExpression> predicates = params.stream().map(param -> {
            UserPredicate predicate = new UserPredicate(param);
            return predicate.getPredicate(role);
        }).toList();

        BooleanExpression result = Expressions.asBoolean(true).isTrue();
        for (BooleanExpression predicate : predicates) {
            if (predicate == null)
                return Expressions.asBoolean(false).isTrue();
            result = result.and(predicate);
        }
        return result;
    }
}
