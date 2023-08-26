package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    //트랜잭션 사용

   private final MemberRepositoryV2 memberRepository;
   private final DataSource dataSource;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        Connection con = dataSource.getConnection();
        try {
            //트랜잭션 시작 - 자동 커밋 모드를 끄면 이후부터 수동 커밋 모드로 동작한다 이를 보통 트랜잭션을 시작한다고 표현
            con.setAutoCommit(false);
            bizLogic(con, fromId, toId, money);//비즈니스 로직 시작
            con.commit(); //성공 시 커밋
        } catch (Exception e) {
            log.error("failed");
            con.rollback(); //실패 시 롤백
            throw new IllegalStateException(e);
        } finally {
            release(con); //커넥션 풀을 돌려주기 전에 기본값인 자동 커밋 모드로 변경
        }

        /**
         * 트랜잭션 ACID
         * - 원자성(Atomicity) : 트랜잭션 내에서 실행한 작업들은 마치 하나의 작업인 것처럼 모두 성공하거나 실패해야 한다
         * - 일관성(Consistency) : 모든 트랜잭션은 일관성 있는 데이터베이스 상태를 유지해야 한다. 예)무결성 제약 조건 항상 만족
         * - 격리성(Isolation) : 동시에 실행되는 트랜잭션들이 서로에게 영향을 미치지 않도록 격리한다. 예)동시에 같은 데이터를 수정하지 못하도록 함
         * - 지속성(Durability) : 트랜잭션을 성공적으로 끝내면 그 결과가 항상 기록되어야 한다. 중간에 시스템에 문제가 생겨도 데이터베이스 로그 등을 사용해서 성공한 트랜잭션 내용을 복구해야 함
         */

        /**
         * 트랜잭션을 적용하면서 생긴 문제
         * - 트랜잭션을 적용하기 위해 JDBC 구현 기술이 서버스 계층에 누수되었다
         * - 데이터 접근 계층의 JDBC 구현 기술 예외가 서비스 계층으로 전파된다
         * - 같은 트랜잭션을 유지하기 위해 커넥션을 파라미터로 넘겨야 한다
         * - 만약 같은 기능이라도 트랜잭션을 유지하지 않다면 기능을 분리해야 한다
         * - 트랜잭션을 적용하기 위한 코드가 반복된다
         * - 이 문제들을 해결하기 위해 트랜잭션 추상화가 필요하다
         */
    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validate(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private void release(Connection con) {
        if(con != null){
            try{
                con.setAutoCommit(true);
                con.close();
            }catch (Exception e){
                log.info("error", e);
            }
        }
    }

    private void validate(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }

}
