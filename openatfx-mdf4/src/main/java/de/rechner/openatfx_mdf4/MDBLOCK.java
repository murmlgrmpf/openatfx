package de.rechner.openatfx_mdf4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;

import de.rechner.openatfx_mdf4.util.MDFUtil;


/**
 * <p>
 * THE META DATA BLOCK <code>MDBLOCK</code>
 * </p>
 * The MDBLOCK contains information encoded as XML string. For example this can be comments for the measured data file,
 * file history information or the identification of a channel. This information is ruled by the parent block and
 * follows specific XML schemas definitions.
 * 
 * @author Christian Rechner
 */
class MDBLOCK extends BLOCK {

    public static String BLOCK_ID = "##MD";

    /** Data section */

    // XML string
    // UTF-8 encoded, zero terminated, new line indicated by CR and LF.
    // CHAR
    private String mdData;

    /**
     * Constructor.
     */
    private MDBLOCK() {}

    public String getMdData() {
        return mdData;
    }

    private void setMdData(String mdData) {
        this.mdData = mdData;
    }

    @Override
    public String toString() {
        return "MDBLOCK [mdData=" + mdData + "]";
    }

    /**
     * Reads a HDBLOCK from the channel starting at current channel position.
     * 
     * @param channel The channel to read from.
     * @param pos The position within the channel.
     * @return The block data.
     * @throws IOException The exception.
     */
    public static MDBLOCK read(SeekableByteChannel channel, long pos) throws IOException {
        MDBLOCK hdBlock = new MDBLOCK();

        // read block header
        ByteBuffer bb = ByteBuffer.allocate(24);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        channel.position(pos);
        channel.read(bb);
        bb.rewind();

        // CHAR 4: Block type identifier, always "##HD"
        hdBlock.setId(MDFUtil.readCharsISO8859(bb, 4));
        if (!hdBlock.getId().equals(BLOCK_ID)) {
            throw new IOException("Wrong block type - expected '" + BLOCK_ID + "', found '" + hdBlock.getId() + "'");
        }

        // BYTE 4: Reserved used for 8-Byte alignment
        bb.get(new byte[4]);

        // UINT64: Length of block
        hdBlock.setLength(MDFUtil.readUInt64(bb));

        // UINT64: Number of links
        hdBlock.setLinkCount(MDFUtil.readUInt64(bb));

        // read block content
        bb = ByteBuffer.allocate((int) hdBlock.getLength() + 24);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        channel.position(pos + 24);
        channel.read(bb);
        bb.rewind();

        // XML String
        hdBlock.setMdData(MDFUtil.readCharsUTF8(bb, (int) (hdBlock.getLength() - 24)));

        return hdBlock;
    }

}