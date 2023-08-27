package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class CheckedTest {

    @Test
    void checkedCatch(){
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checkedThrow() {
        Service service = new Service();
        assertThatThrownBy(service::callThrow).isInstanceOf(MyCheckedException.class);
    }

    //Exception 을 상속받은 예외는 체크 예외가 됨
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message){
            super(message);
        }
    }

    //Checked 예외는 잡아서 던지거나 처리해야 함
    static class Service {

        Repository repository = new Repository();

        //예외를 잡아서 처리
        public void callCatch(){
            try{
                repository.call();
            }catch (MyCheckedException e){
                log.info("예외 처리, message = {}", e.getMessage(), e);
            }
        }

        //예외를 밖으로 던짐
        public void callThrow() throws MyCheckedException {
            repository.call();
        }

        /**
         * 체크 예외의 장점
         * - 개발자가 실수로 예외를 누락하지 않도록 컴파일러를 통해 문제를 잡을 수 있다
         * 체크 예외의 단점
         * - 실제로는 개발자가 모든 체크 예외를 반드시 잡거나 던지도록 처리해야 하기 때문에 번거롭고
         * - 크게 신경쓰고 싶지 않은 예외까지 모두 챙겨야 한다
         * - 의존 관계에 대한 문제가 발생한다
         */
    }

    static class Repository {
        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex");
        }
    }

}
