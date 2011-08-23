/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package quanto.core.protocol;

import java.util.Collection;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import static quanto.core.protocol.Utils.*;

/**
 *
 * @author alemer
 */
public class RequestWriter
{
    private OutputStream output;
    private boolean inMessage = false;
    private boolean argNeedsClosing = false;
    public static final byte ESC = '\u001b';

    public RequestWriter(OutputStream output) {
        this.output = output;
    }

    private byte[] convertInt(int i)
    {
        return stringToAscii(Integer.toString(i));
    }

    public void addHeader(String code, String requestId) throws IOException
    {
        assert !inMessage;
        inMessage = true;
        addEscapedChar('<');
        output.write(stringToAscii(code));
        addEscapedChar(':');
        output.write(stringToUtf8(requestId));
        addEscapedChar('|');
    }

    private void closeArg() throws IOException
    {
        if (argNeedsClosing)
            addEscapedChar(';');
    }

    // only ASCII!!!
    private void addEscapedChar(char ch) throws IOException
    {
        assert ch < 128;
        assert inMessage;
        closeArg();
        output.write(ESC);
        output.write(ch);
    }

    public void addEmptyArg() throws IOException
    {
        assert inMessage;
        argNeedsClosing = true;
    }

    public void closeMessage() throws IOException
    {
        assert inMessage;
        argNeedsClosing = false;
        addEscapedChar('>');
        inMessage = false;
        output.flush();
    }

    private void addDataChunk(byte[] data) throws IOException
    {
        assert inMessage;
        closeArg();
        addEscapedChar('[');
        output.write(convertInt(data.length));
        addEscapedChar('|');
        output.write(data);
        addEscapedChar(']');
    }

    public void addDataChunkArg(byte[] data) throws IOException
    {
        addDataChunk(data);
        argNeedsClosing = true;
    }

    public void addDataChunkArg(String data) throws IOException
    {
        addDataChunk(stringToUtf8(data));
        argNeedsClosing = true;
    }

    public void addTaggedDataChunkArg(char tag, byte[] data) throws IOException
    {
        addEscapedChar(tag);
        addDataChunkArg(data);
    }

    public void addTaggedDataChunkArg(char tag, String data) throws IOException
    {
        addEscapedChar(tag);
        addDataChunkArg(data);
    }

    public void addStringArg(String data) throws IOException
    {
        assert inMessage;
        closeArg();
        output.write(stringToUtf8(data));
        argNeedsClosing = true;
    }

    public void addStringListArg(String[] items) throws IOException
    {
        addStringListArg(Arrays.asList(items));
    }

    public void addStringListArg(Collection<String> items) throws IOException
    {
        assert inMessage;
        closeArg();
        output.write(convertInt(items.size()));
        addEscapedChar(':');
        boolean first = true;
        for (String item : items) {
            if (!first) {
                addEscapedChar(',');
            }
            output.write(stringToUtf8(item));
            first = false;
        }
        argNeedsClosing = true;
    }

    public void addIntArg(int value) throws IOException
    {
        assert inMessage;
        closeArg();
        output.write(stringToAscii(Integer.toString(value)));
        argNeedsClosing = true;
    }
}
