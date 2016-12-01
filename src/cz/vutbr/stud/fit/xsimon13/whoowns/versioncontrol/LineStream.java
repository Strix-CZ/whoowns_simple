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

package cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol;

import java.io.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LineStream implements Iterable<String>, Iterator<String>, Closeable {

    private BufferedReader reader;
    private String nextLine = null;
    private Closeable toClose = null;

    public LineStream(InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public LineStream(InputStream inputStream, Closeable toClose) {
        this(inputStream);
        this.toClose = toClose;
    }

    public String readAll() {
        StringBuilder sb = new StringBuilder();
        for (String line : this) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return fetchNextLine() != null;
    }

    @Override
    public String next() throws NoSuchElementException  {
        String line = fetchNextLine();
        if (line == null)
            throw new NoSuchElementException();
        nextLine = null; // we are consuming the next line
        return line;
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        reader.close();
        if (toClose != null)
            toClose.close();
    }

    private String fetchNextLine() {
        try {
            if (nextLine == null)
                nextLine = reader.readLine();
            return nextLine;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LineStream readProcess(final Process process) {

        return new LineStream(process.getInputStream(), new Closeable(){
            @Override
            public void close() {
                try {
                    process.waitFor();
                }
                catch (InterruptedException e) {
                    new RuntimeException(e);
                }
            }
        });
    }
}
