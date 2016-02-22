package com.zhaoxiaodan.mirserver.network;

import com.zhaoxiaodan.utils.StringUtils;
import io.netty.buffer.ByteBuf;

/**
 * 请求封包类, 封包数据格式: * #符号开头 + 头部 + body + !符号结尾
 * <pre>
 * +-----------------------------------------------------------------------------------------+
 * |  0  |  1  |  2  3  4  5  |  6  7  |  8  9  |  10  11  |  12  13  |  14 ..... n -1 |  n  |
 * +-----------------------------------------------------------------------------------------+
 * |  #  |              header                                        |      body      |  !  |
 * +-----------------------------------------------------------------------------------------+
 * |  #  |index|    p0     |  pid  |    p1  |    p2    |    p3    |      body      |  !  |
 * +-----------------------------------------------------------------------------------------+
 * </pre>
 * 不同类型的请求, body有各自的格式,比如login封包 body 用/符号分隔, 格式为:帐号/密码, 例如:
 * <pre>
 * +-------------------------------------------------+
 * |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 23 31 3c 3c 3c 3c 3c 49 40 43 3c 3c 3c 3c 3c 3c |#1<<<<<I@C<<<<<<|
 * |00000010| 3c 3c 48 4f 44 6f 47 6f 40 6e 48 6c 21          |<<HODoGo@nHl!   |
 * +--------+-------------------------------------------------+----------------+
 * 解密后得到:
 * +-------------------------------------------------+
 * |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 23 31 00 00 00 00 d1 07 00 00 00 00 00 00 31 32 |#1............12|
 * |00000010| 33 2f 31 32 00 00 00 00 00 00 00 00 21          |3/12........!   |
 * +--------+-------------------------------------------------+----------------+
 * </pre>
 * 装配后得到类:
 * <p>
 * Packet{  cmdIndx=2, p0=0, pid=2001, p1=0, p2=0, p3=0, body='123/123'}
 */
public class Packet {

	public static final int DEFAULT_HEADER_SIZE = 12;
	public static final String CONTENT_SPLIT_CHAR = "/";

	public int   p0;     // 未知
	public short pid;      // 协议id
	public short p1;        //
	public short p2;
	public short p3;

	public Packet() {
	}

	public Packet(int p0, Protocol protocol, short p1, short p2, short p3) {
		this.p0 = p0;
		this.pid = protocol.id;
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}

	public Packet(Protocol protocol) {
		this(0, protocol, (short) 0, (short) 0, (short) 0);
	}

	public void readPacket(ByteBuf in) throws WrongFormatException{
		p0 = in.readInt();
		pid = in.readShort();
		p1 = in.readShort();
		p2 = in.readShort();
		p3 = in.readShort();
	}

	public void writePacket(ByteBuf out) {
		out.writeInt(p0);
		out.writeShort(pid);
		out.writeShort(p1);
		out.writeShort(p2);
		out.writeShort(p3);
	}

	public String readString(ByteBuf in) {
		StringBuilder sb = new StringBuilder();
		while (in.readableBytes() > 0) {
			byte c = in.readByte();
			if (c < 0x08) {
				if (0 == sb.length()) // 开头就是 空
					continue;
				else {
					break;
				}
			} else {
				sb.append((char) c);
			}
		}

		return sb.toString().trim();
	}

	@Override
	public String toString() {
		return StringUtils.toString(this);
	}

	public class WrongFormatException extends Exception{
	}
}
