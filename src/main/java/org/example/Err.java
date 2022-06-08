package org.example;

public interface Err {
    int UNKNOWN = 0x20000001;
    int ILLEGAL_OPCODE = 0x20000002;
    int ILLEGAL_LENGTH = 0x20000003;
    int WRONG_VERSION = 0x20000004;
    int MESS_EXISTS = 0x20000005;
    int ILLEGAL_NAME = 0x20000006;
    int ILLEGAL_MESS = 0x20000007;
    int IN_USE = 0x20000008;
    int SCHED_FULL = 0x20000009;
    int NO_SUCH_ITEM = 0x20000010;
    int SCHED_EMPTY = 0x20000011;
}
