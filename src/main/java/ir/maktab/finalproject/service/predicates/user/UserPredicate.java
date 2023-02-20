package ir.maktab.finalproject.service.predicates.user;

import com.querydsl.core.types.dsl.*;
import ir.maktab.finalproject.data.entity.QCredit;
import ir.maktab.finalproject.data.entity.roles.QExpert;
import ir.maktab.finalproject.data.entity.roles.User;
import ir.maktab.finalproject.util.exception.ValidationException;
import ir.maktab.finalproject.util.search.SearchCriteria;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserPredicate {
    private SearchCriteria criteria;

    public UserPredicate(final SearchCriteria criteria) {
        this.criteria = criteria;
    }

    public BooleanExpression getPredicate(Class classType, String className) {
        PathBuilder<User> entityPath = new PathBuilder<>(classType, className);

        if (criteria.getKey().equals("registerDate")) {
            return dateBooleanExpression(entityPath);
        }
        if (criteria.getKey().equals("id")) {
            return intBooleanExpression(entityPath);
        }
        if (criteria.getKey().equals("credit")) {
            return creditBooleanExpression();
        }
        if (criteria.getKey().equals("averageScore")) {
            return intBooleanExpression(entityPath);
        }
        if (criteria.getKey().equals("subService")) {
            return subServiceBooleanExpression();
        }
        StringPath path = entityPath.getString(criteria.getKey());
        if (criteria.getOperation().equalsIgnoreCase(":")) {
            return path.eq(criteria.getValue().toString());
        }
        return null;
    }

    private BooleanExpression subServiceBooleanExpression() {
        StringPath path = QExpert.expert.subServiceList.any().subName;
        return path.eq(criteria.getValue().toString());
    }

    private BooleanExpression creditBooleanExpression() {
        QCredit qCredit = QCredit.credit;
        NumberPath<Double> path = qCredit.amount;
        double value = Double.parseDouble(criteria.getValue().toString());
        return switch (criteria.getOperation()) {
            case ":" -> path.eq(value);
            case ">" -> path.goe(value);
            case "<" -> path.loe(value);
            default -> null;
        };
    }

    private BooleanExpression intBooleanExpression(PathBuilder<User> entityPath) {
        if (!isInteger(criteria.getValue().toString())) {
            QExpert qExpert = QExpert.expert;
            NumberPath<Double> path = qExpert.averageScore;
            return switch (criteria.getValue().toString()) {
                case "max" -> path.goe(4.5);
                case "min" -> path.loe(0.5);
                default -> null;
            };
        }
        NumberPath<Integer> path = entityPath.getNumber(criteria.getKey(), Integer.class);
        int value = Integer.parseInt(criteria.getValue().toString());
        return switch (criteria.getOperation()) {
            case ":" -> path.eq(value);
            case ">" -> path.goe(value);
            case "<" -> path.loe(value);
            default -> null;
        };
    }

    private BooleanExpression dateBooleanExpression(PathBuilder<User> entityPath) {
        DatePath<Date> path = entityPath.getDate(criteria.getKey(), Date.class);
        String operation = criteria.getOperation();
        try {
            return switch (operation) {
                case ":" -> path.eq(
                        new SimpleDateFormat("yyyy-MM-dd").parse(criteria.getValue().toString()));
                case "<" -> path.before(
                        new SimpleDateFormat("yyyy-MM-dd").parse(criteria.getValue().toString()));
                case ">" -> path.after(
                        new SimpleDateFormat("yyyy-MM-dd").parse(criteria.getValue().toString()));
                default -> null;
            };
        } catch (ParseException e) {
            throw new ValidationException("Invalid Date Type(yyyy-MM-dd)");
        }
    }

    public static boolean isInteger(final String str) {
        try {
            Integer.parseInt(str);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }
}