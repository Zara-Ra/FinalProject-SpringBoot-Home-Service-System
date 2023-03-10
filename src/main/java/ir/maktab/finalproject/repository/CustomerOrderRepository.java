package ir.maktab.finalproject.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import ir.maktab.finalproject.data.entity.CustomerOrder;
import ir.maktab.finalproject.data.entity.ExpertOffer;
import ir.maktab.finalproject.data.entity.QCustomerOrder;
import ir.maktab.finalproject.data.entity.SubService;
import ir.maktab.finalproject.data.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Integer>,
        QuerydslPredicateExecutor<CustomerOrder>, QuerydslBinderCustomizer<QCustomerOrder> {
    //@Query("FROM CustomerOrder o WHERE o.subService = ?1 AND o.status = ?2 OR o.status = ?3")

    @Query("FROM CustomerOrder o WHERE o.subService = ?1 AND o.status IN (?2,?3)")
    List<CustomerOrder> findAllBySubServiceAndTwoStatus(SubService subService, OrderStatus status1, OrderStatus status2);

    @Override
    default void customize(
            QuerydslBindings bindings, QCustomerOrder root) {
        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

}
