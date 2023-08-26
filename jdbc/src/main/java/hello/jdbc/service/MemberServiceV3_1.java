package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    //트랜잭션 매니저 사용

   private final MemberRepositoryV3 memberRepository;
   private final PlatformTransactionManager transactionManager; //트랜잭션 매니저를 주입 받는다 JDBC 의 경우 DataSourceTransactionManager

    public void accountTransfer(String fromId, String toId, int money) {

        //트랜잭션 시작 - Transaction status 를 반환한다. 현재 트랜잭션의 상태 정보가 포함되어 있다
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition()); //트랜잭션 관련 옵션 설정

        try {
            bizLogic(fromId, toId, money); //비즈니스 로직 시작
            transactionManager.commit(status); //성공 시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); //실패 시 롤백
            throw new IllegalStateException(e);
        }

        /**
         * 트랜잭션 매니저의 동작 흐름
         * 1. 서비스 계층에서 transactionManager.getTransaction() 을 호출해서 트랜잭션을 시작한다
         * 2. 트랜잭션을 시작하려면 먼저 데이터베이스 커넥션이 필요하다. 내부에서 데이터소스를 사용해서 커넥션을 생성
         * 3. 커넥션을 수동 커밋 모드로 변경해서 실제 데이터베이스 트랜잭션을 시작한다
         * 4. 커넥션을 트랜잭션 동기화 매니저에 저장한다
         * 5. 트랜잭션 동기화 매니저는 쓰레드로컬에 커넥션을 보관한다. 따라서 멀티 쓰레드 환경에서 안전하게 커넥션을 보관
         * 6. 서비스는 비즈니스 로직을 실행하면서 리포지토리의 메서드들을 호출한다. 이때 커넥션을 파라미터로 전달하지 않음
         * 7. 리포지토리 메서드들은 트랜잭션이 시작된 커넥션이 필요하다. DataSourceUtils 를 사용해서 트랜잭션 동기화 매니저에 보관된
         *    커넥션을 꺼내서 사용한다. 이 과정을 통해서 자연스럽게 같은 커넥션을 사용하고 트랜잭션도 유지된다
         * 8. 획득한 커넥션을 사용해서 SQL 을 데이터베이스에 전달해서 실행한다
         * 9. 비즈니스 로직이 끝나고 트랜잭션을 종료한다
         * 10. 트랜잭션을 종료하려면 동기화된 커넥션이 필요한데, 트랜잭션 동기화 매니저를 통해 동기화된 커넥션을 획득한다
         * 11. 획득한 커넥션을 통해 데이터베이스에 트랜잭션을 커밋하거나 롤백한다
         * 12. 전체 리소스를 정리한다
         */
    }

    /**
     * 트랜잭션 추상화 덕분에 서비스 코드는 이제 JDBC 기술에 의존하지 않는다.
     * - 이후 JDBC 에서 JPA 로 변경해도 서비스 코드를 그대로 유지 가능
     * - 기술 변경 시 의존관계 주입만 변경해주면 됨
     * - 트랜잭션 동기화 덕분에 커넥션을 파라미터로 넘기지 않아도 됨
     */
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validate(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validate(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }

}
