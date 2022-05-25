package org.example;

public interface OpCode {
    int ERR = 0x10000001;
    int KEEP_ALIVE = 0x10000002;
    int HELLO = 0x10000003;
    int SET_MESS = 0x10000004;
    int LIST_MESS = 0x10000005;
    int LIST_RESP = 0x10000006;
    int GET_SCHED = 0x10000007;
    int MAKE_SCHED = 0x10000008;
    int DEL_SCHED = 0x10000009;
    int SET_HRS = 0x10000010;
    int GET_HRS = 0x10000011;
}
