package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UncheckedTest {

    @Test
    void uncheckedCatch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void uncheckedThrow() {
        Service service = new Service();
        assertThatThrownBy(service::callThrow)
                .isInstanceOf(MyUncheckedException.class);
    }

    //RuntimeException 을 상속받은 예외는 언체크 예외가 됨
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    //Unchecked 예외는 예외를 잡거나 던지지 않다도 된다
    static class Service {
        Repository repository = new Repository();

        //필요한 경우 예외를 잡아서 처리
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                log.info("예외 처리, message = {}", e.getMessage(), e);
            }
        }

        //예외를 잡지 않으면 자동으로 밖으로 던짐
        public void callThrow() {
            repository.call();
        }

        /**
         * 언체크 예외의 장점
         * - 신경쓰고 싶지 않은 언체크 예외를 무시할 수 있다
         * - 신경쓰고 싶지 않은 예외의 의존관계를 참조하지 않아도 된다
         * 언체크 예외의 단점
         * - 개발자가 실수로 예외를 누락할 수 있다
         */
    }

    static class Repository {
        public void call() {
            throw new MyUncheckedException("ex");
        }
    }

}
