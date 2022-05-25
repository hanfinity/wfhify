package org.example;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class WfhifyServerHandler extends IoHandlerAdapter {
    @Override
    public void exceptionCaught(IoSession session, Throwable cause ) throws Exception {

    }

    @Override
    public void messageReceived( IoSession session, Object message ) throws Exception {

    }
}
