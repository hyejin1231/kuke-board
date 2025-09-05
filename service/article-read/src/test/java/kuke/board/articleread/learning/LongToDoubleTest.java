package kuke.board.articleread.learning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class LongToDoubleTest {
    @Test
    void longToDoubleTest() {
        // long은 64비트로 정수값 표현
        long longValue = 111_111_111_111_111_111L;
        System.out.println("longValue = " + longValue); // 111111111111111111

        // double은 64비트로 부동소수점 표현
        // double 은 데이터를 정밀하게 표현하지 못해서 데이터 유실 사태가 발생함
        double doubleValue = longValue;
        System.out.println("doubleValue = " + new BigDecimal(doubleValue)); // 111111111111111104


        long longValue2 = (long) doubleValue;
        System.out.println("longValue2 = " + longValue2); // 111111111111111104
    }
}
