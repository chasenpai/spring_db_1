package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    /**
     * JDBC 는 java.sql.Connection 표준 커넥션 인터페이스를 정의한다
     * H2 데이터베이스 드라이버는 JDBC Connection 인터페이스를 구현한
     * org.h2.jdbc.JdbcConnection 구현체를 제공한다
     */
    public static Connection getConnection(){
        try {
            /**
             * DriverManager 는 URL 정보를 체크해서 본인이 처리할 수 있는 요청인지 확인하고
             * 실제 데이터베이스에 연결해서 커넥션을 획득하고 클라이언트에 반환한다
             */
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection = {}, class = {}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

}
