package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

@Slf4j
public class MemberServiceV3_2 {

    //트랜잭션 템플릿 사용

    private final MemberRepositoryV3 memberRepository;
    private final TransactionTemplate txTemplate;

    public MemberServiceV3_2(MemberRepositoryV3 memberRepository, PlatformTransactionManager txManager) {
        this.memberRepository = memberRepository;
        this.txTemplate = new TransactionTemplate(txManager);
    }

    public void accountTransfer(String fromId, String toId, int money) {

        /**
         * TransactionTemplate
         * - 템플릿 콜백 패턴을 적용하기 위한 템플릿 클래스
         * - 트랜잭션 템플릿 덕분에 트랜잭션 시작, 커밋, 롤백 코드가 모두 제거되었다
         * - 비즈니스 로직이 정상 수행되면 커밋하고, 언체크 예외가 발생하면 롤백한다
         * - execute() 는 응답 값이 있을 때, executeWithoutResult() 는 응답 값이 없을 때 사용
         */
        txTemplate.executeWithoutResult((status) -> {
            try{
                bizLogic(fromId, toId, money);
            }catch (SQLException e){ //해당 람다에서 체크 예외를 밖으로 던질 수 없어서 언체크 예외로 바꿔서 던짐
                throw new IllegalStateException(e);
            }
        });

        /**
         * 하지만 아직 서비스 비즈니스로직와 트랜잭션을 처리하는 로직이 함께 있다
         * 애플리케이션을 구성하는 로직을 핵심과 부가 기능으로 나눴을 때, 트랜잭션은 부가 기능이다
         * 두 로직이 한 곳에 있으면 두 관심사를 하나의 클래스에서 처리하게 되기 때문에 유지보수가 어려워진다
         */
    }

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
