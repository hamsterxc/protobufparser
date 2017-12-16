package com.lonebytesoft.hamster.protobufparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

final class Reader {

    public static byte[] readBytes(final InputStream input, final int count) throws IOException {
        final byte[] result = new byte[count];

        int readTotal = 0;
        while(readTotal < count) {
            final int read = input.read(result, readTotal, count - readTotal);

            if(read == -1) {
                throw new IOException("Unexpected end of stream");
            }

            readTotal += read;
        }

        return result;
    }

    public static byte[] readVarintBytes(final InputStream input) throws IOException {
        final List<Byte> bytes = new ArrayList<>();
        long bitsRead = 0;
        for(;;) {
            final byte read = readBytes(input, 1)[0];

            if(bitsRead % 8 == 0) {
                bytes.add((byte) (read & 0x7F));
            } else {
                final int indexLast = bytes.size() - 1;
                final long bits = 8 - bitsRead % 8;
                bytes.set(indexLast, (byte) (bytes.get(indexLast) | ((read & (1 << bits - 1)) << (8 - bits))));
                bytes.add((byte) (read >> bits));
            }
            bitsRead += 7;

            if(read >= 0) {
                final int size = bytes.size();
                final byte[] result = new byte[size];
                IntStream.range(0, size).forEach(index -> result[index] = bytes.get(index));
                return result;
            }
        }
    }

    public static long readVarint(final InputStream input) throws IOException {
        // todo: no ZigZag
        return convertToNumber(readVarintBytes(input));
    }

    public static long convertToNumber(final byte[] data) {
        return IntStream.range(0, data.length)
                .mapToLong(index -> Byte.toUnsignedLong(data[data.length - 1 - index]))
                .reduce(0, (result, element) -> (result << 8) | element);
    }

    public static boolean hasMore(final PushbackInputStream input) throws IOException {
        final int next = input.read();
        if(next == -1) {
            return false;
        } else {
            input.unread(next);
            return true;
        }
    }

}
