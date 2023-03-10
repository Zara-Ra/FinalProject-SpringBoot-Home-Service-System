package ir.maktab.finalproject.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import ir.maktab.finalproject.data.entity.roles.Expert;
import ir.maktab.finalproject.data.entity.roles.QExpert;
import ir.maktab.finalproject.data.enums.ExpertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ExpertRepository extends JpaRepository<Expert, Integer>,
        QuerydslPredicateExecutor<Expert>, QuerydslBinderCustomizer<QExpert> {
    Optional<Expert> findByEmail(String email);

    Optional<Expert> findByVerificationCode(String verificationCode);

    List<Expert> findAllByStatus(ExpertStatus status);

    @Override
    default void customize(QuerydslBindings bindings, QExpert root) {
        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::contains);
    }
}
