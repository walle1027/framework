package org.loed.framework.common.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class CompressUtils {
	private static final int BUFFER_SIZE = 4096;
	private static Logger logger = LoggerFactory.getLogger(CompressUtils.class);

	public static void zip(String inputFilename, String zipFilename, String encoding) throws IOException {
		zip(new File(inputFilename), zipFilename, encoding);
	}

	public static void zip(File inputFile, String zipFilename, String encoding) throws IOException {
		ZipOutputStream out = null;
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(zipFilename);
			out = new ZipOutputStream(outputStream);
			zip(inputFile, out, "", encoding);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(outputStream);
		}
	}


	public static void zip(String inputFilename, String zipFilename) throws IOException {
		zip(inputFilename, zipFilename, "GBK");
	}

	public static void zip(File inputFile, String zipFilename) throws IOException {
		ZipOutputStream out = null;
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(zipFilename);
			out = new ZipOutputStream(outputStream);
			zip(inputFile, zipFilename, "GBK");
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(outputStream);
		}
	}

	private static void zip(File inputFile, ZipOutputStream out, String base, String encoding) throws IOException {
		if (inputFile.isDirectory()) {
			File[] inputFiles = inputFile.listFiles();
			ZipEntry zipEntry = new ZipEntry(base + "/");
//			zipEntry.setUnixMode(755);
			if (StringUtils.isNotBlank(base)) {
				out.putNextEntry(zipEntry);
			}
			base = base.length() == 0 ? "" : base + "/";
			if (inputFiles != null) {
				for (File inputFile1 : inputFiles) {
					zip(inputFile1, out, base + inputFile1.getName(), encoding);
				}
			}
		} else {
			ZipEntry zipEntry;
			if (base.length() > 0) {
				zipEntry = new ZipEntry(base);
			} else {
				zipEntry = new ZipEntry(inputFile.getName());
			}
//			zipEntry.setUnixMode(644);
			out.putNextEntry(zipEntry);
			FileInputStream in = null;
			try {
				in = new FileInputStream(inputFile);
				int c;
				byte[] by = new byte[BUFFER_SIZE];
				while ((c = in.read(by)) != -1) {
					out.write(by, 0, c);
				}
			} finally {
				IOUtils.closeQuietly(in);
			}
			out.setEncoding(encoding);
			out.closeEntry();
		}
	}

	public static void unzip(String zipFilename, String outputDirectory) throws IOException {
		unzip(zipFilename, outputDirectory, "GBK");
	}

	public static void unzip(String zipFilename, String outputDirectory, String encoding) throws IOException {
		File outFile = new File(outputDirectory);
		if (!outFile.exists()) {
			outFile.mkdirs();
		}
		logger.debug("create unZipFile:" + zipFilename);
		ZipFile zipFile = new ZipFile(zipFilename, encoding);
		Enumeration en = zipFile.getEntries();
		ZipEntry zipEntry;
		while (en.hasMoreElements()) {
			zipEntry = (ZipEntry) en.nextElement();
			if (zipEntry.isDirectory()) {
				// mkdir directory
				String dirName = zipEntry.getName();
				dirName = dirName.substring(0, dirName.length() - 1);

				File f = new File(outFile.getPath() + File.separator + dirName);
				f.mkdirs();
			} else {
				// unzip file
				File f = new File(outFile.getPath() + File.separator + zipEntry.getName());
				if (!f.getParentFile().exists()) {
					f.getParentFile().mkdirs();
				}
				f.createNewFile();
				FileOutputStream out = null;
				InputStream in = null;
				try {
					out = new FileOutputStream(f);
					in = zipFile.getInputStream(zipEntry);
					int c;
					byte[] by = new byte[BUFFER_SIZE];
					while ((c = in.read(by)) != -1) {
						out.write(by, 0, c);
					}
				} finally {
					IOUtils.closeQuietly(out);
					IOUtils.closeQuietly(in);
				}
			}
		}
	}
}
