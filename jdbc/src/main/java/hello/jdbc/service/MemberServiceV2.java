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
        try{
            //트랜잭션 시작 - 자동 커밋 모드를 끄면 이후부터 수동 커밋 모드로 동작한다 이를 보통 트랜잭션을 시작한다고 표현
            con.setAutoCommit(false);
            bizLogic(con, fromId, toId, money);//비즈니스 로직 시작
            con.commit(); //성공 시 커밋
        }catch (Exception e){
            log.error("failed");
            con.rollback(); //실패 시 롤백
            throw new IllegalStateException(e);
        }finally {
            release(con); //커넥션 풀을 돌려주기 전에 기본값인 자동 커밋 모드로 변경
        }

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
