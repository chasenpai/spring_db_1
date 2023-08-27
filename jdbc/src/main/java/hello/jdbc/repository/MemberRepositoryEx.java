package hello.jdbc.repository;

import hello.jdbc.domain.Member;

import java.sql.SQLException;

public interface MemberRepositoryEx {

    /**
     * 인터페이스의 구현체가 체크 예외를 던지려면 인터페이스 메서드에 체크 예외가 선언되어 있어야 한다
     * 하지만 체크 예외를 인터페이스에 도입 시 특정 기술에 종속되어 버린다
     */
    Member save(Member member) throws SQLException;
    Member findById(String memberId) throws SQLException;
    void update(String memberId, int money) throws SQLException;
    void delete(String memberId) throws SQLException;

}
