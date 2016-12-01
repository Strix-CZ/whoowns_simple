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

import java.io.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Common message board for reporting warnings - singleton.
 */
public class StandardOutputCommunicator {
    private static final String CHARSET = "UTF-8";

    public StandardOutputCommunicator(UnaryOperator<String> makeResponseTo) throws IOException {
        new MessageSplitter(System.in, CHARSET, message -> {
            String response = makeResponseTo.apply(message);
            sendMessage(response);
        });
    }

    private void sendMessage(String message) {
        try {
            byte[] messageBytes = message.getBytes(CHARSET);

            System.out.print(messageBytes.length);
            System.out.print(MessageSplitter.LENGTH_DELIMITER);
            System.out.write(messageBytes);
            System.out.flush();
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class MessageSplitter {
        private static final int MAX_LENGTH_DIGITS = 20;
        private static final char LENGTH_DELIMITER =  ':';

        private InputStream stream;
        private String charsetName;

        public MessageSplitter(InputStream stream, String charsetName, Consumer<String> onMessageReceived) throws IOException {
            this.stream = stream;
            this.charsetName = charsetName;

            while(true) {
                Integer messageLength = readMessageLength();
                if (messageLength == null)
                    return;
                String message = readMessage(messageLength);

                onMessageReceived.accept(message);
            }
        }

        private Integer readMessageLength() throws IOException {
            ByteArrayOutputStream lengthBuffer = new ByteArrayOutputStream();

            for (int i = 0; i < MAX_LENGTH_DIGITS; ++i) {
                int b = stream.read();
                if (b < 0)
                    return null;
                else if (b == LENGTH_DELIMITER)
                    return Integer.parseInt(lengthBuffer.toString(charsetName));
                else
                    lengthBuffer.write(b);
            }

            throw new RuntimeException("Cannot find next message length delimiter");
        }

        private String readMessage(int messageLength) throws IOException {
            ByteArrayOutputStream message = new ByteArrayOutputStream(messageLength + 4);

            for (int i = 0; i < messageLength; ++i) {
                int b = stream.read();
                if (b < 0)
                    throw new RuntimeException("Unexpected end of stream while reading a message");
                else
                    message.write(b);
            }

            return message.toString(charsetName);
        }
    }
}
