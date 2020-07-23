package org.loed.framework;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author Thomason
 * @version 1.0
 * @since 2017/3/13.21:25
 */
public class FileChannelTest {
	@Test
	public void testZeroCopy() throws Exception {
		long start = System.currentTimeMillis();
		File file = new File("");
		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();
		FileOutputStream fos = new FileOutputStream("");
		WritableByteChannel writableByteChannel = Channels.newChannel(fos);
		channel.transferTo(0, channel.size(), writableByteChannel);
		long end = System.currentTimeMillis();
		System.out.println("copy cost:" + (end - start) + " millseconds");
	}
}
