package com.fullarmor.hapi;

/*****************************************************************
 <copyright file="MultipartRequest.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/

import static java.lang.System.currentTimeMillis;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.URLConnection.guessContentTypeFromName;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Helper class to upload a file in multi-part MIME format
 */
public class MultipartRequest {
    private static final Logger log = getLogger(MultipartRequest.class
            .getName());

    private static final String CRLF = "\r\n";
    private static final String CHARSET = "UTF-8";

    private static final int CONNECT_TIMEOUT = 45000;
    private static final int READ_TIMEOUT = 60000;

    private final OutputStream outputStream;
    private final PrintWriter writer;
    private final String boundary;

    // for log formatting only
     private final long start;

    public MultipartRequest(HttpURLConnection connection) throws IOException {
        start = currentTimeMillis();
        boundary = "---------------------------" + currentTimeMillis();
        // initialize the request
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        outputStream = connection.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET),
                true);
    }
    // adds a form field to the request
    public void addFormField(final String name, final String value) {
        writer.append("--").append(boundary).append(CRLF)
                .append("Content-Disposition: form-data; name=\"").append(name)
                .append("\"").append(CRLF).append("Content-Type: text/plain; charset=UTF-8")
                .append(CRLF).append(CRLF).append(value).append(CRLF);
    }
    // adds a file to the request
    public void addFilePart(final String fieldName, final File uploadFile)
            throws IOException {
        final String fileName = uploadFile.getName();
        writer.append("--").append(boundary).append(CRLF)
                .append("Content-Disposition: form-data; name=\"")
                .append(fieldName).append("\"; filename=\"").append(fileName)
                .append("\"").append(CRLF).append("Content-Type: ")
                .append(guessContentTypeFromName(fileName)).append(CRLF)
                .append("Content-Transfer-Encoding: binary").append(CRLF)
                .append(CRLF);

        writer.flush();
        outputStream.flush();
        final FileInputStream inputStream = new FileInputStream(uploadFile);
        try {
            final byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);}
            outputStream.flush();
        }
        finally {
            inputStream.close();
        }

        writer.append(CRLF);
    }
    // adds a chunk of file contents to the request
    public int addFilePartChunk(final String fieldName, final File uploadFile, long start, int chunkBytes)
            throws IOException {
        final String fileName = uploadFile.getName();
        writer.append("--").append(boundary).append(CRLF)
                .append("Content-Disposition: form-data; name=\"")
                .append(fieldName).append("\"; filename=\"").append(fileName)
                .append("\"").append(CRLF).append("Content-Type: ")
                .append(guessContentTypeFromName(fileName)).append(CRLF)
                .append("Content-Transfer-Encoding: binary").append(CRLF)
                .append(CRLF);

        writer.flush();
        outputStream.flush();
        int totalBytesWritten = 0;
        final FileInputStream inputStream = new FileInputStream(uploadFile);
        try {
            inputStream.skip(start);
            final byte[] buffer = new byte[4096];
            int bytesRead;
            int bytesLeftToRead = chunkBytes;
            while (bytesLeftToRead > 0 ) {
                int toRead = buffer.length;
                if (bytesLeftToRead < toRead)
                    toRead = bytesLeftToRead;

                bytesRead = inputStream.read(buffer, 0, toRead);
                if (bytesRead != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesWritten += bytesRead;
                } else {
                    break;
                }
            }
            outputStream.flush();
        }
        finally {
            inputStream.close();
        }

        writer.append(CRLF);
        return totalBytesWritten;
    }
    // adds a header to the request
    public void addHeaderField(String name, String value) {
        writer.append(name).append(": ").append(value).append(CRLF);
    }
    // must be called after all fields have been added to the request
    // adds the terminating boundary characters needed to complete the request format
    public void finish()  {
        writer.append(CRLF).append("--").append(boundary).append("--")
                .append(CRLF);
        writer.close();


    }
}