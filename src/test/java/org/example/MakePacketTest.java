package org.example;

import org.junit.Assert;
import org.junit.Test;
import org.hamcrest.CoreMatchers.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.example.OpCode.*;

import static org.example.MakePacket.*;

public class MakePacketTest {
    @Test
    public void helloPacketFormedProperly() {
        try {
            byte[] test = hello("123");
            System.out.println(Arrays.toString(test));
            assertEquals(test.length, 32);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void invalidPasswordRejected() {
        assertThrows(Exception.class, () -> hello(""));
        assertThrows(Exception.class, () -> hello("averyverylongpasswordindeedsomemightsaytoolong"));
    }

    @Test
    public void opcodeHelloCorrect() {
        try {
            byte[] test = hello("test password");
            byte[] code = new byte[4];
            System.arraycopy(test,0,code,0, 4);
            assertArrayEquals(ByteBuffer.allocate(4).putInt(HELLO).array(), code);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void setMessageHandlesMessage() {
        try {
            byte[] test = set_mess("hello world");
            assertEquals(test[20], 0x00);
            assertEquals(test[18], 'd');
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
