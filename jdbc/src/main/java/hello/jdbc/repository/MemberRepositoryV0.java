package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class MemberRepositoryV0 {

    //JDBC DriverManager 사용
    public Member save(Member member) throws SQLException {

        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try{
            conn = getConnection(); //데이터베이스 커넥션 획득
            pstmt = conn.prepareStatement(sql); //sql 준비
            pstmt.setString(1, member.getMemberId()); //1번째 ? 에 값을 바인딩 - SQL 인젝션 공격 예방
            pstmt.setInt(2, member.getMoney()); //2번째 ? 에 값을 바인딩
            pstmt.executeUpdate(); //준비된 SQL 을 실제로 데이터베이스에 전달
            return member;
        }catch (SQLException e){
            log.error("db error", e);
            throw  e;
        }finally{
            close(conn, pstmt, null);
        }

    }

    //리소스 정리를 하지 않으면 커넥션이 부족해질 수 있음
    private void close(Connection conn, Statement stmt, ResultSet rs){

        if(rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if(stmt != null){
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }


    private Connection getConnection(){
        return DBConnectionUtil.getConnection();
    }

}
