/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */

package cz.vutbr.stud.fit.xsimon13.whoowns;

import cz.vutbr.stud.fit.xsimon13.whoowns.tasks.Logger;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Common message board for reporting warnings - singleton.
 */
public class MessageBoard {

    private static final int LAST_MESSAGES_CAPACITY = 100;

    private static MessageBoard instance = null;
    private MessageBoard() {
        // Prohibit creating an instance
    }

    public static MessageBoard getInstance() {
        if (instance == null)
            instance = new MessageBoard();
        return instance;
    }

    public static enum Types {
        RECOVERABLE_ERROR
    }

    Deque<String> lastMessages = new ArrayDeque<String>();

    public void sendMessage(Types type, String text) {
        if (lastMessages.size() > LAST_MESSAGES_CAPACITY)
            lastMessages.removeLast();
        lastMessages.addFirst(text);

        Logger.log(text);
    }

    public Deque<String> getLastMessages() {
        return lastMessages;
    }

    public void clearLastMessages() {
        lastMessages.clear();
    }

    public void sendRecoverableError(Throwable e) {
        sendRecoverableError("",  e);
    }

    public void sendRecoverableError(String message, Throwable e) {
        StackTraceElement[] stack = e.getStackTrace();
        String stackStr = "";
        for (StackTraceElement frame : stack)
            stackStr += frame.toString() + "\n";

        sendMessage(Types.RECOVERABLE_ERROR, message + " " + e.toString() + "\n" + stackStr);
    }
}
